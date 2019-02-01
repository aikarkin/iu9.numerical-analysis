package ru.bmstu.iu9.numan.commons;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MatrixParser {
    private Map<String, ElemPos> blockPosMap = new HashMap<>();
    private Map<ElemPos, Double> repeatedElementsPositions = new LinkedHashMap<>();
    private int noOfEquations = 0;


    public LinearBlockTridiagonalEquation parse(File file) throws FileNotFoundException {
        Scanner fileScanner = new Scanner(file);

        // read block blockMatrix
        readEquations(fileScanner);
        LinearBlockTridiagonalEquation equation = new LinearBlockTridiagonalEquation(noOfEquations);

        while(fileScanner.hasNextLine()) {
            boolean elemWillBeSkipped = false;
            Optional<String> markOpt = readMark(fileScanner);
            if(!markOpt.isPresent()) {
                if(!fileScanner.hasNextLine())
                    break;
                else {
                    System.out.printf("[error] unknown token - mark expected%n");
                    fileScanner.nextLine();
                    continue;
                }
            }

            String mark = markOpt.get();
            if(!blockPosMap.containsKey(mark)) {
                elemWillBeSkipped = true;
                System.out.printf("[warn] element with name '%s' never used. skipping it %n", markOpt.get());
            }

            ElemPos pos = blockPosMap.get(mark);

            if(mark.matches("[A-Z][a-z0-9_^$]*")) {
                Optional<RealMatrix> matrixOpt = readMatrix(fileScanner);
                if(!matrixOpt.isPresent()) {
                    System.out.printf("[error] failed to parse matrix '%s' - skipping it.%n", mark);
                    if(fileScanner.hasNextLine())
                        fileScanner.nextLine();
                    continue;
                }
                if(!elemWillBeSkipped) {
                    equation.setLhsMatrix(pos.row, pos.col, matrixOpt.get());
                }
            } else {
                Optional<RealVector> rhsVecOpt = readVector(fileScanner);
                if(!rhsVecOpt.isPresent()) {
                    System.out.printf("[error] failed to parse rhs vector '%s' - skipping it.%n", mark);
                    if(fileScanner.hasNextLine())
                        fileScanner.nextLine();
                    continue;
                }
                if(!elemWillBeSkipped) {
                    equation.setRhsVector(pos.row, rhsVecOpt.get());
                }
            }
        }

        fillSingleElementBlocks(equation);
        fillEmptyElements(equation);

        return equation;
    }

    private void fillSingleElementBlocks(LinearBlockTridiagonalEquation equation) {
        for(Map.Entry<ElemPos, Double> entry : repeatedElementsPositions.entrySet()) {
            ElemPos pos = entry.getKey();
            double val = entry.getValue();

            RealMatrix matrix = pos.col == 0 ? equation.getLhs()[pos.row][pos.col + 1] : equation.getLhs()[pos.row][pos.col - 1];
            if(matrix == null)
                continue;

            int rowsDim = matrix.getRowDimension();
            if(pos.col < 3) {
                int colsDim = (pos.row == 0 ? equation.getLhs()[pos.row + 1][pos.col] : equation.getLhs()[pos.row - 1][pos.col]).getColumnDimension();
                equation.setLhsMatrix(pos.row, pos.col, rowsDim, colsDim, val);
            } else {
                equation.setRhsVector(pos.row, rowsDim, val);
            }
        }
    }

    private void fillEmptyElements(LinearBlockTridiagonalEquation equation) {
        for (int i = 0; i < equation.getEquationsCount(); i++) {
            for (int j = 0; j < 3; j++) {
                if(equation.getLhs()[i][j] == null) {
                    ElemPos pos = new ElemPos(i, j);
                    int rowsDim = (pos.col == 0 ? equation.getLhs()[pos.row][pos.col + 1] : equation.getLhs()[pos.row][pos.col - 1]).getRowDimension();
                    int colsDim = (pos.row == 0 ? equation.getLhs()[pos.row + 1][pos.col] : equation.getLhs()[pos.row - 1][pos.col]).getColumnDimension();
                    equation.setLhsMatrix(pos.row, pos.col, rowsDim, colsDim, 0);
                }
            }
        }
    }

    private Optional<RealVector> readVector(Scanner fileScanner) {
        Optional<String> lineOpt = skipEmptyLines(fileScanner);
        if(!lineOpt.isPresent())
            return Optional.empty();

        String[] strElements = lineOpt.get().split("\\s+");
        RealVector vector = new ArrayRealVector(strElements.length);

        for (int i = 0; i < strElements.length; i++) {
            vector.setEntry(i, Double.valueOf(strElements[i]));
        }

        return Optional.of(vector);
    }

    private Optional<RealMatrix> readMatrix(Scanner fileScanner) {
        Optional<String> lineOpt = skipEmptyLines(fileScanner);

        if(!lineOpt.isPresent())
            return Optional.empty();

        String line = lineOpt.get();
        String[] rowElements = line.split("\\s+");
        int colDim = rowElements.length;
        int row = 0;
        RealMatrix matrix = MatrixUtils.createRealMatrix(colDim, colDim);

        while(fileScanner.hasNextLine() && !line.isEmpty()) {

            for (int col = 0; col < colDim; col++) {
                String elem = rowElements[col];
                if(!elem.matches("[-+]?[0-9]*\\.?[0-9]+")) {
                    continue;
                }

                double val = Double.valueOf(elem);
                if(val != 0.0)
                    matrix.setEntry(row, col, val);
            }

            line = fileScanner.nextLine().trim();
            rowElements = line.split("\\s+");
            row++;
        }

        return Optional.of(matrix);
    }


    private Optional<String> readMark(Scanner fileScanner) {
        Optional<String> lineOpt = skipEmptyLines(fileScanner);

        if(!lineOpt.isPresent()) {
            return Optional.empty();
        }

        Pattern labelPattern = Pattern.compile("([a-zA-Z0-9_^$]+):$");
        String line = lineOpt.get();
        Matcher matcher;

        matcher =  labelPattern.matcher(line);
        if(matcher.matches()) {
            return Optional.of(matcher.group(1));
        }

        return Optional.empty();
    }

    private void readEquations(Scanner fileScanner) {
        Optional<String> lineOpt = skipEmptyLines(fileScanner);
        if(!lineOpt.isPresent()) {
            throw new RuntimeException("Parse error - no equation provided");
        }

        String line = lineOpt.get();
        int row = 0, col;

        while(fileScanner.hasNextLine() && !line.isEmpty()) {
            String[] blockElems = line.split("\\s+");
            if(blockElems.length != 4) {
                throw new RuntimeException(String.format("Invalid equation at row#%d: '%s'", row, line));
            }

            col = 0;
            for(String blockElem : blockElems) {
                if(blockElem.matches("[-+]?[0-9]*\\.?[0-9]+")) {
                    repeatedElementsPositions.put(new ElemPos(row, col), Double.parseDouble(blockElem));
                } else if(blockElem.matches("[a-zA-Z0-9_^$]+")) {
                    blockPosMap.put(blockElem, new ElemPos(row, col));
                }
                col++;
            }
            row++;
            line = fileScanner.nextLine().trim();
        }
        this.noOfEquations = row;
    }

    private Optional<String> skipEmptyLines(Scanner fileScanner) {
        String line = "";

        while(fileScanner.hasNextLine() && line.isEmpty()) {
            line = fileScanner.nextLine().trim();
        }

        return Optional.ofNullable(line.isEmpty() ? null : line);
    }

    private static class ElemPos {
        ElemPos(int row, int col) {
            this.row = row;
            this.col = col;
        }

        int id;
        int row;
        int col;

        @Override
        public String toString() {
            return String.format("(%d, %d)", row, col);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ElemPos elemPos = (ElemPos) o;

            if (id != elemPos.id) return false;
            if (row != elemPos.row) return false;
            return col == elemPos.col;
        }

        @Override
        public int hashCode() {
            int result = id;
            result = 31 * result + row;
            result = 31 * result + col;
            return result;
        }
    }
}
