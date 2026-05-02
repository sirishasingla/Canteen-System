package com.cafeteria.canteen.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MealCountDTO {
    private LocalDate date;
    private String mealType;
    private Long orderCount;
    private Double totalRevenue;
}