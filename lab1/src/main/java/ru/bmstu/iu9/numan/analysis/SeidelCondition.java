package ru.bmstu.iu9.numan.analysis;

import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import ru.bmstu.iu9.numan.commons.LinearBlockTridiagonalEquation;
import ru.bmstu.iu9.numan.commons.MatrixHelper;
import ru.bmstu.iu9.numan.commons.RandomEquations;
import ru.bmstu.iu9.numan.commons.SeidelUtils;

import java.util.Random;
import java.util.function.Function;

import static java.lang.Math.abs;

public class SeidelCondition {
    private static final double MIN_DEVIATION = -0.01;
    private static final double MAX_DEVIATION = 0.01;

    private static final double PRECISION = 0.0001;

    private static final double MIN_ELEM_VAL = -5.0;
    private static final double MAX_ELEM_VAL = 5.0;

    private static final int NO_OF_TESTS = 1000;
    private static final int MIN_EQ_COUNT = 3;
    private static final int MAX_EQ_COUNT = 10;

    private static final int MIN_BLOCK_SIZE = 3;
    private static final int MAX_BLOCK_SIZE = 10;

    public static void main(String[] args) {
        double avgRelativeDeviation = 0;
        double avgAbsoluteDeviation = 0;
        double avgMaxRelativeSolDeviation = 0;

        for (int i = 0; i < NO_OF_TESTS; i++) {
            int equationsCount = randIntBetween(MIN_EQ_COUNT, MAX_EQ_COUNT);
            int blockDim = randIntBetween(MIN_BLOCK_SIZE, MAX_BLOCK_SIZE);

            // Let's generate random equation
            LinearBlockTridiagonalEquation equation = new LinearBlockTridiagonalEquation(equationsCount);
            equation.setLhs(RandomEquations.randStrongDiagonalDominatedLhs(equationsCount, blockDim, MIN_ELEM_VAL, MAX_ELEM_VAL));
            for (int j = 0; j < equationsCount; j++) {
                equation.setRhsVector(j, RandomEquations.randVector(blockDim, MIN_ELEM_VAL, MAX_ELEM_VAL));
            }

            // Let's create random deviation system
            LinearBlockTridiagonalEquation deviationEquation = new LinearBlockTridiagonalEquation(equationsCount);
            deviationEquation.setLhs(RandomEquations.randLhs(equationsCount, blockDim, MIN_DEVIATION, MAX_DEVIATION));
            for (int k = 0; k < equationsCount; k++) {
                deviationEquation.setRhsVector(k, RandomEquations.randVector(blockDim, MIN_DEVIATION, MAX_DEVIATION));
            }

            // Generate deviated equation
            LinearBlockTridiagonalEquation deviatedEquation = equation.add(deviationEquation);

            RealVector[] startSol = MatrixHelper.createVectorsOfSameSize(deviatedEquation.getRhs(), 0.0);
            RealVector sol = SeidelUtils.solveWithSeidelMethod2(equation, startSol, PRECISION);
            RealVector deviatedSol = SeidelUtils.solveWithSeidelMethod2(deviatedEquation, startSol, PRECISION);

            RealMatrix equationLhs = equation.lhsAsRealMatrix();
            RealMatrix deviatedLhs = deviatedEquation.lhsAsRealMatrix();

            double condRhs = matrixConditionNumber(equationLhs, equationLhs, RealMatrix::getNorm);
            double condLhs = matrixConditionNumber(deviatedLhs, equationLhs, RealMatrix::getNorm);
            double deltaRhs = condRhs * deviationEquation.getRhsVec().getNorm() / equation.getRhsVec().getNorm();
            double deltaLhs = condLhs * deviationEquation.lhsAsRealMatrix().getNorm() / deviatedEquation.lhsAsRealMatrix().getNorm();
            double maxRelativeSolDeviation = deltaLhs + deltaRhs + deltaLhs * deltaRhs;
            double absSolDeviation = deviatedSol.subtract(sol).getNorm();
            double relativeSolDeviation = absSolDeviation / sol.getNorm();

            avgAbsoluteDeviation += absSolDeviation;
            avgRelativeDeviation += relativeSolDeviation;
            avgMaxRelativeSolDeviation += maxRelativeSolDeviation;

            System.out.printf("%s%n%n", new String(new char[20]).replace("\0", "-"));

            System.out.printf("абсолютное отклонение от исходного решения (L2 норма): %.8f%n", absSolDeviation);
            System.out.printf("относительное отклонение от исходного решения (L2 норма): %.8f%n", relativeSolDeviation);
            System.out.printf("число обусловленности для левой части (столбцовая норма): %.8f%n", condLhs);
            System.out.printf("число обусловленности для правой части (столбцовая норма): %.8f%n", condRhs);
            System.out.printf("теоретическая оценка максимального относительного отклонения (столбцовая норма): %.8f%n", maxRelativeSolDeviation);

            System.out.printf("%s%n%n", new String(new char[20]).replace("\0", "-"));
        }

        avgAbsoluteDeviation /= NO_OF_TESTS;
        avgRelativeDeviation /= NO_OF_TESTS;
        avgMaxRelativeSolDeviation /= NO_OF_TESTS;

        System.out.printf("%n%s%n%n", new String(new char[20]).replace("\0", "#"));
        System.out.printf("среднее абсолютное отклонение: %.8f%n", avgAbsoluteDeviation);
        System.out.printf("среднее относительное отклонение: %.8f%n", avgRelativeDeviation);
        System.out.printf("среднее максимальное относительное отклонение: %.8f%n", avgMaxRelativeSolDeviation);
    }

    public static double matrixConditionNumber(RealMatrix matrixA, RealMatrix matrixB, Function<RealMatrix, Double> matrixNorm) {
        RealMatrix inversedB = MatrixUtils.inverse(matrixB);
        return matrixNorm.apply(matrixA) * matrixNorm.apply(inversedB);
    }

    public static int randIntBetween(int a, int b) {
        return a + (abs(new Random().nextInt()) % (b - a));
    }
}
