package com.cafeteria.canteen.controller;

import com.cafeteria.canteen.entity.Employee;
import com.cafeteria.canteen.service.EmployeeImportService;
import com.cafeteria.canteen.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/employees")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class EmployeeController {
    
    private final EmployeeImportService employeeImportService;
    private final EmployeeService employeeService;
    
    /**
     * Upload Excel file to bulk import employees
     * POST /api/employees/upload
     */
    @PostMapping("/upload")
    public ResponseEntity<?> uploadEmployees(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "clearExisting", defaultValue = "false") boolean clearExisting) {
        
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "File is empty"));
            }
            
            String filename = file.getOriginalFilename();
            if (filename == null || (!filename.endsWith(".xlsx") && !filename.endsWith(".xls"))) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "Only Excel files (.xlsx, .xls) are allowed"));
            }
            
            Map<String, Object> result = employeeImportService.importEmployeesFromExcel(file, clearExisting);
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to import employees: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * Get all employees
     * GET /api/employees
     */
    @GetMapping
    public ResponseEntity<List<Employee>> getAllEmployees(
            @RequestParam(required = false) String search) {
        if (search != null && !search.trim().isEmpty()) {
            return ResponseEntity.ok(employeeService.searchEmployees(search));
        }
        return ResponseEntity.ok(employeeService.getAllEmployees());
    }
    
    /**
     * Get employee by empId
     * GET /api/employees/{empId}
     */
    @GetMapping("/{empId}")
    public ResponseEntity<?> getEmployeeByEmpId(@PathVariable String empId) {
        try {
            Employee employee = employeeService.getEmployeeByEmpId(empId);
            return ResponseEntity.ok(employee);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }
    
    /**
     * Create a new employee
     * POST /api/employees/create
     */
    @PostMapping("/create")
    public ResponseEntity<?> createEmployee(@RequestBody Employee employee) {
        try {
            Employee savedEmployee = employeeService.createEmployee(employee);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of("success", true, "message", "Employee created successfully", "employee", savedEmployee));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }
    
    /**
     * Update an existing employee
     * PUT /api/employees/{empId}
     */
    @PutMapping("/{empId}")
    public ResponseEntity<?> updateEmployee(
            @PathVariable String empId,
            @RequestBody Employee employee) {
        try {
            Employee updatedEmployee = employeeService.updateEmployee(empId, employee);
            return ResponseEntity.ok(Map.of("success", true, "message", "Employee updated successfully", "employee", updatedEmployee));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }
    
    /**
     * Disable an employee (soft delete)
     * DELETE /api/employees/{empId}
     */
    @DeleteMapping("/{empId}")
    public ResponseEntity<?> disableEmployee(@PathVariable String empId) {
        try {
            Employee employee = employeeService.disableEmployee(empId);
            return ResponseEntity.ok(Map.of("success", true, "message", "Employee disabled successfully", "employee", employee));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }
    
    /**
     * Enable an employee
     * POST /api/employees/{empId}/enable
     */
    @PostMapping("/{empId}/enable")
    public ResponseEntity<?> enableEmployee(@PathVariable String empId) {
        try {
            Employee employee = employeeService.enableEmployee(empId);
            return ResponseEntity.ok(Map.of("success", true, "message", "Employee enabled successfully", "employee", employee));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }
    
    /**
     * Toggle employee active status
     * POST /api/employees/{empId}/toggle-status
     */
    @PostMapping("/{empId}/toggle-status")
    public ResponseEntity<?> toggleEmployeeStatus(@PathVariable String empId) {
        try {
            Employee employee = employeeService.toggleEmployeeStatus(empId);
            String status = employee.getIsActive() ? "enabled" : "disabled";
            return ResponseEntity.ok(Map.of("success", true, "message", "Employee " + status + " successfully", "employee", employee));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }
    
    /**
     * Get total employee count
     * GET /api/employees/count
     */
    @GetMapping("/count")
    public ResponseEntity<Map<String, Object>> getEmployeeCount() {
        long count = employeeService.getEmployeeCount();
        return ResponseEntity.ok(Map.of("count", count));
    }
    
    /**
     * Get sample Excel template information
     * GET /api/employees/template-info
     */
    @GetMapping("/template-info")
    public ResponseEntity<Map<String, Object>> getTemplateInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("format", "Excel file (.xlsx or .xls)");
        info.put("columns", List.of(
            Map.of("name", "Employee Code", "required", true, "example", "70000011"),
            Map.of("name", "Employee Name", "required", true, "example", "John Doe"),
            Map.of("name", "Department", "required", false, "example", "Engineering")
        ));
        info.put("notes", List.of(
            "First row should contain column headers",
            "Employee Code must be unique",
            "If Employee Code already exists, the record will be updated",
            "Department is optional (defaults to 'General' if not provided)",
            "Empty rows will be skipped"
        ));
        return ResponseEntity.ok(info);
    }
}