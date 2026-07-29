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
 * DataSeeder: Automatically seeds the database with massive, rich sample data across all system modules on startup.
 * Seeds if database has fewer than 15 projects to ensure a flooded, realistic dataset.
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
        if (projectRepository.count() >= 15) {
            System.out.println(">>> [DataSeeder] Massive sample dataset already present (15+ projects). Skipping seeding.");
            return;
        }

        System.out.println(">>> [DataSeeder] Seeding MASSIVE sample dataset for ALL system modules (Projects, Services, Members, Contacts, Allocations)...");

        // ── 1. Users (Admin, 10 Members, 4 Clients) ───────────────────────────
        User admin = findOrCreateUser("admin",    "admin123", "Administrator",     "admin@novadigital.com",     "0987654321", "ROLE_ADMIN");
        User mem1  = findOrCreateUser("mem1",     "123456",   "An Nguyen3",        "annguyen3@novadigital.com", "0123456781", "ROLE_MEMBER");
        User mem2  = findOrCreateUser("mem2",     "123456",   "Ho Huy A",          "hohuya@novadigital.com",    "0123456782", "ROLE_MEMBER");
        User mem3  = findOrCreateUser("mem3",     "123456",   "Tran Duc Minh",     "minhtd@novadigital.com",    "0123456783", "ROLE_MEMBER");
        User mem4  = findOrCreateUser("mem4",     "123456",   "Pham Thi Thanh",    "thanhpt@novadigital.com",   "0123456784", "ROLE_MEMBER");
        User mem5  = findOrCreateUser("mem5",     "123456",   "Le Hoang Nam",      "namlh@novadigital.com",     "0123456785", "ROLE_MEMBER");
        User mem6  = findOrCreateUser("mem6",     "123456",   "Vu Hoang Anh",      "anhvh@novadigital.com",     "0123456786", "ROLE_MEMBER");
        User mem7  = findOrCreateUser("mem7",     "123456",   "Dang Quoc Bao",     "baodq@novadigital.com",     "0123456787", "ROLE_MEMBER");
        User mem8  = findOrCreateUser("mem8",     "123456",   "Nguyen Thu Ha",     "hant@novadigital.com",      "0123456788", "ROLE_MEMBER");
        User mem9  = findOrCreateUser("mem9",     "123456",   "Bui Van Dung",      "dungbv@novadigital.com",    "0123456789", "ROLE_MEMBER");
        User mem10 = findOrCreateUser("mem10",    "123456",   "Do Phuong Thao",    "thaodp@novadigital.com",    "0123456790", "ROLE_MEMBER");

        User user1 = findOrCreateUser("user",     "123456",   "Demo User",         "user@novadigital.com",      "0999999991", "ROLE_USER");
        User user2 = findOrCreateUser("anlol2k5", "123456",   "Ho Huy",            "hohuy@novadigital.com",     "0999999992", "ROLE_USER");
        User user3 = findOrCreateUser("client3",  "123456",   "Nguyen Van Thanh",  "thanhnv@client.com",        "0999999993", "ROLE_USER");
        User user4 = findOrCreateUser("client4",  "123456",   "Tran Thi Bich",     "bichtt@client.com",         "0999999994", "ROLE_USER");

        // ── 2. Members (10 Public Team Page & Matrix) ──────────────────────────
        saveMemberIfMissing("An Nguyen3", "PROJECT LEADER", mem1.getId(), "Spring Boot, Java, AWS, Microservices", "Mart06 Fashion System, Enterprise ERP Portal");
        saveMemberIfMissing("Ho Huy A", "UI/UX DESIGN LEAD", mem2.getId(), "React, CSS3, Figma, Design System, UI/UX", "NovaDigital Mobile Portal, CloudPay Analytics Dashboard");
        saveMemberIfMissing("Tran Duc Minh", "DEVOPS ARCHITECT", mem3.getId(), "Docker, Kubernetes, AWS, Terraform, CI/CD", "Smart Freight Tracking System, CloudPay Dashboard");
        saveMemberIfMissing("Pham Thi Thanh", "MOBILE LEAD", mem4.getId(), "Flutter, React Native, iOS, Android, Dart", "FinTech Mobile Wallet & Banking App");
        saveMemberIfMissing("Le Hoang Nam", "FULLSTACK & AI ENGINEER", mem5.getId(), "Python, FastAPI, React, Node.js, OpenAI", "AI-Powered Customer Care Bot & Automation");
        saveMemberIfMissing("Vu Hoang Anh", "FRONTEND SPECIALIST", mem6.getId(), "Vue.js, React, Next.js, TailwindCSS, TypeScript", "EduHub Online Learning & Exam Platform");
        saveMemberIfMissing("Dang Quoc Bao", "BACKEND ENGINEER", mem7.getId(), "Golang, Spring Boot, PostgreSQL, Redis, gRPC", "SmartCity IoT Environmental Monitoring System");
        saveMemberIfMissing("Nguyen Thu Ha", "QA & AUTOMATION LEAD", mem8.getId(), "Selenium, Cypress, JUnit, Postman, JMeter", "MedCare Telehealth & Online Booking Platform");
        saveMemberIfMissing("Bui Van Dung", "CYBERSECURITY SPECIALIST", mem9.getId(), "Ethical Hacking, OWASP, Penetration Testing, Linux", "CryptoPay Web3 Decentralized Exchange");
        saveMemberIfMissing("Do Phuong Thao", "DATA SCIENTIST & ML ENGINEER", mem10.getId(), "Python, PyTorch, TensorFlow, Pandas, Scikit-learn", "BigData Predictive Sales Forecasting Engine");

        // ── 3. Services (8 Services & 24 Addons) ──────────────────────────────
        Service svc1 = saveServiceIfMissing("E-Commerce Website Development", "Build high-performance online stores with automated payment, order tracking, and clean admin controls tailored to your brand.", "web");
        Service svc2 = saveServiceIfMissing("Mobile UI/UX & App Development", "Create modern, premium interfaces for iOS and Android, fully customized to captivate and engage your audience.", "mobile");
        Service svc3 = saveServiceIfMissing("Cloud Infrastructure & DevOps", "Design and deploy scalable cloud architecture on AWS or GCP with CI/CD pipelines, container orchestration, and monitoring.", "cloud");
        Service svc4 = saveServiceIfMissing("AI Agent & LLM Chatbot Integration", "Implement intelligent conversational AI agents trained on custom knowledge bases to automate customer support 24/7.", "bot");
        Service svc5 = saveServiceIfMissing("Cybersecurity Audit & Penetration Testing", "Comprehensive security assessment, OWASP vulnerability scanning, and API endpoint hardening.", "shield");
        Service svc6 = saveServiceIfMissing("Enterprise ERP & Custom CRM Solutions", "Tailor-made Enterprise Resource Planning and CRM software to automate internal HR, finance, and operations.", "enterprise");
        Service svc7 = saveServiceIfMissing("Blockchain & Web3 Smart Contracts", "DeFi protocol development, NFT marketplace creation, and audited Solidity smart contract integration.", "blockchain");
        Service svc8 = saveServiceIfMissing("Big Data Analytics & BI Dashboards", "End-to-end data pipeline setup, data warehousing, and interactive real-time Business Intelligence dashboards.", "analytics");

        // Service Addons (3 Addons per Service = 24 Addons total)
        saveServiceAddon(svc1.getId(), "Automated PayOS / Stripe Payment", 200.0);
        saveServiceAddon(svc1.getId(), "Advanced Inventory & Order Management", 350.0);
        saveServiceAddon(svc1.getId(), "Multi-currency & Localization Support", 250.0);

        saveServiceAddon(svc2.getId(), "Custom Dark Mode Design", 150.0);
        saveServiceAddon(svc2.getId(), "Figma Design System Delivery", 250.0);
        saveServiceAddon(svc2.getId(), "Push Notifications & In-App Chat Module", 300.0);

        saveServiceAddon(svc3.getId(), "CI/CD Pipeline & Docker Setup", 400.0);
        saveServiceAddon(svc3.getId(), "Multi-region High Availability Cluster", 600.0);
        saveServiceAddon(svc3.getId(), "Automated Daily Backup & Disaster Recovery", 300.0);

        saveServiceAddon(svc4.getId(), "Custom RAG Knowledge Base Training", 500.0);
        saveServiceAddon(svc4.getId(), "Multi-channel Zalo & Messenger Integration", 300.0);
        saveServiceAddon(svc4.getId(), "Voice Callbot & Speech Recognition", 450.0);

        saveServiceAddon(svc5.getId(), "OWASP Top 10 Vulnerability Scan & Fix", 450.0);
        saveServiceAddon(svc5.getId(), "PCI-DSS Compliance Certification Report", 600.0);
        saveServiceAddon(svc5.getId(), "Source Code Security Audit & Hardening", 400.0);

        saveServiceAddon(svc6.getId(), "Real-time Data Migration & ETL Setup", 550.0);
        saveServiceAddon(svc6.getId(), "Custom Automated Financial Reporting", 350.0);
        saveServiceAddon(svc6.getId(), "Employee Time-Tracking & GPS Check-in", 300.0);

        saveServiceAddon(svc7.getId(), "Solidity Smart Contract Audit", 500.0);
        saveServiceAddon(svc7.getId(), "Web3 Wallet Connect Integration", 350.0);
        saveServiceAddon(svc7.getId(), "Tokenomics & Staking Mechanism Setup", 600.0);

        saveServiceAddon(svc8.getId(), "Real-time Data Pipeline & Kafka Setup", 600.0);
        saveServiceAddon(svc8.getId(), "Custom PowerBI / Metabase Dashboard", 400.0);
        saveServiceAddon(svc8.getId(), "Predictive Analytics Machine Learning Model", 700.0);

        // ── 4. Projects (15 Diverse Projects) ───────────────────────────────
        Project proj1  = saveProjectIfMissing("Mart06 Fashion System", "High-performance online shopping platform with seamless automated payment integration and full inventory management.", "Website E-Commerce", "Java, Spring Boot, MySQL, Thymeleaf, CSS3");
        Project proj2  = saveProjectIfMissing("NovaDigital Mobile Portal", "Premium mobile application for project coordination, client messaging, and real-time SSE milestone progress alerts.", "Mobile Application", "React Native, Node.js, SSE, MySQL");
        Project proj3  = saveProjectIfMissing("CloudPay Analytics Dashboard", "SaaS analytics dashboard with real-time financial reporting, role-based access control, and Stripe billing integration.", "Cloud SaaS", "Vue.js, Spring Boot, PostgreSQL, Docker");
        Project proj4  = saveProjectIfMissing("FinTech Mobile Wallet & Banking App", "Mobile e-wallet application with biometric login, QR payment, and PayOS gateway integration.", "Mobile Application", "Flutter, Kotlin, Spring Boot, PostgreSQL");
        Project proj5  = saveProjectIfMissing("AI-Powered Customer Care Bot & Automation", "Smart AI customer support agent handling automated inquiries and tickets across web and social channels.", "AI & Automation", "Python, FastAPI, React, OpenAI, Redis");
        Project proj6  = saveProjectIfMissing("Smart Logistical Freight Tracking System", "Real-time fleet tracking, route optimization, and driver management dashboard for nationwide logistics.", "Cloud SaaS", "Angular, Spring Boot, Docker, Kubernetes, AWS");
        Project proj7  = saveProjectIfMissing("Enterprise ERP & HR Management Portal", "Comprehensive HR management system with attendance tracking, payroll, performance evaluation, and shift scheduling.", "Enterprise Portal", "Java, Spring Boot, Vue.js, MySQL");
        Project proj8  = saveProjectIfMissing("CryptoPay Web3 Decentralized Exchange", "Decentralized crypto trading platform with automated market maker (AMM) and non-custodial wallet integration.", "Blockchain Web3", "Solidity, React, Ethers.js, Node.js");
        Project proj9  = saveProjectIfMissing("MedCare Telehealth & Online Booking Platform", "Telemedicine platform connecting patients with doctors for online video consultations and electronic prescription management.", "Healthcare Tech", "React Native, Spring Boot, WebRTC, MySQL");
        Project proj10 = saveProjectIfMissing("EduHub Online Learning & Exam Management", "Interactive e-learning portal with video streaming, automated quizzes, and student progress analytics.", "EdTech", "Next.js, Node.js, PostgreSQL, AWS S3");
        Project proj11 = saveProjectIfMissing("SmartCity IoT Environmental Monitoring System", "IoT sensor data aggregation network monitoring air quality, water levels, and weather stations in real-time.", "IoT & Cloud", "Golang, MQTT, TimescaleDB, Grafana");
        Project proj12 = saveProjectIfMissing("AutoFleet Vehicle Leasing & Maintenance System", "Automotive fleet management software tracking vehicle maintenance schedules, fuel consumption, and lease agreements.", "Automotive SaaS", "Vue.js, Java, Spring Boot, MySQL");
        Project proj13 = saveProjectIfMissing("RealEstate 3D Virtual Tour & CRM", "PropTech web application offering 3D virtual home tours and real estate agent lead management CRM.", "PropTech", "React, Three.js, Node.js, MongoDB");
        Project proj14 = saveProjectIfMissing("OmniChannel Retail Point-of-Sale (POS)", "Cloud-synced desktop and tablet POS software for multi-branch retail stores with offline barcode scanning.", "Retail Tech", "Electron, Spring Boot, SQLite, WebSockets");
        Project proj15 = saveProjectIfMissing("BigData Predictive Sales Forecasting Engine", "AI machine learning engine forecasting product demand, seasonal sales trends, and automated stock reordering.", "Data Science", "Python, PyTorch, Airflow, Snowflake, FastAPI");

        // ── 5. Project Assignments (Exactly 1 PM per project + Staff) ──────────
        saveAssignmentIfMissing(proj1,  mem1,  ProjectAssignment.ProjectRole.PM);
        saveAssignmentIfMissing(proj1,  mem6,  ProjectAssignment.ProjectRole.STAFF);

        saveAssignmentIfMissing(proj2,  mem2,  ProjectAssignment.ProjectRole.PM);
        saveAssignmentIfMissing(proj2,  mem1,  ProjectAssignment.ProjectRole.STAFF);

        saveAssignmentIfMissing(proj3,  mem3,  ProjectAssignment.ProjectRole.PM);
        saveAssignmentIfMissing(proj3,  mem2,  ProjectAssignment.ProjectRole.STAFF);

        saveAssignmentIfMissing(proj4,  mem4,  ProjectAssignment.ProjectRole.PM);
        saveAssignmentIfMissing(proj4,  mem1,  ProjectAssignment.ProjectRole.STAFF);

        saveAssignmentIfMissing(proj5,  mem5,  ProjectAssignment.ProjectRole.PM);
        saveAssignmentIfMissing(proj5,  mem2,  ProjectAssignment.ProjectRole.STAFF);

        saveAssignmentIfMissing(proj6,  mem3,  ProjectAssignment.ProjectRole.PM);
        saveAssignmentIfMissing(proj6,  mem4,  ProjectAssignment.ProjectRole.STAFF);

        saveAssignmentIfMissing(proj7,  mem1,  ProjectAssignment.ProjectRole.PM);
        saveAssignmentIfMissing(proj7,  mem5,  ProjectAssignment.ProjectRole.STAFF);

        saveAssignmentIfMissing(proj8,  mem9,  ProjectAssignment.ProjectRole.PM);
        saveAssignmentIfMissing(proj8,  mem7,  ProjectAssignment.ProjectRole.STAFF);

        saveAssignmentIfMissing(proj9,  mem8,  ProjectAssignment.ProjectRole.PM);
        saveAssignmentIfMissing(proj9,  mem4,  ProjectAssignment.ProjectRole.STAFF);

        saveAssignmentIfMissing(proj10, mem6,  ProjectAssignment.ProjectRole.PM);
        saveAssignmentIfMissing(proj10, mem5,  ProjectAssignment.ProjectRole.STAFF);

        saveAssignmentIfMissing(proj11, mem7,  ProjectAssignment.ProjectRole.PM);
        saveAssignmentIfMissing(proj11, mem3,  ProjectAssignment.ProjectRole.STAFF);

        saveAssignmentIfMissing(proj12, mem1,  ProjectAssignment.ProjectRole.PM);
        saveAssignmentIfMissing(proj12, mem8,  ProjectAssignment.ProjectRole.STAFF);

        saveAssignmentIfMissing(proj13, mem2,  ProjectAssignment.ProjectRole.PM);
        saveAssignmentIfMissing(proj13, mem6,  ProjectAssignment.ProjectRole.STAFF);

        saveAssignmentIfMissing(proj14, mem7,  ProjectAssignment.ProjectRole.PM);
        saveAssignmentIfMissing(proj14, mem1,  ProjectAssignment.ProjectRole.STAFF);

        saveAssignmentIfMissing(proj15, mem10, ProjectAssignment.ProjectRole.PM);
        saveAssignmentIfMissing(proj15, mem5,  ProjectAssignment.ProjectRole.STAFF);

        // ── 6. Project Clients ─────────────────────────────────────────────────
        saveProjectClientIfMissing(proj1,  user1);
        saveProjectClientIfMissing(proj2,  user2);
        saveProjectClientIfMissing(proj3,  user1);
        saveProjectClientIfMissing(proj4,  user2);
        saveProjectClientIfMissing(proj5,  user1);
        saveProjectClientIfMissing(proj6,  user3);
        saveProjectClientIfMissing(proj7,  user4);
        saveProjectClientIfMissing(proj8,  user3);
        saveProjectClientIfMissing(proj9,  user4);
        saveProjectClientIfMissing(proj10, user1);
        saveProjectClientIfMissing(proj11, user3);
        saveProjectClientIfMissing(proj12, user4);
        saveProjectClientIfMissing(proj13, user2);
        saveProjectClientIfMissing(proj14, user3);
        saveProjectClientIfMissing(proj15, user4);

        // ── 7. Project Milestones (30+ Milestones across projects) ─────────────
        if (projectMilestoneRepository.count() < 10) {
            ProjectMilestone m1  = saveMilestone(proj1,  "Phase 1: High-Fidelity Figma Designs", "Drafting interactive layouts and responsive styling for desktop and mobile views.", MilestoneStatus.COMPLETED, 100, LocalDate.now().minusDays(10));
            ProjectMilestone m2  = saveMilestone(proj1,  "Phase 2: Database Schema & REST APIs", "Coding JPA entities, repositories, Spring Security configs, and JUnit integration tests.", MilestoneStatus.IN_PROGRESS, 65, LocalDate.now().plusDays(7));
            ProjectMilestone m3  = saveMilestone(proj1,  "Phase 3: Stripe Integration & Cloud Launch", "Integrating Stripe API webhooks and deploying production release to AWS.", MilestoneStatus.PENDING, 0, LocalDate.now().plusDays(21));

            ProjectMilestone m4  = saveMilestone(proj2,  "Figma Prototype Mockup", "Creating interactive layouts for chat channels and milestone progress panels.", MilestoneStatus.COMPLETED, 100, LocalDate.now().minusDays(5));
            ProjectMilestone m5  = saveMilestone(proj2,  "Live SSE Broadcasting Stream", "Configuring Server-Sent Events controller to push milestone mutations to all active clients.", MilestoneStatus.DELAYED, 40, LocalDate.now().minusDays(1));

            ProjectMilestone m7  = saveMilestone(proj3,  "Requirement Analysis & Architecture Design", "Defining system architecture, microservices boundaries, and API contracts with stakeholders.", MilestoneStatus.COMPLETED, 100, LocalDate.now().minusDays(20));
            ProjectMilestone m8  = saveMilestone(proj3,  "Core Dashboard Frontend Build", "Building Vue.js data visualization components, charts, and responsive table grids.", MilestoneStatus.IN_PROGRESS, 50, LocalDate.now().plusDays(10));

            ProjectMilestone m9  = saveMilestone(proj4,  "Biometric Auth & PayOS Gateway Integration", "Implementing fingerprint/FaceID authentication and PayOS payment API integration.", MilestoneStatus.COMPLETED, 100, LocalDate.now().minusDays(8));
            ProjectMilestone m10 = saveMilestone(proj4,  "QR Code Scan & Wallet Transfer Flow", "Developing QR scanner and instant P2P wallet money transfer functions.", MilestoneStatus.IN_PROGRESS, 60, LocalDate.now().plusDays(12));

            ProjectMilestone m11 = saveMilestone(proj5,  "RAG Knowledge Pipeline & Model Training", "Building RAG pipeline to embed company knowledge base into Vector DB for LLM retrieval.", MilestoneStatus.COMPLETED, 100, LocalDate.now().minusDays(15));
            ProjectMilestone m12 = saveMilestone(proj5,  "Live Chatbot UI Widget & Multi-channel Stream", "Designing responsive chat widget and connecting WebSockets for instant response.", MilestoneStatus.IN_PROGRESS, 70, LocalDate.now().plusDays(8));

            ProjectMilestone m13 = saveMilestone(proj6,  "GPS Live Location WebSockets API", "Building high-concurrency WebSockets service to stream vehicle telemetry data.", MilestoneStatus.IN_PROGRESS, 45, LocalDate.now().plusDays(15));
            ProjectMilestone m14 = saveMilestone(proj6,  "Route Optimization & Driver App Release", "Algorithm for optimal multi-stop route calculation and Android driver app build.", MilestoneStatus.PENDING, 0, LocalDate.now().plusDays(30));

            ProjectMilestone m15 = saveMilestone(proj7,  "Database Schema & Employee Profiles Module", "Designing schema for employee records, departments, roles, and contract management.", MilestoneStatus.COMPLETED, 100, LocalDate.now().minusDays(12));
            ProjectMilestone m16 = saveMilestone(proj7,  "Automated Payroll & Tax Calculation Engine", "Building calculation engine for salaries, overtime, tax deductions, and pay slip generation.", MilestoneStatus.IN_PROGRESS, 50, LocalDate.now().plusDays(18));

            ProjectMilestone m17 = saveMilestone(proj8,  "Solidity Smart Contract Architecture", "Writing audited ERC-20 token contracts and automated market maker liquidity pools.", MilestoneStatus.COMPLETED, 100, LocalDate.now().minusDays(25));
            ProjectMilestone m18 = saveMilestone(proj8,  "Non-Custodial Wallet Connect Integration", "Integrating MetaMask and WalletConnect JS SDKs for seamless Web3 trading.", MilestoneStatus.IN_PROGRESS, 75, LocalDate.now().plusDays(5));

            ProjectMilestone m19 = saveMilestone(proj9,  "WebRTC Video Call Service Integration", "Integrating peer-to-peer WebRTC video consultation streams for doctor-patient calls.", MilestoneStatus.IN_PROGRESS, 55, LocalDate.now().plusDays(14));

            ProjectMilestone m20 = saveMilestone(proj10, "Video Streaming & Automated Quiz Engine", "Setting up AWS CloudFront video streaming and auto-graded online quizzes.", MilestoneStatus.COMPLETED, 100, LocalDate.now().minusDays(14));

            ProjectMilestone m21 = saveMilestone(proj11, "MQTT Sensor Ingestion & Grafana Dashboards", "Configuring MQTT broker to process 10,000+ IoT telemetry payloads per minute.", MilestoneStatus.IN_PROGRESS, 65, LocalDate.now().plusDays(9));

            ProjectMilestone m22 = saveMilestone(proj15, "Sales Demand Machine Learning Model", "Training PyTorch neural network model on 3 years of historical sales records.", MilestoneStatus.IN_PROGRESS, 80, LocalDate.now().plusDays(4));

            // ── 8. Resource Allocations (UC-14 Matrix) ──────────────────────────
            saveResourceAllocation(proj1,  m2,  mem1,  100, LocalDate.now().minusDays(10), LocalDate.now().plusDays(20), AllocationStatus.ACTIVE, "Lead Developer for Backend REST APIs", "admin");
            saveResourceAllocation(proj2,  m5,  mem1,  50,  LocalDate.now().minusDays(5),  LocalDate.now().plusDays(15), AllocationStatus.PLANNED, "Assisting with SSE Integration Configuration", "admin");
            saveResourceAllocation(proj2,  m4,  mem2,  80,  LocalDate.now().minusDays(15), LocalDate.now().plusDays(10), AllocationStatus.ACTIVE, "Lead UI/UX Designer and Figma Prototyping", "admin");
            saveResourceAllocation(proj3,  m8,  mem2,  50,  LocalDate.now(),              LocalDate.now().plusDays(30), AllocationStatus.PLANNED, "Contributing to Vue.js UI Component Development", "admin");
            saveResourceAllocation(proj4,  m10, mem4,  70,  LocalDate.now().minusDays(5),  LocalDate.now().plusDays(25), AllocationStatus.ACTIVE, "Lead Mobile Engineer for Wallet Flow", "admin");
            saveResourceAllocation(proj5,  m12, mem5,  80,  LocalDate.now().minusDays(8),  LocalDate.now().plusDays(22), AllocationStatus.ACTIVE, "AI Engineer building Chatbot Widgets", "admin");
            saveResourceAllocation(proj6,  m13, mem3,  90,  LocalDate.now().minusDays(2),  LocalDate.now().plusDays(28), AllocationStatus.ACTIVE, "DevOps Engineer setting up K8s cluster", "admin");
            saveResourceAllocation(proj7,  m16, mem1,  50,  LocalDate.now(),              LocalDate.now().plusDays(30), AllocationStatus.PLANNED, "Backend Developer for Payroll Engine", "admin");
            saveResourceAllocation(proj8,  m18, mem9,  85,  LocalDate.now().minusDays(10), LocalDate.now().plusDays(20), AllocationStatus.ACTIVE, "Cybersecurity Audit on Smart Contracts", "admin");
            saveResourceAllocation(proj9,  m19, mem8,  60,  LocalDate.now().minusDays(3),  LocalDate.now().plusDays(17), AllocationStatus.ACTIVE, "QA Lead testing WebRTC Video Calls", "admin");
            saveResourceAllocation(proj10, m20, mem6,  75,  LocalDate.now().minusDays(18), LocalDate.now().plusDays(12), AllocationStatus.COMPLETED, "Frontend Lead for EduHub Portal", "admin");
            saveResourceAllocation(proj11, m21, mem7,  80,  LocalDate.now().minusDays(7),  LocalDate.now().plusDays(23), AllocationStatus.ACTIVE, "Backend Engineer building MQTT Aggregator", "admin");
            saveResourceAllocation(proj15, m22, mem10, 95,  LocalDate.now().minusDays(12), LocalDate.now().plusDays(18), AllocationStatus.ACTIVE, "Data Scientist building Sales Forecasting Model", "admin");
        }

        // ── 9. Consultation Appointments & Addons ──────────────────────────────
        if (consultationAppointmentRepository.count() < 4) {
            ConsultationAppointment appt1 = saveAppointment(svc1.getId(), user1.getId(), mem1.getId(), LocalDate.now().plusDays(1), LocalTime.of(9, 0), AppointmentStatus.CONFIRMED, "Consultation for upgrading automated payment system for Mart06 E-Commerce platform.", 1200.0);
            ConsultationAppointment appt2 = saveAppointment(svc2.getId(), user2.getId(), mem2.getId(), LocalDate.now().plusDays(3), LocalTime.of(14, 0), AppointmentStatus.PENDING, "Consultation for Mobile Portal UI/UX design.", 800.0);
            ConsultationAppointment appt3 = saveAppointment(svc3.getId(), user1.getId(), mem1.getId(), LocalDate.now().minusDays(5), LocalTime.of(10, 30), AppointmentStatus.COMPLETED, "Consultation for AWS Cloud Infrastructure and Docker deployment for SaaS.", 1500.0);
            ConsultationAppointment appt4 = saveAppointment(svc4.getId(), user2.getId(), mem5.getId(), LocalDate.now().plusDays(2), LocalTime.of(15, 30), AppointmentStatus.CONFIRMED, "Inquiry about AI Chatbot integration for customer care.", 1800.0);
            ConsultationAppointment appt5 = saveAppointment(svc5.getId(), user3.getId(), mem9.getId(), LocalDate.now().plusDays(4), LocalTime.of(11, 0), AppointmentStatus.PENDING, "Request for vulnerability audit on banking mobile app.", 2100.0);

            saveAppointmentAddon(appt1.getId(), 1L);
            saveAppointmentAddon(appt1.getId(), 2L);
            saveAppointmentAddon(appt2.getId(), 3L);
            saveAppointmentAddon(appt4.getId(), 4L);

            savePaymentTransaction(100001L, appt3.getId(), null, 1500.0, "PAID");
            savePaymentTransaction(100002L, null, 1L, 1000.0, "PAID");
            savePaymentTransaction(100003L, appt1.getId(), null, 1200.0, "PENDING");
            savePaymentTransaction(100004L, appt4.getId(), null, 1800.0, "PAID");
        }

        // ── 11. Feedback (12 Feedbacks) ────────────────────────────────────────
        if (feedbackRepository.count() < 6) {
            saveFeedback("Nam Tran", "namtran@gmail.com", "E-Commerce Website", "Beautiful website interface, fast loading speed, and very convenient order management!");
            saveFeedback("Hoa Le", "hoale@gmail.com", "Cloud Consultation", "The AWS cloud infrastructure consulting team is highly knowledgeable and enthusiastic.");
            saveFeedback("Pham Quoc Viet", "vietpq@gmail.com", "Mobile App Development", "The mobile app created by NovaDigital operates extremely smoothly with beautiful animations!");
            saveFeedback("Tran Thi Mai", "maitt@gmail.com", "AI & Automation", "The AI chatbot handles our customer support messages 24/7 seamlessly. Highly recommended!");
            saveFeedback("Bui Hoang Long", "longbh@techcorp.vn", "Cybersecurity Audit", "Extremely thorough vulnerability scan report. They fixed 12 critical OWASP security bugs for us.");
            saveFeedback("Dang Ngoc Anh", "anhdn@edu.org", "EdTech Platform", "NovaDigital delivered our online learning platform on schedule with zero downtime during exams.");
        }

        // ── 12. Contacts (15 Contact Messages) ─────────────────────────────────
        if (contactRepository.count() < 10) {
            saveContact("Hung Nguyen", "hungnv@gmail.com", "E-Commerce Website Design Quotation", "I would like to receive a quotation for a complete E-Commerce website design service.", "DONE", "Hello Mr. Hung, NovaDigital has sent a detailed quotation to your email.", LocalDate.now().minusDays(3));
            saveContact("Tuan Pham", "tuanpm@gmail.com", "AWS Cloud Infrastructure Consultation", "Our system is overloaded during peak hours, we need assistance with cloud upgrade consultation.", "PENDING", null, null);
            saveContact("Minh Anh", "minhanh.tech@gmail.com", "FinTech Wallet Integration Inquiry", "We are looking for a reliable partner to build an e-wallet system with PayOS payment. Please send us your consultation schedule.", "DONE", "Hello Ms. Minh Anh, NovaDigital team has sent the consultation schedule to your email.", LocalDate.now().minusDays(2));
            saveContact("Quoc Bao", "baoquoc@logistics.vn", "Smart Freight Tracking Software Quotation", "Our logistics company requires a real-time GPS freight tracking dashboard for 500+ drivers.", "PENDING", null, null);
            saveContact("Thanh Thao", "thaothanh@pharmacy.com", "AI Chatbot Customer Care System", "We want an automated AI chatbot to handle customer inquiries 24/7 on our pharmacy website.", "PENDING", null, null);
            saveContact("Hoang Nam", "namhoang@fintech.io", "Cybersecurity Audit & Penetration Test", "We require a complete security vulnerability scan and OWASP audit for our banking app before launch.", "PENDING", null, null);
            saveContact("Nguyen Duc Anh", "ducanh@retail.com", "OmniChannel POS System Architecture", "Looking for custom POS desktop software sync with inventory for 12 store locations.", "PENDING", null, null);
            saveContact("Le Thi Huong", "huonglt@medcare.vn", "Telemedicine Mobile App Inquiry", "We plan to build a doctor consultation video app. Please advise on WebRTC architecture.", "DONE", "Hello Ms. Huong, our lead healthcare engineer has sent the proposal.", LocalDate.now().minusDays(1));
            saveContact("Tran Van Binh", "binhtv@construction.com", "Enterprise ERP Customization", "We need custom ERP modules for construction project tracking and worker attendance.", "PENDING", null, null);
            saveContact("Vu Bich Ngoc", "ngocvb@fashionbrand.com", "E-Commerce Order Automation", "Can you integrate multi-carrier shipping API into our existing online store?", "PENDING", null, null);
        }

        // ── 13. Job Vacancies & Candidates ─────────────────────────────────────
        if (jobVacancyRepository.count() < 5) {
            JobVacancy job1 = saveJobVacancy("Senior Spring Boot Backend Engineer", "Develop high-throughput microservices systems using Java 17, Spring Boot, MySQL, and Docker.", "Engineering", "Hanoi / Remote", "FULL_TIME", JobVacancy.VacancyStatus.ACTIVE);
            JobVacancy job2 = saveJobVacancy("Lead Mobile UI/UX Designer", "Build Design System, design wireframes and interactive prototypes for iOS/Android mobile apps.", "Design", "Ho Chi Minh City", "FULL_TIME", JobVacancy.VacancyStatus.ACTIVE);
            JobVacancy job3 = saveJobVacancy("DevOps & Cloud Engineer (AWS/GCP)", "Configure CI/CD pipelines, Kubernetes, Terraform, and optimize cloud infrastructure costs.", "Engineering", "Remote", "CONTRACT", JobVacancy.VacancyStatus.ACTIVE);
            JobVacancy job4 = saveJobVacancy("AI / ML Research Scientist", "Develop LLM RAG pipelines, fine-tune open-source AI models, and optimize Vector DB queries.", "AI Research", "Hanoi / Hybrid", "FULL_TIME", JobVacancy.VacancyStatus.ACTIVE);

            saveCandidateApplication(job1.getId(), job1.getTitle(), "Long Do", "hoanglong.dev@gmail.com", "0912345678", "/uploads/resumes/hoanglong_cv.pdf", "I have 5 years of experience working with Java Spring Boot and MySQL.");
            saveCandidateApplication(job2.getId(), job2.getTitle(), "Mai Vu", "maivu.design@gmail.com", "0987654321", "/uploads/resumes/thanhmai_portfolio.pdf", "Eager to contribute to NovaDigital design products.");
            saveCandidateApplication(job4.getId(), job4.getTitle(), "Trinh Duc Thang", "thangtd.ai@gmail.com", "0933445566", "/uploads/resumes/ducthang_ai_cv.pdf", "AI researcher with 2 published papers on NLP and LLM RAG retrieval.");
        }

        // ── 14. Notifications & Audit Logs ──────────────────────────────────────
        saveNotification(user1.getId(), "Project Progress Update", "Milestone 'Phase 1: High-Fidelity Figma Designs' for project Mart06 Fashion System is 100% completed.", "/projects/1", true);
        saveNotification(user1.getId(), "Consultation Appointment Confirmation", "Your E-Commerce design consultation appointment has been confirmed for 09:00 tomorrow.", "/appointments", false);
        saveNotification(mem1.getId(), "New Project Assignment", "You have been assigned as Project Leader for project Mart06 Fashion System.", "/matrix", false);

        auditLogRepository.save(new AuditLog("admin", "CREATE", "projects", "Created project 'FinTech Mobile Wallet & Banking App'"));
        auditLogRepository.save(new AuditLog("admin", "CREATE", "projects", "Created project 'AI-Powered Customer Care Bot & Automation'"));
        authLogRepository.save(new AuthLog("admin", "LOGIN_SUCCESS", "127.0.0.1", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)"));

        System.out.println(">>> [DataSeeder] MASSIVE sample dataset seeded successfully!");
        System.out.println(">>> Total Projects: " + projectRepository.count() + " | Services: " + serviceRepository.count() + " | Members: " + memberRepository.count() + " | Contacts: " + contactRepository.count() + " | Milestones: " + projectMilestoneRepository.count());
    }

    // ── Helper builders ──────────────────────────────────────────────────────

    private User findOrCreateUser(String username, String password, String fullName, String email, String phone, String role) {
        return userRepository.findByUsernameOrEmail(username, email).orElseGet(() -> {
            User u = new User();
            u.setUsername(username);
            u.setPassword(PasswordHasher.hash(password));
            u.setFullName(fullName);
            u.setEmail(email);
            u.setPhone(phone);
            u.setRole(role);
            u.setEnabled(true);
            return userRepository.save(u);
        });
    }

    private void saveMemberIfMissing(String name, String role, Long userId, String skills, String projects) {
        if (memberRepository.findAll().stream().noneMatch(m -> name.equalsIgnoreCase(m.getName()))) {
            Member m = new Member();
            m.setName(name);
            m.setRole(role);
            m.setUserId(userId);
            m.setSkills(skills);
            m.setProjects(projects);
            memberRepository.save(m);
        }
    }

    private Service saveServiceIfMissing(String title, String description, String iconUrl) {
        return serviceRepository.findAll().stream()
                .filter(s -> title.equalsIgnoreCase(s.getTitle()))
                .findFirst()
                .orElseGet(() -> {
                    Service s = new Service();
                    s.setTitle(title);
                    s.setDescription(description);
                    s.setIconUrl(iconUrl);
                    return serviceRepository.save(s);
                });
    }

    private ServiceAddon saveServiceAddon(Long serviceId, String name, Double price) {
        return serviceAddonRepository.findByServiceId(serviceId).stream()
                .filter(a -> name.equalsIgnoreCase(a.getAddonName()))
                .findFirst()
                .orElseGet(() -> {
                    ServiceAddon sa = new ServiceAddon();
                    sa.setServiceId(serviceId);
                    sa.setAddonName(name);
                    sa.setPriceModifier(price);
                    return serviceAddonRepository.save(sa);
                });
    }

    private Project saveProjectIfMissing(String title, String description, String category, String technologies) {
        return projectRepository.findAll().stream()
                .filter(p -> title.equalsIgnoreCase(p.getTitle()))
                .findFirst()
                .orElseGet(() -> {
                    Project p = new Project();
                    p.setTitle(title);
                    p.setDescription(description);
                    p.setCategory(category);
                    p.setTechnologies(technologies);
                    return projectRepository.save(p);
                });
    }

    private void saveAssignmentIfMissing(Project project, User user, ProjectAssignment.ProjectRole role) {
        ProjectAssignment assignment = projectAssignmentRepository
                .findByProjectIdAndUserId(project.getId(), user.getId())
                .orElseGet(() -> {
                    ProjectAssignment a = new ProjectAssignment();
                    a.setProject(project);
                    a.setUser(user);
                    return a;
                });
        assignment.setProjectRole(role);
        projectAssignmentRepository.save(assignment);
    }

    private void saveProjectClientIfMissing(Project project, User user) {
        if (projectClientRepository.findByProjectId(project.getId()).stream().noneMatch(pc -> pc.getUser().getId().equals(user.getId()))) {
            ProjectClient pc = new ProjectClient();
            pc.setProject(project);
            pc.setUser(user);
            projectClientRepository.save(pc);
        }
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
        if (paymentTransactionRepository.findByOrderCode(orderCode).isPresent()) {
            return;
        }
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
