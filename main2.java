package Land;

	import java.text.DecimalFormat;
	import java.util.ArrayList;
	import javafx.animation.KeyFrame;
	import javafx.animation.Timeline;
	import javafx.application.Application;
	import javafx.event.ActionEvent;
	import javafx.event.Event;
	import javafx.event.EventHandler;
	import javafx.geometry.Insets;
	import javafx.scene.Scene;
	import javafx.scene.canvas.Canvas;
	import javafx.scene.canvas.GraphicsContext;
	import javafx.scene.control.Label;
	import javafx.scene.layout.BorderPane;
	import javafx.scene.layout.HBox;
	import javafx.scene.layout.VBox;
	import javafx.scene.paint.Color;
	import javafx.stage.Stage;
	import javafx.util.Duration;
	import javafx.scene.control.Button;
	import javafx.scene.text.Font;
	import ControlSystem.ClosedLoopV2;
	import ControlSystem.Vector2D;
	import OpenLoopAgain.OpenLoopV4;
	import ControlSystem.SpaceShip;
	
	/*
	 * Can show different methods to land on Titan - even when only the wind is working.
	 * Currently works with the open loop, doesn't use wind.
	 */
	
	public class Main2 extends Application {
		
	    //second to update the model(deltaT)
	    public final double TIME_SLICE = 0.1;

	    private static final int BOTTOM_AREA_HEIGHT = 100;
	    
	    public static final double REAL_SCALE = 750; //if we have 400 for the y-axis, which symbolises 300 km, every amount of meters should be divided by this, to get the number of steps for the coordinate

	    public static final double TRANSLATE_COOR = 700;
	    public static final double TRANSLATE_COOR_TWO = 550;
	    
	    private FPSCounter fps = new FPSCounter();

	    private double canvasWidth = 0;
	    private double canvasHeight = 0;
	    private Label timeLabel;
	    private Label fpsLabel;
	    private Label landingLabel;
	    private Label velocityLabelX;
	    private Label velocityLabelY;
	    private Label locationLabelX;
	    private Label locationLabelY;
	    //private Label goalLabel;
	    private Button pauseButton;
	    private Button fastForwardTwentyMinButton;
	    private Button fastForwardFiveMinButton;
	    private boolean paused = true;
	    private Button playBackButton;
	    private boolean change = false;
	    private double spaceshipMass = 10000; // kg
	    public Land.SpaceShip spaceShip;
	    public Land.SpaceShip spaceShip2;
	    public ControlSystem.SpaceShip spaceShip3;
	    public OpenLoopV4 openLoop;
	    public ClosedLoopV2 closedLoop;
	    
	    //private Vector2D locLanding;
	    private Button restartApplication;
	    private int currentSpaceShip = 0;
	    private ArrayList<Land.SpaceShip> oldSpaceShips = new ArrayList<>();
	    private HBox buttons;
	    private int iterations = 0;

	    void cleanup() {
	        fps = new FPSCounter();
	        canvasWidth = 0;
	        canvasHeight = 0;
	        timeLabel = null;
	        fpsLabel = null;
	        landingLabel = null;
	        velocityLabelX = null;
	        velocityLabelY = null;
	        //private Label goalLabel;
	        pauseButton = null;
	        fastForwardTwentyMinButton = null;
	        fastForwardFiveMinButton = null;
	        paused = true;
	        playBackButton = null;
	        change = false;
	        spaceshipMass = 5000; // kg
	        spaceShip = null;
	        //private Vector2D locLanding;
	        restartApplication = null;
	        currentSpaceShip = 0;
	        oldSpaceShips = new ArrayList<>();
	    }

	    void startProcedure(Stage stage) {
	        restartApplication = new Button("Restart");
	        restartApplication.setFont(new Font("Serif", 16));
	        restartApplication.setOnAction(e -> {
	            restart(stage);
	        });
	        createSpaceship();
	        GraphicsContext gc = createGui(stage);
	        Timeline timeline = new Timeline();
	        timeline.setCycleCount(Timeline.INDEFINITE);
	        KeyFrame kf = new KeyFrame(
	                Duration.millis(2),
	                new EventHandler<ActionEvent>() {
	                    public void handle(ActionEvent ae) {
	                        updateFrame(gc);
	                    }
	                });
	        timeline.getKeyFrames().add(kf);
	        timeline.play();
	        stage.show();
	    }

	    void restart(Stage stage) {
	        cleanup();
	        startProcedure(stage);
	    }

	    @Override
	    public void start(Stage stage) {
	        startProcedure(stage);
	    }

	    protected void updateFrame(GraphicsContext gc) {
	        this.canvasWidth = gc.getCanvas().getWidth();
	        this.canvasHeight = gc.getCanvas().getHeight();
	        gc.clearRect(0, 0, canvasWidth, canvasHeight);
	        
	        gc.setFill(Color.BLACK);
	        gc.fillRect(0, 0, canvasWidth, canvasHeight);
	        
	        gc.setFill(Color.WHITE); 
	        gc.setStroke(Color.WHITE);
	        gc.strokeLine(0, 550, 1450, 550);//this is the ground - 400 km away from titan
	        gc.strokeLine(TRANSLATE_COOR, 0, TRANSLATE_COOR, 550);
	      
	        
	        if ((spaceShip.getLocation().y) > (550 *  REAL_SCALE - (spaceShip.getHeight()))) {
	        	paused = true;
	        	landingLabel.setText("Landed!");
	            buttons.getChildren().remove(0);
	            buttons.getChildren().add(0,restartApplication);
	            fastForwardFiveMinButton.setDisable(true);
	            fastForwardTwentyMinButton.setDisable(true);
	        }
	        
	        // when y < 0, then stop -> it has hit titan
	        if (spaceShip.getLocation().y <= 0) {
	        	paused = true;
	        }
	        
	        
	        if (change) {//to rewind - using the rewind buttons
	        	if (currentSpaceShip>0) {	
	        		spaceShip = oldSpaceShips.get(oldSpaceShips.size()-1).copy();
	        		gc.fillRect(spaceShip.getLocation().getX() / REAL_SCALE + TRANSLATE_COOR,spaceShip.getLocation().getY() / REAL_SCALE , spaceShip.getWidth(), spaceShip.getHeight()); //width and height are not scaled
	        		oldSpaceShips.remove(oldSpaceShips.size()-1);
	        		spaceShip.setSeconds(oldSpaceShips.get(oldSpaceShips.size()-1).getSeconds());
	        		timeLabel.setText(spaceShip.getElapsedTimeAsString());
	        		currentSpaceShip--;
	        		landingLabel.setText("Landing...");
	        	}
	        	change = false;
	        }
	        
	        for (Land.SpaceShip spaces : oldSpaceShips) {
	        //gc.fillOval(spaces.getLocation().getX() / REAL_SCALE + TRANSLATE_COOR, TRANSLATE_COOR_TWO - (spaces.getLocation().getY() / REAL_SCALE + 0.5 * spaceShip.getHeight()), 5, 5 );
	        }
	        
	        gc.fillRect((spaceShip.getLocation().getX() / REAL_SCALE + TRANSLATE_COOR), TRANSLATE_COOR_TWO -(spaceShip.getLocation().getY() / REAL_SCALE + spaceShip.getHeight()) , spaceShip.getWidth(), spaceShip.getHeight());
	        //gc.fillRect((spaceShip2.getLocation().getX() / REAL_SCALE + TRANSLATE_COOR),TRANSLATE_COOR_TWO -(spaceShip2.getLocation().getY() / REAL_SCALE + 0.5 * spaceShip2.getHeight()), spaceShip2.getWidth(), spaceShip2.getHeight());
	        //gc.fillRect((spaceShip3.getLocation().getX() / REAL_SCALE + TRANSLATE_COOR), TRANSLATE_COOR_TWO -(spaceShip3.getLocation().getY() / REAL_SCALE + spaceShip3.getHeight()) , spaceShip3.getWidth(), spaceShip3.getHeight());
	        
	        if (!paused && iterations < openLoop.getLocations().size()) {//the normal s
	        	spaceShip.setLocation(openLoop.getLocations().get(iterations));
	        	//spaceShip3.setLocation(new Vector2D(closedLoop.successfullCoordinate.get(iterations).getX(), closedLoop.successfullCoordinate.get(iterations).getX()));
	        	//spaceShip2.update(TIME_SLICE);
	        	//System.out.println(spaceShip2.getYLocation());
	        	//System.out.println(spaceShip2.getXLocation());
	        	//openLoop.update(TIME_SLICE);
	        	//spaceShip2.setLocation(new Vector2D(closedLoop.successfullCoordinate.get(iterations).getX(), closedLoop.successfullCoordinate.get(iterations).getY()));
	        	iterations++;
	            timeLabel.setText(spaceShip2.getElapsedTimeAsString());
	            if(spaceShip.getSeconds()% (60*60) == 0) {//every 10 min, save a copy
	            	oldSpaceShips.add(spaceShip.copy());
	            	currentSpaceShip++;
	            }
	        }
	        fpsLabel.setText("FPS: " + fps.countFrame());
	        DecimalFormat numberFormat = new DecimalFormat("#0.00"); 
	        
	       // velocityLabelX.setText("Velocity x = " + spaceShip.getVelocity().getX());
	       // velocityLabelY.setText("Velocity y = " + spaceShip.getVelocity().getY());
	        locationLabelX.setText("Location x = " + numberFormat.format(spaceShip.getLocation().getX() / 1000) + " km away from goal");
	        locationLabelY.setText("Location y = " + numberFormat.format((spaceShip.getLocation().y)/ 1000) + " km away from goal");
	    }
	    
	    private void createSpaceship() {
	    	//arbitrary numbers:
	    	Vector2D spaceshipLoc = new ControlSystem.Vector2D(50000, 300*1000); //the location of the upper left corner of the spaceShip - it's 400 km away from Titan
	    	double length = 17; //in m
	    	double width = 4.5; //in m
	    	
	    	spaceShip = new Land.SpaceShip(this.spaceshipMass, new Vector2D(), new Vector2D(), spaceshipLoc, length, width, true );
	    	spaceShip2 = new Land.SpaceShip(this.spaceshipMass, new Vector2D(), new Vector2D(), spaceshipLoc.copy(), length, width, true);
			spaceShip3 = new SpaceShip(this.spaceshipMass, spaceshipLoc.x, spaceshipLoc.y, 0, length, width, true);
			
	    	openLoop = new OpenLoopV4(spaceShip, ControlSystem.SpaceShip.GRAV_TITAN);
	    	//closedLoop = new ClosedLoopV2(spaceShip3.copy());
	    	//closedLoop.helperClosedLoop();
	    }
	    
	    private GraphicsContext createGui(Stage stage) {
	        BorderPane border = new BorderPane();
	        createTimeLabel();
	        createFPSLabel();
	        createLandingLabel();
	        createVelocityLabels();
	        createLocationLabels();
	        
	        HBox hbox = createHBox();
	        border.setBottom(hbox);
	        HBox top = createHBoxTwo();
	        border.setTop(top);
	        
	        Canvas canvas = new Canvas();//createCanvas();
	        border.setCenter(canvas);
	        stage.setTitle("Moon lander - open loop");
	        Scene scene = new Scene(border);
	        stage.setScene(scene);
	        stage.setMaximized(true);

	        // Bind canvas size to stack pane size.
	        canvas.widthProperty().bind(stage.widthProperty());
	        canvas.heightProperty().bind(stage.heightProperty().subtract(BOTTOM_AREA_HEIGHT * 2));
	        return canvas.getGraphicsContext2D();
	    }
	    
	    private HBox createHBox() {
	        HBox hbox = new HBox();
	        buttons = new HBox();
	        
	        createPauseButton();
	        createFastForwardFiveMinButton();
	        createFastForwardTwentyMinButton();
	        createPlayBackButton();
	        
	        buttons.setPadding(new Insets(10, 10, 10, 10));
	        buttons.setSpacing(5);
	        buttons.setStyle("-fx-background-color: #d5d8dc ;");
	        buttons.setFillHeight(true);
	        
	        //buttons.getChildren().add(playBackButton);
	        buttons.getChildren().add(pauseButton);
	        buttons.getChildren().add(fastForwardFiveMinButton);
	        buttons.getChildren().add(fastForwardTwentyMinButton);
	        
	        VBox labels = new VBox();
	        labels.getChildren().add(velocityLabelX);
	        labels.getChildren().add(velocityLabelY);
	        labels.getChildren().add(landingLabel);
	        labels.setSpacing(10);
	        labels.setStyle("-fx-background-color:  #d5d8dc ;");
	        labels.setFillWidth(true);
	        
	        VBox moreLabels = new VBox();
	        moreLabels.getChildren().add(timeLabel);
	        moreLabels.getChildren().add(locationLabelX);
	        moreLabels.getChildren().add(locationLabelY);
	        moreLabels.setSpacing(10);   // Gap between nodes
	        moreLabels.setStyle("-fx-background-color:  #d5d8dc ;");
	        moreLabels.setFillWidth(true);
	        
	        hbox.setPadding(new Insets(15, 12, 15, 12));
	        hbox.setSpacing(10);   // Gap between nodes
	        hbox.setStyle("-fx-background-color:  #d5d8dc ;");
	        hbox.setFillHeight(true);
	        hbox.getChildren().add(moreLabels);
	        hbox.getChildren().add(this.fpsLabel);
	        hbox.getChildren().add(labels);
	        hbox.getChildren().add(buttons);
	        return hbox;
	    }

	    private HBox createHBoxTwo() {
	    	HBox hbox = new HBox();
	    	
	    	Label title = new Label();
	    	title.setText("Landing on Titan");
	    	title.setFont(new Font("Serif", 40));
	    	title.setTextFill(Color.BLACK);
	    	
	    	hbox.getChildren().add(title);
	    	hbox.setPadding(new Insets(15, 12, 15, 12));
	        hbox.setSpacing(10);
	        hbox.setStyle("-fx-background-color: #d5d8dc; ");
	        hbox.setFillHeight(true);
	    	return hbox;
	    }
	    
	    private void createPauseButton() {
	    	pauseButton = new Button();
	    	pauseButton.setText("Play");
	    	pauseButton.setFont(new Font("Serif", 16));
	    	pauseButton.setOnAction(new EventHandler<ActionEvent>() {
	            public void handle(ActionEvent e) {
	                    if (paused) {
	                        paused = false;
	                        pauseButton.setText("Pause");
	                    } else {
	                        paused = true;
	                        pauseButton.setText("Play");
	                    }
	            }
	        });
	    	
	    }
	    private EventHandler<ActionEvent> forwardButtons(){
	        EventHandler eh = new EventHandler<ActionEvent>() {
	            public void handle(ActionEvent e) {
	                for (int i = 0; i <(60*20)/TIME_SLICE; i++) {
	                    openLoop.update(TIME_SLICE);
	                    if (spaceShip.getLocation().getY() <= 0) {
	                        paused = true;
	                        fastForwardFiveMinButton.setDisable(true);
	                        fastForwardTwentyMinButton.setDisable(true);
	                        landingLabel.setText("Landed!");
	                        buttons.getChildren().remove(0);
	                        buttons.getChildren().add(0,restartApplication);
	    		        	/*if (spaceShip.getLocation().getX() < locLanding.getX() + 10*REAL_SCALE && spaceShip.getLocation().getX() > locLanding.getX() - 10*REAL_SCALE && spaceShip.landingSucceeded()) {
	    		        		goalLabel.setText("Landing succeeded");
	    		        	}
	    		        	else {
	    		        		goalLabel.setText("Landing failed");
	    		        	}*/
	                        break;
	                    }
	                    timeLabel.setText(spaceShip.getElapsedTimeAsString());
	                    if (spaceShip.getSeconds() % (60*20) == 0) {
	                        oldSpaceShips.add(spaceShip.copy());
	                        currentSpaceShip++;
	                    }
	                }
	            }
	        };
	        return eh;
	    }

	    private void createFastForwardFiveMinButton() {
	    	fastForwardFiveMinButton = new Button();
	    	fastForwardFiveMinButton.setText("Skip five min");
	    	fastForwardFiveMinButton.setFont(new Font("Serif", 16));
	    	fastForwardFiveMinButton.setOnAction(forwardButtons());
	    }
	    
	    private void createFastForwardTwentyMinButton() {
	    	fastForwardTwentyMinButton = new Button();
	    	fastForwardTwentyMinButton.setText("Skip twenty min");
	    	fastForwardTwentyMinButton.setFont(new Font("Serif", 16));
	    	fastForwardTwentyMinButton.setOnAction(forwardButtons());
	    }
	    
	    private void createPlayBackButton() {
	    	playBackButton = new Button();
	    	playBackButton.setText("Rewind 20 min");
	    	playBackButton.setFont(new Font("Serif", 16));
	    	playBackButton.setOnAction(new EventHandler<ActionEvent>() {
	    		public void handle(ActionEvent e) {
	    			change = true;
	    		}
	    	});
	    }
	    
	    private void createTimeLabel() {
	        timeLabel = new Label();
	        timeLabel.setPrefSize(500, 20);
	        timeLabel.setFont(new Font("Serif", 16));
	        timeLabel.setTextFill(Color.BLACK);
	    }

	    private void createFPSLabel() {
	        fpsLabel = new Label();
	        fpsLabel.setPrefSize(100, 20);
	        fpsLabel.setFont(new Font("Serif", 16));
	        fpsLabel.setTextFill(Color.BLACK);
	    }

	    private void createLandingLabel() {
	        landingLabel = new Label();
	        landingLabel.setPrefSize(300, 20);
	        landingLabel.setFont(new Font("Serif", 16));
	        landingLabel.setTextFill(Color.BLACK);
	        landingLabel.setText("Landing...");
	    }
	    
	    private void createVelocityLabels() {
	    	velocityLabelX = new Label();
	    	velocityLabelX.setPrefSize(300, 20);
	    	velocityLabelX.setFont(new Font("Serif", 16));
	    	velocityLabelX.setTextFill(Color.BLACK);
	    	velocityLabelX.setText("Velocity x");
	    	
	    	velocityLabelY = new Label();
	    	velocityLabelY.setPrefSize(300, 20);
	    	velocityLabelY.setFont(new Font("Serif", 16));
	    	velocityLabelY.setTextFill(Color.BLACK);
	    	velocityLabelY.setText("Velocity y");
	    	
	    }
	    
	    private void createLocationLabels() {
	    	locationLabelX = new Label();
	    	locationLabelX.setPrefSize(300, 20);
	    	locationLabelX.setFont(new Font("Serif", 16));
	    	locationLabelX.setTextFill(Color.BLACK);
	    	locationLabelX.setText("Location x");
	    	
	    	locationLabelY = new Label();
	    	locationLabelY.setPrefSize(300, 20);
	    	locationLabelY.setFont(new Font("Serif", 16));
	    	locationLabelY.setTextFill(Color.BLACK);
	    	locationLabelY.setText("Location y");
	    }

	    public static void main(String[] args) {
	        launch(args);
	    }

	}
