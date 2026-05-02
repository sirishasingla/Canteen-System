package com.cafeteria.canteen.service;

import com.cafeteria.canteen.dto.DetailedOrderReportDTO;
import com.cafeteria.canteen.dto.EmployeePurchaseReportDTO;
import com.cafeteria.canteen.dto.ItemPurchaseReportDTO;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ExcelExportService {
    
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    /**
     * Generate consolidated detailed order report Excel
     * Contains all order details with items
     */
    public byte[] generateDetailedOrderReport(List<DetailedOrderReportDTO> data) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Detailed Orders Report");
            
            // Create header style
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dateStyle = createDateStyle(workbook);
            CellStyle currencyStyle = createCurrencyStyle(workbook);
            
            // Create header row
            Row headerRow = sheet.createRow(0);
            String[] headers = {
                "Order ID", "Order Date/Time", "Customer Type", "Customer ID", 
                "Customer Name", "Meal Type", "Item Name", "Quantity", 
                "Item Price", "Item Total", "Order Total"
            };
            
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            
            // Fill data rows
            int rowNum = 1;
            for (DetailedOrderReportDTO dto : data) {
                Row row = sheet.createRow(rowNum++);
                
                row.createCell(0).setCellValue(dto.getOrderId());
                
                Cell dateCell = row.createCell(1);
                dateCell.setCellValue(dto.getOrderTime().format(DATE_TIME_FORMATTER));
                dateCell.setCellStyle(dateStyle);
                
                row.createCell(2).setCellValue(dto.getCustomerType());
                row.createCell(3).setCellValue(dto.getCustomerId() != null ? dto.getCustomerId() : "N/A");
                row.createCell(4).setCellValue(dto.getCustomerName());
                row.createCell(5).setCellValue(dto.getMealType());
                row.createCell(6).setCellValue(dto.getItemName());
                row.createCell(7).setCellValue(dto.getQuantity());
                
                Cell priceCell = row.createCell(8);
                priceCell.setCellValue(dto.getItemPrice());
                priceCell.setCellStyle(currencyStyle);
                
                Cell itemTotalCell = row.createCell(9);
                itemTotalCell.setCellValue(dto.getItemTotal());
                itemTotalCell.setCellStyle(currencyStyle);
                
                Cell orderTotalCell = row.createCell(10);
                orderTotalCell.setCellValue(dto.getOrderTotal());
                orderTotalCell.setCellStyle(currencyStyle);
            }
            
            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            workbook.write(out);
            return out.toByteArray();
        }
    }
    
    /**
     * Generate per-employee purchase summary report Excel
     */
    public byte[] generateEmployeePurchaseReport(List<EmployeePurchaseReportDTO> data) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Employee Purchase Report");
            
            // Create styles
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle currencyStyle = createCurrencyStyle(workbook);
            
            // Create header row
            Row headerRow = sheet.createRow(0);
            String[] headers = {
                "Employee ID", "Employee Name", "Department", 
                "Total Orders", "Total Items", "Total Cost"
            };
            
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            
            // Fill data rows
            int rowNum = 1;
            for (EmployeePurchaseReportDTO dto : data) {
                Row row = sheet.createRow(rowNum++);
                
                row.createCell(0).setCellValue(dto.getEmployeeId());
                row.createCell(1).setCellValue(dto.getEmployeeName());
                row.createCell(2).setCellValue(dto.getDepartment() != null ? dto.getDepartment() : "N/A");
                row.createCell(3).setCellValue(dto.getTotalOrders());
                row.createCell(4).setCellValue(dto.getTotalItems());
                
                Cell costCell = row.createCell(5);
                costCell.setCellValue(dto.getTotalCost());
                costCell.setCellStyle(currencyStyle);
            }
            
            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            workbook.write(out);
            return out.toByteArray();
        }
    }
    
    /**
     * Generate per-item purchase statistics report Excel
     */
    public byte[] generateItemPurchaseReport(List<ItemPurchaseReportDTO> data) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Item Purchase Report");
            
            // Create styles
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle currencyStyle = createCurrencyStyle(workbook);
            
            // Create header row
            Row headerRow = sheet.createRow(0);
            String[] headers = {
                "Item Name", "Category", "Total Quantity Sold", 
                "Number of Orders", "Total Revenue", "Average Price"
            };
            
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            
            // Fill data rows
            int rowNum = 1;
            for (ItemPurchaseReportDTO dto : data) {
                Row row = sheet.createRow(rowNum++);
                
                row.createCell(0).setCellValue(dto.getItemName());
                row.createCell(1).setCellValue(dto.getCategory());
                row.createCell(2).setCellValue(dto.getTotalQuantity());
                row.createCell(3).setCellValue(dto.getNumberOfOrders());
                
                Cell revenueCell = row.createCell(4);
                revenueCell.setCellValue(dto.getTotalRevenue());
                revenueCell.setCellStyle(currencyStyle);
                
                Cell avgPriceCell = row.createCell(5);
                avgPriceCell.setCellValue(dto.getAveragePrice());
                avgPriceCell.setCellStyle(currencyStyle);
            }
            
            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            workbook.write(out);
            return out.toByteArray();
        }
    }
    
    /**
     * Create header cell style
     */
    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 12);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }
    
    /**
     * Create date cell style
     */
    private CellStyle createDateStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setDataFormat(workbook.getCreationHelper().createDataFormat().getFormat("yyyy-mm-dd hh:mm:ss"));
        return style;
    }
    
    /**
     * Create currency cell style
     */
    private CellStyle createCurrencyStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setDataFormat(workbook.getCreationHelper().createDataFormat().getFormat("₹#,##0.00"));
        return style;
    }
}