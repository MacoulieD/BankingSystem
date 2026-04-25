package bankingsystem.utils;

import java.util.Scanner;

public class CuentaAhorrosFormValidation {
    private static final Scanner sc = new Scanner(System.in);

    public static double validateMonto(String mensaje) {
        double monto;
        do {
            System.out.print(mensaje);
            while (!sc.hasNextDouble()) {
                System.out.println("❎ Error: Ingrese un valor numérico válido.");
                sc.next();
            }
            monto = sc.nextDouble();
            if (monto <= 0) System.out.println("❎ El monto debe ser mayor a cero.");
        } while (monto <= 0);
        return monto;
    }
}