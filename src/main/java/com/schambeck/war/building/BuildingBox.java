package com.schambeck.war.building;

import javafx.scene.shape.Box;

public class BuildingBox extends Box {
    private final Integer reference;
    public BuildingBox(double width, double height, double depth, Integer reference) {
        super(width, height, depth);
        this.reference = reference;
    }
    
    public Integer getReference() {
        return reference;
    }
}
