public class Interpretator {

    public void procces(String line) {
        if (line.equals("stop")) {
            Main.flug = false;
            return;
        }
        String[] comandMas = line.split(" ", 2);
        System.out.print("Entered comand <" + comandMas[0] + "> ");
        if (comandMas.length == 2) {
            System.out.print("with parameters <" + comandMas[1] + ">\n");
        }
    }
}
