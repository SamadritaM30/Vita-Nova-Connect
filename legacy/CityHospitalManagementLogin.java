import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import javafx.scene.control.Label;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;

public class CityHospitalManagementLogin extends Application {

    @Override
    public void start(Stage primaryStage) {
        // Main layout for login page
        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #f0f2f5;");

        // Title
        Label titleLabel = new Label("Medical Aid Tracker - Login");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        titleLabel.setStyle("-fx-text-fill: #333;");

        // Username field
        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");
        usernameField.setMaxWidth(300);
        usernameField.setStyle("-fx-background-radius: 5; -fx-border-radius: 5; -fx-border-color: #ddd;");

        // Password field
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.setMaxWidth(300);
        passwordField.setStyle("-fx-background-radius: 5; -fx-border-radius: 5; -fx-border-color: #ddd;");

        // Login button
        Button loginButton = new Button("Login");
        loginButton.setMaxWidth(300);
        loginButton.setStyle("-fx-background-color: #007bff; -fx-text-fill: white; -fx-font-size: 16; -fx-background-radius: 5;");
        loginButton.setOnAction(e -> {
            String username = usernameField.getText();
            String password = passwordField.getText();
            if (!username.isEmpty() && !password.isEmpty()) {
                System.out.println("Login attempt received for: " + username);
                // Placeholder for actual authentication
                showDashboard(primaryStage);
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Please enter both username and password.");
                alert.showAndWait();
            }
        });

        // Sign-up link
        Hyperlink signupLink = new Hyperlink("Don't have an account? Sign Up");
        signupLink.setStyle("-fx-text-fill: #007bff;");
        signupLink.setOnAction(e -> {
            System.out.println("Sign-up link clicked");
            // Placeholder for sign-up page
        });

        // Add components to the root layout
        root.getChildren().addAll(titleLabel, usernameField, passwordField, loginButton, signupLink);

        // Scene and Stage setup
        Scene scene = new Scene(root, 400, 500);
        primaryStage.setTitle("Medical Aid Tracker - Login");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    // Dashboard with the specified features
    private void showDashboard(Stage stage) {
        VBox dashboard = new VBox(15);
        dashboard.setAlignment(Pos.CENTER);
        dashboard.setPadding(new Insets(20));
        dashboard.setStyle("-fx-background-color: #f0f2f5;");

        Label welcomeLabel = new Label("Medical Aid Tracker - Menu");
        welcomeLabel.setFont(Font.font("Arial", FontWeight.BOLD, 20));

        // Buttons for each feature
        Button searchDoctorsButton = createButton("1. Search Doctors by Specialization");
        searchDoctorsButton.setOnAction(e -> {
            // Prompt for specialization
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Search Doctors");
            dialog.setHeaderText("Enter Specialization");
            dialog.setContentText("Specialization (e.g., Cardiologist):");
            dialog.showAndWait().ifPresent(specialization -> {
                // Prompt for bed check
                ChoiceDialog<String> bedDialog = new ChoiceDialog<>("No", "Yes");
                bedDialog.setTitle("Bed Availability Check");
                bedDialog.setHeaderText("Check Bed Availability?");
                bedDialog.setContentText("Do you want to check available bed numbers?");
                bedDialog.showAndWait().ifPresent(checkBeds -> {
                    String wardType = null;
                    if (checkBeds.equalsIgnoreCase("Yes")) {
                        ChoiceDialog<String> wardDialog = new ChoiceDialog<>("ICU", "General");
                        wardDialog.setTitle("Ward Selection");
                        wardDialog.setHeaderText("Select Ward Type");
                        wardDialog.setContentText("Which ward's bed count do you want?");
                        wardType = wardDialog.showAndWait().orElse(null);
                    }
                    // Call backend method
                    try {
                        ResultSet rs = MedicalAidTracker.searchDoctorsBySpecialization(specialization, checkBeds, wardType);
                        displayDoctorResults(rs, "Doctors with Specialization: " + specialization);
                    } catch (SQLException ex) {
                        showError("Error searching doctors: " + ex.getMessage());
                    }
                });
            });
        });

        Button searchEmergencyDoctorsButton = createButton("2. Search Emergency Doctors with Available Beds");
        searchEmergencyDoctorsButton.setOnAction(e -> {
            ChoiceDialog<String> dialog = new ChoiceDialog<>("ICU", "General");
            dialog.setTitle("Bed Type Selection");
            dialog.setHeaderText("Select Bed Type");
            dialog.setContentText("Enter required bed type:");
            dialog.showAndWait().ifPresent(bedType -> {
                try {
                    ResultSet rs = MedicalAidTracker.searchEmergencyDoctorsWithBeds(bedType);
                    displayEmergencyResults(rs, "Hospitals with Available " + bedType + " Beds and Doctors");
                } catch (SQLException ex) {
                    showError("Error searching emergency doctors: " + ex.getMessage());
                }
            });
        });

        

        Button searchAmbulanceButton = createButton("4. Search for Available Ambulance Services");
        searchAmbulanceButton.setOnAction(e -> {
            try {
                ResultSet rs = MedicalAidTracker.searchAmbulanceServices();
                displayAmbulanceResults(rs, "Available Ambulance Services");
            } catch (SQLException ex) {
                showError("Error searching ambulances: " + ex.getMessage());
            }
        });

        Button bookAppointmentButton = createButton("5. Book a Doctor Appointment");
        bookAppointmentButton.setOnAction(e -> {
            TextInputDialog patientDialog = new TextInputDialog();
            patientDialog.setTitle("Patient ID");
            patientDialog.setHeaderText("Enter Patient ID");
            patientDialog.setContentText("Patient ID:");
            patientDialog.showAndWait().ifPresent(patientId -> {
                TextInputDialog doctorDialog = new TextInputDialog();
                doctorDialog.setTitle("Doctor ID");
                doctorDialog.setHeaderText("Enter Doctor ID");
                doctorDialog.setContentText("Doctor ID:");
                doctorDialog.showAndWait().ifPresent(doctorId -> {
                    TextInputDialog hospitalDialog = new TextInputDialog();
                    hospitalDialog.setTitle("Hospital ID");
                    hospitalDialog.setHeaderText("Enter Hospital ID");
                    hospitalDialog.setContentText("Hospital ID:");
                    hospitalDialog.showAndWait().ifPresent(hospitalId -> {
                        TextInputDialog dateDialog = new TextInputDialog();
                        dateDialog.setTitle("Appointment Date");
                        dateDialog.setHeaderText("Enter Appointment Date");
                        dateDialog.setContentText("Date (YYYY-MM-DD):");
                        dateDialog.showAndWait().ifPresent(date -> {
                            TextInputDialog timeDialog = new TextInputDialog();
                            timeDialog.setTitle("Appointment Time");
                            timeDialog.setHeaderText("Enter Appointment Time");
                            timeDialog.setContentText("Time (HH:MM:SS):");
                            timeDialog.showAndWait().ifPresent(time -> {
                                try {
                                    int pId = Integer.parseInt(patientId);
                                    int dId = Integer.parseInt(doctorId);
                                    int hId = Integer.parseInt(hospitalId);
                                    LocalDate.parse(date); // Validate date format
                                    LocalTime.parse(time); // Validate time format
                                    MedicalAidTracker.bookAppointment(pId, dId, hId, date, time);
                                    showInfo("Appointment booked successfully.");
                                } catch (Exception ex) {
                                    showError("Error booking appointment: " + ex.getMessage());
                                }
                            });
                        });
                    });
                });
            });
        });

        Button addPatientButton = createButton("6. Add a New Patient");
        addPatientButton.setOnAction(e -> {
            TextInputDialog nameDialog = new TextInputDialog();
            nameDialog.setTitle("Patient Name");
            nameDialog.setHeaderText("Enter Patient Name");
            nameDialog.setContentText("Name:");
            nameDialog.showAndWait().ifPresent(name -> {
                TextInputDialog dobDialog = new TextInputDialog();
                dobDialog.setTitle("Date of Birth");
                dobDialog.setHeaderText("Enter Date of Birth");
                dobDialog.setContentText("DOB (YYYY-MM-DD):");
                dobDialog.showAndWait().ifPresent(dob -> {
                    TextInputDialog contactDialog = new TextInputDialog();
                    contactDialog.setTitle("Contact Number");
                    contactDialog.setHeaderText("Enter Contact Number");
                    contactDialog.setContentText("Contact Number:"); 
                    contactDialog.showAndWait().ifPresent(contact_number -> {
                        try {
                            LocalDate.parse(dob); // Validate date format
                            int patientId = MedicalAidTracker.addNewPatient(name, dob, contact_number);
                            showInfo("Patient added successfully! Assigned Patient ID: " + patientId);
                        } catch (Exception ex) {
                    showError("Error adding patient: " + ex.getMessage());
                }
            });
        });
    });
});

        Button exitButton = createButton("7. Exit");
        exitButton.setOnAction(e -> {
            System.out.println("Exit clicked");
            stage.close();
        });

        // Add all buttons to the dashboard
        dashboard.getChildren().addAll(
            welcomeLabel,
            searchDoctorsButton,
            searchEmergencyDoctorsButton,
            searchAmbulanceButton,
            bookAppointmentButton,
            addPatientButton,
            exitButton
        );

        Scene dashboardScene = new Scene(dashboard, 500, 600);
        stage.setScene(dashboardScene);
    }

    // Helper method to create styled buttons
    private Button createButton(String text) {
        Button button = new Button(text);
        button.setMaxWidth(400);
        button.setStyle("-fx-background-color: #007bff; -fx-text-fill: white; -fx-font-size: 14; -fx-background-radius: 5;");
        return button;
    }

    // Display search results for doctors
    private void displayDoctorResults(ResultSet rs, String title) throws SQLException {
        VBox resultBox = new VBox(10);
        resultBox.setAlignment(Pos.CENTER);
        resultBox.setPadding(new Insets(20));
        Label resultLabel = new Label(title);
        resultLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        resultBox.getChildren().add(resultLabel);

        boolean hasResults = false;
        while (rs.next()) {
            hasResults = true;
            int doctorId = rs.getInt("doctor_id");
            String doctorName = rs.getString("doctor_name");
            String availability = rs.getString("availability_status");
            String hospitalName = rs.getString("hospital_name");
            String address = rs.getString("address");
            int availableBeds = rs.getInt("available_beds");
            String resultText;
            if (availableBeds > 0) {
                resultText = String.format("Doctor ID: %d | Name: %s | Availability: %s | Hospital: %s | Address: %s | Available Beds: %d",
                        doctorId, doctorName, availability, hospitalName, address, availableBeds);
            } else {
                resultText = String.format("Doctor ID: %d | Name: %s | Availability: %s | Hospital: %s | Address: %s",
                        doctorId, doctorName, availability, hospitalName, address);
            }
            resultBox.getChildren().add(new Label(resultText));
        }
        if (!hasResults) {
            resultBox.getChildren().add(new Label("No results found."));
        }

        Scene resultScene = new Scene(resultBox, 600, 400);
        Stage resultStage = new Stage();
        resultStage.setTitle("Search Results");
        resultStage.setScene(resultScene);
        resultStage.show();
    }

    // Display search results for emergency doctors with beds
    private void displayEmergencyResults(ResultSet rs, String title) throws SQLException {
        VBox resultBox = new VBox(10);
        resultBox.setAlignment(Pos.CENTER);
        resultBox.setPadding(new Insets(20));
        Label resultLabel = new Label(title);
        resultLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        resultBox.getChildren().add(resultLabel);

        boolean hasResults = false;
        while (rs.next()) {
            hasResults = true;
            int hospitalId = rs.getInt("hospital_id");
            String hospitalName = rs.getString("hospital_name");
            String address = rs.getString("address");
            String contact = rs.getString("contact_number");
            int availableBeds = rs.getInt("available_beds");
            String hospitalText = String.format("Hospital ID: %d | Name: %s | Address: %s | Contact: %s | Available Beds: %d",
                    hospitalId, hospitalName, address, contact, availableBeds);
            resultBox.getChildren().add(new Label(hospitalText));

            // Fetch doctors for this hospital
            ResultSet doctorRs = MedicalAidTracker.getDoctorsForHospital(hospitalId);
            while (doctorRs.next()) {
                int doctorId = doctorRs.getInt("doctor_id");
                String doctorName = doctorRs.getString("name");
                String availability = doctorRs.getString("availability_status");
                String doctorText = String.format("  Doctor ID: %d | Name: %s | Availability: %s",
                        doctorId, doctorName, availability);
                resultBox.getChildren().add(new Label(doctorText));
            }
        }
        if (!hasResults) {
            resultBox.getChildren().add(new Label("No results found."));
        }

        Scene resultScene = new Scene(resultBox, 600, 400);
        Stage resultStage = new Stage();
        resultStage.setTitle("Search Results");
        resultStage.setScene(resultScene);
        resultStage.show();
    }

    // Display search results for ambulances
    private void displayAmbulanceResults(ResultSet rs, String title) throws SQLException {
        VBox resultBox = new VBox(10);
        resultBox.setAlignment(Pos.CENTER);
        resultBox.setPadding(new Insets(20));
        Label resultLabel = new Label(title);
        resultLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        resultBox.getChildren().add(resultLabel);

        boolean hasResults = false;
        while (rs.next()) {
            hasResults = true;
            int ambulanceId = rs.getInt("ambulance_id");
            String driverName = rs.getString("driver_name");
            String contactNumber = rs.getString("contact_number");
            String lastLocation = rs.getString("last_location");
            String resultText = String.format("Ambulance ID: %d | Driver: %s | Contact: %s | Last Location: %s",
                    ambulanceId, driverName, contactNumber, lastLocation);
            resultBox.getChildren().add(new Label(resultText));
        }
        if (!hasResults) {
            resultBox.getChildren().add(new Label("No results found."));
        }

        Scene resultScene = new Scene(resultBox, 600, 400);
        Stage resultStage = new Stage();
        resultStage.setTitle("Search Results");
        resultStage.setScene(resultScene);
        resultStage.show();
    }

    // Show error message
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message);
        alert.showAndWait();
    }

    // Show info message
    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
