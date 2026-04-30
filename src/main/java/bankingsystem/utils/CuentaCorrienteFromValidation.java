package bankingsystem.utils;

import java.util.Scanner;

public class CuentaCorrienteFromValidation {
    private static final Scanner sc = new Scanner(System.in);


    public static double validateMonto(String mensaje) {
        double monto = -1;
        while (monto <= 0) {
            System.out.print(mensaje);
            try {
                String input = sc.nextLine();
                monto = Double.parseDouble(input);

                if (monto <= 0) {
                    System.out.println("❎ Error: El monto de ahorro debe ser mayor a cero.");
                }
            } catch (NumberFormatException e) {
                System.out.println("❎ Error: Ingrese un valor numérico (ejemplo: 150000).");
                monto = -1;
            }
        }
        return monto;
    }

    public static String validateNumeroCuenta(String mensaje) {
        String numero;
        do {
            System.out.print(mensaje);
            numero = sc.nextLine().trim();
            if (numero.isEmpty()) {
                System.out.println("❎ Error: El número de cuenta de ahorros es obligatorio.");
            }
        } while (numero.isEmpty());
        return numero;
    }
}

