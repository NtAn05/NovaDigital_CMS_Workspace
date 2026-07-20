package com.example.demo.config;

import com.example.demo.entity.*;
import com.example.demo.entity.enums.MilestoneStatus;
import com.example.demo.repository.*;
import com.example.demo.service.PasswordHasher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * DataSeeder: Automatically seeds the database with sample data on first startup.
 * Only runs if the `users` table is empty, preventing duplicate seeding on restarts.
 */
@Component
@Order(1)
public class DataSeeder implements CommandLineRunner {

    @Autowired private UserRepository userRepository;
    @Autowired private MemberRepository memberRepository;
    @Autowired private ServiceRepository serviceRepository;
    @Autowired private ProjectRepository projectRepository;
    @Autowired private ProjectAssignmentRepository projectAssignmentRepository;
    @Autowired private ProjectClientRepository projectClientRepository;
    @Autowired private ProjectMilestoneRepository projectMilestoneRepository;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        // Update existing services if they have null or empty iconUrl
        for (Service s : serviceRepository.findAll()) {
            if (s.getIconUrl() == null || s.getIconUrl().isBlank()) {
                if (s.getTitle().toLowerCase().contains("website") || s.getTitle().toLowerCase().contains("e-commerce")) {
                    s.setIconUrl("web");
                } else if (s.getTitle().toLowerCase().contains("ui/ux") || s.getTitle().toLowerCase().contains("design")) {
                    s.setIconUrl("design");
                } else if (s.getTitle().toLowerCase().contains("cloud") || s.getTitle().toLowerCase().contains("devops")) {
                    s.setIconUrl("cloud");
                } else {
                    s.setIconUrl("web");
                }
                serviceRepository.save(s);
            }
        }

        if (userRepository.count() > 0) {
            System.out.println(">>> [DataSeeder] Database already contains data. Skipping seed.");
            return;
        }

        System.out.println(">>> [DataSeeder] Seeding initial sample data...");

        // ── 1. Users ────────────────────────────────────────────────────────
        User admin = createUser("admin",    "admin123", "Administrator",  "admin@novadigital.com",     "0987654321", "ROLE_ADMIN");
        User mem1  = createUser("mem1",     "123456",   "An Nguyen3",     "annguyen3@novadigital.com", "0123456781", "ROLE_MEMBER");
        User mem2  = createUser("mem2",     "123456",   "Ho Huy A",       "hohuya@novadigital.com",    "0123456782", "ROLE_MEMBER");
        User user1 = createUser("user",     "123456",   "Demo User",      "user@novadigital.com",      "0999999991", "ROLE_USER");
        User user2 = createUser("anlol2k5", "123456",   "Ho Huy",         "hohuy@novadigital.com",     "0999999992", "ROLE_USER");

        userRepository.save(admin);
        userRepository.save(mem1);
        userRepository.save(mem2);
        userRepository.save(user1);
        userRepository.save(user2);

        // ── 2. Members (public team page) ───────────────────────────────────
        Member member1 = new Member();
        member1.setName("An Nguyen3");
        member1.setRole("PROJECT LEADER");
        member1.setUserId(mem1.getId());
        member1.setSkills("Spring Boot, Java, AWS, Microservices");
        member1.setProjects("Mart06 Fashion System, NovaDigital Mobile Portal");
        memberRepository.save(member1);

        Member member2 = new Member();
        member2.setName("Ho Huy A");
        member2.setRole("MEMBER");
        member2.setUserId(mem2.getId());
        member2.setSkills("React, CSS3, Figma, UI/UX Design");
        member2.setProjects("NovaDigital Mobile Portal");
        memberRepository.save(member2);

        // ── 3. Services ─────────────────────────────────────────────────────
        Service svc1 = new Service();
        svc1.setTitle("E-Commerce Website Development");
        svc1.setDescription("Build high-performance online stores with automated payment, order tracking, and clean admin controls tailored to your brand.");
        svc1.setIconUrl("web");
        serviceRepository.save(svc1);

        Service svc2 = new Service();
        svc2.setTitle("Mobile UI/UX Design");
        svc2.setDescription("Create modern, premium interfaces for iOS and Android, fully customized to captivate and engage your audience.");
        svc2.setIconUrl("design");
        serviceRepository.save(svc2);

        Service svc3 = new Service();
        svc3.setTitle("Cloud Infrastructure & DevOps");
        svc3.setDescription("Design and deploy scalable cloud architecture on AWS or GCP with CI/CD pipelines, container orchestration, and monitoring.");
        svc3.setIconUrl("cloud");
        serviceRepository.save(svc3);

        // ── 4. Projects ─────────────────────────────────────────────────────
        Project proj1 = new Project();
        proj1.setTitle("Mart06 Fashion System");
        proj1.setDescription("High-performance online shopping platform with seamless automated payment integration and full inventory management.");
        proj1.setCategory("Website E-Commerce");
        proj1.setTechnologies("Java, Spring Boot, MySQL, Thymeleaf, CSS3");
        projectRepository.save(proj1);

        Project proj2 = new Project();
        proj2.setTitle("NovaDigital Mobile Portal");
        proj2.setDescription("Premium mobile application for project coordination, client messaging, and real-time SSE milestone progress alerts.");
        proj2.setCategory("Mobile Application");
        proj2.setTechnologies("React Native, Node.js, SSE, MySQL");
        projectRepository.save(proj2);

        Project proj3 = new Project();
        proj3.setTitle("CloudPay Analytics Dashboard");
        proj3.setDescription("SaaS analytics dashboard with real-time financial reporting, role-based access control, and Stripe billing integration.");
        proj3.setCategory("Cloud SaaS");
        proj3.setTechnologies("Vue.js, Spring Boot, PostgreSQL, Docker");
        projectRepository.save(proj3);

        // ── 5. Project Assignments (PM / STAFF) ─────────────────────────────
        // mem1 = PM on proj1, STAFF on proj2
        // mem2 = PM on proj2, STAFF on proj3
        saveAssignment(proj1, mem1, ProjectAssignment.ProjectRole.PM);
        saveAssignment(proj2, mem1, ProjectAssignment.ProjectRole.STAFF);
        saveAssignment(proj2, mem2, ProjectAssignment.ProjectRole.PM);
        saveAssignment(proj3, mem2, ProjectAssignment.ProjectRole.STAFF);

        // ── 6. Project Clients (which client hired which project) ────────────
        saveProjectClient(proj1, user1);
        saveProjectClient(proj2, user2);
        saveProjectClient(proj3, user1);

        // ── 7. Project Milestones ────────────────────────────────────────────
        // proj1 milestones
        saveMilestone(proj1, "Phase 1: High-Fidelity Figma Designs",
                "Drafting interactive layouts and responsive styling for desktop and mobile views.",
                MilestoneStatus.COMPLETED, 100, LocalDate.now().minusDays(10));

        saveMilestone(proj1, "Phase 2: Database Schema & REST APIs",
                "Coding JPA entities, repositories, Spring Security configs, and JUnit integration tests.",
                MilestoneStatus.IN_PROGRESS, 65, LocalDate.now().plusDays(7));

        saveMilestone(proj1, "Phase 3: Stripe Integration & Cloud Launch",
                "Integrating Stripe API webhooks and deploying production release to AWS.",
                MilestoneStatus.PENDING, 0, LocalDate.now().plusDays(21));

        // proj2 milestones
        saveMilestone(proj2, "Figma Prototype Mockup",
                "Creating interactive layouts for chat channels and milestone progress panels.",
                MilestoneStatus.COMPLETED, 100, LocalDate.now().minusDays(5));

        saveMilestone(proj2, "Live SSE Broadcasting Stream",
                "Configuring Server-Sent Events controller to push milestone mutations to all active clients.",
                MilestoneStatus.DELAYED, 40, LocalDate.now().minusDays(1));

        saveMilestone(proj2, "Beta Release & User Acceptance Testing",
                "Distribute beta build to selected users for feedback and final QA sign-off.",
                MilestoneStatus.PENDING, 0, LocalDate.now().plusDays(14));

        // proj3 milestones
        saveMilestone(proj3, "Requirement Analysis & Architecture Design",
                "Defining system architecture, microservices boundaries, and API contracts with stakeholders.",
                MilestoneStatus.COMPLETED, 100, LocalDate.now().minusDays(20));

        saveMilestone(proj3, "Core Dashboard Frontend Build",
                "Building Vue.js data visualization components, charts, and responsive table grids.",
                MilestoneStatus.IN_PROGRESS, 50, LocalDate.now().plusDays(10));

        System.out.println(">>> [DataSeeder] Sample data seeded successfully!");
    }

    // ── Helper builders ──────────────────────────────────────────────────────

    private User createUser(String username, String password, String fullName, String email, String phone, String role) {
        User u = new User();
        u.setUsername(username);
        u.setPassword(PasswordHasher.hash(password));
        u.setFullName(fullName);
        u.setEmail(email);
        u.setPhone(phone);
        u.setRole(role);
        u.setEnabled(true);
        return u;
    }

    private void saveAssignment(Project project, User user, ProjectAssignment.ProjectRole role) {
        ProjectAssignment a = new ProjectAssignment();
        a.setProject(project);
        a.setUser(user);
        a.setProjectRole(role);
        projectAssignmentRepository.save(a);
    }

    private void saveProjectClient(Project project, User user) {
        ProjectClient pc = new ProjectClient();
        pc.setProject(project);
        pc.setUser(user);
        projectClientRepository.save(pc);
    }

    private void saveMilestone(Project project, String name, String description,
                               MilestoneStatus status, int progress, LocalDate dueDate) {
        ProjectMilestone m = new ProjectMilestone();
        m.setProject(project);
        m.setName(name);
        m.setDescription(description);
        m.setStatus(status);
        m.setProgressPercentage(progress);
        m.setDueDate(dueDate);
        m.setPrice(1000.0 + (double) (Math.abs(name.hashCode()) % 5) * 500.0);
        m.setPaid(false);
        projectMilestoneRepository.save(m);
    }
}
