package io.klinamen.joclray.transformations.ejml;

import org.ejml.data.FixedMatrix4x4_64F;

public class IdentityTransformation extends AbstractTransformation {
    public static FixedMatrix4x4_64F IdentityMatrix = new FixedMatrix4x4_64F();

    static {
        IdentityMatrix.a11 = 1;
        IdentityMatrix.a22 = 1;
        IdentityMatrix.a33 = 1;
        IdentityMatrix.a44 = 1;
    }

    @Override
    protected FixedMatrix4x4_64F buildMatrix() {
        return IdentityMatrix;
    }
}
