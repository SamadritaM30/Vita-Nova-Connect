import java.sql.*;
public class MedicalAidTracker {
    // Archived prototype: credentials must be supplied by the environment.
    private static final String DB_URL = System.getenv().getOrDefault(
            "DB_URL", "jdbc:mysql://localhost:3306/medical_aid_tracker");
    private static final String DB_USERNAME = System.getenv().getOrDefault("DB_USERNAME", "");
    private static final String DB_PASSWORD = System.getenv().getOrDefault("DB_PASSWORD", "");

    // Utility method to obtain a connection
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);
    }

    // Search doctors by specialization, optionally filtering by bed availability
    public static ResultSet searchDoctorsBySpecialization(String specialization, String checkBeds, String wardType) throws SQLException {
        Connection conn = getConnection();
        PreparedStatement ps;
        if (checkBeds.equalsIgnoreCase("No")) {
            String query = "SELECT d.doctor_id, d.name AS doctor_name, d.availability_status, " +
                          "h.hospital_name, h.address, 0 AS available_beds " +
                          "FROM Doctors d JOIN Hospitals h ON d.hospital_id = h.hospital_id " +
                          "WHERE d.specialization = ?";
            ps = conn.prepareStatement(query);
            ps.setString(1, specialization);
        } else if (checkBeds.equalsIgnoreCase("Yes")) {
            String wardColumn = wardType.equalsIgnoreCase("ICU") ? "available_ICU" : "available_General";
            String query = "SELECT d.doctor_id, d.name AS doctor_name, d.availability_status, " +
                          "h.hospital_name, h.address, " +
                          "(SELECT COUNT(*) FROM Bed_Availability b WHERE b.hospital_id = h.hospital_id " +
                          " AND b.status = 'Available' AND b.ward_type = ?) AS " + wardColumn + " " +
                          "FROM Doctors d JOIN Hospitals h ON d.hospital_id = h.hospital_id " +
                          "WHERE d.specialization = ? AND d.availability_status = 'Available' " +
                          "AND (SELECT COUNT(*) FROM Bed_Availability b WHERE b.hospital_id = h.hospital_id " +
                          " AND b.status = 'Available' AND b.ward_type = ?) > 0";
            ps = conn.prepareStatement(query);
            ps.setString(1, wardType);
            ps.setString(2, specialization);
            ps.setString(3, wardType);
        } else {
            throw new SQLException("Invalid bed check option.");
        }
        return ps.executeQuery();
    }

    // Search hospitals with available beds and doctors
    public static ResultSet searchEmergencyDoctorsWithBeds(String bedType) throws SQLException {
        Connection conn = getConnection();
        String hospitalQuery = "SELECT h.hospital_id, h.hospital_name, h.address, h.contact_number, " +
                              " (SELECT COUNT(*) FROM Bed_Availability b " +
                              "   WHERE b.hospital_id = h.hospital_id AND b.status = 'Available' AND b.ward_type = ?) AS available_beds " +
                              "FROM Hospitals h " +
                              "WHERE (SELECT COUNT(*) FROM Bed_Availability b " +
                              "       WHERE b.hospital_id = h.hospital_id AND b.status = 'Available' AND b.ward_type = ?) > 0 " +
                              "  AND h.hospital_id IN (SELECT d.hospital_id FROM Doctors d WHERE d.availability_status = 'Available')";
        PreparedStatement ps = conn.prepareStatement(hospitalQuery);
        ps.setString(1, bedType);
        ps.setString(2, bedType);
        return ps.executeQuery();
    }

    // Fetch available doctors for a specific hospital
    public static ResultSet getDoctorsForHospital(int hospitalId) throws SQLException {
        Connection conn = getConnection();
        String doctorQuery = "SELECT doctor_id, name, availability_status FROM Doctors " +
                            "WHERE hospital_id = ? AND availability_status = 'Available'";
        PreparedStatement ps = conn.prepareStatement(doctorQuery);
        ps.setInt(1, hospitalId);
        return ps.executeQuery();
    }

    // Book an ambulance request
    public static void bookAmbulance(int patientId, int hospitalId) throws SQLException {
        String insert = "INSERT INTO Ambulance_Requests (patient_id, hospital_id, status) " +
                       "VALUES (?, ?, 'Pending')";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(insert)) {
            ps.setInt(1, patientId);
            ps.setInt(2, hospitalId);
            int rows = ps.executeUpdate();
            if (rows <= 0) {
                throw new SQLException("Failed to book ambulance request.");
            }
        }
    }

    // Search for available ambulance services
    public static ResultSet searchAmbulanceServices() throws SQLException {
        String query = "SELECT ambulance_id, driver_name, contact_number, last_location " +
                      "FROM Ambulance_Services WHERE availability_status = 'Available'";
        Connection conn = getConnection();
        Statement stmt = conn.createStatement();
        return stmt.executeQuery(query);
    }

    // Book a doctor appointment
    public static void bookAppointment(int patientId, int doctorId, int hospitalId, String dateStr, String timeStr) throws SQLException {
        String insert = "INSERT INTO Appointments (patient_id, doctor_id, hospital_id, appointment_date, appointment_time, status) " +
                       "VALUES (?, ?, ?, ?, ?, 'Booked')";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(insert)) {
            ps.setInt(1, patientId);
            ps.setInt(2, doctorId);
            ps.setInt(3, hospitalId);
            ps.setDate(4, Date.valueOf(dateStr));
            ps.setTime(5, Time.valueOf(timeStr));
            int rows = ps.executeUpdate();
            if (rows <= 0) {
                throw new SQLException("Failed to book appointment.");
            }
        }
    }
// Add a new patient and display the assigned patient ID
public static int addNewPatient(String name, String dob, String contactNumber) throws SQLException {
    int patientId = -1; // Default value if insertion fails
    String insertQuery = "INSERT INTO patients (name, dob, contact_number) VALUES (?, ?, ?)";

    try (Connection conn = getConnection();
         PreparedStatement pstmt = conn.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS)) {
        
        pstmt.setString(1, name);
        pstmt.setString(2, dob);
        pstmt.setString(3, contactNumber);

        int rowsAffected = pstmt.executeUpdate();
        if (rowsAffected > 0) {
            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    patientId = rs.getInt(1);
                }
            }
        } else {
            throw new SQLException("Failed to add patient.");
        }
    }
    return patientId;
}



}
