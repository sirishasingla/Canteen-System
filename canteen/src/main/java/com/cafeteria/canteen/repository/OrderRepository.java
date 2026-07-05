package com.cafeteria.canteen.repository;

import com.cafeteria.canteen.entity.Employee;
import com.cafeteria.canteen.entity.Orders;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Orders, Long> {

    // Find orders by employee ID (excludes cancelled)
    List<Orders> findByEmployeeIdAndIsCancelledFalse(Long employeeId);

    // Find orders by date range (excludes cancelled — used for reports)
    List<Orders> findByOrderTimeBetweenAndIsCancelledFalse(LocalDateTime startDate, LocalDateTime endDate);

    // Find orders by date range INCLUDING cancelled (admin views)
    List<Orders> findByOrderTimeBetween(LocalDateTime startDate, LocalDateTime endDate);

    // Find orders by employee and date range (excludes cancelled)
    List<Orders> findByEmployeeAndOrderTimeBetweenAndIsCancelledFalse(Employee employee, LocalDateTime startDate, LocalDateTime endDate);

    // Find orders by meal ID
    List<Orders> findByMealId(Long mealId);

    // Custom query to get total amount spent by employee in a date range (excludes cancelled)
    @Query("SELECT SUM(o.totalAmount) FROM Orders o WHERE o.employee.id = :employeeId AND o.orderTime BETWEEN :startDate AND :endDate AND o.isCancelled = false")
    Double getTotalAmountByEmployeeAndDateRange(@Param("employeeId") Long employeeId,
                                                 @Param("startDate") LocalDateTime startDate,
                                                 @Param("endDate") LocalDateTime endDate);

    // Find orders with order items eagerly loaded for detailed reports (excludes cancelled)
    @Query("SELECT DISTINCT o FROM Orders o " +
           "LEFT JOIN FETCH o.orderItems oi " +
           "LEFT JOIN FETCH oi.menu " +
           "LEFT JOIN FETCH o.employee " +
           "LEFT JOIN FETCH o.meal " +
           "WHERE o.orderTime BETWEEN :startDate AND :endDate AND o.isCancelled = false")
    List<Orders> findOrdersWithItemsByOrderTimeBetween(@Param("startDate") LocalDateTime startDate,
                                                        @Param("endDate") LocalDateTime endDate);
}