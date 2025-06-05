import java.sql.*;
import java.util.Scanner;

public class mysqlConnection {

    public static void main(String[] args) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
            System.out.println("Driver definition succeed");
        } catch (Exception ex) {
            System.out.println("Driver definition failed");
        }

        try (Connection conn = DriverManager.getConnection(
                "jdbc:mysql://localhost/test?useSSL=false&serverTimezone=IST", "root", "Aa123456")) {
            System.out.println("SQL connection succeed");

            // 1
            updateArrivalTimeForKU101(conn, "14:30");

            // 2
            updateArrivalTimeForParisBefore15(conn, "14:50");

            // 3 
            updateFlightsManually(conn);

            // 4
            printAllFlights(conn);

        } catch (SQLException ex) {
            System.out.println("SQLException: " + ex.getMessage());
            System.out.println("SQLState: " + ex.getSQLState());
            System.out.println("VendorError: " + ex.getErrorCode());
        }
    }

    // Update arrival time for the flight KU101.
    public static void updateArrivalTimeForKU101(Connection conn, String newArrivalTime) throws SQLException {
        String sql = "UPDATE Flights SET delay = ? WHERE flight = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newArrivalTime);
            ps.setString(2, "KU101");
            int updated = ps.executeUpdate();
            System.out.println("Rows updated for KU101: " + updated);
        }
    }

    // Update arrival time for each flight from Paris which
    // arrives earlier than at 15.00.
    public static void updateArrivalTimeForParisBefore15(Connection conn, String newArrivalTime) throws SQLException {
        String sql = "UPDATE Flights SET delay = ? WHERE `from` = ? AND scheduled < ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newArrivalTime);
            ps.setString(2, "Paris");
            ps.setString(3, "15:00");
            int updated = ps.executeUpdate();
            System.out.println("Rows updated for Paris flights before 15:00: " + updated);
        }
    }

    /*
     *  Give to the dispatcher a possibility to update arrival
     *  time of several flights manually (First download a list
     *  of flights from database, then put necessary changes, and finally update the database using 
     */
    public static void updateFlightsManually(Connection conn) throws SQLException {
        // show all flight
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT flight, delay FROM Flights");

        // collect info
        java.util.List<String> flights = new java.util.ArrayList<>();
        while (rs.next()) {
            String flight = rs.getString("flight");
            String delay = rs.getString("delay");
            System.out.println("Flight: " + flight + ", Current Delay: " + delay);
            flights.add(flight);
        }

        rs.close();

        // user update 
        try (Scanner scanner = new Scanner(System.in)) {
            boolean stopUpdating = false;
            for (String flight : flights) {
                if (stopUpdating)
                    break;

                System.out.print("Enter new arrival time for flight " + flight + " (leave blank to skip, type 'exit' to stop all): ");

                if (!scanner.hasNextLine()) {
                    System.out.println(" No more input. Exiting flight update.");
                    break;
                }

                String newDelay = scanner.nextLine().trim();

                if (newDelay.equalsIgnoreCase("exit")) {
                    System.out.println(" Update cancelled by user.");
                    break;
                }

                if (!newDelay.isEmpty()) {
                    String sql = "UPDATE Flights SET delay = ? WHERE flight = ?";
                    try (PreparedStatement ps = conn.prepareStatement(sql)) {
                        ps.setString(1, newDelay);
                        ps.setString(2, flight);
                        int updated = ps.executeUpdate();
                        System.out.println("Updated flight " + flight + ": " + updated + " row(s)");
                    }
                }
            }
        } catch (Exception e) {
            System.out.println(" Scanner input error: " + e.getMessage());
        }
    }

    // Print all current flight data
    /*
    public static void printAllFlights(Connection conn) throws SQLException {
        String sql = "SELECT scheduled, flight, `from`, delay, terminal FROM Flights";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            System.out.println("\nFinal list of Flights:");
            while (rs.next()) {
                System.out.println("Scheduled: " + rs.getString("scheduled") +
                        ", Flight: " + rs.getString("flight") +
                        ", From: " + rs.getString("from") +
                        ", Delay: " + rs.getString("delay") +
                        ", Terminal: " + rs.getString("terminal"));
            }
        }
    }
    */
 // Print all current flight data to a file
 // Print all current flight data to a file on Desktop
    public static void printAllFlights(Connection conn) throws SQLException {
        String sql = "SELECT scheduled, flight, `from`, delay, terminal FROM Flights";

        // נתיב לקובץ על שולחן העבודה של המשתמש הנוכחי
        String desktopPath = System.getProperty("user.home") + "/Desktop/flights_output.txt";

        System.out.println(">> Starting to write flight data to file at: " + desktopPath);

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql);
             java.io.PrintWriter writer = new java.io.PrintWriter(desktopPath)) {

            writer.println("Final list of Flights:");

            while (rs.next()) {
                writer.println("Scheduled: " + rs.getString("scheduled") +
                        ", Flight: " + rs.getString("flight") +
                        ", From: " + rs.getString("from") +
                        ", Delay: " + rs.getString("delay") +
                        ", Terminal: " + rs.getString("terminal"));
            }

            System.out.println("Flight data successfully written to: " + desktopPath);

        } catch (Exception e) {
            System.out.println(" Error writing to file: " + e.getMessage());
        }
    }
    
    
}
