
import java.util.Scanner;

public class Main {
    public static boolean flug = true;

    public static void main(String[] args) {
        System.out.println("HELLo World!");
        Interpretator interpretator = new Interpretator();
        try (Scanner scanner = new Scanner(System.in)) {
            System.out.println("\nEnter comand, (stop to exit) :\n");
            while (flug) {
                String inLine = scanner.nextLine().trim();
                if (!inLine.equals("")) {
                    interpretator.procces(inLine);
                }
            }
        }
    }
}
