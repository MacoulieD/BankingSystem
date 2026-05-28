package bankingsystem.utils;

import java.util.Scanner;

public class FormValidation {
    static Scanner sc = new Scanner(System.in);

    //Person From validation
    public static int validateInt(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                return Integer.parseInt(sc.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("❎ Error: No puedes ingresar letras o caracteres especiales. Intenta de nuevo.");
            }
        }
    }

    public static String validateStringName(String prompt) {
        while (true) {
            String value = validateString(prompt);

            if (value.matches("^[a-zA-ZáéíóúÁÉÍÓÚñÑ ]+$")) {
                return value;
            } else {
                System.out.println("❎ Error: El nombre no puede contener números ni caracteres especiales.");
            }
        }
    }

    public static String validateintPhone(String prompt) {
        while (true) {
            System.out.print(prompt);
            String value = sc.nextLine().trim();
            if (value.matches("^[0-9]+$")) {
                // Opcional: Validar que tenga una longitud mínima (ej. 10 para celular)
                if (value.length() >= 7 && value.length() <= 15) {
                    return value;
                } else {
                    System.out.println("❎ Error: El número debe tener entre 7 y 15 dígitos.");
                }
            } else {
                System.out.println("❎ Error: El teléfono solo debe contener números (sin letras ni símbolos).");
            }
        }
    }

    public static String validateString(String prompt) {
        while (true) {
            System.out.print(prompt);
            String value = sc.nextLine().trim();
            if (!value.isEmpty()) return value;
            System.out.println("❎ El campo no puede estar vacío.");
        }
    }

    public static boolean validatePassword(String password, String ConfirmPassword) {
        if (!password.equals(ConfirmPassword)) {
            System.out.println("❌ Las contraseñas no coinciden. Reintente.");
            return false;
        }
        return true;
    }

    public static double validateDouble(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = sc.nextLine().trim();

            if (input.isEmpty()) {
                System.out.println("❎ Error: El campo no puede estar vacío. Intente de nuevo.");
                continue;
            }

            // Permitir números enteros o decimales (punto como separador)
            if (!input.matches("^\\d+(\\.\\d+)?$")) {
                System.out.println("❎ Error: El saldo inicial debe ser un número válido (sin letras ni símbolos). Intente de nuevo.");
                continue;
            }

            try {
                double value = Double.parseDouble(input);

                if (value < 0) {
                    System.out.println("❎ Error: El saldo inicial no puede ser negativo. Intente de nuevo.");
                    continue;
                }

                if (value < 10000) {
                    System.out.println("❎ Error: El saldo inicial debe ser mínimo de 10000. Intente de nuevo.");
                    continue;
                }

                return value;
            } catch (NumberFormatException e) {
                // Aunque el regex debería prevenir esto, se deja por seguridad
                System.out.println("❎ Error: Formato de número inválido. Intente de nuevo.");
            }
        }
    }

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

}