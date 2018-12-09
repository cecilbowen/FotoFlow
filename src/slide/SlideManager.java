/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package slide;

import javafx.scene.image.Image;

/**
 *
 * @author louis
 */
public class SlideManager {
    Slide head = null;
    Slide tail = null;
    private int numberOfSlides = 0;
    
    public boolean pushSlide(Slide slide){
        slide.next = head;
        slide.previous = null;
        
        if(head != null){
            head.previous = slide;
        }
        
        head = slide;
        applyIdNumbers();
        return true;
    }
    
    public boolean pushSlideTail(Slide slide) {
        if (head == null){
            head = slide;
            tail = slide;
        }
        else if (tail != null){
            slide.previous = tail;
            tail.next = slide;
        }
        
        tail = slide;
        applyIdNumbers();
        return true;
    }
    
    public boolean removeSlide(int idNumber) {
        
        Slide slide = head;
        while (slide != null){
            if (idNumber == slide.id){
                if (slide.previous == null && slide.next == null){
                    head = null;
                    tail = null;
                }
               
                if (slide.previous != null){
                    if (slide == tail){
                        tail = slide.previous;
                    }
                    slide.previous.next = slide.next;
                }
                if (slide.next != null){
                    if (head == slide){
                        head = slide.next;
                    }
                    slide.next.previous = slide.previous;
                }
                applyIdNumbers();
                return true;
            }
             
            slide = slide.next;
        }
        applyIdNumbers();
        return true;
    }
    
    
    public boolean pushNewSlide(String name){
        Slide newSlide = new Slide(name);
        
        newSlide.next = head;
        newSlide.previous = null;
        
        if(head != null){
            head.previous = newSlide;
        }
        
        head = newSlide;
        
        applyIdNumbers();
        return true;
    }  
    
    public boolean pushNewSlide(Image thumbnail) {
        Slide newSlide = new Slide(thumbnail);
        
        newSlide.next = head;
        newSlide.previous = null;
        
        if (head != null){
            head.previous = newSlide;
        }
        
        head = newSlide;
        
        applyIdNumbers();
        return true;
    }
    
        public boolean pushNewSlideTail(Image thumbnail) {
        Slide newSlide = new Slide(thumbnail);
        
        newSlide.next = null;
        newSlide.previous = tail;
        
        if (tail != null){
            tail.previous = newSlide;
        }
        
        tail = newSlide;
        
        applyIdNumbers();
        return true;
    }
   

    
    public boolean hasSlides(){
        if (head != null){
            return true;
        } else {
            return false;
        }
    }
    
    private void applyIdNumbers(){
        
        if (head != null){
            numberOfSlides = 0;
            int newId = 1;
            Slide slide = head;
            while (slide != null){
                slide.id = newId;
                newId++;
                numberOfSlides++;
                slide = slide.next;
            }
        }
    }
    

    public Slide[] getSlides() {
        if (head == null){
            return null;
        }
        Slide[] slides = new Slide[numberOfSlides];
        Slide testSlide = head;
        
        
        
        for(int i = 0; i < slides.length;i++){
         slides[i] = testSlide;
         if (testSlide.next != null){
            testSlide = testSlide.next;
         }
        }   
        return slides;
    }

    /**
     * This function adds one slide after the index indicated 
     * Starts at 0
     * 
     * @author Louis Filip
     * @param name
     * @param index 
     */
    void addSlideAfterIndex(String name, int index) {
       Slide newSlide = new Slide(name);
       Slide slide = head;
       while (slide != null){
           if(slide.id == index){
                if (slide.next != null){
                    newSlide.next = slide.next;
                    newSlide.previous = slide;
                    newSlide.next.previous = newSlide;
                    slide.next = newSlide;
                    applyIdNumbers();
                    break;
                } else  {
                    slide.next = newSlide;
                    newSlide.previous = slide;
                    break;
                }
               
           } else {
               slide = slide.next;
           }
       }
    }

    public Slide createSlide(Image image, String newPath) {
        Slide newSlide = new Slide(image);
        newSlide.path = newPath;
        
        return newSlide;        
    }

    public void setTransition(int id, int i) {
        Slide slide = head;
        while(slide != null){
            if (slide.id == id){
                slide.trans = i;
                System.out.println("Slide transition changed");
                return;
            }
            slide = slide.next;
        }
        return;
    }

}
