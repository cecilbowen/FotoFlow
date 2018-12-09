/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package slide;

import javafx.beans.property.*;
/**
 *
 * @author jgiff
 */
public class SoundFile {
    Object currentSound;
    SimpleDoubleProperty duration;
    SimpleStringProperty name;
    SimpleStringProperty path;

    SoundFile(Object currentSound,String name,double duration, String path){
            this.currentSound = currentSound;
            this.name = new SimpleStringProperty(name);
            this.duration = new SimpleDoubleProperty(duration);	
            this.path = new SimpleStringProperty(path);
    }

    public String getName() {
            return name.get();
    }

    public double getDuration() {
            return duration.get();
    }
    
    public String getPath(){
            return path.get();
    }
    
}
