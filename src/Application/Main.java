package application;

import javafx.application.Application;
import passanduser.Dao;
import passanduser.Dbconnection; // Added import for Dbconnection
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;

import auth.Login;
import functions.applicationFunctions;

public class Main {
    public static class App extends Application {
        private BorderPane mainLayout; 
        private ObservableList<Appointment> appointmentList = FXCollections.observableArrayList();

        @Override
        public void start(Stage primaryStage) {
            mainLayout = new BorderPane();
            setupHeader(); 
            showDashboardView(); 

            Scene scene = new Scene(mainLayout, 1000, 700);
            scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
            
            primaryStage.setMaximized(true); 
            primaryStage.setScene(scene);
            primaryStage.show();
        }

        private void setupHeader() {
            HBox header = new HBox(0); 
            header.getStyleClass().add("header");
            
            Label logo = new Label("logo");
            logo.getStyleClass().add("logo-box");
            
            Label title = new Label("CARES");
            title.getStyleClass().add("logo-text-style"); 
            
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            
            Button btnProfile = new Button("Profile");
            Button btnLogout = new Button("Logout");
            btnLogout.getStyleClass().add("btn-danger");
            
            btnLogout.setOnAction(e -> {
            	Stage stage = (Stage) btnLogout.getScene().getWindow();
            	stage.close(); 
            	Login.main(new String[0]); 
            });
            
            header.getChildren().addAll(logo, title, spacer, btnProfile, btnLogout);
            mainLayout.setTop(header);
        }

        // Helper method to convert DB name to Display name
        private String getDisplayDentistName(String dbName) {
            if (dbName == null) return null;
            switch (dbName) {
                case "maria_santos": return "Dr. Maria Santos";
                case "ricardo_reyes": return "Dr. Ricardo Reyes";
                case "elena_cruzz": return "Dr. Elena Cruz";
                default: return dbName;
            }
        }

        // Helper method to convert Display name to DB name
        private String getDbDentistName(String displayName) {
            if (displayName == null) return null;
            switch (displayName) {
                case "Dr. Maria Santos": return "maria_santos";
                case "Dr. Ricardo Reyes": return "ricardo_reyes";
                case "Dr. Elena Cruz": return "elena_cruzz";
                default: return displayName;
            }
        }

        // Fetch appointments from the database
        private void loadAppointments() {
            appointmentList.clear();
            String username = "test"; // Using the same hardcoded user as your bookAppointment call
            
            // Added 'id' to the SELECT statement
            String sql = "SELECT id, date, serviceTime, dentist, dentalService FROM appointments WHERE username = ?";
            try (Connection con = Dbconnection.getConnection();
                 PreparedStatement ps = con.prepareStatement(sql)) {
                
                if (con != null) {
                    ps.setString(1, username);
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            // Passing the retrieved id to the Appointment constructor
                            // Mapping internal db dentist name to display name
                            appointmentList.add(new Appointment(
                            	rs.getInt("id"),
                                rs.getString("date"),
                                rs.getString("serviceTime"),
                                getDisplayDentistName(rs.getString("dentist")), // this converts the stored db name to a user-friendly display name (e.g., "Dr. Maria Santos" to "maria_santos")
                                rs.getString("dentalService")
                            ));
                        }
                    }
                }
            } catch (Exception e) {
                System.out.println("❌ Failed to load appointments: " + e.getMessage());
            }
        }

        @SuppressWarnings("unchecked")
		public void showDashboardView() {
            loadAppointments(); // Refresh the list from the database before showing

            VBox content = new VBox(25);
            content.setAlignment(Pos.TOP_CENTER);
            content.setPadding(new Insets(40));

            Label lblHeadline = new Label("Upcoming Appointment");
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

            table.getColumns().addAll(colDate, colTime, colDentist, colService);

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

            ComboBox<String> serviceBox = new ComboBox<>(FXCollections.observableArrayList(
                "Dental Checkup", "Tooth Cleaning", "Tooth Extraction", "Dental Filling", "Root Canal", "Braces"
            ));
            
            ComboBox<String> dentistBox = new ComboBox<>(FXCollections.observableArrayList(
                "Dr. Maria Santos", "Dr. Ricardo Reyes", "Dr. Elena Cruz"
            ));
            
            DatePicker datePicker = new DatePicker();
            
            ComboBox<String> timeBox = new ComboBox<>(FXCollections.observableArrayList(
                "09:00 AM", "10:00 AM", "11:00 AM", "01:00 PM", "02:00 PM", "03:00 PM", "04:00 PM"
            ));

            if (existingAppointment != null) {
                serviceBox.setValue(existingAppointment.getService());
                dentistBox.setValue(existingAppointment.getDentist()); // Display name is stored in the Appointment class
                timeBox.setValue(existingAppointment.getTime());
                datePicker.setValue(LocalDate.parse(existingAppointment.getDate()));
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
					Dao.updateBooking("test", datePicker.getValue().toString(), timeBox.getValue(), getDbDentistName(dentistBox.getValue()), serviceBox.getValue(), null);
                    applicationFunctions.showDialog("INFORMATION", "Successfully rescheduled appointment.", "Reschedule", "Reschedule Successful");
					showDashboardView(); 
				} else if(datePicker.getValue() != null && serviceBox.getValue() != null) {
                    applicationFunctions.showDialog("INFORMATION", "Successfully booked appointment.", "Booking", "Booking Confirmed");
                    Dao.bookAppointment("test", datePicker.getValue().toString(), timeBox.getValue(), getDbDentistName(dentistBox.getValue()), serviceBox.getValue());
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
            private String date, time, dentist, service;
            
            public Appointment(int id, String date, String time, String dentist, String service) {
            	this.id = id;
                this.date = date; this.time = time; this.dentist = dentist; this.service = service;
            }
            
            public int getId() { return id; } 
            public String getDate() { return date; }
            public String getTime() { return time; }
            public String getDentist() { return dentist; }
            public String getService() { return service; }
        }
    }

    public static void main(String[] args) { Application.launch(App.class, args); }
}
