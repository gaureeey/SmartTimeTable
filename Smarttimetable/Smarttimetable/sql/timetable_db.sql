CREATE DATABASE IF NOT EXISTS timetable_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE timetable_db;

DROP TABLE IF EXISTS activity_log;
DROP TABLE IF EXISTS timetable_entries;
DROP TABLE IF EXISTS users;

CREATE TABLE users (
    user_id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE timetable_entries (
    entry_id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    day_of_week ENUM('Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun') NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    subject_name VARCHAR(100) NOT NULL,
    subject_code VARCHAR(20) NOT NULL,
    teacher_name VARCHAR(100) NOT NULL,
    room_number VARCHAR(20) NOT NULL,
    batch_section VARCHAR(20) NOT NULL,
    semester TINYINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    is_active BOOLEAN DEFAULT TRUE,
    INDEX idx_day (day_of_week),
    INDEX idx_teacher (teacher_name),
    INDEX idx_subject (subject_name),
    UNIQUE KEY uk_slot (day_of_week, start_time, room_number),
    CHECK (semester BETWEEN 1 AND 8),
    CHECK (start_time < end_time)
) ENGINE=InnoDB;

CREATE TABLE activity_log (
    activity_id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    action_type VARCHAR(40) NOT NULL,
    action_message VARCHAR(255) NOT NULL,
    performed_by VARCHAR(50) NOT NULL,
    action_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

INSERT INTO users (username, password, full_name) VALUES
('admin', '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9', 'Administrator');

INSERT INTO timetable_entries
(day_of_week, start_time, end_time, subject_name, subject_code, teacher_name, room_number, batch_section, semester, is_active)
VALUES
('Mon', '09:00:00', '10:00:00', 'Data Structures', 'CS301', 'Dr. Sharma', 'LAB-1', 'MCA-1', 3, TRUE),
('Mon', '10:15:00', '11:15:00', 'Database Systems', 'CS302', 'Prof. Nair', 'R-204', 'MCA-1', 3, TRUE),
('Tue', '09:00:00', '10:00:00', 'Computer Networks', 'CS303', 'Dr. Menon', 'R-202', 'MCA-1', 3, TRUE),
('Tue', '10:15:00', '11:15:00', 'Software Engineering', 'CS304', 'Prof. Das', 'R-204', 'MCA-1', 3, TRUE),
('Wed', '09:00:00', '10:00:00', 'Operating Systems', 'CS305', 'Dr. Iyer', 'LAB-2', 'MCA-1', 3, TRUE),
('Wed', '11:30:00', '12:30:00', 'Web Technologies', 'CS306', 'Prof. Rao', 'LAB-3', 'MCA-1', 3, TRUE),
('Thu', '09:00:00', '10:00:00', 'Artificial Intelligence', 'CS401', 'Dr. Thomas', 'R-301', 'MCA-2', 4, TRUE),
('Thu', '10:15:00', '11:15:00', 'Machine Learning', 'CS402', 'Prof. Jacob', 'LAB-4', 'MCA-2', 4, TRUE),
('Fri', '09:00:00', '10:00:00', 'Cloud Computing', 'CS403', 'Dr. Khan', 'R-305', 'MCA-2', 4, TRUE),
('Fri', '10:15:00', '11:15:00', 'Cyber Security', 'CS404', 'Prof. Verma', 'R-306', 'MCA-2', 4, TRUE);

INSERT INTO activity_log (action_type, action_message, performed_by) VALUES
('SEED', 'Initial dataset created for timetable management.', 'system');
