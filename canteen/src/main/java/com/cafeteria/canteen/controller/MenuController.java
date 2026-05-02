package com.cafeteria.canteen.controller;

import com.cafeteria.canteen.dto.MenuResponse;
import com.cafeteria.canteen.service.MenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/menu")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class MenuController {
    
    private final MenuService menuService;
    
    /**
     * Get all active menu items (no time restriction)
     * GET /api/menu/items
     */
    @GetMapping("/items")
    public ResponseEntity<List<MenuResponse>> getAllActiveItems() {
        List<MenuResponse> menu = menuService.getAllActiveItems();
        return ResponseEntity.ok(menu);
    }
    
    /**
     * Get menu items for current meal time
     * GET /api/menu/current
     */
    @GetMapping("/current")
    public ResponseEntity<List<MenuResponse>> getCurrentMealMenu() {
        List<MenuResponse> menu = menuService.getCurrentMealMenu();
        return ResponseEntity.ok(menu);
    }
    
    /**
     * Get menu items by meal ID
     * GET /api/menu/meal/{mealId}
     */
    @GetMapping("/meal/{mealId}")
    public ResponseEntity<List<MenuResponse>> getMenuByMealId(@PathVariable Long mealId) {
        List<MenuResponse> menu = menuService.getMenuByMealId(mealId);
        return ResponseEntity.ok(menu);
    }
}