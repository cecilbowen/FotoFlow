/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package player;

import static player.Player.slides;
import static player.Player.music;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import javafx.animation.FadeTransition;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.Pane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.util.Duration;
import slide.Slide;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;

/**
 *
 * @author cgb0011
 */

enum Transition {
    NONE, LEFT, RIGHT, UP, DOWN, FADE; //0, 1, 2, 3, 4, 5
}

public class SlideshowController extends AnchorPane {
    
    private Stage myStage;
    
    private ContextMenu debugMenu = null;
    
    private MediaView mv;
    
    private boolean noMusic = false;
    
    private boolean resizable = false; //whether we allow player to be resized (via dragging)
    
    private boolean transitionActive = false; //true if a transition is running
    
    private int slideNumber = 0; //current slide (page) number
    
    private int slideTotal; //total number of slides
    
    //slide transition duration in seconds
    //gets updated for every slide, since each slide can have a different duration
    //used to let interval timer (updateTimer()) update
    //now decided upon to have constant transition duration so this will not change
    private int transitionDuration = 3;
    
    private boolean paused = false;
    
    private boolean started = false; //true when start slideshow is pressed
    
    private boolean manual = false; //manual or auto interval
    
    private int interval = 3; //time to show each slide in seconds before going to next (auto only)

    private Timeline autoInterval;
    
    @FXML
    private BorderPane borderPaneSlideCanvas;
    
    @FXML
    private BorderPane borderPaneSlideTransition;
    
    @FXML
    private BorderPane borderPaneControls;
    
    @FXML
    private Pane paneSlideshowControls;    
    
    @FXML
    private ImageView imgViewSlideCanvas;
    
    @FXML
    private Button btnStartSlideshow;
    
    @FXML
    private Button btnPlay;
    
    @FXML
    private Button btnNext;
        
    @FXML
    private Button btnPrevious;
            
    @FXML
    private Button btnStop;
    
    @FXML
    private Button btnFullscreen;
    
    @FXML
    private Slider sliderVolume;
    
    @FXML
    private Label labelCounter;
    
    @FXML
    private ProgressBar progressBarInterval;
    
    //dont really use this default constructor, but it is default..
    public SlideshowController() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("Slideshow.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);
        
        try {
            fxmlLoader.load();            
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }
    
    public SlideshowController(Stage stage) {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("Slideshow.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);
        
        try {
            fxmlLoader.load();            
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
        
        if (!music.isEmpty()) {
            mv = new MediaView(music.get(0));
            sliderVolume.setValue(mv.getMediaPlayer().getVolume() * 100);
            sliderVolume.valueProperty().addListener(new InvalidationListener() {

                @Override
                public void invalidated(Observable observable) {
                    mv.getMediaPlayer().setVolume(sliderVolume.getValue() / 100);
                }
            });
        } else noMusic = true;
        myStage = stage;
        
        myStage.setResizable(resizable);
        myStage.setTitle("FotoFlow Player");
        //debugMenu(); //testing only

        //Setup temporary slide transition border pane
        //The way transitions work right now:
        //crossfade: draw next slide below current slide and fade out/in top/bottom
        this.borderPaneSlideTransition.setPrefSize(stage.getWidth(), stage.getHeight());
        this.borderPaneSlideTransition.prefWidthProperty().bind(stage.widthProperty());
        this.borderPaneSlideTransition.prefHeightProperty().bind(stage.heightProperty());       
        
        //Makes slide images resize correctly according to window size
        this.borderPaneSlideCanvas.setPrefSize(stage.getWidth(), stage.getHeight());
        this.borderPaneSlideCanvas.prefWidthProperty().bind(stage.widthProperty());
        this.borderPaneSlideCanvas.prefHeightProperty().bind(stage.heightProperty());
        this.paneSlideshowControls.prefWidthProperty().bind(stage.widthProperty());
        
        //Makes slide controls correctly stay at bottom of screen, regardless of window size
        this.borderPaneControls.setPrefSize(stage.getWidth(), stage.getHeight());
        this.borderPaneControls.prefWidthProperty().bind(stage.widthProperty());
        this.borderPaneControls.prefHeightProperty().bind(stage.heightProperty());
        
        this.paneSlideshowControls.setOpacity(0);
        
        this.slideTotal = slides.size();
        
        autoInterval = new Timeline(
            new KeyFrame(Duration.ZERO, new KeyValue(progressBarInterval.progressProperty(), 0)),
            new KeyFrame(Duration.seconds(interval+transitionDuration), e-> {
                System.out.println(">auto interval<");
                try {
                    updateSlideshow(1);
                    updateTimer(transitionDuration);
                } catch (FileNotFoundException ex) {
                    Logger.getLogger(SlideshowController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }, new KeyValue(progressBarInterval.progressProperty(), 1))    
        );
        
        /*
        autoInterval = new Timeline(new KeyFrame(Duration.seconds(interval+transitionDuration), new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                System.out.println(">auto interval<");
                try {
                    updateSlideshow(1);
                    updateTimer(transitionDuration);
                } catch (FileNotFoundException ex) {
                    Logger.getLogger(SlideshowController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }));
        */
        autoInterval.setCycleCount(1);
        
        stage.setOnCloseRequest((ev) -> {
            autoInterval.stop();

            //System.exit(0);
        });
        
        borderPaneControls.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                if(mouseEvent.getButton().equals(MouseButton.PRIMARY)){
                    if (mouseEvent.getClickCount() == 2) {
                        System.out.println("doubleClicked. toggling fullscreen");
                        toggleFullscreen();
                    } else if (mouseEvent.getClickCount() == 1) { //play/pause on single click
                        /*
                        try {
                            System.out.println("singleClicked. toggling play/pause");
                            updateSlideshow(0);
                        } catch (FileNotFoundException ex) {
                            Logger.getLogger(SlideshowController.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        */
                    }
                }
            }
        });
    }
    
    //sets amount of time to show each slide
    public void setInterval(int seconds) {
        this.interval = seconds;
    }
    
    //sets whether slideshow is manual or auto-interval
    public void setManual(boolean mode) {
        this.manual = mode;
    }
    
    //Sets up dev only menu
    private void debugMenu() {
        debugMenu = new ContextMenu();
 
        MenuItem item1 = new MenuItem("Toggle Auto/Manual");
        item1.setOnAction(new EventHandler<ActionEvent>() {
 
            @Override
            public void handle(ActionEvent event) {
                manual = !manual;
                System.out.println("Manual? "+manual);
            }
        });
        MenuItem item2 = new MenuItem("Toggle Interval Time (3 or 5)");
        item2.setOnAction(new EventHandler<ActionEvent>() {
 
            @Override
            public void handle(ActionEvent event) {
                if (interval == 3) interval = 5; else interval = 3;
                System.out.println("Interval Time Set: "+interval+" seconds");
            }
        });
 
        // Add MenuItem to ContextMenu
        debugMenu.getItems().addAll(item1, item2);
 
        // When user right-clicks
        /*
        borderPaneControls.setOnContextMenuRequested(new EventHandler<ContextMenuEvent>() {
 
            @Override
            public void handle(ContextMenuEvent event) {
 
                debugMenu.show(borderPaneControls, event.getScreenX(), event.getScreenY());
            }
        });        
        */
    }
    
    //starts slideshow by setting first slide
    @FXML
    private void startSlideshow_button(ActionEvent event) throws FileNotFoundException {
        if (!noMusic) {
            mv.getMediaPlayer().play();
            for (int i = 0; i < music.size(); i++) {
                MediaPlayer p = music.get(i);
                MediaPlayer np = music.get((i+1)%music.size());
                p.setOnEndOfMedia(new Runnable() {
                    
                    @Override 
                    public void run() {
                        p.stop();
                        mv.setMediaPlayer(np);
                        mv.getMediaPlayer().setVolume(sliderVolume.getValue() / 100);
                        np.play();
                    }
                });        
            }
        }
        
        Slide slide = slides.get(this.slideNumber);
        setSlide(slide, true);
        this.labelCounter.setText((this.slideNumber + 1) + "/" + this.slideTotal);
        this.started = true;
        btnPlay.setText("Pause");
        this.btnStartSlideshow.setVisible(false);
        this.btnStartSlideshow.setDisable(true); //disable this button
        
        //prevents volume slider from getting keyboard focus
        //(yes, this happens sometimes..)
        this.borderPaneSlideCanvas.requestFocus();

        //this.manual = false; //testing only
        if (!this.manual) {
            if (!this.progressBarInterval.isVisible()) this.progressBarInterval.setVisible(true);
            autoInterval.play();
            //set up keybinds
            myStage.getScene().setOnKeyReleased(new EventHandler<KeyEvent>() {
                @Override
                public void handle(KeyEvent event) {
                    switch (event.getCode()) {
                        case SPACE:  {
                            try {
                                updateSlideshow(0);
                            } catch (FileNotFoundException ex) {
                                Logger.getLogger(SlideshowController.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        } break;
                        case ENTER:  {
                            try {
                                stopSlideshow();
                            } catch (FileNotFoundException ex) {
                                Logger.getLogger(SlideshowController.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        } break;
                        case F11:  {
                            toggleFullscreen();
                        } break;
                    }
                }
            });
            return;
        }
        this.progressBarInterval.setVisible(false);
        
        //setup gui for manual interval
        
        this.btnNext.setDisable(false);
        this.btnPrevious.setDisable(false);
        //this.btnPlay.setDisable(false);
        
        //set up keybinds
        myStage.getScene().setOnKeyReleased(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                switch (event.getCode()) {
                    case LEFT:  {
                        try {
                            updateSlideshow(-1);
                        } catch (FileNotFoundException ex) {
                            Logger.getLogger(SlideshowController.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    } break;
                    case RIGHT:  {
                        try {
                            updateSlideshow(1);
                        } catch (FileNotFoundException ex) {
                            Logger.getLogger(SlideshowController.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    } break;
                    case SPACE:  {
                        try {
                            updateSlideshow(0);
                        } catch (FileNotFoundException ex) {
                            Logger.getLogger(SlideshowController.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    } break;
                    case ENTER:  {
                        try {
                            stopSlideshow();
                        } catch (FileNotFoundException ex) {
                            Logger.getLogger(SlideshowController.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    } break;
                    case F11:  {
                        toggleFullscreen();
                    } break;
                }
            }
        });
    }
    
    @FXML
    private void showControls_button(MouseEvent me) {
        if (!this.started) return;
        this.paneSlideshowControls.setOpacity(0.85);
        //System.out.println("MOUES IN");
    }
    
    @FXML
    private void hideControls_button(MouseEvent me) {
        this.paneSlideshowControls.setOpacity(0);
        //System.out.println("MOUES OUT");
    }
    
    @FXML
    private void play_button(ActionEvent event) throws FileNotFoundException {
        updateSlideshow(0);
    }

    @FXML
    private void next_button(ActionEvent event) throws FileNotFoundException {
        updateSlideshow(1);
    }

    @FXML
    private void previous_button(ActionEvent event) throws FileNotFoundException {
        updateSlideshow(-1);
    }

    @FXML
    private void stop_button(ActionEvent event) throws FileNotFoundException {
        if (!this.started) return;
        stopSlideshow();
    }
    
    @FXML
    private void showDebugMenu(ContextMenuEvent e) {
        if (debugMenu == null) return;
        if (started) return;
        debugMenu.show(borderPaneControls, e.getScreenX(), e.getScreenY());
    }
    
    @FXML
    private void toggleFullscreen_button(ActionEvent event) {
        if (transitionActive) return;
        toggleFullscreen();
    }
    
    private void toggleFullscreen() {
        if (!transitionActive) myStage.setFullScreen(!myStage.isFullScreen());
    }
    
    //declaring transitions (so that we dont create new one for every slide o_O)
    //the duration and node here don't mean anything as they will be changed
    private TranslateTransition tt = new TranslateTransition(Duration.seconds(3), this.borderPaneSlideTransition);
    private FadeTransition ft = new FadeTransition(Duration.seconds(3), borderPaneSlideTransition);
    private FadeTransition ft2 = new FadeTransition(Duration.seconds(3), borderPaneSlideCanvas);
    
    //Sets slide image
    public void setSlide(Slide slide, boolean firstSlide) throws FileNotFoundException {
        File file = slide.file;
        
        //get slide transition and transition length (time)
        int transition = slide.trans;
        int duration = slide.getTime();
        
        //temporarily set as random transition with default time
        //to be removed after import/export setup
        //transition = ThreadLocalRandom.current().nextInt(0, 5 + 1);
        duration = transitionDuration;
        //if (firstSlide) transition = 0;
        
        transitionDuration = duration; //set to let interval timer know how to change
        System.out.println(file + " / transition: " + transition);
        FileInputStream inputstream = new FileInputStream(file.getPath());
        Image img = new Image(inputstream);
        BackgroundSize backgroundSize = new BackgroundSize(100, 100, true, true, true, false);
        //this.imgViewSlideCanvas.setImage(img);

        //Set up transition slide
        this.borderPaneSlideTransition.setVisible((transition > 0));
        this.borderPaneSlideTransition.setDisable(false);
        this.borderPaneSlideTransition.setTranslateX(0);
        this.borderPaneSlideTransition.setTranslateY(0);
        this.borderPaneSlideTransition.setOpacity(1.0);
        this.borderPaneSlideCanvas.setOpacity(1.0);
        tt.stop();
        ft.stop();
        ft2.stop();
        transitionActive = false;
        //if (resizable) myStage.setResizable(true);
        
        Background background_t = this.borderPaneSlideCanvas.getBackground();
        this.borderPaneSlideTransition.setBackground(background_t);        
        
        //Set new slide underneath current one
        BackgroundImage myBI = new BackgroundImage(img, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER, backgroundSize);
        Background background = new Background(myBI);
        this.borderPaneSlideCanvas.setBackground(background);
        
        if (!this.borderPaneSlideCanvas.isVisible()) this.borderPaneSlideCanvas.setVisible(true);
        
        if (firstSlide) return; //don't need a transition on first slide
        
        //Set up wipe transition
        //Zero out all movement values at first
        //TranslateTransition tt = new TranslateTransition(Duration.seconds(duration), this.borderPaneSlideTransition);
        tt.setNode(this.borderPaneSlideTransition);
        tt.setDuration(Duration.seconds(duration));
        tt.setFromX(0);
        tt.setToX(0);
        tt.setFromY(0);
        tt.setToY(0);
        tt.setCycleCount(1);
        tt.setAutoReverse(false);
        tt.setOnFinished(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                System.out.println("Wipe done. moving waaaay out of bounds");
                borderPaneSlideTransition.setTranslateX(10000);
                transitionActive = false;
                //if (resizable) myStage.setResizable(true);
            }
        });
        
        if (transition == Transition.NONE.ordinal()) {
            //Go ahead and tell wipe transition to stop just in case it's run before
            tt.stop();
            transitionActive = false;
            //this.borderPaneSlideTransition.setDisable(true);
        } else if (transition == Transition.LEFT.ordinal()) { //wipe left
            tt.setFromX(0);
            tt.setToX(-this.myStage.getWidth());
        } else if (transition == Transition.RIGHT.ordinal()) { //wipe right
            tt.setFromX(0);
            tt.setToX(this.myStage.getWidth());
        } else if (transition == Transition.UP.ordinal()) { //wipe up
            tt.setFromY(0);
            tt.setToY(-this.myStage.getHeight());            
        } else if (transition == Transition.DOWN.ordinal()) { //wipe down
            tt.setFromY(0);
            tt.setToY(this.myStage.getHeight());
        } else if (transition == Transition.FADE.ordinal()) { //cross fade
            //this.borderPaneSlideTransition.toFront();
            //this.borderPaneControls.toFront();

            //Go ahead and tell wipe transition to stop just in case it's run before
            //tt.setDuration(Duration.millis(1));
            tt.stop();
            
            //Play crossfade transition(s)
            //FadeTransition ft = new FadeTransition(Duration.seconds(duration), borderPaneSlideTransition);
            //FadeTransition ft2 = new FadeTransition(Duration.seconds(duration), borderPaneSlideCanvas);
            ft.setDuration(Duration.seconds(duration));
            ft.setNode(borderPaneSlideTransition);
            ft2.setDuration(Duration.seconds(duration));
            ft2.setNode(borderPaneSlideCanvas);            
            
            ft.setFromValue(1.0);
            ft.setToValue(0.0);
            ft.setCycleCount(1);
            ft.setAutoReverse(false);
            ft2.setFromValue(0.0);
            ft2.setToValue(1.0);
            ft2.setCycleCount(1);
            ft2.setAutoReverse(false);
            ft.play();
            ft2.play();
            transitionActive = true;
            
            //not really needed but being consistent
            ft.setOnFinished(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent actionEvent) {
                    //getPlotChildren().remove(candle);
                    System.out.println("fade trans finished");
                    transitionActive = false;
                }
            });
            
            
            return;
        }
        
        //Plays wipe transition
        if (transition > 0) {
            tt.play();
            transitionActive = true;
            //if (resizable) myStage.setResizable(false);
        }


    }
    
    //Handles users pressing of play/pause/next/previous buttons on slide control
    public void updateSlideshow(int change) throws FileNotFoundException {
        if (!this.started) return;
        if (change == 0) {
            //play/pause slideshow
            this.paused = !this.paused;
            if (paused) {
                btnPlay.setText("Play");
                if (!manual) autoInterval.pause();
                if (!noMusic) mv.getMediaPlayer().stop();
            } else {
                btnPlay.setText("Pause");
                if (!manual) autoInterval.play();
                if (!noMusic) mv.getMediaPlayer().play();
            }
            return;
        } else if (change == 1) {
            if (this.slideNumber >= slides.size() - 1) return; //cant go past last slide
        } else if (change == -1) {
            if (this.slideNumber <= 0) return; //cant go lower than first slide
        }
        
        this.slideNumber += change;
        Slide slide = slides.get(this.slideNumber);
        setSlide(slide, false);
        
        this.labelCounter.setText((this.slideNumber + 1) + "/" + this.slideTotal);
        this.borderPaneSlideCanvas.requestFocus();
    }
    
    public void stopSlideshow() throws FileNotFoundException {
        if (!noMusic) {
            mv.getMediaPlayer().stop();
            mv.setMediaPlayer(music.get(0));
        }
        if (!manual) autoInterval.stop();
        this.slideNumber = 0;
        //basically, go back to "Start Slideshow" screen
        //add that in later
        Slide slide = slides.get(this.slideNumber);
        setSlide(slide, true);
        this.labelCounter.setText((this.slideNumber + 1) + "/" + this.slideTotal);
        this.started = false;
        this.paused = false;
        this.btnStartSlideshow.setVisible(true);
        this.btnStartSlideshow.setDisable(false); //disable this button
        this.paneSlideshowControls.setOpacity(0);
        
        //prevents volume slider from getting keyboard focus
        //(yes, this happens sometimes..)
        this.btnStartSlideshow.requestFocus();
        
        //setup gui for manual interval
        
        this.btnNext.setDisable(true);
        this.btnPrevious.setDisable(true);
        //this.btnPlay.setDisable(false);
        
        this.borderPaneSlideTransition.setVisible(false);
        this.borderPaneSlideCanvas.setVisible(false);
        
        //set up keybinds
        myStage.getScene().setOnKeyReleased(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                switch (event.getCode()) {
                    case LEFT:  {

                    } break;
                    case RIGHT:  {

                    } break;
                    case SPACE:  {

                    } break;
                    case F11:  {
                        toggleFullscreen();
                    } break;
                }
            }
        });
    }
    
    //called every time the interval ends to update with next slides transition duration
    //(slides can have different transition durations, so the auto interval is the global
    // value defined in the creator + the transition duration, so we must update it after
    // each slide)
    public void updateTimer(int time) {
        //autoInterval.stop();
        autoInterval = new Timeline(
            new KeyFrame(Duration.ZERO, new KeyValue(progressBarInterval.progressProperty(), 0)),
            new KeyFrame(Duration.seconds(interval+time), e-> {
                System.out.println(">auto interval<");
                try {
                    updateSlideshow(1);
                    if (this.slideNumber+1 < this.slideTotal) updateTimer(transitionDuration);
                } catch (FileNotFoundException ex) {
                    Logger.getLogger(SlideshowController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }, new KeyValue(progressBarInterval.progressProperty(), 1))    
        );
        /*
        autoInterval = new Timeline(new KeyFrame(Duration.seconds(interval+time), new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                System.out.println(">auto interval<");
                try {
                    updateSlideshow(1);
                    updateTimer(transitionDuration);
                } catch (FileNotFoundException ex) {
                    Logger.getLogger(SlideshowController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }));
        */
        autoInterval.setCycleCount(1);
        autoInterval.play();
    }
    
}
