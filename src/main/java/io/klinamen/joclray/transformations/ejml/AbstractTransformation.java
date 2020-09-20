package io.klinamen.joclray.transformations.ejml;

import io.klinamen.joclray.transformations.Transformation;
import io.klinamen.joclray.util.FloatVec4;
import org.ejml.data.FixedMatrix4_64F;
import org.ejml.data.FixedMatrix4x4_64F;

import static org.ejml.alg.fixed.FixedOps4.mult;

public abstract class AbstractTransformation implements Transformation {
    FixedMatrix4x4_64F m;

    protected abstract FixedMatrix4x4_64F buildMatrix();

    protected FixedMatrix4x4_64F getMatrix() {
        if (m == null) {
            m = buildMatrix();
        }

        return m;
    }

    public FloatVec4 apply(FloatVec4 vec) {
        FixedMatrix4_64F v = new FixedMatrix4_64F(vec.getX(), vec.getY(), vec.getZ(), 1.0f);
        FixedMatrix4_64F vt = new FixedMatrix4_64F();
        mult(getMatrix(), v, vt);
        FloatVec4 floatVec4 = new FloatVec4((float) vt.a1, (float) vt.a2, (float) vt.a3);
        return floatVec4;
    }

    @Override
    public Transformation transpose() {
        return new TransposedTransformation(this);
    }

    @Override
    public Transformation invert() {
        return new InverseTransformation(this);
    }
}

