package com.example.demo.config;

import com.example.demo.service.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(request -> {
            var corsConfiguration = new org.springframework.web.cors.CorsConfiguration();
            // Replace with your exact frontend URL (e.g., http://127.0.0.1:5500 or http://localhost:3000)
            corsConfiguration.setAllowedOrigins(java.util.List.of("http://127.0.0.1:5500", "http://localhost:3000"));
            corsConfiguration.setAllowedMethods(java.util.List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
            corsConfiguration.setAllowedHeaders(java.util.List.of("*"));
            corsConfiguration.setAllowCredentials(true);
            return corsConfiguration;
        }))
            .csrf(csrf -> csrf.disable())
            .headers(headers -> headers
                .crossOriginOpenerPolicy(coop -> coop.policy(
                    org.springframework.security.web.header.writers.CrossOriginOpenerPolicyHeaderWriter.CrossOriginOpenerPolicy.SAME_ORIGIN_ALLOW_POPUPS
                ))
                .frameOptions(frameOptions -> frameOptions.sameOrigin())
            )
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Public pages (Frontend)

                .requestMatchers("/", "/index.html", "/about.html", "/services.html", "/portfolio.html",
                               "/contact.html", "/feedback.html", "/login.html", "/register.html", "/member.html", "/member-contact.html", "/admin.html", "/admin-messages.html", "/forgot-password.html", "/inbox.html", "/user-profile.html",
                               "/pm-dashboard.html", "/client-dashboard.html", "/booking.html","/rented-project.html", "/member-profile.html",
                               "/resource-allocation.html", "/transaction.html", "/payment-success.html", "/payment-cancel.html",
                               "/careers.html", "/apply.html", "/hr-recruitment.html" /* F_37/F_38 — Careers & Apply pages */).permitAll()
                .requestMatchers("/css/**", "/js/**", "/images/**", "/uploads/**", "/favicon.ico").permitAll()
                
                // Auth APIs
                .requestMatchers("/api/auth/**", "/error").permitAll()
                
                // Public GET APIs
                .requestMatchers(HttpMethod.GET, "/api/projects", "/api/projects/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/services", "/api/services/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/members", "/api/members/**").permitAll()
                // F_37/F_38 — Vacancy APIs
                .requestMatchers(HttpMethod.GET, "/api/vacancies").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/vacancies/list").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/vacancies/apply").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/vacancies/applications").hasAnyRole("ADMIN", "MEMBER", "RESOURCE")
                .requestMatchers("/api/bookings/my").authenticated()
                .requestMatchers("/api/bookings/**").permitAll()
                .requestMatchers("/api/chatbot/**").permitAll()
                .requestMatchers("/api/feedback/**").permitAll()

                // PayOS Payment Routes
                .requestMatchers("/api/payments/payos-webhook").permitAll()
                .requestMatchers("/api/payments/status/*").permitAll()
                .requestMatchers("/api/payments/**").authenticated()

                // Milestone SSE Stream: public so Client View can subscribe without login
                .requestMatchers(HttpMethod.GET, "/api/milestones/stream").permitAll()

                // Milestone read: public (project progress visible on Client View)
                .requestMatchers(HttpMethod.GET, "/api/projects/*/milestones").permitAll()

                // Milestone audit logs: restricted to internal team
                .requestMatchers(HttpMethod.GET, "/api/projects/*/milestones/*/logs").hasAnyRole("ADMIN", "MEMBER")

                // Milestone sync & create: MEMBER only (PM ownership check is in service layer)
                .requestMatchers(HttpMethod.POST, "/api/projects/*/milestones").hasRole("MEMBER")
                .requestMatchers(HttpMethod.PUT, "/api/projects/*/milestones/**").hasRole("MEMBER")

                // Milestone delete: Admin only
                .requestMatchers(HttpMethod.DELETE, "/api/projects/*/milestones/**").hasRole("ADMIN")

                // Assignment management: Admin or Resource Manager (HR)
                .requestMatchers("/api/projects/*/assignments", "/api/projects/*/assignments/**").hasAnyRole("ADMIN", "RESOURCE")
                .requestMatchers("/api/projects/*/clients", "/api/projects/*/clients/**").hasAnyRole("ADMIN", "RESOURCE")

                // Dedicated Resource Allocation workspace: Resource Manager or Admin
                .requestMatchers("/api/resource-allocations/**").hasAnyRole("RESOURCE", "ADMIN")

                // My-projects endpoints: any authenticated user
                .requestMatchers("/api/my/**").authenticated()

                // In-app notifications: any authenticated user (scoped tới user hiện tại trong controller)
                .requestMatchers("/api/notifications/**").authenticated()
                
                // Contact submission: anyone can POST to /api/contacts (send message)
                .requestMatchers(HttpMethod.POST, "/api/contacts").permitAll()
                // Reply to a contact: only ADMIN or MEMBER
                .requestMatchers(HttpMethod.POST, "/api/contacts/**").hasAnyRole("ADMIN", "MEMBER")

                // Security rules for changing content / sensitive endpoints
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                
                // Modifying projects, services, members needs ADMIN or MEMBER (which is Team_Member)
                .requestMatchers(HttpMethod.POST, "/api/projects", "/api/projects/**").hasAnyRole("ADMIN", "MEMBER")
                .requestMatchers(HttpMethod.PUT, "/api/projects", "/api/projects/**").hasAnyRole("ADMIN", "MEMBER")
                .requestMatchers(HttpMethod.DELETE, "/api/projects", "/api/projects/**").hasAnyRole("ADMIN", "MEMBER")
                
                .requestMatchers(HttpMethod.POST, "/api/services", "/api/services/**").hasAnyRole("ADMIN", "MEMBER")
                .requestMatchers(HttpMethod.PUT, "/api/services", "/api/services/**").hasAnyRole("ADMIN", "MEMBER")
                .requestMatchers(HttpMethod.DELETE, "/api/services", "/api/services/**").hasAnyRole("ADMIN", "MEMBER")
                
                .requestMatchers(HttpMethod.POST, "/api/members", "/api/members/**").hasAnyRole("ADMIN", "MEMBER")
                .requestMatchers(HttpMethod.PUT, "/api/members", "/api/members/**").hasAnyRole("ADMIN", "MEMBER")
                .requestMatchers(HttpMethod.DELETE, "/api/members", "/api/members/**").hasAnyRole("ADMIN", "MEMBER")

                // Uploading files is restricted to authenticated users (so users can upload avatars)
                .requestMatchers("/api/upload", "/api/upload/**").authenticated()

                // Get my contacts needs authentication
                .requestMatchers("/api/contacts/my").authenticated()
                .requestMatchers(HttpMethod.DELETE, "/api/contacts/**").authenticated()
                // Manage contacts (list/view) is for Admin / Member
                .requestMatchers(HttpMethod.GET, "/api/contacts", "/api/contacts/**").hasAnyRole("ADMIN", "MEMBER")

                // Any other request
                .anyRequest().authenticated()
            );

        http.addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}