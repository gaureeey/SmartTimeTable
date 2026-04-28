# Smart Timetable Generator

Java MVC web application for timetable management with JSP, Servlets, JDBC, MySQL, Bootstrap, and a taupe theme.

## Project Highlights

- Login authentication with session timeout and logout
- Timetable CRUD (Create, Read, Update, Delete)
- Search and filtering by subject, teacher, day, and semester
- Dashboard with summary cards and recent activity
- Duplicate slot conflict prevention (day + start time + room)
- Ollama-powered automatic timetable generation (7B model support)
- Dynamic timetable grid view by branch/profile and slots per day
- Download timetable grid as PDF
- Eclipse Dynamic Web Project structure compatible with Apache Tomcat

## Tech Stack

- Java 8+
- JSP + Servlets (MVC)
- MySQL 8+
- Bootstrap 5.3
- Apache Tomcat 9+ (works with Tomcat 10 when migrated to jakarta namespace)

## Folder Structure

- src/com/timetable/model: Java models and DAO classes
- src/com/timetable/controller: Servlet controllers
- src/com/timetable/util: Validation and web helpers
- WebContent: JSP pages, css, js, and WEB-INF/web.xml
- sql/timetable_db.sql: Database schema + seed data
- screenshots: Submission screenshots placeholder

## Prerequisites

- JDK 8 or above
- Eclipse IDE for Enterprise Java and Web Developers
- Apache Tomcat 9.x
- MySQL Server 8.x
- MySQL Connector/J 8.0.x JAR
- Ollama installed locally (for AI generation)

## Database Setup

1. Open MySQL client.
2. Run the SQL script from sql/timetable_db.sql.
3. This creates:
   - timetable_db database
   - users table
   - timetable_entries table
   - activity_log table
   - 10 sample timetable records
4. Default login user:
   - Username: admin
   - Password: admin123

## Eclipse Import (Existing Project)

1. Open Eclipse.
2. Go to File > Import > General > Existing Projects into Workspace.
3. Select the Smarttimetable folder and finish import.
4. Ensure project facets are enabled:
   - Java 1.8
   - Dynamic Web Module 4.0
5. Add MySQL connector JAR:
   - Copy mysql-connector-j-8.0.x.jar to WebContent/WEB-INF/lib
   - Refresh project

## Tomcat Configuration

1. In Eclipse, open Servers view.
2. Create a new Apache Tomcat 9 server.
3. Add SmartTimetableGenerator project to server.
4. Start server.
5. Open:
   - http://localhost:8080/SmartTimetableGenerator/

## DB Connection Configuration

Database connection is controlled through environment variables (optional). Defaults are provided in DBConnection.java.

- TT_DB_HOST (default: localhost)
- TT_DB_PORT (default: 3306)
- TT_DB_NAME (default: timetable_db)
- TT_DB_USER (default: root)
- TT_DB_PASS (default: root)

If your MySQL password is different, set TT_DB_PASS in your runtime environment.

## Ollama Configuration (Automatic Timetable Generation)

Automatic generation is available at:

- /auto-generate

Default web.xml settings:

- ollamaApiUrl: http://127.0.0.1:11434/api/generate
- ollamaModel: qwen2.5-coder:7b

Steps:

1. Start Ollama service:
   - ollama serve
2. Pull a 7B model if not available:
   - ollama pull qwen2.5-coder:7b
3. Open Auto Generate page in the app and submit subject constraints.

You can override the model directly in the Auto Generate form (for example: mistral:7b).

## Timetable Grid + PDF Export

Grid view URL:

- /timetable-grid

Features:

- Weekly matrix format (Time x Monday-Sunday) similar to planner layout
- Dynamic rows based on slots/day, start time, slot duration, and break duration
- Profile differentiation:
   - Branch Standard
   - Industry Relevant
- Branch/Batch and Semester filtering
- One-click PDF download of the current grid

## Delete Mode Configuration

Delete behavior is configurable via web.xml context parameter:

- soft (default): marks entry inactive
- hard: permanently removes entry

You can also choose delete mode in the delete confirmation modal.

## Validation & Security Notes

- PreparedStatement used for all SQL operations
- Session timeout: 30 minutes
- HttpOnly session cookie enabled
- No-cache headers added in servlet responses
- Client-side and server-side validation included

## Submission Checklist Mapping

- Importable Eclipse project structure: included
- SQL dump with seed data: included
- Taupe themed responsive UI: included
- Tomcat deployment structure: included
- README setup instructions: included

## Optional Next Enhancements

- Add pagination controls with server-side page size
- Replace SHA-256 with bcrypt hashing
- Add role-based access and multi-user activity tracking
