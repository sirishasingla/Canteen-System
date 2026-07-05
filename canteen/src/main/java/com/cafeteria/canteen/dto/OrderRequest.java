package com.cafeteria.canteen.dto;

import com.cafeteria.canteen.enums.CustomerType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
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
    private String purpose;
    private Integer guestCount;
    private Integer companyEmployeeCount;

    // Admin-only: backdated / retroactive order time. Kiosk POST ignores this.
    private LocalDateTime orderTime;
}