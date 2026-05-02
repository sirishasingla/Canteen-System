package com.cafeteria.canteen.controller;

import com.cafeteria.canteen.dto.*;
import com.cafeteria.canteen.service.ExcelExportService;
import com.cafeteria.canteen.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/api/reports")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class ReportController {
    
    private final ReportService reportService;
    private final ExcelExportService excelExportService;
    
    /**
     * Get sales report between start and end date/time
     * GET /api/reports/sales?startTime=2024-01-01T00:00:00&endTime=2024-01-31T23:59:59
     */
    @GetMapping("/sales")
    public ResponseEntity<List<SalesReportDTO>> getSalesReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        
        List<SalesReportDTO> report = reportService.getSalesReport(startTime, endTime);
        return ResponseEntity.ok(report);
    }
    
    /**
     * Get cost per employee mapping between dates
     * GET /api/reports/employee-cost?startDate=2024-01-01&endDate=2024-01-31
     */
    @GetMapping("/employee-cost")
    public ResponseEntity<List<EmployeeCostDTO>> getEmployeeCostReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        List<EmployeeCostDTO> report = reportService.getEmployeeCostReport(startDate, endDate);
        return ResponseEntity.ok(report);
    }
    
    /**
     * Get order history for a specific employee
     * GET /api/reports/employee-history?empId=EMP001&startDate=2024-01-01&endDate=2024-01-31
     */
    @GetMapping("/employee-history")
    public ResponseEntity<List<SalesReportDTO>> getEmployeeOrderHistory(
            @RequestParam String empId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        List<SalesReportDTO> report = reportService.getEmployeeOrderHistory(empId, startDate, endDate);
        return ResponseEntity.ok(report);
    }
    
    /**
     * Get meal count statistics
     * GET /api/reports/meal-count?startDate=2024-01-01&endDate=2024-01-31&groupBy=day
     * groupBy options: day, meal, both
     */
    @GetMapping("/meal-count")
    public ResponseEntity<List<MealCountDTO>> getMealCountReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "both") String groupBy) {
        
        List<MealCountDTO> report = reportService.getMealCountReport(startDate, endDate, groupBy);
        return ResponseEntity.ok(report);
    }
    
    /**
     * Download detailed consolidated order report as Excel
     * GET /api/reports/excel/detailed-orders?startDate=2024-01-01&endDate=2024-01-31
     */
    @GetMapping("/excel/detailed-orders")
    public ResponseEntity<byte[]> downloadDetailedOrderReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) throws IOException {
        
        List<DetailedOrderReportDTO> data = reportService.getDetailedOrderReport(startDate, endDate);
        byte[] excelFile = excelExportService.generateDetailedOrderReport(data);
        
        String filename = String.format("Detailed_Orders_Report_%s_to_%s.xlsx", 
            startDate.format(DateTimeFormatter.ISO_DATE),
            endDate.format(DateTimeFormatter.ISO_DATE));
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", filename);
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(excelFile);
    }
    
    /**
     * Download employee purchase summary report as Excel
     * GET /api/reports/excel/employee-purchases?startDate=2024-01-01&endDate=2024-01-31
     */
    @GetMapping("/excel/employee-purchases")
    public ResponseEntity<byte[]> downloadEmployeePurchaseReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) throws IOException {
        
        List<EmployeePurchaseReportDTO> data = reportService.getEmployeePurchaseSummary(startDate, endDate);
        byte[] excelFile = excelExportService.generateEmployeePurchaseReport(data);
        
        String filename = String.format("Employee_Purchase_Report_%s_to_%s.xlsx", 
            startDate.format(DateTimeFormatter.ISO_DATE),
            endDate.format(DateTimeFormatter.ISO_DATE));
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", filename);
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(excelFile);
    }
    
    /**
     * Download item purchase statistics report as Excel
     * GET /api/reports/excel/item-statistics?startDate=2024-01-01&endDate=2024-01-31
     */
    @GetMapping("/excel/item-statistics")
    public ResponseEntity<byte[]> downloadItemPurchaseReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) throws IOException {
        
        List<ItemPurchaseReportDTO> data = reportService.getItemPurchaseStatistics(startDate, endDate);
        byte[] excelFile = excelExportService.generateItemPurchaseReport(data);
        
        String filename = String.format("Item_Statistics_Report_%s_to_%s.xlsx", 
            startDate.format(DateTimeFormatter.ISO_DATE),
            endDate.format(DateTimeFormatter.ISO_DATE));
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", filename);
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(excelFile);
    }
}