package ru.bmstu.iu9.numan.autogen;

import org.apache.commons.math3.linear.RealMatrix;
import ru.bmstu.iu9.numan.LinearBlockTridiagonalEquation;
import ru.bmstu.iu9.numan.RandomEquations;
import ru.bmstu.iu9.properties.PropertiesFileParser;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class RunAutoTestGeneration {

    private static int noOfTests;
    private static int startWithTest;
    private static int blockDim;
    private static int noOfEquations;
    private static double minElemVal;
    private static double maxElemVal;
    private static boolean overwriteTestFiles;
    private static String outDir;
    private static MatrixType matrixType;

    public static void main(String[] args) throws IOException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        if (args.length == 0) {
            System.out.println("[warn] No .properties file provided. Using default configuration");
        } else {
            File propFile = new File(args[0]);
            if (propFile.exists() && propFile.isFile()) {
                PropertiesFileParser.parseFile(propFile, TestParamField.values());
            }
        }

        System.out.println("[info] Actual parameters of tests that will be generated: ");

        for(TestParamField param : TestParamField.values()) {
            System.out.printf("\t%s=%s%n", param.getKey(), param.getValue());
        }
        System.out.println();

        setTestParametersValues();

        File dir = new File(outDir);

        if (!dir.exists() && (!dir.isDirectory() || !dir.mkdirs())) {
            System.out.println("[error] Wrong output directory");
            return;
        }

        String fileName;
        for (int i = 0; i < noOfTests; i++) {
            fileName = String.format("%s/test-%d.txt", outDir, startWithTest + i);
            File outFile = new File(fileName);
            if (outFile.exists()) {
                if(!overwriteTestFiles) {
                    Files.copy(outFile.toPath(), new File(fileName + ".bac").toPath(), StandardCopyOption.REPLACE_EXISTING);
                }
            } else {
                if (!outFile.createNewFile()) {
                    System.out.printf("[error] Failed to create new file: %s%n", outFile);
                }
            }

            if (outFile.exists()) {
                writeRandomEquationToFile(outFile);
                System.out.printf("[info] Test #%d saved to file: %s%n", startWithTest + i, fileName);
            } else {
                System.out.printf("[warn] File not found: %s. Skipping it.%n", fileName);
            }
        }
    }

    private static void setTestParametersValues() {
        startWithTest = (int) TestParamField.START_WITH.getValue();
        noOfTests = (int) TestParamField.TESTS.getValue();
        blockDim = (int) TestParamField.BLOCK_DIM.getValue();
        noOfEquations = (int) TestParamField.EQUATIONS.getValue();
        minElemVal = (double) TestParamField.MIN_VAL.getValue();
        maxElemVal = (double) TestParamField.MAX_VAL.getValue();
        overwriteTestFiles = (boolean) TestParamField.SHOULD_OVERWRITE.getValue();
        matrixType = (MatrixType) TestParamField.MATRIX_TYPE.getValue();
        outDir = (String) TestParamField.OUT_DIR.getValue();
    }

    private static void writeRandomEquationToFile(File outFile) throws IOException {
        RealMatrix[][] lhs;

        if(matrixType == MatrixType.DIAGONAL_DOMINANT) {
            lhs = RandomEquations.randStrongDiagonalDominatedLhs(noOfEquations, blockDim, minElemVal, maxElemVal);
        } else if(matrixType == MatrixType.TRIDAGONAL_DIAGONAL_BLOCKS) {
            lhs = RandomEquations.randLhsWithTridiagonalDiagonalBlocks(noOfEquations, blockDim, minElemVal, maxElemVal);
        } else if(matrixType == MatrixType.SELF_ADJOINT_MATRIX) {
            lhs = RandomEquations.randSelfAdjointLhs(noOfEquations, blockDim, minElemVal, maxElemVal);
        } else if(matrixType == MatrixType.DEFAULT) {
            lhs = RandomEquations.randDefaultLhs(noOfEquations, blockDim, maxElemVal);
        } else {
            throw new RuntimeException("Unsupported matrix type: " + matrixType.name());
        }

        LinearBlockTridiagonalEquation equation = new LinearBlockTridiagonalEquation(lhs.length);
        equation.setLhs(lhs);

        for (int i = 0; i < lhs.length; i++) {
            equation.setRhsVector(i, RandomEquations.randVector(blockDim, minElemVal, maxElemVal));
        }

        equation.save(outFile);
    }

}