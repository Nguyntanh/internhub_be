# Technical Audit Report: InternHub-BE Project

## 1. Business Features (Tính năng nghiệp vụ đã hoàn thiện)

- **Authentication & Authorization System:**
  - **User Login:** Authenticates users via email and password, issuing a JSON Web Token (JWT) for secure session management. (`AuthController`)
  - **Permission Retrieval:** Allows authenticated users to fetch their assigned permissions, including base roles and granular function-level access. (`AuthController`)
  - **Role-Based Access Control (RBAC):** Supports distinct roles (e.g., ADMIN, HR, MANAGER, MENTOR, INTERN) with predefined access levels.
  - **Fine-grained Permissions:** Implements a granular permission matrix, enabling specific CRUD (Create, Read, Update, Delete) access for functions (e.g., `USER_MGMT:READ`, `SKILL_LIB:CREATE`).
- **Admin User Management (Requires ADMIN Role):**
  - **User Creation:** Admins can create new user accounts, assigning roles and departments. Features include email uniqueness validation, temporary password generation, activation token generation, and an activation email workflow. (`AdminController`, `AdminUserServiceImpl`)
  - **User Status Management:** Admins can activate or deactivate user accounts. (`AdminController`, `AdminUserServiceImpl`)
  - **Internship Profile Management:**
    - **Full CRUD for Interns:** Quản lý hồ sơ thực tập sinh (InternshipProfile), bao gồm thông tin chuyên ngành, thời gian thực tập, trường đại học, vị trí thực tập.
    - **Assignment:** Gán Mentor và Manager cho từng thực tập sinh.
    - **Flexible Creation:** Hỗ trợ tạo profile mới kèm user hoặc tạo profile từ user đã có sẵn trong hệ thống. (`InternService`)
  - **Manager Review & Decision Workflow:**
    - **Review Dashboard:** Manager có thể xem danh sách các thực tập sinh chờ duyệt (đã có đánh giá từ Mentor).
    - **Performance Analytics:** Tổng hợp điểm kỹ năng theo dạng radar (weighted average) từ các MicroTask và đánh giá chi tiết của Mentor. (`ManagerReviewServiceImpl`)
    - **Final Decision:** Manager đưa ra quyết định cuối cùng: PASS, FAIL, hoặc CONVERT_TO_STAFF kèm theo nhận xét.
  - **Internal Notification System:**
    - **Event-based Notifications:** Tự động tạo thông báo khi Mentor gửi đánh giá hoặc khi Manager ra quyết định cuối cùng.
    - **Notification Management:** Người dùng có thể xem danh sách thông báo, đếm số thông báo chưa đọc, và đánh dấu đã đọc (từng cái hoặc tất cả). (`NotificationController`, `NotificationService`)
- **Initial Data Setup:**
  - Automated creation of essential default data on application startup: "IT Department", standard roles (ADMIN, HR, MANAGER, MENTOR, INTERN), core functions (USER_MGMT, SKILL_LIB), and a super-admin user (`admin@internhub.com` with password "admin") granted all permissions. (`AdminInitializerService`)
- **Email Communication:**
  - System is capable of sending activation emails to new users. (`EmailService`)
- **Audit Logging:**
  - Basic audit logging is implemented for critical administrative actions, such as user creation and status updates. (`AuditLogService`, `AdminUserServiceImpl`)

## 2. Data Entities and Relationships (Thực thể dữ liệu và mối quan hệ)

Based on the `src/main/java/com/example/internhub_be/domain` package:

- **User:** Represents a user of the system.
  - `Many-to-One` with `Role` (a user has one role).
  - `Many-to-One` with `Department` (a user belongs to one department).
- **Role:** Defines user roles (e.g., ADMIN, INTERN).
  - `One-to-Many` with `User`.
  - `One-to-Many` with `RolePermission` (a role has many permissions).
- **Department:** Organizational units.
  - `One-to-Many` with `User`.
- **Function:** Represents a system function or module (e.g., USER_MGMT).
  - `One-to-Many` with `RolePermission` (a function can have many permissions assigned to roles).
- **Notification:** Lưu trữ thông báo nội bộ cho người dùng.
  - Liên kết với `User` (recipient và sender).
  - Hỗ trợ `reference_id` để dẫn tới các thực thể liên quan (Evaluation, Decision).
- **RolePermission:** A composite entity defining specific permissions (canAccess, canCreate, canEdit, canDelete) for a `Function` within a `Role`.
  - `Many-to-One` with `Role`.
  - `Many-to-One` with `Function`.
  - `RolePermissionId`: Embeddable ID for `RolePermission` (composite key of `role_id` and `function_id`).
- **AuditLog:** Records system events and administrative actions.
- **Skill:** Đại diện cho các kỹ năng chuyên môn, có cấu trúc phân cấp (parent-child).
- **University:** Represents a university. (No direct relationship inferred from provided code)
- **InternshipPosition:** Represents an internship opening.
  - Likely has relationships with `Department` and `Skill` (not explicitly shown).
- **InternshipProfile:** Represents an intern's profile.
  - Likely has relationships with `User`, `Skill`, `University` (not explicitly shown).
- **MicroTask:** Represents a small task.
  - Có mối quan hệ với `InternshipProfile`.
- **TaskSkillRating:** Rates a `Skill` for a `MicroTask`.
  - `Many-to-One` with `MicroTask`.
  - `Many-to-One` with `Skill`.
  - `TaskSkillRatingId`: Embeddable ID for `TaskSkillRating` (composite key of `micro_task_id` and `skill_id`).
- **InternshipDecision:** Lưu trữ quyết định cuối cùng của Manager cho một kỳ thực tập.
- **FinalEvaluation:** Bản đánh giá tổng kết từ Mentor trước khi gửi lên Manager.

## 3. Incomplete/Boilerplate Features (Tính năng còn dang dở hoặc chỉ có khung)

- **Limited CRUD Operations:** Mặc dù `InternshipProfile` và `User` đã khá hoàn thiện, các thực thể như `Department`, `Role`, `Skill`, `University` vẫn cần thêm các API quản lý đầy đủ (UI để Admin chỉnh sửa danh mục).
- **`AuditLog` Management:** `AuditLogService` and `AuditLogServiceImpl` exist, and logging is performed, but there are no exposed API endpoints (`@RestController`) to view, filter, or manage audit logs.
- **`EmailService`:** The interface and implementation exist for sending emails (specifically activation emails), but there are no generic email sending functionalities exposed or a comprehensive email template management system.
- **`SecurityService`:** The service interface `SecurityService.java` exists, but its implementation and public methods were not visible, suggesting it's either a placeholder or has internal functionalities not exposed via a controller.
- **User Account Activation:** An activation token and email sending mechanism are in place, but an explicit endpoint for _activating_ the user account using the token (e.g., `GET /api/auth/activate?token=...`) is not present in the provided controller files. Users are created inactive and rely on this (currently missing) activation step.
- **User Details (UserResponse, UserCreationRequest):** The `UserResponse` and `UserCreationRequest` payload objects are well-defined, but for a comprehensive user management system, endpoints for retrieving a list of users, getting a single user's details, or updating other user profile information are missing.

## 4. Technologies/Libraries Used (Công nghệ/Thư viện đang sử dụng)

- **Core Framework:** Spring Boot
- **Web Framework:** Spring Web (REST Controllers via `@RestController`)
- **Data Persistence:** Spring Data JPA (Repositories like `UserRepository`, `RoleRepository`, etc.)
- **Database Interaction:** Hibernate (ORM, inferred from JPA usage)
- **Security:** Spring Security (AuthenticationManager, PasswordEncoder, JWT-based authentication via `JwtTokenProvider`, `JwtAuthenticationFilter`, custom `UserDetailsService`)
- **JSON Processing:** Jackson (inferred for request/response bodies)
- **Validation:** Jakarta Bean Validation (`jakarta.validation.Valid`, `@RequestBody`)
- **Build Tool:** Maven (inferred from `pom.xml`, `mvnw`)
- **Language:** Java
- **Other (inferred):**
  - Logging framework (likely Logback/SLF4J, common in Spring Boot)
  - JUnit/Mockito for testing (inferred from `src/test` structure)

---

# internhub_be
