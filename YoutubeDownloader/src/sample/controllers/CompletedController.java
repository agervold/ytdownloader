package sample.controllers;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.*;
import java.util.HashMap;
import java.util.Properties;
import java.util.Set;

public class CompletedController {

    @FXML private VBox completedContainer;

    @FXML private ImageView completedCheckmark;
    @FXML private Text completedMessageName;

    @FXML private Text fileName;
    @FXML private HBox playButton;
    @FXML private ImageView playButtonIcon;
    @FXML private MediaView MediaView;
    @FXML private HBox explorerButton;
    @FXML private HBox anotherButton;

    //TODO: Set path correctly, not hardcoded
    final static String PATH = "C:\\Users\\Agervold\\Music\\YoutubeDownloader";
    final static String USER_NAME = System.getProperty("user.name");
    private Stage currentStage;
    private MediaPlayer mMediaPlayer;
    private File mFile;
    private boolean mPlaying = false;
    private boolean mPlaylist;
    private String fileExtension;

    public CompletedController(Stage currentStage, String downloadedFile) {
        this.currentStage = currentStage;

        mFile = new File(downloadedFile);
        mPlaylist = mFile.isDirectory();
        if (!mPlaylist) {
            Media media = new Media(mFile.toURI().toString());
            mMediaPlayer = new MediaPlayer(media);
        }
    }

    @FXML private void initialize() {
        Properties prop = readProperties();

        completedMessageName.setText(String.format("%s has successfully been downloaded", mFile.getName()));
        if (!mPlaylist) {
            MediaView.setMediaPlayer(mMediaPlayer);
            MediaView.setManaged(false);
            MediaView.setVisible(false);
            String fileName2 = mFile.getName();
            //fileName.setText(fileName2);

            String extension = getFileExtension(fileName2);

            fileExtension = extension;
            final String finalExtension = extension;
            playButton.setOnMouseClicked(event2 -> {
                if (finalExtension.equals("mp4")) {
                    MediaView.setManaged(true);
                    MediaView.setVisible(true);
                }
                if (!mPlaying) {
                    //MediaView mediaView = new MediaView(mMediaPlayer);
                    //mediaView.setFitHeight(200);
                    //mediaView.setFitWidth(355);
                    //completedContainer.getChildren().add(mediaView);
                    mMediaPlayer.play();
                    playButtonIcon.setImage(new Image("/images/ic_pause_circle_outline_black_48dp_2x.png"));
                    fileName.setText("Pause");
                    mPlaying = true;
                } else {
                    mMediaPlayer.pause();
                    playButtonIcon.setImage(new Image("/images/ic_play_circle_outline_black_48dp_2x.png"));
                    fileName.setText("Play");
                    mPlaying = false;
                }
            });
        } else {
            playButton.setVisible(false);
        }

        new Timeline(new KeyFrame(Duration.seconds(2), event -> {
            completedCheckmark.setVisible(false);
            completedContainer.setVisible(true);
        })).play();

        explorerButton.setOnMouseClicked(event -> {
            try {
                String location;
                if (mPlaylist) {
                    fileExtension = getFileExtension(mFile.listFiles()[0].toString());
                }

                if (fileExtension.equals("mp3")) location = prop.getProperty("pathMusic");
                else location = prop.getProperty("pathVideos");

                if (mPlaylist) location += "\\" + mFile.getName(); //todo: idk, might not work
                Runtime.getRuntime().exec("explorer.exe /," + location);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        anotherButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                Parent root;
                try {
                    FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/main.fxml"));

                    Controller controller = new Controller();
                    controller.setCurrentStage(currentStage);
                    fxmlLoader.setController(controller);
                    root = fxmlLoader.load();
                    currentStage.setScene(new Scene(root));
                    mMediaPlayer.stop();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        });
    }

    private String getFileExtension(String filename) {
        int i = filename.lastIndexOf('.');
        if (i > 0) {
            return filename.substring(i + 1);
        }
        return null;
    }

    private boolean writeProperties(HashMap<String, String> map) {
        Properties prop = new Properties();
        OutputStream output = null;

        try {
            output = new FileOutputStream("config.properties");

            Set keys = map.keySet();

            for (Object key : keys) {
                System.out.println("Properties saved. " + key + ": " + map.get(key));
                prop.setProperty((String) key, map.get(key));
            }

            prop.store(output, null);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            if (output != null) {
                try {
                    output.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return true;
    }

    private Properties readProperties() {
        Properties prop = new Properties();
        InputStream input = null;

        try {
            input = new FileInputStream("config.properties");

            prop.load(input);

            //return prop.getProperty(key);
            return prop;

            //System.out.println("pathMusic");
            //System.out.println("pathVideos");
        } catch (FileNotFoundException fnfe) {
            // If config file doesn't exist, create it.
            HashMap<String, String> map = new HashMap<>();
            map.put("createDirectoryForPlaylist", "true");
            map.put("pathMusic", "C:\\Users\\"+USER_NAME+"\\Music");
            map.put("pathVideos", "C:\\Users\\"+USER_NAME+"\\Videos");

            writeProperties(map);

            //writeProperties("createDirectoryForPlaylist", "true");
            //writeProperties("pathMusic", "C:\\Users\\"+USER_NAME+"\\Music");
            //writeProperties("pathVideos", "C:\\Users\\"+USER_NAME+"\\Videos");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }
}
