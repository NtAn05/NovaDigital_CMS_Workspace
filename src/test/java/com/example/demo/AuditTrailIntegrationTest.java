package com.example.demo;

import com.example.demo.entity.Contact;
import com.example.demo.entity.DataAuditLog;
import com.example.demo.repository.DataAuditLogRepository;
import com.example.demo.service.ContactService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.AfterEach;

@SpringBootTest
public class AuditTrailIntegrationTest {

    @Autowired
    private ContactService contactService;

    @Autowired
    private DataAuditLogRepository dataAuditLogRepository;

    @AfterEach
    public void tearDown() {
        contactService.deleteContactsByEmail("test_audit@example.com");
    }

    @Test
    public void testContactCreationTriggersAuditLog() throws InterruptedException {
        // Clear any old data audit logs for testing clean state
        dataAuditLogRepository.deleteAll();

        // 1. Prepare contact payload
        Contact contact = new Contact();
        contact.setName("Test Audit User");
        contact.setEmail("test_audit@example.com");
        contact.setTitle("Inquiry about Audit Flow");
        contact.setContent("Testing audit aspect functionality.");

        // 2. Perform action annotated with @Auditable(action = "CREATE", table =
        // "contacts")
        Contact saved = contactService.createContact(contact);
        assertNotNull(saved.getId());

        // 3. Wait a brief moment for the asynchronous @Async listener to persist the
        // log
        Thread.sleep(500);

        // 4. Verify that a DataAuditLog was successfully created
        List<DataAuditLog> logs = dataAuditLogRepository.findAllByOrderByCreatedAtDesc();
        assertFalse(logs.isEmpty(), "Audit log should have been captured and saved!");

        // 5. Assert database log matching our action
        DataAuditLog matchingLog = logs.stream()
                .filter(log -> "contacts".equals(log.getTableName()) && "CREATE".equals(log.getAction()))
                .findFirst()
                .orElse(null);

        assertNotNull(matchingLog, "Could not find corresponding CREATE audit log for contacts table!");
        assertEquals("CREATE", matchingLog.getAction());
        assertEquals("contacts", matchingLog.getTableName());
        assertNotNull(matchingLog.getDetail());
        System.out.println(">>> Verified Audit Log Details: " + matchingLog.getDetail());
    }
}
