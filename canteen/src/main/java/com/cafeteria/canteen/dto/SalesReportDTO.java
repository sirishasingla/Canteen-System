package com.cafeteria.canteen.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SalesReportDTO {
    private Long orderId;
    private LocalDateTime orderTime;
    private String customerType;
    private String employeeId;
    private String employeeName;
    private String mealType;
    private Double totalAmount;
    private Integer itemCount;
}