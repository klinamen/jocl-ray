package io.klinamen.joclray.transformations.ejml;

import org.ejml.data.FixedMatrix4x4_64F;

public class TranslateTransformation extends AbstractTransformation {
    private final double xTrans;
    private final double yTrans;
    private final double zTrans;

    public TranslateTransformation(double xTrans, double yTrans, double zTrans) {
        this.xTrans = xTrans;
        this.yTrans = yTrans;
        this.zTrans = zTrans;
    }

    @Override
    protected FixedMatrix4x4_64F buildMatrix() {
        FixedMatrix4x4_64F m = new FixedMatrix4x4_64F();
        m.a11 = 1;
        m.a22 = 1;
        m.a33 = 1;
        m.a44 = 1;

        m.a14 = xTrans;
        m.a24 = yTrans;
        m.a34 = zTrans;

        return m;
    }
}
