import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.io.BufferedReader;
import java.io.FileFilter;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.text.DecimalFormat;

import java.util.Map;
import java.util.Random;
import java.util.Scanner;

import javax.swing.RootPaneContainer;

import java.util.LinkedHashMap;
import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList;

import java.time.DayOfWeek;

//export HP_JDBC_URL=jdbc:mysql://db.labthreesixfive.com/jmonteag?autoReconnect=true\&useSSL=false
//        export HP_JDBC_USER=jmonteag
//        export HP_JDBC_PW=csc365-F2021_026701200

public class InnReservations {

    //int gne = 100002;
    DecimalFormat df = new DecimalFormat("0.00");

    private static BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
    public static void main(String[] args) {
        try {
            String FirstName = "";
            String LastName = "";
            boolean quitProgram = false;
            InnReservations rp = new InnReservations();
            Scanner in = new Scanner(System.in);
            while(quitProgram != true){
                menu();
                int input = in.nextInt();
                in.nextLine();
                if(input == 1){
                    rp.FR1();
                }
                else if(input == 2){
                    rp.FR2();
                }
                else if(input == 3){
                    rp.FR3();
                }
                else if(input == 4 ){
                    rp.FR4();
                }
                else if(input == 5){
                    rp.FR5();
                }
                else if (input == 6){
                    rp.FR6();
                }
                else if(input == 7){
                    quitProgram = true;
                }
            }
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

    private void FR1() throws SQLException{
        try(Connection conn = DriverManager.getConnection(System.getenv("HP_JDBC_URL"), 
        System.getenv("HP_JDBC_USER"), System.getenv("HP_JDBC_PW"))){
            String RoomCode = "";
            String RoomName = "";
            int beds = 0;
            String bedType = "";
            int maxOcc = 0;
            int basePrice = 0;
            String decor = "";
            double popularityScore = 0;
            String NextAvailableCheckIn = "";
            int lenOfStay = 0;

            StringBuilder sb = new StringBuilder("WITH PopulScore AS (SELECT r.RoomCode, r.RoomName, ROUND(SUM(DATEDIFF(CASE WHEN re.CheckOut > CURRENT_DATE THEN ");
            sb.append("CURRENT_DATE ELSE re.CheckOut END, CASE WHEN re.CheckIn < DATE_SUB(CURRENT_DATE, INTERVAL 180 DAY) THEN DATE_SUB(CURRENT_DATE, INTERVAL 180 DAY) ELSE re.CheckIn END)) / 180, 2) AS popularityScore ");
            sb.append("FROM jmonteag.lab7_rooms r ");
            sb.append("INNER JOIN jmonteag.lab7_reservations re ON re.Room = r.RoomCode ");
            sb.append("WHERE NOT (re.CheckOut <= DATE_SUB(CURRENT_DATE, INTERVAL 180 DAY)) AND NOT (re.CheckIn >= CURRENT_DATE()) ");
            sb.append("GROUP BY r.RoomCode, r.RoomName), ");
            sb.append("NextAvailable AS (WITH NextAva AS(SELECT Room, CheckIn, CheckOut ");
            sb.append("FROM jmonteag.lab7_reservations ");
            sb.append("WHERE CheckOut >= CURRENT_DATE GROUP BY Room, CheckIn, CheckOut) ");
            sb.append("SELECT Room, MIN(CheckOut) AS NextAvailableCheckIn ");
            sb.append("FROM NextAva n1 WHERE CheckOut NOT IN(SELECT CheckIn FROM NextAva n2 WHERE n2.Room = n1.Room) ");
            sb.append("GROUP BY Room ORDER BY Room), ");
            sb.append("RecentLength AS (WITH Length AS(WITH MostRecentStay AS ( ");
            sb.append("SELECT Room, CheckIn, CheckOut, DATEDIFF(CheckOut, CheckIn) AS lenOfStay ");
            sb.append("FROM jmonteag.lab7_reservations GROUP BY Room, CheckIn, CheckOut) ");
            sb.append("SELECT Room, MAX(CheckOut) FROM MostRecentStay WHERE CheckOut <= CURDATE() GROUP BY Room) ");
            sb.append("SELECT re.Room, DATEDIFF(CheckOut, CheckIn) AS lenOfStay FROM jmonteag.lab7_reservations re WHERE (re.Room, CheckOut) IN (SELECT * FROM Length) GROUP BY re.Room, re.CheckOut, re.CheckIn),");
            sb.append("Combined AS (SELECT n.Room, NextAvailableCheckIn, IFNULL(lenOfStay, 0) AS lenOfStay ");
            sb.append("FROM NextAvailable n LEFT OUTER JOIN RecentLength l ON n.Room = l.Room UNION  ");
            sb.append("SELECT l.Room, NextAvailableCheckIn, lenOfStay FROM NextAvailable n RIGHT OUTER JOIN RecentLength l ON n.Room = l.Room)");
            sb.append("SELECT r.RoomCode, r.RoomName, r.Beds, r.bedType, r.maxOcc, r.basePrice, r.decor, p.popularityScore, c.NextAvailableCheckIn, c.lenOfStay ");
            sb.append("FROM jmonteag.lab7_rooms r ");
            sb.append("INNER JOIN PopulScore p ON p.RoomCode = r.RoomCode ");
            sb.append("INNER JOIN Combined c ON c.Room = p.RoomCode ");
            sb.append("GROUP BY r.RoomCode, r.RoomName, r.Beds, r.bedType, r.maxOcc, r.basePrice, r.decor, p.popularityScore, c.NextAvailableCheckIn, c.lenOfStay ");
            sb.append("ORDER BY p.popularityScore DESC; ");

            String sql = sb.toString();

            try(PreparedStatement stmt = conn.prepareStatement(sql)){
                try(ResultSet rs = stmt.executeQuery()){
                    while(rs.next()){
                        RoomCode = rs.getString("RoomCode");
                        RoomName = rs.getString("RoomName");
                        beds = rs.getInt("Beds");
                        bedType = rs.getString("bedType");
                        maxOcc = rs.getInt("basePrice");
                        decor = rs.getString("decor");
                        popularityScore = rs.getDouble("popularityScore");
                        NextAvailableCheckIn = rs.getString("NextAvailableCheckIn");
                        lenOfStay = rs.getInt("lenOfStay");

                        if(NextAvailableCheckIn == null){NextAvailableCheckIn = "Today";}

                        System.out.format("|%-10s |%-25s |%-10s |%-10s |%-10s |%-10s |%-15s |%-25s |%-25s\n", RoomCode, RoomName, beds, bedType, maxOcc, basePrice, decor, popularityScore, NextAvailableCheckIn, lenOfStay);

                    }
                }
            }
        }
    
    }

    private void FR2() throws SQLException{
        try(Connection conn = DriverManager.getConnection(System.getenv("HP_JDBC_URL"), 
        System.getenv("HP_JDBC_USER"), System.getenv("HP_JDBC_PW"))){

            Scanner in = new Scanner(System.in);
            String firstName = "";
            String lastName = "";
            String roomId = "";
            String bedType = "";
            String arriveDate = "";
            String departureDate = "";
            int children = 0;
            int adults = 0;
            int answ = 0;
            int gne = 0;
    
            System.out.print("\nFirst Name: ");
            firstName = in.nextLine();
            System.out.print("\nLast Name: ");
            lastName = in.nextLine();
            System.out.print("\nDesired Room Code: ");
            roomId = in.nextLine();
            System.out.print("\nDesired Bed Type: ");
            bedType = in.nextLine();
            System.out.print("\nBeginning Date of Stay [YYYY-MM-DD]: ");
            arriveDate = in.nextLine();
            System.out.print("\nEnding Date of Stay [YYYY-MM-DD]: ");
            departureDate = in.nextLine();
            System.out.print("\nNumber of Children: ");
            children = in.nextInt();
            in.nextLine();
            System.out.print("\nNumber of Adults: ");
            adults = in.nextInt();
            in.nextLine();
            
            String s2 = "SELECT MAX(CODE) AS maxCode FROM jmonteag.lab7_reservations";
            try(Statement st8 = conn.createStatement()){
                ResultSet r1 = st8.executeQuery(s2);
                while(r1.next()){
                    gne = r1.getInt("maxCode");
                }
            }


            String sql = "SELECT MAX(maxOcc) AS maxOccupancy FROM jmonteag.lab7_rooms";

            try(Statement stmt = conn.createStatement()){
                

                ResultSet rs = stmt.executeQuery(sql);
                while(rs.next()){
                    answ = rs.getInt("maxOccupancy");
                }
                if(answ < (children+adults)){
                    System.out.println("\nWe are not able to make your reservation as it exceeds our highest occupancy available in our hotel.");
                    System.out.println("Please try again.\n");
                    return;
                }
                else{ 
                    //find the exact match date and room and bedtype
                    // SELECT * FROM jmonteag.lab7_rooms
                    // 	 // WHERE RoomCode NOT IN (
                    // 	 // SELECT Room FROM jmonteag.lab7_reservations
                    // 	 // WHERE (CheckIn <= @arriveDate AND CheckOut >= @arriveDate)
                    // 	 // OR (CheckIn < @departureDate AND CheckOut >= @departureDate)
                    // 	 // OR (@arriveDate <= CheckIn AND @departureDate >= CheckIn)
                    // 	 // )
                    StringBuilder statement = new StringBuilder("SELECT * FROM jmonteag.lab7_rooms ");
                    statement.append("WHERE RoomCode NOT IN ( ");
                    statement.append("SELECT Room FROM jmonteag.lab7_reservations ");
                    statement.append("WHERE (CheckIn <= ? AND CheckOut >= ?) ");
                    statement.append("OR (CheckIn < ? AND CheckOut >= ?) ");
                    statement.append("OR (? <= CheckIn AND ? >= CheckIn)) AND RoomCode = ?  ");
                    statement.append("GROUP BY RoomCode ");
                    statement.append("HAVING maxOcc > ?");
                    
                    String sql1 = statement.toString();

                    try(PreparedStatement stmt1 = conn.prepareStatement(sql1)){
                        stmt1.setDate(1, Date.valueOf(arriveDate));
        				stmt1.setDate(2, Date.valueOf(arriveDate));
        		        stmt1.setDate(3, Date.valueOf(departureDate));
        		        stmt1.setDate(4, Date.valueOf(departureDate));
        		        stmt1.setDate(5, Date.valueOf(arriveDate));
        		        stmt1.setDate(6, Date.valueOf(departureDate));
                        stmt1.setString(7, roomId);
                        stmt1.setInt(8, children + adults);

                        ResultSet rs1 = stmt1.executeQuery();

                        
                        //if result is found
                        while(rs1.next()){
                            String room = rs1.getString("RoomCode");

                            System.out.println(room);
                            // set variables accordingly
                            // DETERMINE COST HERE
                            double cost = 0;
                            double rate = rs1.getFloat("basePrice");
            
                            for (LocalDate date = LocalDate.parse(arriveDate); date.isBefore(LocalDate.parse(departureDate)); date = date.plusDays(1))
                            {
                                DayOfWeek dayOfWeek = DayOfWeek.from(date);
                                int day = dayOfWeek.getValue();
                                //monday =1, sunday = 7
                                if(day >= 1 && day <=5){
                                    cost += rate;
                                }
                                else if (day == 6 || day == 7){
                                    cost += (rate * 1.10);
                                }
                            }
                            // display confirmation screen:
                            System.out.println("Please confirm reservation with following info [Y/N]:");
                            System.out.println("First Name: " + firstName);
                            System.out.println("Last Name: " + lastName);
                            System.out.println("Room Code: " + roomId);
                            System.out.println("Room Name:" + rs1.getString("RoomName"));
                            System.out.println("Bed Type: " + bedType);
                            System.out.println("Start Date: " + arriveDate);
                            System.out.println("End Date: " + departureDate);
                            System.out.println("Number of Kids: " + children);
                            System.out.println("Number of Adults: " + adults);
                            System.out.println("Total Cost: $" + df.format(cost));
                            String confirm = in.nextLine();                            
                            if(confirm.equals("Y"))
                            {
                                try(PreparedStatement stmt2 = conn.prepareStatement("INSERT INTO lab7_reservations (CODE, Room, CheckIn, CheckOut, Rate, LastName, FirstName, Adults, Kids) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?);"))
                                {
                                    gne += 1;
                                    
                                    stmt2.setInt(1, gne);
                                    stmt2.setString(2, roomId);
                                    stmt2.setString(3, arriveDate);
                                    stmt2.setString(4, departureDate);
                                    stmt2.setDouble(5, cost);
                                    stmt2.setString(6, lastName);
                                    stmt2.setString(7, firstName);
                                    stmt2.setInt(8, adults);
                                    stmt2.setInt(9,children);
                                    
                                    conn.setAutoCommit(false);
                                    stmt2.execute();
                                    conn.commit();
                                    System.out.println("Reservation has been successfully added.\n");
                                    return;
                                }catch(Exception e)
                                {
                                    System.out.println(e);
                                }
                            }
                    		else
                            {
                                System.out.println("Cancelling current request. Returning to the main menu.\n");
                            	return;
                            }
                        }
                        //if no exact results are found --> give alternative
                        System.out.println("No exact rooms match your request. Do you want to look for alternatives? [Y/N]:");
                        String altconfirm = in.nextLine();   
                        if(altconfirm.equals("Y")){
                            ArrayList<ArrayList<String>> totalalternatives = new ArrayList<ArrayList<String>>();
                            LocalDate arrdate = LocalDate.parse(arriveDate);
                            LocalDate depdate = LocalDate.parse(arriveDate);
                            //create sql statement here
                            while(totalalternatives.size() < 5){
                                arrdate = arrdate.plusDays(1);
                                depdate = depdate.plusDays(1);
                                StringBuilder statement1 = new StringBuilder("SELECT * FROM jmonteag.lab7_rooms ");
                                statement1.append("WHERE RoomCode NOT IN ( ");
                                statement1.append("SELECT Room FROM jmonteag.lab7_reservations ");
                                statement1.append("WHERE (CheckIn <= ? AND CheckOut >= ?) ");
                                statement1.append("OR (CheckIn < ? AND CheckOut >= ?) ");
                                statement1.append("OR (? <= CheckIn AND ? >= CheckIn)) AND RoomCode = ?  ");
                                statement1.append("GROUP BY RoomCode ");
                                statement1.append("HAVING maxOcc > ?");

                                String bleh = statement1.toString();
                                try(PreparedStatement stmt3 = conn.prepareStatement(bleh)){
                                    stmt3.setDate(1, Date.valueOf(arrdate));
                                    stmt3.setDate(2, Date.valueOf(arrdate));
                                    stmt3.setDate(3, Date.valueOf(depdate));
                                    stmt3.setDate(4, Date.valueOf(depdate));
                                    stmt3.setDate(5, Date.valueOf(arrdate));
                                    stmt3.setDate(6, Date.valueOf(depdate));
                                    stmt3.setString(7, roomId);
                                    stmt3.setInt(8, children + adults);

                                    ResultSet result = stmt3.executeQuery();
                                    while (result.next()) {
                                        ArrayList<String> inner = new ArrayList<String>();
                                        inner.add(result.getString("RoomCode"));
                                        inner.add(result.getString("RoomName"));
                                        inner.add(result.getString("bedType"));
                                        inner.add(arrdate.toString());
                                        inner.add(depdate.toString());
                                        inner.add(result.getString("BasePrice"));
                                        totalalternatives.add(inner);
                                    }
                                }
                            }
                            //list out options:
                            System.out.println("The available room codes are as follows, type in corresponding number to select an option:");
                            int j =1;
                            for (int i = 0; i < 5; i++) {
                                System.out.println(j +": " + totalalternatives.get(i));
                                j++;
                            }
                            System.out.println("0: Cancel selection, return to main menu");
                            // Scanner in = new Scanner(System.in);
                            int input = in.nextInt();
                            in.nextLine();
                            if(input == 0){
                                System.out.println("Cancelling current request. Returning to the main menu.\n");
                                return;
                            }
                            String altRoomCode = (totalalternatives.get(input-1)).get(0);
                            String altRoomName = (totalalternatives.get(input-1)).get(1);
                            String altbedType = (totalalternatives.get(input-1)).get(2);
                            String altArrive = (totalalternatives.get(input-1)).get(3);
                            String altDepart = (totalalternatives.get(input-1)).get(4);
                            String altRate = (totalalternatives.get(input-1)).get(5);
                            
                            
                            try(PreparedStatement stmt4 = conn.prepareStatement("INSERT INTO lab7_reservations (CODE, Room, CheckIn, CheckOut, Rate, LastName, FirstName, Adults, Kids) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?);")){
                                gne += 1;

                                stmt4.setInt(1, gne);
                                stmt4.setString(2, altRoomCode);
                                stmt4.setString(3, altArrive);
                                stmt4.setString(4, altDepart);
                                stmt4.setString(5, altRate);
                                stmt4.setString(6, lastName);
                                stmt4.setString(7, firstName);
                                stmt4.setInt(8, adults);
                                stmt4.setInt(9,children);

                                
                                
                                // DETERMINE COST HERE
                                double cost = 0;
                                double rate = Double.parseDouble((totalalternatives.get(input-1)).get(5));
                
                                for (LocalDate date = LocalDate.parse(arriveDate); date.isBefore(LocalDate.parse(departureDate)); date = date.plusDays(1))
                                {
                                    DayOfWeek dayOfWeek = DayOfWeek.from(date);
                                    int day = dayOfWeek.getValue();
                                    //monday =1, sunday = 7
                                    if(day >= 1 && day <=5){
                                        cost += rate;
                                    }
                                    else if (day == 6 || day == 7){
                                        cost += (rate * 1.10);
                                    }
                                }
                                // display confirmation screen:
                                System.out.println("Please confirm reservation with following info [Y/N]:");
                                System.out.println("First Name: " + firstName);
                                System.out.println("Last Name: " + lastName);
                                System.out.println("Room Code: " + altRoomCode);
                                System.out.println("Room Name: " + altRoomName);
                                System.out.println("Bed Type: " + altbedType);
                                System.out.println("Start Date: " + altArrive);
                                System.out.println("End Date: " + altDepart);
                                System.out.println("Number of Kids: " + children);
                                System.out.println("Number of Adults: " + adults);
                                System.out.println("Total Cost: $" + df.format(cost));
                                String confirm = "";
                                confirm = in.nextLine();

                                if(confirm.equals("Y"))
                                {
                                    conn.setAutoCommit(false);
                                    stmt4.execute();
                                    conn.commit();
                                    System.out.println("Reservation has been successfully added.\n");
                                }
                                else{
                                    return;
                                }
                            }catch(Exception e)
                            {
                                System.out.println(e);
                            }
                        }
                        else{
                            System.out.println("Cancelling current request. Returning to the main menu.\n");
                            return;
                        }
                    }catch(Exception e)
                    {
                        System.out.println(e);
                    }
                   
                }
            }
            
        }
    }

    private void FR3()throws SQLException{
        
        
        Scanner in = new Scanner(System.in);
        System.out.println("Please enter reservation code: ");
        int Reservation = in.nextInt();
        in.nextLine();

        System.out.println("Please choose a variable you want to change: ");
        System.out.println("1. FirstName");
        System.out.println("2. LastName");
        System.out.println("3. Begin Date");
        System.out.println("4. End Date");
        System.out.println("5. # of children");
        System.out.println("6. # of adults");
        
        try(Connection conn = DriverManager.getConnection(System.getenv("HP_JDBC_URL"), 
        System.getenv("HP_JDBC_USER"), System.getenv("HP_JDBC_PW"))){
            
            int input = in.nextInt();
            in.nextLine();
            
            if(input == 1){
                System.out.println("Enter First Name: ");
                String FirstName = in.nextLine();
                // if(FirstName.isBlank()){FirstName = null;}

                StringBuilder sb = new StringBuilder("update lab7_reservations ");
                sb.append("set FirstName = ? ");
                sb.append("where CODE = ?");
                String sql = sb.toString();
                conn.setAutoCommit(false);

                try(PreparedStatement stmt = conn.prepareStatement(sql)){
                    stmt.setString(1, FirstName);
                    stmt.setInt(2, Reservation);
                    stmt.execute();
                    conn.commit();
                    System.out.println("Updated FirstName");
                }
                catch(SQLException e){
                    conn.rollback();
                    System.out.println("Failed to update");
                }
            }
            else if(input == 2){
                System.out.println("Enter Last Name: ");
                String LastName = in.nextLine();
                if(LastName.isBlank()){LastName = null;}

                String sql = "update lab7_reservations set LastName = ? where CODE = ?";
                conn.setAutoCommit(false);

                try(PreparedStatement stmt = conn.prepareStatement(sql)){
                    stmt.setString(1, LastName);
                    stmt.setInt(2, Reservation);
                    stmt.execute();
                    conn.commit();
                    System.out.println("Updated LastName");
                }
                catch(SQLException e){
                    conn.rollback();
                    System.out.println("Failed to update");
                }
    
            }
            else if(input == 3){
                System.out.println("Enter the Begin Date: ");
                String Begin = in.nextLine();
                if(Begin.isBlank()){Begin = null;}

                StringBuilder sb = new StringBuilder("with sameroom as( ");
                sb.append("select CODE, Room, CheckIn, CheckOut, LastName from lab7_reservations res ");
                sb.append("where Room = (select Room from lab7_reservations where CODE = ?) and CODE != ? ),");
                sb.append("conflict as ( ");
                sb.append("select * from sameroom ");
                sb.append("where (CheckIn between ? and (select Checkout from jmonteag.lab7_reservations res where CODE = ?)) )");
                sb.append("select CODE from conflict ");

                String sql = sb.toString();

                try(PreparedStatement stmt = conn.prepareStatement(sql)){
                    stmt.setInt(1, Reservation);
                    stmt.setInt(2, Reservation);
                    stmt.setDate(3, Date.valueOf(Begin));
                    stmt.setInt(4, Reservation);

                    ResultSet rs = stmt.executeQuery();

                    if(rs.next() == false ){
                        StringBuilder sb2  = new StringBuilder("update lab7_reservations ");
                        sb2.append("set CheckIn = ? where CODE = ?");
                        String sql2 = sb2.toString();
                        try(PreparedStatement stmt2 = conn.prepareStatement(sql2)){
                            stmt2.setDate(1, Date.valueOf(Begin));
                            stmt2.setInt(2, Reservation);
                            stmt2.execute();
                        }
                    }
                }
    
            }
            else if(input == 4){
    
                System.out.println("Enter the End Date: ");
                String End = in.nextLine();
                // if(End.isBlank()){End = null;}

                StringBuilder sb = new StringBuilder("with sameroom as( ");
                sb.append("select CODE, Room, CheckIn, CheckOut, LastName from lab7_reservations res ");
                sb.append("where Room = (select Room from lab7_reservations where CODE = ?) and CODE != ? ),");
                sb.append("conflict as ( ");
                sb.append("select * from sameroom ");
                sb.append("where (Checkout between ? and (select CheckIn from lab7_reservations res where CODE = ?)) )");
                sb.append("select CODE from conflict ");

                String sql = sb.toString();

                try(PreparedStatement stmt = conn.prepareStatement(sql)){
                    stmt.setInt(1, Reservation);
                    stmt.setInt(2, Reservation);
                    stmt.setDate(3, Date.valueOf(End));
                    stmt.setInt(4, Reservation);

                    ResultSet rs = stmt.executeQuery();

                    if(!rs.isBeforeFirst()){
                        StringBuilder sb2  = new StringBuilder("update lab7_reservations ");
                        sb2.append("set Checkout = ? where CODE = ?");
                        String sql2 = sb2.toString();
                        try(PreparedStatement stmt2 = conn.prepareStatement(sql2)){
                            stmt2.setDate(1, Date.valueOf(End));
                            stmt2.setInt(2, Reservation);
                            stmt2.execute();
                        }
                    }
                }
            }
            else if(input == 5){
                
                System.out.println("Enter the # of children: ");
                String NumChild = in.nextLine();
                if(NumChild.isBlank()){NumChild = null;}

                String sql = "update lab7_reservations set Kids = ? where CODE = ?";

                try(PreparedStatement stmt = conn.prepareStatement(sql)){
                    stmt.setString(1, NumChild);
                    stmt.setInt(2, Reservation);
                    stmt.execute();
                    conn.commit();
                    System.out.println("Updated Number of children");
                }
                catch(SQLException e){
                    conn.rollback();
                    System.out.println("Failed to update");
                }
            }
            else if(input == 6){
                
                System.out.println("Enter the # of adults: ");
                String NumAdult = in.nextLine();
                if(NumAdult.isBlank()){NumAdult = null;}

                String sql = "update lab7_reservations set Adults = ? where CODE = ?";

                try(PreparedStatement stmt = conn.prepareStatement(sql)){
                    stmt.setString(1, NumAdult);
                    stmt.setInt(2, Reservation);
                    stmt.execute();
                    conn.commit();
                    System.out.println("Updated Number of Adults");
                }
                catch(SQLException e){
                    conn.rollback();
                    System.out.println("Failed to update");
                }
            }
            
        }
    }

    private void FR4() throws SQLException{

        Scanner in = new Scanner(System.in);
        System.out.println("Please enter the reserveCode you want to cancel: ");
        String reserveCode = in.nextLine();

        System.out.println("Are you sure you want to cancel? (Y/N): ");
        String cancelConfirm = in.nextLine();
        in.close();
        if(cancelConfirm.equals("Y") || cancelConfirm.equals("y")){
         
            try(Connection conn = DriverManager.getConnection(System.getenv("HP_JDBC_URL"), 
            System.getenv("HP_JDBC_USER"), System.getenv("HP_JDBC_PW"))){
        
                try(PreparedStatement stmt = conn.prepareStatement("delete from lab7_reservations where CODE = ? ")){
                    stmt.setString(1,reserveCode);
                    stmt.execute();
                    System.out.println("Reservation has been canceled");
                    conn.commit();
                }catch(SQLException e){
                    conn.rollback();
                }
            }
        }
    }

    private void FR5 () throws SQLException{
        

        Scanner in = new Scanner(System.in);
        System.out.println("Please enter First Name: ");
        String FirstName = in.nextLine();
        System.out.println("Please enter Last Name: ");
        String LastName = in.nextLine();
        System.out.println("Please enter CheckIn date: ");
        String Range = in.nextLine();
        System.out.println("Please enter CheckOut date: ");
        String Range2 = in.nextLine();
        System.out.println("Please enter room code: ");
        String RoomCode = in.nextLine();
        System.out.println("Please enter reservation code: ");
        String Reserve = in.nextLine();

        try(Connection conn = DriverManager.getConnection(System.getenv("HP_JDBC_URL"), 
        System.getenv("HP_JDBC_USER"), System.getenv("HP_JDBC_PW"))){

            StringBuilder sb = new StringBuilder("select * from lab7_reservations where ");
            sb.append("(FirstName = ? OR FirstName is not null) ");
            sb.append("and (LastName = ? OR LastName is not null) ");
            sb.append("and ((CheckIn between ? and ?) OR CheckIn is not null) ");
            sb.append("and ((Checkout between ? and ?) OR Checkout is not null) ");
            sb.append("and (Room = ? OR Room is not null) ");
            sb.append("and (CODE = ? OR CODE is not null) ");

            // if(FirstName.isBlank()){FirstName = "ALL";}
            // if(LastName.isBlank()){LastName = "ALL";}
            // if(Range.isBlank()){Range = "ALL";}
            // if(Range2.isBlank()){Range2 = "ALL";}
            // if(RoomCode.isBlank()){RoomCode = "ALL";}
            // if(Reserve.isBlank()){Reserve = "ALL";}

            String sql = sb.toString();
            try(PreparedStatement stmt = conn.prepareStatement(sql)){
                stmt.setString(1, FirstName);
                stmt.setString(2, LastName);
                stmt.setString(3, Range);
                stmt.setString(4, Range2);
                stmt.setString(5, Range);
                stmt.setString(6, Range2);
                stmt.setString(7, RoomCode);
                stmt.setString(8, Reserve);

                ResultSet rs = stmt.executeQuery();

                while(rs.next()){
                    int Code = rs.getInt("CODE");
                    String Room = rs.getString("Room");
                    String CheckIn = rs.getString("CheckIn");
                    String Checkout = rs.getString("Checkout");
                    double Rate = rs.getFloat("Rate");
                    String Last = rs.getString("LastName");
                    String First = rs.getString("FirstName");
                    int Adults = rs.getInt("Adults");
                    int Kids = rs.getInt("Kids");

                    System.out.format("|%-10s |%-25s |%-10s |%-10s |%-10s |%-10s |%-15s |%-25s |%-25s\n", Code, Room, CheckIn, Checkout, Rate, Last, First, Adults, Kids);

                }
            }
        }
    }

    private void FR6 () throws SQLException{
        try(Connection conn = DriverManager.getConnection(System.getenv("HP_JDBC_URL"), 
        System.getenv("HP_JDBC_USER"), System.getenv("HP_JDBC_PW"))){

            String Room = "";
            String January = "";
            String February = "";
            String March = "";
            String April = "";
            String May = "";
            String June = "";
            String July = "";
            String August = "";
            String September = "";
            String October = "";
            String November = "";
            String December = "";
            String Total = "";

            StringBuilder sb = new StringBuilder("WITH eachMonth AS (SELECT Room, ROUND(SUM(CASE WHEN (MONTH(CheckIn) = 1 OR MONTH(CheckOut) = 1) THEN ");
            sb.append("DATEDIFF(CASE WHEN CheckOut > '2021-01-31' THEN '2021-02-01' ELSE CheckOut END, CASE WHEN CheckIn < '2021-01-01' THEN '2020-12-31' ELSE CheckIn END)* rate ");
            sb.append("ELSE 0 END ), 0) AS January,");
            sb.append("ROUND(SUM(CASE WHEN (MONTH(CheckIn) = 2 OR MONTH(CheckOut) = 2) THEN ");
            sb.append("DATEDIFF(CASE WHEN CheckOut > '2021-02-28' THEN '2021-03-01' ELSE CheckOut END, CASE WHEN CheckIn < '2021-02-01' THEN '2021-01-31' ELSE CheckIn END)* rate ");
            sb.append("ELSE 0 END ), 0) AS February,");
            sb.append("ROUND(SUM(CASE WHEN (MONTH(CheckIn) = 3 OR MONTH(CheckOut) = 3) THEN ");
            sb.append("DATEDIFF(CASE WHEN CheckOut > '2021-03-31' THEN '2021-04-01' ELSE CheckOut END, CASE WHEN CheckIn < '2021-03-01' THEN '2021-02-28' ELSE CheckIn END)* rate ");
            sb.append("ELSE 0 END ), 0) AS March,");
            sb.append("ROUND(SUM(CASE WHEN (MONTH(CheckIn) = 4 OR MONTH(CheckOut) = 4) THEN ");
            sb.append("DATEDIFF(CASE WHEN CheckOut > '2021-04-30' THEN '2021-05-01' ELSE CheckOut END, CASE WHEN CheckIn < '2021-04-01' THEN '2021-03-31' ELSE CheckIn END)* rate ");
            sb.append("ELSE 0 END ), 0) AS April,");
            sb.append("ROUND(SUM(CASE WHEN (MONTH(CheckIn) = 5 OR MONTH(CheckOut) = 5) THEN ");
            sb.append("DATEDIFF(CASE WHEN CheckOut > '2021-05-31' THEN '2021-06-01' ELSE CheckOut END, CASE WHEN CheckIn < '2021-05-01' THEN '2021-04-30' ELSE CheckIn END)* rate ");
            sb.append("ELSE 0 END ), 0) AS May,");
            sb.append("ROUND(SUM(CASE WHEN (MONTH(CheckIn) = 6 OR MONTH(CheckOut) = 6) THEN ");
            sb.append("DATEDIFF(CASE WHEN CheckOut > '2021-06-30' THEN '2021-07-01' ELSE CheckOut END, CASE WHEN CheckIn < '2021-06-01' THEN '2021-05-31' ELSE CheckIn END)* rate ");
            sb.append("ELSE 0 END ), 0) AS June,");
            sb.append("ROUND(SUM(CASE WHEN (MONTH(CheckIn) = 7 OR MONTH(CheckOut) = 7) THEN ");
            sb.append("DATEDIFF(CASE WHEN CheckOut > '2021-07-31' THEN '2021-08-01' ELSE CheckOut END, CASE WHEN CheckIn < '2021-07-01' THEN '2021-06-30' ELSE CheckIn END)* rate ");
            sb.append("ELSE 0 END ), 0) AS July,");
            sb.append("ROUND(SUM(CASE WHEN (MONTH(CheckIn) = 8 OR MONTH(CheckOut) = 8) THEN ");
            sb.append("DATEDIFF(CASE WHEN CheckOut > '2021-08-31' THEN '2021-09-01' ELSE CheckOut END, CASE WHEN CheckIn < '2021-08-01' THEN '2021-07-31' ELSE CheckIn END)* rate ");
            sb.append("ELSE 0 END ), 0) AS August,");
            sb.append("ROUND(SUM(CASE WHEN (MONTH(CheckIn) = 9 OR MONTH(CheckOut) = 9) THEN ");
            sb.append("DATEDIFF(CASE WHEN CheckOut > '2021-09-30' THEN '2021-10-01' ELSE CheckOut END, CASE WHEN CheckIn < '2021-09-01' THEN '2021-08-31' ELSE CheckIn END)* rate ");
            sb.append("ELSE 0 END ), 0) AS September,");
            sb.append("ROUND(SUM(CASE WHEN (MONTH(CheckIn) = 10 OR MONTH(CheckOut) = 10) THEN ");
            sb.append("DATEDIFF(CASE WHEN CheckOut > '2021-10-31' THEN '2021-11-01' ELSE CheckOut END, CASE WHEN CheckIn < '2021-10-01' THEN '2021-09-30' ELSE CheckIn END)* rate ");
            sb.append("ELSE 0 END ), 0) AS October,");
            sb.append("ROUND(SUM(CASE WHEN (MONTH(CheckIn) = 11 OR MONTH(CheckOut) = 11) THEN ");
            sb.append("DATEDIFF(CASE WHEN CheckOut > '2021-11-30' THEN '2021-12-01' ELSE CheckOut END, CASE WHEN CheckIn < '2021-11-01' THEN '2021-10-31' ELSE CheckIn END)* rate ");
            sb.append("ELSE 0 END ), 0) AS November,");
            sb.append("ROUND(SUM(CASE WHEN (MONTH(CheckIn) = 12 OR MONTH(CheckOut) = 12) THEN ");
            sb.append("DATEDIFF(CASE WHEN CheckOut > '2021-12-30' THEN '2022-01-01' ELSE CheckOut END, CASE WHEN CheckIn < '2021-12-01' THEN '2021-11-30' ELSE CheckIn END)* rate ");
            sb.append("ELSE 0 END ), 0) AS December ");
            sb.append("FROM lab7_reservations WHERE YEAR(CheckIn) = YEAR(CURRENT_DATE) AND YEAR(CheckOut) = YEAR(CURRENT_DATE) GROUP BY Room) ");
            sb.append("SELECT * , (January+  February + March + April + May + June + July + August + September + October + November + December)  AS Total ");
            sb.append("FROM eachMonth GROUP BY Room ORDER BY Room;");

            String sql = sb.toString();

            try(PreparedStatement stmt = conn.prepareStatement(sql)){
                try(ResultSet rs = stmt.executeQuery()){
                    System.out.format("|%-10s |%-10s |%-10s |%-10s |%-10s |%-10s |%-10s |%-10s |%-10s |%-10s |%-10s |%-10s |%-10s |%-10s\n", "Room(s)", "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec", "Total");
                    System.out.println("------------------------------------------------------------------------------------------------------------------------------------------------------------------");
                    while(rs.next()){
                        Room = rs.getString("Room");
                        January = rs.getString("January");
                        February = rs.getString("February");
                        March = rs.getString("March");
                        April = rs.getString("April");
                        May = rs.getString("May");
                        June = rs.getString("June");
                        July = rs.getString("July");
                        August = rs.getString("August");
                        September = rs.getString("September");
                        October = rs.getString("October");
                        November = rs.getString("November");
                        December = rs.getString("December");
                        Total = rs.getString("Total");

                        System.out.format("|%-10s |%-10s |%-10s |%-10s |%-10s |%-10s |%-10s |%-10s |%-10s |%-10s |%-10s |%-10s |%-10s |%-10s\n", Room, January, February, March, April, May, June, July, August, September,October,November,December,Total);

                    }
                }
            }
        }
    } 
}   