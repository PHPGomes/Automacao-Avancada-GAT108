package com.example.autotarget;
import org.apache.commons.math3.linear.*;

public class DataReconciliation {
    public static double[] reconcile(double[] y,double[][] V,double[][] A) {
        RealVector yVector = MatrixUtils.createRealVector(y);
        RealMatrix VMatrix = MatrixUtils.createRealMatrix(V);
        RealMatrix AMatrix = MatrixUtils.createRealMatrix(A);
        RealMatrix AT = AMatrix.transpose();
        RealMatrix AVAT = AMatrix.multiply(VMatrix).multiply(AT);
        RealMatrix inversa = new LUDecomposition(AVAT).getSolver().getInverse();
        RealVector correcao = VMatrix.multiply(AT).multiply(inversa).operate(AMatrix.operate(yVector));
        RealVector yHat = yVector.subtract(correcao);
        return yHat.toArray();
    }
}