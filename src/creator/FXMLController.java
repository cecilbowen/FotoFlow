
package creator;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import slide.Slide;
import slide.SlideManager;
import slide.SoundBlock;
import slide.SoundFile;
import slide.SoundManager;

public class FXMLController implements Initializable {
    
    private Label label;
    private final int thumbnailWidth = 225;
    private final int thumbnailHeight = 225;
    private final SlideManager slideManager = new SlideManager();
    private ArrayList<Slide> stagedSlides = new ArrayList<Slide>();
    private ObservableList<File> soundListViewFiles = FXCollections.observableArrayList();
    private ObservableList soundListViewNames = FXCollections.observableArrayList();
    private ArrayList<SoundBlock> soundBlockAL = new ArrayList<>();
    private boolean manualTiming = true;
    private int timeInSeconds = 0;    
    
    SoundManager soundManager = new SoundManager();
    
    @FXML
    private HBox stageImagePane;
    @FXML
    private HBox slidePane;
    @FXML 
    private HBox soundPane;
    @FXML 
    private ListView soundListView;
    @FXML
    private AnchorPane helpPane;
    @FXML
    private TextField textField;
    @FXML
    private Text responseLabel;
    
    @FXML
    private ScrollPane scrollPaneBrowser;
    
    @FXML
    private ScrollPane scrollPaneTimeline;

    
    //   SOUND HANDLING    
    private void handleRightClickedSound(MouseEvent event){
        if (event.getButton() == MouseButton.SECONDARY){            
            StackPane source = (StackPane)event.getSource();
            Text bnum = (Text)source.getChildren().get(1);                          //get block id     
            for(SoundBlock b:soundBlockAL){                                         //loop through soundblock arraylist to check which soundblock matches with the clicked soundblock
                String tBlockID = b.getBlockID().getText();
                String tBnum = bnum.getText();                
                if(tBlockID.equals(tBnum)){
                    soundManager.remove(b.getBlockName().getText());                //sends blockname, which is the soundname, to remove method
                }
            }            
            updateSoundBar(timeInSeconds);                                          //update soundbar after removing a soundblock
        }
    }
   
    @FXML
    private void handleImportSoundDirectory(ActionEvent event){
        DirectoryChooser chooser = new DirectoryChooser();
        File defaultDirectory = new File(System.getProperty("user.home"));
        chooser.setInitialDirectory(defaultDirectory);
        chooser.setTitle("Open Sound Directory");        
        File selectedDirectory = chooser.showDialog(new Stage());
        if (selectedDirectory !=null){
            for(File file :selectedDirectory.listFiles()){
                if(!file.isFile()){
                    System.out.println("file type not supported");
                } else if (file.getName().contains(".wav")||file.getName().contains(".aif")||file.getName().contains(".aiff")){                                               
                    if(!duplicatecheck(file.getName())){
                        soundListViewFiles.add(file);
                        soundListViewNames.add(file.getName());                                          
                    } else {System.out.println("duplicate detected");}
                } 
            }
            soundListView.setItems(soundListViewNames);                      
        }
        soundListView.setOnMouseClicked(e->handleLeftClickSoundListView(e));
    }
    
    private boolean duplicatecheck(String fname){
        boolean check = false;
        for(File i:soundListViewFiles){
            if (i.getName().equals(fname)){
                System.out.println(fname+" duplicate of"+i.getName());
                check = true; 
            }             
        } return check;
    }
        
    @FXML
    private void handleLeftClickSoundListView(MouseEvent event){ 
      if (event.getButton() == MouseButton.PRIMARY) {         
        for(File file :soundListViewFiles){
            if(file.getName().equals(soundListView.getSelectionModel().getSelectedItem().toString())){
                soundManager.load(file);
                updateSoundBar(timeInSeconds);
            }                
        }
      }        
    }
    
    @FXML
    private void updateSoundBar(int slideDuration){        
        soundPane.getChildren().clear();       
        ArrayList<SoundFile> sounds = soundManager.getSoundFileArrayList();
        for (SoundFile i:sounds){            
            SoundBlock soundBlock = new SoundBlock(i.getName(),i.getDuration(),slideDuration);     //slideDuration to enable/disable scaling        
            soundBlockAL.add(soundBlock);
            StackPane stack = new StackPane();
            stack.setOnMouseClicked(e->handleRightClickedSound(e));
            Text blockID = soundBlock.getBlockID();            
            blockID.setVisible(false);                                                             //hide block ID
            stack.getChildren().addAll(soundBlock.makeBlock(),blockID,soundBlock.getDisplayName());//stackpane of some of soundblock's properties
            
            soundPane.getChildren().add(stack);  
          //  soundScroll.hvalueProperty().bindBiderectional(slideScroll.hvalueProperty());
        }              
    }
        
    
    // STAGED SLIDE HANDLING
    @FXML
    private void handleLeftClickedImage(MouseEvent event, Slide slide) {
        if (event.getButton() == MouseButton.PRIMARY) {   
            Slide newSlide = slideManager.createSlide(slide.image, slide.path);
            newSlide.link = slide.link;
            slideManager.pushSlideTail(newSlide);
            updateSlideView();
        }
    }
        
    @FXML
    private void handleImportImageButtonAction(ActionEvent event) {
        DirectoryChooser chooser = new DirectoryChooser();
        File defaultDirectory = new File(System.getProperty("user.home"));
        chooser.setInitialDirectory(defaultDirectory);
        chooser.setTitle("Open Image Directory");        
        File selectedDirectory = chooser.showDialog(new Stage());
        
        if (selectedDirectory != null) {
            for(File file :selectedDirectory.listFiles()){
                Image image = new Image(file.toURI().toString(), thumbnailWidth, thumbnailHeight, true, true);
                Slide newSlide = slideManager.createSlide(image, file.toURI().toString());
                newSlide.link = file.getAbsolutePath();
                stagedSlides.add(newSlide);
            }
            updateStagedSlideView();
        }
    }
    
    @FXML
    private void handleTransitionButtonAction(ActionEvent event, int slideId, int transitionNumber){
        slideManager.setTransition(slideId, transitionNumber);
        updateSlideView();
    }
    
    @FXML
    private void updateStagedSlideView(){
        stageImagePane.getChildren().clear();
        
        for (Slide slide : stagedSlides){
            ImageView imgView = new ImageView(slide.image);
            imgView.setOnMouseClicked(event1 -> handleLeftClickedImage(event1, slide));  // Assigns the last one imported Needs to be dynamic
            stageImagePane.getChildren().add(imgView);
        }

    }
    
    
    // SLIDE HANDLING
    @FXML
    private void updateSlideView(){
        slidePane.getChildren().clear();
        slidePane.setSpacing(0);
        Slide[] slides = slideManager.getSlides();
        if (slides == null){
            return;
        }
        
        
        for (int i = 0; i < slides.length; i++){
            BorderPane borderPane = new BorderPane();
            int slideId = slides[i].id;
            int slideTrans = slides[i].trans;
            ImageView imgView = new ImageView(slides[i].image);
            StackPane picture = new StackPane();
            picture.setMinSize(255,255);
            picture.setMaxWidth(255);
            picture.setMaxHeight(255);
            picture.setPrefWidth(225);
            picture.setPrefHeight(255);
            int buttonHeight = 35;
            int buttonWidth = 25;
            
            imgView.setOnMouseClicked(event1 -> handleRightClickedSlide(event1, slideId));
            picture.getChildren().add(imgView);
            
            
            borderPane.setCenter(picture);
            borderPane.setMargin(picture, new Insets(0,0,0,0));
            borderPane.setPadding( new Insets(0,0,0,0));
            borderPane.setMinWidth(255);
            borderPane.setMinHeight(255);
           
            // Creating the buttons
            HBox hbButtons = new HBox();
            hbButtons.setSpacing(.5);
            ToggleButton button0 = new ToggleButton("X");
            ToggleButton button1 = new ToggleButton("<");
            ToggleButton button2 = new ToggleButton(">");
            ToggleButton button3 = new ToggleButton("^");
            ToggleButton button4 = new ToggleButton("v");
            ToggleButton button5 = new ToggleButton("/");
            Group bottomButtons = new Group();
            if (slideTrans == 0){
                button0.setSelected(true);
                button1.setSelected(false);
                button2.setSelected(false);
                button3.setSelected(false);
                button4.setSelected(false);
                button5.setSelected(false);
            } else if (slideTrans == 1){
                button0.setSelected(false);
                button1.setSelected(true);
                button2.setSelected(false);
                button3.setSelected(false);
                button4.setSelected(false);
                button5.setSelected(false);
            }else if (slideTrans == 2){
                button0.setSelected(false);
                button1.setSelected(false);
                button2.setSelected(true);
                button3.setSelected(false);
                button4.setSelected(false);
                button5.setSelected(false);
            }else if (slideTrans == 3){
                button0.setSelected(false);
                button1.setSelected(false);
                button2.setSelected(false);
                button3.setSelected(true);
                button4.setSelected(false);
                button5.setSelected(false);
            }else if (slideTrans == 4){
                button0.setSelected(false);
                button1.setSelected(false);
                button2.setSelected(false);
                button3.setSelected(false);
                button4.setSelected(true);
                button5.setSelected(false);
            }else if (slideTrans == 5){
                button0.setSelected(false);
                button1.setSelected(false);
                button2.setSelected(false);
                button3.setSelected(false);
                button4.setSelected(false);
                button5.setSelected(true);
            }
            button0.setPrefSize(buttonHeight,buttonWidth);
            button1.setPrefSize(buttonHeight,buttonWidth);
            button2.setPrefSize(buttonHeight,buttonWidth);
            button3.setPrefSize(buttonHeight,buttonWidth);
            button4.setPrefSize(buttonHeight,buttonWidth);
            button5.setPrefSize(buttonHeight,buttonWidth);
            button0.setOnAction(event1 -> handleTransitionButtonAction(event1, slideId, 0));
            button1.setOnAction(event1 -> handleTransitionButtonAction(event1, slideId, 1));
            button2.setOnAction(event1 -> handleTransitionButtonAction(event1, slideId, 2));
            button3.setOnAction(event1 -> handleTransitionButtonAction(event1, slideId, 3));
            button4.setOnAction(event1 -> handleTransitionButtonAction(event1, slideId, 4));
            button5.setOnAction(event1 -> handleTransitionButtonAction(event1, slideId, 5));
            hbButtons.getChildren().addAll(button0, button1, button2, button3, button4, button5);
            
            
           
            borderPane.setBottom(hbButtons);
            
            
            slidePane.getChildren().add(borderPane);
        }
    }
    
    @FXML
    private void handleRightClickedSlide(MouseEvent event, int slideId) {
        if (event.getButton() == MouseButton.SECONDARY) {
            ImageView img = (ImageView)event.getTarget();
            slideManager.removeSlide(slideId);
            updateSlideView();
        }
        System.out.println("Slide Clicked " + slideId);
    }
    
    
    // OPTIONS HANDLING
    @FXML
    private void handleHelpButtonClicked(ActionEvent event){
        helpPane.setVisible(true);
    }
    
    @FXML
    private void handleHelpButtonCloseClicked(ActionEvent event){
        helpPane.setVisible(false);
    }
    
    @FXML 
    private void handleExportButtonAction(ActionEvent event){
        Slide slides[] = slideManager.getSlides();
        ArrayList<SoundFile> sounds = soundManager.getSoundFileArrayList();
        FileWriter fileWriter;
        PrintWriter printWriter = null; 
        try {
            FileChooser fileChooser = new FileChooser();
            FileChooser.ExtensionFilter extFilter;
            extFilter = new FileChooser.ExtensionFilter("FLO files (*.flo)", "*.flo");
            fileChooser.getExtensionFilters().add(extFilter);
            File file = fileChooser.showSaveDialog(new Stage());
            
            fileWriter = new FileWriter(file);
            printWriter = new PrintWriter(fileWriter);
        } catch (IOException ex) {
            Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex);
        }
        printWriter.write("Options\n");
        printWriter.write("TransitionDuration " + Integer.toString(timeInSeconds) + "\n");
        printWriter.write("Manual " + String.valueOf(manualTiming)+ "\n");
        printWriter.write("Slides"+ "\n");
        // output the slide info
        for(int i = 0; i < slides.length; i++){
            //printWriter.write(slides[i].path + "\n");
            printWriter.write(slides[i].link + "\n");
            printWriter.write(slides[i].trans+ "\n");
        }
        
        printWriter.write("Sounds"+ "\n");
        // output sound info
        for (SoundFile i: sounds){
            printWriter.write(i.getPath()+"\n");
        }
        printWriter.close();
    }
    
    @FXML 
    private void handleCloseButtonAction(ActionEvent event){
        System.exit(0);
        //if (event.getButton() == MouseButton.PRIMARY) TODO: Get mouse button from ActionEvent
    }
    
    @FXML 
    private void handleManualButtonAction(ActionEvent event){
        manualTiming = true;
    }
         
    @FXML 
    private void handleTimerButtonAction(ActionEvent event){
        manualTiming = false;
    }
    
    @FXML 
    private void handleTimerInputAction(KeyEvent event){
        int newInt;
        try{
           newInt = Integer.parseInt(textField.getText()); 
           responseLabel.setText("Set to " + Integer.toString(newInt) + " Seconds");
           if (newInt > 30){
               responseLabel.setText("Set Too Long 30 max");
               newInt = 30;
           }
           timeInSeconds = newInt;
        } catch (NumberFormatException e){
           responseLabel.setText("Numbers Only Please");
        }   
        updateSoundBar(timeInSeconds); //soundbar scaling for changes in timer duration
    }
      
         
            
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
        scrollPaneBrowser.setHbarPolicy(ScrollBarPolicy.ALWAYS);
        scrollPaneTimeline.setHbarPolicy(ScrollBarPolicy.ALWAYS);
        //scrollPaneBrowser.setPannable(true);
        //scrollPaneTimeline.setPannable(true);
    }    
}
