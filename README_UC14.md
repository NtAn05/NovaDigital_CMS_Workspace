# UC-14 — Staff Resource Allocation Matrix

## What was implemented

UC-14 allows HR/Admin and assigned Project Managers to allocate internal staff members to a project or an individual project task/milestone based on recorded skills and workload availability.

Implemented areas:

- New `resource_allocations` database entity/table.
- Project-level and task/milestone-level staff allocations.
- Allocation percentage, start/end date, status, notes, and assigned-by audit field.
- Workload calculation across every project for a selected date.
- Validation preventing planned/active workload from exceeding 100%.
- Validation preventing overlapping duplicate allocations for the same staff/project/task.
- Validation that the selected task belongs to the selected project.
- Only enabled `ROLE_MEMBER` accounts can be allocated.
- `ROLE_ADMIN` acts as HR/Admin and may manage every project.
- A `ROLE_MEMBER` may manage only projects where that user has project role `PM`.
- Creating an allocation also ensures the staff member has a `STAFF` project assignment, without replacing an existing PM assignment.
- Responsive `resource-allocation.html` matrix with search, skill filter, availability filter, workload date, CRUD modal, workload bars, and skill-match indicator.
- Navigation links from Admin Panel and PM Dashboard.

## Files added

- `src/main/java/com/example/demo/entity/ResourceAllocation.java`
- `src/main/java/com/example/demo/entity/enums/AllocationStatus.java`
- `src/main/java/com/example/demo/repository/ResourceAllocationRepository.java`
- `src/main/java/com/example/demo/service/ResourceAllocationService.java`
- `src/main/java/com/example/demo/controller/ResourceAllocationController.java`
- `src/main/java/com/example/demo/dto/ResourceAllocationRequest.java`
- `src/main/java/com/example/demo/dto/ResourceAllocationResponse.java`
- `src/main/java/com/example/demo/dto/ResourceMatrixResponse.java`
- `src/main/java/com/example/demo/dto/ResourceProjectOption.java`
- `src/main/java/com/example/demo/dto/ResourceTaskOption.java`
- `src/main/java/com/example/demo/dto/ResourceStaffRow.java`
- `src/main/java/com/example/demo/util/WorkloadCapacityCalculator.java`
- `src/main/resources/static/resource-allocation.html`
- `src/test/java/com/example/demo/util/WorkloadCapacityCalculatorTest.java`
- `database_update_uc14.sql`
- `README_UC14.md`

## Files changed

- `src/main/java/com/example/demo/config/SecurityConfig.java`
- `src/main/resources/static/admin.html`
- `src/main/resources/static/pm-dashboard.html`

No changes were made to `pom.xml` or `src/main/resources/application.properties`.

## Database setup

The project already uses:

```properties
spring.jpa.hibernate.ddl-auto=update
```

Therefore, starting Spring Boot will normally create `resource_allocations` automatically.

For a controlled/manual update, open MySQL Workbench, select the `novadigitalusers` database, and run:

```text
database_update_uc14.sql
```

Do not run this script in Microsoft SQL Server Management Studio because this project is configured for MySQL.

## Build and run on Windows

Open PowerShell in the project root containing `pom.xml`:

```powershell
.\mvnw.cmd clean test
```

Expected result:

```text
BUILD SUCCESS
```

Then run:

```powershell
.\mvnw.cmd spring-boot:run
```

Open:

```text
http://localhost:8080/login.html
```

## Test as HR/Admin

1. Sign in:

```text
Username: admin
Password: admin123
```

2. Open **Admin Panel**.
3. Click **Resource Allocation** in the left sidebar.
4. Select a project.
5. Confirm that staff skills, skill match, total workload, selected-project workload, and availability appear.
6. Click **New Allocation** or **+ Assign** on a staff row.
7. Select project-level allocation or a task/milestone.
8. Enter a percentage from 1 to 100, start date, end date, status, and optional notes.
9. Save and confirm the new record appears immediately.
10. Refresh the page and confirm the record remains.
11. Edit the allocation and verify the changes.
12. Delete the allocation and verify it disappears.

## Test as Project Manager

1. Sign out and sign in:

```text
Username: mem1
Password: 123456
```

2. Open **PM Dashboard**.
3. Click **Resource Allocation**.
4. Confirm only projects where `mem1` is assigned as PM appear.
5. Create/edit/delete an allocation on an allowed project.
6. Try manually changing the URL/API project ID to a project where `mem1` is only STAFF. The API must return HTTP 403.

## Validation test cases

### 1. Workload cannot exceed 100%

1. Create an active allocation of 80% for one staff member over a date range.
2. Create another active/planned allocation of 30% for the same staff member with overlapping dates.
3. Expected result: request is rejected with a message similar to:

```text
Workload limit exceeded: <staff> would reach 110% on <date>. Maximum allowed workload is 100%.
```

### 2. Completed and cancelled records do not consume capacity

Create a `COMPLETED` or `CANCELLED` allocation. It remains visible for history but does not reduce current availability.

### 3. End date validation

Set the end date before the start date. Expected: HTTP 400 and a clear validation message.

### 4. Wrong task/project validation

Attempt to submit a milestone ID belonging to another project. Expected: HTTP 400.

### 5. Duplicate overlap validation

Create a second planned/active allocation for the same staff, project, and task over an overlapping period. Expected: HTTP 400.

### 6. Role validation

A normal `ROLE_USER` account cannot open or call UC-14 APIs. A member who is not PM on a project cannot manage that project's allocations.

## Database verification

Run in MySQL Workbench:

```sql
USE novadigitalusers;

SELECT ra.id,
       u.full_name AS staff_name,
       p.title AS project_title,
       pm.name AS task_name,
       ra.allocation_percentage,
       ra.start_date,
       ra.end_date,
       ra.status,
       ra.assigned_by
FROM resource_allocations ra
JOIN users u ON u.id = ra.user_id
JOIN projects p ON p.id = ra.project_id
LEFT JOIN project_milestones pm ON pm.id = ra.milestone_id
ORDER BY ra.id DESC;
```

## Push to the requested branch

Confirm the active branch:

```powershell
git branch --show-current
```

Expected:

```text
feature/doan-uc14-resource-allocation
```

Then:

```powershell
git status
.\mvnw.cmd clean test
git add .
git commit -m "Implement UC-14 staff resource allocation matrix"
git push -u origin feature/doan-uc14-resource-allocation
```
