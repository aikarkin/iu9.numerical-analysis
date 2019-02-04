package ru.bmstu.iu9.numan;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

import java.util.Random;

import static java.lang.Math.abs;
import static org.apache.commons.math3.linear.MatrixUtils.createRealMatrix;

public class RandomEquations {

    private static final int LINE_BLOCKS_COUNT = 3;
    private static final int BAND_WIDTH = 2;

    public static RealVector randVector(int dim, double minElemVal, double maxElemVal) {
        RealVector vector = new ArrayRealVector(dim);

        for (int i = 0; i < dim; i++) {
            vector.setEntry(i, randDoubleBetween(minElemVal, maxElemVal));
        }

        return vector;
    }

    public static RealMatrix[][] randStrongDiagonalDominatedLhs(int noOfEquations, int blockDim, double minElemVal, double maxElemVal) {
        RealMatrix[][] lhs = new RealMatrix[noOfEquations][LINE_BLOCKS_COUNT];
        double rowSum;

        for (int i = 0; i < noOfEquations; i++) {
            setDefaultRandLhsVec(noOfEquations, blockDim, minElemVal, maxElemVal, lhs, i);

            for (int l = 0; l < blockDim; l++) {
                rowSum = 0;
                for (int p = 0; p < blockDim; p++) {
                    for (int j = 0; j < LINE_BLOCKS_COUNT; j++) {
                        rowSum += abs(lhs[i][j].getEntry(l, p));
                    }
                }
                lhs[i][1].setEntry(l, l, rowSum);
            }
        }

        return lhs;
    }

    private static void setDefaultRandLhsVec(int noOfEquations, int blockDim, double minElemVal, double maxElemVal, RealMatrix[][] lhs, int i) {
        lhs[i][0] = (i == 0) ? createRealMatrix(blockDim, blockDim) : randAnyMatrix(blockDim, blockDim, minElemVal, maxElemVal);
        lhs[i][1] = randBandedMatrix(blockDim, minElemVal, maxElemVal);
        lhs[i][2] = (i == noOfEquations - 1) ? createRealMatrix(blockDim, blockDim) : randAnyMatrix(blockDim, blockDim, minElemVal, maxElemVal);
    }

    public static RealMatrix[][] randDefaultLhs(int noOfEquations, int blockDim, double maxElemDeviation) {
        RealMatrix[][] lhs = new RealMatrix[noOfEquations][LINE_BLOCKS_COUNT];

        for (int i = 0; i < noOfEquations; i++) {
            lhs[i][0] = (i == 0) ? createRealMatrix(blockDim, blockDim) : randAnyMatrix(blockDim, blockDim, 0.0, abs(maxElemDeviation));
            lhs[i][1] = randBandedMatrix(blockDim, -abs(maxElemDeviation), 0.0);
            lhs[i][2] = (i == noOfEquations - 1) ? createRealMatrix(blockDim, blockDim) : randAnyMatrix(blockDim, blockDim, 0.0, abs(maxElemDeviation));
        }

        return toDiagonalDominant(lhs);
    }

    public static RealMatrix[][] randLhsWithTridiagonalDiagonalBlocks(int noOfEquations, int blockDim, double minElemVal, double maxElemVal) {
        RealMatrix[][] lhs = new RealMatrix[noOfEquations][LINE_BLOCKS_COUNT];

        for (int i = 0; i < noOfEquations; i++) {
            setDefaultRandLhsVec(noOfEquations, blockDim, minElemVal, maxElemVal, lhs, i);
        }

        return toDiagonalDominant(lhs);
    }

    public static RealMatrix[][] randSelfAdjointLhs(int noOfEquations, int blockDim, double minElemVal, double maxElemVal) {
        RealMatrix[][] lhs = new RealMatrix[noOfEquations][LINE_BLOCKS_COUNT];
        lhs[0][0] = createRealMatrix(blockDim, blockDim);
        lhs[0][1] = randSelfAdjointTridiaganalMatrix(blockDim, minElemVal, maxElemVal);
        lhs[0][2] = randAnyMatrix(blockDim, blockDim, minElemVal, maxElemVal);

        for (int i = 1; i < noOfEquations; i++) {
            lhs[i][0] = lhs[i - 1][2].transpose();
            lhs[i][1] = randSelfAdjointTridiaganalMatrix(blockDim, minElemVal, maxElemVal);
            lhs[i][2] = randAnyMatrix(blockDim, blockDim, minElemVal, maxElemVal);
        }

        lhs[noOfEquations - 1][2] = createRealMatrix(blockDim, blockDim);

        return toDiagonalDominant(lhs);
    }

    public static RealMatrix[][] randLhs(int noOfEquations, int blockDim, double minElemVal, double maxElemVal) {
        RealMatrix[][] lhs = new RealMatrix[noOfEquations][LINE_BLOCKS_COUNT];
        lhs[0][0] = createRealMatrix(blockDim, blockDim);
        lhs[0][1] = randAnyMatrix(blockDim, blockDim, minElemVal, maxElemVal);
        lhs[0][2] = randAnyMatrix(blockDim, blockDim, minElemVal, maxElemVal);

        for (int i = 1; i < noOfEquations; i++) {
            lhs[i][0] = lhs[i - 1][2].transpose();
            lhs[i][1] = randAnyMatrix(blockDim, blockDim, minElemVal, maxElemVal);
            lhs[i][2] = randAnyMatrix(blockDim, blockDim, minElemVal, maxElemVal);
        }

        lhs[noOfEquations - 1][2] = createRealMatrix(blockDim, blockDim);

        return lhs;
    }


    private static RealMatrix randAnyMatrix(int rows, int cols, double minElemVal, double maxElemVal) {
        RealMatrix matrix = createRealMatrix(rows, cols);

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                matrix.setEntry(i, j, randDoubleBetween(minElemVal, maxElemVal));
            }
        }

        return matrix;
    }

    private static RealMatrix[][] toDiagonalDominant(RealMatrix[][] lhs) {
        for (RealMatrix[] lh : lhs) {
            for (int i = 0; i < lh[0].getRowDimension(); i++) {
                double rowSum = 0;
                for (int j = 0; j < lh[0].getColumnDimension(); j++) {
                    for (int l = 0; l < LINE_BLOCKS_COUNT; l++) {
                        rowSum += abs(lh[l].getEntry(i, j));
                    }
                }
                lh[1].setEntry(i, i, rowSum);
            }
        }

        return lhs;
    }

    private static double randDoubleBetween(double a, double b) {
        return a + (b - a) * (new Random().nextDouble());
    }

    private static RealMatrix randBandedMatrix(int dim, double minElemVal, double maxElemVal) {
        RealMatrix matrix = createRealMatrix(dim, dim);

        for (int i = 0; i < dim; i++) {
            for (int j = i - BAND_WIDTH + 1; j <= i + BAND_WIDTH - 1; j++) {
                if (j >= 0 && j < dim) {
                    matrix.setEntry(i, j, randDoubleBetween(minElemVal, maxElemVal));
                }
            }
        }

        return matrix;
    }

    private static RealMatrix randSelfAdjointTridiaganalMatrix(int dim, double minElemVal, double maxElemVal) {
        RealMatrix matrix = createRealMatrix(dim, dim);
        double randVal;

        for (int i = 0; i < dim; i++) {
            for (int j = i + 1; j < i + 2; j++) {
                if (j < dim) {
                    randVal = randDoubleBetween(minElemVal, maxElemVal);
                    matrix.setEntry(i, j, randVal);
                    matrix.setEntry(j, i, randVal);
                }
            }

            randVal = randDoubleBetween(minElemVal, maxElemVal);
            matrix.setEntry(i, i, randVal);
        }

        return matrix;
    }

}