package io.klinamen.joclray.transformations.ejml;

import org.ejml.alg.fixed.FixedOps4;
import org.ejml.data.FixedMatrix4x4_64F;

public class TransposedTransformation extends AbstractTransformation {
    private final AbstractTransformation t;

    public TransposedTransformation(AbstractTransformation t) {
        this.t = t;
    }

    @Override
    protected FixedMatrix4x4_64F buildMatrix() {
        FixedMatrix4x4_64F inv = new FixedMatrix4x4_64F();
        FixedOps4.transpose(t.getMatrix(), inv);
        return inv;
    }
}
