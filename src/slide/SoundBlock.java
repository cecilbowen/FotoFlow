/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package slide;

import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

/**
 *
 * @author ccw0011
 */
public class SoundBlock {
    Text name;
    Text displayName;
    double blockWidth;
    int secondLength = 10;                                                  //default size for how a large a second of time will be graphically represented     
    int blockID;
    static int counter = 0;
        
   public SoundBlock(String name, double duration, int slideDuration){
       if(slideDuration <= 1){slideDuration = 1;}           
        this.blockWidth = (secondLength * duration * 10) / (slideDuration);  //adjust scaling
            if(this.blockWidth <= 20){this.blockWidth = 20;}                //minimum blockwidth to prevent blocks from being to small
        this.name = new Text(name);        
        double tWidth = this.name.getLayoutBounds().getWidth();             //text width
        if(this.blockWidth <= tWidth){                                      //resize display name if it is too large to fit in box
            String tname = this.name.getText();
            int tbWidth = (int)(this.blockWidth/5);            
            if(tbWidth <= 2){tbWidth = 2;}                                  //lower limit for substring
            if(tbWidth > tname.length()){tbWidth = tname.length();}         //upper for substring        
            String xname = tname.substring(0,tbWidth);             
            this.displayName = new Text(xname);
            this.displayName.setWrappingWidth(blockWidth-1);                //textwrapping
        } else {this.displayName = new Text(name); }              
        this.blockID = counter;                                             //counter used for block identification
        counter++;                                                          
    }
    
    public Rectangle makeBlock(){                                           //making a rectangle to send to stackpane
        Rectangle rect = new Rectangle();
        rect.setHeight(30);
        rect.setWidth(this.blockWidth);
        rect.setStroke(Color.rgb(1,1,1));
        rect.setFill(Color.rgb(102,255,0));                                 //bright green
        return rect;       
    }
    
    public Text getBlockName(){
        return this.name;
    }
    
    public Text getDisplayName(){
        return this.displayName;
    }
    
    public Text getBlockID(){                                               //block identification as text to use in stackpane
        Text blockID = new Text(Integer.toString(this.blockID));            
        return blockID;
    }
    
}
