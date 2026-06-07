package com.cafeteria.canteen.service;

import com.cafeteria.canteen.entity.Meal;
import com.cafeteria.canteen.enums.MealType;
import com.cafeteria.canteen.repository.MealRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MealService {
    
    private final MealRepository mealRepository;
    
    /**
     * Get the current meal based on the current time
     */
    public Meal getCurrentMeal() {
        LocalTime currentTime = LocalTime.now();
        List<Meal> meals = mealRepository.findAll();
        
        for (Meal meal : meals) {
            if (isTimeInRange(currentTime, meal.getStartTime(), meal.getEndTime())) {
                return meal;
            }
        }
        
        throw new RuntimeException("No meal is currently being served at this time");
    }
    
    /**
     * Check if current time is within meal time range
     */
    private boolean isTimeInRange(LocalTime current, LocalTime start, LocalTime end) {
        return !current.isBefore(start) && !current.isAfter(end);
    }
    
    /**
     * Get meal by type
     */
    public Meal getMealByType(MealType mealType) {
        return mealRepository.findByType(mealType)
                .orElseThrow(() -> new RuntimeException("Meal type not found: " + mealType));
    }
    
    /**
     * Get all meals
     */
    public List<Meal> getAllMeals() {
        return mealRepository.findAll();
    }
    
    /**
     * Create a new meal
     */
    public Meal createMeal(Meal meal) {
        // Check if meal type already exists
        if (mealRepository.findByType(meal.getType()).isPresent()) {
            throw new RuntimeException("Meal type " + meal.getType() + " already exists");
        }
        
        // Validate time range
        if (meal.getStartTime().isAfter(meal.getEndTime())) {
            throw new RuntimeException("Start time must be before end time");
        }
        
        return mealRepository.save(meal);
    }
    
    /**
     * Update an existing meal
     */
    public Meal updateMeal(Long id, Meal mealData) {
        Meal existingMeal = mealRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Meal not found with ID: " + id));
        
        // Validate time range
        if (mealData.getStartTime().isAfter(mealData.getEndTime())) {
            throw new RuntimeException("Start time must be before end time");
        }
        
        // Update fields
        existingMeal.setStartTime(mealData.getStartTime());
        existingMeal.setEndTime(mealData.getEndTime());
        // Note: We don't update the type as it's unique and shouldn't change
        
        return mealRepository.save(existingMeal);
    }
}