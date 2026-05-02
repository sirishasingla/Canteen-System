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
}