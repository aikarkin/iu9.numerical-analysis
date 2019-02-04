package ru.bmstu.iu9.numan;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.lang.Math.abs;

public class MatrixHelper {

    static double vecDist(RealVector[] x, RealVector[] xPrev) {
        double normInf = 0;

        for (int i = 0; i < x.length; i++) {
            for (int j = 0; j < x[i].getDimension(); j++) {
                if(abs(x[i].getEntry(j) - xPrev[i].getEntry(j)) > normInf)
                    normInf = abs(x[i].getEntry(j) - xPrev[i].getEntry(j));
            }
        }

        return normInf;
    }

    public static RealVector[] createVectorsOfSameSize(RealVector[] vec, double val) {
        RealVector[] vecs = new RealVector[vec.length];

        for (int i = 0; i < vec.length; i++) {
            vecs[i] = new ArrayRealVector(vec[i].getDimension());
            for (int j = 0; j < vec[i].getDimension(); j++) {
                vecs[i].setEntry(j, val);
            }
        }

        return vecs;
    }

    static RealVector joinVectors(RealVector[] vectors) {
        int dim = 0, vecsCount = vectors.length;
        for (RealVector vector : vectors) {
            dim += vector.getDimension();
        }

        RealVector vec = new ArrayRealVector(dim);

        for (int i = 0; i < vecsCount; i++) {
            dim = vectors[i].getDimension();
            for (int j = 0; j < dim; j++) {
                vec.setEntry(i * dim + j, vectors[i].getEntry(j));
            }
        }

        return vec;
    }

    static RealMatrix toTridiagonal(RealMatrix matrix) {
        RealMatrix newMatrix = MatrixUtils.createRealMatrix(matrix.getRowDimension(), matrix.getColumnDimension());
        int n = matrix.getRowDimension();

        for (int i = 0; i < n; i++) {

            for (int k = i; k < i + 3; k++) {
                if (k > 0 && k <= n) {
                    newMatrix.setEntry(i, k - 1, matrix.getEntry(i, k - 1));
                }
            }
        }

        return newMatrix;
    }

    public static void printMatrix(RealMatrix matrix, String caption, Object ...params) {
        if (caption != null) {
            System.out.println(String.format(caption, params));
        }

        for (int i = 0; i < matrix.getRowDimension(); i++) {
            List<String> valuesList = Arrays.stream(matrix.getRow(i)).mapToObj(elem -> String.format("%.3f", elem)).collect(Collectors.toList());
            System.out.println(String.join(", ", valuesList));
        }
    }

    public static void printVector(RealVector vector, String caption, Object ...params) {
        if(caption!= null)
            System.out.println(String.format(caption, params));

        for (int i = 0; i < vector.getDimension(); i++) {
            System.out.printf("%.6f ", vector.getEntry(i));
        }

        System.out.println();
    }

    static RealMatrix inverseOfTridiagonal(final RealMatrix matrix) {
        final int n = matrix.getRowDimension(), m = matrix.getColumnDimension();
        Function<Integer, Double> a = (k) -> k > 0 ? matrix.getEntry(k, k - 1) : 0.0;
        Function<Integer, Double> b = (k) -> matrix.getEntry(k, k);
        Function<Integer, Double> c = (k) -> k < n - 1 ? matrix.getEntry(k, k + 1) : 0.0;


        RealVector alphaVec = MatrixUtils.createRealVector(new double[n]);
        RealVector betaVec = MatrixUtils.createRealVector(new double[n]);
        RealMatrix retMatrix = MatrixUtils.createRealMatrix(n, m);

        alphaVec.setEntry(0, -c.apply(0) / b.apply(0));
        betaVec.setEntry(n - 1, -a.apply(n - 1) / b.apply(n - 1));

        for (int i = 1; i < n; i++) {
            alphaVec.setEntry(i, -c.apply(i) / (b.apply(i) + a.apply(i) * alphaVec.getEntry(i - 1)));
        }

        for (int i = n - 2; i >= 0; i--) {
            betaVec.setEntry(i, -a.apply(i) / (b.apply(i) + c.apply(i) * betaVec.getEntry(i + 1)));
        }

        for (int l = 0; l < m; l++) {
            double alphaPrev = l > 0 ? alphaVec.getEntry(l - 1) : 0.0;
            double betaNext = l < m - 1 ? betaVec.getEntry(l + 1) : 0.0;

            retMatrix.setEntry(
                    l,
                    l,
                    (1.0 / (b.apply(l) + alphaPrev * a.apply(l) + c.apply(l) * betaNext))
            );

            for (int k = l - 1; k >= 0; k--) {
                retMatrix.setEntry(k, l, alphaVec.getEntry(k) * retMatrix.getEntry(k + 1, l));
            }

            for (int k = l + 1; k < n; k++) {
                retMatrix.setEntry(k, l, betaVec.getEntry(k) * retMatrix.getEntry(k - 1, l));
            }
        }

        return retMatrix;
    }

}
