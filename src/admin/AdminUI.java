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
import auth.Login;
import admin.FileMaintenance;
public class AdminUI extends Application {

    private TableView<Appointment> table = new TableView<>();

    @Override
    public void start(Stage stage) {
        stage.setTitle("CARES Dashboard");

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
        MenuItem services = new MenuItem("Services");

        manageBtn.getItems().addAll(patients, doctors, admins, services);

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

        Label totalPatients = new Label("Total patients: 120");
        Label appointmentsToday = new Label("Appointments Today: 25");
        Label more = new Label("And so on....");

        for (Label lbl : new Label[]{totalPatients, appointmentsToday, more}) {
            lbl.setMaxWidth(Double.MAX_VALUE);
            lbl.setAlignment(Pos.CENTER);
        }

        reportsBox.getChildren().addAll(
                totalPatients,
                appointmentsToday,
                more
        );

        // ===== TABLE =====
        Label appTitle = new Label("Appointments");
        appTitle.setStyle("-fx-padding:5; -fx-font-weight:bold;");
        appTitle.setMaxWidth(Double.MAX_VALUE);
        appTitle.setAlignment(Pos.CENTER);

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

        dateCol.setCellFactory(centerCell);
        timeCol.setCellFactory(centerCell);
        patientCol.setCellFactory(centerCell);
        dentistCol.setCellFactory(centerCell);
        serviceCol.setCellFactory(centerCell);

        String centerStyle = "-fx-alignment: CENTER;";
        dateCol.setStyle(centerStyle);
        timeCol.setStyle(centerStyle);
        patientCol.setStyle(centerStyle);
        dentistCol.setStyle(centerStyle);
        serviceCol.setStyle(centerStyle);

        table.getColumns().addAll(dateCol, timeCol, patientCol, dentistCol, serviceCol);

        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setMaxWidth(Double.MAX_VALUE);
        table.setMaxHeight(Double.MAX_VALUE);
        table.setStyle("-fx-border-color:green;");

        table.getItems().add(
                new Appointment("3/19/26", "9:00 AM", "SiPatient", "Dr. Ewan", "Cleaning")
        );

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
            if (selected != null) {
                table.getItems().remove(selected);
            }
        });

        // TABLE WRAPPER
        HBox tableWrapper = new HBox(table);
        tableWrapper.setAlignment(Pos.CENTER);
        HBox.setHgrow(table, Priority.ALWAYS);

        // ===== CENTER CONTENT =====
        VBox center = new VBox(15,
                reportsTitle,   // OUTSIDE
                reportsBox,     // BOX CONTENT
                appTitle,
                tableWrapper,
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
        VBox loginPane = new VBox(10);
        loginPane.setPadding(new Insets(20));
//
        patients.setOnAction(e -> {
			FileMaintenance.main(new String[0]);
        });
//        doctors.setOnAction(e -> stage.setScene(fileScene));
//        admins.setOnAction(e -> stage.setScene(fileScene));
//        services.setOnAction(e -> stage.setScene(fileScene));
//        
        logoutBtn.setOnAction(e -> {
        	Stage s = (Stage) logoutBtn.getScene().getWindow();
			s.close();
			auth.Login.main(new String[0]);
        });

        stage.setScene(dashboardScene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }

    public static class Appointment {
        private SimpleStringProperty date, time, patient, dentist, service;

        public Appointment(String d, String t, String p, String den, String s) {
            date = new SimpleStringProperty(d);
            time = new SimpleStringProperty(t);
            patient = new SimpleStringProperty(p);
            dentist = new SimpleStringProperty(den);
            service = new SimpleStringProperty(s);
        }

        public SimpleStringProperty dateProperty() { return date; }
        public SimpleStringProperty timeProperty() { return time; }
        public SimpleStringProperty patientProperty() { return patient; }
        public SimpleStringProperty dentistProperty() { return dentist; }
        public SimpleStringProperty serviceProperty() { return service; }
    }
}