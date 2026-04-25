package bankingsystem.utils;

import java.util.Scanner;

public class PersonFormValidation {
    static Scanner sc = new Scanner(System.in);

    public static int validateInt(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                return Integer.parseInt(sc.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("❎ Error: Debe ingresar un número entero.");
            }
        }
    }

    public static double validateDouble(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                double value = Double.parseDouble(sc.nextLine());
                if (value < 0) throw new Exception();
                return value;
            } catch (Exception e) {
                System.out.println("❎ Error: Ingrese un valor decimal positivo.");
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

    public static boolean validatePassword(String p1, String p2) {
        if (!p1.equals(p2)) {
            System.out.println("❌ Las contraseñas no coinciden. Reintente.");
            return false;
        }
        return true;
    }
    public static String validateStringName(String prompt) {
        while (true) {
            // Primero nos aseguramos de que no sea un string vacío usando el método base
            String value = validateString(prompt);

            if (value.matches("^[a-zA-ZáéíóúÁÉÍÓÚñÑ ]+$")) {
                return value;
            } else {
                System.out.println("❎ Error: El nombre no puede contener números ni caracteres especiales.");
            }
        }
    }

    public static String validateStringPhone(String prompt) {
        while (true) {
            String value = validateString(prompt);
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
}