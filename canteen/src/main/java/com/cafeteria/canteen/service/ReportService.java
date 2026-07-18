package com.cafeteria.canteen.service;

import com.cafeteria.canteen.dto.*;
import com.cafeteria.canteen.entity.Employee;
import com.cafeteria.canteen.entity.OrderItems;
import com.cafeteria.canteen.entity.Orders;
import com.cafeteria.canteen.enums.CustomerType;
import com.cafeteria.canteen.repository.EmployeeRepository;
import com.cafeteria.canteen.repository.OrderRepository;
import com.cafeteria.canteen.util.EmpIdUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {
    
    private final OrderRepository orderRepository;
    private final EmployeeRepository employeeRepository;
    
    /**
     * Get sales report between start and end date/time
     */
    public List<SalesReportDTO> getSalesReport(LocalDateTime startTime, LocalDateTime endTime) {
        List<Orders> orders = orderRepository.findByOrderTimeBetweenAndIsCancelledFalse(startTime, endTime);
        
        return orders.stream().map(order -> {
            SalesReportDTO dto = new SalesReportDTO();
            dto.setOrderId(order.getId());
            dto.setOrderTime(order.getOrderTime());
            dto.setCustomerType(order.getCustomerType().toString());
            dto.setMealType(order.getMeal().getType().toString());
            dto.setTotalAmount(order.getTotalAmount());
            dto.setItemCount(order.getOrderItems().size());
            
            if (order.getEmployee() != null) {
                dto.setEmployeeId(order.getEmployee().getEmpId());
                dto.setEmployeeName(order.getEmployee().getName());
            } else if (order.getOutsiderName() != null) {
                dto.setEmployeeName(order.getOutsiderName());
            } else if (order.getHostEmployee() != null) {
                dto.setEmployeeName("Guest of " + order.getHostEmployee().getName());
            }
            
            return dto;
        }).collect(Collectors.toList());
    }
    
    /**
     * Cost per employee. Only includes direct EMPLOYEE orders — guest orders
     * live in the dedicated Guest Orders report.
     */
    public List<EmployeeCostDTO> getEmployeeCostReport(LocalDate startDate, LocalDate endDate) {
        LocalDateTime startTime = startDate.atStartOfDay();
        LocalDateTime endTime = endDate.atTime(LocalTime.MAX);

        List<Orders> orders = orderRepository.findByOrderTimeBetweenAndIsCancelledFalse(startTime, endTime);

        Map<String, EmployeeCostData> employeeMap = new HashMap<>();

        for (Orders order : orders) {
            if (order.getCustomerType() != CustomerType.EMPLOYEE || order.getEmployee() == null) {
                continue;
            }
            Employee targetEmployee = order.getEmployee();
            String empId = targetEmployee.getEmpId();
            EmployeeCostData data = employeeMap.getOrDefault(empId,
                new EmployeeCostData(targetEmployee));
            data.addOrder(order.getTotalAmount());
            employeeMap.put(empId, data);
        }

        return employeeMap.values().stream()
            .map(data -> new EmployeeCostDTO(
                data.employee.getEmpId(),
                data.employee.getName(),
                data.employee.getDepartment(),
                data.totalCost,
                data.orderCount
            ))
            .collect(Collectors.toList());
    }
    
    /**
     * Get order history for a specific employee
     */
    public List<SalesReportDTO> getEmployeeOrderHistory(String empId, LocalDate startDate, LocalDate endDate) {
        Employee employee = employeeRepository.findByEmpId(EmpIdUtil.normalize(empId))
            .orElseThrow(() -> new RuntimeException("Employee not found with ID: " + empId));
        
        LocalDateTime startTime = startDate.atStartOfDay();
        LocalDateTime endTime = endDate.atTime(LocalTime.MAX);
        
        List<Orders> orders = orderRepository.findByEmployeeAndOrderTimeBetweenAndIsCancelledFalse(employee, startTime, endTime);
        
        return orders.stream().map(order -> {
            SalesReportDTO dto = new SalesReportDTO();
            dto.setOrderId(order.getId());
            dto.setOrderTime(order.getOrderTime());
            dto.setCustomerType(order.getCustomerType().toString());
            dto.setEmployeeId(employee.getEmpId());
            dto.setEmployeeName(employee.getName());
            dto.setMealType(order.getMeal().getType().toString());
            dto.setTotalAmount(order.getTotalAmount());
            dto.setItemCount(order.getOrderItems().size());
            return dto;
        }).collect(Collectors.toList());
    }
    
    /**
     * Get meal count statistics (daily, monthly, or custom range)
     */
    public List<MealCountDTO> getMealCountReport(LocalDate startDate, LocalDate endDate, String groupBy) {
        LocalDateTime startTime = startDate.atStartOfDay();
        LocalDateTime endTime = endDate.atTime(LocalTime.MAX);
        
        List<Orders> orders = orderRepository.findByOrderTimeBetweenAndIsCancelledFalse(startTime, endTime);
        
        if ("day".equalsIgnoreCase(groupBy)) {
            return groupByDay(orders);
        } else if ("meal".equalsIgnoreCase(groupBy)) {
            return groupByMeal(orders);
        } else {
            return groupByDayAndMeal(orders);
        }
    }
    
    private List<MealCountDTO> groupByDay(List<Orders> orders) {
        Map<LocalDate, MealCountData> dayMap = new HashMap<>();
        
        for (Orders order : orders) {
            LocalDate date = order.getOrderTime().toLocalDate();
            MealCountData data = dayMap.getOrDefault(date, new MealCountData());
            data.addOrder(order.getTotalAmount());
            dayMap.put(date, data);
        }
        
        return dayMap.entrySet().stream()
            .map(entry -> new MealCountDTO(
                entry.getKey(),
                "ALL",
                entry.getValue().count,
                entry.getValue().revenue
            ))
            .sorted((a, b) -> a.getDate().compareTo(b.getDate()))
            .collect(Collectors.toList());
    }
    
    private List<MealCountDTO> groupByMeal(List<Orders> orders) {
        Map<String, MealCountData> mealMap = new HashMap<>();
        
        for (Orders order : orders) {
            String mealType = order.getMeal().getType().toString();
            MealCountData data = mealMap.getOrDefault(mealType, new MealCountData());
            data.addOrder(order.getTotalAmount());
            mealMap.put(mealType, data);
        }
        
        return mealMap.entrySet().stream()
            .map(entry -> new MealCountDTO(
                null,
                entry.getKey(),
                entry.getValue().count,
                entry.getValue().revenue
            ))
            .collect(Collectors.toList());
    }
    
    private List<MealCountDTO> groupByDayAndMeal(List<Orders> orders) {
        Map<String, MealCountData> combinedMap = new HashMap<>();
        
        for (Orders order : orders) {
            LocalDate date = order.getOrderTime().toLocalDate();
            String mealType = order.getMeal().getType().toString();
            String key = date + "_" + mealType;
            
            MealCountData data = combinedMap.getOrDefault(key, new MealCountData());
            data.addOrder(order.getTotalAmount());
            data.date = date;
            data.mealType = mealType;
            combinedMap.put(key, data);
        }
        
        return combinedMap.values().stream()
            .map(data -> new MealCountDTO(
                data.date,
                data.mealType,
                data.count,
                data.revenue
            ))
            .sorted((a, b) -> {
                int dateCompare = a.getDate().compareTo(b.getDate());
                if (dateCompare != 0) return dateCompare;
                return a.getMealType().compareTo(b.getMealType());
            })
            .collect(Collectors.toList());
    }
    /**
     * Get detailed order report with all items for Excel export
     * Report 1: Consolidated detailed report with all order and item information
     */
    public List<DetailedOrderReportDTO> getDetailedOrderReport(LocalDate startDate, LocalDate endDate) {
        LocalDateTime startTime = startDate.atStartOfDay();
        LocalDateTime endTime = endDate.atTime(LocalTime.MAX);
        
        List<Orders> orders = orderRepository.findOrdersWithItemsByOrderTimeBetween(startTime, endTime);
        List<DetailedOrderReportDTO> report = new ArrayList<>();
        
        for (Orders order : orders) {
            for (OrderItems item : order.getOrderItems()) {
                DetailedOrderReportDTO dto = new DetailedOrderReportDTO();
                dto.setOrderId(order.getId());
                dto.setOrderTime(order.getOrderTime());
                dto.setCustomerType(order.getCustomerType().toString());
                dto.setMealType(order.getMeal() != null ? order.getMeal().getType().toString() : "N/A");
                dto.setOrderTotal(order.getTotalAmount());
                
                // Set customer details
                if (order.getEmployee() != null) {
                    dto.setCustomerId(order.getEmployee().getEmpId());
                    dto.setCustomerName(order.getEmployee().getName());
                } else if (order.getOutsiderName() != null) {
                    dto.setCustomerId("N/A");
                    dto.setCustomerName(order.getOutsiderName());
                } else if (order.getHostEmployee() != null) {
                    // For guest orders, show host employee ID and name
                    dto.setCustomerId(order.getHostEmployee().getEmpId());
                    dto.setCustomerName(order.getHostEmployee().getName());
                }
                
                // Set item details
                dto.setItemName(item.getMenu().getItemName());
                dto.setQuantity(item.getQuantity());
                dto.setItemPrice(item.getPrice());
                dto.setItemTotal(item.getPrice() * item.getQuantity());
                
                report.add(dto);
            }
        }
        
        return report;
    }
    
    /**
     * Get employee purchase summary report for Excel export
     * Report 2: Per-employee consolidated purchase summary.
     * Excludes GUEST orders — those live in the dedicated Guest Orders report.
     */
    public List<EmployeePurchaseReportDTO> getEmployeePurchaseSummary(LocalDate startDate, LocalDate endDate) {
        LocalDateTime startTime = startDate.atStartOfDay();
        LocalDateTime endTime = endDate.atTime(LocalTime.MAX);

        List<Orders> orders = orderRepository.findByOrderTimeBetweenAndIsCancelledFalse(startTime, endTime);

        Map<String, EmployeePurchaseData> employeeMap = new HashMap<>();

        for (Orders order : orders) {
            // Only direct employee orders — skip GUEST and OUTSIDER
            if (order.getCustomerType() != CustomerType.EMPLOYEE || order.getEmployee() == null) {
                continue;
            }
            Employee targetEmployee = order.getEmployee();
            String empId = targetEmployee.getEmpId();
            EmployeePurchaseData data = employeeMap.getOrDefault(empId,
                new EmployeePurchaseData(targetEmployee));
            data.addOrder(order);
            employeeMap.put(empId, data);
        }

        return employeeMap.values().stream()
            .map(data -> new EmployeePurchaseReportDTO(
                data.employee.getEmpId(),
                data.employee.getName(),
                data.employee.getDepartment(),
                data.orderCount,
                data.itemCount,
                data.totalCost
            ))
            .sorted((a, b) -> b.getTotalCost().compareTo(a.getTotalCost()))
            .collect(Collectors.toList());
    }

    /**
     * Guest orders report — one row per guest order.
     */
    public List<GuestOrderReportDTO> getGuestOrderReport(LocalDate startDate, LocalDate endDate) {
        LocalDateTime startTime = startDate.atStartOfDay();
        LocalDateTime endTime = endDate.atTime(LocalTime.MAX);

        List<Orders> orders = orderRepository.findByOrderTimeBetweenAndIsCancelledFalse(startTime, endTime);

        return orders.stream()
            .filter(o -> o.getCustomerType() == CustomerType.GUEST && o.getHostEmployee() != null)
            .map(o -> new GuestOrderReportDTO(
                o.getId(),
                o.getOrderTime(),
                o.getHostEmployee().getEmpId(),
                o.getHostEmployee().getName(),
                o.getHostEmployee().getDepartment(),
                o.getPurpose(),
                o.getGuestCount(),
                o.getCompanyEmployeeCount(),
                o.getOrderItems().size(),
                o.getTotalAmount()
            ))
            .sorted((a, b) -> b.getOrderTime().compareTo(a.getOrderTime()))
            .collect(Collectors.toList());
    }
    
    /**
     * Get item purchase statistics report for Excel export
     * Report 3: Per-item purchase statistics
     */
    public List<ItemPurchaseReportDTO> getItemPurchaseStatistics(LocalDate startDate, LocalDate endDate) {
        LocalDateTime startTime = startDate.atStartOfDay();
        LocalDateTime endTime = endDate.atTime(LocalTime.MAX);
        
        List<Orders> orders = orderRepository.findOrdersWithItemsByOrderTimeBetween(startTime, endTime);
        
        // Group by item
        Map<String, ItemPurchaseData> itemMap = new HashMap<>();
        
        for (Orders order : orders) {
            for (OrderItems item : order.getOrderItems()) {
                String itemName = item.getMenu().getItemName();
                String category = (item.getMenu().getMeals() != null && !item.getMenu().getMeals().isEmpty())
                    ? item.getMenu().getMeals().stream()
                        .map(m -> m.getType().toString())
                        .sorted()
                        .collect(java.util.stream.Collectors.joining(", "))
                    : "GENERAL";
                ItemPurchaseData data = itemMap.getOrDefault(itemName,
                    new ItemPurchaseData(itemName, category));
                data.addItem(item.getQuantity(), item.getPrice());
                itemMap.put(itemName, data);
            }
        }
        
        return itemMap.values().stream()
            .map(data -> new ItemPurchaseReportDTO(
                data.itemName,
                data.category,
                data.totalQuantity,
                data.orderCount,
                data.totalRevenue,
                data.totalRevenue / data.totalQuantity
            ))
            .sorted((a, b) -> b.getTotalQuantity().compareTo(a.getTotalQuantity()))
            .collect(Collectors.toList());
    }
    
    // Helper class for employee purchase data
    private static class EmployeePurchaseData {
        Employee employee;
        Integer orderCount = 0;
        Integer itemCount = 0;
        Double totalCost = 0.0;
        
        EmployeePurchaseData(Employee employee) {
            this.employee = employee;
        }
        
        void addOrder(Orders order) {
            this.orderCount++;
            this.itemCount += order.getOrderItems().size();
            this.totalCost += order.getTotalAmount();
        }
    }
    
    // Helper class for item purchase data
    private static class ItemPurchaseData {
        String itemName;
        String category;
        Integer totalQuantity = 0;
        Integer orderCount = 0;
        Double totalRevenue = 0.0;
        
        ItemPurchaseData(String itemName, String category) {
            this.itemName = itemName;
            this.category = category;
        }
        
        void addItem(Integer quantity, Double price) {
            this.totalQuantity += quantity;
            this.orderCount++;
            this.totalRevenue += (quantity * price);
        }
    }
    
    // Helper classes
    private static class EmployeeCostData {
        Employee employee;
        Double totalCost = 0.0;
        Integer orderCount = 0;
        
        EmployeeCostData(Employee employee) {
            this.employee = employee;
        }
        
        void addOrder(Double amount) {
            this.totalCost += amount;
            this.orderCount++;
        }
    }
    
    private static class MealCountData {
        Long count = 0L;
        Double revenue = 0.0;
        LocalDate date;
        String mealType;
        
        void addOrder(Double amount) {
            this.count++;
            this.revenue += amount;
        }
    }
}