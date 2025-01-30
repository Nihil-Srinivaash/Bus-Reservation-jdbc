import java.sql.*;
import java.util.Scanner;

public class BusReservationSystem {
    private static Connection connection = DatabaseConnection.getConnection();

    public static void main(String[] args) {
        if (connection == null) {
            System.out.println("Database connection failed. Exiting.");
            return;
        }

        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("\n--- Bus Reservation System ---");
            System.out.println("1. View All Buses");
            System.out.println("2. Add New Bus");
            System.out.println("3. Update Bus Details");
            System.out.println("4. Delete Bus");
            System.out.println("5. Book a Seat");
            System.out.println("6. Cancel a Reservation");
						System.out.println("7. View Passenger Details");
            System.out.println("8. Exit");
            System.out.print("Choose an option: ");
            int choice = scanner.nextInt();

            switch (choice) {
                case 1:
                    viewAllBuses();
                    break;
                case 2:
                    addNewBus(scanner);
                    break;
                case 3:
                    updateBus(scanner);
                    break;
                case 4:
                    deleteBus(scanner);
                    break;
                case 5:
                    bookSeat(scanner);
                    break;
                case 6:
                    cancelReservation(scanner);
                    break;
                		case 7:
    viewPassengerDetails();
    break;	
				case 8:
                    System.out.println("Thank you for using the Bus Reservation System.");
                    System.exit(0);
			

                default:
                    System.out.println("Invalid choice. Try again.");
            }
        }
    }

    private static void viewAllBuses() {
    String query = "SELECT * FROM buses";
    try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
        System.out.println("\nAvailable Buses:");
        System.out.printf("%-10s | %-20s | %-12s | %-16s%n", "Bus ID", "Bus Name", "Total Seats", "Available Seats");
        System.out.println("-------------------------------------------------------------");
        
        while (rs.next()) {
            System.out.printf("%-10d | %-20s | %-12d | %-16d%n",
                    rs.getInt("bus_id"),
                    rs.getString("bus_name"),
                    rs.getInt("total_seats"),
                    rs.getInt("available_seats"));
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
}


    private static void addNewBus(Scanner scanner) {
    System.out.print("Enter Bus Name: ");
    scanner.nextLine(); // Consume newline
    String busName = scanner.nextLine();
    System.out.print("Enter Total Seats: ");
    int totalSeats = scanner.nextInt();

    String query = "INSERT INTO buses (bus_name, total_seats, available_seats) VALUES (?, ?, ?)";
    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
        pstmt.setString(1, busName);
        pstmt.setInt(2, totalSeats);
        pstmt.setInt(3, totalSeats);
        pstmt.executeUpdate();
        System.out.println("New bus added successfully.");
    } catch (SQLException e) {
        e.printStackTrace();
    }
}

    private static void updateBus(Scanner scanner) {
        System.out.print("Enter Bus ID to Update: ");
        int busId = scanner.nextInt();
        System.out.print("Enter New Bus Name: ");
        scanner.nextLine(); // Consume newline
        String newBusName = scanner.nextLine();
        System.out.print("Enter New Total Seats: ");
        int newTotalSeats = scanner.nextInt();

        String query = "UPDATE buses SET bus_name = ?, total_seats = ?, available_seats = ? WHERE bus_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, newBusName);
            pstmt.setInt(2, newTotalSeats);
            pstmt.setInt(3, newTotalSeats);
            pstmt.setInt(4, busId);
            pstmt.executeUpdate();
            System.out.println("Bus details updated successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void deleteBus(Scanner scanner) {
        System.out.print("Enter Bus ID to Delete: ");
        int busId = scanner.nextInt();

        String query = "DELETE FROM buses WHERE bus_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, busId);
            pstmt.executeUpdate();
            System.out.println("Bus deleted successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void bookSeat(Scanner scanner) {
        System.out.print("Enter Bus ID: ");
        int busId = scanner.nextInt();
        System.out.print("Enter Your Name: ");
        scanner.nextLine(); // Consume newline
        String passengerName = scanner.nextLine();
        System.out.print("Enter Number of Seats to Book: ");
        int seatsToBook = scanner.nextInt();

        String checkSeatsQuery = "SELECT available_seats FROM buses WHERE bus_id = ?";
        String updateSeatsQuery = "UPDATE buses SET available_seats = available_seats - ? WHERE bus_id = ?";
        String insertReservationQuery = "INSERT INTO reservations (bus_id, passenger_name, seats_booked) VALUES (?, ?, ?)";

        try (PreparedStatement checkStmt = connection.prepareStatement(checkSeatsQuery);
             PreparedStatement updateStmt = connection.prepareStatement(updateSeatsQuery);
             PreparedStatement insertStmt = connection.prepareStatement(insertReservationQuery)) {

            checkStmt.setInt(1, busId);
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next() && rs.getInt("available_seats") >= seatsToBook) {
                updateStmt.setInt(1, seatsToBook);
                updateStmt.setInt(2, busId);
                updateStmt.executeUpdate();

                insertStmt.setInt(1, busId);
                insertStmt.setString(2, passengerName);
                insertStmt.setInt(3, seatsToBook);
                insertStmt.executeUpdate();

                System.out.println("Reservation successful!");
            } else {
                System.out.println("Insufficient seats available.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
	private static void viewPassengerDetails() {
    String query = """
        SELECT r.passenger_name, r.seats_booked, b.bus_id, b.bus_name
        FROM reservations r
        JOIN buses b ON r.bus_id = b.bus_id
        ORDER BY b.bus_id, r.passenger_name
    """;

    try (Statement stmt = connection.createStatement();
         ResultSet rs = stmt.executeQuery(query)) {

        System.out.println("\nPassenger Booking Details:");
        System.out.println("Passenger Name   | Seats Booked | Bus ID | Bus Name");
        System.out.println("---------------------------------------------------");

        while (rs.next()) {
            String passengerName = rs.getString("passenger_name");
            int seatsBooked = rs.getInt("seats_booked");
            int busId = rs.getInt("bus_id");
            String busName = rs.getString("bus_name");

            System.out.printf("%-17s | %-15d | %-10d | %s%n", 
                              passengerName, seatsBooked, busId, busName);
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
}

	

    private static void cancelReservation(Scanner scanner) {
        System.out.print("Enter Reservation ID to Cancel: ");
        int reservationId = scanner.nextInt();

        String getReservationQuery = "SELECT bus_id, seats_booked FROM reservations WHERE reservation_id = ?";
        String deleteReservationQuery = "DELETE FROM reservations WHERE reservation_id = ?";
        String updateSeatsQuery = "UPDATE buses SET available_seats = available_seats + ? WHERE bus_id = ?";

        try (PreparedStatement getStmt = connection.prepareStatement(getReservationQuery);
             PreparedStatement deleteStmt = connection.prepareStatement(deleteReservationQuery);
             PreparedStatement updateStmt = connection.prepareStatement(updateSeatsQuery)) {

            getStmt.setInt(1, reservationId);
            ResultSet rs = getStmt.executeQuery();

            if (rs.next()) {
                int busId = rs.getInt("bus_id");
                int seatsBooked = rs.getInt("seats_booked");

                deleteStmt.setInt(1, reservationId);
                deleteStmt.executeUpdate();

                updateStmt.setInt(1, seatsBooked);
                updateStmt.setInt(2, busId);
                updateStmt.executeUpdate();

                System.out.println("Reservation canceled successfully.");
            } else {
                System.out.println("Reservation ID not found.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}


//compile-->javac -cp .;mysql.jar BusReservationSystem.java
//run-->java -cp .;mysql.jar BusReservationSystem