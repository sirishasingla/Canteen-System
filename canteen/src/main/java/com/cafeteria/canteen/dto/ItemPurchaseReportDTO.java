package com.cafeteria.canteen.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for per-item purchase statistics report
 * Shows how many times each item was purchased during a time period
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemPurchaseReportDTO {
    private String itemName;
    private String category;
    private Integer totalQuantity;
    private Integer numberOfOrders;
    private Double totalRevenue;
    private Double averagePrice;
}