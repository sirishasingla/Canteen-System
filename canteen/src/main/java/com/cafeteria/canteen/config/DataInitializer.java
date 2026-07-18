package com.cafeteria.canteen.config;

import com.cafeteria.canteen.entity.AdminUser;
import com.cafeteria.canteen.entity.Employee;
import com.cafeteria.canteen.entity.Meal;
import com.cafeteria.canteen.entity.Menu;
import com.cafeteria.canteen.enums.AdminRole;
import com.cafeteria.canteen.enums.EmployeeRole;
import com.cafeteria.canteen.enums.MealType;
import com.cafeteria.canteen.repository.AdminUserRepository;
import com.cafeteria.canteen.repository.EmployeeRepository;
import com.cafeteria.canteen.repository.MealRepository;
import com.cafeteria.canteen.repository.MenuRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalTime;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final EmployeeRepository employeeRepository;
    private final MealRepository mealRepository;
    private final MenuRepository menuRepository;
    private final AdminUserRepository adminUserRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.username:admin}")
    private String adminUsername;

    @Value("${app.admin.password:admin123}")
    private String adminPassword;

    @Value("${app.manager.username:manager}")
    private String managerUsername;

    @Value("${app.manager.password:manager123}")
    private String managerPassword;

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

        initializeAdminUsers();
    }

    private void initializeAdminUsers() {
        if (adminUserRepository.findByUsername(adminUsername).isEmpty()) {
            AdminUser admin = new AdminUser();
            admin.setUsername(adminUsername);
            admin.setPasswordHash(passwordEncoder.encode(adminPassword));
            admin.setRole(AdminRole.ADMIN);
            adminUserRepository.save(admin);
            System.out.println("Admin user seeded: " + adminUsername);
        }
        if (adminUserRepository.findByUsername(managerUsername).isEmpty()) {
            AdminUser manager = new AdminUser();
            manager.setUsername(managerUsername);
            manager.setPasswordHash(passwordEncoder.encode(managerPassword));
            manager.setRole(AdminRole.MANAGER);
            adminUserRepository.save(manager);
            System.out.println("Manager user seeded: " + managerUsername);
        }
    }
    
    private void initializeEmployees() {
        // Create sample employees
        // Employee emp1 = new Employee();
        // emp1.setEmpId("EMP001");
        // emp1.setName("John Doe");
        // emp1.setDepartment("IT");
        // emp1.setRole(EmployeeRole.WORKER);
        
        // Employee emp2 = new Employee();
        // emp2.setEmpId("EMP002");
        // emp2.setName("Jane Smith");
        // emp2.setDepartment("HR");
        // emp2.setRole(EmployeeRole.STAFF);
        
        // Employee emp3 = new Employee();
        // emp3.setEmpId("EMP003");
        // emp3.setName("Bob Johnson");
        // emp3.setDepartment("Finance");
        // emp3.setRole(EmployeeRole.WORKER);
        
        // employeeRepository.save(emp1);
        // employeeRepository.save(emp2);
        // employeeRepository.save(emp3);
        
        // System.out.println("Sample employees initialized");
    }
    
    private void initializeMeals() {
        Meal breakfast = new Meal();
        breakfast.setType(MealType.BREAKFAST);
        breakfast.setStartTime(LocalTime.of(8, 0));
        breakfast.setEndTime(LocalTime.of(9, 0));

        Meal snacks = new Meal();
        snacks.setType(MealType.SNACKS);
        snacks.setStartTime(LocalTime.of(11, 0));
        snacks.setEndTime(LocalTime.of(12, 0));

        Meal lunch = new Meal();
        lunch.setType(MealType.LUNCH);
        lunch.setStartTime(LocalTime.of(15, 0));
        lunch.setEndTime(LocalTime.of(17, 0));

        Meal dinner = new Meal();
        dinner.setType(MealType.DINNER);
        dinner.setStartTime(LocalTime.of(20, 0));
        dinner.setEndTime(LocalTime.of(21, 0));

        mealRepository.save(breakfast);
        mealRepository.save(snacks);
        mealRepository.save(lunch);
        mealRepository.save(dinner);

        System.out.println("Meal times initialized");
    }

    private void initializeMenu() {
        Meal breakfast = mealRepository.findByType(MealType.BREAKFAST).orElseThrow();
        Meal snacks = mealRepository.findByType(MealType.SNACKS).orElseThrow();
        Meal lunch = mealRepository.findByType(MealType.LUNCH).orElseThrow();
        Meal dinner = mealRepository.findByType(MealType.DINNER).orElseThrow();

        // Order matches admin's current preferred layout. displayOrder in steps of 10 so
        // admins can slot new items in between without a renumber.
        int order = 10;

        // Collapsed LUNCH / DINNER items with per-audience pricing (base is a fallback for
        // GUEST / no-audience callers; kiosk hides them from Guest since role-restricted).
        menuRepository.save(rolePriced("LUNCH", 0.0, 40.0, 30.0, 50.0, order++, lunch));
        menuRepository.save(rolePriced("Dinner", 0.0, 40.0, 30.0, 50.0, order++, dinner));

        // Always available (no meal restriction)
        menuRepository.save(basic("TEA", 5.0, order++));

        // Breakfast + snacks
        menuRepository.save(basic("BREAD PAKODA", 5.0, order++, breakfast));
        menuRepository.save(basic("SAMOSA", 5.0, order++, breakfast, snacks));
        menuRepository.save(basic("MATTHI", 5.0, order++, breakfast, snacks));
        menuRepository.save(basic("PARONTHA", 10.0, order++, breakfast));
        menuRepository.save(basic("MAKKHAN TIKKI", 5.0, order++, breakfast));
        menuRepository.save(basic("DAHI 100 GMS", 10.0, order++, breakfast, lunch, dinner));

        // Lunch + Dinner shared items
        menuRepository.save(basic("CHAPATI", 2.5, order++, lunch, dinner));
        menuRepository.save(basic("DAL OR SABJI", 10.0, order++, lunch, dinner));

        // Always-available snacks and drinks
        menuRepository.save(basic("BISCUIT", 10.0, order++));
        menuRepository.save(basic("NAMKEEN", 10.0, order++));
        menuRepository.save(basic("JUICE", 10.0, order++));
        menuRepository.save(basic("MILK BOTTLE", 25.0, order++));
        menuRepository.save(basic("JAL ZEERA", 10.0, order++));
        menuRepository.save(basic("CHIPS", 10.0, order++));
        menuRepository.save(basic("KURKURE", 10.0, order++));
        menuRepository.save(basic("LASSI", 10.0, order++));

        System.out.println("Menu items initialized");
    }

    private Menu basic(String itemName, Double price, int displayOrder, Meal... meals) {
        Menu menu = new Menu();
        menu.setItemName(itemName);
        menu.setPrice(price);
        menu.setIsActive(true);
        menu.setDisplayOrder(displayOrder * 10);
        if (meals != null && meals.length > 0) {
            menu.setMeals(new java.util.HashSet<>(java.util.Arrays.asList(meals)));
        }
        return menu;
    }

    private Menu rolePriced(String itemName, Double basePrice, Double staff, Double worker,
                            Double outsider, int displayOrder, Meal... meals) {
        Menu menu = basic(itemName, basePrice, displayOrder, meals);
        menu.setStaffPrice(staff);
        menu.setWorkerPrice(worker);
        menu.setOutsiderPrice(outsider);
        return menu;
    }
}