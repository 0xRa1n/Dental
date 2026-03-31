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

import java.io.*;

public class FileMaintenance extends Application {

    // Data model
    public static class Doctor {
        private final SimpleIntegerProperty no;
        private final SimpleStringProperty name;
        private final SimpleStringProperty password;

        public Doctor(int no, String name, String password) {
            this.no = new SimpleIntegerProperty(no);
            this.name = new SimpleStringProperty(name);
            this.password = new SimpleStringProperty(password);
        }

        public int getNo() { return no.get(); }
        public String getName() { return name.get(); }
        public String getPassword() { return password.get(); }

        public void setNo(int no) { this.no.set(no); }
        public void setName(String name) { this.name.set(name); }
        public void setPassword(String password) { this.password.set(password); }
    }

    private final ObservableList<Doctor> data = FXCollections.observableArrayList();

    @Override
    public void start(Stage stage) {
        // Title
        Label title = new Label("Manage Doctors");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
        title.setMaxWidth(Double.MAX_VALUE);
        title.setAlignment(Pos.CENTER);

        // Table
        TableView<Doctor> table = new TableView<>(data);

        TableColumn<Doctor, Integer> colNo = new TableColumn<>("No.");
        colNo.setCellValueFactory(new PropertyValueFactory<>("no"));

        TableColumn<Doctor, String> colName = new TableColumn<>("Name");
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<Doctor, String> colPass = new TableColumn<>("Password");
        colPass.setCellValueFactory(new PropertyValueFactory<>("password"));

        table.getColumns().addAll(colNo, colName, colPass);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Buttons
        Button addBtn = new Button("Add");
        Button editBtn = new Button("Edit");
        Button deleteBtn = new Button("Delete");
        Button saveBtn = new Button("Save");

        HBox buttonBox = new HBox(10, addBtn, editBtn, deleteBtn, saveBtn);
        buttonBox.setAlignment(Pos.CENTER);

        // ADD
        addBtn.setOnAction(e -> {
            Dialog<Doctor> dialog = createDialog(null);
            dialog.showAndWait().ifPresent(doc -> {
                doc.setNo(data.size() + 1);
                data.add(doc);
            });
        });

        // EDIT
        editBtn.setOnAction(e -> {
            Doctor selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) {
                Dialog<Doctor> dialog = createDialog(selected);
                dialog.showAndWait().ifPresent(updated -> {
                    selected.setName(updated.getName());
                    selected.setPassword(updated.getPassword());
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
                data.remove(selected);

                // Re-number
                for (int i = 0; i < data.size(); i++) {
                    data.get(i).setNo(i + 1);
                }
            } else {
                showAlert("Select a user to delete first.");
            }
        });

        // SAVE
        saveBtn.setOnAction(e -> {
            try (PrintWriter writer = new PrintWriter(new FileWriter("doctors.txt"))) {
                for (Doctor d : data) {
                    writer.println(d.getNo() + " | " + d.getName() + " | " + d.getPassword());
                }
                showAlert("Data saved to doctors.txt!");
            } catch (IOException ex) {
                showAlert("Error saving file!");
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

        TextField nameField = new TextField();
        PasswordField passField = new PasswordField();

        if (existing != null) {
            nameField.setText(existing.getName());
            passField.setText(existing.getPassword());
        }

        GridPane grid = new GridPane();
        grid.setVgap(10);
        grid.setHgap(10);
        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Password:"), 0, 1);
        grid.add(passField, 1, 1);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(button -> {
            if (button == ButtonType.OK) {
                return new Doctor(0, nameField.getText(), passField.getText());
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