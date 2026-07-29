package com.example.demo.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DatabaseConstraintFixer {

    private static final Logger log = LoggerFactory.getLogger(DatabaseConstraintFixer.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @EventListener(ApplicationReadyEvent.class)
    public void dropObsoleteCheckConstraints() {
        try {
            jdbcTemplate.execute("ALTER TABLE consultation_appointment DROP CONSTRAINT IF EXISTS consultation_appointment_status_check");
            log.info("Successfully dropped obsolete constraint 'consultation_appointment_status_check'");
        } catch (Exception e) {
            log.warn("Could not drop constraint 'consultation_appointment_status_check': {}", e.getMessage());
        }
    }
}
