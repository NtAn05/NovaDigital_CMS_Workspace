package com.example.demo.config;

import com.example.demo.entity.Member;
import com.example.demo.entity.Project;
import com.example.demo.entity.Service;
import com.example.demo.entity.User;
import com.example.demo.repository.MemberRepository;
import com.example.demo.repository.ProjectRepository;
import com.example.demo.repository.ServiceRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.utils.PasswordHasher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ServiceRepository serviceRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    public void run(String... args) throws Exception {
        // Seed Members
        if (memberRepository.count() == 0) {
            memberRepository.save(new Member(null, "DucDai", "MEMBER", "/images/member-ducdai.png", "https://www.facebook.com/ucai.161364/", "https://github.com/Lill21", "www.linkedin.com/in/đại-đức-46559a415"));
            memberRepository.save(new Member(null, "ThanhAn", "PROJECT LEADER", "/images/member-thanhan.png", "#", "https://github.com/NtAn05", "#"));
            memberRepository.save(new Member(null, "DanhDoan", "MEMBER", "/images/member-danhdoan.jpg", "#", "#", "#"));
            memberRepository.save(new Member(null, "Long", "MEMBER", "/images/member-long.png", "#", "#", "#"));
            memberRepository.save(new Member(null, "ManhLinh", "MEMBER", "/images/member-manhlinh.png", "#", "#", "#"));
            memberRepository.save(new Member(null, "KieuSon", "MEMBER", "/images/member-kieuson.png", "#", "https://github.com/duyson197", "#"));
        } else {
            memberRepository.findAll().forEach(m -> {
                if ("DanhDoan".equals(m.getName()) && !"/images/member-danhdoan.jpg".equals(m.getAvatarUrl())) {
                    m.setAvatarUrl("/images/member-danhdoan.jpg");
                    memberRepository.save(m);
                }
            });
        }

        // Seed Services
        if (serviceRepository.count() == 0) {
            serviceRepository.save(new Service(null, "Professional Web Design", "We build SEO-optimized, responsive websites that are compatible with all mobile devices, have optimized page loading speeds, and offer high security.", "web", null));
            serviceRepository.save(new Service(null, "Licensed UI/UX Design", "Research user behavior, create wireframes, and design modern application interfaces (Mobile App/Web) to increase conversion rates.", "design", null));
            serviceRepository.save(new Service(null, "Digital Marketing", "Search engine optimization (SEO) and running Google/Facebook Ads campaigns help you reach the right target audience of potential customers.", "marketing", null));
            serviceRepository.save(new Service(null, "Mobile App Development", "The application is developed to run smoothly on both iOS and Android operating systems, meeting all the operational needs of businesses.", "mobile", null));
            serviceRepository.save(new Service(null, "Brand Positioning", "Designing a core brand identity includes a logo, slogan, catalog, and business card, helping to establish the company's position.", "branding", null));
            serviceRepository.save(new Service(null, "Cloud Computing Solutions", "Consulting and implementing Cloud Server, AWS, and Google Cloud infrastructure helps optimize big data systems and ensure stable operation.", "cloud", null));
        }

        // Seed Projects
        if (projectRepository.count() == 0) {
            projectRepository.save(new Project(null, "Mart06 Fashion System", "Designing a high-performance online shopping platform with seamless automated payment integration.", "Website E-Commerce", "/images/project-fashion.png", "React, Node.js, MongoDB, Stripe", null));
            projectRepository.save(new Project(null, "FinTech E-Wallet Application", "A premium, minimalist UI, fingerprint security, and optimized ultra-fast transaction experience.", "Mobile Application", "/images/project-wallet.png", "React Native, Firebase, Redux", null));
            projectRepository.save(new Project(null, "ERP Enterprise Resource Planning System", "Real-time intelligent chart analysis systems help businesses control cash flow.", "UI/UX Dashboard", "/images/project-erp.png", "Vue.js, Spring Boot, PostgreSQL", null));
        }

        // Seed Users
        if (!userRepository.existsByUsername("user")) {
            userRepository.save(new User(null, "user", PasswordHasher.hash("user123"), "Demo User", "user@novadigital.com", "0123456789", "ROLE_USER", true, null, null, null));
        }
        if (!userRepository.existsByUsername("admin")) {
            userRepository.save(new User(null, "admin", PasswordHasher.hash("admin123"), "Administrator", "admin@novadigital.com", "0987654321", "ROLE_ADMIN", true, null, null, null));
        }
        if (!userRepository.existsByUsername("member")) {
            userRepository.save(new User(null, "member", PasswordHasher.hash("member123"), "Team Member", "member@novadigital.com", "0112233445", "ROLE_MEMBER", true, null, null, null));
        }
    }
}
