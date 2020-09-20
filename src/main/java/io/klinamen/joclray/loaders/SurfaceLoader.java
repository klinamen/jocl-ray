package io.klinamen.joclray.loaders;

import io.klinamen.joclray.geom.Surface;

import java.io.IOException;
import java.io.InputStream;

public interface SurfaceLoader {
    Surface load(InputStream inputStream) throws IOException;
}
