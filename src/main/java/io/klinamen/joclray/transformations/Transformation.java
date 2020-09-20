package io.klinamen.joclray.transformations;

import io.klinamen.joclray.util.FloatVec4;

public interface Transformation {
    FloatVec4 apply(FloatVec4 v);
    Transformation transpose();
    Transformation invert();
}