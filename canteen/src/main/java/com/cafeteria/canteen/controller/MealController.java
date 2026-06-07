package com.cafeteria.canteen.controller;

import com.cafeteria.canteen.entity.Meal;
import com.cafeteria.canteen.service.MealService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/meals")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class MealController {
    
    private final MealService mealService;
    
    /**
     * Get current meal based on time
     * GET /api/meals/current
     */
    @GetMapping("/current")
    public ResponseEntity<Meal> getCurrentMeal() {
        Meal meal = mealService.getCurrentMeal();
        return ResponseEntity.ok(meal);
    }
    
    /**
     * Get all meals
     * GET /api/meals
     */
    @GetMapping
    public ResponseEntity<List<Meal>> getAllMeals() {
        List<Meal> meals = mealService.getAllMeals();
        return ResponseEntity.ok(meals);
    }
    
    /**
     * Create a new meal
     * POST /api/meals
     */
    @PostMapping
    public ResponseEntity<?> createMeal(@RequestBody Meal meal) {
        try {
            Meal savedMeal = mealService.createMeal(meal);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of("success", true, "message", "Meal created successfully", "meal", savedMeal));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }
    
    /**
     * Update an existing meal
     * PUT /api/meals/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateMeal(@PathVariable Long id, @RequestBody Meal meal) {
        try {
            Meal updatedMeal = mealService.updateMeal(id, meal);
            return ResponseEntity.ok(Map.of("success", true, "message", "Meal updated successfully", "meal", updatedMeal));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }
}