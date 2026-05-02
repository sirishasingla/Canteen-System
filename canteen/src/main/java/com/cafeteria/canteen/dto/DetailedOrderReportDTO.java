package com.cafeteria.canteen.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for detailed consolidated order report
 * Contains all order details including items
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DetailedOrderReportDTO {
    private Long orderId;
    private LocalDateTime orderTime;
    private String customerType;
    private String customerId;
    private String customerName;
    private String mealType;
    private String itemName;
    private Integer quantity;
    private Double itemPrice;
    private Double itemTotal;
    private Double orderTotal;
}