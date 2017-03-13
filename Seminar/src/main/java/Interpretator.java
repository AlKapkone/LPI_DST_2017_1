
public class Interpretator {
	
	public void procces (String line){
		
		if(line.equals("stop")){
			Main.flug = false;
		} else if (!line.equals("")){
			
			String [] comandMas = line.split(" ");
		
			for (int i = 0; i < comandMas.length; i++){
				if (i == 0){
					System.out.print("Entered comand <" + comandMas[i] + ">");
				} else {
					if(i == 1){
						System.out.print(" and parameters");
					}
					System.out.print(" <" + comandMas[i] + ">");
				}
			}		
		}
	}
}
