package functions;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

public class applicationFunctions {
    public static void showDialog(String alertType, String content, String title, String header) {
        Alert.AlertType type; // declares a variable to hold the alert type (we can think of the Alert.AlertType as a category of alert, like INFORMATION, WARNING, ERROR, etc.), and the name type is just a variable name that we can use to refer to the alert type later in the code. 
        try {
            type = Alert.AlertType.valueOf(alertType.toUpperCase()); // e.g. if alertType is "warning", it will be converted to "WARNING" and then matched to Alert.AlertType.WARNING
        } catch (IllegalArgumentException e) {
            // Default to INFORMATION if the string doesn't match a valid type
            type = Alert.AlertType.INFORMATION;
        }

        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
    public static boolean showConfirmationDialog(String content, String title, String header) {
    	Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setContentText(content);
        alert.showAndWait();
        
        if(alert.getResult() == ButtonType.OK) {
        	return true;
		} else {
			return false; // User cancelled the delete action
		}
	}
}
