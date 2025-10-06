# Attendance & Payroll Management System - Backend

A comprehensive Spring Boot application for managing employee attendance, leave requests, and payroll calculations with location-based check-in/check-out functionality.

## Features

### Core Functionality
- **Role-Based Access Control**: HR, Admin, Manager, and Employee roles
- **Location-Based Attendance**: GPS proximity verification for check-in/check-out
- **Automated Payroll Calculation**:
    - Regular hours and overtime tracking
    - Weekend overtime (1.5x multiplier)
    - Holiday overtime (2.0x multiplier)
    - Regular overtime (1.25x multiplier)
- **Leave Management**: Paid and unpaid leave tracking
- **Public Holiday Configuration**: Kuwait holidays pre-configured
- **Approval Workflows**: HR/Manager approval for attendance and leaves
- **Smart Payment Dates**: Automatic calculation of payment date (26th or nearest Thursday)

## Technology Stack

- Java 17
- Spring Boot 3.2.0
- Spring Security with JWT
- Spring Data JPA
- MySQL Database
- Lombok
- MapStruct
- Maven

## Prerequisites

- JDK 17 or higher
- Maven 3.6+
- MySQL 8.0+
- Your favorite IDE (IntelliJ IDEA, Eclipse, VS Code)

## Database Setup

1. Install MySQL and create a database:
```sql
CREATE DATABASE attendance_payroll_db;
```

2. Update database credentials in `src/main/resources/application.properties`:
```properties
spring.datasource.username=your_mysql_username
spring.datasource.password=your_mysql_password
```

## Installation & Running

1. Clone the repository
2. Navigate to project directory
3. Build the project:
```bash
mvn clean install
```

4. Run the application:
```bash
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

## Default Users

The system initializes with three default users:

| Email | Password | Role | Hourly Rate |
|-------|----------|------|-------------|
| admin@company.com | admin123 | ADMIN, HR | $50 |
| hr@company.com | hr123 | HR | $40 |
| employee@company.com | emp123 | EMPLOYEE | $25 |

## Project Structure

```
src/main/java/com/company/attendance/
├── config/              # Configuration classes
│   ├── SecurityConfig.java
│   ├── WebConfig.java
│   └── DataInitializer.java
├── controller/          # REST Controllers
│   ├── AuthenticationController.java
│   ├── AttendanceController.java
│   ├── LeaveController.java
│   ├── PayrollController.java
│   ├── UserController.java
│   ├── PublicHolidayController.java
│   └── CompanySettingsController.java
├── dto/                # Data Transfer Objects
│   ├── UserPrincipal.java
│   ├── AuthenticationRequest.java
│   ├── AuthenticationResponse.java
│   ├── RegisterRequest.java
│   └── Various request DTOs
├── entity/             # JPA Entities
│   ├── User.java
│   ├── Attendance.java
│   ├── Leave.java
│   ├── Payroll.java
│   ├── PublicHoliday.java
│   └── CompanySettings.java
├── repository/         # Spring Data Repositories
├── security/           # Security & JWT
│   ├── JwtService.java
│   ├── JwtAuthenticationFilter.java
│   └── CustomUserDetailsService.java
├── service/            # Business Logic
│   ├── AttendanceService.java
│   ├── LeaveService.java
│   ├── PayrollService.java
│   ├── LocationService.java
│   └── AuthenticationService.java
└── exception/          # Exception Handlers
    └── GlobalExceptionHandler.java
```

## API Endpoints

### Authentication

#### Register User
```http
POST /api/auth/register
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "password123",
  "firstName": "John",
  "lastName": "Doe",
  "hourlyRate": 25.0,
  "roles": ["ROLE_EMPLOYEE"]
}
```

#### Login
```http
POST /api/auth/login
Content-Type: application/json

{
  "email": "employee@company.com",
  "password": "emp123"
}

Response:
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "email": "employee@company.com",
  "firstName": "John",
  "lastName": "Doe",
  "userId": 1
}
```

### Attendance Management

#### Check In
```http
POST /api/attendance/check-in
Authorization: Bearer {token}
Content-Type: application/json

{
  "latitude": 29.3759,
  "longitude": 47.9774
}
```

#### Check Out
```http
POST /api/attendance/check-out
Authorization: Bearer {token}
Content-Type: application/json

{
  "latitude": 29.3759,
  "longitude": 47.9774
}
```

#### Get My Attendances
```http
GET /api/attendance/my-attendances?startDate=2025-01-01T00:00:00&endDate=2025-01-31T23:59:59
Authorization: Bearer {token}
```

#### Get Pending Attendances (HR/Manager)
```http
GET /api/attendance/pending
Authorization: Bearer {token}
```

#### Approve Attendance (HR/Manager)
```http
POST /api/attendance/{id}/approve
Authorization: Bearer {token}
```

#### Reject Attendance (HR/Manager)
```http
POST /api/attendance/{id}/reject
Authorization: Bearer {token}
Content-Type: application/json

{
  "reason": "Invalid timing"
}
```

### Leave Management

#### Apply for Leave
```http
POST /api/leaves/apply
Authorization: Bearer {token}
Content-Type: application/json

{
  "startDate": "2025-03-01",
  "endDate": "2025-03-05",
  "leaveType": "PAID",
  "reason": "Family vacation"
}
```

#### Get My Leaves
```http
GET /api/leaves/my-leaves
Authorization: Bearer {token}
```

#### Get Pending Leaves (HR/Manager)
```http
GET /api/leaves/pending
Authorization: Bearer {token}
```

#### Approve Leave (HR/Manager)
```http
POST /api/leaves/{id}/approve
Authorization: Bearer {token}
```

#### Reject Leave (HR/Manager)
```http
POST /api/leaves/{id}/reject
Authorization: Bearer {token}
Content-Type: application/json

{
  "reason": "Insufficient leave balance"
}
```

### Payroll Management

#### Generate Payroll (HR/Admin)
```http
POST /api/payroll/generate/{userId}?month=1&year=2025
Authorization: Bearer {token}
```

#### Get My Payroll
```http
GET /api/payroll/my-payroll?month=1&year=2025
Authorization: Bearer {token}
```

#### Get Payrolls by Month (HR/Admin)
```http
GET /api/payroll/month?month=1&year=2025
Authorization: Bearer {token}
```

#### Process Payroll (HR/Admin)
```http
POST /api/payroll/{id}/process
Authorization: Bearer {token}
```

#### Mark as Paid (HR/Admin)
```http
POST /api/payroll/{id}/mark-paid
Authorization: Bearer {token}
```

### User Management

#### Get Current User
```http
GET /api/users/me
Authorization: Bearer {token}
```

#### Get All Users (HR/Admin)
```http
GET /api/users
Authorization: Bearer {token}
```

#### Get User by ID (HR/Admin)
```http
GET /api/users/{id}
Authorization: Bearer {token}
```

### Public Holidays

#### Get All Holidays
```http
GET /api/holidays
Authorization: Bearer {token}
```

#### Create Holiday (HR/Admin)
```http
POST /api/holidays
Authorization: Bearer {token}
Content-Type: application/json

{
  "name": "Independence Day",
  "date": "2025-12-25",
  "description": "National holiday"
}
```

#### Delete Holiday (HR/Admin)
```http
DELETE /api/holidays/{id}
Authorization: Bearer {token}
```

### Company Settings

#### Get Settings
```http
GET /api/settings
Authorization: Bearer {token}
```

#### Update Settings (Admin)
```http
PUT /api/settings/{id}
Authorization: Bearer {token}
Content-Type: application/json

{
  "officeLatitude": 29.3759,
  "officeLongitude": 47.9774,
  "proximityRadiusMeters": 100.0,
  "standardWorkHoursPerDay": 8.0,
  "paymentDay": 26
}
```

## Business Logic Details

### Attendance Calculation

1. **Regular Hours**: Up to 8 hours per day (configurable)
2. **Regular Overtime**: Hours beyond standard hours on weekdays (1.25x)
3. **Weekend Overtime**: All hours worked on Friday/Saturday (1.5x)
4. **Holiday Overtime**: All hours worked on public holidays (2.0x)

### Payroll Calculation

The system automatically calculates:
- Total regular hours (including paid leave hours)
- Total overtime hours by category
- Pay = (Regular Hours × Hourly Rate) + (Overtime Hours × Rate × Multiplier)

### Payment Date Logic

- Default payment day: 26th of each month
- If 26th falls on Friday: Payment on Thursday (25th)
- If 26th falls on Saturday: Payment on Thursday (24th)
- Otherwise: Payment on the 26th

### Location-Based Check-In

- Uses Haversine formula for distance calculation
- Validates user is within specified radius (default 100 meters)
- Prevents check-in/check-out outside office proximity

### Leave Management

- **Paid Leave**: Full salary for leave days (8 hours per day)
- **Unpaid Leave**: No salary calculation
- **Sick Leave**: Treated as paid leave
- **Casual Leave**: Configurable

## Security

- JWT-based authentication
- Role-based access control (RBAC)
- Password encryption using BCrypt
- CORS enabled for React frontend
- Stateless session management

## Error Handling

The application includes global exception handling:
- `RuntimeException`: 400 Bad Request
- `BadCredentialsException`: 401 Unauthorized
- `Generic Exception`: 500 Internal Server Error

All errors return JSON format:
```json
{
  "timestamp": "2025-01-15T10:30:00",
  "message": "Error description",
  "status": 400
}
```

## Testing with Postman/Curl

### Example: Complete Flow

1. **Login**
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"employee@company.com","password":"emp123"}'
```

2. **Check In** (use token from login)
```bash
curl -X POST http://localhost:8080/api/attendance/check-in \
  -H "Authorization: Bearer YOUR_TOKEN_HERE" \
  -H "Content-Type: application/json" \
  -d '{"latitude":29.3759,"longitude":47.9774}'
```

3. **Check Out**
```bash
curl -X POST http://localhost:8080/api/attendance/check-out \
  -H "Authorization: Bearer YOUR_TOKEN_HERE" \
  -H "Content-Type: application/json" \
  -d '{"latitude":29.3759,"longitude":47.9774}'
```

4. **Apply for Leave**
```bash
curl -X POST http://localhost:8080/api/leaves/apply \
  -H "Authorization: Bearer YOUR_TOKEN_HERE" \
  -H "Content-Type: application/json" \
  -d '{
    "startDate":"2025-03-01",
    "endDate":"2025-03-05",
    "leaveType":"PAID",
    "reason":"Vacation"
  }'
```

## Database Schema

### Main Tables

- **users**: Employee information and authentication
- **attendance**: Check-in/check-out records
- **leaves**: Leave applications
- **payroll**: Monthly payroll records
- **public_holidays**: Kuwait public holidays
- **company_settings**: System configuration

### Key Relationships

- User → Attendance (One-to-Many)
- User → Leave (One-to-Many)
- User → Payroll (One-to-Many)
- Attendance → User (approved_by)
- Leave → User (approved_by)

## Configuration Options

### Company Settings
- Office Location (Latitude/Longitude)
- Proximity Radius (meters)
- Standard Work Hours per Day
- Payment Day of Month

### User-Specific Settings
- Hourly Rate
- Weekend Overtime Multiplier
- Holiday Overtime Multiplier
- Regular Overtime Multiplier

## Deployment Considerations

### Production Checklist

1. **Security**
    - Change JWT secret key
    - Use environment variables for sensitive data
    - Enable HTTPS
    - Update CORS allowed origins

2. **Database**
    - Use production-grade database
    - Set up database backups
    - Configure connection pooling
    - Change `spring.jpa.hibernate.ddl-auto` to `validate`

3. **Performance**
    - Enable database indexing
    - Configure caching
    - Set appropriate JVM heap size
    - Monitor application metrics

4. **Monitoring**
    - Set up logging (ELK stack, Splunk)
    - Configure health checks
    - Set up alerts for errors

### Environment Variables

```bash
export DB_URL=jdbc:mysql://your-db-host:3306/attendance_payroll_db
export DB_USERNAME=your_username
export DB_PASSWORD=your_password
export JWT_SECRET=your_super_secret_key_here
export JWT_EXPIRATION=86400000
```

Update `application.properties`:
```properties
spring.datasource.url=${DB_URL}
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}
jwt.secret=${JWT_SECRET}
jwt.expiration=${JWT_EXPIRATION}
```

## Integration with React Frontend

### CORS Configuration
Already configured to allow `http://localhost:3000`

### API Base URL in React
```javascript
const API_BASE_URL = 'http://localhost:8080/api';
```

### Authentication Header
```javascript
const token = localStorage.getItem('token');
const headers = {
  'Authorization': `Bearer ${token}`,
  'Content-Type': 'application/json'
};
```

## Future Enhancements

- [ ] Email notifications for approvals
- [ ] SMS alerts for check-in reminders
- [ ] Biometric authentication
- [ ] Mobile app integration
- [ ] Advanced reporting and analytics
- [ ] Multi-tenancy support
- [ ] Integration with accounting software
- [ ] Shift management
- [ ] Performance review module
- [ ] Document management

## Troubleshooting

### Common Issues

**1. Application won't start**
- Check MySQL is running
- Verify database credentials
- Check port 8080 is not in use

**2. JWT Token Invalid**
- Token might be expired (24 hours default)
- Login again to get new token

**3. Location Check-In Failed**
- Verify GPS coordinates are correct
- Check proximity radius in settings
- Ensure you're within office radius

**4. Database Connection Error**
- Verify MySQL service is running
- Check database URL and credentials
- Ensure database exists

## Support & Contact

For issues or questions:
- Create an issue in the repository
- Contact: support@company.com

## License

This project is proprietary software for internal company use.

---

## Quick Start Summary

```bash
# 1. Setup database
mysql -u root -p
CREATE DATABASE attendance_payroll_db;

# 2. Update application.properties with your DB credentials

# 3. Build and run
mvn clean install
mvn spring-boot:run

# 4. Test with default credentials
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"employee@company.com","password":"emp123"}'

# 5. Access the API at http://localhost:8080
```

**Your Spring Boot backend is now ready to integrate with React frontend!**