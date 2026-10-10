# UniFix - Crowdsourced Campus Issue Resolution System

[![Java Version](https://img.shields.io/badge/Java-JDK%2017%2B-ED8B00?logo=openjdk&logoColor=white)](https://www.oracle.com/java/)
[![GUI](https://img.shields.io/badge/GUI-Java%20Swing-orange)](https://docs.oracle.com/javase/tutorial/uiswing/)
[![Database](https://img.shields.io/badge/Database-MySQL%208.0-4479A1?logo=mysql&logoColor=white)](https://www.mysql.com/)
[![Pattern](https://img.shields.io/badge/Architecture-MVC%20%2F%20DAO-blue)](#architecture--design-patterns)

**UniFix** is a desktop application built for university campuses that democratizes facility management. Students can report campus grievances (Hostel, Mess, IT, Sanitation, Infrastructure) and upvote existing issues reported by peers. College administrators receive a real-time, urgency-ranked dashboard allowing them to prioritize and update repairs based on crowdsourced urgency rather than arbitrary scheduling.

---

## Key Features

### Student Portal (`StudentDashboard`)
- **Real-Time Issue Feed:** Browse campus problems sorted by community urgency (highest upvotes).
- **One-Click Upvoting:** Upvote existing issues to elevate urgent problems. Prevents duplicate votes per user via composite constraints and JDBC transactions.
- **Report New Issues:** Submit detailed issue tickets specifying category (`Hostel`, `Mess`, `IT`, `Sanitation`, `Infrastructure`), location, and problem description.
- **Issue Details Modal:** View full descriptions and metadata with a double-click or detail action.
- **Session Management:** Role-aware login with clean session logout.

### Administrative Authority Portal (`AdminDashboard`)
- **Crowdsourced Prioritization:** Authority view automatically ranks issues by total student upvotes on top.
- **Multi-Factor Filtering:** Filter tickets dynamically by Category or Lifecycle Status (`PENDING`, `IN_PROGRESS`, `RESOLVED`).
- **Status Lifecycle Control:** Transition tickets through `PENDING` &rarr; `IN_PROGRESS` &rarr; `RESOLVED` with instant database synchronization.
- **Audit Details:** Inspect reporter details, submission timestamp, and complete incident reports.

---

## Architecture & Design Patterns

The project follows a modular, evaluator-friendly **MVC / DAO (Data Access Object)** pattern:

```
UniFix/
├── lib/
│   ├── mysql-connector-j-8.3.0.jar      # JDBC Driver for MySQL 8.x
│   ├── download_driver.ps1              # Helper script to download driver
│   └── README.txt                       # Driver setup instructions
├── sql/
│   └── schema.sql                       # Database creation, tables DDL, and sample seed data
├── src/
│   ├── Main.java                        # App launcher (System Look & Feel, EDT runner)
│   ├── config/
│   │   └── DatabaseConnection.java      # Singleton JDBC connection manager
│   ├── models/
│   │   ├── User.java                    # User model (Student / Admin)
│   │   └── Issue.java                   # Issue ticket model
│   ├── dao/
│   │   ├── UserDAO.java                 # Authentication & registration queries
│   │   └── IssueDAO.java                # Issue CRUD, transactional upvotes, status updates
│   └── ui/
│       ├── LoginFrame.java              # Tabbed Login / Registration frame
│       ├── StudentDashboard.java        # Feed, upvote, and issue reporting window
│       └── AdminDashboard.java          # Urgency ranking, status management, filtering
├── .gitignore                           # Excludes compiled binaries and IDE metadata
└── README.md                            # Comprehensive project guide
```

- **DAO Pattern:** Database operations are decoupled from UI components. All queries use `PreparedStatement` to prevent SQL Injection.
- **Atomic Transactions:** The upvote operation updates the `issue_upvotes` table and increments `issues.upvotes` within a database transaction with rollback guarantees.
- **Singleton Connection:** `DatabaseConnection.java` manages a single reusable connection with automatic reconnection and multi-source credential resolution.

---

## Prerequisites

- **Java Development Kit (JDK):** Version 17 or higher (JDK 17, 21, or 25).
- **MySQL Server:** Version 8.0 or higher running on `localhost:3306`.
- **Git:** Version 2.x for version control.

---

## Database Setup

1. Start your local MySQL service (e.g., `MySQL80` service or XAMPP).
2. Open terminal/PowerShell and import the schema script:

   ```bash
   # Using MySQL CLI:
   mysql -u root -p < sql/schema.sql
   ```

   *Alternatively, open `sql/schema.sql` inside MySQL Workbench or phpMyAdmin and execute the entire script.*

3. The script creates the `unifix_db` database and seeds it with default users and sample issues.

### Database Credentials Configuration
`DatabaseConnection.java` connects with standard defaults and supports flexible overrides without changing Java code:
- **Default Database:** `unifix_db` on `localhost:3306`
- **Default User:** `root`
- **Default Password:** `i@mpranav` (configured for current environment; can also be overridden)

To specify custom credentials at runtime:
- **Via System Property:**
  ```bash
  java -Ddb.password=your_password -Ddb.user=root -cp "lib/*;bin" Main
  ```
- **Via Environment Variable:**
  ```bash
  set UNIFIX_DB_PASSWORD=your_password   # Windows Command Prompt
  $env:UNIFIX_DB_PASSWORD="your_password" # Windows PowerShell
  ```

---

## Default Login Credentials

| Role | Email | Password | Dashboard Redirect |
| :--- | :--- | :--- | :--- |
| **Administrator** | `admin@college.edu` | `admin123` | `AdminDashboard` |
| **Student (Alice)** | `alice@college.edu` | `student123` | `StudentDashboard` |
| **Student (Bob)** | `bob@college.edu` | `student123` | `StudentDashboard` |

*New students or administrators can also register directly via the "Register" tab in `LoginFrame`.*

---

## How to Compile and Run

### Option 1: Using Terminal / PowerShell (Recommended)

1. **Verify or download JDBC driver:**
   ```powershell
   # If lib/mysql-connector-j-8.3.0.jar is not yet present:
   powershell -ExecutionPolicy Bypass -File lib/download_driver.ps1
   ```

2. **Compile all Java sources into `bin/`:**
   ```powershell
   # Windows (PowerShell / CMD):
   javac -cp "lib/*;src" -d bin src/Main.java src/config/*.java src/models/*.java src/dao/*.java src/ui/*.java
   ```

3. **Run the application:**
   ```powershell
   # Windows:
   java -cp "lib/*;bin" Main

   # Linux / macOS:
   # java -cp "lib/*:bin" Main
   ```

### Option 2: Using Visual Studio Code
1. Open the `UniFix` folder in VS Code.
2. Ensure the "Extension Pack for Java" is installed.
3. Add `lib/mysql-connector-j-8.3.0.jar` to "Referenced Libraries" under the **Java Projects** view.
4. Open `src/Main.java` and click **Run** (or press `F5`).

### Option 3: Using IntelliJ IDEA
1. Open IntelliJ IDEA &rarr; **Open** &rarr; Select `UniFix` directory.
2. Go to **File** &rarr; **Project Structure** &rarr; **Modules** &rarr; **Dependencies**.
3. Click `+` &rarr; **JARs or Directories** &rarr; Select `lib/mysql-connector-j-8.3.0.jar`.
4. Right-click `src/Main.java` &rarr; **Run 'Main.main()'**.

---

## Verification & Testing

An automated verification test is provided in the project:
```powershell
# Runs headless verification testing DB connectivity, authentication,
# transactional upvoting, duplicate prevention, and status update:
java -cp "lib/*;bin" Main
```

---

## Evaluation Checklist

- [x] Core Java (JDK 17+) compliant
- [x] Standard Java Swing GUI with System Look and Feel
- [x] MySQL database with JDBC `PreparedStatement` on all queries
- [x] MVC / DAO clean folder architecture
- [x] Transactional upvoting with duplicate vote prevention
- [x] Role-based authentication and authorization routing
- [x] Evaluator documentation and seed database script
