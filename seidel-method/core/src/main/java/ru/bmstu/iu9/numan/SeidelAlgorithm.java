package ru.bmstu.iu9.numan;

import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

import static ru.bmstu.iu9.numan.MatrixHelper.*;

public class SeidelAlgorithm {

    public static RealVector solveWithSeidelMethod(LinearBlockTridiagonalEquation eq, RealVector[] startSol, double precision) {
        int n = 0;
        int l = eq.getEquationsCount();

        // изначально предыдущее значение = начальному приближению, а текущее = вектора заполненые нулями
        RealVector[] x = startSol, xPrev = createVectorsOfSameSize(startSol, 1.0);
        RealMatrix[] G = new RealMatrix[l];
        RealMatrix[] D = new RealMatrix[l];
        G[0] = inverseOfTridiagonal(eq.B(0));
        D[0] = toTridiagonal(G[0]);

        for (int k = 1; k < l; k++) {
            G[k] = inverseOfTridiagonal(
                    eq.B(k)
                            .subtract(
                                    eq.A(k)
                                            .multiply(D[k - 1])
                                            .multiply(eq.C(k - 1))
                            )

            );
            D[k] = toTridiagonal(G[k]);
        }

        while (vecDist(x, xPrev) > precision) {
            RealVector[] z = new RealVector[l];
            xPrev = x;
            x = createVectorsOfSameSize(xPrev, 0.0);
            z[0] = G[0]
                    .operate(eq.f(0).add(eq.C(0).operate(xPrev[1])))
                    .subtract(D[0].multiply(eq.C(0)).operate(xPrev[1]));

            for (int k = 1; k < l - 1; k++) {
                z[k] = G[k]
                        .operate(
                                eq.f(k)
                                        .add(
                                                eq.A(k).operate(z[k - 1])
                                        )
                                        .add(
                                                eq.C(k).operate(xPrev[k])
                                        )
                        ).subtract(D[k].multiply(eq.C(k)).operate(xPrev[k + 1]));
            }

            x[l - 1] = z[l - 1] = G[l - 1]
                    .operate(
                            eq.f(l - 1)
                                    .add(
                                            eq.A(l - 1)
                                                    .operate(z[l - 2])
                                    )
                    );


            for (int k = l - 2; k >= 0; k--) {
                x[k] = D[k]
                        .multiply(eq.C(k))
                        .operate(x[k + 1])
                        .add(z[k]);
            }

            n++;
        }

        RealVector sol = joinVectors(x);
        System.out.printf("%n%s%n Решение СЛАУ методом Зейделя:%n", new String(new char[20]).replace("\0", "-"));
        System.out.printf("\tточность: %f%n", precision);
        printVector(joinVectors(startSol), "\tначальное приближение:");
        System.out.printf("\tчисло итераций: %d%n", n);
        printVector(sol, "\tрешение:");
        System.out.printf("%s%n", new String(new char[20]).replace("\0", "-"));

        return sol;
    }

}
