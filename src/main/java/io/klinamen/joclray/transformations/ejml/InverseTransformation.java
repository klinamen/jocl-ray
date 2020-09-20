package io.klinamen.joclray.transformations.ejml;

import org.ejml.alg.fixed.FixedOps4;
import org.ejml.data.FixedMatrix4x4_64F;

public class InverseTransformation extends AbstractTransformation {
    private final AbstractTransformation t;

    public InverseTransformation(AbstractTransformation t) {
        this.t = t;
    }

    @Override
    protected FixedMatrix4x4_64F buildMatrix() {
        FixedMatrix4x4_64F inv = new FixedMatrix4x4_64F();
        FixedOps4.invert(t.getMatrix(), inv);
        return inv;
    }
}

