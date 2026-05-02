#!/usr/bin/env python3
"""
Generate SQL script to load all employees from Employee Master.xlsx
"""

import openpyxl
import sys

def escape_sql_string(s):
    """Escape single quotes in SQL strings"""
    if s is None:
        return ''
    return str(s).replace("'", "''")

def generate_sql():
    # Load the Excel file
    wb = openpyxl.load_workbook('/Users/sirishasingla/Downloads/Employee Master.xlsx')
    sheet = wb.active
    
    sql_lines = []
    sql_lines.append("-- Clear existing employees and load complete employee master list")
    sql_lines.append("-- Total: 486 employees from Employee Master.xlsx\n")
    sql_lines.append("-- First, disable foreign key constraints temporarily")
    sql_lines.append("SET session_replication_role = 'replica';\n")
    sql_lines.append("-- Clear existing employees")
    sql_lines.append("TRUNCATE TABLE employee CASCADE;\n")
    sql_lines.append("-- Re-enable foreign key constraints")
    sql_lines.append("SET session_replication_role = 'origin';\n")
    
    # Process rows (skip header)
    employees = []
    for row in sheet.iter_rows(min_row=2, values_only=True):
        if row[0]:  # If employee code exists
            emp_code = str(row[0]).strip()
            emp_name = escape_sql_string(row[1])
            department = escape_sql_string(row[2])
            employees.append((emp_code, emp_name, department))
    
    # Split into chunks of 100 for better readability
    chunk_size = 100
    for i in range(0, len(employees), chunk_size):
        chunk = employees[i:i+chunk_size]
        sql_lines.append(f"\n-- Employees {i+1} to {min(i+chunk_size, len(employees))}")
        sql_lines.append("INSERT INTO employee (emp_id, name, department, role) VALUES")
        
        values = []
        for emp_code, emp_name, department in chunk:
            values.append(f"('{emp_code}', '{emp_name}', '{department}', 'EMPLOYEE')")
        
        sql_lines.append(",\n".join(values) + ";\n")
    
    sql_lines.append(f"\n-- Total employees loaded: {len(employees)}")
    sql_lines.append("SELECT COUNT(*) as total_employees FROM employee;")
    
    return "\n".join(sql_lines)

if __name__ == "__main__":
    try:
        sql_content = generate_sql()
        with open('load_employee_master.sql', 'w', encoding='utf-8') as f:
            f.write(sql_content)
        print(f"Successfully generated load_employee_master.sql")
    except Exception as e:
        print(f"Error: {e}", file=sys.stderr)
        sys.exit(1)