package com.cafeteria.canteen.controller;

import com.cafeteria.canteen.entity.Meal;
import com.cafeteria.canteen.enums.MealType;
import com.cafeteria.canteen.repository.MealRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AdminController {
    
    private final MealRepository mealRepository;
    
    /**
     * Update meal time
     * PUT /api/admin/meals/{mealType}/time?startTime=15:00&endTime=17:00
     */
    @PutMapping("/meals/{mealType}/time")
    public ResponseEntity<Meal> updateMealTime(
            @PathVariable MealType mealType,
            @RequestParam String startTime,
            @RequestParam String endTime) {
        
        Meal meal = mealRepository.findByType(mealType)
                .orElseThrow(() -> new RuntimeException("Meal not found: " + mealType));
        
        meal.setStartTime(LocalTime.parse(startTime));
        meal.setEndTime(LocalTime.parse(endTime));
        
        Meal updated = mealRepository.save(meal);
        return ResponseEntity.ok(updated);
    }
    
    /**
     * Get all meals
     * GET /api/admin/meals
     */
    @GetMapping("/meals")
    public ResponseEntity<List<Meal>> getAllMeals() {
        return ResponseEntity.ok(mealRepository.findAll());
    }
}