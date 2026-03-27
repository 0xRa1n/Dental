package Application;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.time.LocalDate;

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
            
            header.getChildren().addAll(logo, title, spacer, btnProfile, btnLogout);
            mainLayout.setTop(header);
        }

        public void showDashboardView() {
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
            Button btnResched = new Button("Reschedule");

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
                dentistBox.setValue(existingAppointment.getDentist());
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
            btnCancel.getStyleClass().add("btn-danger");

            btnConfirm.setOnAction(e -> {
                if(datePicker.getValue() != null && serviceBox.getValue() != null) {
                    if (existingAppointment != null) {
                        appointmentList.remove(existingAppointment);
                    }
                    appointmentList.add(new Appointment(
                        datePicker.getValue().toString(),
                        timeBox.getValue(),
                        dentistBox.getValue(),
                        serviceBox.getValue()
                    ));
                    showDashboardView();
                }
            });

            btnCancel.setOnAction(e -> showDashboardView());

            formActions.getChildren().addAll(btnConfirm, btnCancel);
            formBox.getChildren().addAll(formTitle, grid, formActions);
            
            StackPane container = new StackPane(formBox);
            container.setPadding(new Insets(30));
            mainLayout.setCenter(container);
        }

        public static class Appointment {
            private String date, time, dentist, service;
            public Appointment(String date, String time, String dentist, String service) {
                this.date = date; this.time = time; this.dentist = dentist; this.service = service;
            }
            public String getDate() { return date; }
            public String getTime() { return time; }
            public String getDentist() { return dentist; }
            public String getService() { return service; }
        }
    }

    public static void main(String[] args) { Application.launch(App.class, args); }
}