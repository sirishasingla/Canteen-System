package com.cafeteria.canteen.dto;

import com.cafeteria.canteen.enums.CustomerType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderResponse {
    private Long orderId;
    private CustomerType customerType;
    private String employeeName;
    private String mealType;
    private LocalDateTime orderTime;
    private Double totalAmount;
    private List<OrderItemResponse> items;
    
    // For outsider
    private String outsiderName;
    
    // For guest
    private String hostEmployeeName;
    private String hostEmployeeId;
    private String purpose;
    private Integer guestCount;
    private Integer companyEmployeeCount;

    private String employeeId;
    private Boolean isCancelled;
}