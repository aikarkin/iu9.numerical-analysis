package ru.bmstu.iu9.numan.commons;

import org.apache.commons.math3.linear.RealVector;
import org.testng.annotations.Test;

import static org.apache.commons.math3.linear.MatrixUtils.createRealVector;
import static org.testng.Assert.assertEquals;
import static ru.bmstu.iu9.numan.commons.MatrixHelper.vecDist;

public class MatrixHelperTest {

    @Test
    public void testVecDist() {
        RealVector[] vecs1 = new RealVector[] {
                createRealVector(new double[] {-2.5, 3.1, 4.3, 0.4}),
                createRealVector(new double[] {-5.3, 1.2, 0.05, -15.45}),
                createRealVector(new double[] {2.9, 8.123, 0.004, 1.45}),
        };

        RealVector[] vecs2 = new RealVector[] {
                createRealVector(new double[] {12.5, -6.5, 3.12, -15.8}),
                createRealVector(new double[] {-1.7, 7.23, 0.072, 1.8}),
                createRealVector(new double[] {-54.4, 2.5, 10, 4.284}),
        };

        assertEquals(57.3, vecDist(vecs1, vecs2));


        /*
        *
-2.5, 3.1, 4.3, 0.4,
-5.3, 1.2, 0.05, -15.45,
2.9, 8.123, 0.004, 1.45
        * */

        /*
12.5, -6.5, 3.12, -15.8,
-1.7, 7.23, 0.072, 1.8,
-54.4, 2.5, 10, 4.284
        * */

    }
}