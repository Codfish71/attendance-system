package com.geeksmetrics.attendance_mgmt.config;

import com.geeksmetrics.attendance_mgmt.entity.CompanySettings;
import com.geeksmetrics.attendance_mgmt.entity.PublicHoliday;
import com.geeksmetrics.attendance_mgmt.entity.Role;
import com.geeksmetrics.attendance_mgmt.entity.User;
import com.geeksmetrics.attendance_mgmt.repository.CompanySettingsRepository;
import com.geeksmetrics.attendance_mgmt.repository.PublicHolidayRepository;
import com.geeksmetrics.attendance_mgmt.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import java.time.LocalDate;
import java.util.HashSet;


@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CompanySettingsRepository settingsRepository;
    private final PublicHolidayRepository holidayRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, CompanySettingsRepository settingsRepository, PublicHolidayRepository holidayRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.settingsRepository = settingsRepository;
        this.holidayRepository = holidayRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        // Initialize company settings if not exists
        if (settingsRepository.count() == 0) {
            CompanySettings settings = new CompanySettings();
            settings.setOfficeLatitude(18.546688); // Kuwait City example
            settings.setOfficeLongitude(73.940992);
            settings.setProximityRadiusMeters(100.0);
            settings.setStandardWorkHoursPerDay(8.0);
            settings.setPaymentDay(26);
            settingsRepository.save(settings);
            System.out.println("Company settings initialized");
        }

        // Create admin user if not exists
        if (!userRepository.existsByEmail("admin@company.com")) {
            User admin = new User();
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

        // Create sample HR user if not exists
        if (!userRepository.existsByEmail("hr@company.com")) {
            User hr = new User();
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

        // Create sample employee if not exists
        if (!userRepository.existsByEmail("employee@company.com")) {
            User employee = new User();
            employee.setEmail("employee@company.com");
            employee.setPassword(passwordEncoder.encode("emp123"));
            employee.setFirstName("John");
            employee.setLastName("Doe");
            employee.setHourlyRate(25.0);
            employee.setRoles(new HashSet<>());
            employee.getRoles().add(Role.ROLE_EMPLOYEE);
            employee.setActive(true);
            userRepository.save(employee);
            System.out.println("Employee user created: employee@company.com / emp123");
        }

        // Initialize Kuwait public holidays for 2025
        if (holidayRepository.count() == 0) {
            addHoliday("New Year's Day", LocalDate.of(2025, 1, 1));
            addHoliday("National Day", LocalDate.of(2025, 2, 25));
            addHoliday("Liberation Day", LocalDate.of(2025, 2, 26));
            addHoliday("Isra and Mi'raj", LocalDate.of(2025, 1, 27));
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