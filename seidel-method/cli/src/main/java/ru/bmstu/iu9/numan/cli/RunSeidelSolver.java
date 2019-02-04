package ru.bmstu.iu9.numan.cli;

import org.apache.commons.math3.linear.LUDecomposition;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import ru.bmstu.iu9.numan.LinearBlockTridiagonalEquation;
import ru.bmstu.iu9.numan.MatrixParser;
import ru.bmstu.iu9.numan.SeidelAlgorithm;

import java.io.File;
import java.io.IOException;

import static ru.bmstu.iu9.numan.MatrixHelper.createVectorsOfSameSize;
import static ru.bmstu.iu9.numan.MatrixHelper.printVector;

public class RunSeidelSolver {
    private static final double PRECISION = 0.01;

    public static void main(String[] args) throws IOException {
        String filename = args[0];

        File inputFile = new File(filename);
        MatrixParser parser = new MatrixParser();
        LinearBlockTridiagonalEquation equation = parser.parse(inputFile);

        RealMatrix realMatrix = equation.lhsAsRealMatrix();

        RealVector[] startSol = createVectorsOfSameSize(equation.rhs(), 0.0);
        long startTime = System.currentTimeMillis(), endTime;
        RealVector mySol = SeidelAlgorithm.solveWithSeidelMethod(
                equation,
                startSol,
                PRECISION
        );
        endTime = System.currentTimeMillis();
        System.out.printf("Время поиска решения методом Зейделя: %dms%n", endTime - startTime);
        printVector(mySol, "%nРешение, полученное с помощью метода Зейделя:");

        startTime = System.currentTimeMillis();
        RealVector libSol = new LUDecomposition(realMatrix).getSolver().solve(equation.rhsAsVector());
        endTime = System.currentTimeMillis();

        System.out.printf("Время поиска решения библиотечными средствами: %dms%n", endTime - startTime);
        printVector(libSol, "%nРешение, полученное средствами библиотеки Apache Commons Math (x = M^(-1) * f):");
    }
}