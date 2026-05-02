package com.cafeteria.canteen.service;

import com.cafeteria.canteen.dto.*;
import com.cafeteria.canteen.entity.*;
import com.cafeteria.canteen.enums.CustomerType;
import com.cafeteria.canteen.repository.EmployeeRepository;
import com.cafeteria.canteen.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {
    
    private final OrderRepository orderRepository;
    private final EmployeeRepository employeeRepository;
    private final MealService mealService;
    private final MenuService menuService;
    
    /**
     * Create a new order
     */
    @Transactional
    public OrderResponse createOrder(OrderRequest request) {
        // Try to get current meal, but don't fail if none exists
        Meal currentMeal = null;
        try {
            currentMeal = mealService.getCurrentMeal();
        } catch (Exception e) {
            // No meal time restriction - meal can be null
            System.out.println("No current meal time - order without meal restriction");
        }
        
        // Create order entity
        Orders order = new Orders();
        order.setMeal(currentMeal);
        order.setOrderTime(LocalDateTime.now());
        order.setCustomerType(request.getCustomerType());
        
        // Set customer-specific fields based on type
        switch (request.getCustomerType()) {
            case EMPLOYEE:
                Employee employee = employeeRepository.findByEmpId(request.getEmpId())
                        .orElseThrow(() -> new RuntimeException("Employee not found with ID: " + request.getEmpId()));
                
                // Validate employee is active
                if (!employee.getIsActive()) {
                    throw new RuntimeException("Cannot place order: Employee " + employee.getName() +
                            " (" + employee.getEmpId() + ") is currently inactive. Please contact admin.");
                }
                
                order.setEmployee(employee);
                break;
                
            case OUTSIDER:
                order.setOutsiderName(request.getOutsiderName());
                break;
                
            case GUEST:
                Employee hostEmployee = employeeRepository.findByEmpId(request.getHostEmpId())
                        .orElseThrow(() -> new RuntimeException("Host employee not found with ID: " + request.getHostEmpId()));
                
                // Validate host employee is active
                if (!hostEmployee.getIsActive()) {
                    throw new RuntimeException("Cannot place order: Host employee " + hostEmployee.getName() +
                            " (" + hostEmployee.getEmpId() + ") is currently inactive. Please contact admin.");
                }
                
                order.setHostEmployee(hostEmployee);
                order.setTeamName(request.getTeamName());
                order.setGuestCount(request.getGuestCount());
                break;
        }
        
        // Calculate total amount and create order items
        double totalAmount = 0.0;
        List<OrderItems> orderItemsList = new ArrayList<>();
        
        for (OrderItemRequest itemRequest : request.getItems()) {
            Menu menuItem = menuService.getMenuById(itemRequest.getMenuId());
            
            OrderItems orderItem = new OrderItems();
            orderItem.setOrder(order);
            orderItem.setMenu(menuItem);
            orderItem.setQuantity(itemRequest.getQuantity());
            orderItem.setPrice(menuItem.getPrice());
            
            double itemTotal = menuItem.getPrice() * itemRequest.getQuantity();
            totalAmount += itemTotal;
            
            orderItemsList.add(orderItem);
        }
        
        order.setTotalAmount(totalAmount);
        order.setOrderItems(orderItemsList);
        
        // Save order (cascade will save order items)
        Orders savedOrder = orderRepository.save(order);
        
        // Convert to response
        return convertToOrderResponse(savedOrder);
    }
    
    /**
     * Get order by ID
     */
    public OrderResponse getOrderById(Long orderId) {
        Orders order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));
        return convertToOrderResponse(order);
    }
    
    /**
     * Get all orders for an employee
     */
    public List<OrderResponse> getOrdersByEmployee(String empId) {
        Employee employee = employeeRepository.findByEmpId(empId)
                .orElseThrow(() -> new RuntimeException("Employee not found with ID: " + empId));
        
        List<Orders> orders = orderRepository.findByEmployeeId(employee.getId());
        return orders.stream()
                .map(this::convertToOrderResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Get orders by date range
     */
    public List<OrderResponse> getOrdersByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        List<Orders> orders = orderRepository.findByOrderTimeBetween(startDate, endDate);
        return orders.stream()
                .map(this::convertToOrderResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Get total amount spent by employee in date range
     */
    public Double getTotalAmountByEmployeeAndDateRange(String empId, LocalDateTime startDate, LocalDateTime endDate) {
        Employee employee = employeeRepository.findByEmpId(empId)
                .orElseThrow(() -> new RuntimeException("Employee not found with ID: " + empId));
        
        Double total = orderRepository.getTotalAmountByEmployeeAndDateRange(employee.getId(), startDate, endDate);
        return total != null ? total : 0.0;
    }
    
    /**
     * Convert Orders entity to OrderResponse DTO
     */
    private OrderResponse convertToOrderResponse(Orders order) {
        OrderResponse.OrderResponseBuilder builder = OrderResponse.builder()
                .orderId(order.getId())
                .customerType(order.getCustomerType())
                .orderTime(order.getOrderTime())
                .totalAmount(order.getTotalAmount());
        
        // Only set mealType if meal is associated
        if (order.getMeal() != null) {
            builder.mealType(order.getMeal().getType().toString());
        }
        
        // Set customer-specific fields
        if (order.getCustomerType() == CustomerType.EMPLOYEE && order.getEmployee() != null) {
            builder.employeeName(order.getEmployee().getName());
        } else if (order.getCustomerType() == CustomerType.OUTSIDER) {
            builder.outsiderName(order.getOutsiderName());
        } else if (order.getCustomerType() == CustomerType.GUEST && order.getHostEmployee() != null) {
            builder.hostEmployeeName(order.getHostEmployee().getName())
                   .teamName(order.getTeamName())
                   .guestCount(order.getGuestCount());
        }
        
        // Convert order items
        List<OrderItemResponse> itemResponses = order.getOrderItems().stream()
                .map(this::convertToOrderItemResponse)
                .collect(Collectors.toList());
        
        builder.items(itemResponses);
        
        return builder.build();
    }
    
    /**
     * Convert OrderItems entity to OrderItemResponse DTO
     */
    private OrderItemResponse convertToOrderItemResponse(OrderItems orderItem) {
        return OrderItemResponse.builder()
                .itemId(orderItem.getId())
                .itemName(orderItem.getMenu().getItemName())
                .quantity(orderItem.getQuantity())
                .price(orderItem.getPrice())
                .totalPrice(orderItem.getPrice() * orderItem.getQuantity())
                .build();
    }
}