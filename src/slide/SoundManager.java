/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package slide;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import javax.sound.sampled.*;
/**
 *
 * @author jgiff
 */
public class SoundManager {	
    Object currentSound;
    double duration;
    double totalDuration = 0;
    String name;
    int count = 0;
    ArrayList<SoundFile> soundFileArrayList = new ArrayList<SoundFile>();	 
    //ObservableList<SoundFile> data = FXCollections.observableArrayList(soundFileArrayList);	

    public void load(File file) {
        try {
               currentSound = AudioSystem.getAudioInputStream(file);
       } catch (UnsupportedAudioFileException e) {
               System.out.println(e.getMessage());
               e.printStackTrace();
       } catch (IOException e) {
               System.out.println(e.getMessage());
               e.printStackTrace();
       }

        AudioInputStream stream = (AudioInputStream) currentSound;
        AudioFormat format = stream.getFormat();
        DataLine.Info info = new DataLine.Info(Clip.class, stream.getFormat(), (int) stream.getFrameLength()*format.getFrameSize());

        try {
               Clip clip = (Clip) AudioSystem.getLine(info);               
               currentSound = clip;
               duration = clip.getBufferSize() / (clip.getFormat().getFrameSize() * clip.getFormat().getFrameRate());
       } catch (LineUnavailableException e) {
               System.out.println(e.getMessage());
               e.printStackTrace();
       }		 
    name = file.getName();    
    for (SoundFile i:soundFileArrayList){
        if (i.getName().equals(name)){
            count ++;
            name = name+"("+count+")";
        }
    }
    String p = file.getAbsolutePath();
    SoundFile soundFile = new SoundFile(currentSound,name,duration,p); 	
    soundFileArrayList.add(soundFile);
    //System.out.format(soundFile.getName() + " is %3.2f seconds%n",soundFile.getDuration());
    calcDuration();    
    }

    public void remove(String name) {  
            for(SoundFile i:new ArrayList<SoundFile>(soundFileArrayList)){
                if (i.getName().equals(name)){
                    soundFileArrayList.remove(i);
                }
            }
            calcDuration();             
    }

    public void calcDuration() {
           // System.out.println("updateGUI called");
            totalDuration = 0;
            for(SoundFile i:soundFileArrayList) {
                    totalDuration += i.getDuration();
            }
            //System.out.println("total duration is: " + totalDuration);	            
    }

    public double getDuration() {
            return totalDuration;
    }

    public void displaySoundList() {
            for(SoundFile i:soundFileArrayList) {
                    System.out.println(i.name);
            }
    }	
    
    public ArrayList<SoundFile> getSoundFileArrayList(){
        return soundFileArrayList;
    }	
	 
}
