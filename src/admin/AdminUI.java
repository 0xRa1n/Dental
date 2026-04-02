package admin;

import javafx.application.Application;
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
    
    // function to read appointments from database and display it in the table
    
    private void loadAppointments() {
    	// clear the table first
    	table.getItems().clear();
    	
    	String sql = "SELECT id, date, serviceTime, username, dentist, dentalService FROM appointments";
    	
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
						
						appointments.add(new Appointment(id, date, time, patient, dentist, service));
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
						label.setText("Total patients: " + total);
					}
					
				} catch(Exception e) {
					System.out.println("❌ Failed to load patient count: " + e.getMessage());
				}
			} 
		} catch (Exception e) {
			System.out.println("❌ Failed to load patient count: " + e.getMessage());
		}
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

        Label logo = new Label("logo");
        logo.setMinWidth(80);
        logo.setAlignment(Pos.CENTER);
        logo.setStyle("-fx-background-color:#1f5f7a; -fx-text-fill:white; -fx-border-color:green;");

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

//        String centerStyle = "-fx-alignment: CENTER;";
//        idCol.setStyle(centerStyle);
//        dateCol.setStyle(centerStyle);
//        timeCol.setStyle(centerStyle);
//        patientCol.setStyle(centerStyle);
//        dentistCol.setStyle(centerStyle);
//        serviceCol.setStyle(centerStyle);

        table.getColumns().addAll(idCol, dateCol, timeCol, patientCol, dentistCol, serviceCol);

        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setMaxWidth(Double.MAX_VALUE);
        table.setMaxHeight(Double.MAX_VALUE);
        table.setStyle("-fx-border-color:green;");

        // ===== PAGINATOR =====
        Pagination pagination = new Pagination((int) Math.ceil(appointments.size() / (double)ROWS_PER_PAGE), 0);
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
            Appointment selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) return;

            Stage popup = new Stage();
            popup.initModality(Modality.APPLICATION_MODAL);
            popup.setTitle("Edit Appointment");

            TextField dateField = new TextField(selected.dateProperty().get());
            TextField timeField = new TextField(selected.timeProperty().get());
            TextField patientField = new TextField(selected.patientProperty().get());
            TextField dentistField = new TextField(selected.dentistProperty().get());
            TextField serviceField = new TextField(selected.serviceProperty().get());

            VBox form = new VBox(10,
                    new Label("Date"), dateField,
                    new Label("Time"), timeField,
                    new Label("Patient"), patientField,
                    new Label("Dentist"), dentistField,
                    new Label("Service"), serviceField
            );

            Button saveBtn = new Button("Save");

            saveBtn.setOnAction(ev -> {
                selected.dateProperty().set(dateField.getText());
                selected.timeProperty().set(timeField.getText());
                selected.patientProperty().set(patientField.getText());
                selected.dentistProperty().set(dentistField.getText());
                selected.serviceProperty().set(serviceField.getText());
                table.refresh();
                popup.close();
            });

            VBox layout = new VBox(10, form, saveBtn);
            layout.setPadding(new Insets(15));

            popup.setScene(new Scene(layout, 300, 400));
            popup.showAndWait();
        });

        // ===== REMOVE =====
        removeBtn.setOnAction(e -> {
            Appointment selected = table.getSelectionModel().getSelectedItem();
            // get the id of the selected appointment
            if (selected == null) return;
            int id = Integer.parseInt(selected.idProperty().get());
            boolean confirmation = functions.applicationFunctions.showConfirmationDialog("Are you sure you want to delete this appointment?", "Confirm Deletion", "Delete Appointment");
           if(confirmation) {
        	   functions.applicationFunctions.showDialog("Appointment deleted successfully!", "Deletion Successful", "Success", "INFORMATION");
        	   Dao.deleteBooking(id);
//        	   loadAppointments(); // refresh the table after deletion
               pagination.setPageCount((int) Math.ceil(appointments.size() / (double)ROWS_PER_PAGE));
               pagination.setPageFactory(this::createPage);
               table.refresh();
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
        private SimpleStringProperty ID, date, time, patient, dentist, service;

        public Appointment(int I, String d, String t, String p, String den, String s) {
        	ID = new SimpleStringProperty(String.valueOf(I));
            date = new SimpleStringProperty(d);
            time = new SimpleStringProperty(t);
            patient = new SimpleStringProperty(p);
            dentist = new SimpleStringProperty(den);
            service = new SimpleStringProperty(s);
        }
        
        
        // these functions are used to get the value of the properties
        public SimpleStringProperty idProperty() { return ID; }
        public SimpleStringProperty dateProperty() { return date; }
        public SimpleStringProperty timeProperty() { return time; }
        public SimpleStringProperty patientProperty() { return patient; }
        public SimpleStringProperty dentistProperty() { return dentist; }
        public SimpleStringProperty serviceProperty() { return service; }
    }
}