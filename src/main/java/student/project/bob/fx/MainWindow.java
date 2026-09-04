package student.project.bob.fx;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import student.project.bob.Bob;

/**
 * Controller for the main GUI.
 */
public class MainWindow extends AnchorPane {
    @FXML
    private ScrollPane scrollPane;

    @FXML
    private VBox dialogContainer;

    @FXML
    private TextField userInput;

    @FXML
    private Button sendButton;

    private Bob bob;

    private Image userImage = new Image(this.getClass().getResourceAsStream("/images/Orange.jpeg"));
    private Image bobImage = new Image(this.getClass().getResourceAsStream("/images/Apple.jpeg"));

    /**
     * Binds the dialog container's scroll position to its content height.
     */
    @FXML
    public void initialize() {
        scrollPane.vvalueProperty().bind(dialogContainer.heightProperty());
    }

    /**
     * Injects the Bob instance used to process commands.
     *
     * @param bob Bob instance used by this window
     */
    public void setBob(Bob bob) {
        this.bob = bob;
    }

    /**
     * Creates two dialog boxes, one echoing user input and the other containing Bob's reply and then appends them to
     * the dialog container. Clears the user input after processing.
     */
    @FXML
    private void handleUserInput() {
        String input = userInput.getText();

        String response = bob.getResponse(input);
        dialogContainer
                .getChildren()
                .addAll(DialogBox.getUserDialog(input, userImage), DialogBox.getBobDialog(response, bobImage));
        userInput.clear();
    }
}
