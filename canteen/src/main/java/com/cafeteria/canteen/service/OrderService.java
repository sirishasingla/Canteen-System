package com.cafeteria.canteen.service;

import com.cafeteria.canteen.dto.*;
import com.cafeteria.canteen.entity.*;
import com.cafeteria.canteen.enums.CustomerType;
import com.cafeteria.canteen.repository.EmployeeRepository;
import com.cafeteria.canteen.repository.OrderRepository;
import com.cafeteria.canteen.util.EmpIdUtil;
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
     * Create a new order (kiosk flow). Always stamped with the current time.
     */
    @Transactional
    public OrderResponse createOrder(OrderRequest request) {
        Orders order = new Orders();
        order.setOrderTime(LocalDateTime.now());
        order.setMeal(getCurrentMealOrNull());
        populateCustomerFields(order, request, true);
        populateItemsAndTotal(order, request);
        return convertToOrderResponse(orderRepository.save(order));
    }

    /**
     * Admin flow: create an order (possibly backdated). Skips the active-employee check
     * because admins may need to record historical orders for inactive employees.
     */
    @Transactional
    public OrderResponse createOrderAsAdmin(OrderRequest request) {
        Orders order = new Orders();
        order.setOrderTime(request.getOrderTime() != null ? request.getOrderTime() : LocalDateTime.now());
        order.setMeal(getCurrentMealOrNull());
        populateCustomerFields(order, request, false);
        populateItemsAndTotal(order, request);
        return convertToOrderResponse(orderRepository.save(order));
    }

    /**
     * Admin flow: fully update an existing order. Replaces items and customer info.
     */
    @Transactional
    public OrderResponse updateOrder(Long orderId, OrderRequest request) {
        Orders order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));

        if (request.getOrderTime() != null) {
            order.setOrderTime(request.getOrderTime());
        }
        // Wipe customer-specific fields so switching type doesn't leave stale data
        order.setEmployee(null);
        order.setOutsiderName(null);
        order.setHostEmployee(null);
        order.setPurpose(null);
        order.setGuestCount(null);
        order.setCompanyEmployeeCount(null);
        order.setCustomerType(request.getCustomerType());
        populateCustomerFields(order, request, false);

        // Replace items — clear then re-add so orphanRemoval fires
        order.getOrderItems().clear();
        populateItemsAndTotal(order, request);

        return convertToOrderResponse(orderRepository.save(order));
    }

    /**
     * Soft delete — flags order as cancelled. Reports exclude cancelled orders.
     */
    @Transactional
    public OrderResponse cancelOrder(Long orderId) {
        Orders order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));
        order.setIsCancelled(true);
        return convertToOrderResponse(orderRepository.save(order));
    }

    /**
     * Reactivate a soft-deleted order.
     */
    @Transactional
    public OrderResponse restoreOrder(Long orderId) {
        Orders order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));
        order.setIsCancelled(false);
        return convertToOrderResponse(orderRepository.save(order));
    }

    /**
     * Admin: list orders (any state) in date range, including cancelled.
     */
    public List<OrderResponse> getAllOrdersInRange(LocalDateTime startDate, LocalDateTime endDate) {
        return orderRepository.findByOrderTimeBetween(startDate, endDate).stream()
                .map(this::convertToOrderResponse)
                .collect(Collectors.toList());
    }

    private Meal getCurrentMealOrNull() {
        try {
            return mealService.getCurrentMeal();
        } catch (Exception e) {
            return null;
        }
    }

    private void populateCustomerFields(Orders order, OrderRequest request, boolean enforceActive) {
        order.setCustomerType(request.getCustomerType());
        switch (request.getCustomerType()) {
            case EMPLOYEE:
                Employee employee = employeeRepository.findByEmpId(EmpIdUtil.normalize(request.getEmpId()))
                        .orElseThrow(() -> new RuntimeException("Employee not found with ID: " + request.getEmpId()));
                if (enforceActive && !employee.getIsActive()) {
                    throw new RuntimeException("Cannot place order: Employee " + employee.getName() +
                            " (" + employee.getEmpId() + ") is currently inactive. Please contact admin.");
                }
                order.setEmployee(employee);
                break;

            case OUTSIDER:
                order.setOutsiderName(request.getOutsiderName());
                break;

            case GUEST:
                if (request.getPurpose() == null || request.getPurpose().trim().isEmpty()) {
                    throw new RuntimeException("Purpose is required for guest orders");
                }
                Employee hostEmployee = employeeRepository.findByEmpId(EmpIdUtil.normalize(request.getHostEmpId()))
                        .orElseThrow(() -> new RuntimeException("Host employee not found with ID: " + request.getHostEmpId()));
                if (enforceActive && !hostEmployee.getIsActive()) {
                    throw new RuntimeException("Cannot place order: Host employee " + hostEmployee.getName() +
                            " (" + hostEmployee.getEmpId() + ") is currently inactive. Please contact admin.");
                }
                order.setHostEmployee(hostEmployee);
                order.setPurpose(request.getPurpose().trim());
                order.setGuestCount(request.getGuestCount());
                order.setCompanyEmployeeCount(request.getCompanyEmployeeCount());
                break;
        }
    }

    private void populateItemsAndTotal(Orders order, OrderRequest request) {
        double totalAmount = 0.0;
        if (order.getOrderItems() == null) {
            order.setOrderItems(new ArrayList<>());
        }
        for (OrderItemRequest itemRequest : request.getItems()) {
            Menu menuItem = menuService.getMenuById(itemRequest.getMenuId());
            OrderItems orderItem = new OrderItems();
            orderItem.setOrder(order);
            orderItem.setMenu(menuItem);
            orderItem.setQuantity(itemRequest.getQuantity());
            orderItem.setPrice(menuItem.getPrice());
            order.getOrderItems().add(orderItem);
            totalAmount += menuItem.getPrice() * itemRequest.getQuantity();
        }
        order.setTotalAmount(totalAmount);
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
        Employee employee = employeeRepository.findByEmpId(EmpIdUtil.normalize(empId))
                .orElseThrow(() -> new RuntimeException("Employee not found with ID: " + empId));
        
        List<Orders> orders = orderRepository.findByEmployeeIdAndIsCancelledFalse(employee.getId());
        return orders.stream()
                .map(this::convertToOrderResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Get orders by date range
     */
    public List<OrderResponse> getOrdersByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        List<Orders> orders = orderRepository.findByOrderTimeBetweenAndIsCancelledFalse(startDate, endDate);
        return orders.stream()
                .map(this::convertToOrderResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Get total amount spent by employee in date range
     */
    public Double getTotalAmountByEmployeeAndDateRange(String empId, LocalDateTime startDate, LocalDateTime endDate) {
        Employee employee = employeeRepository.findByEmpId(EmpIdUtil.normalize(empId))
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
                .totalAmount(order.getTotalAmount())
                .isCancelled(order.getIsCancelled());

        // Only set mealType if meal is associated
        if (order.getMeal() != null) {
            builder.mealType(order.getMeal().getType().toString());
        }

        // Set customer-specific fields
        if (order.getCustomerType() == CustomerType.EMPLOYEE && order.getEmployee() != null) {
            builder.employeeName(order.getEmployee().getName())
                   .employeeId(order.getEmployee().getEmpId());
        } else if (order.getCustomerType() == CustomerType.OUTSIDER) {
            builder.outsiderName(order.getOutsiderName());
        } else if (order.getCustomerType() == CustomerType.GUEST && order.getHostEmployee() != null) {
            builder.hostEmployeeName(order.getHostEmployee().getName())
                   .hostEmployeeId(order.getHostEmployee().getEmpId())
                   .purpose(order.getPurpose())
                   .guestCount(order.getGuestCount())
                   .companyEmployeeCount(order.getCompanyEmployeeCount());
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