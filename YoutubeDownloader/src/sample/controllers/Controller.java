package sample.controllers;

import javafx.concurrent.Task;
import javafx.css.PseudoClass;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import sample.FileManager;

import java.io.IOException;

public class Controller {
    @FXML private TextField linkTextField;
    @FXML private RadioButton audioRadioButton;
    @FXML private Button downloadButton;
    @FXML private Text statusText;
    @FXML private ImageView settingsButton;

    final PseudoClass errorClass = PseudoClass.getPseudoClass("error");
    private FileManager mFileManager;
    private String downloadedFile;
    private boolean playlist = false;

    Stage currentStage;

    public void setCurrentStage(Stage stage){
        this.currentStage = stage;
    }

    public Controller() {
        mFileManager = new FileManager("Agervold");
    }

    @FXML private void initialize() {
        downloadButton.setOnAction(e -> {
            if (linkTextField.getText().isEmpty()) {
                linkTextField.pseudoClassStateChanged(errorClass, true);
                linkTextField.setPromptText("Must enter a link");
            } else {
                linkTextField.pseudoClassStateChanged(errorClass, false);
                linkTextField.setPromptText("Link");
                Parent parent = new VBox();
                Scene scene = new Scene(parent);
                try {
                    //setLoading(true);
                    scene = new Scene(FXMLLoader.load(getClass().getResource("/fxml/load.fxml")));
                    currentStage.setScene(scene);
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                final Scene finalScene = scene;
                Task task = new Task<Void>() {
                    @Override
                    protected Void call() throws Exception {
                        String link = linkTextField.getText();
                        if (link.equals("")) link = "https://www.youtube.com/watch?v=eH4F1Tdb040";
                        boolean audio = audioRadioButton.isSelected();
                        if (link.contains("playlist")) playlist = true;

                        try {
                            downloadedFile = mFileManager.download(link, audio, finalScene);
                        } catch (IOException ioe) {
                            ioe.printStackTrace();
                        }
                        return null;
                    }
                };
                task.setOnSucceeded(event -> {
                    Parent root;
                    try {
                        FXMLLoader fxmlLoader;
                        //if (!playlist) {
                            fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/completed.fxml"));
                            //TODO: Fix, won't work because downloadedFile is only the name of the playlist, not the entire path
                            CompletedController controller = new CompletedController(currentStage, downloadedFile);
                            fxmlLoader.setController(controller);
                        /*} else {
                            fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/main.fxml"));
                            fxmlLoader.setController(this);
                        }*/
                        root = fxmlLoader.load();
                        currentStage.setScene(new Scene(root));
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                });
                new Thread(task).start();
            }
        });

        settingsButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                Parent root;
                try {
                    FXMLLoader fxmlLoader;
                    fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/settings.fxml"));

                    SettingsController settingsController = new SettingsController();
                    settingsController.setCurrentStage(currentStage);
                    fxmlLoader.setController(settingsController);

                    root = fxmlLoader.load();
                    currentStage.setScene(new Scene(root));
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        });
    }

    /*
    private void setLoading(boolean load) throws IOException {
        Parent fxml;
        if (load) {
            fxml = FXMLLoader.load(getClass().getResource("/fxml/load.fxml"));
        } else {
            fxml = FXMLLoader.load(getClass().getResource("/fxml/main.fxml"));
        }
        Scene scene = new Scene(fxml);
        currentStage.setScene(scene);
    }
    */
}
