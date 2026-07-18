package com.cafeteria.canteen.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MenuResponse {
    private Long id;
    private String itemName;
    /** Effective price for the requesting caller (already role-resolved). */
    private Double price;
    /** Raw per-audience prices (nullable). Included so admin UI can render them. */
    private Double staffPrice;
    private Double workerPrice;
    private Double outsiderPrice;
    private Integer displayOrder;
    private List<String> mealTypes;
}
