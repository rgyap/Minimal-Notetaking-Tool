import java.io.BufferedReader;
import java.io.FileReader;

import java.nio.file.Files;
import java.nio.file.Paths;

import java.io.IOException;

import java.util.ArrayList;
import java.util.Stack;

public class Test {


    public static void main(String[] arg) {
        BufferedReader reader;
        ArrayList<ArrayList<Character>> lines = new ArrayList<ArrayList<Character>>();

        try {
            reader  = new BufferedReader(new FileReader("testtext.txt"));
            String l;
            while ((l = reader.readLine())  != null) {
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
        
        ArrayList<String> converts = conv(lines);        

        for (String c : converts) {
            System.out.println(c);
        }

        try {
            Files.write(Paths.get("output.txt"), converts);
        } catch (IOException e) {
            System.out.println(e);
        }
        
    }

    public static ArrayList<String> conv(ArrayList<ArrayList<Character>> lines) {
        ArrayList<String> converts = new ArrayList<String>();
        Stack<Integer> st = new Stack<>();

        for (int j = 0; j < lines.size(); j++) {
            ArrayList<Character> s = new ArrayList<>(lines.get(j)); 
            
            String res = ""; 

            if (s.size()  == 0) {
                continue;    
            } 
            
            if (s.get(0) == '#') {
                int[] toh = toHeaderize(lines, j);
                if (toh[0] == 1) {
                    int hc = toh[1];
                    res = "<h" + hc + ">" + res;
                    for (int i = hc+1; i < s.size(); i++) {
                        res = res + s.get(i);
                    }
                    res = res + "</h"+hc+">";
                    converts.add(res);
                    j++;
                    continue;
                }
            }
            res = lineFormatting(s, st);

            if (((j > 0) && (lines.get(j-1).size() == 0))) {
               res = "<p>" + res;
            }  

            if (((j < lines.size() - 1) && (lines.get(j+1).size() == 0)) || (j==lines.size()-1)) {
                res = res + "</p>";
            }

            if ((j < lines.size() - 1) && (lines.get(j+1).size() > 0)) {
                res = res + "<br>";
            } 

            converts.add(res);
        }

        return converts;

    }

    public static int[] toHeaderize(ArrayList<ArrayList<Character>> lines, int index) {
        int hc = 0;
        int headerize = 0;
        ArrayList<Character> s = new ArrayList<>(lines.get(index));

        // padding to not make Java angry with out-of-bounds exceptions
        s.add((char)0);

        while (hc < 6 && s.get(hc) == '#') {
            hc++;
        }

        if (s.get(hc) != ' ') {
            return new int[]{0, 0};
        } 

        if (index == 0) {
            if (lines.get(1).size() == 0) {
                headerize = 1;
            } 
        } else if (index > 0 && index < lines.size() - 1) {
            if ((lines.get(index-1).size() == 0) && (lines.get(index+1).size() == 0)) {
                headerize = 1;
            }
        } else {
            if (lines.get(lines.size()-2).size() == 0) {
                headerize = 1; 
            }
        }
        return new int[]{headerize, hc};
    }

    public static String lineFormatting(ArrayList<Character> s1, Stack<Integer> st) {
        ArrayList<Character> s = new ArrayList<>(s1);
        String[] asts = new String[]{"<i>", "</i>", "<b>", "</b>", "<i><b>", "</b></i>", "<em>", "</em>"};
        String res = "";
        
        // padding to not make Java angry with out-of-bounds exceptions
        s.add((char)0);
        s.add((char)0);
        s.add((char)0);
        
        for (int i = 0; i < s.size()-3; i++) {
            // Delimiter
            if (s.get(i) == '\\') {
                res = res + s.get(i+1);
                i+=2;
            } 

            // Bold and Italics
            if (s.get(i) == '*') {
                int ac = 0;

                while ((ac < 3) && (s.get(i+ac) == '*')) {
                    ac++;
                }
                i += ac;
                if (st.contains(ac)) {
                    st.pop();
                    res = res + asts[2*ac-1];
                } else {
                    st.push(ac);
                    res = res + asts[2*ac-2];
                
                }
            }

            // Alternative Emphasis
            if (s.get(i) == '_') {
                i++;
                if (st.contains(4)) {
                    st.pop();
                    res = res + asts[7];
                } else {
                    st.push(4);
                    res = res + asts[6];
                }
            } 

            res = res + s.get(i);

            
            
        } 
        return res;

    }

    
}
