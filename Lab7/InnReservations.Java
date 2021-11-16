import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

import java.util.Map;
import java.util.Scanner;
import java.util.LinkedHashMap;
import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList;

//export HP_JDBC_URL=jdbc:mysql://db.labthreesixfive.com/jmonteag?autoReconnect=true\&useSSL=false
//        export HP_JDBC_USER=jmonteag
//        export HP_JDBC_PW=csc365-F2021_026701200

public class InnReservations {

    public static void main(String[] args) {
        try {
            // boolean quitProgram = false;
            InnReservations rp = new InnReservations();
            
            // while(quitPro != true){
                menu();
                Scanner in = new Scanner(System.in);
                int input = in.nextInt();
                in.close();

                switch(input){
                    case 1:
                        rp.RoomsAndRate();
                        break;
                    case 2:
                        break;
                    case 3:
                        break;
                    case 4:
                        break;
                    case 5:
                        break;
                    case 6:
                        break;
                    // case 7:
                    //     quitProgram = true;
                    //     break;
                }
            // }

            rp.demo();

        } catch (SQLException e) {
            System.err.println("SQLException: " + e.getMessage());
        } catch (Exception e2) {
            System.err.println("Exception: " + e2.getMessage());
        }
    }

    private static void menu(){
        System.out.println("Choose one of the following: ");
        System.out.println("1. Rooms and Rates");
        System.out.println("2. Reservations");
        System.out.println("3. Reservation Change");
        System.out.println("4. Reservation Cancellation");
        System.out.println("5. Detailed Reservation Information");
        System.out.println("6. Revenue");
        System.out.println("7. Quit");
    }

    private void demo() throws SQLException {

        // Step 1: Establish connection to RDBMS
        try (Connection conn = DriverManager.getConnection(System.getenv("HP_JDBC_URL"),
                System.getenv("HP_JDBC_USER"),
                System.getenv("HP_JDBC_PW"))) {
            // Step 2: Construct SQL statement
            String sql = "ALTER TABLE lab7_rooms Drop Email";

            // Step 3: (omitted in this example) Start transaction

            try (Statement stmt = conn.createStatement()) {

                // Step 4: Send SQL statement to DBMS
                boolean exRes = stmt.execute(sql);

                // Step 5: Handle results
                System.out.format("Result from ALTER: %b %n", exRes);
            }

            // Step 6: (omitted in this example) Commit or rollback transaction
        }
        // Step 7: Close connection (handled by try-with-resources syntax)
    }

    private void RoomsAndRate() throws SQLException{
        try(Connection conn = DriverManager.getConnection(System.getenv("HP_JDBC_URL"), 
        System.getenv("HP_JDBC_USER"), System.getenv("HP_JDBC_PW"))){

        }
    }
}
