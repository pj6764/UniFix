# UniFix - Campus Issue Resolution System

UniFix is a desktop application built using Core Java (Swing) and MySQL. It provides university students with a shared platform to report campus infrastructure problems (such as Wi-Fi drops, hostel maintenance, mess hygiene, or water supply issues) and upvote existing ones. Campus administration gets a dashboard sorted by student upvotes, helping them prioritize urgent repairs based on real student demand rather than arbitrary scheduling.

---

## Tech Stack

- **Language:** Java (JDK 17 or newer)
- **GUI:** Java Swing (standard components with System Look-and-Feel)
- **Database:** MySQL 8.0
- **Connectivity:** JDBC (`mysql-connector-j-8.3.0.jar`)
- **Architecture:** MVC / DAO pattern

---

## Key Features

### Student Portal
- **Browse Issues:** View all reported campus issues with category, location, status, and current upvote count.
- **Upvote Issues:** Upvote unresolved complaints. Each student can only upvote a specific issue once.
- **Report New Issues:** Submit a new ticket with title, category (`Hostel`, `Mess`, `IT`, `Sanitation`, `Infrastructure`), location, and description.
- **View Full Details:** Double-click any row or click "View Details" to read the full description in a popup window.

### Admin Portal
- **Urgency-Based Queue:** Issues are automatically sorted by total upvotes so high-priority complaints appear at the top.
- **Filter Complaints:** Filter issues by category or current status (`PENDING`, `IN_PROGRESS`, `RESOLVED`).
- **Update Status:** Change the progress status of any selected issue, which immediately updates the database.
- **Inspect Details:** View full problem reports along with the reporting student's name and timestamp.

---

## Project Structure

```
UniFix/
├── lib/
│   ├── mysql-connector-j-8.3.0.jar   # MySQL JDBC driver
│   ├── download_driver.ps1           # Optional PowerShell helper to fetch driver
│   └── README.txt                    # Driver setup instructions
├── sql/
│   └── schema.sql                    # Database tables and sample seed data
├── src/
│   ├── Main.java                     # Application entry point
│   ├── config/
│   │   └── DatabaseConnection.java   # Reusable JDBC connection utility
│   ├── models/
│   │   ├── User.java                 # User entity (Student / Admin)
│   │   └── Issue.java                # Issue ticket entity
│   ├── dao/
│   │   ├── UserDAOInterface.java     # User DAO contract interface
│   │   ├── UserDAO.java              # User login and registration queries
│   │   ├── IssueDAOInterface.java    # Issue DAO contract interface
│   │   └── IssueDAO.java             # CRUD operations & transactional upvoting
│   └── ui/
│       ├── UIUtils.java              # Helper for consistent, high-contrast button styling
│       ├── LoginFrame.java           # Login and registration screen
│       ├── StudentDashboard.java     # Student feed, upvoting, and report dialog
│       └── AdminDashboard.java       # Admin prioritization and status updater
├── .gitignore
└── README.md
```

---

## How to Set Up and Run

### 1. Database Setup
1. Make sure your local MySQL service is running on `localhost:3306`.
2. Open PowerShell or Terminal and import the database schema:
   ```bash
   mysql -u root -p < sql/schema.sql
   ```
   *(Alternatively, run the contents of `sql/schema.sql` inside MySQL Workbench).*
3. This creates the `unifix_db` database and populates it with sample student accounts, an admin account, and three initial issues.

### 2. Configure Database Credentials
By default, `DatabaseConnection.java` connects to `localhost:3306/unifix_db` with user `root` and password `i@mpranav`.

If your MySQL uses a different password, you can supply it at runtime without editing code:
- **Windows PowerShell:**
  ```powershell
  $env:UNIFIX_DB_PASSWORD="your_password"
  ```
- **macOS / Linux:**
  ```bash
  export UNIFIX_DB_PASSWORD="your_password"
  ```
- **Or via JVM System Property:**
  ```bash
  java -Ddb.password=your_password -cp "lib/*;bin" Main
  ```

### 3. Compile and Run

**Windows (PowerShell / Command Prompt):**
```powershell
# 1. Compile all Java sources into bin/
javac -cp "lib/*;src" -d bin src/Main.java src/config/*.java src/models/*.java src/dao/*.java src/ui/*.java

# 2. Run the application
java -cp "lib/*;bin" Main
```

**macOS / Linux:**
```bash
# 1. Compile all Java sources into bin/
javac -cp "lib/*:src" -d bin src/Main.java src/config/*.java src/models/*.java src/dao/*.java src/ui/*.java

# 2. Run the application
java -cp "lib/*:bin" Main
```

---

## Default Login Credentials

| Role | Email | Password | Redirects To |
| :--- | :--- | :--- | :--- |
| **Admin** | `admin@college.edu` | `admin123` | `AdminDashboard` |
| **Student (Alice)** | `alice@college.edu` | `student123` | `StudentDashboard` |
| **Student (Bob)** | `bob@college.edu` | `student123` | `StudentDashboard` |

*You can also create a new student or admin account directly from the "Register" tab on the login screen.*

---

## Course Evaluation Checklist (Marking Rubric Mapping)

This section maps our implementation to the evaluation criteria of the Java GUI course project rubric:

| Syllabus Requirement | Where It Is Implemented | How & Why It Was Used |
| :--- | :--- | :--- |
| **Inheritance** | `ui/LoginFrame.java`, `ui/StudentDashboard.java`, `ui/AdminDashboard.java` | Main windows extend `JFrame`; popups instantiate modal `JDialog` for reporting issues and viewing full descriptions. |
| **Polymorphism** | `models/*.java`, `ui/StudentDashboard.java`, `dao/*.java` | Overriding `@Override toString()` in models, `isCellEditable()` in `DefaultTableModel`, `doInBackground()` in `SwingWorker`, and implementing interface methods in DAOs. |
| **Interfaces** | `dao/UserDAOInterface.java`, `dao/IssueDAOInterface.java`, `ui/*.java` | DAO interfaces define the contract separately from database logic; standard Swing listeners (`ActionListener`, `MouseListener`) handle user interactions. |
| **Exception Handling** | `config/DatabaseConnection.java`, `dao/*.java` | Try-with-resources blocks safely close `PreparedStatement` and `ResultSet` objects. `SQLException` and `ClassNotFoundException` are caught with meaningful console logs and `JOptionPane` popups for users. |
| **Collections & Generics** | `dao/IssueDAO.java`, `ui/StudentDashboard.java` | Uses type-safe `List<Issue>` and `ArrayList<Issue>` to pass data between database queries and GUI tables; `JComboBox<String>` and `List<Object>` handle category/status filtering. |
| **Multithreading (EDT & SwingWorker)** | `Main.java`, `ui/StudentDashboard.java`, `ui/AdminDashboard.java` | `SwingUtilities.invokeLater()` ensures GUI creation runs on the Swing Event Dispatch Thread; `SwingWorker<List<Issue>, Void>` loads issues in a background thread so the interface does not freeze during queries. |
| **Thread Synchronization** | `config/DatabaseConnection.java` | `synchronized` methods on `getConnection()` and `closeConnection()` ensure safe access if multiple components request the database connection at once. |
| **Encapsulated Models** | `models/User.java`, `models/Issue.java` | Models use private fields with standard getters, setters, and constructors to keep object state protected. |
| **JDBC CRUD & PreparedStatements** | `dao/UserDAO.java`, `dao/IssueDAO.java` | Implements Create (`createIssue`, `registerUser`), Read (`getAllIssues`, `authenticate`), Update (`updateIssueStatus`, `upvoteIssue`), and Delete (`deleteIssue`). Every query uses `PreparedStatement` with `?` parameters to prevent SQL injection. |
| **Transaction Management** | `dao/IssueDAO.java` (`upvoteIssue`) | Uses `connection.setAutoCommit(false)`, `commit()`, and `rollback()` inside a `try-catch` block so inserting into `issue_upvotes` and incrementing `issues.upvotes` succeed together or roll back cleanly on failure. |
| **UI Design & Usability** | `ui/UIUtils.java` | A simple helper using `BasicButtonUI` that gives primary action buttons solid dark backgrounds with crisp white text, and secondary buttons light backgrounds with dark text, avoiding the classic Windows Look-and-Feel button contrast bug. |
