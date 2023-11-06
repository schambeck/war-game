package com.schambeck.war.core;

import javafx.scene.Group;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;
import lombok.Getter;

public class Xform extends Group {
    @Getter
    private Translate t  = new Translate();
    private Translate p  = new Translate();
    private Translate ip = new Translate();
    @Getter
    private Rotate rx = new Rotate();
    @Getter
    private Rotate ry = new Rotate();
    private Rotate rz = new Rotate();
    private Scale s = new Scale();

    public Xform() {
        super();
        initAxis();
        getTransforms().addAll(t, rz, ry, rx, s);
    }
    
    private void initAxis() {
        rx.setAxis(Rotate.X_AXIS);
        ry.setAxis(Rotate.Y_AXIS);
        rz.setAxis(Rotate.Z_AXIS);
    }
    
    @Override public String toString() {
        return "Xform[t = (" +
                           t.getX() + ", " +
                           t.getY() + ", " +
                           t.getZ() + ")  " +
                           "r = (" +
                           rx.getAngle() + ", " +
                           ry.getAngle() + ", " +
                           rz.getAngle() + ")  " +
                           "s = (" +
                           s.getX() + ", " +
                           s.getY() + ", " +
                           s.getZ() + ")  " +
                           "p = (" +
                           p.getX() + ", " +
                           p.getY() + ", " +
                           p.getZ() + ")  " +
                           "ip = (" +
                           ip.getX() + ", " +
                           ip.getY() + ", " +
                           ip.getZ() + ")]";
    }
}
