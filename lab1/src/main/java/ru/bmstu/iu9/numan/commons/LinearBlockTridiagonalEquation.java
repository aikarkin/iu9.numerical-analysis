package ru.bmstu.iu9.numan.commons;

import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.OpenMapRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;

public class LinearBlockTridiagonalEquation {
    private RealMatrix[][] lhs;
    private RealVector[] rhs;

    public LinearBlockTridiagonalEquation(int noOfEquations) {
        this.lhs = new RealMatrix[noOfEquations][3];
        this.rhs = new RealVector[noOfEquations];
    }

    public void save(File file) throws IOException {
        FileOutputStream fos = new FileOutputStream(file);
        PrintWriter writer = new PrintWriter(fos);

        String[] symbols = new String[] {"A", "B", "C"};

        for (int i = 0; i < getEquationsCount(); i++) {
            if(i == 0) {
                writer.printf("0 B%d C%d f%d", i + 1, i + 1, i + 1);
            } else if(i == getEquationsCount() - 1) {
                writer.printf("A%d B%d 0 f%d", i + 1, i + 1, i + 1);
            } else {
                writer.printf("A%d B%d C%d f%d", i + 1, i + 1, i + 1, i + 1);
            }
            writer.println();
        }

        writer.println();

        for (int i = 0; i < getEquationsCount(); i++) {
            int k = 0;

            if(i == 0) {
                for (String symbol : new String[]{"B", "C"}) {
                    writer.printf("%s%d:%n", symbol, i + 1);
                    for (int j = 0; j < getLhs()[i][k].getColumnDimension(); j++) {
                        for (int l = 0; l < getLhs()[i][k].getRowDimension(); l++) {
                            writer.printf("%.3f ", getLhs()[i][k + 1].getEntry(j, l));
                        }
                        writer.println();
                    }
                    writer.println("\n");
                    k++;
                }
            } else if(i == getEquationsCount() - 1) {
                for (String symbol : new String[]{"A", "B"}) {
                    writer.printf("%s%d:%n", symbol, i + 1);
                    for (int j = 0; j < getLhs()[i][k].getColumnDimension(); j++) {
                        for (int l = 0; l < getLhs()[i][k].getRowDimension(); l++) {
                            writer.printf("%.3f ", getLhs()[i][k].getEntry(j, l));
                        }
                        writer.println();
                    }
                    writer.println("\n");
                    k++;
                }
            } else {

                for (String symbol : symbols) {
                    writer.printf("%s%d:%n", symbol, i + 1);
                    for (int j = 0; j < getLhs()[i][k].getColumnDimension(); j++) {
                        for (int l = 0; l < getLhs()[i][k].getRowDimension(); l++) {
                            writer.printf("%.3f ", getLhs()[i][k].getEntry(j, l));
                        }
                        writer.println();
                    }
                    writer.println("\n");
                    k++;
                }

            }

            writer.printf("f%d:%n", i + 1);
            for (int j = 0; j < getRhs()[i].getDimension(); j++) {
                writer.printf("%.3f ", getRhs()[i].getEntry(j));
            }
            writer.println("\n");
        }



        writer.close();
        fos.close();
    }

    public RealMatrix[][] getLhs() {
        return lhs;
    }

    public int getBlockDim() {
        return rhs[0].getDimension();
    }

    public RealVector[] getRhs() {
        return rhs;
    }

    public RealVector getRhsVec() {
        int dim = 0, vecsCount = getEquationsCount();
        for (int i = 0; i < vecsCount; i++) {
            dim += getRhs()[i].getDimension();
        }

        double[] vecData = new double[dim];

        for (int i = 0; i < vecsCount; i++) {
            dim = getRhs()[i].getDimension();
            for (int j = 0; j < dim; j++) {
                vecData[i * dim + j] = getRhs()[i].getEntry(j);
            }
        }

        return MatrixUtils.createRealVector(vecData);
    }

    public void setLhs(RealMatrix[][] lhs) {
        this.lhs = lhs;
    }

    public void setLhsMatrix(int k, int l, RealMatrix matrix) {
        lhs[k][l] = matrix;
    }

    public void setLhsMatrix(int k, int l, int noOfRows, int noOfCols, double repeatedElement) {
        double[][] matrixData = new double[noOfRows][noOfCols];
        for (int i = 0; i < noOfRows; i++) {
            for (int j = 0; j < noOfCols; j++) {
                matrixData[i][j] = repeatedElement;
            }
        }
        lhs[k][l] = MatrixUtils.createRealMatrix(matrixData);
    }

    public void setRhsVector(int k, RealVector vector) {
        rhs[k] = vector;
    }

    public void setRhsVector(int k, int size, double repeatedElement) {
        double[] vecData = new double[size];
        Arrays.fill(vecData, repeatedElement);
        rhs[k] = MatrixUtils.createRealVector(vecData);
    }

    public RealMatrix A(int k) {
        return lhs[k][0];
    }


    public RealMatrix B(int k) {
        return lhs[k][1];
    }

    public RealMatrix C(int k) {
        return lhs[k][2];
    }

    public RealVector f(int k) {
        return rhs[k];
    }

    public int getEquationsCount() {
        return lhs.length;
    }

    public LinearBlockTridiagonalEquation add(LinearBlockTridiagonalEquation eq) {
        LinearBlockTridiagonalEquation res = new LinearBlockTridiagonalEquation(getEquationsCount());

        for (int k = 0; k < getEquationsCount(); k++) {
            for (int i = 0; i < 3; i++) {
                res.setLhsMatrix(k, i, this.getLhs()[k][i].add(eq.getLhs()[k][i]));
            }
            res.setRhsVector(k, this.getRhs()[k].add(eq.getRhs()[k]));
        }

        return res;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LinearBlockTridiagonalEquation that = (LinearBlockTridiagonalEquation) o;

        if (!Arrays.deepEquals(lhs, that.lhs)) return false;
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        return Arrays.equals(rhs, that.rhs);
    }

    @Override
    public int hashCode() {
        int result = Arrays.deepHashCode(lhs);
        result = 31 * result + Arrays.hashCode(rhs);
        return result;
    }

    public RealMatrix lhsAsRealMatrix() {
        RealMatrix[][] lhs = getLhs();
        int rowsCount = getEquationsCount();
        int blockMatrixColDim = 0, blockMatrixRowDim = 0;
        int[] maxRowRowDims = new int[rowsCount], maxRowColDims = new int[rowsCount];

        for (int l = 0; l < rowsCount; l++) {
            maxRowRowDims[l] = 0;
            maxRowColDims[l] = 0;

            for (int k = 0; k < 3; k++) {
                if (lhs[l][k].getRowDimension() > maxRowRowDims[l])
                    maxRowRowDims[l] = lhs[l][k].getRowDimension();
                if (lhs[l][k].getColumnDimension() > maxRowColDims[l])
                    maxRowColDims[l] = lhs[l][k].getRowDimension();
            }

            blockMatrixRowDim += maxRowRowDims[l];
            blockMatrixColDim += maxRowColDims[l];
        }

        int i = 0;
        RealMatrix realMatrix = new OpenMapRealMatrix(blockMatrixRowDim, blockMatrixColDim);
        for (int l = 0; l < rowsCount; l++) {
            if(l > 0)
                realMatrix.setSubMatrix(lhs[l][0].scalarMultiply(-1).getData(), i, i - maxRowColDims[0]);

            realMatrix.setSubMatrix(lhs[l][1].getData(), i, i + maxRowColDims[0] - maxRowColDims[1]);

            if(l < rowsCount - 1)
                realMatrix.setSubMatrix(lhs[l][2].scalarMultiply(-1).getData(), i, i + maxRowColDims[0] + maxRowColDims[1] - maxRowColDims[1]);

            i += maxRowRowDims[l];
        }

        return realMatrix;
    }
}
