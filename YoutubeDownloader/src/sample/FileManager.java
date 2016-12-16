package sample;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.scene.Scene;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.text.Text;
import javafx.util.Duration;
import jdk.nashorn.internal.ir.annotations.Ignore;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.*;

//TODO: Playlist doesn't work with settings

public class FileManager {

    //Temp
    final static String USER_NAME = System.getProperty("user.name");

    private String mMusicPath;
    private Text mVideoName;

    public FileManager(String username) {
        mMusicPath = "C:\\Users\\"+username+"\\Music\\YoutubeDownloader\\";
    }

    private HttpClient httpclient;

    public String download(String link, boolean audio, Scene scene) throws IOException {
        System.out.println("Downloading " + link);

        httpclient = HttpClients.createDefault();

        if (link.contains("playlist")) {
            HttpPost httppost = new HttpPost(link);

            HttpResponse response = httpclient.execute(httppost);
            HttpEntity entity = response.getEntity();
            mVideoName = (Text) scene.lookup("#VideoName");

            if (entity != null) {
                String body = EntityUtils.toString(entity);
                Document doc = Jsoup.parse(body);

                String playlistTitle = doc.getElementsByClass("pl-header-title").get(0).text();
                Elements videos = doc.getElementsByClass("pl-video");

                Properties properties = readProperties();
                //String mPathMusic = properties.getProperty("pathMusic");
                String savePath;
                if (audio) {
                    savePath = properties.getProperty("pathMusic");
                } else {
                    savePath = properties.getProperty("pathVideos");
                }

                //ProgressIndicator progressIndicator = (ProgressIndicator) scene.lookup("#StatusProgressIndicator");
                ProgressBar progressBar = (ProgressBar) scene.lookup("#PlaylistProgressBar");
                Text CounterText = (Text) scene.lookup("#CounterText");

                //progressIndicator.setVisible(false);
                progressBar.setVisible(true);

                int playlistSize = videos.size();

                System.out.println("Playlist length: " + videos.size());
                System.out.println("Each video is " + 1f / videos.size());
                float downloadCounter = 0f;
                int downloadCounter2 = 0;

                String createDirectory = properties.getProperty("createDirectoryForPlaylist");
                if (createDirectory.equals("true")) {
                    //File directory = new File(mMusicPath+playlistTitle);
                    File directory = new File(savePath+"\\"+playlistTitle);
                    directory.mkdir();
                } else playlistTitle = "";

                for (Element video : videos) { // for video in playlist
                    //progressBar.setProgress(downloadCounter / playlistSize);

                    Timeline timeline = new Timeline();
                    KeyValue keyValue = new KeyValue(progressBar.progressProperty(), (downloadCounter / playlistSize));
                    KeyFrame keyFrame = new KeyFrame(new Duration(500), keyValue);
                    timeline.getKeyFrames().add(keyFrame);
                    timeline.play();

                    CounterText.setText(String.format("%d/%d", downloadCounter2, playlistSize));
                    downloadCounter++;
                    downloadCounter2++;

                    String videoId = video.attr("data-video-id");
                    download2("https://www.youtube.com/watch?v=" + videoId, audio, playlistTitle);
                }

                //String downloadLink = doc.select(".download").attr("href");

                //return saveFile(downloadLink);
                return savePath + "\\" + playlistTitle;
            }
        } else {
            //ProgressBar progressBar = (ProgressBar) scene.lookup("#PlaylistProgressBar");
            ProgressIndicator progressIndicator = (ProgressIndicator) scene.lookup("#StatusProgressIndicator");
            Text CounterText = (Text) scene.lookup("#CounterText");
            mVideoName = (Text) scene.lookup("#VideoName");

            //progressBar.setVisible(false);
            progressIndicator.setVisible(true);
            CounterText.setVisible(false);

            return download2(link, audio, "");
        }
        return null;
    }

    private String download2(String link, boolean audio, String playlistTitle) throws IOException {
        link = URLEncoder.encode(link, "UTF-8");

        HttpPost httppost = createHttpPost(link, audio);

        HttpResponse response = httpclient.execute(httppost);
        HttpEntity entity = response.getEntity();

        if (entity != null) {
            if (audio) {
                String body = EntityUtils.toString(entity);
                Document doc = Jsoup.parse(body);
                String downloadLink = doc.select(".download").attr("href");

                return saveFile(downloadLink, playlistTitle);
            } else {
                HttpPost httppostVideo = createHttpPost(link, false);

                HttpResponse responseVideo = httpclient.execute(httppostVideo);
                HttpEntity entityVideo = responseVideo.getEntity();

                if (entityVideo != null) {
                    String body = EntityUtils.toString(entityVideo);
                    Document doc = Jsoup.parse(body);
                    String downloadLink = doc.select(".download").get(1).attr("href");

                    return saveFile(downloadLink, playlistTitle);
                }
            }
        }
        return null;
    }

    public HttpPost createHttpPost(String link, boolean audio) throws UnsupportedEncodingException {
        HttpPost httppost = new HttpPost("http://offliberty.com/off03.php");

        List<NameValuePair> params = createParams(link, audio);
        httppost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
        return httppost;
    }

    public List<NameValuePair> createParams(String link, boolean audio) {
        List<NameValuePair> params = new ArrayList<>(2);
        params.add(new BasicNameValuePair("track", link));
        if (audio) {
            params.add(new BasicNameValuePair("refext", "http%3A%2F%2Foffliberty.com%2F"));
        } else {
            params.add(new BasicNameValuePair("video_file", "1"));
        }
        return params;
    }

    public String saveFile(String downloadLink, String playlistName) throws IOException {
        URL url = new URL(downloadLink);

        HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
        int responseCode = urlConn.getResponseCode();

        if (responseCode == HttpURLConnection.HTTP_OK) {

            String fileName = "";
            String disposition = urlConn.getHeaderField("Content-Disposition");
            String contentType = urlConn.getContentType();
            int contentLength = urlConn.getContentLength();

            if (disposition != null) {
                // extracts file name from header field
                int index = disposition.indexOf("filename=");
                if (index > 0) {
                    fileName = disposition.substring(index + 10,
                            disposition.length() - 1);
                }
            } else {
                // extracts file name from URL
                fileName = downloadLink.substring(downloadLink.lastIndexOf("/") + 1,
                        downloadLink.length());
            }

            fileName = fileName.replace(" (Official Video)", "").replace(" - from YouTube", "").replaceAll(":", "").replace("\\", "");

            System.out.println("Content-Type = " + contentType);
            System.out.println("Content-Disposition = " + disposition);
            System.out.println("Content-Length = " + contentLength);
            System.out.println("fileName = " + fileName);

            mVideoName.setText("Downloading: " + fileName);

            Properties properties = readProperties();
            //String mPathMusic = properties.getProperty("pathMusic");
            String savePath;
            if (contentType.equals("audio/mpeg")) {
                savePath = properties.getProperty("pathMusic");
            } else {
                savePath = properties.getProperty("pathVideos");
            }

            // opens input stream from the HTTP connection
            InputStream inputStream = urlConn.getInputStream();
            //String saveFilePath = mMusicPath + playlistName + "\\" + fileName;

            String saveFilePath = savePath;
            if (!playlistName.equals("")) saveFilePath += "\\" + playlistName;
            saveFilePath += "\\" + fileName;

            //String saveFilePath = savePath + playlistName + "\\" + fileName;

            // opens an output stream to save into file
            FileOutputStream outputStream = new FileOutputStream(saveFilePath);

            int bytesRead;
            byte[] buffer = new byte[4096];
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            outputStream.close();
            inputStream.close();

            System.out.println("File downloaded");
            return saveFilePath;
        } else {
            System.out.println("No file to download. Server replied HTTP Code: " + responseCode);
            return null;
        }
    }

    //TEMP
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
