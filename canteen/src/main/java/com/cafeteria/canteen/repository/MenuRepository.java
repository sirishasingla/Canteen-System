package com.cafeteria.canteen.repository;

import com.cafeteria.canteen.entity.Menu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MenuRepository extends JpaRepository<Menu, Long> {

    @Query("SELECT m FROM Menu m WHERE m.isActive = true " +
           "ORDER BY CASE WHEN m.displayOrder IS NULL THEN 1 ELSE 0 END, m.displayOrder ASC, m.id ASC")
    List<Menu> findByIsActiveTrue();

    @Query("SELECT DISTINCT m FROM Menu m JOIN m.meals meal " +
           "WHERE meal.id = :mealId AND m.isActive = true " +
           "ORDER BY CASE WHEN m.displayOrder IS NULL THEN 1 ELSE 0 END, m.displayOrder ASC, m.id ASC")
    List<Menu> findActiveByMealId(@Param("mealId") Long mealId);

    @Query("SELECT m FROM Menu m " +
           "ORDER BY CASE WHEN m.displayOrder IS NULL THEN 1 ELSE 0 END, m.displayOrder ASC, m.id ASC")
    List<Menu> findAllOrdered();
}
