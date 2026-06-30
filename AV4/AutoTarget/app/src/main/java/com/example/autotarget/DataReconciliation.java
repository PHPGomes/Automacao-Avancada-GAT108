package com.example.autotarget;

import android.util.Log;

import org.apache.commons.math3.linear.LUDecomposition;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.linear.SingularMatrixException;

public class DataReconciliation {

    private static final String TAG = "RECON";

    private static final double EPSILON = 1e-6;

    // Limite de segurança para evitar travamento por inversão de matriz grande.
    private static final int MAX_DIMENSAO_RECONCILIACAO = 80;

    public static double[] reconcile(double[] y, double[][] V, double[][] A) {

        if (!entradaValida(y, V, A)) {
            return y;
        }

        if (y.length > MAX_DIMENSAO_RECONCILIACAO) {
            Log.w(TAG,
                    "Reconciliação ignorada: dimensão alta (" + y.length + ")");
            return y;
        }

        try {

            RealVector yVector = MatrixUtils.createRealVector(y);

            RealMatrix VMatrix = MatrixUtils.createRealMatrix(V);
            RealMatrix AMatrix = MatrixUtils.createRealMatrix(A);

            RealMatrix AT = AMatrix.transpose();

            RealMatrix AVAT = AMatrix
                    .multiply(VMatrix)
                    .multiply(AT);

            regularizarDiagonal(AVAT);

            RealMatrix inversa =
                    new LUDecomposition(AVAT)
                            .getSolver()
                            .getInverse();

            RealVector correcao =
                    VMatrix
                            .multiply(AT)
                            .multiply(inversa)
                            .operate(AMatrix.operate(yVector));

            RealVector yHat =
                    yVector.subtract(correcao);

            return yHat.toArray();

        } catch (SingularMatrixException e) {

            Log.e(TAG,
                    "Matriz singular. Usando dados originais.",
                    e);

            return y;

        } catch (Exception e) {

            Log.e(TAG,
                    "Erro na reconciliação. Usando dados originais.",
                    e);

            return y;
        }
    }

    private static boolean entradaValida(
            double[] y,
            double[][] V,
            double[][] A
    ) {

        if (y == null || V == null || A == null) {
            return false;
        }

        if (y.length == 0) {
            return false;
        }

        if (V.length != y.length || A.length != y.length) {
            return false;
        }

        for (int i = 0; i < y.length; i++) {

            if (V[i] == null || V[i].length != y.length) {
                return false;
            }

            if (A[i] == null || A[i].length != y.length) {
                return false;
            }
        }

        return true;
    }

    private static void regularizarDiagonal(RealMatrix matriz) {

        int limite = Math.min(
                matriz.getRowDimension(),
                matriz.getColumnDimension()
        );

        for (int i = 0; i < limite; i++) {
            matriz.addToEntry(i, i, EPSILON);
        }
    }
}