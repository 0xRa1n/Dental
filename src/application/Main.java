package application;

import javafx.application.Application;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import passanduser.Dao;
import passanduser.Dbconnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;

import java.util.ArrayList;
import java.util.List;


import java.util.function.UnaryOperator;

import auth.Login;
import functions.applicationFunctions;

public class Main {
    public static class App extends Application {
        
        private BorderPane mainLayout; 
        private ObservableList<Appointment> appointmentList = FXCollections.observableArrayList();
        private String username;

        public static class DoctorAvailability {
            public List<String> recurringDays = new ArrayList<>();
            public List<LocalDate> blockedDates = new ArrayList<>();
            public String startTime;
            public String endTime;
        }
        
        // Overloaded start method that takes the name
        public void start(Stage primaryStage, String patientName) {
            this.username = patientName;
            
            mainLayout = new BorderPane();
            setupHeader(); 
            showDashboardView(); 

            Scene scene = new Scene(mainLayout, 1000, 700);
            scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());

            primaryStage.setScene(scene);
            primaryStage.show();
        }
       
        
        private void updateProfilePopup() {
        	String currentEmail = "";
        	String currentFullName = "";
        	String currentPassword = "";
        	int userId = 0;
        	
        	// sql to fetch current profile details and pre-fill the form
        	String SQL = "SELECT username, email, password, full_name, id FROM users WHERE username = ?";
        	try (Connection con = Dbconnection.getConnection();
        		PreparedStatement ps = con.prepareStatement(SQL)) {
        		if (con != null) {
        			ps.setString(1, this.username);
					try (ResultSet rs = ps.executeQuery()) {
						if (rs.next()) {
							// Pre-fill form fields with current profile data
							currentEmail = rs.getString("email");
							currentFullName = rs.getString("full_name");
							currentPassword = rs.getString("password");
							userId = rs.getInt("id");
							// You can use these variables to set the initial values of the form fields
						}
					}
        		}
        	} catch (Exception e) {
        		System.out.println("❌ Failed to load profile details: " + e.getMessage());        		
        	}
        	
            Stage popup = new Stage();
            popup.initModality(Modality.APPLICATION_MODAL);
            popup.setTitle("Edit Profile");
            
            // Integer Only TextField
            TextField txtID = new TextField();
            txtID.setPromptText("User ID");
            UnaryOperator<TextFormatter.Change> integerFilter = change -> {
                String newText = change.getControlNewText();
                if (newText.matches("\\d*")) {
                    return change;
                }
                return null;
            };
            txtID.setTextFormatter(new TextFormatter<>(integerFilter));
            txtID.setDisable(true); // ID should not be editable, just displayed

            TextField txtName = new TextField(currentFullName);
            txtName.setPromptText("Full Name");
            
            TextField txtUsername = new TextField(username);
            txtUsername.setPromptText("Username");
            
            TextField txtPassword = new TextField(currentPassword);
            txtPassword.setPromptText("Current Password");
            
            TextField txtEmail = new TextField(currentEmail);
            txtEmail.setPromptText("Email");
           

            VBox form = new VBox(10,
            		new Label("Full Name:"), txtName,
            		new Label("Username:"), txtUsername,
            		new Label("New Password:"), txtPassword,
            		new Label("Email:"), txtEmail
            );
            
            Button btnSave = new Button("Save");
            
            final int finalUserID = userId;
            
            btnSave.setOnAction(e -> {
                System.out.println("Attempting to update profile for user: " + username);
                String newName = txtName.getText().trim();
                String newUsername = txtUsername.getText().trim();
                String newPassword = txtPassword.getText().trim();
                String newEmail = txtEmail.getText().trim();            	

                // Update the database
                Dao.updateUser(finalUserID, newName, newUsername, newPassword, newEmail, "patient");
                
                // Show success dialog
                applicationFunctions.showDialog("INFORMATION", "Profile updated successfully.", "Profile Update", "Update Successful");
                
                // Update the local variable
                this.username = newUsername; 
                
                // Close the popup first
                popup.close();

                // Safely refresh the dashboard UI on the JavaFX Application Thread
                javafx.application.Platform.runLater(() -> {
                    showDashboardView();
                });
            });


            VBox layout = new VBox(10, form, btnSave);
            layout.setPadding(new Insets(15));

            popup.setScene(new Scene(layout, 300, 305));
            popup.showAndWait();
        }

        // Default start method required by JavaFX
        @Override
        public void start(Stage primaryStage) {
            start(primaryStage, "Unknown Patient");
        }
        
        private int parseHour(String time) {
            String[] parts = time.split(":");
            int hour = Integer.parseInt(parts[0]);
            if (time.contains("PM") && hour != 12) hour += 12;
            if (time.contains("AM") && hour == 12) hour = 0;
            return hour;
        }

        private String formatHour(int h) {
            int hour = h % 12;
            if (hour == 0) hour = 12;
            return String.format("%02d:00 %s", hour, h < 12 ? "AM" : "PM");
        }

        private void setupHeader() {
            HBox header = new HBox(0); 
            header.getStyleClass().add("header");
            
            Image image = new Image(getClass().getResourceAsStream("/images/logo.jpg"));
            ImageView logo = new ImageView(image);

            logo.setFitWidth(80);
            logo.setFitHeight(50);
            logo.setPreserveRatio(true);
            
            Label title = new Label("CARES");
            title.getStyleClass().add("logo-text-style"); 
            
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            
            Button btnProfile = new Button("Profile");
            Button btnLogout = new Button("Logout");
            btnLogout.getStyleClass().add("btn-danger");
            
            btnProfile.setOnAction(e -> updateProfilePopup());
            
            btnLogout.setOnAction(e -> {
                Stage stage = (Stage) btnLogout.getScene().getWindow();
                stage.close(); 
                Login.main(new String[0]); 
            });
            
            header.getChildren().addAll(logo, title, spacer, btnProfile, btnLogout);
            mainLayout.setTop(header);
        }

        private String getDisplayDentistName(String dbName) {
            if (dbName == null) return null;
            switch (dbName) {
                case "maria_santos": return "Dr. Maria Santos";
                case "ricardo_reyes": return "Dr. Ricardo Reyes";
                case "elena_cruzz": return "Dr. Elena Cruz";
                default: return dbName;
            }
        }

        private String getDbDentistName(String displayName) {
            if (displayName == null) return null;
            switch (displayName) {
                case "Dr. Maria Santos": return "maria_santos";
                case "Dr. Ricardo Reyes": return "ricardo_reyes";
                case "Dr. Elena Cruz": return "elena_cruzz";
                default: return displayName;
            }
        }

        private void loadAppointments() {
            appointmentList.clear();
            String sql = "SELECT id, date, serviceTime, dentist, status, dentalService FROM appointments WHERE username = ?";
            try (Connection con = Dbconnection.getConnection();
                 PreparedStatement ps = con.prepareStatement(sql)) {
                
                if (con != null) {
                    ps.setString(1, this.username);
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            appointmentList.add(new Appointment(
                                rs.getInt("id"),
                                rs.getString("date"),
                                rs.getString("serviceTime"),
                                getDisplayDentistName(rs.getString("dentist")),
                                rs.getString("dentalService"),
                                rs.getString("status")
                            ));
                        }
                    }
                }
            } catch (Exception e) {
                System.out.println("❌ Failed to load appointments: " + e.getMessage());
            }
        }

        public void showDashboardView() {
            loadAppointments(); 

            VBox content = new VBox(25);
            content.setAlignment(Pos.TOP_CENTER);
            content.setPadding(new Insets(40));

            Label lblHeadline = new Label("Upcoming Appointment for " + (username != null ? username : "Patient"));
            lblHeadline.getStyleClass().add("headline-main");

            TableView<Appointment> table = new TableView<>();
            table.setItems(appointmentList); 
            table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
            table.setPrefHeight(450);

            TableColumn<Appointment, String> colDate = new TableColumn<>("Date");
            colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
            TableColumn<Appointment, String> colTime = new TableColumn<>("Time");
            colTime.setCellValueFactory(new PropertyValueFactory<>("time"));
            TableColumn<Appointment, String> colDentist = new TableColumn<>("Dentist");
            colDentist.setCellValueFactory(new PropertyValueFactory<>("dentist"));
            TableColumn<Appointment, String> colService = new TableColumn<>("Service");
            colService.setCellValueFactory(new PropertyValueFactory<>("service"));
            TableColumn<Appointment, String> colStatus = new TableColumn<>("Status");
            colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

            table.getColumns().addAll(colDate, colTime, colDentist, colService, colStatus);

            HBox actionButtons = new HBox(20);
            actionButtons.setAlignment(Pos.CENTER);
            
            Button btnBook = new Button("Book");
            Button btnResched = new Button("Edit");

            btnBook.setOnAction(e -> showBookingView(null));
            
            btnResched.setOnAction(e -> {
                Appointment selected = table.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    showBookingView(selected);
                } else {
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setContentText("Please select an appointment from the table to reschedule.");
                    alert.show();
                }
            });

            actionButtons.getChildren().addAll(btnBook, btnResched);
            content.getChildren().addAll(lblHeadline, table, actionButtons);
            mainLayout.setCenter(content); 
        }

        public void showBookingView(Appointment existingAppointment) {
            VBox formBox = new VBox(25);
            formBox.getStyleClass().add("booking-border-box");
            formBox.setMaxWidth(600);
            formBox.setAlignment(Pos.CENTER);
        
            String titleText = (existingAppointment == null) ? "Book Appointment" : "Reschedule Appointment";
            Label formTitle = new Label(titleText);
            formTitle.getStyleClass().add("headline-main");
        
            GridPane grid = new GridPane();
            grid.setVgap(15); grid.setHgap(15);
            grid.setAlignment(Pos.CENTER);
        
            ComboBox<String> serviceBox = new ComboBox<>();
            serviceBox.setDisable(true); 
            serviceBox.setPromptText("Select a Dentist first");

            ComboBox<String> dentistBox = new ComboBox<>(FXCollections.observableArrayList(
                "Dr. Maria Santos", "Dr. Ricardo Reyes", "Dr. Elena Cruz"
            ));

            DatePicker datePicker = new DatePicker();
            datePicker.setDisable(true); 

            ComboBox<String> timeBox = new ComboBox<>();
            timeBox.setDisable(true); 

            dentistBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null) {
                    String dbName = getDbDentistName(newVal);

                    DoctorAvailability avail = Dao.getDoctorAvailability(dbName);
                    List<String> availableServices = Dao.getDoctorServices(dbName);

                    datePicker.setDisable(false);
                    timeBox.setDisable(false);
                    serviceBox.setDisable(false);

                    serviceBox.getItems().clear();
                    if (!availableServices.isEmpty()) {
                        serviceBox.getItems().addAll(availableServices);
                        serviceBox.setPromptText("Select a Service");
                    } else {
                        serviceBox.setPromptText("No services configured");
                    }

                    datePicker.setDayCellFactory(dp -> new DateCell() {
                        @Override
                        public void updateItem(LocalDate item, boolean empty) {
                            super.updateItem(item, empty);
                            if (empty || item == null) return;

                            if (item.isBefore(LocalDate.now())) { 
                                setDisable(true);
                                setStyle("-fx-background-color: #e0e0e0;");
                                return;
                            }

                            String dayName = item.getDayOfWeek().name();

                            if (!avail.recurringDays.contains(dayName) || avail.blockedDates.contains(item)) { 
                                setDisable(true);
                                setStyle("-fx-background-color: #ffcdd2;"); 
                            }
                        }
                    });

                    timeBox.getItems().clear();
                    if (avail.startTime != null && avail.endTime != null) {
                        int startHour = parseHour(avail.startTime);
                        int endHour = parseHour(avail.endTime);

                        for (int h = startHour; h <= endHour; h++) {
                            timeBox.getItems().add(formatHour(h)); 
                        }
                    }
                }
            });

            if (existingAppointment != null) { 
                dentistBox.setValue(existingAppointment.getDentist()); 
                serviceBox.setValue(existingAppointment.getService());
                datePicker.setValue(LocalDate.parse(existingAppointment.getDate()));
                timeBox.setValue(existingAppointment.getTime());
            }
        
            grid.addRow(0, new Label("Service:"), serviceBox);
            grid.addRow(1, new Label("Dentist:"), dentistBox);
            grid.addRow(2, new Label("Date:"), datePicker);
            grid.addRow(3, new Label("Time:"), timeBox);
        
            HBox formActions = new HBox(15);
            formActions.setAlignment(Pos.CENTER);
        
            Button btnConfirm = new Button("Confirm");
            Button btnCancel = new Button("Cancel");
        
            btnConfirm.setOnAction(e -> {
                if (existingAppointment != null) { 
                    Dao.updateBooking(existingAppointment.getId(), datePicker.getValue().toString(), timeBox.getValue(), getDbDentistName(dentistBox.getValue()), serviceBox.getValue(), null);
                    applicationFunctions.showDialog("INFORMATION", "Successfully rescheduled appointment.", "Reschedule", "Reschedule Successful");
                    showDashboardView(); 
                } else if(datePicker.getValue() != null && serviceBox.getValue() != null && dentistBox.getValue() != null && timeBox.getValue() != null) {
                    if(datePicker.getEditor().getText().isEmpty() || timeBox.getValue() == null) {
                        Alert alert = new Alert(Alert.AlertType.WARNING, "Please select a valid date and time from the available options.");
                        alert.showAndWait();
                        return;
                    }
                    applicationFunctions.showDialog("INFORMATION", "Successfully booked appointment.", "Booking", "Booking Confirmed");
                    Dao.bookAppointment(username, datePicker.getValue().toString(), timeBox.getValue(), getDbDentistName(dentistBox.getValue()), serviceBox.getValue());
                    showDashboardView(); 
                } else {
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setContentText("Please fill in all required fields.");
                    alert.showAndWait();
                }
            });
        
            btnCancel.setOnAction(e -> showDashboardView());
        
            if(existingAppointment != null) {
                Button btnDelete = new Button("Delete");
                btnDelete.getStyleClass().add("btn-danger");
                
                btnDelete.setOnAction(e -> {
                    boolean confirmDelete = applicationFunctions.showConfirmationDialog("Are you sure you want to delete this appointment?", "Delete Appointment", "Confirm Deletion");
                    if (confirmDelete) {
                        Dao.deleteBooking(existingAppointment.getId()); 
                        applicationFunctions.showDialog("CONFIRMATION", "Successfully deleted appointment.", "Delete", "Deletion Successful");
                    }
                    showDashboardView(); 
                }); 
                
                formActions.getChildren().addAll(btnConfirm, btnCancel, btnDelete);
            } else {
                formActions.getChildren().addAll(btnConfirm, btnCancel);
            }
            
            formBox.getChildren().addAll(formTitle, grid, formActions);
            
            StackPane container = new StackPane(formBox);
            container.setPadding(new Insets(30));
            mainLayout.setCenter(container);
        }

        public static class Appointment {
            private int id;
            private String date, time, dentist, service, status;
            
            public Appointment(int id, String date, String time, String dentist, String service, String Status) {
                this.id = id;
                this.date = date; this.time = time; this.dentist = dentist; this.service = service; this.status = Status;
            }
            
            public int getId() { return id; } 
            public String getDate() { return date; }
            public String getTime() { return time; }
            public String getDentist() { return dentist; }
            public String getService() { return service; }
            public String getStatus() { return status; }
        }
    }

    public static void main(String[] args) { Application.launch(App.class, args); }
}
