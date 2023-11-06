package com.schambeck.war.defender;

import com.schambeck.war.attacker.GunAttacker;
import com.schambeck.war.core.Xform;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.Node;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
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
    private final Consumer<Node> consumer;
    private final List<GunAttacker> movingGuns;
    @Getter
    private final Integer reference;
    private final AtomicInteger defenderGunIdGenerator;
    
    public DefenderBox(double width, double height, double depth, Consumer<Node> consumer, List<GunAttacker> movingGuns, Integer reference, AtomicInteger defenderGunIdGenerator) {
        super(width, height, depth);
        this.consumer = consumer;
        this.movingGuns = movingGuns;
        this.reference = reference;
        this.defenderGunIdGenerator = defenderGunIdGenerator;
    }
    
    public void defend(Xform world, GunAttacker gun) {
        PhongMaterial grayMaterial = new PhongMaterial();
        grayMaterial.setDiffuseColor(DARKVIOLET);
        grayMaterial.setSpecularColor(VIOLET);
        GunDefender defenderGun = new GunDefender(4, 12, this, defenderGunIdGenerator.incrementAndGet());
        defenderGun.setMaterial(grayMaterial);
        defenderGun.setTranslateX(getTranslateX());
        defenderGun.setTranslateY(getTranslateY());
        defenderGun.setTranslateZ(getTranslateZ());
        defenderGun.setRotate(90);
        world.getChildren().addAll(defenderGun);
        Timeline timeline = new Timeline(new KeyFrame(millis(3), event -> runTranslateTransition(world, gun, defenderGun)));
        defenderGun.setTimeline(timeline);
        timeline.setCycleCount(INDEFINITE);
        timeline.play();
    }
    
    private void playExplosionSong() {
        String song = "song/explosion.wav";
        log.debug("Playing explosion: " + song);
        String externalForm = Objects.requireNonNull(getClass().getClassLoader().getResource(song)).toExternalForm();
        Media hit = new Media(externalForm);
        MediaPlayer mediaPlayer = new MediaPlayer(hit);
        mediaPlayer.play();
    }
    
    private void runTranslateTransition(Xform world, GunAttacker gun, GunDefender defenderGun) {
        if (gun.getTranslateX() >= gun.getAttacker().getTranslateX() - 70) {
            return;
        }
        if (hit(gun, defenderGun)) {
            log.debug("Intercepted - Gun #" + gun.getReference() + " Attacker #" + gun.getAttacker().getReference());
            playExplosionSong();
            if (gun.getTransition() != null) {
                gun.getTransition().stop();
            }
            if (defenderGun.getTimeline() != null) {
                defenderGun.getTimeline().stop();
            }
            world.getChildren().remove(gun);
            world.getChildren().remove(defenderGun);
            movingGuns.remove(gun);
            consumer.accept(this);
            return;
        }
        if (gun.getTranslateX() > defenderGun.getTranslateX()) {
            defenderGun.setTranslateX(defenderGun.getTranslateX() + 1);
            if (gun.getTranslateY() > defenderGun.getTranslateY()) {
                defenderGun.setTranslateY(defenderGun.getTranslateY() + 1);
            } else if (gun.getTranslateY() < defenderGun.getTranslateY()) {
                defenderGun.setTranslateY(defenderGun.getTranslateY() - 1);
            }
            if (gun.getTranslateZ() > defenderGun.getTranslateZ()) {
                defenderGun.setTranslateZ(defenderGun.getTranslateZ() + 1);
            } else if (gun.getTranslateZ() < defenderGun.getTranslateZ()) {
                defenderGun.setTranslateZ(defenderGun.getTranslateZ() - 1);
            }
        } else {
            defenderGun.setTranslateX(defenderGun.getTranslateX() + 1);
            defenderGun.setTranslateY(defenderGun.getTranslateY() - 1);
            defenderGun.setTranslateZ(defenderGun.getTranslateZ() + 1);
        }
    }
    
    private boolean hit(GunAttacker gun, GunDefender defenderGun) {
        return hit(defenderGun.getTranslateX(), gun.getTranslateX())
                && hit(defenderGun.getTranslateY(), gun.getTranslateY())
                && hit(defenderGun.getTranslateZ(), gun.getTranslateZ());
    }
    
    private boolean hit(double translateGun, double translateDefenderGun) {
        double minMargin = translateGun - 5;
        double maxMargin = translateGun + 5;
        return translateDefenderGun >= minMargin && translateDefenderGun <= maxMargin;
    }
}
