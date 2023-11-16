package com.schambeck.war.defender;

import com.schambeck.war.CameraView;
import com.schambeck.war.attacker.GunAttacker;
import com.schambeck.war.core.Xform;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.Node;
import javafx.scene.PerspectiveCamera;
import javafx.scene.media.AudioClip;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static javafx.animation.Animation.INDEFINITE;
import static javafx.scene.paint.Color.DARKVIOLET;
import static javafx.scene.paint.Color.VIOLET;
import static javafx.util.Duration.millis;

@Slf4j
public class DefenderBox extends Box {
    private static final String SONG = "song/explosion.wav";
    private static final String EXTERNAL_FORM = Objects.requireNonNull(DefenderBox.class.getClassLoader().getResource(SONG)).toExternalForm();
    private static final AudioClip CLIP = new AudioClip(EXTERNAL_FORM);
    private final Consumer<Node> consumer;
    private final Consumer<Node> onHit;
    private final List<GunAttacker> movingGuns;
    @Getter
    private final Integer reference;
    private final AtomicInteger defenderGunIdGenerator;
    private final PerspectiveCamera camera;
    private final List<GunDefender> gunDefenders;
    
    public DefenderBox(double width, double height, double depth, Consumer<Node> consumer, Consumer<Node> onHit, List<GunAttacker> movingGuns, Integer reference, AtomicInteger defenderGunIdGenerator, PerspectiveCamera camera, List<GunDefender> gunDefenders) {
        super(width, height, depth);
        this.consumer = consumer;
        this.onHit = onHit;
        this.movingGuns = movingGuns;
        this.reference = reference;
        this.defenderGunIdGenerator = defenderGunIdGenerator;
        this.camera = camera;
        this.gunDefenders = gunDefenders;
    }
    
    public void defend(Xform world, GunAttacker gunAttacker, CameraView cameraView) {
        PhongMaterial grayMaterial = new PhongMaterial();
        grayMaterial.setDiffuseColor(DARKVIOLET);
        grayMaterial.setSpecularColor(VIOLET);
        GunDefender gunDefender = new GunDefender(4, 12, this, defenderGunIdGenerator.incrementAndGet());
        gunDefender.setMaterial(grayMaterial);
        gunDefender.setTranslateX(getTranslateX());
        gunDefender.setTranslateY(getTranslateY());
        gunDefender.setTranslateZ(getTranslateZ());
        gunDefender.translateXProperty().addListener((observable, oldValue, newValue) -> consumer.accept(gunDefender));
        gunDefender.translateYProperty().addListener((observable, oldValue, newValue) -> consumer.accept(gunDefender));
        gunDefender.translateZProperty().addListener((observable, oldValue, newValue) -> consumer.accept(gunDefender));
        
        switch (cameraView) {
            case DEFAULT_DEFENDER_GUN:
                gunDefender.translateXProperty().addListener((observable, oldValue, newValue) -> camera.setTranslateX(newValue.doubleValue()));
                gunDefender.translateYProperty().addListener((observable, oldValue, newValue) -> camera.setTranslateY(newValue.doubleValue()));
                gunDefender.translateZProperty().addListener((observable, oldValue, newValue) -> camera.setTranslateZ(newValue.doubleValue() - 600));
                break;
            case DEFENDER_GUN:
                gunDefender.translateXProperty().addListener((observable, oldValue, newValue) -> camera.setTranslateZ(newValue.doubleValue() - 200));
                gunDefender.translateYProperty().addListener((observable, oldValue, newValue) -> camera.setTranslateY(newValue.doubleValue()));
                gunDefender.translateZProperty().addListener((observable, oldValue, newValue) -> camera.setTranslateX(-newValue.doubleValue()));
                break;
        }
        
        gunDefender.setRotate(90);
        gunDefenders.add(gunDefender);
        world.getChildren().addAll(gunDefender);
        Timeline timeline = new Timeline(new KeyFrame(millis(3), event -> runTranslateTransition(world, gunAttacker, gunDefender)));
        gunDefender.setTimeline(timeline);
        timeline.setCycleCount(INDEFINITE);
        timeline.play();
    }
    
    private void playExplosionSong() {
        CLIP.play();
    }
    
    private void runTranslateTransition(Xform world, GunAttacker gunAttacker, GunDefender gunDefender) {
        if (gunAttacker.getTranslateX() >= gunAttacker.getAttacker().getTranslateX() - 70) {
            return;
        }
        if (hit(gunAttacker, gunDefender)) {
            log.debug("Intercepted Gun #" + gunAttacker.getReference() + " Attacker #" + gunAttacker.getAttacker().getReference());
            onHit.accept(this);
            playExplosionSong();
            if (gunAttacker.getTransition() != null) {
                gunAttacker.getTransition().stop();
            }
            if (gunDefender.getTimeline() != null) {
                gunDefender.getTimeline().stop();
            }
            world.getChildren().remove(gunAttacker);
            world.getChildren().remove(gunDefender);
            movingGuns.remove(gunAttacker);
            consumer.accept(this);
            return;
        }
        if (gunAttacker.getTranslateX() > gunDefender.getTranslateX()) {
            gunDefender.setTranslateX(gunDefender.getTranslateX() + 1);
            if (gunAttacker.getTranslateY() > gunDefender.getTranslateY()) {
                gunDefender.setTranslateY(gunDefender.getTranslateY() + 1);
            } else if (gunAttacker.getTranslateY() < gunDefender.getTranslateY()) {
                gunDefender.setTranslateY(gunDefender.getTranslateY() - 1);
            }
            if (gunAttacker.getTranslateZ() > gunDefender.getTranslateZ()) {
                gunDefender.setTranslateZ(gunDefender.getTranslateZ() + 1);
            } else if (gunAttacker.getTranslateZ() < gunDefender.getTranslateZ()) {
                gunDefender.setTranslateZ(gunDefender.getTranslateZ() - 1);
            }
        } else {
            gunDefender.setTranslateX(gunDefender.getTranslateX() + 1);
            gunDefender.setTranslateY(gunDefender.getTranslateY() - 1);
            gunDefender.setTranslateZ(gunDefender.getTranslateZ() + 1);
        }
    }
    
    private boolean hit(GunAttacker gunAttacker, GunDefender gunDefender) {
        return hit(gunDefender.getTranslateX(), gunAttacker.getTranslateX())
                && hit(gunDefender.getTranslateY(), gunAttacker.getTranslateY())
                && hit(gunDefender.getTranslateZ(), gunAttacker.getTranslateZ());
    }
    
    private boolean hit(double translateGunAttacker, double translateGunDefender) {
        double minMargin = translateGunAttacker - 5;
        double maxMargin = translateGunAttacker + 5;
        return translateGunDefender >= minMargin && translateGunDefender <= maxMargin;
    }
}
