package com.cafeteria.canteen.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemResponse {
    private Long itemId;
    private String itemName;
    private Integer quantity;
    private Double price;
    private Double totalPrice;
}