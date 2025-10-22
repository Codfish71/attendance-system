package com.geeksmetrics.attendance_mgmt.config;

import com.geeksmetrics.attendance_mgmt.entity.CompanySettings;
import com.geeksmetrics.attendance_mgmt.entity.PublicHoliday;
import com.geeksmetrics.attendance_mgmt.entity.Role;
import com.geeksmetrics.attendance_mgmt.entity.User;
import com.geeksmetrics.attendance_mgmt.repository.CompanySettingsRepository;
import com.geeksmetrics.attendance_mgmt.repository.PublicHolidayRepository;
import com.geeksmetrics.attendance_mgmt.repository.UserRepository;
import com.geeksmetrics.attendance_mgmt.service.EmployeeIdService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import java.time.LocalDate;
import java.util.HashSet;



@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CompanySettingsRepository settingsRepository;
    private final PublicHolidayRepository holidayRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmployeeIdService employeeIdService;

    @Override
    public void run(String... args) throws Exception {
        // Initialize company settings if not exists
        if (settingsRepository.count() == 0) {
            CompanySettings settings = new CompanySettings();
            settings.setOfficeLatitude(29.3759); // Kuwait City
            settings.setOfficeLongitude(47.9774);
            settings.setProximityRadiusMeters(100.0);
            settings.setStandardWorkHoursPerDay(8.0);
            settings.setPaymentDay(26);
            settingsRepository.save(settings);
            System.out.println("Company settings initialized");
        }

        // Create admin user if not exists
        if (!userRepository.existsByEmail("admin@company.com")) {
            User admin = new User();
            admin.setEmployeeId(employeeIdService.generateEmployeeIdWithPrefix("AD"));
            admin.setEmail("admin@company.com");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setFirstName("Admin");
            admin.setLastName("User");
            admin.setHourlyRate(50.0);
            admin.setRoles(new HashSet<>());
            admin.getRoles().add(Role.ROLE_ADMIN);
            admin.getRoles().add(Role.ROLE_HR);
            admin.setActive(true);
            userRepository.save(admin);
            System.out.println("Admin user created: admin@company.com / admin123");
        }

        // Create HR user if not exists
        if (!userRepository.existsByEmail("hr@company.com")) {
            User hr = new User();
            hr.setEmployeeId(employeeIdService.generateEmployeeIdWithPrefix("HR"));
            hr.setEmail("hr@company.com");
            hr.setPassword(passwordEncoder.encode("hr123"));
            hr.setFirstName("HR");
            hr.setLastName("Manager");
            hr.setHourlyRate(40.0);
            hr.setRoles(new HashSet<>());
            hr.getRoles().add(Role.ROLE_HR);
            hr.setActive(true);
            userRepository.save(hr);
            System.out.println("HR user created: hr@company.com / hr123");
        }

        // Create Project Manager if not exists
        if (!userRepository.existsByEmail("pm@company.com")) {
            User pm = new User();
            pm.setEmployeeId(employeeIdService.generateEmployeeIdWithPrefix("PM"));
            pm.setEmail("pm@company.com");
            pm.setPassword(passwordEncoder.encode("pm123"));
            pm.setFirstName("Project");
            pm.setLastName("Manager");
            pm.setHourlyRate(60.0);
            pm.setRoles(new HashSet<>());
            pm.getRoles().add(Role.ROLE_PROJECT_MANAGER);
            pm.setActive(true);
            userRepository.save(pm);
            System.out.println("PM user created: pm@company.com / pm123");
        }

        // Create Site Lead if not exists
        if (!userRepository.existsByEmail("sitelead@company.com")) {
            User siteLead = new User();
            siteLead.setEmployeeId(employeeIdService.generateEmployeeIdWithPrefix("SL"));
            siteLead.setEmail("sitelead@company.com");
            siteLead.setPassword(passwordEncoder.encode("lead123"));
            siteLead.setFirstName("Site");
            siteLead.setLastName("Lead");
            siteLead.setHourlyRate(45.0);
            siteLead.setRoles(new HashSet<>());
            siteLead.getRoles().add(Role.ROLE_SITE_LEAD);
            siteLead.setActive(true);

            // Set reports to PM
            User pm = userRepository.findByEmail("pm@company.com").orElse(null);
            if (pm != null) {
                siteLead.setReportsTo(pm);
            }

            userRepository.save(siteLead);
            System.out.println("Site Lead user created: sitelead@company.com / lead123");
        }

        // Create Coordinator if not exists
        if (!userRepository.existsByEmail("coordinator@company.com")) {
            User coordinator = new User();
            coordinator.setEmployeeId(employeeIdService.generateEmployeeIdWithPrefix("CO"));
            coordinator.setEmail("coordinator@company.com");
            coordinator.setPassword(passwordEncoder.encode("coord123"));
            coordinator.setFirstName("John");
            coordinator.setLastName("Coordinator");
            coordinator.setHourlyRate(30.0);
            coordinator.setRoles(new HashSet<>());
            coordinator.getRoles().add(Role.ROLE_COORDINATOR);
            coordinator.setActive(true);

            // Set reports to Site Lead
            User siteLead = userRepository.findByEmail("sitelead@company.com").orElse(null);
            if (siteLead != null) {
                coordinator.setReportsTo(siteLead);
            }

            userRepository.save(coordinator);
            System.out.println("Coordinator user created: coordinator@company.com / coord123");
        }

        // Initialize Kuwait public holidays for 2025
        if (holidayRepository.count() == 0) {
            addHoliday("New Year's Day", LocalDate.of(2025, 1, 1));
            addHoliday("Isra and Mi'raj", LocalDate.of(2025, 1, 27));
            addHoliday("National Day", LocalDate.of(2025, 2, 25));
            addHoliday("Liberation Day", LocalDate.of(2025, 2, 26));
            addHoliday("Eid al-Fitr", LocalDate.of(2025, 3, 30));
            addHoliday("Eid al-Fitr Holiday", LocalDate.of(2025, 3, 31));
            addHoliday("Eid al-Fitr Holiday", LocalDate.of(2025, 4, 1));
            addHoliday("Arafat Day", LocalDate.of(2025, 6, 6));
            addHoliday("Eid al-Adha", LocalDate.of(2025, 6, 7));
            addHoliday("Eid al-Adha Holiday", LocalDate.of(2025, 6, 8));
            addHoliday("Eid al-Adha Holiday", LocalDate.of(2025, 6, 9));
            addHoliday("Islamic New Year", LocalDate.of(2025, 6, 27));
            addHoliday("Prophet's Birthday", LocalDate.of(2025, 9, 5));
            System.out.println("Kuwait public holidays initialized for 2025");
        }
    }

    private void addHoliday(String name, LocalDate date) {
        if (!holidayRepository.existsByDate(date)) {
            PublicHoliday holiday = new PublicHoliday();
            holiday.setName(name);
            holiday.setDate(date);
            holidayRepository.save(holiday);
        }
    }
}