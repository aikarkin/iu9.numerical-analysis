package ru.bmstu.iu9.numan.analysis;

import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import ru.bmstu.iu9.numan.LinearBlockTridiagonalEquation;
import ru.bmstu.iu9.numan.MatrixHelper;
import ru.bmstu.iu9.numan.RandomEquations;
import ru.bmstu.iu9.properties.PropertiesFileParser;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Random;
import java.util.function.Function;

import static java.lang.Math.abs;
import static ru.bmstu.iu9.numan.SeidelAlgorithm.solveWithSeidelMethod;

public class RunSeidelConditionAnalysis {

    private static double minDeviation;
    private static double maxDeviation;
    private static double precision;
    private static double minElemVal;
    private static double maxElemVal;
    private static int testsCount;
    private static int minEquationsCount;
    private static int maxEquationsCount;
    private static int minBlockDim;
    private static int maxBlockDim;

    public static void main(String[] args) {
        if (args.length == 0) {
            throw new RuntimeException("[error] No property file specified in command line arguments. Program will be terminated.");
        }

        File propertyFile = new File(args[0]);

        try {
            PropertiesFileParser.parseFile(propertyFile, AnalysisPropField.values());
        } catch (IOException | NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
            System.out.println("[error] Failed to parse property file");
            return;
        }

        fillFieldsFromProperties();

        double avgRelativeDeviation = 0;
        double avgAbsoluteDeviation = 0;
        double avgMaxRelativeSolDeviation = 0;

        for (int i = 0; i < testsCount; i++) {
            int equationsCount = randIntBetween(minEquationsCount, maxEquationsCount);
            int blockDim = randIntBetween(minBlockDim, maxBlockDim);

            // Let's generate random equation
            LinearBlockTridiagonalEquation equation = new LinearBlockTridiagonalEquation(equationsCount);
            equation.setLhs(RandomEquations.randStrongDiagonalDominatedLhs(equationsCount, blockDim, minElemVal, maxElemVal));
            for (int j = 0; j < equationsCount; j++) {
                equation.setRhsVector(j, RandomEquations.randVector(blockDim, minElemVal, maxElemVal));
            }

            // Let's create random deviation system
            LinearBlockTridiagonalEquation deviationEquation = new LinearBlockTridiagonalEquation(equationsCount);
            deviationEquation.setLhs(RandomEquations.randLhs(equationsCount, blockDim, minDeviation, maxDeviation));
            for (int k = 0; k < equationsCount; k++) {
                deviationEquation.setRhsVector(k, RandomEquations.randVector(blockDim, minDeviation, maxDeviation));
            }

            // Generate deviated equation
            LinearBlockTridiagonalEquation deviatedEquation = equation.add(deviationEquation);

            RealVector[] startSol = MatrixHelper.createVectorsOfSameSize(deviatedEquation.rhs(), 0.0);
            RealVector sol = solveWithSeidelMethod(equation, startSol, precision);
            RealVector deviatedSol = solveWithSeidelMethod(deviatedEquation, startSol, precision);

            RealMatrix equationLhs = equation.lhsAsRealMatrix();
            RealMatrix deviatedLhs = deviatedEquation.lhsAsRealMatrix();

            double condRhs = matrixConditionNumber(equationLhs, equationLhs, RealMatrix::getNorm);
            double condLhs = matrixConditionNumber(deviatedLhs, equationLhs, RealMatrix::getNorm);
            double deltaRhs = condRhs * deviationEquation.rhsAsVector().getNorm() / equation.rhsAsVector().getNorm();
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
            System.out.printf("теоретическая оценка максимального относительного отклонения (столбцовая норма): %.8f%n", maxRelativeSolDeviation);

            System.out.printf("%s%n%n", new String(new char[20]).replace("\0", "-"));
        }

        avgAbsoluteDeviation /= testsCount;
        avgRelativeDeviation /= testsCount;
        avgMaxRelativeSolDeviation /= testsCount;

        System.out.printf("%n%s%n%n", new String(new char[20]).replace("\0", "#"));
        System.out.printf("среднее абсолютное отклонение: %.8f%n", avgAbsoluteDeviation);
        System.out.printf("среднее относительное отклонение: %.8f%n", avgRelativeDeviation);
        System.out.printf("среднее максимальное относительное отклонение: %.8f%n", avgMaxRelativeSolDeviation);
    }

    private static void fillFieldsFromProperties() {
        minDeviation = (double) AnalysisPropField.MIN_DEVIATION.getValue();
        maxDeviation = (double) AnalysisPropField.MAX_DEVIATION.getValue();
        precision = (double) AnalysisPropField.PRECISION.getValue();
        minElemVal = (double) AnalysisPropField.MIN_ELEM_VAL.getValue();
        maxElemVal = (double) AnalysisPropField.MAX_ELEM_VAL.getValue();
        testsCount = (int) AnalysisPropField.TESTS_COUNT.getValue();
        minEquationsCount = (int) AnalysisPropField.MIN_EQUATIONS_COUNT.getValue();
        maxEquationsCount = (int) AnalysisPropField.MAX_EQUATIONS_COUNT.getValue();
        minBlockDim = (int) AnalysisPropField.MIN_BLOCK_DIM.getValue();
        maxBlockDim = (int) AnalysisPropField.MAX_BLOCK_DIM.getValue();
    }

    private static double matrixConditionNumber(RealMatrix matrixA, RealMatrix matrixB, Function<RealMatrix, Double> matrixNorm) {
        RealMatrix inversedB = MatrixUtils.inverse(matrixB);
        return matrixNorm.apply(matrixA) * matrixNorm.apply(inversedB);
    }

    private static int randIntBetween(int a, int b) {
        return a + (abs(new Random().nextInt()) % (b - a));
    }

}
