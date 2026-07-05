package com.cafeteria.canteen.entity;

import com.cafeteria.canteen.enums.CustomerType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Orders {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id")
    private Employee employee;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meal_id", nullable = true)
    private Meal meal;
    
    @Column(name = "order_time", nullable = false)
    private LocalDateTime orderTime;
    
    @Column(name = "total_amount", nullable = false)
    private Double totalAmount;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "customer_type", nullable = false)
    private CustomerType customerType;
    
    @Column(name = "outsider_name")
    private String outsiderName;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "host_employee_id")
    private Employee hostEmployee;
    
    @Column(name = "purpose")
    private String purpose;

    @Column(name = "guest_count")
    private Integer guestCount;

    @Column(name = "company_employee_count")
    private Integer companyEmployeeCount;

    @Column(name = "is_cancelled", nullable = false)
    private Boolean isCancelled = false;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItems> orderItems = new ArrayList<>();
}