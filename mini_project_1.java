package com.dbms.lab;

import java.sql.*;
import java.util.Scanner;

public class mini_project_1 {

    // ================= DATABASE URL =================
    static final String DB_URL = "jdbc:derby:LibraryDB;create=true";

    // ================= MAIN =================
    public static void main(String[] args) {

        Connection con = null;
        Scanner sc = new Scanner(System.in);

        try {

            // Establish connection
            con = DriverManager.getConnection(DB_URL);

            System.out.println("Database Connected Successfully.");

            // Create tables if not exists
            createTables(con);

            while (true) {

                System.out.println("\n========== LIBRARY LOAN MANAGEMENT ==========");
                System.out.println("1. Register Member");
                System.out.println("2. Add Book");
                System.out.println("3. Process Loan");
                System.out.println("4. Return Book");
                System.out.println("5. View Active Loans");
                System.out.println("6. Performance Test");
                System.out.println("7. Exit");

                System.out.print("Enter Choice: ");

                int choice = sc.nextInt();
                sc.nextLine();

                switch (choice) {

                    case 1:
                        registerMember(con, sc);
                        break;

                    case 2:
                        addBook(con, sc);
                        break;

                    case 3:
                        processLoan(con, sc);
                        break;

                    case 4:
                        returnBook(con, sc);
                        break;

                    case 5:
                        viewLoans(con);
                        break;

                    case 6:
                        performanceTest(con);
                        break;

                    case 7:
                        System.out.println("Exiting Program...");
                        closeConnection(con);
                        shutdownDatabase();
                        sc.close();
                        System.exit(0);
                        break;

                    default:
                        System.out.println("Invalid Choice.");
                }
            }

        } catch (SQLException e) {

            System.out.println("\nERROR: " + e.getMessage());

            if ("XSDB6".equals(e.getSQLState())) {

                System.out.println("\nDerby database is locked.");
                System.out.println("Close previous running programs or restart Eclipse.");
                System.out.println("If problem continues:");
                System.out.println("1. Delete db.lck and dbex.lck");
                System.out.println("2. Or delete LibraryDB folder");
            }

        } finally {

            try {

                if (con != null && !con.isClosed()) {
                    con.close();
                }

            } catch (SQLException e) {
                e.printStackTrace();
            }

            shutdownDatabase();
        }
    }

    // ================= CREATE TABLES =================
    static void createTables(Connection con) {

        try (Statement st = con.createStatement()) {

            // MEMBERS TABLE
            try {

                st.executeUpdate(
                        "CREATE TABLE Members (" +
                                "MemberID INT PRIMARY KEY, " +
                                "Name VARCHAR(100), " +
                                "ActiveLoans INT DEFAULT 0)"
                );

                System.out.println("Members table created.");

            } catch (SQLException e) {

                if ("X0Y32".equals(e.getSQLState())) {
                    System.out.println("Members table already exists.");
                }
            }

            // BOOKS TABLE
            try {

                st.executeUpdate(
                        "CREATE TABLE Books (" +
                                "BookID INT PRIMARY KEY, " +
                                "Title VARCHAR(200), " +
                                "Author VARCHAR(100), " +
                                "Available BOOLEAN DEFAULT TRUE)"
                );

                System.out.println("Books table created.");

            } catch (SQLException e) {

                if ("X0Y32".equals(e.getSQLState())) {
                    System.out.println("Books table already exists.");
                }
            }

            // LOANS TABLE
            try {

                st.executeUpdate(
                        "CREATE TABLE Loans (" +
                                "LoanID INT PRIMARY KEY GENERATED ALWAYS AS IDENTITY, " +
                                "MemberID INT, " +
                                "BookID INT, " +
                                "LoanDate DATE, " +
                                "ReturnDate DATE, " +
                                "FOREIGN KEY (MemberID) REFERENCES Members(MemberID), " +
                                "FOREIGN KEY (BookID) REFERENCES Books(BookID))"
                );

                System.out.println("Loans table created.");

            } catch (SQLException e) {

                if ("X0Y32".equals(e.getSQLState())) {
                    System.out.println("Loans table already exists.");
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ================= REGISTER MEMBER =================
    static void registerMember(Connection con, Scanner sc) {

        String sql =
                "INSERT INTO Members(MemberID, Name, ActiveLoans) VALUES (?, ?, 0)";

        try (PreparedStatement ps = con.prepareStatement(sql)) {

            System.out.print("Enter Member ID: ");
            int memberId = sc.nextInt();
            sc.nextLine();

            System.out.print("Enter Member Name: ");
            String name = sc.nextLine();

            ps.setInt(1, memberId);
            ps.setString(2, name);

            ps.executeUpdate();

            System.out.println("Member Registered Successfully.");

        } catch (SQLException e) {

            if ("23505".equals(e.getSQLState())) {
                System.out.println("Member ID already exists.");
            } else {
                e.printStackTrace();
            }
        }
    }

    // ================= ADD BOOK =================
    static void addBook(Connection con, Scanner sc) {

        String sql =
                "INSERT INTO Books(BookID, Title, Author, Available) VALUES (?, ?, ?, TRUE)";

        try (PreparedStatement ps = con.prepareStatement(sql)) {

            System.out.print("Enter Book ID: ");
            int bookId = sc.nextInt();
            sc.nextLine();

            System.out.print("Enter Book Title: ");
            String title = sc.nextLine();

            System.out.print("Enter Author Name: ");
            String author = sc.nextLine();

            ps.setInt(1, bookId);
            ps.setString(2, title);
            ps.setString(3, author);

            ps.executeUpdate();

            System.out.println("Book Added Successfully.");

        } catch (SQLException e) {

            if ("23505".equals(e.getSQLState())) {
                System.out.println("Book ID already exists.");
            } else {
                e.printStackTrace();
            }
        }
    }

    // ================= PROCESS LOAN =================
    static void processLoan(Connection con, Scanner sc) {

        Savepoint savepoint = null;

        try {

            con.setAutoCommit(false);

            System.out.print("Enter Member ID: ");
            int memberId = sc.nextInt();

            System.out.print("Enter Book ID: ");
            int bookId = sc.nextInt();

            // CHECK BOOK
            PreparedStatement checkBook = con.prepareStatement(
                    "SELECT Available FROM Books WHERE BookID=?"
            );

            checkBook.setInt(1, bookId);

            ResultSet rs = checkBook.executeQuery();

            if (!rs.next()) {

                System.out.println("Book Not Found.");
                con.rollback();
                return;
            }

            boolean available = rs.getBoolean("Available");

            if (!available) {

                System.out.println("Book Already Loaned.");
                con.rollback();
                return;
            }

            // INSERT LOAN
            PreparedStatement loanStmt = con.prepareStatement(
                    "INSERT INTO Loans(MemberID, BookID, LoanDate) " +
                            "VALUES (?, ?, CURRENT_DATE)"
            );

            loanStmt.setInt(1, memberId);
            loanStmt.setInt(2, bookId);

            loanStmt.executeUpdate();

            savepoint = con.setSavepoint();

            // UPDATE BOOK STATUS
            PreparedStatement updateBook = con.prepareStatement(
                    "UPDATE Books SET Available=FALSE WHERE BookID=?"
            );

            updateBook.setInt(1, bookId);

            updateBook.executeUpdate();

            // UPDATE MEMBER LOAN COUNT
            PreparedStatement updateMember = con.prepareStatement(
                    "UPDATE Members SET ActiveLoans=ActiveLoans+1 WHERE MemberID=?"
            );

            updateMember.setInt(1, memberId);

            updateMember.executeUpdate();

            con.commit();

            System.out.println("Loan Processed Successfully.");

            rs.close();
            checkBook.close();
            loanStmt.close();
            updateBook.close();
            updateMember.close();

        } catch (SQLException e) {

            try {

                if (savepoint != null) {
                    con.rollback(savepoint);
                } else {
                    con.rollback();
                }

            } catch (SQLException ex) {
                ex.printStackTrace();
            }

            System.out.println("Transaction Failed.");
            e.printStackTrace();

        } finally {

            try {
                con.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    // ================= RETURN BOOK =================
    static void returnBook(Connection con, Scanner sc) {

        try {

            con.setAutoCommit(false);

            System.out.print("Enter Loan ID: ");
            int loanId = sc.nextInt();

            PreparedStatement ps = con.prepareStatement(
                    "SELECT MemberID, BookID FROM Loans " +
                            "WHERE LoanID=? AND ReturnDate IS NULL"
            );

            ps.setInt(1, loanId);

            ResultSet rs = ps.executeQuery();

            if (!rs.next()) {

                System.out.println("Loan Not Found.");
                con.rollback();
                return;
            }

            int memberId = rs.getInt("MemberID");
            int bookId = rs.getInt("BookID");

            // UPDATE RETURN DATE
            PreparedStatement returnStmt = con.prepareStatement(
                    "UPDATE Loans SET ReturnDate=CURRENT_DATE WHERE LoanID=?"
            );

            returnStmt.setInt(1, loanId);

            returnStmt.executeUpdate();

            // MAKE BOOK AVAILABLE
            PreparedStatement updateBook = con.prepareStatement(
                    "UPDATE Books SET Available=TRUE WHERE BookID=?"
            );

            updateBook.setInt(1, bookId);

            updateBook.executeUpdate();

            // UPDATE MEMBER COUNT
            PreparedStatement updateMember = con.prepareStatement(
                    "UPDATE Members SET ActiveLoans=ActiveLoans-1 WHERE MemberID=?"
            );

            updateMember.setInt(1, memberId);

            updateMember.executeUpdate();

            con.commit();

            System.out.println("Book Returned Successfully.");

            rs.close();
            ps.close();
            returnStmt.close();
            updateBook.close();
            updateMember.close();

        } catch (SQLException e) {

            try {
                con.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }

            e.printStackTrace();

        } finally {

            try {
                con.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    // ================= VIEW LOANS =================
    static void viewLoans(Connection con) {

        try {

            Statement st = con.createStatement();

            ResultSet rs = st.executeQuery(
                    "SELECT * FROM Loans WHERE ReturnDate IS NULL"
            );

            System.out.println("\n===== ACTIVE LOANS =====");

            while (rs.next()) {

                System.out.println(
                        "Loan ID: " + rs.getInt("LoanID") +
                                " | Member ID: " + rs.getInt("MemberID") +
                                " | Book ID: " + rs.getInt("BookID") +
                                " | Loan Date: " + rs.getDate("LoanDate")
                );
            }

            rs.close();
            st.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ================= PERFORMANCE TEST =================
    static void performanceTest(Connection con) {

        try {

            long start1, end1;

            Statement st = con.createStatement();

            // NORMAL INSERT
            start1 = System.nanoTime();

            for (int i = 1; i <= 1000; i++) {

                st.executeUpdate(
                        "INSERT INTO Members(MemberID, Name, ActiveLoans) " +
                                "VALUES (" + (10000 + i) + ", 'User" + i + "',0)"
                );
            }

            end1 = System.nanoTime();

            System.out.println("\nNormal Insert Time : "
                    + ((end1 - start1) / 1000000.0) + " ms");

            // BATCH INSERT
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO Members(MemberID, Name, ActiveLoans) VALUES (?, ?, 0)"
            );

            long start2 = System.nanoTime();

            for (int i = 1; i <= 1000; i++) {

                ps.setInt(1, 20000 + i);
                ps.setString(2, "BatchUser" + i);

                ps.addBatch();
            }

            ps.executeBatch();

            long end2 = System.nanoTime();

            System.out.println("Batch Insert Time  : "
                    + ((end2 - start2) / 1000000.0) + " ms");

            double throughput =
                    1000.0 / ((end2 - start2) / 1000000000.0);

            System.out.println("Throughput         : "
                    + throughput + " ops/sec");

            ps.close();
            st.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ================= CLOSE CONNECTION =================
    static void closeConnection(Connection con) {

        try {

            if (con != null && !con.isClosed()) {
                con.close();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ================= SHUTDOWN DATABASE =================
    static void shutdownDatabase() {

        try {

            DriverManager.getConnection(
                    "jdbc:derby:LibraryDB;shutdown=true"
            );

        } catch (SQLException e) {

            if ("XJ015".equals(e.getSQLState())) {
                System.out.println("Database Shutdown Successfully.");
            }
        }
    }
}
