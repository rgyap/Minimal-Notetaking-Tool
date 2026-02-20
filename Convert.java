import java.io.BufferedReader;
import java.io.FileReader;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;

import java.io.IOException;

import java.util.ArrayList;
import java.util.Stack;

public final class Convert {

    private Convert() {
    }

    public static ArrayList<String> conv(ArrayList<ArrayList<Character>> lines) {
        ArrayList<String> converts = new ArrayList<String>();
        Stack<Integer> st = new Stack<>();

        for (int j = 0; j < lines.size(); j++) {
            ArrayList<Character> s = new ArrayList<>(lines.get(j)); 
            
            String res = ""; 

            if (s.size()  == 0) {
				converts.add("");
                continue;    
            } 
            
			// headings
            if (s.get(0) == '#') {
                int[] toh = toHeaderize1(lines, j);
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
			
			// headings 2
			if (j < lines.size() - 1) {
				if (threeOrMoreLineCharacters(lines.get(j+1))) {
					int[] toh = toHeaderize2(lines, j);
					if (toh[0] == 1) {
						int hc = toh[1];
						res = "<h" + hc + ">" + res;
						for (char c : s) {
							res = res + c;
						}
						res = res + "</h"+hc+">";
						converts.add(res);
						j+=2;
						continue;
					}
				}
			}
			
			// horizontal lines
			
			if (s.size() >= 3) {
				boolean addHorizontalLine = false;
				
				if (threeOrMoreLineCharacters(s)) {
					if (j == 0) {
						if (lines.get(1).size() == 0) {
							addHorizontalLine = true;
						} 
					} else if (j > 0 && j < lines.size() - 1) {
						if ((lines.get(j-1).size() == 0) && (lines.get(j+1).size() == 0)) {
							addHorizontalLine = true;
						}
					} else {
						if (lines.get(lines.size()-2).size() == 0) {
							addHorizontalLine = true; 
						}
					}
				}
				if (addHorizontalLine) {
					res = res + "<hr>";
					converts.add(res);
					j++;
					continue;
				}
			}
			
			// formatting
            res = lineFormatting(s, st);
			
			// process paragraphs
			
			if (j == 0) {
				res = "<p>" + res;
			}
			
			if (j > 0 && lines.get(j-1).size() == 0) {
				res = "<p>" + res;
			}
			
			if ((j < lines.size() - 1 && lines.get(j+1).size() == 0) || j==lines.size()-1) {
				res = res + "</p>";
			}
			
			// process blockquotes
			
			if (s.size() > 2) {
				char[] arr = res.toCharArray();
				int offset = 0;
				String tag = res.substring(0, 3);
				if (tag.equals("<p>")) {
					offset = 3;
				}
				
				if (arr[0+offset] == '>' && arr[1+offset] == ' ') {
					res = res.substring(2+offset);
					if (lines.get(j-1).size() == 0) {
						res = "<blockquote>" + res;
					} 
					if (lines.get(j+1).size() == 0) {
						res = res + "</blockquote>";
					}
				}
			}
			
			// add line breaks
			
            if ((j < lines.size() - 1) && (lines.get(j+1).size() > 0)) {
                res = res + "<br>";
            } 
		
            converts.add(res);
        }

        return converts;

    }

	public static boolean threeOrMoreLineCharacters(ArrayList<Character> s) {
		if (s.size() < 3) {
			return false;
		}
		
		char lineChar = s.get(0); 
		 
		if ((lineChar == '-') || (lineChar == '=') || (lineChar == '_')) {
			for (int i = 0; i < s.size(); i++) {
				if (s.get(i) != lineChar) {
					return false;
				}
			}
			return true;
		}
		return false;
	}

    public static int[] toHeaderize1(ArrayList<ArrayList<Character>> lines, int index) {
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
	
	public static int[] toHeaderize2(ArrayList<ArrayList<Character>> liness, int index) {
		 
        ArrayList<ArrayList<Character>> lines = new ArrayList<ArrayList<Character>>(liness);
		char l = lines.get(index+1).get(0);
		
		if (!(l == '-' || l == '=' || l == '_')) {
			return new int[]{0,0};
		}
		
		int hc = 1;
        int headerize = 0;
		
		if (l == '-' || l == '_') {
			hc = 2;
		}

        // padding to not make Java angry with out-of-bounds exceptions
        lines.add(null);
		lines.add(null);
		
		if (index == 0) {
            if (threeOrMoreLineCharacters(lines.get(1)) && lines.get(2).size() == 0) {
                headerize = 1;
            } 
        } else if (index > 0 && index < lines.size() - 1) {
            if ((lines.get(index-1).size() == 0) && threeOrMoreLineCharacters(lines.get(index+1))) {
                headerize = 1;
            }
        } 
        return new int[]{headerize, hc};
		
	}

    public static String lineFormatting(ArrayList<Character> s1, Stack<Integer> st) {
        ArrayList<Character> s = new ArrayList<>(s1);
		
		/*
		LEGEND:
		0 -> Uh, nothing!
		1 -> *
		2 -> **
		3 -> ***
		4 -> _
		5 -> $
		6 -> $$
		7 -> `
		8 -> ```
		*/
		 
        String[][] formatters = {
			{"", ""},	                        // 0
			{"<i>", "</i>"},                    // 1
			{"<b>", "</b>"},                    // 2
			{"<i><b>", "</b></i>"},             // 3
			{"<em>", "</em>"},                  // 4
			{"\\(", "\\)"},                     // 5
			{"$$", "$$"},                       // 6
			{"<code>", "</code>"},              // 7
			{"<pre><code>", "</code></pre>"}    // 8
		};
        String res = "";
        
        // padding to not make Java angry with out-of-bounds exceptions
        s.add((char)0);
        s.add((char)0);
        s.add((char)0);
        
        for (int i = 0; i < s.size()-3; i++) {
            // Delimiter
            if (s.get(i) == '\\') {
                if (!(st.contains(5) || st.contains(6))) {

                    res = res + s.get(i+1);
                    //i+=2;
                    i+=1;
                    continue;
                }
            } 

            // Bold and Italics
            if (s.get(i) == '*' || s.get(i) == '_') {
				char emph = s.get(i);
				if (!(st.contains(5) || st.contains(6))) {
                    int ac = 0;

					while ((ac < 3) && (s.get(i+ac) == emph)) {
						ac++;
					}
					//i += ac;
					i+= (ac-1);
					
					if (ac==1 && emph == '_') {
						ac = 4;
					}
					
					if (st.contains(ac)) {
						st.pop();
						res = res + formatters[ac][1];
					} else {
						st.push(ac);
						res = res + formatters[ac][0];
					
					}
					continue;
                }
            }

			// Code Blocks
			if (s.get(i) == '`') {
                if (!(st.contains(5) || st.contains(6))) {
					int cc = 0;
					int m = 0;
					if ((s.get(i+1) == '`') && (s.get(i+2) == '`')) {
						cc++;
						m = 2;
					}
					i += m; 
                    int k = 7+cc;
                    if (st.contains(k)) {
                        st.pop();
                        res = res + formatters[k][1];
                    } else {
                        st.push(k);
                        res = res + formatters[k][0];
                    }
                    continue;
                }
            }

            // MathJax 
            if (s.get(i) == '$') {
                int dc = 0; 
                if (s.get(i+1) == '$') {
                    dc++;
                }
                //i += dc;
                i += dc;
                int k = 5 + dc;
                if (st.contains(k)) {
                    st.pop();
                    res = res + formatters[k][1];
                } else {
                    st.push(k);
                    res = res + formatters[k][0];
                }
                continue;
            } 

            res = res + s.get(i);
        } 
        return res;
    }

    
}
