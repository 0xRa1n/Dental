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

public class FileMaintenance_Patient extends Application {

    // Data model
    public static class Patient {
        private final SimpleIntegerProperty no;
        private final SimpleStringProperty name;
        private final SimpleStringProperty username;
        private final SimpleStringProperty password;
        private final SimpleStringProperty email;
        

        public Patient(int no, String name, String username, String password, String email) {
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


    private final ObservableList<Patient> data = FXCollections.observableArrayList();
	private void loadPatients() {
		data.clear();
        
        // Added 'id' to the SELECT statement
        String sql = "SELECT id, username, password, full_name, email FROM users WHERE role = 'patient'";
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

					data.add(new Patient(id, fullName, username, password, email)); // Use count for No.
				}
            }
        } catch (Exception e) {
            System.out.println("❌ Failed to load patients: " + e.getMessage());
        }
        
    }
    @Override
    public void start(Stage stage) {
        // Title
        Label title = new Label("Manage Patients");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
        title.setMaxWidth(Double.MAX_VALUE);
        title.setAlignment(Pos.CENTER);
      
        loadPatients(); // Load data from database
        
        
        // Table
        TableView<Patient> table = new TableView<>(data);

        TableColumn<Patient, Integer> colNo = new TableColumn<>("No.");
        colNo.setCellValueFactory(new PropertyValueFactory<>("no"));

        TableColumn<Patient, String> colName = new TableColumn<>("Name");
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        
        TableColumn<Patient, String> colUsername = new TableColumn<>("Username");
        colUsername.setCellValueFactory(new PropertyValueFactory<>("username"));

        TableColumn<Patient, String> colPass = new TableColumn<>("Password");
        colPass.setCellValueFactory(new PropertyValueFactory<>("password"));
        
        TableColumn<Patient, String> colEmail = new TableColumn<>("Email");
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
//            Dialog<Patient> dialog = createDialog(null);
//            dialog.showAndWait().ifPresent(doc -> {
//                doc.setNo(data.size() + 1);
//                data.add(doc);
//            });
//        });
        addBtn.setOnAction(e -> {
            Dialog<Patient> dialog = createDialog(null);
            dialog.showAndWait().ifPresent(doc -> {
                // Discard the manual calculation and dummy object
                // Force a complete refresh from the database to get the real ID
                loadPatients(); 
                table.refresh();
            });
        });

        // EDIT
//        editBtn.setOnAction(e -> {
//        	Patient selected = table.getSelectionModel().getSelectedItem();
//            if (selected != null) {
//                Dialog<Patient> dialog = createDialog(selected);
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
            Patient selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) {
                Dialog<Patient> dialog = createDialog(selected);
                dialog.showAndWait().ifPresent(updated -> {
                    // Force a complete refresh from the database
                    loadPatients();
                    table.refresh();
                });
            } else {
                showAlert("Select a user to edit first.");
            }
        });
        // DELETE
        deleteBtn.setOnAction(e -> {
        	Patient selected = table.getSelectionModel().getSelectedItem();
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
    private Dialog<Patient> createDialog(Patient existing) {
        Dialog<Patient> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "Add Patient" : "Edit Patient");

        TextField nameField = new TextField();
        PasswordField passField = new PasswordField();
        TextField emailField = new TextField();
        TextField usernameField = new TextField();

        if (existing != null) {
            usernameField.setText(existing.getUsername()); 
        	nameField.setText(existing.getName());
            passField.setText(existing.getPassword());
            emailField.setText(existing.getEmail()); 
            
        }

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


        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(button -> {
            if (button == ButtonType.OK) {
            	// reason behind this is that the add and edit functionalities are combined in one dialog, so we need to check if the admin is editing an existing entry or creating a new one. If the existing parameter is not null, it means that the admin is editing an existing entry, otherwise, they are creating a new one.
            	// determine if the admin selects on an already existing entry
            	if(existing != null) { // this means that the admin is editing an existing entry
            		
            		int id = existing.getNo(); // Get the 'id' from the existing Patient object
            		String name = nameField.getText();
					String username = usernameField.getText();
					String password = passField.getText();
					String email = emailField.getText();
					
					existing.setName(name);
					existing.setUsername(username);
					existing.setPassword(password);
					existing.setEmail(email);
					
					try {
						if(Dao.updateUser(id, name, username, password, email, "patient")) {
							functions.applicationFunctions.showDialog("Patient account updated successfully!", "Update Successful", "Success", "INFORMATION");
							return existing; // Return the updated Patient object, this will be seen in the main table and updated in the data list
						} else {
							functions.applicationFunctions.showDialog("Error updating patient!", "Error", "Error", "ERROR");
							return null;
						}
					} catch (Exception e) {
						e.printStackTrace();
						functions.applicationFunctions.showDialog("Error: " + e.getMessage(), "Database Error", "Error", "ERROR");
						return null;
					}
            		
            	} else { // otherwise, the admin is creating a new entry
            		String name = nameField.getText();
                	String username = usernameField.getText();
    				String password = passField.getText();
    				String email = emailField.getText();
    				
    				User newUser = new User(username, email, password, name, "patient");
    				
    				try {
    					if(Dao.registerUser(newUser)) {
    						functions.applicationFunctions.showDialog("Patient account created successfully!", "Account Creation Successful", "Success", "INFORMATION");
    						return new Patient(0, name, username, password, email); // Return a new Patient object (No. will be set later), this will be seen in the main table and added to the data list
    					} else {
    						functions.applicationFunctions.showDialog("Error registering patient!", "Error", "Error", "ERROR");
    						return null;
    					}
    				} catch (Exception e) {
    					e.printStackTrace();
    					functions.applicationFunctions.showDialog("Error: " + e.getMessage(), "Database Error", "Error", "ERROR");
    					return null;
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