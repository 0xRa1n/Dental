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

import java.time.DayOfWeek;
import java.time.LocalDate;

public class AdminUI extends Application {

    private TableView<Appointment> table = new TableView<>();
    private ObservableList<Appointment> appointments = FXCollections.observableArrayList();
    private static final int ROWS_PER_PAGE = 15;
    
    // Elevate pagination to class scope
    private Pagination pagination;
    
    private void loadAppointments() {
        appointments.clear();
        String sql = "SELECT id, username, date, serviceDate, serviceTime, dentist, status, dentalService FROM appointments";
        try (Connection con = Dbconnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            if (con != null) {
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        int id = rs.getInt("id");
                        String date;
                        String serviceDate;
                        
                        try {
                            java.sql.Timestamp timestamp = rs.getTimestamp("date");
                            date = timestamp != null ? new java.sql.Date(timestamp.getTime()).toString() : LocalDate.now().toString();
                        } catch (Exception e) {
                            date = rs.getString("date");
                        }
                        
                        try {
                            java.sql.Timestamp timestamp = rs.getTimestamp("serviceDate");
                            serviceDate = timestamp != null ? new java.sql.Date(timestamp.getTime()).toString() : LocalDate.now().toString();
                        } catch (Exception e) {
                            serviceDate = rs.getString("serviceDate");
                        }
                        
                        String time = rs.getString("serviceTime");
                        String patient = rs.getString("username");
                        String dentist = rs.getString("dentist");
                        String service = rs.getString("dentalService");
                        String status = rs.getString("status");

                        appointments.add(new Appointment(id, date, serviceDate, time, patient, dentist, service, status));
                    }
                } catch (Exception e) {
                    System.out.println("❌ Failed to load appointments: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            System.out.println("❌ Failed to load appointments: " + e.getMessage());
            e.printStackTrace();
        }
    }


    private int countAppointmentsByRangeAndStatus(LocalDate fromInclusive, LocalDate toExclusive, String status) {
        String fromDate = fromInclusive.toString();
        String toDate = toExclusive.toString();
        
        // Use serviceDate instead of date
        String sql = "SELECT COUNT(*) FROM appointments WHERE date >= ? AND date < ? AND status = ?";
        try (Connection con = Dbconnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, fromDate);
            ps.setString(2, toDate);
            ps.setString(3, status);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int count = rs.getInt(1);
                    System.out.println("DEBUG: Found " + count + " appointments between " + fromDate + " and " + toDate + " with status '" + status + "'");
                    return count;
                }
            }
        } catch (Exception e) {
            System.out.println("❌ Failed to count appointments for status '" + status + "' in range " + fromDate + " to " + toDate);
            e.printStackTrace();
        }
        return 0;
    }

    private void loadDailyAndWeeklyCounts(
    	    Label dailyCompleted, Label dailyPending,
    	    Label weeklyCompleted, Label weeklyPending) {

    	    LocalDate today = LocalDate.now();
    	    LocalDate tomorrow = today.plusDays(1);

    	    LocalDate weekStart = today.with(DayOfWeek.MONDAY);
    	    LocalDate weekEnd = weekStart.plusWeeks(1);

    	    // Daily counts: only TODAY's serviceDate
    	    int dailyComp = countAppointmentsByRangeAndStatus(today, tomorrow, "Completed");
    	    int dailyPend = countAppointmentsByRangeAndStatus(today, tomorrow, "Pending");
    	    
    	    // Weekly counts: from Monday to Sunday (end of current week), excluding today (already counted in daily)
    	    int weeklyComp = countAppointmentsByRangeAndStatus(weekStart, today, "Completed") + 
    	                     countAppointmentsByRangeAndStatus(tomorrow, weekEnd, "Completed");
    	    int weeklyPend = countAppointmentsByRangeAndStatus(weekStart, today, "Pending") + 
    	                     countAppointmentsByRangeAndStatus(tomorrow, weekEnd, "Pending");

    	    dailyCompleted.setText("Completed Appointments: " + dailyComp);
    	    dailyPending.setText("Pending Appointments: " + dailyPend);
    	    weeklyCompleted.setText("Completed Appointments: " + weeklyComp);
    	    weeklyPending.setText("Pending Appointments: " + weeklyPend);
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
        stage.setHeight(800);
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
        MenuItem archives = new MenuItem("Archives");

        manageBtn.getItems().addAll(patients, doctors, admins, archives);

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

     // ===== REPORTS ROW (3 CARDS) =====
        HBox reportsRow = new HBox(25);
        reportsRow.setAlignment(Pos.CENTER);
        reportsRow.setMaxWidth(Double.MAX_VALUE);

        // Totals card (dynamic from DB)
        Label totalsTitle = new Label("Totals");

        Label totalAccounts = new Label("Accounts: Loading...");
        loadPatientCount(totalAccounts);

        Label totalAppointments = new Label("Appointments: Loading...");
        loadAppointmentCount(totalAppointments);

        // Daily card
        Label dailyTitle = new Label("Daily");
        Label dailyCompleted = new Label("Completed Appointments: Loading...");
        Label dailyIncomplete = new Label("Pending Appointments: Loading...");

        // Weekly card
        Label weeklyTitle = new Label("Weekly");
        Label weeklyCompleted = new Label("Completed Appointments: Loading...");
        Label weeklyIncomplete = new Label("Pending Appointments: Loading...");

        // Apply daily/weekly DB counts here
        loadDailyAndWeeklyCounts(
            dailyCompleted, dailyIncomplete,
            weeklyCompleted, weeklyIncomplete
        );

        VBox totalsCard = new VBox(8, totalsTitle, totalAccounts, totalAppointments);
        totalsCard.setPadding(new Insets(12));
        totalsCard.setAlignment(Pos.CENTER);
        totalsCard.setPrefWidth(290);
        totalsCard.setStyle("-fx-border-color:green; -fx-border-width:1.5;");

        VBox dailyCard = new VBox(8, dailyTitle, dailyCompleted, dailyIncomplete);
        dailyCard.setPadding(new Insets(12));
        dailyCard.setAlignment(Pos.CENTER);
        dailyCard.setPrefWidth(290);
        dailyCard.setStyle("-fx-border-color:green; -fx-border-width:1.5;");

        VBox weeklyCard = new VBox(8, weeklyTitle, weeklyCompleted, weeklyIncomplete);
        weeklyCard.setPadding(new Insets(12));
        weeklyCard.setAlignment(Pos.CENTER);
        weeklyCard.setPrefWidth(290);
        weeklyCard.setStyle("-fx-border-color:green; -fx-border-width:1.5;");

        // Center labels
        for (Label lbl : new Label[]{
                totalAccounts, totalAppointments,
                dailyCompleted, dailyIncomplete,
                weeklyCompleted, weeklyIncomplete
        }) {
            lbl.setMaxWidth(Double.MAX_VALUE);
            lbl.setAlignment(Pos.CENTER);
        }

        reportsRow.getChildren().addAll(totalsCard, dailyCard, weeklyCard);

        // ===== TABLE =====
        Label appTitle = new Label("Appointments");
        appTitle.setStyle("-fx-padding:5; -fx-font-weight:bold;");
        appTitle.setMaxWidth(Double.MAX_VALUE);
        appTitle.setAlignment(Pos.CENTER);
        
        TableColumn<Appointment, String> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(data -> data.getValue().idProperty());

        TableColumn<Appointment, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(data -> data.getValue().dateProperty());
        
     // Add serviceDate column to the table
        TableColumn<Appointment, String> serviceDateCol = new TableColumn<>("Service Date");
        serviceDateCol.setCellValueFactory(data -> data.getValue().serviceDateProperty());

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
        serviceDateCol.setCellFactory(centerCell);
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

     // Add to columns list (after dateCol)
        table.getColumns().addAll(idCol, dateCol, serviceDateCol, timeCol, patientCol, dentistCol, serviceCol, statusCol);

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
         // In the editBtn.setOnAction section, replace the form with:
            TextField dateField = new TextField(selected.dateProperty().get());
            dateField.setDisable(true); // Booking date is read-only

            DatePicker serviceDatePicker = new DatePicker();
            serviceDatePicker.setValue(java.time.LocalDate.parse(selected.serviceDateProperty().get()));

			
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
                    new Label("Booking Date"), dateField,
                    new Label("Service Date"), serviceDatePicker,
                    new Label("Patient"), patientField,
                    new Label("Time"), timeField,
                    new Label("Dentist"), dentistField,
                    new Label("Service"), serviceField,
                    new Label("Status"), statusField
            );
            
            Button saveBtn = new Button("Save");

            saveBtn.setOnAction(ev -> {
                int id = Integer.parseInt(selected.idProperty().get());
                String date = selected.dateProperty().get(); // Keep original booking date
                String serviceDate = serviceDatePicker.getValue() != null ? serviceDatePicker.getValue().toString() : selected.serviceDateProperty().get();
                String time = timeField.getValue() != null ? timeField.getValue() : selected.timeProperty().get();
                String dentist = dentistField.getValue() != null ? dentistField.getValue().toLowerCase().replace("dr. ", "").replace(" ", "_") : selected.dentistProperty().get();
                String service = serviceField.getValue() != null ? serviceField.getValue() : selected.serviceProperty().get();
                String status = statusField.getValue() != null ? statusField.getValue() : selected.statusProperty().get();
                
                // Verify that serviceDate is being used correctly
                System.out.println("DEBUG: Updating appointment " + id + " with serviceDate: " + serviceDate);
                
                Dao.updateBooking(id, serviceDate, time, dentist, service, status);
                
                refreshTableData();
                loadAppointmentCount(totalAppointments);
                loadDailyAndWeeklyCounts(dailyCompleted, dailyIncomplete, weeklyCompleted, weeklyIncomplete);

                popup.close();
            });


            VBox layout = new VBox(10, form, saveBtn);
            layout.setPadding(new Insets(15));

            popup.setScene(new Scene(layout, 300, 550));
            popup.showAndWait();
        });

        // ===== REMOVE =====
        removeBtn.setOnAction(e -> {
            Appointment selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) return;

            int id = Integer.parseInt(selected.idProperty().get());
            boolean confirmation = functions.applicationFunctions.showConfirmationDialog(
                    "Are you sure you want to delete this appointment?",
                    "Confirm Deletion",
                    "Delete Appointment"
            );

            if (confirmation) {
                boolean deleted = Dao.deleteBooking(id);
                if (deleted) {
                    functions.applicationFunctions.showDialog(
                            "Appointment deleted successfully!",
                            "Deletion Successful",
                            "Success",
                            "INFORMATION"
                    );

                    refreshTableData();
                    loadAppointmentCount(totalAppointments);
                    loadDailyAndWeeklyCounts(dailyCompleted, dailyIncomplete, weeklyCompleted, weeklyIncomplete);
                } else {
                    functions.applicationFunctions.showDialog(
                            "Deletion failed. Check console logs for details.",
                            "Deletion Error",
                            "Error",
                            "ERROR"
                    );
                }
            }
        });

        // TABLE WRAPPER
        HBox tableWrapper = new HBox(table);
        tableWrapper.setAlignment(Pos.CENTER);
        HBox.setHgrow(table, Priority.ALWAYS);

        // ===== CENTER CONTENT =====
        VBox center = new VBox(15,
                reportsTitle,
                reportsRow,
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
        archives.setOnAction(e -> {
            try {
                new FileMaintenance_Archives().start(new Stage());
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
        private SimpleStringProperty ID, date, serviceDate, time, patient, dentist, service, status;

        public Appointment(int I, String d, String sd, String t, String p, String den, String s, String stat) {
            ID = new SimpleStringProperty(String.valueOf(I));
            date = new SimpleStringProperty(d);
            serviceDate = new SimpleStringProperty(sd);
            time = new SimpleStringProperty(t);
            patient = new SimpleStringProperty(p);
            dentist = new SimpleStringProperty(den);
            service = new SimpleStringProperty(s);
            status = new SimpleStringProperty(stat);
        }

        public SimpleStringProperty idProperty() { return ID; }
        public SimpleStringProperty dateProperty() { return date; }
        public SimpleStringProperty serviceDateProperty() { return serviceDate; }
        public SimpleStringProperty timeProperty() { return time; }
        public SimpleStringProperty patientProperty() { return patient; }
        public SimpleStringProperty dentistProperty() { return dentist; }
        public SimpleStringProperty serviceProperty() { return service; }
        public SimpleStringProperty statusProperty() { return status; }
    }

}
