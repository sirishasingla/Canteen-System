package com.cafeteria.canteen.dto;

import com.cafeteria.canteen.enums.CustomerType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderRequest {
    
    // Common fields
    private CustomerType customerType;
    private List<OrderItemRequest> items;
    
    // For EMPLOYEE type
    private String empId;
    
    // For OUTSIDER type
    private String outsiderName;
    
    // For GUEST type
    private String hostEmpId;
    private String teamName;
    private Integer guestCount;
}