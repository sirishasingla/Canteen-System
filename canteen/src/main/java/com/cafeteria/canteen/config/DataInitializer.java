package com.cafeteria.canteen.config;

import com.cafeteria.canteen.entity.Employee;
import com.cafeteria.canteen.entity.Meal;
import com.cafeteria.canteen.entity.Menu;
import com.cafeteria.canteen.enums.EmployeeRole;
import com.cafeteria.canteen.enums.MealType;
import com.cafeteria.canteen.repository.EmployeeRepository;
import com.cafeteria.canteen.repository.MealRepository;
import com.cafeteria.canteen.repository.MenuRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalTime;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {
    
    private final EmployeeRepository employeeRepository;
    private final MealRepository mealRepository;
    private final MenuRepository menuRepository;
    
    @Override
    public void run(String... args) throws Exception {
        // Initialize only if database is empty
        if (employeeRepository.count() == 0) {
            initializeEmployees();
        }
        
        if (mealRepository.count() == 0) {
            initializeMeals();
        }
        
        if (menuRepository.count() == 0) {
            initializeMenu();
        }
    }
    
    private void initializeEmployees() {
        // Create sample employees
        Employee emp1 = new Employee();
        emp1.setEmpId("EMP001");
        emp1.setName("John Doe");
        emp1.setDepartment("IT");
        emp1.setRole(EmployeeRole.WORKER);
        
        Employee emp2 = new Employee();
        emp2.setEmpId("EMP002");
        emp2.setName("Jane Smith");
        emp2.setDepartment("HR");
        emp2.setRole(EmployeeRole.STAFF);
        
        Employee emp3 = new Employee();
        emp3.setEmpId("EMP003");
        emp3.setName("Bob Johnson");
        emp3.setDepartment("Finance");
        emp3.setRole(EmployeeRole.WORKER);
        
        employeeRepository.save(emp1);
        employeeRepository.save(emp2);
        employeeRepository.save(emp3);
        
        System.out.println("Sample employees initialized");
    }
    
    private void initializeMeals() {
        // Create meal times
        Meal breakfast = new Meal();
        breakfast.setType(MealType.BREAKFAST);
        breakfast.setStartTime(LocalTime.of(8, 0));
        breakfast.setEndTime(LocalTime.of(9, 0));
        
        Meal lunch = new Meal();
        lunch.setType(MealType.LUNCH);
        lunch.setStartTime(LocalTime.of(15, 0));
        lunch.setEndTime(LocalTime.of(17, 0));
        
        Meal dinner = new Meal();
        dinner.setType(MealType.DINNER);
        dinner.setStartTime(LocalTime.of(20, 0));
        dinner.setEndTime(LocalTime.of(21, 0));
        
        mealRepository.save(breakfast);
        mealRepository.save(lunch);
        mealRepository.save(dinner);
        
        System.out.println("Meal times initialized");
    }
    
    private void initializeMenu() {
        // Create menu items without meal time restrictions
        menuRepository.save(createMenuItem("BREAD PAKODA", 5.0));
        menuRepository.save(createMenuItem("SAMOSA", 5.0));
        menuRepository.save(createMenuItem("MATTHI", 5.0));
        menuRepository.save(createMenuItem("PARONTHA", 10.0));
        menuRepository.save(createMenuItem("MAKKHAN TIKKI", 5.0));
        menuRepository.save(createMenuItem("DAHI 100 GMS", 10.0));
        menuRepository.save(createMenuItem("TEA", 5.0));
        menuRepository.save(createMenuItem("CHAPATI", 2.5));
        menuRepository.save(createMenuItem("DAL OR SABJI", 10.0));
        menuRepository.save(createMenuItem("LUNCH/DINNER (WORKER)", 30.0));
        menuRepository.save(createMenuItem("LUNCH/DINNER (STAFF)", 40.0));
        menuRepository.save(createMenuItem("LUNCH/DINNER (OUTSIDER)", 50.0));
        menuRepository.save(createMenuItem("LUNCH/DINNER (MEAL SLIP)", 30.0));
        menuRepository.save(createMenuItem("BISCUIT", 10.0));
        menuRepository.save(createMenuItem("NAMKEEN", 10.0));
        menuRepository.save(createMenuItem("JUICE", 10.0));
        menuRepository.save(createMenuItem("MILK BOTTLE", 25.0));
        menuRepository.save(createMenuItem("JAL ZEERA", 10.0));
        menuRepository.save(createMenuItem("CHIPS", 10.0));
        menuRepository.save(createMenuItem("KURKURE", 10.0));
        menuRepository.save(createMenuItem("LASSI", 10.0));
        
        System.out.println("Menu items initialized");
    }
    
    private Menu createMenuItem(String itemName, Double price) {
        Menu menu = new Menu();
        menu.setMeal(null);  // No meal association - available all the time
        menu.setItemName(itemName);
        menu.setPrice(price);
        menu.setIsActive(true);
        return menu;
    }
}