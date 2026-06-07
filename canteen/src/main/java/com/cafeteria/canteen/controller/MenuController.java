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
     * Get all active menu items (no time restriction)
     * GET /api/menu/items
     */
    @GetMapping("/items")
    public ResponseEntity<List<MenuResponse>> getAllActiveItems() {
        List<MenuResponse> menu = menuService.getAllActiveItems();
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
}