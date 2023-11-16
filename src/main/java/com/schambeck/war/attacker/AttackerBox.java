package com.schambeck.war.attacker;

import com.schambeck.war.CameraView;
import com.schambeck.war.building.BuildingBox;
import com.schambeck.war.core.Xform;
import javafx.animation.ParallelTransition;
import javafx.animation.PathTransition;
import javafx.animation.TranslateTransition;
import javafx.scene.Camera;
import javafx.scene.Node;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.CubicCurveTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.util.Duration;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static javafx.scene.paint.Color.*;

@Slf4j
public class AttackerBox extends Box {
    private final Consumer<Node> consumer;
    @Getter
    private final Integer reference;
    private final AtomicInteger attackerGunIdGenerator;
    
    public AttackerBox(double width, double height, double depth, Consumer<Node> consumer, Integer reference, AtomicInteger attackerGunIdGenerator) {
        super(width, height, depth);
        this.consumer = consumer;
        this.reference = reference;
        this.attackerGunIdGenerator = attackerGunIdGenerator;
    }
    
    public GunAttacker shoot(Xform world, BuildingBox target, List<GunAttacker> guns, List<GunAttacker> movingGuns, List<BuildingBox> untouchedBuildings, Camera camera, CameraView cameraView) {
        PhongMaterial grayMaterial = new PhongMaterial();
        grayMaterial.setDiffuseColor(DARKGOLDENROD);
        grayMaterial.setSpecularColor(YELLOW);
        GunAttacker gunAttacker = new GunAttacker(4, 12, this, attackerGunIdGenerator.incrementAndGet());
        log.debug("Shoot Gun #" + gunAttacker.getReference() + " Building #" + target.getReference());
        gunAttacker.setMaterial(grayMaterial);
        gunAttacker.setTranslateX(getTranslateX());
        gunAttacker.setTranslateY(-50);
        gunAttacker.translateXProperty().addListener((observable, oldValue, newValue) -> consumer.accept(gunAttacker));
        gunAttacker.translateYProperty().addListener((observable, oldValue, newValue) -> consumer.accept(gunAttacker));
        gunAttacker.translateZProperty().addListener((observable, oldValue, newValue) -> consumer.accept(gunAttacker));
        gunAttacker.rotateProperty().addListener((observable, oldValue, newValue) -> consumer.accept(gunAttacker));
        
        switch (cameraView) {
            case DEFAULT_ATTACKER_GUN:
                gunAttacker.translateXProperty().addListener((observable, oldValue, newValue) -> camera.setTranslateX(newValue.doubleValue()));
                gunAttacker.translateYProperty().addListener((observable, oldValue, newValue) -> camera.setTranslateY(newValue.doubleValue()));
                gunAttacker.translateZProperty().addListener((observable, oldValue, newValue) -> camera.setTranslateZ(newValue.doubleValue() - 600));
                break;
            case ATTACKER_GUN:
                gunAttacker.translateXProperty().addListener((observable, oldValue, newValue) -> camera.setTranslateZ(-newValue.doubleValue() - 600));
                gunAttacker.translateYProperty().addListener((observable, oldValue, newValue) -> camera.setTranslateY(newValue.doubleValue()));
                gunAttacker.translateZProperty().addListener((observable, oldValue, newValue) -> camera.setTranslateX(newValue.doubleValue()));
                break;
        }
        
        world.getChildren().addAll(gunAttacker);
        animateGun(target, gunAttacker, guns, movingGuns, untouchedBuildings);
        return gunAttacker;
    }
    
    private void animateGun(BuildingBox building, GunAttacker gun, List<GunAttacker> guns, List<GunAttacker> movingGuns, List<BuildingBox> untouchedBuildings) {
        Path path = new Path();
        path.getElements().add(new MoveTo(getTranslateX(), getTranslateY()));
        path.getElements().add(new CubicCurveTo(getTranslateX(), getTranslateY(), 200, -200, building.getTranslateX(), building.getTranslateY()));
        PathTransition pathTransition = new PathTransition();
        pathTransition.setDuration(Duration.millis(4000));
        pathTransition.setPath(path);
        pathTransition.setNode(gun);
        pathTransition.setOrientation(PathTransition.OrientationType.ORTHOGONAL_TO_TANGENT);
        
        TranslateTransition translateTransition = new TranslateTransition(Duration.millis(4000), gun);
        translateTransition.setFromZ(getTranslateZ());
        translateTransition.setToZ(building.getTranslateZ());
        
        ParallelTransition transition = new ParallelTransition(gun, pathTransition, translateTransition);
        
        gun.setTransition(transition);
        guns.add(gun);
        movingGuns.add(gun);
        consumer.accept(this);
        transition.play();
        transition.setOnFinished(event -> {
            movingGuns.remove(gun);
            if (hit(building, gun)) {
                log.debug("Hit Building #" + building.getReference() + " Gun #" + gun.getReference());
                untouchedBuildings.remove(building);
                consumer.accept(this);
                PhongMaterial material = new PhongMaterial();
                material.setDiffuseColor(DARKORANGE);
                material.setSpecularColor(ORANGE);
                building.setMaterial(material);
            } else {
                log.debug("Not Hit Building #" + building.getReference() + " Gun #" + gun.getReference());
            }
            consumer.accept(this);
        });
    }

    private boolean hit(Node target, Node gun) {
        return hit(gun.getTranslateX(), target.getTranslateX())
                && hit(gun.getTranslateY(), target.getTranslateY())
                && hit(gun.getTranslateZ(), target.getTranslateZ());
    }
    
    private boolean hit(double translateGun, double translateTarget) {
        double minMargin = translateGun - 40;
        double maxMargin = translateGun + 40;
        return translateTarget >= minMargin && translateTarget <= maxMargin;
    }
    
}
