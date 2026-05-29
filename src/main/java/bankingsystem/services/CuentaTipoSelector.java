package bankingsystem.services;

import bankingsystem.utils.FormValidation;

// esta clase es para seleccionar el tipo de cuenta a crear, ya sea corriente o de ahorros, dependiendo de la eleccion del usuario se llamara a un metodo diferente en el servicio de creacion de cuentas
public class CuentaTipoSelector {
    //
    public static String CuentaTipoSelector(){


        System.out.println("Seleccione el tipo de cuenta: ");
        System.out.println("1. Cuenta Corriente 2. Cuenta de Ahorros");
        String value = "";

        int option = FormValidation.validateInt("Seleccione una opcion: ");
        switch (option){
            case 1:
                value = "Cuenta Corriente";
                break;
            case 2:
                value = "Cuenta de Ahorros";
                break;
            default:
                System.out.println("Opción no válida");

        }

        return value;
    }

}
