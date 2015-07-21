package function;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {

    public static void main(String[] args) {
        Parser setParser = new Parser();
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String text = "";
        "quit".intern();
        do {
            try {
                System.out.print(">: ");
                text = br.readLine().intern();
                boolean t = setParser.eval(text);
                System.out.println(t);
            } catch (IOException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            }
        } while (text != "quit");
    }
    
}
