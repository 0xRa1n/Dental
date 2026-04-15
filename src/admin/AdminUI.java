package admin;

import javafx.application.Application;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.beans.property.SimpleStringProperty;
import javafx.util.Callback;
import passanduser.Dbconnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import passanduser.Dao;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class AdminUI extends Application {

    private TableView<Appointment> table = new TableView<>();
    private ObservableList<Appointment> appointments = FXCollections.observableArrayList();
    private static final int ROWS_PER_PAGE = 15;
    
    // Elevate pagination to class scope
    private Pagination pagination;
    
    // function to read appointments from database and display it in the table
    
    private void loadAppointments() {
    	// clear the table first
    	appointments.clear();
    	
    	String sql = "SELECT id, date, serviceTime, username, dentist, dentalService, status FROM appointments";
    	
    	try (Connection con = Dbconnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)){
    		if(con != null) {
    			try (ResultSet rs = ps.executeQuery()) {
					while(rs.next()) {
						int id = rs.getInt("id");
						String date = rs.getString("date");
						String time = rs.getString("serviceTime");
						String patient = rs.getString("username");
						String dentist = rs.getString("dentist");
						String service = rs.getString("dentalService");
						String status = rs.getString("status");
						
						appointments.add(new Appointment(id, date, time, patient, dentist, service, status));
					}
    				
    			} catch(Exception e) {
    				System.out.println("❌ Failed to load appointments: " + e.getMessage());
    			}
    		} 
    	} catch (Exception e) {
            System.out.println("❌ Failed to load appointments: " + e.getMessage());
        }
    }
    
    private void loadPatientCount(Label label) {
		String sql = "SELECT COUNT(*) FROM users";
		
		try (Connection con = Dbconnection.getConnection();
				PreparedStatement ps = con.prepareStatement(sql)){
			if(con != null) {
				try (ResultSet rs = ps.executeQuery()) {
					if(rs.next()) {
						System.out.println(rs);
						int total = rs.getInt(1); // in order to get the count of the patients, we need to set it to index 1, so that it could return the total of patients 
						label.setText("Total accounts: " + total);
					}
					
				} catch(Exception e) {
					System.out.println("❌ Failed to load patient count: " + e.getMessage());
				}
			} 
		} catch (Exception e) {
			System.out.println("❌ Failed to load patient count: " + e.getMessage());
		}
	}
    
    private void refreshTableData() {
        // 1. Synchronize the master list with the database
        loadAppointments();
        
        // 2. Safely recalculate total pages, preventing the 0-page exception
        int newPageCount = (int) Math.ceil(appointments.size() / (double) ROWS_PER_PAGE);
        pagination.setPageCount(Math.max(1, newPageCount));
        
        // 3. Manually calculate the mathematical subset bounds for the current page
        int currentPage = pagination.getCurrentPageIndex();
        int fromIndex = currentPage * ROWS_PER_PAGE;
        int toIndex = Math.min(fromIndex + ROWS_PER_PAGE, appointments.size());
        
        // 4. Force the table to display the newly bounded subset
        if (fromIndex < appointments.size()) {
            table.setItems(FXCollections.observableArrayList(appointments.subList(fromIndex, toIndex)));
        } else {
            table.getItems().clear(); // Clears the view if deleting the last item left the page empty
        }
        
        table.refresh();
    }

    
    private void loadAppointmentCount(Label label) {
    	String sql = "SELECT COUNT(*) FROM appointments";
		try (Connection con = Dbconnection.getConnection();
				PreparedStatement ps = con.prepareStatement(sql)){
			if(con != null) {
				try (ResultSet rs = ps.executeQuery()) {
					if(rs.next()) {
						int total = rs.getInt(1); // in order to get the count of the appointments, we need to set it to index 1, so that it could return the total of appointments 
						label.setText("Total appointments: " + total);
					}
					
				} catch(Exception e) {
					System.out.println("❌ Failed to load appointment count: " + e.getMessage());
				}
			} 
		} catch (Exception e) {
			System.out.println("❌ Failed to load appointment count: " + e.getMessage());
		}
    }
    @Override
    public void start(Stage stage) {
        stage.setTitle("CARES Dashboard");
        stage.setWidth(900);
        stage.setHeight(770);
        loadAppointments();

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color:#e6e6e6; -fx-border-color:green; -fx-border-width:2;");

        // ===== TOP BAR =====
        HBox topBar = new HBox();
        topBar.setStyle("-fx-border-color:green; -fx-border-width:1;");

        Image image = new Image(getClass().getResourceAsStream("/images/logo.jpg"));
        ImageView logo = new ImageView(image);

        logo.setFitWidth(80);
        logo.setFitHeight(50);
        logo.setPreserveRatio(true);

        Label title = new Label("CARES");
        title.setStyle("-fx-padding:10;");

        MenuButton manageBtn = new MenuButton("Manage");
        MenuItem patients = new MenuItem("Patients");
        MenuItem doctors = new MenuItem("Doctors");
        MenuItem admins = new MenuItem("Admins");

        manageBtn.getItems().addAll(patients, doctors, admins);

        Button logoutBtn = new Button("Logout");

        HBox right = new HBox(10, manageBtn, logoutBtn);
        right.setAlignment(Pos.CENTER_RIGHT);
        right.setPadding(new Insets(10));
        HBox.setHgrow(right, Priority.ALWAYS);

        topBar.getChildren().addAll(logo, title, right);

        // ===== REPORTS TITLE (OUTSIDE BOX) =====
        Label reportsTitle = new Label("Reports");
        reportsTitle.setStyle("-fx-font-weight:bold;");
        reportsTitle.setMaxWidth(Double.MAX_VALUE);
        reportsTitle.setAlignment(Pos.CENTER);

        // ===== REPORTS BOX (ONLY CONTENT INSIDE) =====
        VBox reportsBox = new VBox(5);
        reportsBox.setPadding(new Insets(10));
        reportsBox.setStyle("-fx-border-color:green;");
        reportsBox.setAlignment(Pos.CENTER);
        reportsBox.setMaxWidth(Double.MAX_VALUE);
//
//        Label totalPatients = new Label("Total patients: 120");
        
        // count all the patients in the database and display it in the label

        Label totalPatients = new Label("Total patients: Loading...");
        loadPatientCount(totalPatients);
        
        Label appointmentsToday = new Label("Total appointments: Loading...");
        loadAppointmentCount(appointmentsToday);

        for (Label lbl : new Label[]{totalPatients, appointmentsToday}) {
            lbl.setMaxWidth(Double.MAX_VALUE);
            lbl.setAlignment(Pos.CENTER);
        }

        reportsBox.getChildren().addAll(
                totalPatients,
                appointmentsToday
        );

        // ===== TABLE =====
        Label appTitle = new Label("Appointments");
        appTitle.setStyle("-fx-padding:5; -fx-font-weight:bold;");
        appTitle.setMaxWidth(Double.MAX_VALUE);
        appTitle.setAlignment(Pos.CENTER);
        
        TableColumn<Appointment, String> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(data -> data.getValue().idProperty());

        TableColumn<Appointment, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(data -> data.getValue().dateProperty());

        TableColumn<Appointment, String> timeCol = new TableColumn<>("Time");
        timeCol.setCellValueFactory(data -> data.getValue().timeProperty());

        TableColumn<Appointment, String> patientCol = new TableColumn<>("Patient");
        patientCol.setCellValueFactory(data -> data.getValue().patientProperty());

        TableColumn<Appointment, String> dentistCol = new TableColumn<>("Dentist");
        dentistCol.setCellValueFactory(data -> data.getValue().dentistProperty());

        TableColumn<Appointment, String> serviceCol = new TableColumn<>("Service");
        serviceCol.setCellValueFactory(data -> data.getValue().serviceProperty());
        
        TableColumn<Appointment, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(data -> data.getValue().statusProperty());

        // ===== CENTER CELL FACTORY =====
        Callback<TableColumn<Appointment, String>, TableCell<Appointment, String>> centerCell =
                col -> new TableCell<>() {
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        setText(empty || item == null ? null : item);
                        setAlignment(Pos.CENTER);
                    }
                };

        idCol.setCellFactory(centerCell);
        dateCol.setCellFactory(centerCell);
        timeCol.setCellFactory(centerCell);
        patientCol.setCellFactory(centerCell);
        dentistCol.setCellFactory(centerCell);
        serviceCol.setCellFactory(centerCell);
        statusCol.setCellFactory(centerCell);

//        String centerStyle = "-fx-alignment: CENTER;";
//        idCol.setStyle(centerStyle);
//        dateCol.setStyle(centerStyle);
//        timeCol.setStyle(centerStyle);
//        patientCol.setStyle(centerStyle);
//        dentistCol.setStyle(centerStyle);
//        serviceCol.setStyle(centerStyle);

        table.getColumns().addAll(idCol, dateCol, timeCol, patientCol, dentistCol, serviceCol, statusCol);

        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setMaxWidth(Double.MAX_VALUE);
        table.setMaxHeight(Double.MAX_VALUE);
        table.setStyle("-fx-border-color:green;");

        // ===== PAGINATOR =====
        // Assign to the class-level field instead of creating a local variable
        pagination = new Pagination((int) Math.ceil(appointments.size() / (double)ROWS_PER_PAGE), 0);
        pagination.setPageFactory(this::createPage);


        // ===== BUTTONS =====
        Button editBtn = new Button("Edit Appointment");
        Button removeBtn = new Button("Remove Appointment");

        editBtn.setStyle("-fx-border-color:green;");
        removeBtn.setStyle("-fx-border-color:green;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox btnBox = new HBox(20, editBtn, spacer, removeBtn);
        btnBox.setPadding(new Insets(10));
        btnBox.setAlignment(Pos.CENTER_LEFT);
        btnBox.setMaxWidth(Double.MAX_VALUE);

        // ===== EDIT POPUP =====
        editBtn.setOnAction(e -> {
        	// set a custom size for this popup, so that it could fit the content of the popup, and also make it look better
        	
            Appointment selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) {
            	functions.applicationFunctions.showDialog("Please select an appointment to edit.", "No Selection", "Error", "ERROR");
				return;
            };

            Stage popup = new Stage();
            popup.initModality(Modality.APPLICATION_MODAL);
            popup.setTitle("Edit Appointment");

            // START OF THE FORM IN THE EDIT POPUP
            TextField patientField = new TextField(selected.patientProperty().get());
            patientField.setDisable(true); // disable the patient field since we don't want to change the patient of the appointment, if you want to change it, you can remove this line and add a dropdown for the patients in the edit popup	
            // datepicker and automatically set the value of the datepicker to the date of the appointment, so that the user can see the original date of the appointment in the datepicker, and also make it easier for the user to change the date of the appointment if needed
            // START OF THE DATEPICKER IN THE EDIT POPUP
            DatePicker datePicker = new DatePicker();
			datePicker.setValue(java.time.LocalDate.parse(selected.dateProperty().get())); // set the value of the datepicker to the date of the appointment, so that the user can see the original date of the appointment in the datepicker, and also make it easier for the user to change the date of the appointment if needed
			
			// START OF THE DROPDOWN FOR THE TIME IN THE EDIT POPUP
			ObservableList<String> timeOptions = FXCollections.observableArrayList("1:00 AM", "2:00 AM", "3:00 AM", "4:00 AM", "5:00 AM", "6:00 AM", "7:00 AM", "8:00 AM", "9:00 AM", "10:00 AM", "11:00 AM",
					"12:00 PM", "1:00 PM", "2:00 PM", "3:00 PM", "4:00 PM", "5:00 PM", "6:00 PM", "7:00 PM", "8:00 PM", "9:00 PM", "10:00 PM", "11:00 PM");
			ComboBox<String> timeField = new ComboBox<>(timeOptions);
			timeField.setPromptText("Select time");
			timeField.setValue(selected.timeProperty().get()); // set the value of the time field to the time of the appointment, so that the user can see the original time of the appointment in the time field, and also make it easier for the user to change the time of the appointment if needed
            
			// START OF THE DROPDOWN FOR THE DENTISTS IN THE EDIT POPUP
			ObservableList<String> dentistOptions = FXCollections.observableArrayList("Dr. Maria Santos", "Dr. Ricardo Reyes", "Dr. Elena Cruz");
			ComboBox<String> dentistField = new ComboBox<>(dentistOptions);
			dentistField.setPromptText("Select dentist");
			dentistField.setValue(selected.dentistProperty().get()); 
			

			// START OF THE DROPDOWN FOR THE SERVICES IN THE EDIT POPUP
			ObservableList<String> serviceOptions = FXCollections.observableArrayList("Dental Checkup", "Tooth Cleaning", "Tooth Extraction", "Dental Filling", "Root Canal", "Braces");
			ComboBox<String> serviceField = new ComboBox<>(serviceOptions);
			serviceField.setPromptText("Select service");
			serviceField.setValue(selected.serviceProperty().get());
            
            // START OF THE DROPDOWN FOR THE DENTISTS IN THE EDIT POPUP
            // set the status field to be a dropdown with the options "Cancelled", "Completed", "Pending"
            ObservableList<String> statusOptions = FXCollections.observableArrayList("Cancelled", "Completed", "Pending");
            ComboBox<String> statusField = new ComboBox<>(statusOptions);
            statusField.setPromptText("Select status");
            statusField.setValue(selected.statusProperty().get()); // set the value of the status field to the status of the appointment, so that the user can see the original status of the appointment in the status field, and also make it easier for the user to change the status of the appointment if needed
            
            VBox form = new VBox(10,
                    new Label("Patient"), patientField,
                    new Label("Date"), datePicker,
                    new Label("Time"), timeField,
                    new Label("Dentist"), dentistField,
                    new Label("Service"), serviceField,
                    new Label("Status"), statusField  // you can change this to whatever status you want, or you can add a dropdown for the status in the edit popup
            );

            Button saveBtn = new Button("Save");

            saveBtn.setOnAction(ev -> {
                int id = Integer.parseInt(selected.idProperty().get());
                
                // the first condition here is to check if the date picker has a value, if it has a value, then we use the value of the date picker, if it doesn't have a value, then we use the original date of the appointment, this is to prevent the date from being changed to null if the user doesn't change the date in the edit popup
                // it the datepicker is not null, then we use the value of the datepicker, if it is null, then we use the original date of the appointment, this is to prevent the date from being changed to null if the user doesn't change the date in the edit popup
                String date = datePicker.getValue() != null ? datePicker.getValue().toString() : selected.dateProperty().get(); // if the date is not changed, use the original date
                String time = timeField.getValue() != null ? timeField.getValue() : selected.timeProperty().get(); // if the time is not changed, use the original time
                String dentist = dentistField.getValue() != null ? dentistField.getValue().toLowerCase().replace("dr. ", "").replace(" ", "_") : selected.dentistProperty().get(); // if the dentist is not changed, use the original dentist, if the dentist is changed, then we convert it to the format that we need to store in the database (with underscores and without dr.); // if the dentist is not changed, use the original dentist
                String service = serviceField.getValue() != null ? serviceField.getValue() : selected.serviceProperty().get(); // if the service is not changed, use the original service
                // same as the date and time, if the status field is not null, then we use the value of the status field, if it is null, then we use the original status of the appointment, this is to prevent the status from being changed to null if the user doesn't change the status in the edit popup
                String status = statusField.getValue() != null ? statusField.getValue() : selected.statusProperty().get(); // if the status is not changed, use the original status
                
                Dao.updateBooking(id, date, time, dentist, service, status);
                
                // Call the new unified refresh method
                refreshTableData();
                popup.close();
            });

            VBox layout = new VBox(10, form, saveBtn);
            layout.setPadding(new Insets(15));

            popup.setScene(new Scene(layout, 300, 450));
            popup.showAndWait();
        });

        // ===== REMOVE =====
        removeBtn.setOnAction(e -> {
            Appointment selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) return;
            
            int id = Integer.parseInt(selected.idProperty().get());
            boolean confirmation = functions.applicationFunctions.showConfirmationDialog("Are you sure you want to delete this appointment?", "Confirm Deletion", "Delete Appointment");
            
            if(confirmation) {
                Dao.deleteBooking(id);
                functions.applicationFunctions.showDialog("Appointment deleted successfully!", "Deletion Successful", "Success", "INFORMATION");

                // Call the unified refresh method and update the statistics label
                refreshTableData(); 
                loadAppointmentCount(appointmentsToday); 
            }
        });

        // TABLE WRAPPER
        HBox tableWrapper = new HBox(table);
        tableWrapper.setAlignment(Pos.CENTER);
        HBox.setHgrow(table, Priority.ALWAYS);

        // ===== CENTER CONTENT =====
        VBox center = new VBox(15,
                reportsTitle,
                reportsBox,
                appTitle,
                pagination,
                btnBox
        );

        center.setPadding(new Insets(15));
        center.setAlignment(Pos.TOP_LEFT);
        center.setFillWidth(true);

        VBox.setVgrow(tableWrapper, Priority.ALWAYS);

        // SCROLLABLE CENTER
        ScrollPane scrollPane = new ScrollPane(center);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background:#e6e6e6;");

        root.setTop(topBar);
        root.setCenter(scrollPane);

        Scene dashboardScene = new Scene(root, 800, 500);

//        // ===== FILE MAINTENANCE =====
//        StackPane filePane = new StackPane(new Label("File Maintenance Page (Under Construction - Charles)"));
//        Scene fileScene = new Scene(filePane, 800, 500);

        // ===== LOGIN =====
        // also the dropdown for the file maintenance should be added here, so that when the user clicks on the file maintenance, it will open a new stage for the file maintenance, and the user can still see the dashboard in the background
        VBox loginPane = new VBox(10);
        loginPane.setPadding(new Insets(20));
        
        patients.setOnAction(e -> {
			try {
				new FileMaintenance_Patient().start(new Stage()); // since we don't want to close the dashboard, we open a new stage for file maintenance (stage means window)
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		});
        doctors.setOnAction(e -> {
            try {
                new FileMaintenance_Doctor().start(new Stage()); // since we don't want to close the dashboard, we open a new stage for file maintenance (stage means window)
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
        admins.setOnAction(e -> {
			try {
				new FileMaintenance_Admin().start(new Stage()); // since we don't want to close the dashboard, we open a new stage for file maintenance (stage means window)
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		});
        logoutBtn.setOnAction(e -> {
        	Stage s = (Stage) logoutBtn.getScene().getWindow();
			s.close();
			auth.Login.main(new String[0]);
        });

        stage.setScene(dashboardScene);
        stage.show();
    }
    
    
    // ===== PAGINATOR PAGE FACTORY =====
    private VBox createPage(int pageIndex) {
        int fromIndex = pageIndex * ROWS_PER_PAGE;
        int toIndex = Math.min(fromIndex + ROWS_PER_PAGE, appointments.size());
        table.setItems(FXCollections.observableArrayList(appointments.subList(fromIndex, toIndex)));

        VBox box = new VBox(table);
        VBox.setVgrow(table, Priority.NEVER);
        return box;
    }


    public static void main(String[] args) {
        launch();
    }

    public static class Appointment {
        private SimpleStringProperty ID, date, time, patient, dentist, service, status;

        public Appointment(int I, String d, String t, String p, String den, String s, String stat) {
        	ID = new SimpleStringProperty(String.valueOf(I));
            date = new SimpleStringProperty(d);
            time = new SimpleStringProperty(t);
            patient = new SimpleStringProperty(p);
            dentist = new SimpleStringProperty(den);
            service = new SimpleStringProperty(s);
            status = new SimpleStringProperty(stat);
        }
        
        
        // these functions are used to get the value of the properties
        public SimpleStringProperty idProperty() { return ID; }
        public SimpleStringProperty dateProperty() { return date; }
        public SimpleStringProperty timeProperty() { return time; }
        public SimpleStringProperty patientProperty() { return patient; }
        public SimpleStringProperty dentistProperty() { return dentist; }
        public SimpleStringProperty serviceProperty() { return service; }
        public SimpleStringProperty statusProperty() { return status; }
    }
}
