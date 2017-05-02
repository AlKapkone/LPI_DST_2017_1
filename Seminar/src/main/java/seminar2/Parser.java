package seminar2;

public class Parser {
    
    public String[] parsForComand(String line) {
        String[] parsMas = line.split(" ", 2);

        switch (parsMas[0]) {
            // comand _ anyText 
            case "eho":
                return parsMas;

            // comand _ destination _ messegeText 
            case "msg":
                return line.split(" ", 3);

            default:
                return line.split(" ");
        }
    }
}
