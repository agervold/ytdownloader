/*
TODO:
Actually use settings

Keep read/writeProperties methods in one place, instead of copying around.

Fix:
-Videos from playlist
-Will only download the first 100 videos from playlist
 */

package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import sample.controllers.CompletedController;
import sample.controllers.Controller;
import sample.controllers.SettingsController;

public class Main extends Application {

    @Override
    public void start(Stage window) throws Exception{
        window.setTitle("Youtube Downloader - fwra");
        if (true) { //Main
            FXMLLoader myLoader = new FXMLLoader(getClass().getResource("/fxml/main.fxml"));
            myLoader.setController(new Controller());

            Parent mainFXML = myLoader.load();

            Controller controller = myLoader.getController();

            controller.setCurrentStage(window);

            Scene mainScene = new Scene(mainFXML);
            window.setScene(mainScene);
        } else if (false) { //Loading
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/load.fxml"));
            window.setScene(new Scene(root));
        } else if (false) { //Completed
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/completed.fxml"));

            //CompletedController controller = new CompletedController(window, "C:\\Users\\Agervold\\Music\\YoutubeDownloader\\Anatu - Bleach.mp3");
            CompletedController controller = new CompletedController(window, "C:\\Users\\Agervold\\Music\\YoutubeDownloader\\Robin Schulz - Sugar (feat. Francesco Yates) (OFFICIAL MUSICVIDEO).mp3");
            fxmlLoader.setController(controller);
            Parent root = fxmlLoader.load();
            window.setScene(new Scene(root));
        } else if (true) { //Settings
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/settings.fxml"));

            SettingsController controller = new SettingsController();
            controller.setCurrentStage(window);
            fxmlLoader.setController(controller);
            Parent root = fxmlLoader.load();
            window.setScene(new Scene(root));
        }

        window.setMinHeight(300);
        window.setMinWidth(500);
        window.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
