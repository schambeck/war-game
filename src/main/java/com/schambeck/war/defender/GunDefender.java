package com.schambeck.war.defender;

import javafx.animation.Timeline;
import javafx.scene.shape.Cylinder;

public class GunDefender extends Cylinder {
    private final DefenderBox defender;
    private final Integer reference;
    private Timeline timeline;
    public GunDefender(double radius, double height, DefenderBox defender, Integer reference) {
        super(radius, height);
        this.defender = defender;
        this.reference = reference;
        
    }
    
    public DefenderBox getDefender() {
        return defender;
    }
    
    public Integer getReference() {
        return reference;
    }
    
    public Timeline getTimeline() {
        return timeline;
    }
    
    public void setTimeline(Timeline timeline) {
        this.timeline = timeline;
    }
}
