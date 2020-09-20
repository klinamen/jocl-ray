package io.klinamen.joclray.transformations.ejml;

import org.ejml.data.FixedMatrix4x4_64F;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.ejml.alg.fixed.FixedOps4.mult;

public class CompositeTransformation extends AbstractTransformation {
    private final List<AbstractTransformation> chain = new ArrayList<>();

    public CompositeTransformation(AbstractTransformation... transformations) {
        chain.addAll(Arrays.asList(transformations));
    }

    public CompositeTransformation(Iterable<AbstractTransformation> transformations) {
        transformations.forEach(chain::add);
    }

    public CompositeTransformation() {

    }

    public CompositeTransformation add(AbstractTransformation t) {
        chain.add(t);
        return this;
    }

    @Override
    protected FixedMatrix4x4_64F buildMatrix() {
        FixedMatrix4x4_64F m = IdentityTransformation.IdentityMatrix;
        for (int i = chain.size() - 1; i >= 0; i--) {
            AbstractTransformation t = chain.get(i);
            FixedMatrix4x4_64F r = new FixedMatrix4x4_64F();
            mult(m, t.getMatrix(), r);
            m = r;
        }

        return m;
    }
}
