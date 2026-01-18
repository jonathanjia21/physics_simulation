import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import org.example.Ball;


public class Main extends Application {

    private double y = 100;
    private double vy = 0;

    private double y1;
    private double vy1;
    private final int pixelsPerMeter = 50;


    private double x = 500;
    private double vx = 0;

    private final double g = 490;

    private static final double WIDTH = 1000;
    private static final double HEIGHT = 700;

    private boolean dragging = false;
    private double mouseEndX, mouseEndY;
    private double mouseStartX, mouseStartY;
    private double mouseX = 0;
    private double mouseY = 0;
    private double offsetX, offsetY;
    private long lastMouseTime;
    private int change = 1; // for unit conversions, 50px = 1m
    private double radius = 15;
    //BUTTON BOOLEANS
    private boolean paused = true;
    private boolean simulate1 = false; //for falling simulations
    private boolean reset_time = false;

    double computeTimeToGround(double heightMeters, double vyMeters) {
        double h = heightMeters;      // meters
        double v0 = -vyMeters;         // m/s
        double g = 9.8;               // m/s^2

        // h = v0 t + ½ g t²
        double a = 0.5 * g;
        double b = v0;
        double c = -h;

        double disc = b*b - 4*a*c;
        if (disc < 0) return Double.NaN;

        return (-b + Math.sqrt(disc)) / (2 * a);
    }
    @Override
    public void start(Stage primaryStage) {
        BorderPane root = new BorderPane();

        Pane world = new Pane();
        world.setPrefSize(WIDTH, HEIGHT);
        root.setCenter(world);
        Ball ball = new Ball(x, y , radius);



        //BUTTONS
        Button pauseBtn = new Button("Start ");
        Button changeBtn = new Button("Change units");
        Button simulate_fall = new Button("Simulate Fall");
        Button resetTimeBtn = new Button("Reset Time");
        HBox controls = new HBox(10);
        controls.setPadding(new Insets(10));
        controls.getChildren().addAll(pauseBtn, changeBtn, simulate_fall,   resetTimeBtn);

        //UI FOR SIMULATION 1 (FALLING)
        VBox inputPanel = new VBox(10);
        inputPanel.setPadding(new Insets(15));
        inputPanel.setPrefWidth(220);
        inputPanel.setStyle(""" 
        -fx-background-color: #f4f4f4;
        -fx-border-color: #cccccc;
        """);

        Label title = new Label("Free Fall Input");
        title.setStyle("-fx-font-size: 18; -fx-font-weight: bold;");
        Label heightLabel = new Label("Initial height (m):");
        TextField heightField = new TextField("5.0");
        Label vyLabel = new Label("Initial vy (m/s):");
        TextField vyField = new TextField("0.0");
        Button applyBtn = new Button("Apply & Start");
        Label timeLabel = new Label("Time to ground: —");
        timeLabel.setStyle("-fx-font-size: 14;");
        inputPanel.getChildren().addAll(
                title,
                heightLabel, heightField,
                vyLabel, vyField,
                applyBtn,
                timeLabel
        );
        root.setTop(controls);

        // Debug text
        Text debug = new Text(10, 20, "");
        debug.setStyle("-fx-font-size: 16;");

        world.getChildren().addAll(ball.getView(), debug);

        Scene scene = new Scene(root, WIDTH, HEIGHT);
        primaryStage.setTitle("Physics Simulation");
        primaryStage.setScene(scene);
        primaryStage.show();
        double worldHeight = world.getHeight();
        double worldWidth  = world.getWidth();

        //SIMULATE FALL BUTTON



        simulate_fall.setOnAction( e -> {
            simulate1 = false;
            paused = true;
            pauseBtn.setText("Resume");

            root.setRight(inputPanel);

        });
        applyBtn.setOnAction(e -> {
            try {
                double heightMeters = Double.parseDouble(heightField.getText());
                double vyMeters = Double.parseDouble(vyField.getText());
                // Compute time analytically
                double tHit = computeTimeToGround(heightMeters, vyMeters);

                if (!Double.isNaN(tHit)) {
                    timeLabel.setText(
                            String.format("Time to ground: %.2f s", tHit)
                    );
                } else {
                    timeLabel.setText("Time to ground: —");
                }
                y1 = worldHeight - heightMeters * pixelsPerMeter;
                vy1 = -vyMeters * pixelsPerMeter;
                y = y1;
                vy = vy1;

                simulate1 = true;
                paused = false;
                pauseBtn.setText("Pause");
            } catch (NumberFormatException ex) {
                System.out.println("Invalid input");
            }
        });

        Button closeBtn = new Button("Close");
        inputPanel.getChildren().add(closeBtn);

        closeBtn.setOnAction(e -> root.setRight(null));

        resetTimeBtn.setOnAction(e -> reset_time = !reset_time);

        boolean inputOpen = root.getRight() != null;
        if (inputOpen) return;


        // CHANGE UNITS BUTTON
        changeBtn.setOnAction(e -> {
            if(change == 1) {
                change = 50;
            }
            else {
                change = 1;
            }
        });

        //PAUSE BUTTON
        pauseBtn.setOnAction(e -> {
            paused = !paused;
            if (paused) {
                pauseBtn.setText("Resume");
            } else {
                pauseBtn.setText("Pause");
            }
        });

        //MOUSE INPUT HANDLING FOR DRAGGING BALL
            world.setOnMousePressed(e -> {
                    mouseX = e.getX(); // Get coordinates on input
                    mouseY = e.getY();
                    double dx = e.getX() - ball.getX(); // Distance from center of ball to input
                    double dy = e.getY() - ball.getY();
                    if(dx*dx + dy*dy <= ball.getRadius() * ball.getRadius()) { // Check if distance is within radius
                        ball.setVy(0); // Set velocities to zero since holding ball
                        ball.setVx(0);
                        offsetX = dx; // Get offset from center if holding ball
                        offsetY = dy;
//                mouseStartX = e.getX();
//                mouseStartY = e.getX();
                        dragging = true;
                    }
            });

            world.setOnMouseDragged(e -> {
                if(dragging) { // Change position accordingly if dragging ball, with offset from center
                    ball.setX(e.getX() - offsetX);
                    ball.setY(e.getY() - offsetY);
//            mouseEndX = e.getX();
//            mouseEndY = e.getY();
                }
            });

            world.setOnMouseReleased(e -> {
                    if(dragging) { // If release and mouse on press was on ball, set dragging to false
                        dragging = false;
                }
            });
            //MOUSE INPUT HANDLING END


        //ANIMATION HANDLING
        AnimationTimer timer = new AnimationTimer() {
            private double total_time = 0;
            private long lastTime = 0;

            @Override
            public void handle(long now) {
                if (lastTime == 0) {
                    lastTime = now;
                    return;
                }

                double dt = (now - lastTime) / 1_000_000_000.0;
                lastTime = now;

                if(reset_time) {
                    total_time = 0;
                    reset_time = false;
                }

                if(!paused) {
                    total_time += dt;
                }

                // ---- Physics ----
                if(!paused) {
                    if (!dragging) {
                        ball.update(dt, g);
                    }
                }

                // Floor collision
                // Needed to change to not include ball radius, so calculated time and actual time matches for free-fall
                if (ball.getY()  >= worldHeight) {
                    ball.setY(worldHeight - ball.getRadius());
                    ball.setVy(0);

                    if (simulate1) {
                        paused = true;
                        simulate1 = false;
                        pauseBtn.setText("Resume");
                    }
                }



//
//                // Right wall collision
//                if (x + ball.getRadius() >worldWidth) {
//                    x = worldWidth - ball.getRadius();
//                    vx = -vx * 0.7;
//                }

                // Left wall collision
                if (ball.getX() - ball.getRadius() < 0) {
                    ball.setX(ball.getRadius());
                    ball.setVx(-ball.getVx()*0.7 );
                }

                // Update debug info

                if(change == 50) {
                    debug.setText(String.format(
                            "x  = %.2fm\nvx = %.2fm/s\ny  = %.2fm\nvy = %.2fm/s\ntime elapsed = %.4fs\nmouseX = %.2fm\nmouseY = %.2fm\ngravity = %.2fm/s^2",
                            ball.getX()/change, ball.getVx()/change, -(ball.getY()/change - 12.8), -ball.getVy()/change, total_time, mouseX/change, -(mouseY/change -12.8), g/change
                    ));
                }
                else {
                    debug.setText(String.format(
                    "x  = %.2fpx\nvx = %.2fpx/s\ny  = %.2fpx\nvy = %.2fpx/s\ntime elapsed = %.4fs\nmouseX = %.2fpx\nmouseY = %.2fpx\ngravity = %.2fpx/s^2",
                            ball.getX(), ball.getVx(), -(ball.getY() -12.8*50), -ball.getVy(), total_time, mouseX, -(mouseY -12.8*50), g
                    ));
                }

            }
        };

        timer.start();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
