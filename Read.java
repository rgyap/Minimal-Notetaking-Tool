import java.io.BufferedReader;
import java.io.FileReader;

import java.io.IOException;

import java.util.ArrayList;

public final class Read {

    private Read() {
    }
	
    public static ArrayList<ArrayList<Character>> readFileLines(String filename) {
        ArrayList<ArrayList<Character>> lines = new ArrayList<ArrayList<Character>>();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(filename));
            String l;
            while ((l = reader.readLine()) != null) {
                ArrayList<Character> ch = new ArrayList<Character>();
                char[] arr = l.toCharArray();
                for (char a : arr) {
                    ch.add(a);
                }
                lines.add(ch);
            }
            reader.close();
        } catch (IOException e) {
            System.out.println(e);
        }

        return lines;
    }
}
