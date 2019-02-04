package ru.bmstu.iu9.numan;

import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class RandomEquationsTest {

    @Test
    public void testRandSelfAdjointLhs() {
        RealMatrix[][] lhs = RandomEquations.randSelfAdjointLhs(5, 5, -3, 4);

        LinearBlockTridiagonalEquation equation = new LinearBlockTridiagonalEquation(5);
        equation.setLhs(lhs);
        RealMatrix fullMatrix = equation.lhsAsRealMatrix();

        assertEquals(fullMatrix, fullMatrix.transpose());
    }

    @Test
    public void testRandDefaultLhs() {
        RealMatrix[][] lhs = RandomEquations.randDefaultLhs(5, 5, 4);
        LinearBlockTridiagonalEquation equation = new LinearBlockTridiagonalEquation(5);
        equation.setLhs(lhs);
        RealMatrix fullMatrix = equation.lhsAsRealMatrix();
        RealMatrix inversedMatrix = MatrixUtils.inverse(fullMatrix);

        // M - монотонная с диагональным доминированием
        for (int i = 0; i < inversedMatrix.getRowDimension(); i++) {
            for (int j = 0; j < inversedMatrix.getColumnDimension(); j++) {
                assertTrue(inversedMatrix.getEntry(i, j) > 0);
            }
            assertTrue(fullMatrix.getEntry(i, i) >= 0.0);
        }

        // Ak, Ck >= 0

        for (int k = 0; k < equation.getEquationsCount(); k++) {
            RealMatrix A = equation.A(k);
            RealMatrix Binv = MatrixUtils.inverse(equation.B(k));
            RealMatrix C = equation.C(k);

            for (int i = 0; i < A.getRowDimension(); i++) {
                for (int j = 0; j < A.getColumnDimension(); j++) {
                    assertTrue(A.getEntry(i, j) >= 0);
                    assertTrue(Binv.getEntry(i, j) >= 0);
                    assertTrue(C.getEntry(i, j) >= 0);
                }
            }
        }

    }
}