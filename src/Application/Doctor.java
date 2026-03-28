package application;

import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import passanduser.Dao;
import passanduser.Dbconnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import auth.Login;

public class Doctor extends Application {    // Fetch appointments from the database
    ObservableList<Appointment> patientsBooking = FXCollections.observableArrayList();
    // Fetch appointments from the database
    private void loadAppointments(String doctorName) {
    	patientsBooking.clear();     
        // Added 'id' to the SELECT statement
        String sql = "SELECT id, username, date, serviceTime, dentalService, status FROM appointments WHERE dentist = ?";
        try (Connection con = Dbconnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            if (con != null) {
                ps.setString(1, doctorName);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        // Passing the retrieved id to the Appointment constructor
                    	patientsBooking.add(new Appointment(
                        	rs.getInt("id"),
                            rs.getString("username"),
                            rs.getString("serviceTime"),
                            rs.getString("date"),
                            rs.getString("dentalService"),
                            rs.getString("status")
                        ));
//                        String[] colNames = {"ID", "Patient", "Service Time", "Date", "Service", "Status"};
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("❌ Failed to load appointments: " + e.getMessage());
        }
    }
    public void start(Stage stage, String doctorName) {
    	
        Label logo = new Label("logo");
        logo.setStyle("-fx-background-color: #1a5276; -fx-text-fill: white; -fx-padding: 15 25; -fx-font-weight: bold;");
        
        Label brand = new Label("CARES");
        brand.setStyle("-fx-font-weight: bold; -fx-font-size: 16;");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Button btnLogout = new Button("Logout");
        btnLogout.setStyle("-fx-background-color: white; -fx-border-color: #2e7d32;");
        btnLogout.setOnAction(e -> {
			Stage s = (Stage) btnLogout.getScene().getWindow();
			s.close();
			Login.main(new String[0]);
		});

        HBox header = new HBox(15, logo, brand, spacer, btnLogout);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(0, 15, 0, 0));
        header.setStyle("-fx-border-color: #2e7d32; -fx-border-width: 0 0 1 0;");

        TableView<Appointment> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPrefHeight(200);
        table.setStyle("-fx-border-color: #2e7d32; -fx-background-color: white;");

     // Explicitly map columns to the correct getter method names
        TableColumn<Appointment, String> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<Appointment, String> colPatient = new TableColumn<>("Patient");
        colPatient.setCellValueFactory(new PropertyValueFactory<>("patient"));

        TableColumn<Appointment, String> colServiceTime = new TableColumn<>("Service Time");
        colServiceTime.setCellValueFactory(new PropertyValueFactory<>("serviceTime"));

        TableColumn<Appointment, String> colDate = new TableColumn<>("Date");
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));

        TableColumn<Appointment, String> colService = new TableColumn<>("Service");
        colService.setCellValueFactory(new PropertyValueFactory<>("dentalService"));

        TableColumn<Appointment, String> colStatus = new TableColumn<>("Status");
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        table.getColumns().addAll(colId, colPatient, colServiceTime, colDate, colService, colStatus);

        loadAppointments(doctorName); // Load appointments from the database
        table.setItems(patientsBooking);
        
        VBox center = new VBox(15, new Label("Appointments"), table);
        center.setAlignment(Pos.CENTER);
        center.setPadding(new Insets(20, 60, 20, 60));

        String btnS = "-fx-background-color: white; -fx-border-color: #2e7d32; -fx-padding: 8 18;";
        Button btnUpdate = new Button("Update status");
        Button btnView = new Button("View Notes");
        Button btnAdd = new Button("Add notes");
        
        btnUpdate.setStyle(btnS);
        btnView.setStyle(btnS);
        btnAdd.setStyle(btnS);

        btnUpdate.setOnAction(e -> {
            Appointment selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) {
                String s = selected.getStatus();
                // Cycle: Pending -> Completed -> Canceled -> Pending
                ChoiceDialog<String> dialog = new ChoiceDialog<>(s, "Completed", "Cancelled", "Pending");
                dialog.setHeaderText("Update Appointment Status");
                dialog.showAndWait().ifPresent(newStatus -> {
					selected.setStatus(newStatus);
					// get the ID of the selected appointment
					int appointmentId = Integer.parseInt(selected.getId());
					// Update the status in the database
					Dao.updateAppointmentStatus(appointmentId, newStatus);
				});
                table.refresh();
                
            }
        });

        btnView.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Viewing notes...");
            alert.show();
        });

        btnAdd.setOnAction(e -> {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setHeaderText("Enter Appointment Notes");
            dialog.showAndWait();
        });

        HBox footer = new HBox(20, btnUpdate, btnView, btnAdd);
        footer.setAlignment(Pos.CENTER);
        footer.setPadding(new Insets(0, 0, 40, 0));

        BorderPane root = new BorderPane(center, header, null, footer, null);
        root.setStyle("-fx-border-color: #2e7d32; -fx-border-width: 2; -fx-background-color: white;");

        StackPane wrap = new StackPane(root);
        wrap.setPadding(new Insets(25));
        wrap.setStyle("-fx-background-color: white;");

        stage.setScene(new Scene(wrap, 850, 550));
        stage.setTitle("CARES - Doctor Portal");
        stage.show();
    }

    public static class Appointment {
		private final SimpleStringProperty id, patient, serviceTime, time, dentalService, status;
        public Appointment(int id, String patientUsername, String serviceTime, String date, String dentalService, String status) {
			this.id = new SimpleStringProperty(String.valueOf(id));
			this.patient = new SimpleStringProperty(patientUsername);
			this.serviceTime = new SimpleStringProperty(serviceTime);
			this.time = new SimpleStringProperty(date);
			this.dentalService = new SimpleStringProperty(dentalService);
			this.status = new SimpleStringProperty(status);
        }
		public String getId() { return id.get(); }
		public String getPatient() { return patient.get(); }
		public String getServiceTime() { return serviceTime.get(); }
		public String getDate() { return time.get(); }
		public String getDentalService() { return dentalService.get(); }
		public String getStatus() { return status.get(); }
		public void setStatus(String newStatus) { this.status.set(newStatus); }
		
    }
    @Override
    public void start(Stage stage) {
        // Default fallback if launched directly without a doctor name
        start(stage, "Unknown Doctor");
    }

    public static void main(String[] args) { Application.launch(args); }
}