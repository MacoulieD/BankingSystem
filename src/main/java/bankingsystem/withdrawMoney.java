package bankingsystem;

import java.util.Date;
import java.util.Scanner;

// CLase retirar
public class withdrawMoney {

    // Atributos de la clase retirar
    private int id;
    private String type;
    private double amount;
    private Date date;

    // Constructor de la clase retirar

    public withdrawMoney(String type, int id, int amount, Date date) {
        this.type = type;
        this.id = id;
        this.amount = amount;
        this.date = date;
    }
    //Clase sin argumento
    public withdrawMoney(){

    }

    // Getters y Setters de la clase retiro

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
    //Metodo crear retiro

    Scanner sc = new Scanner(System.in);

    public withdrawMoney CreateWihtdrawMoney(withdrawMoney user){
        System.out.println("Ingrese el valor a retirar");
        double amount = sc.nextDouble();
        return user;
    }

    //Metodo Get
    public void displayWithdrawMoney(){
        System.out.println("id " + id + "\n" +
                "tipo: " + type + "\n" +
                "Quantity: " + amount + "\n" +
                "Fecha: " + date);

    }
    //Metodo para consultar todos los retiros
    public void getAllWithdrawMoney(withdrawMoney[] withdrawMonies) {
        for (withdrawMoney withdrawMoney : withdrawMonies) {
            if (withdrawMoney != null) {
                withdrawMoney.displayWithdrawMoney();
            }
        }
    }

}
