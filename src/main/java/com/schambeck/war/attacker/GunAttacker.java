package com.schambeck.war.attacker;

import javafx.animation.Transition;
import javafx.scene.shape.Cylinder;
import lombok.Getter;
import lombok.Setter;

@Getter
public class GunAttacker extends Cylinder {
    private final AttackerBox attacker;
    private final Integer reference;
    @Setter
    private Transition transition;
    public GunAttacker(double radius, double height, AttackerBox attacker, Integer reference) {
        super(radius, height);
        this.attacker = attacker;
        this.reference = reference;
    }
}
