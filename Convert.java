//import java.io.BufferedReader;
//import java.io.FileReader;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;

import java.io.IOException;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Stack;

public final class Convert {

    private Convert() {
    }

    public static ArrayList<String> conv(ArrayList<ArrayList<Character>> lines) {
        ArrayList<String> converts = new ArrayList<String>();
        Stack<Integer> st = new Stack<>();
		
		// Conversion magic
		// WARNING: HUGE function
		
        for (int j = 0; j < lines.size(); j++) {
            ArrayList<Character> s = new ArrayList<>(lines.get(j)); 
            
            String res = "";
			String str = ""; // for general use for processing

            if (s.size()  == 0) {
				converts.add("");
                continue;    
            } 
            
            // images
            
            //if (s.get(0) == '!') {
			str = processImage1(s) + processImage2(s);
			if (!str.equals("")) {
				converts.add(str);
				continue;
			}
            //}
            

			// headings

			str = processHeader1(s);
			if (!str.equals("")) {
				converts.add(str);
				continue;
			}

			
			// headings (alternative style)

            if (j < lines.size() - 1) {
                if (threeOrMoreLineCharacters(lines.get(j+1))) {
                    int hc = 0;
                    if (lines.get(j+1).get(0) == '-') {
                        hc = 2;
                    }
                    if (lines.get(j+1).get(0) == '=') {
                        hc = 1;
                    }

					if (hc > 0) {
						converts.add(processHeader2(s, hc));
                        j++;
                        continue;
					}
						
                }
            }
			
			// horizontal lines
			
			if (s.size() >= 3) {
				boolean addHorizontalLine = false;
				
				if (threeOrMoreLineCharacters(s)) {
					addHorizontalLine = true;
				}
				if (addHorizontalLine) {
					res = res + "<hr>";
					converts.add(res);

					continue;
				}
			}
			
			// formatting
            res = lineFormatting(s, st);
			
			// process blockquotes
			
			if (s.size() > 2 && s.get(0) == '>') {
				converts.add(processBlockQuotes(lines, res, j));
				continue;	
			}
			
			// process paragraphs
			/*
			if (j == 0) {
				res = "<p>" + res;
			}
			
			if (j > 0 && lines.get(j-1).size() == 0) {
				res = "<p>" + res;
			}
			
			if ((j < lines.size() - 1 && lines.get(j+1).size() == 0) || j==lines.size()-1) {
				res = res + "</p>";
			}*/
			
			// add line breaks
			
            if ((j < lines.size() - 1) && (lines.get(j+1).size() > 0)) {
				if (!(st.contains(5) || st.contains(6) || st.contains(8)) && !(res.substring(Math.max(res.length() - 2, 0)).equals("$$"))) {
					res = res + "<br>";
				}
            }
		
            converts.add(res);
        }
		
		for (String convLine : converts) {
			char[] arr = convLine.toCharArray();
			String[] tags = {
				"$$", "<h1>", "<h2>", "<h3>", "<h4>", "<h5>", "<h6>",
				"<img>","<hr>", "<blockquote>"
			}; 
			HashSet<String> blacklistedTags = new HashSet<String>(Arrays.asList(tags));
			if (
		}

        return converts;
	
    }
	
	private static String processBlockQuotes(ArrayList<ArrayList<Character>> lines, String in, int index) {
		ArrayList<String> arr = new ArrayList<String>(Arrays.asList(in.split("")));
		String out = "";
		 
		int k = 0;
		while (arr.get(k).equals(">")) {
			char prev = (char)0;
			if (k < lines.get(index-1).size()) {
				prev = lines.get(index-1).get(k);
			}
			
			if (prev != '>') {
				arr.set(k, "<blockquote>");
			} else {
				arr.set(k, "");
			}
			arr.remove(k+1);
			k++;
		}
		 
		int countNext = 0;
		if (index < lines.size()-1) {
			if (lines.get(index+1).size() > 0) {
				while (lines.get(index+1).get(countNext) == '>') {
					countNext++;
				}
			}
		}
		
		if (k > countNext) {
			if (k > 1) {
				k--;
			}
			for (int k2 = 0; k2 < k; k2++) {
				arr.add("</blockquote>");
			}						
		}
		
		for (int k3=0; k3 < arr.size(); k3++) {
			out += arr.get(k3);
		} 
		return out;
	}

    private static String processImage1(ArrayList<Character> s) {
        String imageName = "";
        String imageAltText = "";

        if (s.size() < 3) {
            return "";
        }

        if (!(s.get(0) == '!') || !(s.get(1) == '[') || (s.get(2) == '[')) {
            return "";
        }

        int k = 2;
        for (; k < s.size(); k++) {
            if (s.get(k) == ']') {
                break;
            }
            imageAltText = imageAltText + s.get(k);
        }
        k++;
        if (!(s.get(k) == '(') || !(s.get(s.size()-1) == ')')) {
            return "";
        }
        k++;
        for (; k < s.size()-1; k++) {
            imageName = imageName + s.get(k);
        }

        String path = Find.findFile(Paths.get("./notes"), imageName);
        String out = "<img src='"+path+"' alt='"+imageAltText+"'>";
        return out;
    }
    
    private static String processImage2(ArrayList<Character> s) {
        // ![[imagename|width size in px or %]]
        // For example, 
        // ![[image.png|300]]
        // ![[another.png|50%]]

        String imageName = "";
        String imageSize = "";

        if (s.size() < 3) {
            return "";
        }
        if (s.get(0) != '!' || s.get(1) != '[' || s.get(2) != '[') {
            return "";
        }
        if (s.get(s.size()-1) != ']' || s.get(s.size()-2) != ']') {
            return "";
        }

        int k=3;
        for (; k < s.size(); k++) {
            if (s.get(k) == '|') {
                break;
            }
            imageName = imageName + s.get(k);
        }
        k++;
        for(; k < s.size(); k++) {
            if (s.get(k) == ']') {
                break;
            }
            imageSize = imageSize + s.get(k);
        }
        
        String path = Find.findFile(Paths.get("./notes"), imageName);
        String out = "<img src='"+path+"' width='"+imageSize+"'>";
        return out;
    }

	private static boolean threeOrMoreLineCharacters(ArrayList<Character> s) {
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

    private static String processHeader1(ArrayList<Character> s) {
        int hc = 0;

        if (s.size() < 3) {
            return "";
        }
		
		if (s.get(0) != '#') {
            return "";
        }

        for(; hc < s.size(); hc++) {
            if (s.get(hc) != '#') {
                break;
            }
        }

        if (s.get(hc) != ' ') {
            return "";
        }

        String content = "";
        for (int k = hc+1; k < s.size(); k++) {
            content = content + s.get(k);
        }

        String out = "<h"+hc+">"+content+"</h"+hc+">";
        return out;
    }

    private static String processHeader2(ArrayList<Character> s, int hc) {
        String out = "";
        for (char c : s) {
            out = out + c;
        }
        out = "<h"+hc+">"+out+"</h"+hc+">";
        return out;
    }

    private static String lineFormatting(ArrayList<Character> s1, Stack<Integer> st) {
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
