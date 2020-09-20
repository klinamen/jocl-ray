package io.klinamen.joclray.transformations.ejml;

import org.ejml.data.FixedMatrix4x4_64F;

public class RotateZTransformation extends AbstractTransformation {
    private final double angleRad;

    public RotateZTransformation(double angleRad) {
        this.angleRad = angleRad;
    }

    public static RotateZTransformation withGradAngle(double angleGrad){
        return new RotateZTransformation(angleGrad * Math.PI / 180);
    }

    @Override
    protected FixedMatrix4x4_64F buildMatrix() {
        double cos = Math.cos(angleRad);
        double sin = Math.sin(angleRad);

        FixedMatrix4x4_64F m = new FixedMatrix4x4_64F();
        m.a11 = cos;
        m.a21 = sin;

        m.a12 = -sin;
        m.a22 = cos;

        m.a33 = 1;

        m.a44 = 1;

        return m;
    }
}
