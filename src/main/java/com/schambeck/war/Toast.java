package com.schambeck.war;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.util.Objects;

import static javafx.scene.text.FontWeight.BOLD;

public final class Toast {
    public static void makeText(Stage ownerStage, String toastMsg, Integer toastDelay, int fadeInDelay, Integer fadeOutDelay) {
        Stage toastStage = new Stage();
        toastStage.initOwner(ownerStage);
        toastStage.setResizable(false);
        toastStage.initStyle(StageStyle.TRANSPARENT);
        
        Text text = new Text(toastMsg);
        text.setFont(Font.font("Courier New", BOLD, 17));
        text.setFill(Color.BLACK);
        
        StackPane root = new StackPane(text);
        root.setStyle("-fx-background-radius: 20; -fx-background-color: rgba(0, 0, 0, 0.2); -fx-padding: 20px;");
        root.setOpacity(0);
        
        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);
        handleKeyboard(toastStage, scene);
        toastStage.setScene(scene);
        toastStage.show();
        
        Timeline fadeInTimeline = new Timeline();
        KeyFrame fadeInKey1 = new KeyFrame(Duration.millis(fadeInDelay), new KeyValue(toastStage.getScene().getRoot().opacityProperty(), 1));
        fadeInTimeline.getKeyFrames().add(fadeInKey1);
        if (toastDelay != null) {
            fadeInTimeline.setOnFinished((ae) -> new Thread(() -> {
                try {
                    Thread.sleep(toastDelay);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e.getMessage(), e);
                }
                Timeline fadeOutTimeline = new Timeline();
                KeyFrame fadeOutKey1 = new KeyFrame(Duration.millis(fadeOutDelay), new KeyValue(toastStage.getScene().getRoot().opacityProperty(), 0));
                fadeOutTimeline.getKeyFrames().add(fadeOutKey1);
                fadeOutTimeline.setOnFinished((aeb) -> toastStage.close());
                fadeOutTimeline.play();
            }).start());
        }
        fadeInTimeline.play();
    }
    
    private static void handleKeyboard(Stage stage, Scene scene) {
        scene.setOnKeyPressed(event -> {
            if (Objects.requireNonNull(event.getCode()) == KeyCode.ESCAPE) {
                stage.close();
            }
        });
    }
}
