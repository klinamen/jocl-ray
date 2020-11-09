package io.klinamen.joclray.geom;

import io.klinamen.joclray.util.FloatVec4;
import org.junit.Assert;
import org.junit.Test;

public class AABBTest {

    @Test
    public void getCenter() {
        FloatVec4 c = new FloatVec4(1, 2, 3);
        FloatVec4 e = new FloatVec4(3, 3, 3);
        AABB bb = new AABB(c.minus(e), c.plus(e));

        Assert.assertArrayEquals(c.getArray(), bb.getCenter().getArray(), 0.001f);
    }
}