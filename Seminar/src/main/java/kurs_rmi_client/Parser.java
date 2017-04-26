package kurs_rmi_client;

public class Parser {

    public String[] parsForComand(String line) {
        String[] parsMas = line.split(" ", 2);

        switch (parsMas[0]) {
            case "echo":
                return parsMas; // comand _ anyText 
                
            default:
                return line.split(" ");                
        }
    }
}
