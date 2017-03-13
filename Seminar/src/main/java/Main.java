import java.util.Scanner;

public class Main {
	
	public static boolean flug = true;

	public static void main(String[] args) {
		
		System.out.println("HELLo World!");
		
		Scanner scanner = new Scanner(System.in);	
		Interpretator interP = new Interpretator();		
		
		while(flug){
			System.out.println("\nEnter comand, (stop to exit) :\n");
			if(scanner.hasNextLine()){				
				interP.procces(scanner.nextLine());
			}			
		}
		scanner.close();
	}
}
