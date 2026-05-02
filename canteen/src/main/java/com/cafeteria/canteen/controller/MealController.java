package com.cafeteria.canteen.controller;

import com.cafeteria.canteen.entity.Meal;
import com.cafeteria.canteen.service.MealService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
}