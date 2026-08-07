import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.Scanner;


public class med_aid_track {

 
    private static final String DB_URL = "jdbc:mysql://localhost:3306/medical_aid_tracker";
    private static final String DB_USERNAME = System.getenv().getOrDefault("DB_USERNAME", "");
    private static final String DB_PASSWORD = System.getenv().getOrDefault("DB_PASSWORD", "");


    private static String readValidContactNumber(Scanner scanner, String prompt) {
    while (true) {
        System.out.print(prompt);
        String input = scanner.nextLine().trim();
        
        if (input.matches("^(\\+)?\\d+$")) {
            return input;
        } else {
            System.out.println("Invalid contact number! Please enter a valid contact number (only digits and an optional '+' at the beginning).");
        }
    }
    }

    private static int readIntInput(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine();
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Invalid input! Please enter a valid number.");
            }
        }
    }

  
    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC Driver not found.");
            e.printStackTrace();
        }
        return DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);
    }

   
    public static void testConnection() {
        try (Connection conn = getConnection()) {
            if (conn != null && !conn.isClosed()) {
                System.out.println("Database connected successfully!");
            } else {
                System.out.println("Failed to connect to the database.");
            }
        } catch (SQLException e) {
            System.err.println("Database connection failed: " + e.getMessage());
        }
    }

    
    public static void printHeader(String header) {
        System.out.println();
        System.out.println("========== " + header + " ==========");
    }

   
    public static void printSeparator() {
        System.out.println("---------------------------------------------------------------------");
    }

    
     // Searches for emergency doctors with available beds.
     
    public static void searchEmergencyDoctorsWithBeds() {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter required bed type (ICU/General): ");
        String bedType = scanner.nextLine().trim();

        String hospitalQuery = "SELECT h.hospital_id, h.hospital_name, h.address, " +
                               " (SELECT COUNT(*) FROM beds b WHERE b.hospital_id = h.hospital_id " +
                               "   AND b.status = 'Available' AND b.ward_type = ?) AS available_beds " +
                               "FROM hospitals h " +
                               "WHERE (SELECT COUNT(*) FROM beds b WHERE b.hospital_id = h.hospital_id " +
                               "       AND b.status = 'Available' AND b.ward_type = ?) > 0 " +
                               "  AND h.hospital_id IN (SELECT d.hospital_id FROM doctors d WHERE d.availability_status = 'Available')";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(hospitalQuery)) {

             ps.setString(1, bedType);
             ps.setString(2, bedType);
             ResultSet rs = ps.executeQuery();

             boolean hospitalFound = false;
             printHeader("Hospitals with available " + bedType + " beds and available doctors");
             while (rs.next()) {
                 hospitalFound = true;
                 int hospitalId = rs.getInt("hospital_id");
                 String hospitalName = rs.getString("hospital_name");
                 String address = rs.getString("address");
                 int availableBeds = rs.getInt("available_beds");

                 System.out.printf("Hospital ID: %-4d | Name: %-30s | Available %s Beds: %-3d%n", 
                                   hospitalId, hospitalName, bedType, availableBeds);
                 System.out.printf("Address: %s%n", address);
                 printSeparator();

                 // Query for available doctors in this hospital.
                 String doctorQuery = "SELECT doctor_id, name, availability_status, from_time, to_time FROM doctors " +
                                      "WHERE hospital_id = ? AND availability_status = 'Available'";
                 try (PreparedStatement psDoctor = conn.prepareStatement(doctorQuery)) {
                     psDoctor.setInt(1, hospitalId);
                     ResultSet rsDoctor = psDoctor.executeQuery();
                     System.out.println("Available Doctors:");
                     boolean doctorFound = false;
                     System.out.printf("  %-10s %-25s %-15s %-10s %-10s%n", "Doctor ID", "Name", "Availability", "From", "To");
                     printSeparator();
                     while (rsDoctor.next()) {
                         doctorFound = true;
                         int doctorId = rsDoctor.getInt("doctor_id");
                         String doctorName = rsDoctor.getString("name");
                         String availability = rsDoctor.getString("availability_status");
                         Time fromTime = rsDoctor.getTime("from_time");
                         Time toTime = rsDoctor.getTime("to_time");
                         System.out.printf("  %-10d %-25s %-15s %-10s %-10s%n", doctorId, doctorName, availability, fromTime, toTime);
                     }
                     if (!doctorFound) {
                         System.out.println("  No available doctors found in this hospital.");
                     }
                     System.out.println();
                 }
             }
             if (!hospitalFound) {
                 System.out.println("No hospitals found with available " + bedType + " beds and available doctors.");
             }
        } catch (SQLException e) {
             System.err.println("Error searching emergency doctors with beds: " + e.getMessage());
        }
    }

    
     // Searches for doctors by specialization.
     
    public static void searchDoctorsBySpecialization() {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter the desired specialization (e.g., Cardiologist, Neurologist): ");
        String specialization = scanner.nextLine().trim();
        System.out.print("Do you want to check available bed numbers? (Yes/No): ");
        String checkBeds = scanner.nextLine().trim();

        if (checkBeds.equalsIgnoreCase("No")) {
            String query = "SELECT d.doctor_id, d.name AS doctor_name, d.availability_status, d.from_time, d.to_time, " +
                           "h.hospital_id, h.hospital_name, h.address " +
                           "FROM doctors d JOIN hospitals h ON d.hospital_id = h.hospital_id " +
                           "WHERE d.specialization = ?";
            try (Connection conn = getConnection();
                 PreparedStatement ps = conn.prepareStatement(query)) {
                ps.setString(1, specialization);
                ResultSet rs = ps.executeQuery();
                printHeader("Doctors with specialization '" + specialization + "'");
                System.out.printf("%-10s %-25s %-15s %-10s %-10s %-12s %-30s %s%n", 
                                  "Doctor ID", "Name", "Availability", "From", "To", "Hosp. ID", "Hospital", "Address");
                printSeparator();
                boolean hasResults = false;
                while (rs.next()) {
                    hasResults = true;
                    int doctorId = rs.getInt("doctor_id");
                    String doctorName = rs.getString("doctor_name");
                    String availability = rs.getString("availability_status");
                    Time fromTime = rs.getTime("from_time");
                    Time toTime = rs.getTime("to_time");
                    int hospitalId = rs.getInt("hospital_id");
                    String hospitalName = rs.getString("hospital_name");
                    String address = rs.getString("address");
                    System.out.printf("%-10d %-25s %-15s %-10s %-10s %-12d %-30s %s%n", 
                                      doctorId, doctorName, availability, fromTime, toTime, hospitalId, hospitalName, address);
                }
                if (!hasResults) {
                    System.out.println("No doctors found with specialization " + specialization + ".");
                }
            } catch (SQLException e) {
                System.err.println("Error searching for doctors: " + e.getMessage());
            }
        } else if (checkBeds.equalsIgnoreCase("Yes")) {
            System.out.print("Which ward's bed count do you want? (ICU/General): ");
            String wardType = scanner.nextLine().trim();

            String subquery = wardType.equalsIgnoreCase("ICU")
                ? "(SELECT COUNT(*) FROM beds b WHERE b.hospital_id = h.hospital_id AND b.status = 'Available' AND b.ward_type = 'ICU')"
                : "(SELECT COUNT(*) FROM beds b WHERE b.hospital_id = h.hospital_id AND b.status = 'Available' AND b.ward_type = 'General')";

            String query = "SELECT d.doctor_id, d.name AS doctor_name, d.availability_status, d.from_time, d.to_time, " +
                           "h.hospital_id, h.hospital_name, h.address, " + subquery + " AS available_beds " +
                           "FROM doctors d JOIN hospitals h ON d.hospital_id = h.hospital_id " +
                           "WHERE d.specialization = ? AND d.availability_status = 'Available' " +
                           "AND " + subquery + " > 0";
            try (Connection conn = getConnection();
                 PreparedStatement ps = conn.prepareStatement(query)) {
                ps.setString(1, specialization);
                ResultSet rs = ps.executeQuery();
                printHeader("Doctors with specialization '" + specialization + "' and available " + wardType + " beds");
                System.out.printf("%-10s %-25s %-15s %-10s %-10s %-12s %-30s %-20s %-15s%n", 
                                  "Doctor ID", "Name", "Availability", "From", "To", "Hosp. ID", "Hospital", "Address", "Avail. Beds");
                printSeparator();
                boolean hasResults = false;
                while (rs.next()) {
                    hasResults = true;
                    int doctorId = rs.getInt("doctor_id");
                    String doctorName = rs.getString("doctor_name");
                    String availability = rs.getString("availability_status");
                    Time fromTime = rs.getTime("from_time");
                    Time toTime = rs.getTime("to_time");
                    int hospitalId = rs.getInt("hospital_id");
                    String hospitalName = rs.getString("hospital_name");
                    String address = rs.getString("address");
                    int availableBeds = rs.getInt("available_beds");
                    System.out.printf("%-10d %-25s %-15s %-10s %-10s %-12d %-30s %-20s %-15d%n", 
                                      doctorId, doctorName, availability, fromTime, toTime, hospitalId, hospitalName, address, availableBeds);
                }
                if (!hasResults) {
                    System.out.println("No doctors found with specialization " + specialization + " and available " + wardType + " beds.");
                }
            } catch (SQLException e) {
                System.err.println("Error searching for doctors with bed availability: " + e.getMessage());
            }
        } else {
            System.out.println("Invalid input. Please answer Yes or No.");
        }
    }

    
    //   Searches for available ambulance services.
  
public static void searchAmbulanceServices() {
   
    String query = "SELECT a.id, a.driver, " +
                   "       (SELECT ap1.phone FROM ambulance_phone ap1 WHERE ap1.id = a.id LIMIT 1) AS phone1, " +
                   "       (SELECT ap2.phone FROM ambulance_phone ap2 WHERE ap2.id = a.id LIMIT 1 OFFSET 1) AS phone2, " +
                   "       a.location, a.status " +
                   "FROM ambulance a " +
                   "WHERE a.status = 'Available'";
    try (Connection conn = getConnection();
         Statement stmt = conn.createStatement();
         ResultSet rs = stmt.executeQuery(query)) {

         printHeader("Available Ambulance Services");
         System.out.printf("%-10s %-20s %-20s %-20s %-30s %-15s%n",
                           "Ambulance ID", "Driver", "Phone1", "Phone2", "Location", "Status");
         printSeparator();
         boolean hasResults = false;
         while (rs.next()) {
             hasResults = true;
             String phone1 = rs.getString("phone1");
             String phone2 = rs.getString("phone2");
             System.out.printf("%-10d %-20s %-20s %-20s %-30s %-15s%n",
                               rs.getInt("id"),
                               rs.getString("driver"),
                               phone1 != null ? phone1 : "No Phone",
                               phone2 != null ? phone2 : "No Phone",
                               rs.getString("location"),
                               rs.getString("status"));
         }
         if (!hasResults) {
             System.out.println("No available ambulance services found.");
         }
    } catch (SQLException e) {
         System.err.println("Error retrieving ambulance services: " + e.getMessage());
    }
}


    
     // Books a doctor appointment.
     
    public static void bookAppointment() {
        Scanner scanner = new Scanner(System.in);
        int patientId = readIntInput(scanner, "Enter Patient ID: ");
        if (!isPatientRegistered(patientId)) {
            System.out.println("Error: Patient with ID " + patientId + " is not registered.");
            return;
        }

        int doctorId = readIntInput(scanner, "Enter Doctor ID: ");
        int hospitalId = readIntInput(scanner, "Enter Hospital ID: ");
        
        System.out.print("Enter Appointment Date (YYYY-MM-DD): ");
        String dateStr = scanner.next();

        try {
            LocalDate appointmentDate = LocalDate.parse(dateStr);
            if (appointmentDate.isBefore(LocalDate.now())) {
                System.out.println("Error: Cannot book appointments in the past.");
                return;
            }

            try (Connection conn = getConnection()) {
                String doctorCheck = "SELECT availability_status, from_time, to_time FROM doctors " +
                                     "WHERE doctor_id = ? AND hospital_id = ?";
                LocalTime doctorStart, doctorEnd;
                String availability;
                try (PreparedStatement ps = conn.prepareStatement(doctorCheck)) {
                    ps.setInt(1, doctorId);
                    ps.setInt(2, hospitalId);
                    ResultSet rs = ps.executeQuery();

                    if (!rs.next()) {
                        System.out.println("Error: Doctor with ID " + doctorId + " not found in hospital " + hospitalId + ".");
                        return;
                    }

                    availability = rs.getString("availability_status");
                    if (!"Available".equalsIgnoreCase(availability)) {
                        System.out.println("Error: Doctor with ID " + doctorId + " is not available for appointments.");
                        return;
                    }
                    doctorStart = rs.getTime("from_time").toLocalTime();
                    doctorEnd = rs.getTime("to_time").toLocalTime();
                }

                LocalTime appointmentTime = doctorStart;
                boolean slotFound = false;
                while (!appointmentTime.plusHours(1).isAfter(doctorEnd)) {
                    if (appointmentDate.equals(LocalDate.now()) && appointmentTime.isBefore(LocalTime.now())) {
                        appointmentTime = appointmentTime.plusHours(1);
                        continue;
                    }

                    String conflictCheck = "SELECT id FROM appointments " +
                                           "WHERE doctor_id = ? AND date = ? AND time = ?";
                    try (PreparedStatement ps = conn.prepareStatement(conflictCheck)) {
                        ps.setInt(1, doctorId);
                        ps.setDate(2, Date.valueOf(appointmentDate));
                        ps.setTime(3, Time.valueOf(appointmentTime));
                        ResultSet rs = ps.executeQuery();

                        if (!rs.next()) {
                            slotFound = true;
                            break;
                        }
                    }
                    appointmentTime = appointmentTime.plusHours(1);
                }

                if (!slotFound) {
                    System.out.println("Error: No available appointment slot on " + appointmentDate +
                                       " within doctor's working hours (" + doctorStart + " to " + doctorEnd + ").");
                    return;
                }

                String insert = "INSERT INTO appointments (patient, doctor_id, hospital, date, time, status) " +
                                "VALUES (?, ?, ?, ?, ?, 'Booked')";
                try (PreparedStatement ps = conn.prepareStatement(insert, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, patientId);
                    ps.setInt(2, doctorId);
                    ps.setInt(3, hospitalId);
                    ps.setDate(4, Date.valueOf(appointmentDate));
                    ps.setTime(5, Time.valueOf(appointmentTime));

                    int rows = ps.executeUpdate();
                    if (rows > 0) {
                        try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                            if (generatedKeys.next()) {
                                int appointmentId = generatedKeys.getInt(1);
                                LocalTime appointmentEnd = appointmentTime.plusHours(1);
                                System.out.println("Appointment booked successfully!");
                                System.out.println("Appointment ID: " + appointmentId);
                                System.out.println("Date: " + appointmentDate);
                                System.out.println("Time: " + appointmentTime + " - " + appointmentEnd);
                            } else {
                                System.out.println("Appointment booked, but appointment ID could not be retrieved.");
                            }
                        }
                    } else {
                        System.out.println("Failed to book appointment.");
                    }
                }
            }
        } catch (DateTimeParseException e) {
            System.err.println("Invalid date format. Please use YYYY-MM-DD.");
        } catch (SQLException e) {
            System.err.println("Database error during appointment booking: " + e.getMessage());
        }
    }

    
    //   Checks if a patient with the given ID is registered in the system.
     
    public static boolean isPatientRegistered(int patientId) {
        String query = "SELECT patient_id FROM patient WHERE patient_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, patientId);
            ResultSet rs = ps.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            System.err.println("Error checking patient registration: " + e.getMessage());
            return false;
        }
    }

    
    //   Checks if the blood group is valid.
     
    private static boolean isValidBloodGroup(String bloodGroup) {
        
        String[] validBloodGroups = {"A+", "A-", "B+", "B-", "O+", "O-", "AB+", "AB-"};
        for (String valid : validBloodGroups) {
            if (valid.equalsIgnoreCase(bloodGroup.trim())) {
                return true;
            }
        }
        return false;
    }

    
    //   Adds a new patient.
    
    public static void addNewPatient() {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter Patient Name: ");
        String name = scanner.nextLine();
        System.out.print("Enter Date of Birth (YYYY-MM-DD): ");
        String dobStr = scanner.nextLine();
        System.out.print("Enter Contact Number: ");
        String contactNumber = readValidContactNumber(scanner, " (only digits and optional '+'): ");
        System.out.print("Enter Blood Group (e.g., A+, B-, O+, AB+): ");
        String bloodGroup = scanner.nextLine();

     
        LocalDate dob;
        try {
            dob = LocalDate.parse(dobStr);
        } catch (DateTimeParseException e) {
            System.err.println("Invalid date format for Date of Birth. Please use YYYY-MM-DD.");
            return;
        }
        
       
        if (dob.isAfter(LocalDate.now())) {
            System.err.println("Invalid Date of Birth. Date of Birth cannot be in the future.");
            return;
        }
        
   
        if (!isValidBloodGroup(bloodGroup)) {
            System.err.println("Invalid blood group entered. Please enter one of: A+, A-, B+, B-, O+, O-, AB+, AB-.");
            return;
        }

        String insert = "INSERT INTO patient (name, dob, contact_number, blood_group) VALUES (?, ?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(insert, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, name);
            ps.setDate(2, Date.valueOf(dob));
            ps.setString(3, contactNumber);
            ps.setString(4, bloodGroup.trim().toUpperCase()); 

            int rows = ps.executeUpdate();
            if (rows > 0) {
                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int patientIdGenerated = generatedKeys.getInt(1);
                        System.out.println("Patient added successfully with ID: " + patientIdGenerated);
                    } else {
                        System.out.println("Patient added, but generated ID could not be retrieved.");
                    }
                }
            } else {
                System.out.println("Failed to add patient.");
            }
        } catch (SQLException e) {
            System.err.println("Error adding patient: " + e.getMessage());
        }
    }

    
    //   Updates the availability status of a doctor.
     
    public static void updateDoctorAvailabilityStatus() {
        Scanner scanner = new Scanner(System.in);
        int doctorId = readIntInput(scanner, "Enter Doctor ID: ");
        System.out.print("Enter new availability status (Available/Unavailable): ");
        String newStatus = scanner.nextLine().trim();

        if (!newStatus.equalsIgnoreCase("Available") && !newStatus.equalsIgnoreCase("Unavailable")) {
            System.out.println("Invalid status. Please enter 'Available' or 'Unavailable'.");
            return;
        }

        if (!isDoctorRegistered(doctorId)) {
            System.out.println("Error: No doctor found with ID " + doctorId + ".");
            return;
        }

        String updateQuery = "UPDATE doctors SET availability_status = ? WHERE doctor_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(updateQuery)) {
            ps.setString(1, newStatus);
            ps.setInt(2, doctorId);

            int rows = ps.executeUpdate();
            if (rows > 0) {
                System.out.println("Doctor availability updated successfully!");
            } else {
                System.out.println("Update failed. Please check the doctor ID and try again.");
            }
        } catch (SQLException e) {
            System.err.println("Database error while updating doctor status: " + e.getMessage());
        }
    }

    
    //  Updates the availability status of a bed by its bed ID.
    
    public static void updateBedAvailabilityStatus() {
        Scanner scanner = new Scanner(System.in);
        int bedId = readIntInput(scanner, "Enter Bed ID: ");
        System.out.print("Enter new status for the bed (Available/Occupied): ");
        String newStatus = scanner.nextLine().trim();

        if (!newStatus.equalsIgnoreCase("Available") && !newStatus.equalsIgnoreCase("Occupied")) {
            System.out.println("Invalid status. Please enter 'Available' or 'Occupied'.");
            return;
        }

        String checkQuery = "SELECT bed_id FROM beds WHERE bed_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement checkPs = conn.prepareStatement(checkQuery)) {
            checkPs.setInt(1, bedId);
            ResultSet rs = checkPs.executeQuery();
            if (!rs.next()) {
                System.out.println("No bed found with ID " + bedId + ".");
                return;
            }
        } catch (SQLException e) {
            System.err.println("Database error while checking bed: " + e.getMessage());
            return;
        }

        String updateQuery = "UPDATE beds SET status = ? WHERE bed_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(updateQuery)) {
            ps.setString(1, newStatus);
            ps.setInt(2, bedId);
            int rows = ps.executeUpdate();
            if (rows > 0) {
                System.out.println("Bed status updated successfully for Bed ID " + bedId + ".");
            } else {
                System.out.println("Failed to update bed status for Bed ID " + bedId + ".");
            }
        } catch (SQLException e) {
            System.err.println("Database error while updating bed status: " + e.getMessage());
        }
    }

    
    //   Cancels all appointments for a given doctor ID.
     
    public static void cancelAppointmentsByDoctorId() {
        Scanner scanner = new Scanner(System.in);
        int doctorId = readIntInput(scanner, "Enter Doctor ID for which all appointments should be cancelled: ");

        String deleteQuery = "DELETE FROM appointments WHERE doctor_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(deleteQuery)) {
            ps.setInt(1, doctorId);
            int count = ps.executeUpdate();
            if (count > 0) {
                System.out.println("Cancelled " + count + " appointment(s) for Doctor ID " + doctorId + ".");
            } else {
                System.out.println("No appointments found for Doctor ID " + doctorId + ".");
            }
        } catch (SQLException e) {
            System.err.println("Error cancelling appointments: " + e.getMessage());
        }
    }

    
     // Checks if a doctor with the given ID exists.
     
    public static boolean isDoctorRegistered(int doctorId) {
        String query = "SELECT doctor_id FROM doctors WHERE doctor_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, doctorId);
            ResultSet rs = ps.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            System.err.println("Error checking doctor registration: " + e.getMessage());
            return false;
        }
    }

    
     // Shows appointment details for a given patient.
    
    public static void showAppointmentDetailsByPatient() {
        Scanner scanner = new Scanner(System.in);
        int patientId = readIntInput(scanner, "Enter Patient ID: ");
        String query = "SELECT id, patient, doctor_id, hospital, date, time, status FROM appointments WHERE patient = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, patientId);
            ResultSet rs = ps.executeQuery();
            printHeader("Appointments for Patient ID " + patientId);
            System.out.printf("%-10s %-10s %-10s %-10s %-15s %-10s %-10s%n", 
                              "App.ID", "Patient", "DoctorID", "Hosp.ID", "Date", "Time", "Status");
            printSeparator();
            boolean hasResults = false;
            while (rs.next()) {
                hasResults = true;
                System.out.printf("%-10d %-10d %-10d %-10d %-15s %-10s %-10s%n",
                                  rs.getInt("id"), rs.getInt("patient"), rs.getInt("doctor_id"),
                                  rs.getInt("hospital"), rs.getDate("date"), rs.getTime("time"),
                                  rs.getString("status"));
            }
            if (!hasResults) {
                System.out.println("No appointments found for Patient ID " + patientId + ".");
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving appointments: " + e.getMessage());
        }
    }

    /**
     * Shows appointment details by Patient ID or Doctor ID.
     * (In Patient Login, only Patient ID option will be offered.)
     */
    public static void showAppointmentDetails() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("View appointments by:");
        System.out.println("1. Patient ID");
        System.out.println("2. Doctor ID");
        int option = readIntInput(scanner, "Enter your choice (1 or 2): ");
        
        String query = "";
        if (option == 1) {
            int patientId = readIntInput(scanner, "Enter Patient ID: ");
            query = "SELECT id, patient, doctor_id, hospital, date, time, status FROM appointments WHERE patient = ?";
            try (Connection conn = getConnection();
                 PreparedStatement ps = conn.prepareStatement(query)) {
                ps.setInt(1, patientId);
                ResultSet rs = ps.executeQuery();
                printHeader("Appointments for Patient ID " + patientId);
                System.out.printf("%-10s %-10s %-12s %-10s %-15s %-10s %-10s%n",
                                  "App.ID", "Patient", "DoctorID", "Hosp.ID", "Date", "Time", "Status");
                printSeparator();
                boolean hasResults = false;
                while (rs.next()) {
                    hasResults = true;
                    int appointmentId = rs.getInt("id");
                    int patient = rs.getInt("patient");
                    int doctorId = rs.getInt("doctor_id");
                    int hospital = rs.getInt("hospital");
                    Date date = rs.getDate("date");
                    Time time = rs.getTime("time");
                    String status = rs.getString("status");
                    System.out.printf("%-10d %-10d %-12d %-10d %-15s %-10s %-10s%n", 
                                      appointmentId, patient, doctorId, hospital, date, time, status);
                }
                if (!hasResults) {
                    System.out.println("No appointments found for Patient ID " + patientId + ".");
                }
            } catch (SQLException e) {
                System.err.println("Error retrieving appointments: " + e.getMessage());
            }
        } else if (option == 2) {
            int doctorId = readIntInput(scanner, "Enter Doctor ID: ");
            query = "SELECT id, patient, doctor_id, hospital, date, time, status FROM appointments WHERE doctor_id = ?";
            try (Connection conn = getConnection();
                 PreparedStatement ps = conn.prepareStatement(query)) {
                ps.setInt(1, doctorId);
                ResultSet rs = ps.executeQuery();
                printHeader("Appointments for Doctor ID " + doctorId);
                System.out.printf("%-10s %-10s %-12s %-10s %-15s %-10s %-10s%n",
                                  "App.ID", "Patient", "DoctorID", "Hosp.ID", "Date", "Time", "Status");
                printSeparator();
                boolean hasResults = false;
                while (rs.next()) {
                    hasResults = true;
                    int appointmentId = rs.getInt("id");
                    int patient = rs.getInt("patient");
                    int docId = rs.getInt("doctor_id");
                    int hospital = rs.getInt("hospital");
                    Date date = rs.getDate("date");
                    Time time = rs.getTime("time");
                    String status = rs.getString("status");
                    System.out.printf("%-10d %-10d %-12d %-10d %-15s %-10s %-10s%n", 
                                      appointmentId, patient, docId, hospital, date, time, status);
                }
                if (!hasResults) {
                    System.out.println("No appointments found for Doctor ID " + doctorId + ".");
                }
            } catch (SQLException e) {
                System.err.println("Error retrieving appointments: " + e.getMessage());
            }
        } else {
            System.out.println("Invalid choice. Please enter 1 or 2.");
        }
    }
    
    public static void deletePatientRecord() {
        Scanner scanner = new Scanner(System.in);
        int patientId = readIntInput(scanner, "Enter Patient ID to delete: ");

        if (!isPatientRegistered(patientId)) {
            System.out.println("Error: Patient with ID " + patientId + " does not exist.");
            return;
        }

        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);

            String deleteAppointments = "DELETE FROM appointments WHERE patient = ?";
            try (PreparedStatement ps1 = conn.prepareStatement(deleteAppointments)) {
                ps1.setInt(1, patientId);
                int appDeleted = ps1.executeUpdate();
                System.out.println("Deleted " + appDeleted + " appointment(s) for Patient ID " + patientId + ".");
            }

            String deletePatient = "DELETE FROM patient WHERE patient_id = ?";
            try (PreparedStatement ps2 = conn.prepareStatement(deletePatient)) {
                ps2.setInt(1, patientId);
                int patientDeleted = ps2.executeUpdate();
                if (patientDeleted > 0) {
                    System.out.println("Patient record with ID " + patientId + " deleted successfully.");
                } else {
                    System.out.println("Failed to delete patient record with ID " + patientId + ".");
                }
            }

            conn.commit();
        } catch (SQLException e) {
            System.err.println("Error deleting patient record: " + e.getMessage());
        }
    }

    
    //   Cancels an appointment by its appointment ID.
    
    public static void cancelAppointmentById() {
        Scanner scanner = new Scanner(System.in);
        int appointmentId = readIntInput(scanner, "Enter Appointment ID to cancel: ");

        try (Connection conn = getConnection()) {
            String deleteQuery = "DELETE FROM appointments WHERE id = ?";
            try (PreparedStatement ps = conn.prepareStatement(deleteQuery)) {
                ps.setInt(1, appointmentId);
                int count = ps.executeUpdate();
                if (count > 0) {
                    System.out.println("Appointment ID " + appointmentId + " has been cancelled and deleted.");
                } else {
                    System.out.println("No appointment found with ID " + appointmentId + ".");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error cancelling appointment: " + e.getMessage());
        }
    }

    
    //  Shows patient details by Patient ID.
     
    public static void showPatientDetails() {
        Scanner scanner = new Scanner(System.in);
        int patientId = readIntInput(scanner, "Enter Patient ID: ");
        String query = "SELECT patient_id, name, dob, contact_number, blood_group FROM patient WHERE patient_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, patientId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                printHeader("Patient Details for ID " + patientId);
                System.out.printf("Patient ID: %d%n", rs.getInt("patient_id"));
                System.out.printf("Name: %s%n", rs.getString("name"));
                System.out.printf("Date of Birth: %s%n", rs.getDate("dob"));
                System.out.printf("Contact Number: %s%n", rs.getString("contact_number"));
                System.out.printf("Blood Group: %s%n", rs.getString("blood_group"));
                printSeparator();
            } else {
                System.out.println("No patient found with ID " + patientId + ".");
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving patient details: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        testConnection();
        Scanner scanner = new Scanner(System.in);
        int loginChoice = 0;

        do {
            System.out.println("\n===== Login Menu =====");
            System.out.println("1. Patient Login");
            System.out.println("2. Medical Representative Login");
            System.out.println("3. Exit Application");
            loginChoice = readIntInput(scanner, "Enter your choice: ");

            if (loginChoice == 1) {
                int choice;
                do {
                    System.out.println("\n===== Patient Menu =====");
                    System.out.println("0. Go Back to Login Menu");
                    System.out.println("1. Search Doctors by Specialization");
                    System.out.println("2. Search Emergency Doctors with Available Beds");
                    System.out.println("3. Search for Available Ambulance Services");
                    System.out.println("4. Book a Doctor Appointment");
                    System.out.println("5. Add a New Patient");
                    System.out.println("6. Show Appointment Details");
                    System.out.println("7. Delete Patient Record (and its Appointments)");
                    System.out.println("8. Cancel Appointment by Appointment ID");
                    System.out.println("9. Show Patient Details");
                    System.out.println("10. Exit Application");
                    choice = readIntInput(scanner, "Enter your choice: ");

                    switch (choice) {
                        case 0: break;
                        case 1: searchDoctorsBySpecialization(); break;
                        case 2: searchEmergencyDoctorsWithBeds(); break;
                        case 3: searchAmbulanceServices(); break;
                        case 4: bookAppointment(); break;
                        case 5: addNewPatient(); break;
                        case 6: showAppointmentDetailsByPatient(); break;
                        case 7: deletePatientRecord(); break;
                        case 8: cancelAppointmentById(); break;
                        case 9: showPatientDetails(); break;
                        case 10: System.exit(0);
                        default: System.out.println("Invalid choice.");
                    }
                } while (choice != 0);

            } else if (loginChoice == 2) {
                int choice;
                do {
                    System.out.println("\n===== Medical Representative Menu =====");
                    System.out.println("0. Go Back to Login Menu");
                    System.out.println("1. Update Doctor Availability Status");
                    System.out.println("2. Update Bed Availability Status");
                    System.out.println("3. Cancel Appointments by Doctor ID");
                    System.out.println("4. Delete Patient Record (and its Appointments)");
                    System.out.println("5. Show Appointment Details");
                    System.out.println("6. Show Patient Details");
                    System.out.println("7. Exit Application");
                    choice = readIntInput(scanner, "Enter your choice: ");

                    switch (choice) {
                        case 0: break;
                        case 1: updateDoctorAvailabilityStatus(); break;
                        case 2: updateBedAvailabilityStatus(); break;
                        case 3: cancelAppointmentsByDoctorId(); break;
                        case 4: deletePatientRecord(); break;
                        case 5: showAppointmentDetails(); break;
                        case 6: showPatientDetails(); break;
                        case 7: System.exit(0);
                        default: System.out.println("Invalid choice.");
                    }
                } while (choice != 0);

            } else if (loginChoice == 3) {
                System.exit(0);
            } else {
                System.out.println("Invalid choice. Please try again.");
            }
        } while (true);
    }
}
