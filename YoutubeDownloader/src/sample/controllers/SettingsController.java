package sample.controllers;

import javafx.css.PseudoClass;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import sample.FileManager;

import java.io.*;
import java.util.HashMap;
import java.util.Properties;
import java.util.Set;

public class SettingsController {
    @FXML private HBox playlistHBox;
    @FXML private HBox directoryMusicHBox;
    @FXML private HBox directoryVideosHBox;
    @FXML private CheckBox createDirectoryCheckBox;
    @FXML private Text directoryChooserMusic;
    @FXML private Text directoryChooserVideos;
    @FXML private ImageView returnButton;

    final static String USER_NAME = System.getProperty("user.name");

    //private static final String pathMusic = "C:\\Users\\"+USER_NAME+"\\Music";
    //private static final String pathVideos = "C:\\Users\\"+USER_NAME+"\\Videos";
    private static final String PATH_MUSIC = "path_music";
    private static final String PATH_VIDEOS = "path_videos";

    private String mCreateDirectoryForPlaylist;
    private String mPathMusic;
    private String mPathVideos;

    final PseudoClass errorClass = PseudoClass.getPseudoClass("error");
    private FileManager mFileManager;
    private String downloadedFile;
    private boolean playlist = false;

    Stage currentStage;

    public void setCurrentStage(Stage stage){
        this.currentStage = stage;
    }

    public SettingsController() {
        mFileManager = new FileManager("Agervold");
    }

    @FXML private void initialize() {
        //boolean createDir = readProperties("createDirectoryForPlaylist").equals("true");

        Tooltip.install(playlistHBox, new Tooltip("Create a separate directory when downloading playlist"));
        Tooltip.install(directoryMusicHBox, new Tooltip("Directory where music files will be saved"));
        Tooltip.install(directoryVideosHBox, new Tooltip("Directory where video files will be saved"));

        //Preferences prefs = Preferences.systemNodeForPackage(SettingsController.class);
        //String pathMusic = prefs.get(PATH_MUSIC, "C:\\Users\\"+USER_NAME+"\\Music");

        //String pathMusic = readProperties("pathMusic");
        //String pathVideos = readProperties("pathVideos");
        Properties prop = readProperties();
        mCreateDirectoryForPlaylist = prop.getProperty("createDirectoryForPlaylist");
        mPathMusic = prop.getProperty("pathMusic");
        mPathVideos = prop.getProperty("pathVideos");

        if (!mCreateDirectoryForPlaylist.equals("true")) createDirectoryCheckBox.setSelected(false);

        //directoryChooserMusic.setText("C:\\Users\\"+USER_NAME+"\\Music");
        directoryChooserMusic.setText(mPathMusic);
        //directoryChooserVideos.setText("C:\\Users\\"+USER_NAME+"\\Videos");
        directoryChooserVideos.setText(mPathVideos);

        DirectoryChooser directoryChooser = new DirectoryChooser();

        createDirectoryCheckBox.setOnAction(event -> {
            mCreateDirectoryForPlaylist = createDirectoryCheckBox.isSelected() ? "true" : "false";
            writeProperties(createMap());
        });

        directoryChooserMusic.setOnMouseClicked(event -> {
            directoryChooser.setInitialDirectory(new File(mPathMusic));
            //directoryChooser.setInitialDirectory(new File(pathMusic));
            File dir = directoryChooser.showDialog(currentStage);

            if (dir != null) {
                directoryChooserMusic.setText(dir.getAbsolutePath());
                //writeProperties("pathMusic", dir.getAbsolutePath());
                mPathMusic = dir.getAbsolutePath();
                writeProperties(createMap());
            }
        });

        directoryChooserVideos.setOnMouseClicked(event -> {
            directoryChooser.setInitialDirectory(new File(mPathVideos));
            File dir = directoryChooser.showDialog(currentStage);

            if (dir != null) {
                directoryChooserVideos.setText(dir.getAbsolutePath());
                //writeProperties("pathVideos", dir.getAbsolutePath());
                mPathVideos = dir.getAbsolutePath();
                writeProperties(createMap());
            }
        });

        returnButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
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
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        });
    }

    private HashMap<String, String> createMap() {
        HashMap<String, String> map = new HashMap<>();
        map.put("createDirectoryForPlaylist", mCreateDirectoryForPlaylist);
        map.put("pathMusic", mPathMusic);
        map.put("pathVideos", mPathVideos);

        return map;
    }

    private boolean writeProperties(String key, String value) {
        Properties prop = new Properties();
        OutputStream output = null;

        try {
            output = new FileOutputStream("config.properties");

            prop.setProperty(key, value);
            //prop.setProperty("pathMusic", "C:\\Users\\"+USER_NAME+"\\Music");

            prop.store(output, null);

            System.out.println("Properties saved. " + key + ": " + value);
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
