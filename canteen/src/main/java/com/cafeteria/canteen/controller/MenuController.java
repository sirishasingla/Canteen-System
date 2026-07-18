package com.cafeteria.canteen.controller;

import com.cafeteria.canteen.dto.MenuResponse;
import com.cafeteria.canteen.entity.Menu;
import com.cafeteria.canteen.service.MenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/menu")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class MenuController {
    
    private final MenuService menuService;
    
    /**
     * Get all active menu items (no time restriction). Pass customerType (+empId
     * for EMPLOYEE) to get a menu filtered + priced for that audience.
     * GET /api/menu/items?customerType=EMPLOYEE&empId=...
     */
    @GetMapping("/items")
    public ResponseEntity<List<MenuResponse>> getAllActiveItems(
            @RequestParam(required = false) String customerType,
            @RequestParam(required = false) String empId) {
        List<MenuResponse> menu = menuService.getAllActiveItems(customerType, empId);
        return ResponseEntity.ok(menu);
    }

    /**
     * Get ALL menu items including inactive (for management)
     * GET /api/menu/all
     */
    @GetMapping("/all")
    public ResponseEntity<List<Menu>> getAllMenuItems() {
        List<Menu> menu = menuService.getAllMenuItems();
        return ResponseEntity.ok(menu);
    }

    /**
     * Get menu items for current meal time
     * GET /api/menu/current?customerType=...&empId=...
     */
    @GetMapping("/current")
    public ResponseEntity<List<MenuResponse>> getCurrentMealMenu(
            @RequestParam(required = false) String customerType,
            @RequestParam(required = false) String empId) {
        List<MenuResponse> menu = menuService.getCurrentMealMenu(customerType, empId);
        return ResponseEntity.ok(menu);
    }

    /**
     * Get menu items by meal ID
     * GET /api/menu/meal/{mealId}?customerType=...&empId=...
     */
    @GetMapping("/meal/{mealId}")
    public ResponseEntity<List<MenuResponse>> getMenuByMealId(@PathVariable Long mealId,
                                                              @RequestParam(required = false) String customerType,
                                                              @RequestParam(required = false) String empId) {
        List<MenuResponse> menu = menuService.getMenuByMealId(mealId, customerType, empId);
        return ResponseEntity.ok(menu);
    }
    
    /**
     * Create a new menu item
     * POST /api/menu
     */
    @PostMapping
    public ResponseEntity<?> createMenuItem(@RequestBody Map<String, Object> menuData) {
        try {
            Menu savedMenu = menuService.createMenuItem(menuData);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of("success", true, "message", "Menu item created successfully", "menu", savedMenu));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }
    
    /**
     * Update an existing menu item
     * PUT /api/menu/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateMenuItem(@PathVariable Long id, @RequestBody Map<String, Object> menuData) {
        try {
            Menu updatedMenu = menuService.updateMenuItem(id, menuData);
            return ResponseEntity.ok(Map.of("success", true, "message", "Menu item updated successfully", "menu", updatedMenu));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }
    
    /**
     * Toggle menu item active status
     * POST /api/menu/{id}/toggle-status
     */
    @PostMapping("/{id}/toggle-status")
    public ResponseEntity<?> toggleMenuItemStatus(@PathVariable Long id) {
        try {
            Menu menu = menuService.toggleMenuItemStatus(id);
            String status = menu.getIsActive() ? "enabled" : "disabled";
            return ResponseEntity.ok(Map.of("success", true, "message", "Menu item " + status + " successfully", "menu", menu));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    /**
     * Move a menu item one position up or down in the admin display order.
     * POST /api/menu/{id}/move?direction=up|down
     */
    @PostMapping("/{id}/move")
    public ResponseEntity<?> moveMenuItem(@PathVariable Long id,
                                          @RequestParam String direction) {
        try {
            Menu menu = menuService.moveMenuItem(id, direction);
            return ResponseEntity.ok(Map.of("success", true, "message", "Menu item moved", "menu", menu));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    /**
     * Bulk reorder — accepts a full ordered list of menu IDs.
     * Body: {"orderedIds": [3, 1, 2, ...]}
     * POST /api/menu/reorder
     */
    @PostMapping("/reorder")
    public ResponseEntity<?> reorderMenuItems(@RequestBody Map<String, Object> body) {
        try {
            @SuppressWarnings("unchecked")
            List<Object> raw = (List<Object>) body.get("orderedIds");
            if (raw == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("success", false, "message", "orderedIds is required"));
            }
            List<Long> ids = raw.stream().map(o -> ((Number) o).longValue()).toList();
            menuService.reorderMenuItems(ids);
            return ResponseEntity.ok(Map.of("success", true, "message", "Menu reordered"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }
}