-- UC-14 Staff Resource Allocation Matrix
-- Target database: MySQL / novadigitalusers
-- This script is optional when spring.jpa.hibernate.ddl-auto=update is enabled.

USE novadigitalusers;

CREATE TABLE IF NOT EXISTS resource_allocations (
    id BIGINT NOT NULL AUTO_INCREMENT,
    project_id BIGINT NOT NULL,
    milestone_id BIGINT NULL,
    user_id BIGINT NOT NULL,
    allocation_percentage INT NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PLANNED',
    notes TEXT NULL,
    assigned_by VARCHAR(100) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_ra_project FOREIGN KEY (project_id) REFERENCES projects(id),
    CONSTRAINT fk_ra_milestone FOREIGN KEY (milestone_id) REFERENCES project_milestones(id),
    CONSTRAINT fk_ra_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT chk_ra_percentage CHECK (allocation_percentage BETWEEN 1 AND 100),
    CONSTRAINT chk_ra_dates CHECK (end_date >= start_date),
    CONSTRAINT chk_ra_status CHECK (status IN ('PLANNED','ACTIVE','COMPLETED','CANCELLED')),
    INDEX idx_ra_project (project_id),
    INDEX idx_ra_user_dates (user_id, start_date, end_date),
    INDEX idx_ra_milestone (milestone_id)
);

-- Verification query
SELECT id, project_id, milestone_id, user_id, allocation_percentage,
       start_date, end_date, status, assigned_by
FROM resource_allocations
ORDER BY id DESC;
