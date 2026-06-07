package com.cafeteria.canteen.service;

import com.cafeteria.canteen.entity.Employee;
import com.cafeteria.canteen.enums.EmployeeRole;
import com.cafeteria.canteen.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmployeeImportService {
    
    private final EmployeeRepository employeeRepository;
    
    /**
     * Import employees from Excel file
     * Expected format: Employee Code | Employee Name | Department | Role (optional)
     * @param file Excel file with employee data
     * @param clearExisting if true, clears all existing employees before import
     * @return Map with import statistics
     */
    @Transactional
    public Map<String, Object> importEmployeesFromExcel(MultipartFile file, boolean clearExisting) throws IOException {
        Map<String, Object> result = new HashMap<>();
        List<String> errors = new ArrayList<>();
        int successCount = 0;
        int skipCount = 0;
        int updateCount = 0;
        int rowNum = 0;
        
        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            int totalRows = sheet.getLastRowNum();
            
            log.info("Starting employee import. Total rows in sheet: {}", totalRows);
            
            // Clear existing employees if requested
            if (clearExisting) {
                long deletedCount = employeeRepository.count();
                try {
                    employeeRepository.deleteAll();
                    log.info("Cleared {} existing employees", deletedCount);
                    result.put("deletedCount", deletedCount);
                } catch (Exception e) {
                    throw new IOException("Cannot clear existing employees because they are referenced in orders. " +
                            "Please uncheck 'Clear existing employees' option to update/add employees instead.");
                }
            }
            
            // Process all rows
            for (Row row : sheet) {
                rowNum = row.getRowNum() + 1; // 1-based for user messages
                
                // Skip header row (first row)
                if (row.getRowNum() == 0) {
                    log.info("Skipping header row");
                    continue;
                }
                
                try {
                    // Try to get cell values from different column positions
                    // Sometimes Excel files have hidden columns or tabs
                    String empCode = "";
                    String empName = "";
                    String department = "";
                    String role = "";
                    
                    // Try to find employee code in first few columns
                    for (int col = 0; col < 5 && empCode.isEmpty(); col++) {
                        Cell cell = row.getCell(col);
                        if (cell != null) {
                            String value = getCellValueAsString(cell).trim();
                            // Employee codes are numeric and start with 7
                            if (!value.isEmpty() && value.matches("^7\\d+$")) {
                                empCode = value;
                                // Get name from next column
                                Cell nameCell = row.getCell(col + 1);
                                if (nameCell != null) {
                                    empName = getCellValueAsString(nameCell).trim();
                                }
                                // Get department from column after that
                                Cell deptCell = row.getCell(col + 2);
                                if (deptCell != null) {
                                    department = getCellValueAsString(deptCell).trim();
                                }
                                // Get role from column after that (optional)
                                Cell roleCell = row.getCell(col + 3);
                                if (roleCell != null) {
                                    role = getCellValueAsString(roleCell).trim();
                                }
                                break;
                            }
                        }
                    }
                    
                    // Skip empty rows
                    if (empCode.isEmpty()) {
                        skipCount++;
                        log.debug("Row {}: Skipping - no valid employee code found", rowNum);
                        continue;
                    }
                    
                    log.debug("Row {}: Processing empCode='{}', empName='{}', dept='{}', role='{}'", rowNum, empCode, empName, department, role);
                    
                    // Validate required fields
                    if (empCode.isEmpty() || empName.isEmpty()) {
                        errors.add("Row " + rowNum + ": empCode='" + empCode + "', name='" + empName + "' - Both required");
                        log.warn("Row {}: Validation failed", rowNum);
                        continue;
                    }
                    
                    // Check if employee already exists
                    Employee employee = employeeRepository.findByEmpId(empCode)
                            .orElse(new Employee());
                    
                    boolean isUpdate = employee.getId() != null;
                    
                    // Determine role - default to STAFF if not specified or invalid
                    EmployeeRole employeeRole = EmployeeRole.STAFF;
                    if (!role.isEmpty()) {
                        try {
                            employeeRole = EmployeeRole.valueOf(role.toUpperCase());
                        } catch (IllegalArgumentException e) {
                            log.warn("Row {}: Invalid role '{}', defaulting to STAFF", rowNum, role);
                        }
                    }
                    
                    // Set employee data
                    employee.setEmpId(empCode);
                    employee.setName(empName);
                    employee.setDepartment(department.isEmpty() ? "General" : department);
                    employee.setRole(employeeRole);
                    
                    // Save employee
                    employeeRepository.save(employee);
                    log.info("Row {}: {} employee {}", rowNum, isUpdate ? "Updated" : "Created", empCode);
                    
                    if (isUpdate) {
                        updateCount++;
                    } else {
                        successCount++;
                    }
                    
                } catch (Exception e) {
                    errors.add("Row " + rowNum + ": " + e.getMessage());
                    log.error("Error processing row {}: {}", rowNum, e.getMessage(), e);
                }
            }
            
        } catch (Exception e) {
            log.error("Error reading Excel file: {}", e.getMessage(), e);
            throw new IOException("Failed to read Excel file: " + e.getMessage());
        }
        
        // Prepare result
        result.put("success", true);
        result.put("totalProcessed", successCount + updateCount);
        result.put("newEmployees", successCount);
        result.put("updatedEmployees", updateCount);
        result.put("skippedRows", skipCount);
        result.put("errors", errors);
        result.put("errorCount", errors.size());
        
        log.info("Employee import completed: {} new, {} updated, {} skipped, {} errors", 
                successCount, updateCount, skipCount, errors.size());
        
        return result;
    }
    
    /**
     * Get cell value as string, handling different cell types
     */
    private String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return "";
        }
        
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                // Handle numeric values (like employee codes stored as numbers)
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    // Convert to long to avoid decimal points for whole numbers
                    double numValue = cell.getNumericCellValue();
                    if (numValue == (long) numValue) {
                        return String.valueOf((long) numValue);
                    } else {
                        return String.valueOf(numValue);
                    }
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            case BLANK:
                return "";
            default:
                return "";
        }
    }
    
    /**
     * Get total employee count
     */
    public long getEmployeeCount() {
        return employeeRepository.count();
    }
    
    /**
     * Get all employees (for verification)
     */
    public List<Employee> getAllEmployees() {
        return employeeRepository.findAll();
    }
}