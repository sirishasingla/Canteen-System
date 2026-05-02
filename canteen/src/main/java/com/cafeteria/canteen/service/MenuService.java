package com.cafeteria.canteen.service;

import com.cafeteria.canteen.dto.MenuResponse;
import com.cafeteria.canteen.entity.Meal;
import com.cafeteria.canteen.entity.Menu;
import com.cafeteria.canteen.repository.MenuRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MenuService {
    
    private final MenuRepository menuRepository;
    private final MealService mealService;
    
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
}