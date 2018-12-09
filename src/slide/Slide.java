/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package slide;

import java.io.File;
import javafx.scene.image.Image;


/**
 *
 * @author louis
 */
public class Slide {
    public File file; //image file
    public int trans; //transition type (0-5): NONE, LEFT, RIGHT, UP, DOWN, FADE
    public String name;
    public Image image = null;
    public String path;
    public String link; //absolute file path used for importing
    public String transition;
    Slide next;
    Slide previous;
    public int id;
    int time; //transition duration (seconds)
    
    //used in player
    public Slide(File file) {
        this.file = file;
        this.trans = 0;
        this.time = 0;
        this.transition = "0";
    }
    
    public int getTime() {
        return this.time;
    }

    Slide(String name){
       this.name = name; 
       this.trans = 0;
       this.time = 0;
       this.transition = "0";
    }
    
    Slide(Image img){
        this.image = img;
        this.time = 0;
        this.trans = 0;
        this.transition = "0";
    }
    
}
