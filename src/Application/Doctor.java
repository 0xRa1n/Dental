package Application;

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

public class Doctor extends Application {

    @Override
    public void start(Stage stage) {
        Label logo = new Label("logo");
        logo.setStyle("-fx-background-color: #1a5276; -fx-text-fill: white; -fx-padding: 15 25; -fx-font-weight: bold;");
        
        Label brand = new Label("CARES");
        brand.setStyle("-fx-font-weight: bold; -fx-font-size: 16;");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Button btnLogout = new Button("Logout");
        btnLogout.setStyle("-fx-background-color: white; -fx-border-color: #2e7d32;");
        btnLogout.setOnAction(e -> stage.close());

        HBox header = new HBox(15, logo, brand, spacer, btnLogout);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(0, 15, 0, 0));
        header.setStyle("-fx-border-color: #2e7d32; -fx-border-width: 0 0 1 0;");

        TableView<Appointment> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPrefHeight(200);
        table.setStyle("-fx-border-color: #2e7d32; -fx-background-color: white;");

        String[] colNames = {"Patient", "Time", "Service", "Status"};
        for (String name : colNames) {
            TableColumn<Appointment, String> col = new TableColumn<>(name);
            col.setCellValueFactory(new PropertyValueFactory<>(name.toLowerCase()));
            table.getColumns().add(col);
        }

        ObservableList<Appointment> data = FXCollections.observableArrayList(
            new Appointment("Ewanko", "10:00 AM", "Cleaning", "Pending")
        );
        table.setItems(data);

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
                if (s.equals("Pending")) selected.setStatus("Completed");
                else if (s.equals("Completed")) selected.setStatus("Canceled");
                else selected.setStatus("Pending...");
                
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
        private final SimpleStringProperty patient, time, service, status;
        public Appointment(String p, String t, String ser, String sta) {
            this.patient = new SimpleStringProperty(p);
            this.time = new SimpleStringProperty(t);
            this.service = new SimpleStringProperty(ser);
            this.status = new SimpleStringProperty(sta);
        }
        public String getPatient() { return patient.get(); }
        public String getTime() { return time.get(); }
        public String getService() { return service.get(); }
        public String getStatus() { return status.get(); }
        public void setStatus(String s) { this.status.set(s); }
    }

    public static void main(String[] args) { launch(args); }
}