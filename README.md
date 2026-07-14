# NovaDigital CMS & Project Management System

A premium Content Management & Project Coordination System (CMS) designed by **NovaDigital**, optimized for team assignment, real-time project milestone tracking, and client collaboration.

---

## ⚡ Key Features

### 1. Authentication & Authorization
* **JWT Stateless Auth**: Secure token-based authentication stored in the browser's `LocalStorage`.
* **Role-Based Access Control (RBAC)**:
  * `ROLE_ADMIN`: Full system control (manage projects, members, services, and assignments).
  * `ROLE_MEMBER`: Internal team members (assigned as Project Managers or Staff/Coders).
  * `ROLE_USER`: External clients hiring projects.
* **Password Reset & OTP**: Standard password recovery with OTP confirmation codes sent via Email (SMTP).

### 2. Administrator Panel (`admin.html`)
* Complete CRUD management for projects, services, and internal members.
* **Project Assignment (UC-13)**: Assign team members to specific projects with designated roles (PM or Staff).
* **Client Linkage**: Connect external clients (`ROLE_USER`) to their respective projects.
* Support inbox monitoring and ticket resolution.

### 3. Member Portal (`member-contact.html`)
* **My Projects Dashboard**:
  * Clear visual separation: "Projects I Manage as PM" (full milestone controls) and "Projects I'm Assigned to as Staff" (read-only progress view).
  * Right-aligned section headers with clean, responsive grid layout card aesthetics.
* **Milestone Management**:
  * Project Managers (PM) can create new milestones, adjust completion percentages (%) via progress sliders, and update statuses (`PENDING`, `IN_PROGRESS`, `COMPLETED`, `BLOCKED`).
  * Comprehensive **Audit Logs** showing historical changes (who changed what field, old vs. new values, and timestamps) for each milestone.
* **Client Request Handling**: Respond directly to client support and contact messages.

### 4. Client Portal (`rented-project.html`)
* **Hired Projects Dashboard**: Clients can monitor the overall progress of all projects they have hired in a premium, personalized dashboard.
* **Milestone Progress Tracking**: Slide-out panel detail view showing specific milestone statuses, description, and completion percentages.
* **Audit History Logs**: Transparency for clients to view the timeline of changes (who modified which milestone, old values, new values, and dates).
* **SSE Live Stream Sync**: Connects to the Server-Sent Events stream to automatically apply updates on the page without reload.

### 5. Real-Time Sync (Server-Sent Events)
* Live **SSE Stream** broadcasted from the backend pushes milestone updates instantly to active Member and Client dashboards without requiring manual page refreshes.

### 6. Premium UI/UX & Theme Persistence
* Modern **Glassmorphism** styling with smooth hover transitions and animations.
* Integrated **Light/Dark Mode toggle** that persists across all pages using local storage, including custom dark styling for the Milestones slide-out panel to prevent glaring screen flashes.

---

## 🛠️ Tech Stack

### Backend:
* **Java 17** & **Spring Boot 4.1.0**
* **Spring Security** (Stateless JWT Filter)
* **Spring Data JPA** (Hibernate)
* **MySQL Database**
* **Spring Boot Starter Mail** (for OTP generation)

### Frontend:
* **HTML5** & **Vanilla CSS3** (Curated custom palette, dark/light theme variables)
* **Vanilla JavaScript** (SSE Client implementation, Event Listeners, Fetch API)

---

## 🚀 Setup & Installation

### 1. Database Configuration
1. Install and run MySQL Server.
2. Create a new database named `novadigitalusers`:
   ```sql
   CREATE DATABASE novadigitalusers CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   ```
3. Update your database credentials in `src/main/resources/application.properties`:
   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/novadigitalusers?allowPublicKeyRetrieval=true&useSSL=false
   spring.datasource.username=YOUR_MYSQL_USERNAME
   spring.datasource.password=YOUR_MYSQL_PASSWORD
   ```

### 2. Run the Application
Use the provided Maven Wrapper to compile and start the Spring Boot application:

* **Windows (PowerShell/CMD)**:
  ```powershell
  .\mvnw.cmd spring-boot:run
  ```
* **Linux / macOS**:
  ```bash
  chmod +x mvnw
  ./mvnw spring-boot:run
  ```

The server will start on default port `8080`: `http://localhost:8080`

### 3. Default Test Credentials
The system automatically seeds demo data if the database is empty. You can log in using the following accounts:

* **Administrator**:
  * Username: `admin` (or `admin@novadigital.com`)
  * Password: `admin123`
* **Project Manager / Member**:
  * Username: `mem1` (or `annguyen3@novadigital.com`)
  * Password: `123456`
* **Client / User**:
  * Username: `user` (or `user@novadigital.com`)
  * Password: `123456`

---

## 📁 Key Project Structure

```text
├── src/main/java/com/example/demo/
│   ├── config/             # Spring Security, JWT Filter, CORS, and DataSeeder configs
│   ├── controller/         # REST Controllers (Auth, Projects, Milestones, Contacts...)
│   ├── dto/                # Request/Response payloads (DTOs)
│   ├── entity/             # JPA Entities (User, Project, ProjectAssignment, ProjectMilestone...)
│   ├── repository/         # Spring Data JPA Repositories
│   └── service/            # Business Logic & Server-Sent Events (SSE) Broadcast Service
│
├── src/main/resources/
│   ├── static/             # Static frontend code (HTML, CSS, JS, Images, Uploads)
│   │   ├── css/style.css   # Global stylesheet with light/dark variables
│   │   ├── js/main.js      # Core JS handler (Auth Modals, API integrations, theme persistence...)
│   │   ├── admin.html      # Administrator management console
│   │   ├── member-contact.html # Combined Member Dashboard (PM Milestones & Support Contacts)
│   │   └── rented-project.html # Client's Rented Projects progress & audit logs panel
│   └── application.properties # Server port, JDBC datasource, and SMTP configurations
```
