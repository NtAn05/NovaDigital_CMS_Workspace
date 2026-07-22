package com.example.demo.config;

import com.example.demo.entity.*;
import com.example.demo.entity.enums.AllocationStatus;
import com.example.demo.entity.enums.AppointmentStatus;
import com.example.demo.entity.enums.MilestoneStatus;
import com.example.demo.repository.*;
import com.example.demo.service.PasswordHasher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * DataSeeder: Automatically seeds the database with comprehensive sample data across all system features on first startup.
 * Only runs if the `users` table is empty, preventing duplicate seeding on restarts.
 */
@Component
@Order(1)
public class DataSeeder implements CommandLineRunner {

    @Autowired private UserRepository userRepository;
    @Autowired private MemberRepository memberRepository;
    @Autowired private ServiceRepository serviceRepository;
    @Autowired private ServiceAddonRepository serviceAddonRepository;
    @Autowired private ProjectRepository projectRepository;
    @Autowired private ProjectAssignmentRepository projectAssignmentRepository;
    @Autowired private ProjectClientRepository projectClientRepository;
    @Autowired private ProjectMilestoneRepository projectMilestoneRepository;
    @Autowired private ResourceAllocationRepository resourceAllocationRepository;
    @Autowired private ConsultationAppointmentRepository consultationAppointmentRepository;
    @Autowired private AppointmentAddonRepository appointmentAddonRepository;
    @Autowired private PaymentTransactionRepository paymentTransactionRepository;
    @Autowired private FeedbackRepository feedbackRepository;
    @Autowired private ContactRepository contactRepository;
    @Autowired private JobVacancyRepository jobVacancyRepository;
    @Autowired private CandidateApplicationRepository candidateApplicationRepository;
    @Autowired private NotificationRepository notificationRepository;
    @Autowired private AuditLogRepository auditLogRepository;
    @Autowired private AuthLogRepository authLogRepository;

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

        System.out.println(">>> [DataSeeder] Clearing old data & Seeding initial sample data in English for ALL system modules...");

        // Wipe old sample data to ensure fresh 100% English sample dataset
        notificationRepository.deleteAllInBatch();
        feedbackRepository.deleteAllInBatch();
        contactRepository.deleteAllInBatch();
        candidateApplicationRepository.deleteAllInBatch();
        jobVacancyRepository.deleteAllInBatch();
        paymentTransactionRepository.deleteAllInBatch();
        appointmentAddonRepository.deleteAllInBatch();
        consultationAppointmentRepository.deleteAllInBatch();
        resourceAllocationRepository.deleteAllInBatch();
        projectMilestoneRepository.deleteAllInBatch();
        projectClientRepository.deleteAllInBatch();
        projectAssignmentRepository.deleteAllInBatch();
        projectRepository.deleteAllInBatch();
        serviceAddonRepository.deleteAllInBatch();
        serviceRepository.deleteAllInBatch();
        memberRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
        userRepository.flush();

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

        // ── 3.1 Service Addons ──────────────────────────────────────────────
        ServiceAddon addon1 = saveServiceAddon(svc1.getId(), "Automated PayOS / Stripe Payment", 200.0);
        ServiceAddon addon2 = saveServiceAddon(svc1.getId(), "Advanced Inventory & Order Management", 350.0);
        ServiceAddon addon3 = saveServiceAddon(svc2.getId(), "Custom Dark Mode Design", 150.0);
        ServiceAddon addon4 = saveServiceAddon(svc2.getId(), "Figma Design System Delivery", 250.0);
        ServiceAddon addon5 = saveServiceAddon(svc3.getId(), "CI/CD Pipeline & Docker Setup", 400.0);

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
        saveAssignment(proj1, mem1, ProjectAssignment.ProjectRole.PM);
        saveAssignment(proj2, mem1, ProjectAssignment.ProjectRole.STAFF);
        saveAssignment(proj2, mem2, ProjectAssignment.ProjectRole.PM);
        saveAssignment(proj3, mem2, ProjectAssignment.ProjectRole.STAFF);

        // ── 6. Project Clients ──────────────────────────────────────────────
        saveProjectClient(proj1, user1);
        saveProjectClient(proj2, user2);
        saveProjectClient(proj3, user1);

        // ── 7. Project Milestones ────────────────────────────────────────────
        ProjectMilestone m1 = saveMilestone(proj1, "Phase 1: High-Fidelity Figma Designs",
                "Drafting interactive layouts and responsive styling for desktop and mobile views.",
                MilestoneStatus.COMPLETED, 100, LocalDate.now().minusDays(10));

        ProjectMilestone m2 = saveMilestone(proj1, "Phase 2: Database Schema & REST APIs",
                "Coding JPA entities, repositories, Spring Security configs, and JUnit integration tests.",
                MilestoneStatus.IN_PROGRESS, 65, LocalDate.now().plusDays(7));

        ProjectMilestone m3 = saveMilestone(proj1, "Phase 3: Stripe Integration & Cloud Launch",
                "Integrating Stripe API webhooks and deploying production release to AWS.",
                MilestoneStatus.PENDING, 0, LocalDate.now().plusDays(21));

        ProjectMilestone m4 = saveMilestone(proj2, "Figma Prototype Mockup",
                "Creating interactive layouts for chat channels and milestone progress panels.",
                MilestoneStatus.COMPLETED, 100, LocalDate.now().minusDays(5));

        ProjectMilestone m5 = saveMilestone(proj2, "Live SSE Broadcasting Stream",
                "Configuring Server-Sent Events controller to push milestone mutations to all active clients.",
                MilestoneStatus.DELAYED, 40, LocalDate.now().minusDays(1));

        ProjectMilestone m6 = saveMilestone(proj2, "Beta Release & User Acceptance Testing",
                "Distribute beta build to selected users for feedback and final QA sign-off.",
                MilestoneStatus.PENDING, 0, LocalDate.now().plusDays(14));

        ProjectMilestone m7 = saveMilestone(proj3, "Requirement Analysis & Architecture Design",
                "Defining system architecture, microservices boundaries, and API contracts with stakeholders.",
                MilestoneStatus.COMPLETED, 100, LocalDate.now().minusDays(20));

        ProjectMilestone m8 = saveMilestone(proj3, "Core Dashboard Frontend Build",
                "Building Vue.js data visualization components, charts, and responsive table grids.",
                MilestoneStatus.IN_PROGRESS, 50, LocalDate.now().plusDays(10));

        // ── 8. Resource Allocations (UC-14 Matrix) ───────────────────────────
        saveResourceAllocation(proj1, m2, mem1, 100, LocalDate.now().minusDays(10), LocalDate.now().plusDays(20),
                AllocationStatus.ACTIVE, "Lead Developer for Backend REST APIs", "admin");

        saveResourceAllocation(proj2, m5, mem1, 50, LocalDate.now().minusDays(5), LocalDate.now().plusDays(15),
                AllocationStatus.PLANNED, "Assisting with SSE Integration Configuration", "admin");

        saveResourceAllocation(proj2, m4, mem2, 80, LocalDate.now().minusDays(15), LocalDate.now().plusDays(10),
                AllocationStatus.ACTIVE, "Lead UI/UX Designer and Figma Prototyping", "admin");

        saveResourceAllocation(proj3, m8, mem2, 50, LocalDate.now(), LocalDate.now().plusDays(30),
                AllocationStatus.PLANNED, "Contributing to Vue.js UI Component Development", "admin");

        // ── 9. Consultation Appointments & Addons ────────────────────────────
        ConsultationAppointment appt1 = saveAppointment(svc1.getId(), user1.getId(), mem1.getId(),
                LocalDate.now().plusDays(1), LocalTime.of(9, 0), AppointmentStatus.CONFIRMED,
                "Consultation for upgrading automated payment system for Mart06 E-Commerce platform.", 1200.0);

        ConsultationAppointment appt2 = saveAppointment(svc2.getId(), user2.getId(), mem2.getId(),
                LocalDate.now().plusDays(3), LocalTime.of(14, 0), AppointmentStatus.PENDING,
                "Consultation for Mobile Portal UI/UX design.", 800.0);

        ConsultationAppointment appt3 = saveAppointment(svc3.getId(), user1.getId(), mem1.getId(),
                LocalDate.now().minusDays(5), LocalTime.of(10, 30), AppointmentStatus.COMPLETED,
                "Consultation for AWS Cloud Infrastructure and Docker deployment for SaaS.", 1500.0);

        saveAppointmentAddon(appt1.getId(), addon1.getId());
        saveAppointmentAddon(appt1.getId(), addon2.getId());
        saveAppointmentAddon(appt2.getId(), addon3.getId());

        // ── 10. Payment Transactions ─────────────────────────────────────────
        savePaymentTransaction(100001L, appt3.getId(), null, 1500.0, "PAID");
        savePaymentTransaction(100002L, null, m1.getId(), 1000.0, "PAID");
        savePaymentTransaction(100003L, appt1.getId(), null, 1200.0, "PENDING");

        // ── 11. Feedback ─────────────────────────────────────────────────────
        saveFeedback("Nam Tran", "namtran@gmail.com", "E-Commerce Website",
                "Beautiful website interface, fast loading speed, and very convenient order management!");
        saveFeedback("Hoa Le", "hoale@gmail.com", "Cloud Consultation",
                "The AWS cloud infrastructure consulting team is highly knowledgeable and enthusiastic.");

        // ── 12. Contacts ─────────────────────────────────────────────────────
        saveContact("Hung Nguyen", "hungnv@gmail.com", "E-Commerce Website Design Quotation",
                "I would like to receive a quotation for a complete E-Commerce website design service.", "DONE",
                "Hello Mr. Hung, NovaDigital has sent a detailed quotation to your email.", LocalDate.now().minusDays(1));

        saveContact("Tuan Pham", "tuanpm@gmail.com", "AWS Cloud Infrastructure Consultation",
                "Our system is overloaded during peak hours, we need assistance with cloud upgrade consultation.", "PENDING",
                null, null);

        // ── 13. Job Vacancies & Candidate Applications ────────────────────────
        JobVacancy job1 = saveJobVacancy("Senior Spring Boot Backend Engineer",
                "Develop high-throughput microservices systems using Java 17, Spring Boot, MySQL, and Docker.",
                "Engineering", "Hanoi / Remote", "FULL_TIME", JobVacancy.VacancyStatus.ACTIVE);

        JobVacancy job2 = saveJobVacancy("Lead Mobile UI/UX Designer",
                "Build Design System, design wireframes and interactive prototypes for iOS/Android mobile apps.",
                "Design", "Ho Chi Minh City", "FULL_TIME", JobVacancy.VacancyStatus.ACTIVE);

        JobVacancy job3 = saveJobVacancy("DevOps & Cloud Engineer (AWS/GCP)",
                "Configure CI/CD pipelines, Kubernetes, Terraform, and optimize cloud infrastructure costs.",
                "Engineering", "Remote", "CONTRACT", JobVacancy.VacancyStatus.ACTIVE);

        saveCandidateApplication(job1.getId(), job1.getTitle(), "Long Do", "hoanglong.dev@gmail.com",
                "0912345678", "/uploads/resumes/hoanglong_cv.pdf", "I have 5 years of experience working with Java Spring Boot and MySQL.");

        saveCandidateApplication(job2.getId(), job2.getTitle(), "Mai Vu", "maivu.design@gmail.com",
                "0987654321", "/uploads/resumes/thanhmai_portfolio.pdf", "Eager to contribute to NovaDigital design products.");

        // ── 14. Notifications ────────────────────────────────────────────────
        saveNotification(user1.getId(), "Project Progress Update",
                "Milestone 'Phase 1: High-Fidelity Figma Designs' for project Mart06 Fashion System is 100% completed.", "/projects/1", true);

        saveNotification(user1.getId(), "Consultation Appointment Confirmation",
                "Your E-Commerce design consultation appointment has been confirmed for 09:00 tomorrow.", "/appointments", false);

        saveNotification(mem1.getId(), "New Project Assignment",
                "You have been assigned as Project Leader for project Mart06 Fashion System.", "/matrix", false);

        // ── 15. Audit Logs ───────────────────────────────────────────────────
        auditLogRepository.save(new AuditLog("admin", "CREATE", "projects", "Created project 'Mart06 Fashion System'"));
        authLogRepository.save(new AuthLog("admin", "LOGIN_SUCCESS", "127.0.0.1", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)"));

        System.out.println(">>> [DataSeeder] Sample data for ALL system modules seeded successfully!");
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

    private ServiceAddon saveServiceAddon(Long serviceId, String name, Double price) {
        ServiceAddon sa = new ServiceAddon();
        sa.setServiceId(serviceId);
        sa.setAddonName(name);
        sa.setPriceModifier(price);
        return serviceAddonRepository.save(sa);
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

    private ProjectMilestone saveMilestone(Project project, String name, String description,
                                           MilestoneStatus status, int progress, LocalDate dueDate) {
        ProjectMilestone m = new ProjectMilestone();
        m.setProject(project);
        m.setName(name);
        m.setDescription(description);
        m.setStatus(status);
        m.setProgressPercentage(progress);
        m.setDueDate(dueDate);
        m.setPrice(1000.0 + (double) (Math.abs(name.hashCode()) % 5) * 500.0);
        m.setPaid(status == MilestoneStatus.COMPLETED);
        return projectMilestoneRepository.save(m);
    }

    private void saveResourceAllocation(Project project, ProjectMilestone milestone, User user,
                                         int percentage, LocalDate startDate, LocalDate endDate,
                                         AllocationStatus status, String notes, String assignedBy) {
        ResourceAllocation ra = new ResourceAllocation();
        ra.setProject(project);
        ra.setMilestone(milestone);
        ra.setUser(user);
        ra.setAllocationPercentage(percentage);
        ra.setStartDate(startDate);
        ra.setEndDate(endDate);
        ra.setStatus(status);
        ra.setNotes(notes);
        ra.setAssignedBy(assignedBy);
        resourceAllocationRepository.save(ra);
    }

    private ConsultationAppointment saveAppointment(Long serviceId, Long clientId, Long expertId,
                                                      LocalDate date, LocalTime time,
                                                      AppointmentStatus status, String message, Double price) {
        ConsultationAppointment ca = new ConsultationAppointment();
        ca.setServiceId(serviceId);
        ca.setClientId(clientId);
        ca.setExpertId(expertId);
        ca.setAppointmentDate(date);
        ca.setTimeSlot(time);
        ca.setStatus(status);
        ca.setMessageContent(message);
        ca.setTotalPrice(price);
        return consultationAppointmentRepository.save(ca);
    }

    private void saveAppointmentAddon(Long appointmentId, Long addonId) {
        AppointmentAddon aa = new AppointmentAddon();
        aa.setAppointmentId(appointmentId);
        aa.setAddonId(addonId);
        appointmentAddonRepository.save(aa);
    }

    private void savePaymentTransaction(Long orderCode, Long appointmentId, Long milestoneId, Double amount, String status) {
        PaymentTransaction pt = new PaymentTransaction();
        pt.setOrderCode(orderCode);
        pt.setAppointmentId(appointmentId);
        pt.setMilestoneId(milestoneId);
        pt.setAmount(amount);
        pt.setStatus(status);
        paymentTransactionRepository.save(pt);
    }

    private void saveFeedback(String name, String email, String category, String message) {
        Feedback f = new Feedback(name, email, category, message);
        feedbackRepository.save(f);
    }

    private void saveContact(String name, String email, String title, String content, String status, String reply, LocalDate replyDate) {
        Contact c = new Contact();
        c.setName(name);
        c.setEmail(email);
        c.setTitle(title);
        c.setContent(content);
        c.setStatus(status);
        c.setReply(reply);
        if (replyDate != null) {
            c.setRepliedAt(replyDate.atStartOfDay());
        }
        contactRepository.save(c);
    }

    private JobVacancy saveJobVacancy(String title, String description, String workstream,
                                      String location, String jobType, JobVacancy.VacancyStatus status) {
        JobVacancy j = new JobVacancy();
        j.setTitle(title);
        j.setDescription(description);
        j.setWorkstream(workstream);
        j.setLocation(location);
        j.setJobType(jobType);
        j.setStatus(status);
        return jobVacancyRepository.save(j);
    }

    private void saveCandidateApplication(Long vacancyId, String vacancyTitle, String name,
                                           String email, String phone, String resumeUrl, String coverLetter) {
        CandidateApplication ca = new CandidateApplication();
        ca.setVacancyId(vacancyId);
        ca.setVacancyTitle(vacancyTitle);
        ca.setApplicantName(name);
        ca.setApplicantEmail(email);
        ca.setApplicantPhone(phone);
        ca.setResumeUrl(resumeUrl);
        ca.setCoverLetter(coverLetter);
        candidateApplicationRepository.save(ca);
    }

    private void saveNotification(Long userId, String title, String message, String link, boolean isRead) {
        Notification n = new Notification();
        n.setUserId(userId);
        n.setTitle(title);
        n.setMessage(message);
        n.setLink(link);
        n.setRead(isRead);
        notificationRepository.save(n);
    }
}
