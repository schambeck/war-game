package com.schambeck.war.building;

import javafx.scene.shape.Box;
import lombok.Getter;

@Getter
public class BuildingBox extends Box {
    private final Integer reference;
    public BuildingBox(double width, double height, double depth, Integer reference) {
        super(width, height, depth);
        this.reference = reference;
    }
}
