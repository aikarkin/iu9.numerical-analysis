package ru.bmstu.iu9.numan.testgen;

import org.apache.commons.math3.linear.RealMatrix;
import ru.bmstu.iu9.numan.commons.LinearBlockTridiagonalEquation;
import ru.bmstu.iu9.numan.commons.RandomEquations;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashSet;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;

public class RunTestsGen {

    private static final Set<Class<?>> SUPPORTED_NUMBER_CLASSES;

    static {
        SUPPORTED_NUMBER_CLASSES = new HashSet<>();
        SUPPORTED_NUMBER_CLASSES.add(Integer.class);
        SUPPORTED_NUMBER_CLASSES.add(Double.class);
        SUPPORTED_NUMBER_CLASSES.add(Float.class);
    }

    private static int noOfTests;
    private static int startWithTest;
    private static int blockDim;
    private static int noOfEquations;
    private static double minElemVal;
    private static double maxElemVal;
    private static boolean overwriteTestFiles;
    private static MatrixType matrixType;

    public static void main(String[] args) throws IOException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {

        if (args.length == 0) {
            System.out.println("[warn] Please specify output directory for tests");
            return;
        }

        String outDir = args[0];
        File dir = new File(outDir);

        if (!dir.exists() && (!dir.isDirectory() || !dir.mkdirs())) {
            System.out.println("[error] Wrong output directory");
            return;
        }

        if (args.length > 1) {
            File propFile = new File(args[1]);
            if (propFile.exists() && propFile.isFile()) {
                readPropertiesFromFile(propFile);
            }
        }

        System.out.println("[info] Actual parameters of tests that will be generated: ");

        for(TestParameter param : TestParameter.values()) {
            System.out.printf("\t%s=%s%n", param.getKey(), param.getValue());
        }
        System.out.println();

        setTestParametersValues();

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
        startWithTest = (int) TestParameter.START_WITH.getValue();
        noOfTests = (int) TestParameter.TESTS.getValue();
        blockDim = (int) TestParameter.BLOCK_DIM.getValue();
        noOfEquations = (int) TestParameter.EQUATIONS.getValue();
        minElemVal = (double) TestParameter.MIN_VAL.getValue();
        maxElemVal = (double) TestParameter.MAX_VAL.getValue();
        overwriteTestFiles = (boolean) TestParameter.SHOULD_OVERWRITE.getValue();
        matrixType = (MatrixType) TestParameter.MATRIX_TYPE.getValue();
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

    @SuppressWarnings("unchecked")
    private static void readPropertiesFromFile(File propFile) throws IOException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Properties props = new Properties();
        InputStream inputStream = new FileInputStream(propFile);

        props.load(inputStream);

        for(TestParameter param : TestParameter.values()) {
            if(props.containsKey(param.getKey())) {
                Object paramVal = null;
                String propVal = props.getProperty(param.getKey());
                if(SUPPORTED_NUMBER_CLASSES.contains(param.getType())) {
                    Optional<? extends Number> numOpt = tryParseNumber((Class<? extends Number>) param.getType(), propVal);
                    paramVal = numOpt.isPresent() ? numOpt.get() : propVal;
                } else if(param.getType() == Boolean.class) {
                    paramVal = Boolean.parseBoolean(propVal);
                } else if(param.getType().isEnum()) {
                    Method valuesMethod = param.getType().getDeclaredMethod("values");
                    Enum[] enumValues = (Enum[]) valuesMethod.invoke(null);

                    Optional<? extends Number> intOpt = tryParseNumber(Integer.class, propVal);
                    if(intOpt.isPresent()) {
                        int idx = (Integer) intOpt.get();
                        if(idx > enumValues.length) {
                            System.out.printf("[error] Invalid ordinal '%d' for enum type: %s%n", idx, param.getType().toString());
                            continue;
                        } else {
                            paramVal = enumValues[idx];
                        }
                    } else {
                        boolean isValidEnumName = false;

                        for (Enum val : enumValues) {
                            if (val.name().equals(propVal)) {
                                paramVal = val;
                                isValidEnumName = true;
                                break;
                            }
                        }

                        if(!isValidEnumName) {
                            System.out.printf("[error] Invalid enum type name: %s%n", propVal);
                            continue;
                        }
                    }

                } else {
                    paramVal = propVal;
                }

                param.setValue(paramVal);
            }
        }

        inputStream.close();
    }

    private static Optional<? extends Number> tryParseNumber(Class<? extends Number> clazz, String val) {
        if (val != null && !val.isEmpty()) {
            try {
                if (clazz == Integer.class) {
                    return Optional.of(Integer.parseInt(val));
                } else if (clazz == Double.class) {
                    return Optional.of(Double.parseDouble(val));
                } else if (clazz == Float.class) {
                    return Optional.of(Float.parseFloat(val));
                }
            } catch (NumberFormatException ignored) {
            }
        }

        return Optional.empty();
    }

}