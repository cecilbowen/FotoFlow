/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package player;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.File;
import java.util.ArrayList;
import javafx.scene.media.MediaPlayer;
import slide.Slide;

/**
 *
 * @author cgb0011
 * Flow so Far:
 * - hit load slideshow
 * - pick image folder for now (read creator export file eventually)
 * - load slideshow images into array
 * - (load music into array)
 * - open actual slideshow (separate) scene, replacing old one
 * - when "start slideshow" is hit, start slides (and music)
 * - bring up slideshow controls in same window (only appear when mouse in view)
 * 
 * TODO:
 * - crop FotoFlow.png image to better align button in Title
 * - center alignment of player controls
 * - resize player controls to fit in small window (optional)
 * - add in transitions DONE
 * - add in automatic slide changing (using custom interval) DONE
 * - add in music and (working) volume controls
 * - stop slideshow function
 * - play/pause slideshow function DONE
 * - stop window from (slightly) resizing after loading slideshow
 * 
 * PROBLEMS:
 * - when on manual interval, when pressing next/previous button fast, if the new slide
 *   has a crossfade transition, while the old slide had some wipe transition,
 *   the old slide could still be in process of "wiping" while crossfade is called. .
 * - wipe transitions work, but if window is resized during transition, old image will be partially on screen
 *   this also happens if you resize window after a transition but before moving to the next image
 */
public class Player extends Application {
    
    public static ArrayList<Slide> slides = new ArrayList<>(); //arraylist holding all slides (as Slide objects)
    public static ArrayList<MediaPlayer> music = new ArrayList<>(); //arraylist holding all songs (as MediaPlayer objects)
    
    @Override
    public void start(Stage stage) throws Exception {
        TitleController titleController = new TitleController();
        
        stage.setScene(new Scene(titleController));
        stage.setTitle("Fotoflow - Player");
        //stage.setWidth(300);
        //stage.setHeight(200);
        stage.show();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
    

    
}
