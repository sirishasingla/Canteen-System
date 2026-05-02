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
    
    // Find orders by employee ID
    List<Orders> findByEmployeeId(Long employeeId);
    
    // Find orders by date range
    List<Orders> findByOrderTimeBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    // Find orders by employee and date range
    List<Orders> findByEmployeeIdAndOrderTimeBetween(Long employeeId, LocalDateTime startDate, LocalDateTime endDate);
    
    // Find orders by employee object and date range
    List<Orders> findByEmployeeAndOrderTimeBetween(Employee employee, LocalDateTime startDate, LocalDateTime endDate);
    
    // Find orders by meal ID
    List<Orders> findByMealId(Long mealId);
    
    // Custom query to get total amount spent by employee in a date range
    @Query("SELECT SUM(o.totalAmount) FROM Orders o WHERE o.employee.id = :employeeId AND o.orderTime BETWEEN :startDate AND :endDate")
    Double getTotalAmountByEmployeeAndDateRange(@Param("employeeId") Long employeeId,
                                                 @Param("startDate") LocalDateTime startDate,
                                                 @Param("endDate") LocalDateTime endDate);
    
    // Find orders with order items eagerly loaded for detailed reports
    @Query("SELECT DISTINCT o FROM Orders o " +
           "LEFT JOIN FETCH o.orderItems oi " +
           "LEFT JOIN FETCH oi.menu " +
           "LEFT JOIN FETCH o.employee " +
           "LEFT JOIN FETCH o.meal " +
           "WHERE o.orderTime BETWEEN :startDate AND :endDate")
    List<Orders> findOrdersWithItemsByOrderTimeBetween(@Param("startDate") LocalDateTime startDate,
                                                        @Param("endDate") LocalDateTime endDate);
}