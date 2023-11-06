package com.schambeck.war;

import com.schambeck.war.attacker.AttackerBox;
import com.schambeck.war.attacker.GunAttacker;
import com.schambeck.war.building.BuildingBox;
import com.schambeck.war.core.Ground;
import com.schambeck.war.core.Xform;
import com.schambeck.war.defender.DefenderBox;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.*;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static javafx.scene.paint.Color.*;

@Slf4j
public class WarApp extends Application {
    final AtomicInteger attackerIdGenerator = new AtomicInteger();
    final AtomicInteger attackerGunIdGenerator = new AtomicInteger();
    final AtomicInteger defenderIdGenerator = new AtomicInteger();
    final AtomicInteger defenderGunIdGenerator = new AtomicInteger();
    final AtomicInteger buildingIdGenerator = new AtomicInteger();
    final Group root = new Group();
    final Xform axisGroup = new Xform();
    final Xform world = new Xform();
    final PerspectiveCamera camera = new PerspectiveCamera(false);
    final Xform cameraXform = new Xform();
    final Xform cameraXform2 = new Xform();
    final Xform cameraXform3 = new Xform();
    final Ground defendersGround = new Ground(1000, 20, 500);
    final Ground attackersGround = new Ground(50, 20, 500);
    final List<AttackerBox> attackers = new ArrayList<>();
    final List<DefenderBox> defenders = new ArrayList<>();
    final List<BuildingBox> buildings = new ArrayList<>();
    final List<BuildingBox> untouchedBuildings = new ArrayList<>();
    final List<GunAttacker> guns = new ArrayList<>();
    final List<GunAttacker> movingGuns = new ArrayList<>();
    private static final double CAMERA_INITIAL_DISTANCE = 150;
    private static final double CAMERA_INITIAL_X_ANGLE = -15;
    private static final double CAMERA_INITIAL_Y_ANGLE = -15;
    private static final double CAMERA_NEAR_CLIP = 0.1;
    private static final double CAMERA_FAR_CLIP = 10000.0;
    private static final double AXIS_LENGTH = 250.0;
    private static final double CONTROL_MULTIPLIER = 0.1;
    private static final double SHIFT_MULTIPLIER = 10.0;
    private static final double MOUSE_SPEED = 0.1;
    private static final double ROTATION_SPEED = 2.0;
    private static final double TRACK_SPEED = 0.3;
    private final Random random = new Random();
    
    double mousePosX;
    double mousePosY;
    double mouseOldX;
    double mouseOldY;
    double mouseDeltaX;
    double mouseDeltaY;
    private Label buildingsLabel;
    private Label gunsLabel;
    private final Image buildingMap = new Image("image/building.jpg");
    private final Image tankMap = new Image("image/tank.jpg");
    private final Image defenderMap = new Image("image/defender.jpg");
    private final Image grassMap = new Image("image/grass.jpg");
    private final Image sandMap = new Image("image/sand.jpg");
    
    @Override
    public void start(Stage stage) {
        root.getChildren().add(world);
        root.setDepthTest(DepthTest.ENABLE);
        
        buildCamera();
        buildLight();
        buildAxes();
        buildDefendersGround();
        buildAttackersGround();
        addDefenders(2);
        addBuildings(3, 2);
        addAttackers(5);
        buildScoreboard();
        play();
        
        Scene scene = new Scene(root, 1024, 768, true);
        scene.setFill(GREY);
        handleKeyboard(scene);
        handleMouse(scene);
        
        stage.setTitle("War Game");
        stage.setMaximized(true);
        stage.setScene(scene);
        stage.show();
        
        scene.setCamera(camera);
    }
    
    public static void main(String[] args) {
        launch();
    }
    
    private void buildCamera() {
        root.getChildren().add(cameraXform);
        cameraXform.getChildren().add(cameraXform2);
        cameraXform2.getChildren().add(cameraXform3);
        cameraXform3.getChildren().add(camera);
        camera.setNearClip(CAMERA_NEAR_CLIP);
        camera.setFarClip(CAMERA_FAR_CLIP);
        camera.setTranslateX(-700);
        camera.setTranslateY(-450);
        camera.setTranslateZ(CAMERA_INITIAL_DISTANCE);
        cameraXform.ry.setAngle(CAMERA_INITIAL_Y_ANGLE);
        cameraXform.rx.setAngle(CAMERA_INITIAL_X_ANGLE);
    }
    
    private void buildLight() {
        LightBase light;
        light = new PointLight(WHITE);
        light.setTranslateX(0);
        light.setTranslateY(-600);
        light.setTranslateZ(0);
        LightBase light2 = new PointLight(WHITE);
        light2.setTranslateX(0);
        light2.setTranslateY(-20);
        light2.setTranslateZ(600);
        LightBase light3 = new PointLight(WHITE);
        light3.setTranslateX(0);
        light3.setTranslateY(-20);
        light3.setTranslateZ(-600);
        Rotate rotate = new Rotate(180);
        light3.getTransforms().add(rotate);
        world.getChildren().addAll(light, light2, light3);
    }
    
    private void buildAxes() {
        final PhongMaterial redMaterial = new PhongMaterial();
        redMaterial.setDiffuseColor(DARKRED);
        redMaterial.setSpecularColor(RED);

        final PhongMaterial greenMaterial = new PhongMaterial();
        greenMaterial.setDiffuseColor(DARKGREEN);
        greenMaterial.setSpecularColor(GREEN);

        final PhongMaterial blueMaterial = new PhongMaterial();
        blueMaterial.setDiffuseColor(DARKBLUE);
        blueMaterial.setSpecularColor(BLUE);

        final Box xAxis = new Box(AXIS_LENGTH, 1, 1);
        final Box yAxis = new Box(1, AXIS_LENGTH, 1);
        final Box zAxis = new Box(1, 1, AXIS_LENGTH);

        xAxis.setMaterial(redMaterial);
        yAxis.setMaterial(greenMaterial);
        zAxis.setMaterial(blueMaterial);

        axisGroup.getChildren().addAll(xAxis, yAxis, zAxis);
        axisGroup.setVisible(false);
        world.getChildren().addAll(axisGroup);
    }
    
    private void buildDefendersGround() {
        PhongMaterial greenMaterial = new PhongMaterial();
        greenMaterial.setDiffuseMap(grassMap);
        defendersGround.setMaterial(greenMaterial);
        world.getChildren().addAll(defendersGround);
    }
    
    private void buildAttackersGround() {
        PhongMaterial blueMaterial = new PhongMaterial();
        blueMaterial.setDiffuseMap(sandMap);
        attackersGround.setMaterial(blueMaterial);
        attackersGround.setTranslateX((defendersGround.getWidth() / 2) + (attackersGround.getWidth() / 2));
        world.getChildren().addAll(attackersGround);
    }
    
    private void addDefenders(int count) {
        IntStream.range(0, count).forEach(value -> addDefender(count));
    }
    
    private void addDefender(int count) {
        PhongMaterial magentaMaterial = new PhongMaterial();
        magentaMaterial.setDiffuseMap(defenderMap);
        PhongMaterial selectedMaterial = new PhongMaterial();
        selectedMaterial.setDiffuseColor(MAGENTA);
        selectedMaterial.setSpecularColor(DARKMAGENTA);
        DefenderBox defender = new DefenderBox(40, 20, 20, o -> updateScoreboard(), movingGuns, defenderIdGenerator.incrementAndGet(), defenderGunIdGenerator);
        defender.setMaterial(magentaMaterial);
        defender.setTranslateX((((defendersGround.getWidth() / 2) - (defender.getWidth() * 4)) * -1) + 20);
        defender.setTranslateY(((defendersGround.getHeight() / 2) + (defender.getHeight() / 2)) * -1);
        defender.setTranslateZ((defendersGround.getDepth() / 2 * -1) + (defendersGround.getDepth() / (count + 1) * (defenders.size() + 1)));
        Label label = createDefenderLabel("Defender", defender, selectedMaterial, magentaMaterial, defender.getReference());
        defenders.add(defender);
        world.getChildren().addAll(defender, label);
    }
    
    private <T extends Box> Label createDefenderLabel(String prefix, T box, PhongMaterial selectedMaterial, PhongMaterial magentaMaterial, Integer reference) {
        Label label = new Label(prefix + " #" + reference);
        label.setTranslateX((box.getTranslateX()));
        label.setTranslateY((box.getTranslateY() - box.getHeight() - 10));
        return createLabel(label, box, selectedMaterial, magentaMaterial);
    }

    private <T extends Box> Label createAttackerLabel(String prefix, T box, PhongMaterial selectedMaterial, PhongMaterial magentaMaterial, Integer reference) {
        Label label = new Label(prefix + " #" + reference);
        label.setTranslateX((box.getTranslateX() - 10));
        label.setTranslateY((box.getTranslateY() - box.getLayoutBounds().getHeight() - 10));
        return createLabel(label, box, selectedMaterial, magentaMaterial);
    }

    private <T extends Box> Label createLabel(Label label, T box, PhongMaterial selectedMaterial, PhongMaterial magentaMaterial) {
        label.setTranslateZ((box.getTranslateZ()));
        label.setVisible(false);
        box.setOnMouseEntered(event -> {
            box.setMaterial(selectedMaterial);
            label.setVisible(true);
        });
        box.setOnMouseExited(event -> {
            box.setMaterial(magentaMaterial);
            label.setVisible(false);
        });
        return label;
    }
    
    private void addBuildings(int count, int rows) {
        IntStream.range(0, rows).forEach(row -> IntStream.range(0, count).forEach(item -> addBuilding(count, item, row)));
    }
    
    private void addBuilding(int count, int item, int row) {
        PhongMaterial grayMaterial = new PhongMaterial();
        grayMaterial.setDiffuseMap(buildingMap);
        PhongMaterial selectedMaterial = new PhongMaterial();
        selectedMaterial.setDiffuseColor(GRAY);
        selectedMaterial.setSpecularColor(DARKGRAY);
        BuildingBox building = new BuildingBox(30, 100, 30, buildingIdGenerator.incrementAndGet());
        building.setMaterial(grayMaterial);
        building.setTranslateX(((defendersGround.getWidth() / 2) - (building.getWidth() * 2 * (row + 1))) * -1);
        building.setTranslateY(((defendersGround.getHeight() / 2) + (building.getHeight() / 2)) * -1);
        building.setTranslateZ((defendersGround.getDepth() / 2 * -1) + (defendersGround.getDepth() / (count + 1) * (item + 1)));
        Label label = createDefenderLabel("Building", building, selectedMaterial, grayMaterial, building.getReference());
        buildings.add(building);
        untouchedBuildings.add(building);
        world.getChildren().addAll(building, label);
    }
    
    private void addAttackers(int count) {
        IntStream.range(0, count).forEach(value -> addAttacker(count));
    }
    
    private void addAttacker(int count) {
        PhongMaterial redMaterial = new PhongMaterial();
        redMaterial.setDiffuseMap(tankMap);
        PhongMaterial selectedMaterial = new PhongMaterial();
        selectedMaterial.setDiffuseColor(RED);
        selectedMaterial.setSpecularColor(DARKRED);
        AttackerBox attacker = new AttackerBox(40, 20, 20, o -> updateScoreboard(), attackerIdGenerator.incrementAndGet(), attackerGunIdGenerator);
        attacker.setMaterial(redMaterial);
        attacker.setTranslateX(attackersGround.getTranslateX());
        attacker.setTranslateY(((defendersGround.getHeight() / 2) + (attacker.getHeight() / 2)) * -1);
        attacker.setTranslateZ((defendersGround.getDepth() / 2 * -1) + (defendersGround.getDepth() / (count + 1) * (attackers.size() + 1)));
        attackers.add(attacker);
        Label label = createAttackerLabel("Attacker", attacker, selectedMaterial, redMaterial, attacker.getReference());
        world.getChildren().addAll(attacker, label);
    }
    
    private void buildScoreboard() {
        buildingsLabel = new Label("Buildings: " + buildings.size());
        buildingsLabel.setFont(Font.font("Courier New", FontWeight.BOLD, 30));
        gunsLabel = new Label("Guns: " + movingGuns.size());
        gunsLabel.setFont(Font.font("Courier New", FontWeight.BOLD, 30));
        VBox scoreboard = new VBox(buildingsLabel, gunsLabel);
        scoreboard.setTranslateX(300);
        scoreboard.setTranslateY((scoreboard.getHeight() + 100) * -1);
        scoreboard.setTranslateZ(attackersGround.getDepth() / 2);
        root.getChildren().add(scoreboard);
    }
    
    private void play() {
        if (attackers.isEmpty()) {
            log.debug("Can't play because there's no attackers...");
            return;
        }
        if (untouchedBuildings.isEmpty()) {
            log.debug("Can't play because there's no buildings...");
            return;
        }
        AttackerBox attacker = attackers.get(getRandomAttacker());
        BuildingBox building = untouchedBuildings.get(getRandomBuilding());
        GunAttacker gun = attacker.shoot(world, building, guns, movingGuns, untouchedBuildings);
        playShootSong();
        if (!defenders.isEmpty()) {
            DefenderBox defender = defenders.get(getRandomDefender());
            defender.defend(world, gun);
        }
    }
    
    private void playShootSong() {
        String song = "song/missile-firing.wav";
        log.debug("Playing shoot: " + song);
        String externalForm = Objects.requireNonNull(getClass().getClassLoader().getResource(song)).toExternalForm();
        Media hit = new Media(externalForm);
        MediaPlayer mediaPlayer = new MediaPlayer(hit);
        mediaPlayer.play();
    }
    
    private int getRandomDefender() {
        int min = 0;
        int max = defenders.size();
        return random.nextInt(max - min) + min;
    }
    
    private int getRandomBuilding() {
        int min = 0;
        int max = untouchedBuildings.size();
        return random.nextInt(max - min) + min;
    }
    
    private int getRandomAttacker() {
        int min = 0;
        int max = attackers.size();
        return random.nextInt(max - min) + min;
    }
    
    private void handleKeyboard(Scene scene) {
        scene.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case Z:
                    cameraXform2.t.setX(0.0);
                    cameraXform2.t.setY(0.0);
                    camera.setTranslateZ(CAMERA_INITIAL_DISTANCE);
                    cameraXform.ry.setAngle(CAMERA_INITIAL_Y_ANGLE);
                    cameraXform.rx.setAngle(CAMERA_INITIAL_X_ANGLE);
                    break;
                case X:
                    axisGroup.setVisible(!axisGroup.isVisible());
                    break;
                case V:
                    defendersGround.setVisible(!defendersGround.isVisible());
                    break;
                case R:
                    defendersGround.setRotate(defendersGround.getRotate() + 1);
                    break;
                case P:
                    play();
                    break;
                case UP:
                    if (event.isShiftDown()) {
                        camera.setTranslateZ(camera.getTranslateZ() + 10);
                    } else {
                        camera.setTranslateY(camera.getTranslateY() - 10);
                    }
                    break;
                case DOWN:
                    if (event.isShiftDown()) {
                        camera.setTranslateZ(camera.getTranslateZ() - 10);
                    } else {
                        camera.setTranslateY(camera.getTranslateY() + 10);
                    }
                    break;
                case LEFT:
                    camera.setTranslateX(camera.getTranslateX() - 10);
                    break;
                case RIGHT:
                    camera.setTranslateX(camera.getTranslateX() + 10);
                    break;
            }
        });
    }
    
    private void handleMouse(Scene scene) {
        scene.setOnMousePressed(me -> {
            mousePosX = me.getSceneX();
            mousePosY = me.getSceneY();
            mouseOldX = me.getSceneX();
            mouseOldY = me.getSceneY();
        });
        scene.setOnMouseDragged(me -> {
            mouseOldX = mousePosX;
            mouseOldY = mousePosY;
            mousePosX = me.getSceneX();
            mousePosY = me.getSceneY();
            mouseDeltaX = (mousePosX - mouseOldX);
            mouseDeltaY = (mousePosY - mouseOldY);
            
            double modifier = 1.0;
            
            if (me.isControlDown()) {
                modifier = CONTROL_MULTIPLIER;
            }
            if (me.isShiftDown()) {
                modifier = SHIFT_MULTIPLIER;
            }
            if (me.isPrimaryButtonDown()) {
                cameraXform.ry.setAngle(cameraXform.ry.getAngle() - mouseDeltaX*MOUSE_SPEED*modifier*ROTATION_SPEED);
                cameraXform.rx.setAngle(cameraXform.rx.getAngle() + mouseDeltaY*MOUSE_SPEED*modifier*ROTATION_SPEED);
            }
            else if (me.isSecondaryButtonDown()) {
                double z = camera.getTranslateZ();
                double newZ = z + mouseDeltaX*MOUSE_SPEED*modifier;
                camera.setTranslateZ(newZ);
            }
            else if (me.isMiddleButtonDown()) {
                cameraXform2.t.setX(cameraXform2.t.getX() + mouseDeltaX*MOUSE_SPEED*modifier*TRACK_SPEED);
                cameraXform2.t.setY(cameraXform2.t.getY() + mouseDeltaY*MOUSE_SPEED*modifier*TRACK_SPEED);
            }
        });
    }
    
    private void updateScoreboard() {
        Platform.runLater(() -> {
            buildingsLabel.setText("Buildings: " + untouchedBuildings.size());
            gunsLabel.setText("Guns: " + movingGuns.size());
        });
    }
}
