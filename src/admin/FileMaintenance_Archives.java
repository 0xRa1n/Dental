// File: admin/FileMaintenance_Archives.java
package admin;

import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import passanduser.Dbconnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class FileMaintenance_Archives extends Application {

    private final TableView<ArchivedAppointment> table = new TableView<>();
    private final ObservableList<ArchivedAppointment> data = FXCollections.observableArrayList();

    @Override
    public void start(Stage stage) {
        stage.setTitle("Archived Appointments");

        TableColumn<ArchivedAppointment, String> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(v -> v.getValue().idProperty());

        TableColumn<ArchivedAppointment, String> appointmentIdCol = new TableColumn<>("Appointment ID");
        appointmentIdCol.setCellValueFactory(v -> v.getValue().appointmentIdProperty());

        TableColumn<ArchivedAppointment, String> usernameCol = new TableColumn<>("Username");
        usernameCol.setCellValueFactory(v -> v.getValue().usernameProperty());

        TableColumn<ArchivedAppointment, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(v -> v.getValue().dateProperty());

        TableColumn<ArchivedAppointment, String> timeCol = new TableColumn<>("Service Time");
        timeCol.setCellValueFactory(v -> v.getValue().serviceTimeProperty());

        TableColumn<ArchivedAppointment, String> dentistCol = new TableColumn<>("Dentist");
        dentistCol.setCellValueFactory(v -> v.getValue().dentistProperty());

        TableColumn<ArchivedAppointment, String> serviceCol = new TableColumn<>("Dental Service");
        serviceCol.setCellValueFactory(v -> v.getValue().dentalServiceProperty());

        TableColumn<ArchivedAppointment, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(v -> v.getValue().statusProperty());

        TableColumn<ArchivedAppointment, String> notesCol = new TableColumn<>("Notes");
        notesCol.setCellValueFactory(v -> v.getValue().notesProperty());

        table.getColumns().addAll(
                idCol, appointmentIdCol, usernameCol, dateCol, timeCol,
                dentistCol, serviceCol, statusCol, notesCol
        );
        table.setItems(data);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        loadArchivedAppointments();

        VBox root = new VBox(10, new Label("Deleted Appointments Archive"), table);
        root.setPadding(new Insets(12));

        stage.setScene(new Scene(root, 1100, 500));
        stage.show();
    }

    private void loadArchivedAppointments() {
        data.clear();
        String sql = "SELECT id, appointment_id, username, date, serviceTime, dentist, dentalService, status, notes " +
                     "FROM deleted_appointments ORDER BY id DESC";

        try (Connection con = Dbconnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                data.add(new ArchivedAppointment(
                        rs.getInt("id"),
                        rs.getInt("appointment_id"),
                        rs.getString("username"),
                        rs.getString("date"),
                        rs.getString("serviceTime"),
                        rs.getString("dentist"),
                        rs.getString("dentalService"),
                        rs.getString("status"),
                        rs.getString("notes")
                ));
            }
        } catch (Exception e) {
            System.out.println("❌ Failed to load archived appointments: " + e.getMessage());
        }
    }

    public static class ArchivedAppointment {
        private final SimpleStringProperty id;
        private final SimpleStringProperty appointmentId;
        private final SimpleStringProperty username;
        private final SimpleStringProperty date;
        private final SimpleStringProperty serviceTime;
        private final SimpleStringProperty dentist;
        private final SimpleStringProperty dentalService;
        private final SimpleStringProperty status;
        private final SimpleStringProperty notes;

        public ArchivedAppointment(int id, int appointmentId, String username, String date, String serviceTime,
                                   String dentist, String dentalService, String status, String notes) {
            this.id = new SimpleStringProperty(String.valueOf(id));
            this.appointmentId = new SimpleStringProperty(String.valueOf(appointmentId));
            this.username = new SimpleStringProperty(username == null ? "" : username);
            this.date = new SimpleStringProperty(date == null ? "" : date);
            this.serviceTime = new SimpleStringProperty(serviceTime == null ? "" : serviceTime);
            this.dentist = new SimpleStringProperty(dentist == null ? "" : dentist);
            this.dentalService = new SimpleStringProperty(dentalService == null ? "" : dentalService);
            this.status = new SimpleStringProperty(status == null ? "" : status);
            this.notes = new SimpleStringProperty(notes == null ? "" : notes);
        }

        public SimpleStringProperty idProperty() { return id; }
        public SimpleStringProperty appointmentIdProperty() { return appointmentId; }
        public SimpleStringProperty usernameProperty() { return username; }
        public SimpleStringProperty dateProperty() { return date; }
        public SimpleStringProperty serviceTimeProperty() { return serviceTime; }
        public SimpleStringProperty dentistProperty() { return dentist; }
        public SimpleStringProperty dentalServiceProperty() { return dentalService; }
        public SimpleStringProperty statusProperty() { return status; }
        public SimpleStringProperty notesProperty() { return notes; }
    }
}
