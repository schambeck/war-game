package com.schambeck.war.defender;

import javafx.animation.Timeline;
import javafx.scene.shape.Cylinder;
import lombok.Getter;
import lombok.Setter;

@Getter
public class GunDefender extends Cylinder {
    private final DefenderBox defender;
    private final Integer reference;
    @Setter
    private Timeline timeline;
    public GunDefender(double radius, double height, DefenderBox defender, Integer reference) {
        super(radius, height);
        this.defender = defender;
        this.reference = reference;
    }
}
