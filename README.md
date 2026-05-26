# Library Loan Management System

A console-based **Library Loan Management System** developed in **Java using JDBC and Apache Derby Database**.  
This project demonstrates:

- JDBC connectivity
- Transaction management
- Savepoints & rollback
- Prepared Statements
- Batch processing
- Performance benchmarking
- ACID property implementation

---

# Features

## Member Management
- Register new library members
- Prevent duplicate member IDs

## Book Management
- Add new books
- Store title and author details
- Track book availability

## Loan Management
- Issue books to members
- Return books
- Prevent issuing unavailable books
- Automatically update active loan count

## Transaction Management
- Explicit transaction handling using:
  - `commit()`
  - `rollback()`
  - `Savepoint`
- Ensures database consistency during failures

## Performance Evaluation
Compares:
- Normal Inserts vs Batch Inserts
- Execution Time
- Throughput (operations/sec)

---

# Technologies Used

- Java
- JDBC
- Apache Derby Database
- Eclipse IDE

---

# Database Schema

## Members Table
| Column | Type |
|---|---|
| MemberID | INT (PK) |
| Name | VARCHAR |
| ActiveLoans | INT |

## Books Table
| Column | Type |
|---|---|
| BookID | INT (PK) |
| Title | VARCHAR |
| Author | VARCHAR |
| Available | BOOLEAN |

## Loans Table
| Column | Type |
|---|---|
| LoanID | INT (PK, Auto Increment) |
| MemberID | INT (FK) |
| BookID | INT (FK) |
| LoanDate | DATE |
| ReturnDate | DATE |

---

# Project Structure

```text
src/
└── com/dbms/lab/
    └── mini_project_1.java
```

---

# How to Run

## Step 1: Clone Repository

```bash
git clone https://github.com/tusharmallick/Library-Loan-Management-System.git
```

## Step 2: Open in Eclipse

* Open Eclipse
* Import Existing Java Project
* Add Apache Derby library to Build Path

---

## Step 3: Run Program

Run:

```bash
mini_project_1.java
```

---

# Menu Options

```text
========== LIBRARY LOAN MANAGEMENT ==========
1. Register Member
2. Add Book
3. Process Loan
4. Return Book
5. View Active Loans
6. Performance Test
7. Exit
```

---

# Sample Output

```text
Database Connected Successfully.

========== LIBRARY LOAN MANAGEMENT ==========
1. Register Member
2. Add Book
3. Process Loan
4. Return Book
5. View Active Loans
6. Performance Test
7. Exit

Enter Choice: 1

Enter Member ID: 101
Enter Member Name: Tushar

Member Registered Successfully.
```

---

# Transaction Workflow

## Loan Processing

The system performs:

1. Check book availability
2. Insert loan record
3. Update book availability
4. Update member active loan count
5. Commit transaction

If any step fails:

* Rollback occurs automatically
* Database consistency is preserved

---

# Performance Benchmark

The application compares:

* Normal `executeUpdate()`
* Batch `executeBatch()`

Metrics:

* Execution Time (ms)
* Throughput (ops/sec)

---

# Error Handling

The system handles:

* Duplicate IDs
* Constraint violations
* Database lock issues
* Transaction failures

---

# ACID Properties Demonstrated

| Property    | Implementation           |
| ----------- | ------------------------ |
| Atomicity   | Commit & Rollback        |
| Consistency | Constraints & FK         |
| Isolation   | Transaction control      |
| Durability  | Derby persistent storage |

---

# Future Improvements

* GUI using Java Swing/JavaFX
* Search books feature
* Fine calculation
* Multi-user concurrency
* Admin authentication
* Report generation

---

# Author

Tushar Mallick
B.Tech CSE (AI/ML)

---

# License

This project is developed for educational and academic purposes.
