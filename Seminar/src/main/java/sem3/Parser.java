package sem3;
public class Parser {
    
    public String[] parsForComand(String line) {
        String[] parsMas = line.split(" ", 2);

        switch (parsMas[0]) {
            case "echo":
                return line.split(" ", 2); // comand _ anyText                  
                
            case "msg":
                return line.split(" ", 3); // comand _ destination _ messegeText 
               
            default:
                return line.split(" ");
        }
    }    
}
