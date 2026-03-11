package bankingsystem;
import java.util.Date;
import java.util.Scanner;

// CLase depositar
public class depositBalance {
    //Atributos Clase consignar
    private int id;
    private String type;
    private double deposit;
    private Date date;

    // Constructor depositar

    public depositBalance(int id, String type, double deposit, Date date) {
        this.id = id;
        this.type = type;
        this.deposit = deposit;
        this.date = date;
    }

    //Clase sin argumento
    public depositBalance() {

    }

    // Getters y Setters de la clase deposito
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public double getDeposit() {
        return deposit;
    }

    public void setDeposit(double deposit) {
        this.deposit = deposit;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    //Metodo crear deposito

    Scanner sc = new Scanner(System.in);

    public depositBalance createDepositBalance(depositBalance user) {
        System.out.println("Ingrese el valor a depositar");
        double deposit = sc.nextDouble();
        return user;
    }

    //Metodo Get
    public void displaydepsoitBalance() {
        System.out.println("id " + id + "\n" +
                "tipo: " + type + "\n" +
                "Quantity: " + deposit + "\n" +
                "Fecha: " + date);
    }
    //Metodo para consultar todos los depositos
    public void getAlldepositBalance(depositBalance[] depositBalances) {
        for (depositBalance depositBalance : depositBalances) {
            if (depositBalance != null) {
                depositBalance.displaydepsoitBalance();
            }
        }
    }
}
