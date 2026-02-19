import java.io.BufferedReader;
import java.io.FileReader;

import java.nio.file.Files;
import java.nio.file.Paths;

import java.io.IOException;

import java.util.ArrayList;
import java.util.Stack;

public class Test {


    public static void main(String[] arg) {
        ArrayList<ArrayList<Character>> lines = readFileLines("testtext.txt");
        ArrayList<String> converts = conv(lines);
        writeConvertedLines(converts, "output.txt");
       
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

    public static void writeConvertedLines(ArrayList<String> convLines, String name) {
        try {
            Files.write(Paths.get(name), convLines);
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
        String[] asts = new String[]{"<i>", "</i>", "<b>", "</b>", "<i><b>", "</b></i>", "<em>", "</em>", "\\(", "\\)", "$$", "$$"};
        String res = "";
        
        // padding to not make Java angry with out-of-bounds exceptions
        s.add((char)0);
        s.add((char)0);
        s.add((char)0);
        
        for (int i = 0; i < s.size()-3; i++) {
            // Delimiter
            if (s.get(i) == '\\') {
                res = res + s.get(i+1);
                //i+=2;
                i+=1;
                continue;
            } 

            // Bold and Italics
            if (s.get(i) == '*') {
                int ac = 0;

                while ((ac < 3) && (s.get(i+ac) == '*')) {
                    ac++;
                }
                //i += ac;
                i+= (ac-1);
                if (st.contains(ac)) {
                    st.pop();
                    res = res + asts[2*ac-1];
                } else {
                    st.push(ac);
                    res = res + asts[2*ac-2];
                
                }
                continue;
            }
            
            // Alternative Emphasis
            if (s.get(i) == '_') {
                
                if (!(st.contains(5) || st.contains(6))) {
                    //i++;
                    if (st.contains(4)) {
                        st.pop();
                        res = res + asts[7];
                    } else {
                        st.push(4);
                        res = res + asts[6];
                    }
                    continue;
                }
            } 

            // MathJax preservation
            if (s.get(i) == '$') {
                int dc = 1; 
                if (s.get(i+1) == '$') {
                    dc++;
                }
                //i += dc;
                i+=(dc-1);
                int k = 4+dc;
                if (st.contains(k)) {
                    st.pop();
                    res = res + asts[2*k-1];
                } else {
                    st.push(k);
                    res = res + asts[2*k-2];
                }
                continue;
            } 

            res = res + s.get(i);
        } 
        return res;

    }

    
}
