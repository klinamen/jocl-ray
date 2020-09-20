package io.klinamen.joclray.transformations.ejml;

import org.ejml.data.FixedMatrix4x4_64F;

public class ScaleTransformation extends AbstractTransformation {
    private final double xScale;
    private final double yScale;
    private final double zScale;

    public ScaleTransformation(double xScale, double yScale, double zScale) {
        this.xScale = xScale;
        this.yScale = yScale;
        this.zScale = zScale;
    }

    @Override
    protected FixedMatrix4x4_64F buildMatrix() {
        FixedMatrix4x4_64F m = new FixedMatrix4x4_64F();
        m.a11 = xScale;
        m.a22 = yScale;
        m.a33 = zScale;
        m.a44 = 1;

        return m;
    }
}
