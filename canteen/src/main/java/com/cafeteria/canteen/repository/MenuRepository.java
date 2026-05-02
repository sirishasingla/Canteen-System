package com.cafeteria.canteen.repository;

import com.cafeteria.canteen.entity.Menu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MenuRepository extends JpaRepository<Menu, Long> {
    List<Menu> findByMealIdAndIsActiveTrue(Long mealId);
    List<Menu> findByIsActiveTrue();
}