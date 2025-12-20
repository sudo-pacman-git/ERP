# University ERP Desktop Application

**A secure, role-based academic management system developed using Java Swing and MySQL.**

This project is a comprehensive desktop application designed to streamline university department operations. Developed as a final project for the Advanced Programming (AP) course, it handles the complete lifecycle of academic management, including user authentication, course enrollment, section management, and grading.

## 🛠 Technology Stack

* **Language:** Java (JDK 21)
* **GUI Framework:** Java Swing (Custom UI components)
* **Database:** MySQL 8.0
* **Build Tool:** Maven (Shade Plugin for executable generation)
* **Architecture:** MVC Pattern (Model-View-Controller)

##  Key Features

* **Role-Based Access Control (RBAC):** Distinct interfaces and permissions for three user types:
    * **Admins:** Manage users, force password resets, and oversee system data.
    * **Instructors:** Manage sections and assign grades to enrolled students.
    * **Students:** View available sections, enroll in courses, and view transcripts.
* **Secure Authentication:** Custom login validation with temporary password logic for first-time users.
* **Data Integrity:** Normalized MySQL schema ensuring consistent relationships between students, sections, and grades.

---

##  Setup & Installation

### 1. Prerequisites

| Software | Version | Purpose |
| :--- | :--- | :--- |
| **Java Development Kit (JDK)** | **21+** | Required to run the application. |
| **MySQL Server** | **8.0+** | Required to host the database. |

### 2. Database Setup (Crucial First Step)

1.  **Execute SQL Script:** Open `schema_setup.sql` in MySQL Workbench and execute the entire script. This creates the necessary tables (`users_auth`, `students`, `sections`) and inserts seed data.
2.  **Configure Connection:** Ensure your `DBConnection.java` file matches your local MySQL configuration:

| Setting | Auth Database | ERP Database |
| :--- | :--- | :--- |
| **URL Base** | `jdbc:mysql://localhost:3306/university_auth` | `jdbc:mysql://localhost:3306/university_erp` |
| **Username** | `root` | `root` |
| **Password** | *(Your Local MySQL Password)* | *(Your Local MySQL Password)* |

### 3. Running the Application

The application is provided as a single, self-contained executable JAR.

1.  Navigate your terminal to the **`target`** folder of the project.
2.  Execute the following command:

    ```bash
    java -jar univ-erp-java-swing-executable.jar
    ```

---

##  Default Login Credentials

Use these credentials to test the specific role functionalities.

| Role | Username | Password | Notes |
| :--- | :--- | :--- | :--- |
| **Admin** | `admin1` | `password123` | Full system control. |
| **Instructor** | `inst1` | `password123` | Assigned to all initial sections. |
| **Student** | `stu1` | `password123` | Used for acceptance tests. |
| **Temporary** | *(Any User)* | `temp123` | Temporary password if Admin forces a reset. |

---

##  Contributors

* **Harsh Panchal**
* **Aniket Kumar Rai**