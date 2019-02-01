package ru.bmstu.iu9.numan;

import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import ru.bmstu.iu9.numan.commons.LinearBlockTridiagonalEquation;
import ru.bmstu.iu9.numan.commons.MatrixParser;
import ru.bmstu.iu9.numan.commons.SeidelUtils;

import java.io.File;
import java.io.IOException;

import static ru.bmstu.iu9.numan.commons.MatrixHelper.*;

public class RunSeidel {

    public static void main(String[] args) throws IOException {
        String filename = args[0];

        File inputFile = new File(filename);
        MatrixParser parser = new MatrixParser();
        LinearBlockTridiagonalEquation equation = parser.parse(inputFile);

        for (int i = 0; i < equation.getEquationsCount(); i++) {
            int num = i + 1;

            if(i > 0) {
                printMatrix(equation.A(i), "A_%d: ", num);
                System.out.println();
            }
            printMatrix(equation.B(i), "B_%d: ", num);

            if(i < equation.getEquationsCount() - 1) {
                System.out.println();
                printMatrix(equation.C(i), "C_%d: ", num);
            }
            System.out.println();
            printVector(equation.f(i), "f_%d: ", num);
            System.out.println("-------\n");
        }

        System.out.println();

        RealMatrix realMatrix = equation.lhsAsRealMatrix();
//        printMatrix(realMatrix, "M: ");
//        printVector(equation.getRhsVec(), "f: ");


//        RealVector libSol = new LUDecomposition(realMatrix).getSolver().solve(equation.getRhsVec());
        RealVector libSol = MatrixUtils.inverse(realMatrix).operate(equation.getRhsVec());
        RealVector[] startSol = createVectorsOfSameSize(equation.getRhs(), 0.0);
//        RealVector[] startSol = new RealVector[equation.getEquationsCount()];
//
//        for (int i = 0; i < startSol.length   ; i++) {
//            startSol[i] = MatrixUtils.createRealVector(
//                    Arrays.copyOfRange(libSol.toArray(), i * equation.getBlockDim(), (i + 1) * equation.getBlockDim())
//            );
//        }

        RealVector mySol = SeidelUtils.solveWithSeidelMethod2(
                equation,
                startSol,
                0.001
        );

        printVector(libSol, "%nРешение, полученное средствами библиотеки Apache Commons Math (x = M^(-1) * f):");
        printVector(mySol, "%nРешение, полученное с помощью метода Зейделя:");
    }
}