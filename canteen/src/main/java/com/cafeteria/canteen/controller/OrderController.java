package com.cafeteria.canteen.controller;

import com.cafeteria.canteen.dto.OrderRequest;
import com.cafeteria.canteen.dto.OrderResponse;
import com.cafeteria.canteen.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class OrderController {

    private final OrderService orderService;

    /**
     * Create a new order (kiosk — public).
     */
    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@RequestBody OrderRequest request) {
        OrderResponse response = orderService.createOrder(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Admin: create an order (may be backdated via request.orderTime).
     */
    @PostMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OrderResponse> createOrderAsAdmin(@RequestBody OrderRequest request) {
        OrderResponse response = orderService.createOrderAsAdmin(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Admin: update an existing order.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OrderResponse> updateOrder(@PathVariable Long id, @RequestBody OrderRequest request) {
        return ResponseEntity.ok(orderService.updateOrder(id, request));
    }

    /**
     * Admin: soft-delete (cancel) an order.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OrderResponse> cancelOrder(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.cancelOrder(id));
    }

    /**
     * Admin: restore a cancelled order.
     */
    @PostMapping("/{id}/restore")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OrderResponse> restoreOrder(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.restoreOrder(id));
    }

    /**
     * Admin: list all orders (including cancelled) in a date range.
     */
    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<OrderResponse>> getAllOrdersInRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        return ResponseEntity.ok(orderService.getAllOrdersInRange(startDate, endDate));
    }

    /**
     * Get order by ID
     * GET /api/orders/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrderById(@PathVariable Long id) {
        OrderResponse response = orderService.getOrderById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Get all orders for an employee
     * GET /api/orders/employee/{empId}
     */
    @GetMapping("/employee/{empId}")
    public ResponseEntity<List<OrderResponse>> getOrdersByEmployee(@PathVariable String empId) {
        List<OrderResponse> orders = orderService.getOrdersByEmployee(empId);
        return ResponseEntity.ok(orders);
    }

    /**
     * Get orders by date range
     */
    @GetMapping("/date-range")
    public ResponseEntity<List<OrderResponse>> getOrdersByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        List<OrderResponse> orders = orderService.getOrdersByDateRange(startDate, endDate);
        return ResponseEntity.ok(orders);
    }

    /**
     * Get total amount spent by employee in date range
     */
    @GetMapping("/employee/{empId}/total")
    public ResponseEntity<Double> getTotalAmountByEmployee(
            @PathVariable String empId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        Double total = orderService.getTotalAmountByEmployeeAndDateRange(empId, startDate, endDate);
        return ResponseEntity.ok(total);
    }
}