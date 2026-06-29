package com.example.autotarget;

import android.util.Log;

import org.apache.commons.math3.linear.*;

public class DataReconciliation {

    public static double[] reconcile(double[] y, double[][] V, double[][] A) {

        try {

            RealVector yVector = MatrixUtils.createRealVector(y);

            RealMatrix VMatrix = MatrixUtils.createRealMatrix(V);

            RealMatrix AMatrix = MatrixUtils.createRealMatrix(A);


            RealMatrix AT = AMatrix.transpose();

            RealMatrix AVAT = AMatrix
                    .multiply(VMatrix)
                    .multiply(AT);


            // Regularização para evitar matriz singular
            double epsilon = 1e-6;

            for (int i = 0; i < AVAT.getRowDimension(); i++) {
                AVAT.addToEntry(i, i, epsilon);
            }


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


            Log.e("RECON",
                    "Matriz singular detectada. Retornando dados originais",
                    e);


            // fallback seguro
            return y;


        } catch (Exception e) {


            Log.e("RECON",
                    "Erro na reconciliação",
                    e);


            return y;
        }
    }
}