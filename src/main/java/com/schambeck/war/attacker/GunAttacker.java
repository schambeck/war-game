package com.schambeck.war.attacker;

import javafx.animation.Transition;
import javafx.scene.shape.Cylinder;

public class GunAttacker extends Cylinder {
    private final AttackerBox attacker;
    private final Integer reference;
    private Transition transition;
    public GunAttacker(double radius, double height, AttackerBox attacker, Integer reference) {
        super(radius, height);
        this.attacker = attacker;
        this.reference = reference;
    }
    
    public AttackerBox getAttacker() {
        return attacker;
    }
    
    public Transition getTransition() {
        return transition;
    }
    
    public void setTransition(Transition transition) {
        this.transition = transition;
    }
    
    public Integer getReference() {
        return reference;
    }
}
