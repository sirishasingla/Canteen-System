package com.cafeteria.canteen.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeCostDTO {
    private String employeeId;
    private String employeeName;
    private String department;
    private Double totalCost;
    private Integer orderCount;
}