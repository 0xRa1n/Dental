package admin;

import javafx.application.Application;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import passanduser.Dbconnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import passanduser.Dao;
import model.User;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import admin.FileMaintenance_Patient.Patient;
import application.Main.App.DoctorAvailability;

public class FileMaintenance_Doctor extends Application {

    // Data model
    public static class Doctor {
        private final SimpleIntegerProperty no;
        private final SimpleStringProperty name;
        private final SimpleStringProperty username;
        private final SimpleStringProperty password;
        private final SimpleStringProperty email;
        

        public Doctor(int no, String name, String username, String password, String email) {
            this.no = new SimpleIntegerProperty(no);
            this.name = new SimpleStringProperty(name);
            this.username = new SimpleStringProperty(username);
            this.password = new SimpleStringProperty(password);
            this.email = new SimpleStringProperty(email);
        }

        public int getNo() { return no.get(); }
        public String getName() { return name.get(); }
        public String getPassword() { return password.get(); }
        public String getUsername() { return username.get(); }
        public String getEmail() { return email.get(); }

        public void setNo(int no) { this.no.set(no); }
        public void setName(String name) { this.name.set(name); }
        public void setPassword(String password) { this.password.set(password); }
        public void setUsername(String username) { this.username.set(username); }
        public void setEmail(String email) { this.email.set(email); }
    }

    private void populateTimes(ComboBox<String> box) {
        for (int i = 1; i <= 12; i++) box.getItems().add(String.format("%02d:00 AM", i));
        for (int i = 1; i <= 12; i++) box.getItems().add(String.format("%02d:00 PM", i));
    }

    private CheckBox createDayCheck(String name, DayOfWeek day, List<String> existingDays, Set<DayOfWeek> selectedDays) {
        CheckBox cb = new CheckBox(name);
        if (existingDays.contains(day.name())) {
            cb.setSelected(true);
            selectedDays.add(day);
        }
        cb.setOnAction(e -> {
            if (cb.isSelected()) selectedDays.add(day);
            else selectedDays.remove(day);
        });
        return cb;
    }

    private final ObservableList<Doctor> data = FXCollections.observableArrayList();
	private void loadDoctors() {
		data.clear();
        
        // Added 'id' to the SELECT statement
        String sql = "SELECT id, username, password, full_name, email FROM users WHERE role = 'doctor'";
        try (Connection con = Dbconnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            if (con != null) {
            	ResultSet rs = ps.executeQuery();
				while (rs.next()) {					
					int id = rs.getInt("id"); // Get the 'id' column
					String fullName = rs.getString("full_name");
					String username = rs.getString("username");
					String password = rs.getString("password");
					String email = rs.getString("email");

					data.add(new Doctor(id, fullName, username, password, email)); // Use count for No.
				}
            }
        } catch (Exception e) {
            System.out.println("❌ Failed to load doctors: " + e.getMessage());
        }
        
    }
    @Override
    public void start(Stage stage) {
        // Title
        Label title = new Label("Manage Doctors");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
        title.setMaxWidth(Double.MAX_VALUE);
        title.setAlignment(Pos.CENTER);
      
        loadDoctors(); // Load data from database
        
        
        // Table
        TableView<Doctor> table = new TableView<>(data);

        TableColumn<Doctor, Integer> colNo = new TableColumn<>("No.");
        colNo.setCellValueFactory(new PropertyValueFactory<>("no"));

        TableColumn<Doctor, String> colName = new TableColumn<>("Name");
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        
        TableColumn<Doctor, String> colUsername = new TableColumn<>("Username");
        colUsername.setCellValueFactory(new PropertyValueFactory<>("username"));

        TableColumn<Doctor, String> colPass = new TableColumn<>("Password");
        colPass.setCellValueFactory(new PropertyValueFactory<>("password"));
        
        TableColumn<Doctor, String> colEmail = new TableColumn<>("Email");
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));

        table.getColumns().addAll(colNo, colName, colPass, colUsername, colEmail);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        // Buttons
        Button addBtn = new Button("Add");
        Button editBtn = new Button("Edit");
        Button deleteBtn = new Button("Delete");

        HBox buttonBox = new HBox(10, addBtn, editBtn, deleteBtn);
        buttonBox.setAlignment(Pos.CENTER);

        // ADD
//        addBtn.setOnAction(e -> {
//            Dialog<Doctor> dialog = createDialog(null);
//            dialog.showAndWait().ifPresent(doc -> {
//                doc.setNo(data.size() + 1);
//                data.add(doc);
//            });
//        });

        addBtn.setOnAction(e -> {
            Dialog<Doctor> dialog = createDialog(null);
            dialog.showAndWait().ifPresent(doc -> {
                // Discard the manual calculation and dummy object
                // Force a complete refresh from the database to get the real ID
                loadDoctors(); 
                table.refresh();
            });
        });
        
        // EDIT
//        editBtn.setOnAction(e -> {
//            Doctor selected = table.getSelectionModel().getSelectedItem();
//            if (selected != null) {
//                Dialog<Doctor> dialog = createDialog(selected);
//                dialog.showAndWait().ifPresent(updated -> {
//                	selected.setUsername(updated.getUsername());
//                    selected.setName(updated.getName());
//                    selected.setPassword(updated.getPassword());
//                    selected.setEmail(updated.getEmail());
//                    table.refresh();
//                });
//            } else {
//                showAlert("Select a user to edit first.");
//            }
//        });
        editBtn.setOnAction(e -> {
            Doctor selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) {
                Dialog<Doctor> dialog = createDialog(selected);
                dialog.showAndWait().ifPresent(updated -> {
                    // Force a complete refresh from the database
                    loadDoctors();
                    table.refresh();
                });
            } else {
                showAlert("Select a user to edit first.");
            }
        });

        // DELETE
        deleteBtn.setOnAction(e -> {
            Doctor selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) {
                boolean confirmation = functions.applicationFunctions.showConfirmationDialog("Are you sure you want to delete this user?", "Confirm Deletion", "Delete User");
                if (confirmation) {
					Dao.deleteUser(selected.getNo()); // Use the 'no' property which is actually the 'id'
					data.remove(selected);
		        	   functions.applicationFunctions.showDialog("Appointment deleted successfully!", "Deletion Successful", "Success", "INFORMATION");
				}
            } else {
                showAlert("Select a user to delete first.");
            }
        });

        // Layout
        VBox root = new VBox(10, title, table, buttonBox);
        root.setStyle("-fx-padding: 10;");

        Scene scene = new Scene(root, 600, 500);
        stage.setTitle("User Management");
        stage.setScene(scene);
        stage.show();
    }

	    // Dialog for Add/Edit
	    private Dialog<Doctor> createDialog(Doctor existing) {
	        Dialog<Doctor> dialog = new Dialog<>();
	        dialog.setTitle(existing == null ? "Add Doctor" : "Edit Doctor");
	
	        // Core fields
	        TextField nameField = new TextField();
	        PasswordField passField = new PasswordField();
	        TextField emailField = new TextField();
	        TextField usernameField = new TextField();
	
	        // Schedule state tracking
	        // hashset to prevent duplicates
	        Set<DayOfWeek> selectedDays = new HashSet<>(); 
	        Set<LocalDate> blockedDates = new HashSet<>();
	        ComboBox<String> startTime = new ComboBox<>();
	        ComboBox<String> endTime = new ComboBox<>();
	        populateTimes(startTime);
	        populateTimes(endTime);
	        List<String> existingDaysList = new ArrayList<>();
	
	        if (existing != null) {
	            usernameField.setText(existing.getUsername()); 
	            nameField.setText(existing.getName());
	            passField.setText(existing.getPassword());
	            emailField.setText(existing.getEmail()); 
	            
	            // Cross-search existing schedule data
	            DoctorAvailability avail = Dao.getDoctorAvailability(existing.getUsername());
	            if (avail.blockedDates != null) blockedDates.addAll(avail.blockedDates); // This will add the existing blocked dates from the database into a set that will be used to display the blocked dates in the UI. So if the doctor already has certain dates marked as unavailable, those dates will be shown in the blocked dates section when editing.
	            existingDaysList.addAll(avail.recurringDays); // what this does is it adds the existing recurring days from the database into a list that will be used to pre-check the checkboxes in the UI. So if the doctor already has certain days marked as available, those checkboxes will be selected when editing.
	            
	            if (avail.startTime != null && !avail.startTime.isEmpty()) startTime.setValue(avail.startTime); // if start time exists, set it. Otherwise default to 10:00 AM
	            else startTime.setValue("10:00 AM");
	            
	            if (avail.endTime != null && !avail.endTime.isEmpty()) endTime.setValue(avail.endTime); // if end time exists, set it. Otherwise default to 07:00 PM
	            else endTime.setValue("07:00 PM");
	        } else {
	            startTime.setValue("10:00 AM");
	            endTime.setValue("07:00 PM");
	        }
	
	        // Core UI Grid
	        GridPane grid = new GridPane();
	        grid.setVgap(10);
	        grid.setHgap(10);
	        grid.add(new Label("Name:"), 0, 0);
	        grid.add(nameField, 1, 0);
	        grid.add(new Label("Username:"), 0, 1);
	        grid.add(usernameField, 1, 1);
	        grid.add(new Label("Password:"), 0, 2);
	        grid.add(passField, 1, 2);
	        grid.add(new Label("Email:"), 0, 3);
	        grid.add(emailField, 1, 3);
	
	        // Schedule UI Construction
	        Label scheduleLabel = new Label("Doctor Schedule Configuration");
	        scheduleLabel.setStyle("-fx-font-weight: bold; -fx-padding: 15 0 5 0;");
	
	        // Recurring Days
	        GridPane daysGrid = new GridPane();
	        daysGrid.setHgap(10);
	        daysGrid.setVgap(5);
	        daysGrid.add(createDayCheck("Mon", DayOfWeek.MONDAY, existingDaysList, selectedDays), 0, 0);
	        daysGrid.add(createDayCheck("Tue", DayOfWeek.TUESDAY, existingDaysList, selectedDays), 1, 0);
	        daysGrid.add(createDayCheck("Wed", DayOfWeek.WEDNESDAY, existingDaysList, selectedDays), 2, 0);
	        daysGrid.add(createDayCheck("Thu", DayOfWeek.THURSDAY, existingDaysList, selectedDays), 0, 1);
	        daysGrid.add(createDayCheck("Fri", DayOfWeek.FRIDAY, existingDaysList, selectedDays), 1, 1);
	        daysGrid.add(createDayCheck("Sat", DayOfWeek.SATURDAY, existingDaysList, selectedDays), 2, 1);
	        daysGrid.add(createDayCheck("Sun", DayOfWeek.SUNDAY, existingDaysList, selectedDays), 0, 2);
	
	        // Time Window
	        HBox timeBox = new HBox(10, new Label("Start:"), startTime, new Label("End:"), endTime);
	        timeBox.setAlignment(Pos.CENTER_LEFT);
	
	        // Blocked Dates
	        VBox blockedBox = new VBox(5);
	        DatePicker datePicker = new DatePicker();
	        Button addDateBtn = new Button("Add Blocked Date");
	        VBox blockedList = new VBox(2);
	        
	        // Populate existing blocked dates into the view
	        for (LocalDate d : blockedDates) {
	            blockedList.getChildren().add(new Label("• " + d.toString()));
	        }
	
	        addDateBtn.setOnAction(e -> {
	            LocalDate d = datePicker.getValue();
	            if (d != null && !blockedDates.contains(d)) {
	                blockedDates.add(d);
	                blockedList.getChildren().add(new Label("• " + d.toString()));
	            }
	        });
	        blockedBox.getChildren().addAll(new HBox(10, datePicker, addDateBtn), blockedList);
	
	        VBox layout = new VBox(10, grid, new Separator(), scheduleLabel, new Label("Recurring Days:"), daysGrid, new Label("Time Window:"), timeBox, new Label("Blocked Dates:"), blockedBox);
	        layout.setPadding(new javafx.geometry.Insets(10));
	        
	        // Wrap in ScrollPane to ensure UI fits within bounds
	        ScrollPane scrollPane = new ScrollPane(layout);
	        scrollPane.setFitToWidth(true);
	        scrollPane.setPrefViewportHeight(450);
	
	        dialog.getDialogPane().setContent(scrollPane);
	        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
	
	        dialog.setResultConverter(button -> {
	            if (button == ButtonType.OK) {
	                String name = nameField.getText();
	                String username = usernameField.getText();
	                String password = passField.getText();
	                String email = emailField.getText();
	                String finalStart = startTime.getValue();
	                String finalEnd = endTime.getValue();
	
	                if (existing != null) { 
	                    int id = existing.getNo();
	                    existing.setName(name);
	                    existing.setUsername(username);
	                    existing.setPassword(password);
	                    existing.setEmail(email);
	                    
	                    try {
	                        if (Dao.updateUser(id, name, username, password, email, "doctor")) {
	                            Dao.saveDoctorAvailability(username, selectedDays, finalStart, finalEnd, blockedDates);
	                            functions.applicationFunctions.showDialog("Doctor account and schedule updated successfully!", "Update Successful", "Success", "INFORMATION");
	                            return existing;
	                        }
	                    } catch (Exception e) {
	                        functions.applicationFunctions.showDialog("Error: " + e.getMessage(), "Database Error", "Error", "ERROR");
	                    }
	                } else { 
	                    User newUser = new User(username, email, password, name, "doctor");
	                    try {
	                        if (Dao.registerUser(newUser)) {
	                            Dao.saveDoctorAvailability(username, selectedDays, finalStart, finalEnd, blockedDates);
	                            functions.applicationFunctions.showDialog("Doctor account and schedule created successfully!", "Account Creation Successful", "Success", "INFORMATION");
	                            return new Doctor(0, name, username, password, email);
	                        }
	                    } catch (Exception e) {
	                        functions.applicationFunctions.showDialog("Error: " + e.getMessage(), "Database Error", "Error", "ERROR");
	                    }
	                }
	            }
	            return null;
	        });
	
	        return dialog;
	    }
    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}