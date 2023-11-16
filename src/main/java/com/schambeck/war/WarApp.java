package com.schambeck.war;

import com.schambeck.war.attacker.AttackerBox;
import com.schambeck.war.attacker.GunAttacker;
import com.schambeck.war.building.BuildingBox;
import com.schambeck.war.core.Ground;
import com.schambeck.war.core.Xform;
import com.schambeck.war.defender.DefenderBox;
import com.schambeck.war.defender.GunDefender;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.*;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.scene.media.AudioClip;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;

import java.security.SecureRandom;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static javafx.scene.DepthTest.ENABLE;
import static javafx.scene.control.Alert.AlertType.INFORMATION;
import static javafx.scene.paint.Color.*;
import static javafx.stage.Modality.WINDOW_MODAL;
import static javafx.stage.StageStyle.TRANSPARENT;

@Slf4j
public class WarApp extends Application {
    static final String SONG = "song/missile-firing.wav";
    static final String EXTERNAL_FORM = Objects.requireNonNull(WarApp.class.getClassLoader().getResource(SONG)).toExternalForm();
    static final AudioClip CLIP = new AudioClip(EXTERNAL_FORM);
    final AtomicInteger attackerIdGenerator = new AtomicInteger();
    final AtomicInteger attackerGunIdGenerator = new AtomicInteger();
    final AtomicInteger defenderIdGenerator = new AtomicInteger();
    final AtomicInteger defenderGunIdGenerator = new AtomicInteger();
    final AtomicInteger buildingIdGenerator = new AtomicInteger();
    final Group root = new Group();
    final Xform axisGroup = new Xform();
    final Xform world = new Xform();
    final PerspectiveCamera camera = new PerspectiveCamera(true);
    //    final Group cameraGroup = new Group();
    final Xform cameraXform = new Xform();
    final Xform cameraXform2 = new Xform();
    final Xform cameraXform3 = new Xform();
    final Xform scoreboardXform = new Xform();
    CameraView cameraView = CameraView.DEFAULT;
    final Ground defendersGround = new Ground(1000, 20, 500);
    final Ground attackersGround = new Ground(50, 20, 500);
    
    final List<AttackerBox> attackers = new ArrayList<>();
    final List<GunAttacker> gunAttackers = new ArrayList<>();
    final List<DefenderBox> defenders = new ArrayList<>();
    final List<BuildingBox> buildings = new ArrayList<>();
    final List<BuildingBox> untouchedBuildings = new ArrayList<>();
    final List<GunAttacker> guns = new ArrayList<>();
    final List<GunAttacker> movingGuns = new ArrayList<>();
    final List<GunDefender> gunDefenders = new ArrayList<>();
    static final double CAMERA_INITIAL_X = 0;
    static final double CAMERA_INITIAL_HEIGHT = -200;
    static final double CAMERA_INITIAL_DISTANCE = -1400;
    static final double CAMERA_NEAR_CLIP = 0.1;
    static final double CAMERA_FAR_CLIP = 10000.0;
    static final double AXIS_LENGTH = 1500.0;
    static final double CONTROL_MULTIPLIER = 0.1;
    static final double SHIFT_MULTIPLIER = 10.0;
    static final double MOUSE_SPEED = 0.1;
    static final double ROTATION_SPEED = 2.0;
    static final double TRACK_SPEED = 0.3;
    final SecureRandom random = new SecureRandom();
    
    double mousePosX;
    double mousePosY;
    double mouseOldX;
    double mouseOldY;
    double mouseDeltaX;
    double mouseDeltaY;
    Label buildingsLabel;
    Label gunsLabel;
    Label camerasLabel;
    Label attackersLabel;
    Label defendersLabel;
    Label gunAttackerLabel;
    Label gunDefenderLabel;
    final Image buildingMap = new Image("image/building.jpg");
    final Image tankMap = new Image("image/tank.jpg");
    final Image defenderMap = new Image("image/defender.jpg");
    final Image grassMap = new Image("image/grass.jpg");
    final Image sandMap = new Image("image/sand.jpg");
    VBox scoreboard;
    Stage stage;
    
    @Override
    public void start(Stage stage) {
        this.stage = stage;
        root.getChildren().add(world);
        root.setDepthTest(ENABLE);
        
        buildCamera();
        buildLight();
        buildAxes();
        buildDefendersGround();
        buildAttackersGround();
        addDefenders(4);
        addBuildings(5, 2);
        addAttackers(3);
        buildScoreboard();
        updateScoreboard();
        
        Scene scene = new Scene(root, 1024, 768, true);
        scene.setFill(GREY);
        handleKeyboard(scene);
        handleMouse(scene);
        
        stage.setTitle("War Game");
        stage.setMaximized(true);
        stage.setScene(scene);
        stage.show();
        
        scene.setCamera(camera);
        showAutocloseToastHelp(createShortcutsContent());
    }
    
    public static void main(String[] args) {
        launch();
    }
    
    private void buildCamera() {
//        cameraGroup.getChildren().add(camera);
//        root.getChildren().add(cameraGroup);
//        camera.setRotate(45);
//        cameraGroup.setTranslateZ(-75);
        
        root.getChildren().add(cameraXform);
        cameraXform.getChildren().add(cameraXform2);
        cameraXform2.getChildren().add(cameraXform3);
        cameraXform3.getChildren().add(camera);
        camera.setNearClip(CAMERA_NEAR_CLIP);
        camera.setFarClip(CAMERA_FAR_CLIP);
        camera.setTranslateY(CAMERA_INITIAL_HEIGHT);
        camera.setTranslateZ(CAMERA_INITIAL_DISTANCE);
        camera.translateXProperty().addListener((observable, oldValue, newValue) -> updateScoreboard());
        camera.translateYProperty().addListener((observable, oldValue, newValue) -> updateScoreboard());
        camera.translateZProperty().addListener((observable, oldValue, newValue) -> updateScoreboard());
        cameraXform.translateXProperty().addListener((observable, oldValue, newValue) -> updateScoreboard());
        cameraXform.translateYProperty().addListener((observable, oldValue, newValue) -> updateScoreboard());
        cameraXform.translateZProperty().addListener((observable, oldValue, newValue) -> updateScoreboard());
        cameraXform.getRx().angleProperty().addListener((observable, oldValue, newValue) -> updateScoreboard());
        cameraXform.getRy().angleProperty().addListener((observable, oldValue, newValue) -> updateScoreboard());
        cameraXform2.translateXProperty().addListener((observable, oldValue, newValue) -> updateScoreboard());
        cameraXform2.translateYProperty().addListener((observable, oldValue, newValue) -> updateScoreboard());
        cameraXform2.translateZProperty().addListener((observable, oldValue, newValue) -> updateScoreboard());
        cameraXform2.getRx().angleProperty().addListener((observable, oldValue, newValue) -> updateScoreboard());
        cameraXform2.getRy().angleProperty().addListener((observable, oldValue, newValue) -> updateScoreboard());
        cameraXform3.translateXProperty().addListener((observable, oldValue, newValue) -> updateScoreboard());
        cameraXform3.translateYProperty().addListener((observable, oldValue, newValue) -> updateScoreboard());
        cameraXform3.translateZProperty().addListener((observable, oldValue, newValue) -> updateScoreboard());
        cameraXform3.getRx().angleProperty().addListener((observable, oldValue, newValue) -> updateScoreboard());
        cameraXform3.getRy().angleProperty().addListener((observable, oldValue, newValue) -> updateScoreboard());
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
        DefenderBox defender = new DefenderBox(40, 20, 20, node -> updateScoreboard(), node -> resetCamera(), movingGuns, defenderIdGenerator.incrementAndGet(), defenderGunIdGenerator, camera, gunDefenders);
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
        Label label = new Label(prefix + " #" + reference + " ");
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
        double translateX = attackersGround.getTranslateX();
        attacker.setTranslateX(translateX);
        double translateY = ((defendersGround.getHeight() / 2) + (attacker.getHeight() / 2)) * -1;
        attacker.setTranslateY(translateY);
        double translateZ = (defendersGround.getDepth() / 2 * -1) + (defendersGround.getDepth() / (count + 1) * (attackers.size() + 1));
        attacker.setTranslateZ(translateZ);
        attackers.add(attacker);
        Label label = createAttackerLabel("Attacker", attacker, selectedMaterial, redMaterial, attacker.getReference());
        world.getChildren().addAll(attacker, label);
        attacker.translateXProperty().addListener((observable, oldValue, newValue) -> updateScoreboard());
        attacker.translateYProperty().addListener((observable, oldValue, newValue) -> updateScoreboard());
        attacker.translateZProperty().addListener((observable, oldValue, newValue) -> updateScoreboard());
    }
    
    private void buildScoreboard() {
        buildingsLabel = new Label("Buildings: " + buildings.size());
        buildingsLabel.setFont(Font.font("Courier New", FontWeight.BOLD, 30));
        
        gunsLabel = new Label("Guns: " + movingGuns.size());
        gunsLabel.setFont(Font.font("Courier New", FontWeight.BOLD, 30));
        
        camerasLabel = new Label();
        camerasLabel.setFont(Font.font("Courier New", FontWeight.BOLD, 30));
        
        attackersLabel = new Label();
        attackersLabel.setFont(Font.font("Courier New", FontWeight.BOLD, 30));
        
        defendersLabel = new Label();
        defendersLabel.setFont(Font.font("Courier New", FontWeight.BOLD, 30));
        
        gunAttackerLabel = new Label();
        gunAttackerLabel.setFont(Font.font("Courier New", FontWeight.BOLD, 30));
        
        gunDefenderLabel = new Label();
        gunDefenderLabel.setFont(Font.font("Courier New", FontWeight.BOLD, 30));
        
        scoreboard = new VBox(camerasLabel, gunAttackerLabel, gunDefenderLabel);
        scoreboard.setVisible(false);
        scoreboard.setTranslateX(-800);
        scoreboard.setTranslateY((scoreboard.getHeight() + 600) * -1);
        scoreboard.setTranslateZ(attackersGround.getDepth() / 2);
        
        scoreboardXform.getChildren().add(scoreboard);
        root.getChildren().add(scoreboardXform);
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
        GunAttacker gun = attacker.shoot(world, building, guns, movingGuns, untouchedBuildings, camera, cameraView);
        gun.translateXProperty().addListener((observable, oldValue, newValue) -> updateScoreboard());
        gun.translateYProperty().addListener((observable, oldValue, newValue) -> updateScoreboard());
        gun.translateZProperty().addListener((observable, oldValue, newValue) -> updateScoreboard());
        gun.rotateProperty().addListener((observable, oldValue, newValue) -> updateScoreboard());
        gunAttackers.add(gun);
        
        playShootSong();
        if (!defenders.isEmpty()) {
            DefenderBox defender = defenders.get(getRandomDefender());
            defender.defend(world, gun, cameraView);
        }
    }
    
    private void playShootSong() {
        CLIP.play();
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
                case F1:
                    showHelp(event);
                    break;
                case DIGIT0:
                    cameraView = CameraView.DEFAULT;
                    changeCameraView();
                    break;
                case DIGIT1:
                    cameraView = CameraView.DEFAULT_ATTACKER_GUN;
                    changeCameraView();
                    break;
                case DIGIT2:
                    cameraView = CameraView.DEFAULT_DEFENDER_GUN;
                    changeCameraView();
                    break;
                case DIGIT3:
                    cameraView = CameraView.ATTACKER_GUN;
                    changeCameraView();
                    break;
                case DIGIT4:
                    cameraView = CameraView.DEFENDER_GUN;
                    changeCameraView();
                    break;
                case Z:
                    resetCamera();
                    world.setRotate(0);
                    break;
                case X:
                    axisGroup.setVisible(!axisGroup.isVisible());
                    break;
                case V:
                    scoreboard.setVisible(!scoreboard.isVisible());
                    break;
                case R:
                    if (event.isShiftDown()) {
                        world.setRotate(world.getRotate() - 1);
                    } else {
                        world.setRotate(world.getRotate() + 1);
                    }
                    break;
                case P:
                    play();
                    break;
                case UP:
                    if (event.isShiftDown()) {
                        cameraXform.getRx().setAngle(cameraXform.getRx().getAngle() - 10);
                    } else if (event.isControlDown()) {
                        camera.setTranslateZ(camera.getTranslateZ() + 10);
                    } else {
                        camera.setTranslateY(camera.getTranslateY() - 10);
                    }
                    break;
                case DOWN:
                    if (event.isShiftDown()) {
                        cameraXform.getRx().setAngle(cameraXform.getRx().getAngle() + 10);
                    } else if (event.isControlDown()) {
                        camera.setTranslateZ(camera.getTranslateZ() - 10);
                    } else {
                        camera.setTranslateY(camera.getTranslateY() + 10);
                    }
                    break;
                case LEFT:
                    if (event.isShiftDown()) {
                        cameraXform.getRy().setAngle(cameraXform.getRy().getAngle() + 10);
                    } else {
                        camera.setTranslateX(camera.getTranslateX() - 10);
                    }
                    break;
                case RIGHT:
                    if (event.isShiftDown()) {
                        cameraXform.getRy().setAngle(cameraXform.getRy().getAngle() - 10);
                    } else {
                        camera.setTranslateX(camera.getTranslateX() + 10);
                    }
                    break;
            }
        });
    }
    
    private void showHelp(KeyEvent event) {
        String content = createShortcutsContent();
        if (event.isShiftDown()) {
            showAlertHelp(content);
        } else {
            showToastHelp(content);
        }
    }
    
    private void showToastHelp(String content) {
        createToast("Shortcuts:\n\n" + content);
    }
    
    private void showAutocloseToastHelp(String content) {
        createToastAutoclose("Shortcuts:\n\n" + content);
    }
    
    private static void showAlertHelp(String content) {
        Alert alert = new Alert(INFORMATION);
        alert.initModality(WINDOW_MODAL);
        alert.initStyle(TRANSPARENT);
        alert.setTitle("Help");
        alert.setHeaderText("Shortcuts");
        Label label = new Label(content);
        alert.getDialogPane().setContent(label);
        alert.getDialogPane().getContent().setStyle("-fx-font-family: 'Courier New';");
        alert.showAndWait();
    }
    
    private static String createShortcutsContent() {
        return "  F1 - Show Toast Help             | SHIFT+F1 - Show Alert Help\n" +
                "   0 - CameraView.DEFAULT          |        P - Play\n" +
                "   1 - CameraView.DEFAULT_ATTACKER |        R - Rotate\n" +
                "   2 - CameraView.DEFAULT_DEFENDER |        V - Show Scoreboard\n" +
                "   3 - CameraView.ATTACKER         |        X - Show Axis\n" +
                "   4 - CameraView.DEFENDER         |        Z - Reset Camera\n\n" +
                "  UP - Move Camera UP              |     DOWN - Move Camera DOWN\n" +
                "LEFT - Move Camera LEFT            |    RIGHT - Move Camera RIGHT";
    }
    
    private void changeCameraView() {
        log.debug("CameraView: {}", cameraView);
        createToastAutoclose("CameraView: ".concat(cameraView.name()));
        resetCamera();
    }
    
    private void createToast(String toastMsg) {
        int fadeInTime = 500;
        Toast.makeText(stage, toastMsg, null, fadeInTime, null);
    }
    
    private void createToastAutoclose(String toastMsg) {
        int toastMsgTime = 2000;
        int fadeInTime = 500;
        int fadeOutTime = 500;
        Toast.makeText(stage, toastMsg, toastMsgTime, fadeInTime, fadeOutTime);
    }
    
    private void resetCamera() {
        cameraXform2.getT().setX(0.0);
        cameraXform2.getT().setY(0.0);
        camera.setTranslateX(CAMERA_INITIAL_X);
        camera.setTranslateY(CAMERA_INITIAL_HEIGHT);
        camera.setTranslateZ(CAMERA_INITIAL_DISTANCE);
        cameraXform.getRx().setAngle(0);
        cameraXform.getRy().setAngle(0);
        switch (cameraView) {
            case DEFAULT:
            case DEFAULT_ATTACKER_GUN:
            case DEFAULT_DEFENDER_GUN:
                cameraXform.getRy().setAngle(0);
                scoreboardXform.getRy().setAngle(0);
                break;
            case ATTACKER_GUN:
                cameraXform.getRy().setAngle(-84);
                scoreboardXform.getRy().setAngle(-90);
                break;
            case DEFENDER_GUN:
                cameraXform.getRy().setAngle(84);
                scoreboardXform.getRy().setAngle(90);
                break;
        }
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
                cameraXform.getRy().setAngle(cameraXform.getRy().getAngle() - mouseDeltaX * MOUSE_SPEED * modifier * ROTATION_SPEED);
                cameraXform.getRx().setAngle(cameraXform.getRx().getAngle() + mouseDeltaY * MOUSE_SPEED * modifier * ROTATION_SPEED);
            } else if (me.isSecondaryButtonDown()) {
                double z = camera.getTranslateZ();
                double newZ = z + mouseDeltaX * MOUSE_SPEED * modifier;
                camera.setTranslateZ(newZ);
            } else if (me.isMiddleButtonDown()) {
                cameraXform2.getT().setX(cameraXform2.getT().getX() + mouseDeltaX * MOUSE_SPEED * modifier * TRACK_SPEED);
                cameraXform2.getT().setY(cameraXform2.getT().getY() + mouseDeltaY * MOUSE_SPEED * modifier * TRACK_SPEED);
            }
        });
    }
    
    private void updateScoreboard() {
        Platform.runLater(() -> {
            buildingsLabel.setText("Buildings: " + untouchedBuildings.size());
            gunsLabel.setText("Guns: " + movingGuns.size());
            camerasLabel.setText("Camera          = X: " + DecimalFormat.getInstance().format(camera.getTranslateX()) + " Y: " + DecimalFormat.getInstance().format(camera.getTranslateY()) + " Z: " + DecimalFormat.getInstance().format(camera.getTranslateZ()) + " R: " + DecimalFormat.getInstance().format(camera.getRotate()) + "\n" + "CameraXform     = X: " + DecimalFormat.getInstance().format(cameraXform.getTranslateX()) + " Y: " + DecimalFormat.getInstance().format(cameraXform.getTranslateY()) + " Z: " + DecimalFormat.getInstance().format(cameraXform.getTranslateZ()) + " R: " + cameraXform.getRotate() + " AX: " + cameraXform.getRx().getAngle() + " AY: " + cameraXform.getRy().getAngle() + "\n" + "CameraXform2    = X: " + DecimalFormat.getInstance().format(cameraXform2.getTranslateX()) + " Y: " + DecimalFormat.getInstance().format(cameraXform2.getTranslateY()) + " Z: " + DecimalFormat.getInstance().format(cameraXform2.getTranslateZ()) + " R: " + cameraXform2.getRotate() + " AX: " + DecimalFormat.getInstance().format(cameraXform2.getRx().getAngle()) + " AY: " + DecimalFormat.getInstance().format(cameraXform2.getRy().getAngle()) + "\n" + "CameraXform3    = X: " + DecimalFormat.getInstance().format(cameraXform3.getTranslateX()) + " Y: " + cameraXform3.getTranslateY() + " Z: " + cameraXform3.getTranslateZ() + " R: " + cameraXform3.getRotate() + " AX: " + cameraXform3.getRx().getAngle() + " AY: " + cameraXform3.getRy().getAngle());
            String defendersText = defenders.stream().map(defender -> "Defender #" + defender.getReference() + "     = X: " + DecimalFormat.getInstance().format(defender.getTranslateX()) + " Y: " + DecimalFormat.getInstance().format(defender.getTranslateY()) + " Z: " + DecimalFormat.getInstance().format(defender.getTranslateZ()) + " R: " + DecimalFormat.getInstance().format(defender.getRotate())).collect(Collectors.joining("\n"));
            defendersLabel.setText(defendersText);
            String attackersText = attackers.stream().map(attacker -> "Attacker #" + attacker.getReference() + "     = X: " + DecimalFormat.getInstance().format(attacker.getTranslateX()) + " Y: " + DecimalFormat.getInstance().format(attacker.getTranslateY()) + " Z: " + DecimalFormat.getInstance().format(attacker.getTranslateZ()) + " R: " + DecimalFormat.getInstance().format(attacker.getRotate())).collect(Collectors.joining("\n"));
            attackersLabel.setText(attackersText);
            String gunDefenderText = gunDefenders.stream().map(defender -> "GunDefender #" + defender.getReference() + "  = X: " + DecimalFormat.getInstance().format(defender.getTranslateX()) + " Y: " + DecimalFormat.getInstance().format(defender.getTranslateY()) + " Z: " + DecimalFormat.getInstance().format(defender.getTranslateZ()) + " R: " + DecimalFormat.getInstance().format(defender.getRotate())).collect(Collectors.joining("\n"));
            gunDefenderLabel.setText(gunDefenderText);
            String gunAttackerText = guns.stream().map(attacker -> "GunAttacker #" + attacker.getReference() + "  = X: " + DecimalFormat.getInstance().format(attacker.getTranslateX()) + " Y: " + DecimalFormat.getInstance().format(attacker.getTranslateY()) + " Z: " + DecimalFormat.getInstance().format(attacker.getTranslateZ()) + " R: " + DecimalFormat.getInstance().format(attacker.getRotate())).collect(Collectors.joining("\n"));
            gunAttackerLabel.setText(gunAttackerText);
        });
    }
}
