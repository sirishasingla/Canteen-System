package com.cafeteria.canteen.service;

import com.cafeteria.canteen.dto.MenuResponse;
import com.cafeteria.canteen.entity.Employee;
import com.cafeteria.canteen.entity.Meal;
import com.cafeteria.canteen.entity.Menu;
import com.cafeteria.canteen.enums.CustomerType;
import com.cafeteria.canteen.enums.EmployeeRole;
import com.cafeteria.canteen.repository.EmployeeRepository;
import com.cafeteria.canteen.repository.MealRepository;
import com.cafeteria.canteen.repository.MenuRepository;
import com.cafeteria.canteen.util.EmpIdUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MenuService {

    /** Who the menu is being fetched for, drives per-audience filtering + pricing. */
    public enum Audience { STAFF, WORKER, OUTSIDER }

    private final MenuRepository menuRepository;
    private final MealService mealService;
    private final MealRepository mealRepository;
    private final EmployeeRepository employeeRepository;

    /**
     * Kiosk-visible items right now, in the admin-controlled display order.
     * An item is visible if:
     *   • it has no meals (always available), OR
     *   • at least one of its meals' time window contains the current time
     *
     * Audience filter: if any audience-specific price is set on an item, the item
     * is only visible to matching audiences (STAFF/WORKER/OUTSIDER). If none are
     * set, the item is universal.
     */
    public List<MenuResponse> getAllActiveItems(String customerType, String empId) {
        Audience audience = resolveAudience(customerType, empId);
        LocalTime now = LocalTime.now();
        return menuRepository.findByIsActiveTrue().stream()
                .filter(m -> isVisibleTo(m, audience))
                .filter(m -> isAvailableNow(m, now))
                .map(m -> toResponse(m, audience))
                .collect(Collectors.toList());
    }

    private boolean isAvailableNow(Menu m, LocalTime now) {
        if (m.getMeals() == null || m.getMeals().isEmpty()) return true;
        return m.getMeals().stream()
                .anyMatch(meal -> isTimeInRange(now, meal.getStartTime(), meal.getEndTime()));
    }

    /**
     * Menu items for the meal that is currently being served.
     */
    public List<MenuResponse> getCurrentMealMenu(String customerType, String empId) {
        Meal currentMeal = mealService.getCurrentMeal();
        return getMenuByMealId(currentMeal.getId(), customerType, empId);
    }

    /**
     * Menu items linked to a specific meal (audience-filtered).
     */
    public List<MenuResponse> getMenuByMealId(Long mealId, String customerType, String empId) {
        Audience audience = resolveAudience(customerType, empId);
        return menuRepository.findActiveByMealId(mealId).stream()
                .filter(m -> isVisibleTo(m, audience))
                .map(m -> toResponse(m, audience))
                .collect(Collectors.toList());
    }

    /**
     * All menu items including inactive (for management). No filter, no price resolution.
     * Returned in admin-controlled display order.
     */
    public List<Menu> getAllMenuItems() {
        return menuRepository.findAllOrdered();
    }

    public Menu getMenuById(Long menuId) {
        return menuRepository.findById(menuId)
                .orElseThrow(() -> new RuntimeException("Menu item not found with id: " + menuId));
    }

    /**
     * Effective price for the given audience. Falls back to base price when the
     * audience-specific price is missing. Called by OrderService when stamping a line-item price.
     */
    public Double effectivePriceFor(Menu menu, Audience audience) {
        if (audience == Audience.STAFF && menu.getStaffPrice() != null) return menu.getStaffPrice();
        if (audience == Audience.WORKER && menu.getWorkerPrice() != null) return menu.getWorkerPrice();
        if (audience == Audience.OUTSIDER && menu.getOutsiderPrice() != null) return menu.getOutsiderPrice();
        return menu.getPrice();
    }

    /**
     * Derives the audience from the request context.
     *   • EMPLOYEE + valid empId → STAFF or WORKER based on employee role.
     *   • OUTSIDER               → OUTSIDER.
     *   • GUEST / admin / unknown → null (base price, universal items only).
     */
    public Audience resolveAudience(String customerType, String empId) {
        if (customerType == null || customerType.isBlank()) return null;
        CustomerType type;
        try {
            type = CustomerType.valueOf(customerType.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
        return switch (type) {
            case EMPLOYEE -> resolveRoleAudience(empId);
            case OUTSIDER -> Audience.OUTSIDER;
            case GUEST -> null;
        };
    }

    private Audience resolveRoleAudience(String empId) {
        if (empId == null || empId.isBlank()) return null;
        return employeeRepository.findByEmpId(EmpIdUtil.normalize(empId))
                .map(Employee::getRole)
                .map(r -> r == EmployeeRole.STAFF ? Audience.STAFF : Audience.WORKER)
                .orElse(null);
    }

    private boolean isVisibleTo(Menu m, Audience audience) {
        boolean hasStaff = m.getStaffPrice() != null;
        boolean hasWorker = m.getWorkerPrice() != null;
        boolean hasOutsider = m.getOutsiderPrice() != null;
        boolean restricted = hasStaff || hasWorker || hasOutsider;
        if (!restricted) return true;
        if (audience == Audience.STAFF) return hasStaff;
        if (audience == Audience.WORKER) return hasWorker;
        if (audience == Audience.OUTSIDER) return hasOutsider;
        // No audience (admin/unknown) — don't leak restricted items on kiosk endpoints.
        return false;
    }

    private MenuResponse toResponse(Menu menu, Audience audience) {
        List<String> mealTypes = menu.getMeals() == null ? List.of() :
                menu.getMeals().stream()
                        .map(m -> m.getType().toString())
                        .sorted()
                        .collect(Collectors.toList());
        return MenuResponse.builder()
                .id(menu.getId())
                .itemName(menu.getItemName())
                .price(effectivePriceFor(menu, audience))
                .staffPrice(menu.getStaffPrice())
                .workerPrice(menu.getWorkerPrice())
                .outsiderPrice(menu.getOutsiderPrice())
                .displayOrder(menu.getDisplayOrder())
                .mealTypes(mealTypes)
                .build();
    }

    public Menu createMenuItem(Map<String, Object> menuData) {
        Menu menu = new Menu();
        menu.setItemName((String) menuData.get("itemName"));
        menu.setPrice(((Number) menuData.get("price")).doubleValue());
        menu.setStaffPrice(readOptionalPrice(menuData, "staffPrice"));
        menu.setWorkerPrice(readOptionalPrice(menuData, "workerPrice"));
        menu.setOutsiderPrice(readOptionalPrice(menuData, "outsiderPrice"));
        menu.setIsActive((Boolean) menuData.getOrDefault("isActive", true));
        menu.setMeals(resolveMeals(menuData));
        // New items land at the end of the list; use max(displayOrder) + 10 so admins can
        // slot other items in between without renumbering.
        menu.setDisplayOrder(nextDisplayOrder());
        return menuRepository.save(menu);
    }

    private Integer nextDisplayOrder() {
        return menuRepository.findAllOrdered().stream()
                .map(Menu::getDisplayOrder)
                .filter(o -> o != null)
                .max(Integer::compareTo)
                .map(max -> max + 10)
                .orElse(10);
    }

    public Menu updateMenuItem(Long id, Map<String, Object> menuData) {
        Menu existing = menuRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Menu item not found with ID: " + id));

        if (menuData.containsKey("itemName")) {
            existing.setItemName((String) menuData.get("itemName"));
        }
        if (menuData.containsKey("price")) {
            existing.setPrice(((Number) menuData.get("price")).doubleValue());
        }
        if (menuData.containsKey("staffPrice")) {
            existing.setStaffPrice(readOptionalPrice(menuData, "staffPrice"));
        }
        if (menuData.containsKey("workerPrice")) {
            existing.setWorkerPrice(readOptionalPrice(menuData, "workerPrice"));
        }
        if (menuData.containsKey("outsiderPrice")) {
            existing.setOutsiderPrice(readOptionalPrice(menuData, "outsiderPrice"));
        }
        if (menuData.containsKey("isActive")) {
            existing.setIsActive((Boolean) menuData.get("isActive"));
        }
        if (menuData.containsKey("mealIds") || menuData.containsKey("mealId")) {
            existing.setMeals(resolveMeals(menuData));
        }
        return menuRepository.save(existing);
    }

    public Menu toggleMenuItemStatus(Long id) {
        Menu menu = menuRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Menu item not found with ID: " + id));
        menu.setIsActive(!menu.getIsActive());
        return menuRepository.save(menu);
    }

    /**
     * Move an item one slot up or down in the admin-controlled display order.
     * Swaps its displayOrder with the immediate neighbour.
     * @param direction "up" or "down" — anything else is treated as a no-op
     */
    public Menu moveMenuItem(Long id, String direction) {
        List<Menu> all = menuRepository.findAllOrdered();
        // Backfill any nulls first so a swap always sees two real integers.
        backfillDisplayOrder(all);
        int idx = -1;
        for (int i = 0; i < all.size(); i++) {
            if (all.get(i).getId().equals(id)) { idx = i; break; }
        }
        if (idx < 0) throw new RuntimeException("Menu item not found with ID: " + id);
        int neighbourIdx = "up".equalsIgnoreCase(direction) ? idx - 1
                          : "down".equalsIgnoreCase(direction) ? idx + 1
                          : idx;
        if (neighbourIdx < 0 || neighbourIdx >= all.size() || neighbourIdx == idx) {
            return all.get(idx); // no-op: already at boundary
        }
        Menu current = all.get(idx);
        Menu neighbour = all.get(neighbourIdx);
        Integer tmp = current.getDisplayOrder();
        current.setDisplayOrder(neighbour.getDisplayOrder());
        neighbour.setDisplayOrder(tmp);
        menuRepository.save(neighbour);
        return menuRepository.save(current);
    }

    /** Assign display_order (in-place + persisted) to any rows still holding null. */
    private void backfillDisplayOrder(List<Menu> orderedList) {
        int next = 10;
        for (Menu m : orderedList) {
            if (m.getDisplayOrder() == null) {
                m.setDisplayOrder(next);
                menuRepository.save(m);
            }
            next = m.getDisplayOrder() + 10;
        }
    }

    /**
     * Bulk reorder: apply the given id sequence as the new display order (steps of 10).
     * IDs not in the list keep their current order values (appended after the reordered set).
     */
    @org.springframework.transaction.annotation.Transactional
    public void reorderMenuItems(List<Long> orderedIds) {
        if (orderedIds == null || orderedIds.isEmpty()) return;
        int step = 10;
        int next = step;
        for (Long id : orderedIds) {
            Menu m = menuRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Menu item not found with ID: " + id));
            m.setDisplayOrder(next);
            menuRepository.save(m);
            next += step;
        }
    }

    /**
     * Accepts `mealIds` (list) — preferred. Falls back to legacy single `mealId` for backwards
     * compatibility. Missing/empty means "always available".
     */
    private Set<Meal> resolveMeals(Map<String, Object> menuData) {
        Set<Meal> result = new HashSet<>();
        Object mealIdsObj = menuData.get("mealIds");
        if (mealIdsObj instanceof Collection<?> ids) {
            for (Object idObj : ids) {
                if (idObj == null) continue;
                Long mealId = ((Number) idObj).longValue();
                Meal meal = mealRepository.findById(mealId)
                        .orElseThrow(() -> new RuntimeException("Meal not found with ID: " + mealId));
                result.add(meal);
            }
            return result;
        }
        Object mealIdObj = menuData.get("mealId");
        if (mealIdObj != null && !mealIdObj.toString().isEmpty()) {
            Long mealId = ((Number) mealIdObj).longValue();
            Meal meal = mealRepository.findById(mealId)
                    .orElseThrow(() -> new RuntimeException("Meal not found with ID: " + mealId));
            result.add(meal);
        }
        return result;
    }

    private Double readOptionalPrice(Map<String, Object> data, String key) {
        Object v = data.get(key);
        if (v == null) return null;
        if (v instanceof String s && s.isBlank()) return null;
        return ((Number) v).doubleValue();
    }

    private boolean isTimeInRange(LocalTime current, LocalTime start, LocalTime end) {
        return !current.isBefore(start) && !current.isAfter(end);
    }
}
