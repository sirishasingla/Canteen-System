package com.cafeteria.canteen.repository;

import com.cafeteria.canteen.entity.Meal;
import com.cafeteria.canteen.enums.MealType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MealRepository extends JpaRepository<Meal, Long> {
    Optional<Meal> findByType(MealType type);
}