import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Stack;
import java.util.HashMap;


public class Test {

    //public static HashMap<String, Integer> OPS = new HashMap<>();
   // OPS.put("*", 1);
   // OPS.put("**", 2); 

    public static void main(String[] arg) {
        BufferedReader reader;
        ArrayList<char[]> lines = new ArrayList<char[]>();

        try {
            reader  = new BufferedReader(new FileReader("testtext.txt"));
            String l;
            while ((l = reader.readLine())  != null) {
                 lines.add(l.toCharArray());
            }
            reader.close();


        } catch (IOException e) {
            System.out.println("ERROR!");
        }
        
        Stack<Integer> st = new Stack<>();

        for (int j = 0; j < lines.size(); j++) {
            char[] s = lines.get(j); 
            String res = ""; 

            if (s.length == 0) {
                //res = res + "<br>";
                //System.out.println(res);
                continue;
            } 

            int hc = 0;

            if (s[0] == '#') {
                hc++;
                if (s[1] == '#') {
                    hc++;
                    if (s[2] == '#') {
                        hc++;
                    }
                }
            }

            if (s[hc] == ' ') {
                 boolean headerize = false;
                if (j == 0) {
                    if (lines.get(1).length == 0) {
                        headerize = true;
                    } 
                } else if (j > 0 && j < lines.size() - 1) {
                   if ((lines.get(j-1).length == 0) && (lines.get(j+1).length == 0)) {
                       headerize = true;
                   }
                } else {
                    if (lines.get(lines.size()-2).length == 0) {
                        headerize = true; 
                    }
                } 
                
                if (headerize) {
                    res = "<h" + hc + ">" + res;
                    for (int i = hc+1; i < s.length; i++) {
                        res = res + s[i];
                    }
                    res = res + "</h"+hc+">";
                    System.out.println(res);
                    j++;
                    continue;
                }

            }


            for (int i = 0; i < s.length; i++) {
            
               if (s[i] == '*')  {
                   if (s[i+1] == '*') {
                       if (s[i+2] == '*') {
                           i+=3;
                           if (st.contains(3)) {
                               st.pop();
                               res = res + "</i></b>";
                           } else {
                               st.push(3);
                               res = res + "<b><i>";
                           } 

                       } else { 
                           i+=2;
                           if (st.contains(2)) {
                               st.pop();
                               res = res + "</b>";
                          
                           } else {
                               st.push(2);
                               res = res + "<b>";
                           }
                       } 
                   } else {
                       i+=1;
                       if (st.contains(1)) {
                           st.pop();
                           res = res + "</i>";
                       } else {
                           st.push(1);
                           res = res + "<i>";
                       }
                   }
                   
                 }
                res = res + s[i];
                
            } 

            if ((j > 0) && (lines.get(j-1).length == 0)) {
               res = "<p>" + res;
            }  

            if (((j < lines.size() - 1) && (lines.get(j+1).length == 0)) || (j==lines.size()-1)) {
                res = res + "</p>";
            }
            System.out.println(res);
        }
        
    }


}
