package io.klinamen.joclray.transformations.ejml;

import org.ejml.data.FixedMatrix4x4_64F;

public class RotateXTransformation extends AbstractTransformation {
    private final double angleRad;

    public RotateXTransformation(double angleRad) {
        this.angleRad = angleRad;
    }

    public static RotateXTransformation withGradAngle(double angleGrad){
        return new RotateXTransformation(angleGrad * Math.PI / 180);
    }

    @Override
    protected FixedMatrix4x4_64F buildMatrix() {
        double cos = Math.cos(angleRad);
        double sin = Math.sin(angleRad);

        FixedMatrix4x4_64F m = new FixedMatrix4x4_64F();
        m.a11 = 1;

        m.a22 = cos;
        m.a32 = sin;

        m.a23 = -sin;
        m.a33 = cos;

        m.a44 = 1;

        return m;
    }
}
