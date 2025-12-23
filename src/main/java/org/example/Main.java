import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class Main extends Application {

    private double y = 100;
    private double vy = 0;

    private double x = 100;
    private double vx = 0;

    private final double g = 500;

    private static final double WIDTH = 1000;
    private static final double HEIGHT = 700;

    private boolean dragging = false;
    private double mouseEndX, mouseEndY;
    double mouseStartX, mouseStartY;
    double mouseX = 0;
    double mouseY = 0;
    double offsetX, offsetY;
    long lastMouseTime;

    private boolean paused = true;



    @Override
    public void start(Stage primaryStage) {


        BorderPane root = new BorderPane();


        Pane world = new Pane();
        world.setPrefSize(WIDTH, HEIGHT);
        root.setCenter(world);
        Circle ball = new Circle(15, Color.RED);
        ball.setCenterX(x);
        ball.setCenterY(y);
        Button pauseBtn = new Button("Start ");
        HBox controls = new HBox(10);
        controls.setPadding(new Insets(10));
        controls.getChildren().add(pauseBtn);

        root.setTop(controls);




        // Debug text
        Text debug = new Text(10, 20, "");
        debug.setStyle("-fx-font-size: 16;");

        world.getChildren().addAll(ball, debug);

        Scene scene = new Scene(root, WIDTH, HEIGHT);
        primaryStage.setTitle("Physics Simulation");
        primaryStage.setScene(scene);
        primaryStage.show();
        double worldHeight = world.getHeight();
        double worldWidth  = world.getWidth();


        pauseBtn.setOnAction(e -> {
            paused = !paused;
            if (paused) {
                pauseBtn.setText("Resume");
            } else {
                pauseBtn.setText("Pause");
            }
        });

        world.setOnMousePressed(e -> {
            mouseX = e.getX(); // Get coordinates on input
            mouseY = e.getY();

            double dx = e.getX() - x; // Distance from center of ball to input
            double dy = e.getY() - y;
            if(dx*dx + dy*dy <= ball.getRadius() * ball.getRadius()) { // Check if distance is within radius

                vy = 0; // Set velocities to zero since holding ball
                vx = 0;
                offsetX = dx; // Get offset from center if holding ball
                offsetY = dy;

//                mouseStartX = e.getX();
//                mouseStartY = e.getX();
                dragging = true;

            }
        });

        world.setOnMouseDragged(e -> {
            if(dragging) { // Change position accordingly if dragging ball, with offset from center
                x = e.getX() - offsetX;
                y = e.getY() - offsetY;
            }
//            mouseEndX = e.getX();
//            mouseEndY = e.getY();

        });

        world.setOnMouseReleased(e -> {
            if(dragging) { // If release and mouse on press was on ball, set dragging to false
                dragging = false;
            }
        });


        AnimationTimer timer = new AnimationTimer() {
            private long lastTime = 0;

            @Override
            public void handle(long now) {
                if (lastTime == 0) {
                    lastTime = now;
                    return;
                }

                double dt = (now - lastTime) / 1_000_000_000.0;
                lastTime = now;

                if(paused) {
                    return;
                }

                // ---- Physics ----
                if (!dragging) {
                    vy += g * dt;
                    y += vy * dt;
                    x += vx * dt;
                }

                // Floor collision
                if (y + ball.getRadius() > worldHeight) {
                    y = worldHeight - ball.getRadius();
                    vy = -vy * 0.7;
                }

                // Ceiling collision
                if (y - ball.getRadius() < 0) {
                    y = ball.getRadius();
                    vy = -vy * 0.7;
                }

                // Right wall collision
                if (x + ball.getRadius() >worldWidth) {
                    x = worldWidth - ball.getRadius();
                    vx = -vx * 0.7;
                }

                // Left wall collision
                if (x - ball.getRadius() < 0) {
                    x = ball.getRadius();
                    vx = -vx * 0.7;
                }

                // Apply position
                ball.setCenterX(x);
                ball.setCenterY(y);

                // Update debug info
                debug.setText(String.format(
                        "x  = %.2f\nvx = %.2f\ny  = %.2f\nvy = %.2f\ndt = %.4f\nmouseX = %.2f\nmouseY = %.2f",
                        x, vx, y, vy, dt, mouseX, mouseY
                ));
            }
        };

        timer.start();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
