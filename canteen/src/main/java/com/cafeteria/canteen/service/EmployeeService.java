package com.cafeteria.canteen.service;

import com.cafeteria.canteen.entity.Employee;
import com.cafeteria.canteen.repository.EmployeeRepository;
import com.cafeteria.canteen.util.EmpIdUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeRepository employeeRepository;

    /**
     * Get all employees
     */
    public List<Employee> getAllEmployees() {
        return employeeRepository.findAll();
    }

    /**
     * Get employee by empId. Accepts either the full 8-digit ID or the last 5 digits.
     */
    public Employee getEmployeeByEmpId(String empId) {
        String normalized = EmpIdUtil.normalize(empId);
        return employeeRepository.findByEmpId(normalized)
                .orElseThrow(() -> new RuntimeException("Employee not found with ID: " + empId));
    }
    
    /**
     * Create new employee
     */
    @Transactional
    public Employee createEmployee(Employee employee) {
        // Check if employee code already exists
        if (employeeRepository.findByEmpId(employee.getEmpId()).isPresent()) {
            throw new RuntimeException("Employee with code " + employee.getEmpId() + " already exists");
        }
        return employeeRepository.save(employee);
    }
    
    /**
     * Update existing employee
     */
    @Transactional
    public Employee updateEmployee(String empId, Employee employeeDetails) {
        Employee employee = getEmployeeByEmpId(empId);
        
        employee.setName(employeeDetails.getName());
        employee.setDepartment(employeeDetails.getDepartment());
        employee.setRole(employeeDetails.getRole());
        
        return employeeRepository.save(employee);
    }
    
    /**
     * Disable employee (soft delete)
     * This is safer than hard delete as it preserves data integrity
     */
    @Transactional
    public Employee disableEmployee(String empId) {
        Employee employee = getEmployeeByEmpId(empId);
        employee.setIsActive(false);
        return employeeRepository.save(employee);
    }
    
    /**
     * Enable employee
     */
    @Transactional
    public Employee enableEmployee(String empId) {
        Employee employee = getEmployeeByEmpId(empId);
        employee.setIsActive(true);
        return employeeRepository.save(employee);
    }
    
    /**
     * Toggle employee active status
     */
    @Transactional
    public Employee toggleEmployeeStatus(String empId) {
        Employee employee = getEmployeeByEmpId(empId);
        employee.setIsActive(!employee.getIsActive());
        return employeeRepository.save(employee);
    }
    
    /**
     * Get only active employees
     */
    public List<Employee> getActiveEmployees() {
        return employeeRepository.findAll().stream()
                .filter(Employee::getIsActive)
                .toList();
    }
    
    /**
     * Get employee count
     */
    public long getEmployeeCount() {
        return employeeRepository.count();
    }
    
    /**
     * Search employees by name or empId
     */
    public List<Employee> searchEmployees(String query) {
        return employeeRepository.findAll().stream()
                .filter(e -> e.getEmpId().toLowerCase().contains(query.toLowerCase()) ||
                           e.getName().toLowerCase().contains(query.toLowerCase()) ||
                           (e.getDepartment() != null && e.getDepartment().toLowerCase().contains(query.toLowerCase())))
                .toList();
    }
}