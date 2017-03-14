package seminar5;

import java.util.LinkedList;
import java.util.List;

public class ParsResp {

    public List<String> pars(String line) {

        String[] pars = line.split("\"");
        
        List<String> par = new LinkedList<>();
        
        for (int i = 1; i < pars.length; i = i + 2) {
                par.add(pars[i]);
        }
        
        if (par.get(0).equals("items")){
            par.remove(0);
        }
        
        return par;
    }
}
