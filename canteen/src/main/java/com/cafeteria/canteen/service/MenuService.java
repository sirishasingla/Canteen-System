package com.cafeteria.canteen.service;

import com.cafeteria.canteen.dto.MenuResponse;
import com.cafeteria.canteen.entity.Meal;
import com.cafeteria.canteen.entity.Menu;
import com.cafeteria.canteen.repository.MealRepository;
import com.cafeteria.canteen.repository.MenuRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MenuService {
    
    private final MenuRepository menuRepository;
    private final MealService mealService;
    private final MealRepository mealRepository;
    
    /**
     * Get active menu items for current meal
     */
    public List<MenuResponse> getCurrentMealMenu() {
        Meal currentMeal = mealService.getCurrentMeal();
        return getMenuByMealId(currentMeal.getId());
    }
    
    /**
     * Get menu items by meal ID
     */
    public List<MenuResponse> getMenuByMealId(Long mealId) {
        List<Menu> menuItems = menuRepository.findByMealIdAndIsActiveTrue(mealId);
        
        return menuItems.stream()
                .map(this::convertToMenuResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Get all active menu items (without meal time restriction)
     */
    public List<MenuResponse> getAllActiveItems() {
        List<Menu> menuItems = menuRepository.findByIsActiveTrue();
        
        return menuItems.stream()
                .map(this::convertToMenuResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Get ALL menu items including inactive (for management)
     */
    public List<Menu> getAllMenuItems() {
        return menuRepository.findAll();
    }
    
    /**
     * Get menu item by ID
     */
    public Menu getMenuById(Long menuId) {
        return menuRepository.findById(menuId)
                .orElseThrow(() -> new RuntimeException("Menu item not found with id: " + menuId));
    }
    
    /**
     * Convert Menu entity to MenuResponse DTO
     */
    private MenuResponse convertToMenuResponse(Menu menu) {
        MenuResponse.MenuResponseBuilder builder = MenuResponse.builder()
                .id(menu.getId())
                .itemName(menu.getItemName())
                .price(menu.getPrice());
        
        // Only set mealType if meal is associated
        if (menu.getMeal() != null) {
            builder.mealType(menu.getMeal().getType().toString());
        }
        
        return builder.build();
    }
    
    /**
     * Create a new menu item
     */
    public Menu createMenuItem(Map<String, Object> menuData) {
        Menu menu = new Menu();
        menu.setItemName((String) menuData.get("itemName"));
        menu.setPrice(((Number) menuData.get("price")).doubleValue());
        menu.setIsActive((Boolean) menuData.getOrDefault("isActive", true));
        
        // Set meal if mealId is provided
        if (menuData.get("mealId") != null) {
            Long mealId = ((Number) menuData.get("mealId")).longValue();
            Meal meal = mealRepository.findById(mealId)
                    .orElseThrow(() -> new RuntimeException("Meal not found with ID: " + mealId));
            menu.setMeal(meal);
        }
        
        return menuRepository.save(menu);
    }
    
    /**
     * Update an existing menu item
     */
    public Menu updateMenuItem(Long id, Map<String, Object> menuData) {
        Menu existingMenu = menuRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Menu item not found with ID: " + id));
        
        // Update fields
        if (menuData.containsKey("itemName")) {
            existingMenu.setItemName((String) menuData.get("itemName"));
        }
        if (menuData.containsKey("price")) {
            existingMenu.setPrice(((Number) menuData.get("price")).doubleValue());
        }
        if (menuData.containsKey("isActive")) {
            existingMenu.setIsActive((Boolean) menuData.get("isActive"));
        }
        
        // Update meal if mealId is provided
        if (menuData.containsKey("mealId")) {
            Object mealIdObj = menuData.get("mealId");
            if (mealIdObj != null && !mealIdObj.toString().isEmpty()) {
                Long mealId = ((Number) mealIdObj).longValue();
                Meal meal = mealRepository.findById(mealId)
                        .orElseThrow(() -> new RuntimeException("Meal not found with ID: " + mealId));
                existingMenu.setMeal(meal);
            } else {
                existingMenu.setMeal(null);
            }
        }
        
        return menuRepository.save(existingMenu);
    }
    
    /**
     * Toggle menu item active status (enable/disable)
     */
    public Menu toggleMenuItemStatus(Long id) {
        Menu menu = menuRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Menu item not found with ID: " + id));
        
        menu.setIsActive(!menu.getIsActive());
        return menuRepository.save(menu);
    }
}