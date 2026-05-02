package com.cafeteria.canteen.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for per-employee purchase summary report
 * Consolidates all purchases by employee during a time period
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeePurchaseReportDTO {
    private String employeeId;
    private String employeeName;
    private String department;
    private Integer totalOrders;
    private Integer totalItems;
    private Double totalCost;
}