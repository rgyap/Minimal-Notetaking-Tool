//import java.io.BufferedReader;
//import java.io.FileReader;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;

import java.io.IOException;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Stack;
import java.util.HashSet;

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
            
			// headings

			str = processHeader1(s);
			if (!str.equals("")) {
				converts.add(str);
				continue;
			}

			
			// headings (alternative style)

             
            if (j < lines.size() - 1 && threeOrMoreLineCharacters(lines.get(j+1))) {
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
			
			// links
			s = processLinks(s);
			
			 
					
			// formatting
			s = lineFormatting(s,st);
			
			// images
			s = processImages(s); 

			// process blockquotes
			if (s.size() > 2 && s.get(0) == '>') {
				converts.add(processBlockQuotes(lines, s, j));
				continue;	
			}

			// add line breaks
			if (shouldInsertBreak(j, lines, st)) {
				s.add('<');s.add('b');s.add('r');s.add('>');
			}
			
			String rrr = "";
			for (char c : s) {
				rrr = rrr + c;
			}

            converts.add(rrr);
        }

		converts = processParagraphs(converts);

        return converts;
    }
	
	
	private static ArrayList<String> processParagraphs(ArrayList<String> c) {
		ArrayList<String> converts = new ArrayList<String>(c);
		for (int l=0; l < converts.size(); l++) {
			if (converts.get(l).length() == 0) {
				continue;
			}
			
			char[] arr = converts.get(l).toCharArray();
			String[] tags = {
				"$$", "<h1>", "<h2>", "<h3>", "<h4>", "<h5>", "<h6>",
				"<hr>", "<blockquote>", "<!--", "<div>"
			}; 
			HashSet<String> blacklistedTags = new HashSet<String>(Arrays.asList(tags));
			
			String lineTag = "";
			boolean shouldContinue = false;
			for (int ll = 0; ll < arr.length; ll++) {
				lineTag = lineTag + arr[ll];
				if (blacklistedTags.contains(lineTag)) {
					shouldContinue = true;
				}
			}

			if (shouldContinue) {
				continue;
			}
			
			if (l == 0 && l+1 < converts.size() && converts.get(l+1).length() == 0) {
				String modded = "<p>" + converts.get(l);
				converts.set(l, modded);
			}
			if (l > 0 && converts.get(l-1).length() == 0) {
				String modded = "<p>" + converts.get(l);
				converts.set(l, modded);
			}
			
			 
			if (l == converts.size() - 1) {
				String modded = converts.get(l) + "</p>";
				converts.set(l, modded);
			} else if (converts.get(l+1).length() == 0) {
				String modded = converts.get(l) + "</p>";
				converts.set(l, modded);
			}
			
		}
		return converts;
		
	}

	private static boolean shouldInsertBreak(int j, ArrayList<ArrayList<Character>> lines, Stack<Integer> st) {
		
		String res = "";
		for (char c : lines.get(j)) {
			res = res + c;
		}
		
		if (j >= lines.size() - 1) {
			return false;
		}

		ArrayList<Character> nextLine = lines.get(j + 1);

		if (nextLine.size() == 0) {
			return false;
		}
		
		// No breaks before and within MathJax math

		if (st.contains(5) || st.contains(6) || st.contains(8)) {
			return false;
		}

		if (res.endsWith("$$")) {
			return false;
		}

		if (nextLine.size() >= 2 && nextLine.get(0) == '$' && nextLine.get(1) == '$') {
			return false;
		}
		
		// No breaks before and after code blocks
		
		if (res.endsWith("</code></pre>")) {
			return false;
		}
		
		if (nextLine.size() >= 3) {
			String check = "";
			for (int i = 0; i < 3; i++) {
				check = check + nextLine.get(i);
			}
			if (check.equals("```")) {
				return false;
			}
		}
		
		// No breaks before block quotes

		if (nextLine.size() >= 2) {
			String check = "";
			int i = 0;
			while (i < nextLine.size()) {
				if (nextLine.get(i) != '>') {
					break;
				}
				i++;
			}
			if (nextLine.get(i) == ' ') {
				return false;
			}
		}

		return true;
	}
	
	private static String processBlockQuotes(ArrayList<ArrayList<Character>> lines, ArrayList<Character> s, int index) {
		ArrayList<String> arr = new ArrayList<String>();
		
		for (char c : s) {
			arr.add(String.valueOf(c));
		}
		
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
 
			k++;
		}
		
		arr.remove(k);
			
		int countNext = 0;
		
		if (index < lines.size()-1 && lines.get(index+1).size() > 0) {
			while (lines.get(index+1).get(countNext) == '>') {
				countNext++;
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

	private static ArrayList<Character> processLinks(ArrayList<Character> in) {
		ArrayList<Character> arr = new ArrayList<Character>(in);
		ArrayList<Character> result = new ArrayList<Character>();
		 
		if (in.size() < 4) {
			return in;
		}
		
		for (int i = 0; i < arr.size(); i++) {
			 
			if (arr.get(0) == '[' || (i > 0 && arr.get(i) == '[' && (arr.get(i-1) != '\\' && arr.get(i-1) != '!'))) {
				String linkText = "";
				String url = "";
				int j = i + 1;
				
				while (j < arr.size() && (arr.get(j) != ']' || arr.get(j-1) == '\\')) {
					linkText = linkText + arr.get(j);
					j++;
				}

				if (j == arr.size()) {
					result.add(arr.get(i));
					continue;
				}
				
				j++;
				
				if (arr.get(j) != '(') {
					result.add(arr.get(i));
					continue;
				}
				
				j++;
				
				while (j < arr.size() && arr.get(j) != ')') {
					url = url + arr.get(j);
					j++;
				}
				
				if (j == arr.size()) {
					result.add(arr.get(i));
					continue;
				}
						
				char[] curl = url.toCharArray();
				String newUrl = "";
				for (char c : curl) {
					if (c == '\\') {
						newUrl = newUrl + '\\';
					}
					newUrl = newUrl + c;
				}
				
				char[] dtxt = linkText.toCharArray();
				String displayText = "";
				for (char c : dtxt) {
					if (c == '\\') {
						displayText = displayText + '\\';
					}
					displayText = displayText + c;
				}
				 
				String str = "<a href='"+newUrl+"'>"+linkText+"</a>";
				System.out.println(str);
				char[] link = str.toCharArray();
				for (char c : link) {
					//if (c == '_') {
					//	result.add('\\');
					//}
					result.add(c);
				}
				i = j;
				continue;
			}
			result.add(arr.get(i));
		}
		return result;
	}
	/*
	private static ArrayList<Character> processLinks2(ArrayList<Character> in) {
		ArrayList<Character> arr = new ArrayList<Character>(in);
		ArrayList<Character> result = new ArrayList<Character>();
		 
		if (in.size() < 4) {
			return in;
		}
		for (int i = 0; i < arr.size(); i++) {
			 
			//if (arr.get(0) == '[' || (i > 0 && arr.get(i) == '[' && (arr.get(i-1) != '\\' && arr.get(i-1) != '!'))) {
			if (arr.get(0) == '[' && arr.get(1) == '[') || (i > 0 && arr.get(i) == '[' && arr.get(i-1) != ')
			}
		}
	}*/
	
	private static ArrayList<Character> processImages(ArrayList<Character> in) {
		ArrayList<Character> arr = new ArrayList<Character>(in);
		ArrayList<Character> result = new ArrayList<Character>();
		
		if (in.size() < 5) {
			return in;
		}
		
		for (int i = 0; i < arr.size(); i++) {
			if (i > 0 && arr.get(i) == '[' && arr.get(Math.max(i-2,0)) != '\\' && arr.get(i-1) == '!') {
				
				String linkText = "";
				String url = "";
				int j = i + 1;
				
				//while (j < arr.size() && (arr.get(j) != ']' || arr.get(j-1) == '\\')) {
				while (j < arr.size() && (arr.get(j) != ']')) {
					linkText = linkText + arr.get(j);
					j++;
				}

				if (j >= arr.size()) {
					result.add(arr.get(i));
					continue;
				}
				
				j++;
				
				if (arr.get(j) != '(') {
					result.add(arr.get(i));
					continue;
				}
				
				j++;
				
				
				while (j < arr.size() && arr.get(j) != ')') {
					url = url + arr.get(j);
					j++;
				}
				
				 
				
				if (j == arr.size()) {
					result.add(arr.get(i));
					continue;
				}
				
				result.remove(i-1);
				
				char[] curl = url.toCharArray();
				String newUrl = "";
				for (char c : curl) {
					//if (c == '\\' ) {
					//	newUrl = newUrl + '\\';
					//}
					newUrl = newUrl + c;
				}
				 
				String str = "<img src='"+newUrl+"' alt='"+linkText+"'>";
				
				char[] link = str.toCharArray();
				for (char c : link) {
					//if (c == '_') {
					//	result.add('\\');
					//}
					result.add(c);
				}
				i = j;
				continue;
			}
			result.add(arr.get(i));
		}
		return result;
	}
	/*
	private static ArrayList<Character> processImages2(ArrayList<Character> in) {
		ArrayList<Character> arr = new ArrayList<Character>(in);
		ArrayList<Character> result = new ArrayList<Character>();
		 
		if (in.size() < 6) {
			return in;
		}
		
		if (i > 0 && arr.get(i) == '[' && arr.get(i-1) != '\\' && arr.get(i-1) == '!') {
		
		
	}*/
	 
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
	
    private static ArrayList<Character> lineFormatting(ArrayList<Character> s1, Stack<Integer> st) {
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
            // Escape
            if (s.get(i) == '\\' && !st.contains(5) && !st.contains(6)) {
                res = res + s.get(i+1);
                i++;
                continue;
            } 

            // Bold and Italics
            if ((s.get(i) == '*' || s.get(i) == '_') && !st.contains(5) && !st.contains(6)) {
				char emph = s.get(i);
                int ac = 0;

				while (ac < 3 && s.get(i+ac) == emph) {
					ac++;
				}
				
				i += ac - 1;
				
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

			// Code Blocks
			if (s.get(i) == '`' && !st.contains(5) && !st.contains(6)) {
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

            // MathJax 
            if (s.get(i) == '$') {
                int dc = 0; 
                if (s.get(i+1) == '$') {
                    dc++;
                }
                
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
		ArrayList<Character> out = new ArrayList<Character>();
		for (char c : res.toCharArray()) {
			out.add(c);
		}

        return out;
    }

    
}
