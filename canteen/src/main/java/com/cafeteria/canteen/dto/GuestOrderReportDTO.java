package com.cafeteria.canteen.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for the standalone Guest Orders report.
 * One row per guest order; includes host details, purpose, headcount, and total.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GuestOrderReportDTO {
    private Long orderId;
    private LocalDateTime orderTime;
    private String hostEmployeeId;
    private String hostEmployeeName;
    private String hostDepartment;
    private String purpose;
    private Integer guestCount;
    private Integer companyEmployeeCount;
    private Integer totalItems;
    private Double totalAmount;
}
