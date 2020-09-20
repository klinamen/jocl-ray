package io.klinamen.joclray.transformations.ejml;

import org.ejml.data.FixedMatrix4x4_64F;

public class RotateYTransformation extends AbstractTransformation {
    private final double angleRad;

    public RotateYTransformation(double angleRad) {
        this.angleRad = angleRad;
    }

    public static RotateYTransformation withGradAngle(double angleGrad){
        return new RotateYTransformation(angleGrad * Math.PI / 180);
    }

    @Override
    protected FixedMatrix4x4_64F buildMatrix() {
        double cos = Math.cos(angleRad);
        double sin = Math.sin(angleRad);

        FixedMatrix4x4_64F m = new FixedMatrix4x4_64F();
        m.a11 = cos;
        m.a31 = -sin;

        m.a22 = 1;

        m.a13 = sin;
        m.a33 = cos;

        m.a44 = 1;

        return m;
    }
}
