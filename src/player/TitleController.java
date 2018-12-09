/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package player;

import java.awt.FileDialog;
import java.awt.Frame;
import java.io.BufferedReader;
import static player.Player.slides;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.Stage;
import javax.swing.JFileChooser;
import static player.Player.music;
import slide.Slide;

/**
 *
 * @author cgb0011
 */
public class TitleController extends VBox {
    
    private int interval;
    
    private boolean manual;
    
    private Media me;
    private MediaPlayer mp;    
    
    @FXML
    private Button btnLoadSlideshow;
    
    @FXML
    private ImageView imgViewLogo;
    
    public TitleController() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("Title.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);
        
        try {
            fxmlLoader.load();            
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }
    
    public static File getDirectory() {
        JFileChooser picker = new JFileChooser();
        picker.setCurrentDirectory(new java.io.File("."));
        picker.setDialogTitle("Choose Image Directory");
        picker.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        picker.setAcceptAllFileFilterUsed(false);

        if (picker.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            //System.out.println("getCurrentDirectory(): " + picker.getCurrentDirectory());
            System.out.println("getDirectory() - " + picker.getSelectedFile());
            return picker.getSelectedFile();
        } else {
            //System.out.println("No Selection ");
            return null;
        }        
    }
    
    public File getSlideshowFile() {
        FileDialog dialog = new FileDialog((Frame)null, "Choose Slideshow File");
        dialog.setMode(FileDialog.LOAD);
        dialog.setFile("*.flo");
        dialog.setVisible(true);
        String filename = dialog.getFile();
        String path = dialog.getDirectory() + dialog.getFile();
        dialog.dispose(); //fixes thread still running bug FINALLY
        System.out.println(path);
        System.out.println("getSlideshowFile() - " + filename);
        
        if (!filename.endsWith(".flo")) path = "null";
        
        File file = new File(path);
        return file;
    }
    
    //Loads image files into arraylist and setup slideshow options
    //returns true if successful, false otherwise
    public boolean loadSlides(File file) {
        if (!file.exists()) {
            System.out.println("Null slideshow file.");
            return false;
        }
        
        int counter = 0; //for testing purposes. number of images added to arraylist
        int mCounter = 0; //same as above but for music
        
        //Parse through slideshow export file and import slides
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line, oldLine;
            boolean checkForTrans = false;
            int section = 0;
            int autoTimer, transition;
            File path = null; //used here for slide images and sound files
            while ((line = br.readLine()) != null) {
                //Line by line
                //System.out.println(line + " / section: " + section);
                if (section == 0) { //look for Options
                    if (line.contains("Options")) {
                        section += 1;
                        //continue;
                    }
                } else if (section == 1) { //look for TransitionDuration
                    if (line.contains("TransitionDuration")) {
                        String[] tempLine = line.split(" ");
                        interval = Integer.parseInt(tempLine[1]);
                        section += 1;
                        //continue;
                    }
                } else if (section == 2) { //look for Manual
                    if (line.contains("Manual")) {
                        String[] tempLine = line.split(" ");
                        manual = Boolean.parseBoolean(tempLine[1]);
                        section += 1;
                        //continue;
                    }                   
                } else if (section == 3) { //look for Slides
                    if (line.equals("Slides")) {
                        continue;
                    }
                    
                    if (line.equals("Sounds")) {
                        section += 1;
                        continue;
                    }
                    
                    if (!checkForTrans) {
                        path = new File(line);
                        System.out.println("image exists? " + path.exists());
                        //oldLine = line;
                        checkForTrans = true;
                        continue;
                    } else {
                        //System.out.println(">> " + line);
                        transition = Integer.parseInt(line);
                        Slide slide = new Slide(path);
                        slide.trans = transition;
                        slides.add(slide); //store slides (image files) in global arraylist
                        counter++;
                        System.out.println(counter + " - loadSlides() - Added " + path.getName() + " to slide list with transition [" + transition +"].");
                        checkForTrans = false;
                        //continue;
                    }
                } else if (section == 4) { //look for Sounds
                    if (!(line.contains(".wav") || line.contains(".aif"))) { //reached end of file
                        break; 
                    }
                    String tline = line.trim();
                    path = new File(tline);
                    if (path.exists()) {
                        //System.out.println(path.toURI().toString());  
                        me = new Media(path.toURI().toString());
                        mp = new MediaPlayer(me);
                        music.add(mp);
                        mCounter++;
                        System.out.println(mCounter + " - loadSlides() - Added " + path.getName() + " to music list.");
                        //continue;
                    } else {
                        System.out.println(line + "does not exist as music");
                    }
                }
            }

        } catch (FileNotFoundException ex) {        
            Logger.getLogger(TitleController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(TitleController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return true;        
    }
    
    public static boolean loadSlidesOLD(File directory) {
        if (directory == null) {
            System.out.println("Null directory.");
            return false;
        }
        
        //File dir = new File(directory.toString());

        //Image types to look for
        String[] types = new String[]{
            "jpg", "jpeg"
        };
        
        //Get image types above from folder
        FilenameFilter typesFilter = new FilenameFilter() {

            @Override
            public boolean accept(final File dir, final String name) {
                for (final String ext : types) {
                    if (name.endsWith("." + ext)) {
                        //System.out.println(">> " + name);
                        return (true);
                    }
                }
                return (false);
            }
        };
        
        int counter = 0; //for testing purposes. number of images added to arraylist
        if (directory.isDirectory()) { //if directory exists
            for (final File f : directory.listFiles(typesFilter)) { //go through jpegs found
                Slide slide = new Slide(f);
                slides.add(slide); //store slides (image files) in global arraylist
                counter++;
                System.out.println(counter + " - loadSlides() - Added " + f.getName() + " to slide list.");
            }
        } else {
            System.out.println("Invalid directory.");
            return false;
        }
        
        return true;
    }    
    
    @FXML
    private void loadSlideshow_button(ActionEvent event) {
        System.out.println("Load slideshow button clicked.");
        
        if (!loadSlides(getSlideshowFile())) {
            System.out.println("Failed to load slideshow. - loadSlideshow_button()");
            return;
        }
        
        //Gets parent stage (from Player.java start() method)
        Stage stage = Stage.class.cast(Control.class.cast(event.getSource()).getScene().getWindow());
        //Stage stage = (Stage)btnLoadSlideshow.getScene().getWindow();
        
        SlideshowController slideshowController = new SlideshowController(stage);
        
        slideshowController.setInterval(interval);
        slideshowController.setManual(manual);
        System.out.println("Set slide auto-interval slide uptime to " + interval + " seconds.");
        System.out.println("Is auto-interval? " + !manual);
        
        //stage.setFullScreen(true);        
        stage.setScene(new Scene(slideshowController));
        //stage.setTitle("Fotoflow - Player");
        //stage.setWidth(300);
        //stage.setHeight(200);
        //stage.show();
    }
}
