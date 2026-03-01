import java.util.Arrays;
import java.util.ArrayList;
import java.util.Stack;
import java.util.HashSet;

public final class Convert {

    private Convert() {
		
    }
	
	private static String[] tags = {
        "$$", "<h1>", "<h2>", "<h3>", "<h4>", "<h5>", "<h6>",
        "<hr>", "<blockquote>", "<!--", "<div>", ">", "<pre>", "<ul>", "<ol>"
    }; 
    private static String[] tagsClose = {
        "$$", "</h1>", "</h2>", "</h3>", "</h4>", "</h5>", "</h6>",
        "</hr>", "</blockquote>", "-->", "</div>", "</pre>", "</ul>", "</ol>"
    }; 
    private static HashSet<String> blacklistedTags = new HashSet<String>(Arrays.asList(tags));
    private static HashSet<String> blacklistedTagsClose = new HashSet<String>(Arrays.asList(tagsClose));
	
	private static Stack<Integer> st = new Stack<>(); // FOR FORMATTING
    private static Stack<Integer> sbq = new Stack<>(); // FOR BLOCK QUOTE PROCESSING
	private static Stack<Integer> suo = new Stack<>(); // FOR UNORDERED LISTS
	private static Stack<Integer> sor = new Stack<>(); // FOR ORDERED LISTS
    
    public static ArrayList<String> conv(ArrayList<ArrayList<Character>> lines) {
        ArrayList<String> converts = new ArrayList<String>();
        
        // Conversion magic
        
        for (int j = 0; j < lines.size(); j++) {
            ArrayList<Character> s = new ArrayList<>(lines.get(j)); 

            if (s.size() == 0) {
                converts.add("");
                continue;    
            } 
            
            // Stack lookups are not constant time, but I think these stacks usually don't stack high.
            // Thus, I think I can get away with this. 
			boolean notFormattable = st.contains(5) || st.contains(6) || st.contains(7) || st.contains(8);  // no headers inside math or code!
            
            
            if (!notFormattable) { 

                 
                // headings

                s = processHeader1(s); // hash style
 
                if (j < lines.size() - 1 && threeOrMoreLineCharacters(lines.get(j+1))) { // lines style
                    
                    int hc = 0;
                    if (lines.get(j+1).get(0) == '-') {
                        hc = 2;
                    }
                    if (lines.get(j+1).get(0) == '=') {
                        hc = 1;
                    }

                    if (hc > 0) {
                        s = processHeader2(s, hc);
                        j++;
                    }
                }
                
                // horizontal lines
                if (threeOrMoreLineCharacters(s) && (s.get(0) == '-' || s.get(0) == '_')) {
                    converts.add("<hr>");
                    continue;
                }
                
                // links
                s = processLinks(s);
                s = processLinks2(s);
				
				s = processImages(s); 
                s = processImages2(s); 
        
                // process blockquotes
                if (s.size() >= 1 && s.get(0) == '>') {
                    s = processBlockQuotes(lines, s, j, sbq);
                }		
				
                ArrayList<Character> s1 = new ArrayList<Character>(s);

               // try {
                    if (s.get(0) == '-' || !suo.empty()) {
                        s = processLists(lines, s, j, suo, false);
                    }
                    // ORDERED LISTS MUST START WITH "1."
                    if ((1 < s.size() && Character.isDigit(s.get(0)) && s.get(1) == '.') || !sor.empty()) { 
                        s = processLists(lines, s, j, sor, true);
                    }
                //} catch (Exception e)  {
               //     converts.add("</ul>");
                //    s = s1;
               // }

            } 
    
            // formatting
            s = lineFormatting(s);

            String rrr = "";
            for (char c : s) {
                rrr = rrr + c;
            }

            converts.add(rrr);
        }
		
		// FIX CODEBLOCKS TYPED IN A CERTAIN WAY
		
		for (int j = 0; j < converts.size(); j++) {
			if (converts.get(j).length() == 11 && converts.get(j).startsWith("<pre><code>") && j+1 < converts.size()) {
				String next = converts.get(j+1);
				next = "<pre><code>" + next;
				converts.set(j+1, next);
				converts.set(j, "");
			}
		}
		
		
		converts = processParagraphs(converts);

        return converts;
    }
	
	private static ArrayList<String> processParagraphs(ArrayList<String> strl) {
		ArrayList<String> res = new ArrayList<String>(strl);
		
		if (strl.size() == 0) {
			return strl;
		}
		
		boolean active = true;
		
		String lastStart = "";
		
		for (int j = 0; j < res.size(); j++) {
			String curr = res.get(j);
			if (curr.length() == 0) {
				continue;
			}
			
			String startCurr = startsWithWhatTag(curr);
			String endCurr = endsWithWhatClosingTag(curr);
			
			if (startCurr.length() != 0) {
				active = false;
				lastStart = startCurr;
				if (endCurr.length() != 0) {
					active = true;
					continue;
				}
			}
			
			if (endCurr.length() != 0) {
				active = true;
				continue;
			}
			
			String prev = (j-1 >= 0) ? res.get(j-1) : "";
			if (active && (prev.length() == 0 || endsWithWhatClosingTag(prev).length() != 0)) {
				curr = "<p>" + curr;
			}
				
			String next = (j+1 < res.size()) ? res.get(j+1) : "";

            boolean active2 = active || lastStart.equals("<blockquote>");
			
			if (next.length() != 0 && startsWithWhatTag(next).length() == 0 && active2) {
				//if (active || lastStart.equals("<blockquote>")) {
				curr = curr + "<br>";
				//}
			} else if (active) {
				curr = curr + "</p>";
			}
			
			//if (active || lastStart.equals("<blockquote>")) {
			if (active2) {
                res.set(j, curr);
			}
			
		}
		return res;
	}

	private static String startsWithWhatTag(String s) {
		char[] arr = s.toCharArray();
		String test = "";
		for (int i = 0 ; i < arr.length ; i++) {
			test = test + arr[i];
			if (blacklistedTags.contains(test)) {
				return test;
			}
		}
		return "";
	}
	
	private static String endsWithWhatClosingTag(String str) {
		char[] arr = str.toCharArray();
		String test = "";
		for (int i = arr.length - 1; i >= 0; i--) {
			test = arr[i] + test;
			if (blacklistedTagsClose.contains(test)) {
				return test;
			}
		}
		return "";
	}

	private static ArrayList<Character> processLists(ArrayList<ArrayList<Character>> lines, ArrayList<Character> s, int index, Stack<Integer> stack, boolean ordered) {
		
		final int IND = 2; // Indentation
		
		String xl = "<ul>";
		String xlc = "</ul>";
		
		if (ordered) {
			xl = "<ol>";
			xlc = "</ol>";
		}
		
		ArrayList<String> arr = new ArrayList<String>();
		
		ArrayList<Character> prevprev = new ArrayList<Character>();
        if (index-2 >= 0) {
            prevprev = lines.get(index-2);
        }
		ArrayList<Character> prev = new ArrayList<Character>();
        if (index-1 >= 0) {
            prev = lines.get(index-1);
        }
        ArrayList<Character> next = new ArrayList<Character>();
        if (index+1 < lines.size()) {
            next = lines.get(index+1);
        }
		ArrayList<Character> nextnext = new ArrayList<Character>();
		if (index+2 < lines.size()) {
			nextnext = lines.get(index+2);
		}
		
        int prevCount = countSpacesAtStart(prev) / IND;
        int nextCount = countSpacesAtStart(next) / IND;
        int currCount = countSpacesAtStart(s) / IND;
		int nextnextCount = countSpacesAtStart(nextnext) / IND;
		
		if (!listable(s)) {
			if (!stack.empty()) {
				return new ArrayList<Character>();
			}
			return s;
		}
		
		if (IND*currCount >= s.size()) {
			return s;
		}
		
		
		if (!listable(prevprev) && !listable(prev) && listable(s)) {
			arr.add(xl);
			stack.push(currCount);
		}
		
		if (prevCount < currCount && listable(prev) && listable(s)) {
			arr.add(xl);
			stack.push(currCount);
		}
		
		if (!stack.empty() && listable(s)) {
			arr.add("<li>");
		}
		
		for (int i = IND*currCount+1; i < s.size(); i++) {
			if (ordered && i == IND*currCount+1) {
				continue;
			}
			arr.add(String.valueOf(s.get(i)));
		}
		
		if (!listable(next) && listable(nextnext) && nextCount == currCount+1) {
			arr.add("<p>");
			for (int i = IND*nextCount; i < next.size(); i++) {
				arr.add(String.valueOf(next.get(i)));
			}
			arr.add("</p>");
		}
		
		if (listable(s) && (!listable(next) || nextCount <= currCount)) {
			arr.add("</li>");
		}
		
		if (listable(next) && nextCount < currCount) {
			for (int i = 0 ; i < currCount - nextCount; i++) {
				if (!stack.empty()) {
                    arr.add(xlc);
                    stack.pop();
					arr.add("</li>");
				}
			}
		} 
		
		if (!listable(next) && nextnextCount < currCount) {
			for (int i = 0 ; i < currCount - nextnextCount; i++) {
				if (!stack.empty()) {
                    arr.add(xlc);
                    stack.pop();
					arr.add("</li>");
				}
			}
		}
		
		if (!listable(next) && !listable(nextnext) && !stack.empty()) {
			arr.add(xlc);
			stack.pop();
		}
		
		ArrayList<Character> out = new ArrayList<Character>();
		String o1 = "";
		for (String str : arr) {
			o1 = o1 + str;
		}
		for (char chr : o1.toCharArray()) {
			out.add(chr);
		}
		return out;
	}
    
	private static int countSpacesAtStart(ArrayList<Character> line) {
		if (line.size() == 0) {
			return 0;
		}
		int result = 0;
		while (result < line.size() && line.get(result) == ' ') {
			result++;
		}
		if (result >= line.size()) {
			return 0;
		}
		
		return result;
	}
	
	private static boolean listable(ArrayList<Character> line) {
		int spaces = countSpacesAtStart(line);
		if (spaces < line.size() && line.get(spaces) == '-') {
			return true;
		}
		if (spaces + 1 < line.size() && Character.isDigit(line.get(spaces)) && line.get(spaces+1) == '.') {
			return true;
		}
		
	
		return false;
	}
	
    private static ArrayList<Character> processBlockQuotes(ArrayList<ArrayList<Character>> lines, ArrayList<Character> s, int index, Stack<Integer> stack) {
        ArrayList<String> arr = new ArrayList<String>();

        String content = bqcont(s);

        if (s.size() == 0) {
            return s;
        }
        if (s.get(0) != '>') {
            return s;
        }

        ArrayList<Character> prev = new ArrayList<Character>();
        if (index-1 >= 0) {
            prev = lines.get(index-1);
        }
        ArrayList<Character> next = new ArrayList<Character>();
        if (index+1 < lines.size()) {
            next = lines.get(index+1);
        }
        int prevCount = lenofgr(prev);
        int nextCount = lenofgr(next);
        int currCount = lenofgr(s);

        if (prevCount < currCount && !stack.contains(currCount)) {
            int diff = currCount - prevCount; 
            for (int i = 0; i < diff; i++) {
                stack.push(1);
                arr.add("<blockquote>");
            }
        }
        
        arr.add(content);

        if (nextCount < currCount) {
            int diff = currCount - nextCount;
            for (int i = 0; i < diff; i++) {
                stack.pop();
                arr.add("</blockquote>");
            }
        }

        String o1 = "";
        for (String str : arr) {
            o1 = o1 + str;
        }
        ArrayList<Character> out = new ArrayList<Character>();
        for (char c : o1.toCharArray()) {
            out.add(c);
        }
		
		if (out.size() == 0) {
			out.add((char)0);
		}
		
        return out;
    }

    private static int lenofgr(ArrayList<Character> s) {
        if (s.size() == 0) {
            return 0;
        }
        if (s.get(0) != '>') {
            return 0;
        }
        int result = 0;
        while (result < s.size() && s.get(result) == '>') {
            result++;
        }
        return result;
    }

    private static String bqcont(ArrayList<Character> s) {
        if (s.size() == 0) {
            return "";
        }
        String result = "";
        int i = 0;
        while (i < s.size() && s.get(i) == '>') {
            i++;
        }
        for (int j=i; j<s.size(); j++) {
			if (j == i && s.get(j) == ' ') {
				continue;
			}
            result = result + s.get(j);
        }
        return result;
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
                    if (arr.get(j) == '\\') {
                        j++;
                    }
                    linkText = linkText + arr.get(j);
                    j++;
                }

                if (j == arr.size()) {
                    result.add(arr.get(i));
                    continue;
                }
                
                j++;
                
                if (j >= arr.size() || arr.get(j) != '(') { // We still need to check j >= arr.size() because there still might be confusion with Wiki-style links
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
				/*
                char[] curl = url.toCharArray();
                String newUrl = "";
                for (char c : curl) {
                    newUrl = newUrl + c;
                }*/
                /*
                char[] dtxt = linkText.toCharArray();
                String displayText = "";
                for (char c : dtxt) {
                    if (c == '\\') {
                        displayText = displayText + '\\';
                    }
                    displayText = displayText + c;
                }*/
                 
                String str = "<a href='"+url+"'>"+linkText+"</a>";
                 
                char[] link = str.toCharArray();
                for (char c : link) {
                    result.add(c);
                }
                i = j;
                continue;
            }
            result.add(arr.get(i));
        }
        return result;
    }
    
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
				String title = "";
                int j = i + 1;
                
                while (j < arr.size() && (arr.get(j) != ']' || arr.get(j-1) == '\\')) {
                    if (arr.get(j) == '\\') {
                        j++;
                    } 
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
                
                 
                
                if (j >= arr.size()) {
                    result.add(arr.get(i));
                    continue;
                }
                
                result.remove(i-1);
                
                char[] curl = url.toCharArray();
                String newUrl = "";
                for (char c : curl) {
                    newUrl = newUrl + c;
                }
                 
                String str = "<img src='"+newUrl+"' alt='"+linkText+"'>";
                
                char[] link = str.toCharArray();
                for (char c : link) {
                    result.add(c);
                }
                i = j;
                continue;
            }
            result.add(arr.get(i));
        }
        return result;
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
	
    private static ArrayList<Character> processHeader1(ArrayList<Character> s) {
        int hc = 0;
     
        if (s.size() < 3) {
            return s;
        }
        
        if (s.get(0) != '#') {
            return s;
        }

        for(; hc < s.size(); hc++) {
            if (s.get(hc) != '#') {
                break;
            }
        }

        if (s.get(hc) != ' ') {
            return s;
        }

        String content = "";
        for (int k = hc+1; k < s.size(); k++) {
            content = content + s.get(k);
        }

        String result = "<h"+hc+">"+content+"</h"+hc+">";
        
        ArrayList<Character> out = new ArrayList<Character>();
        for (char c : result.toCharArray()) {
            out.add(c);
        }

        return out;

    }
    
    private static ArrayList<Character> processHeader2(ArrayList<Character> s, int hc) {
        char hcchar = (char)(hc + '0');
        ArrayList<Character> out = new ArrayList<Character>();
        
        out.add('<');out.add('h');out.add(hcchar);out.add('>');
        
        for (char c : s) {
            out.add(c);
        }
        
        out.add('<');out.add('/');out.add('h');out.add(hcchar);out.add('>');
        
        return out;
    }
    
    private static ArrayList<Character> lineFormatting(ArrayList<Character> s1) {
        ArrayList<Character> s = new ArrayList<>(s1);
        
        /*
        LEGEND for the stack st:
        0 -> Uh, nothing!
        1 -> *
        2 -> **
        3 -> ***
        4 -> _
        5 -> $
        6 -> $$
        7 -> `
        8 -> ```
		9 -> __
		10 -> ___
        */
		         
        String[][] formatters = {
            {"", ""},                           // 0
            {"<i>", "</i>"},                    // 1
            {"<b>", "</b>"},                    // 2
            {"<i><b>", "</b></i>"},             // 3
            {"<em>", "</em>"},                  // 4
            {"\\(", "\\)"},                     // 5
            {"$$", "$$"},                       // 6
            {"<code>", "</code>"},              // 7
            {"<pre><code>", "</code></pre>"},   // 8
			{"<strong>", "</strong>"},          // 9
			{"<em><strong>", "</strong></em>"}  // 10
        };
        String res = "";
        
        // padding to not make Java angry with out-of-bounds exceptions
        s.add((char)0);
        s.add((char)0);
        s.add((char)0);

        for (int i = 0; i < s.size()-3; i++) {

            // Escape
            if (s.get(i) == '\\' && !st.contains(5) && !st.contains(6) && !st.contains(7) && !st.contains(8)) {
                res = res + s.get(i+1);
                i++;
                continue;
            } 

            // Bold and Italics
            if ((s.get(i) == '*' || s.get(i) == '_') && !st.contains(5) && !st.contains(6) && !st.contains(7) && !st.contains(8)) {
                char emph = s.get(i);
                int ac = 0;

                while (ac < 3 && s.get(i+ac) == emph) {
                    ac++;
                }
                
                i += ac - 1;
                
				if (emph == '_') {
					if (ac == 1) {
						ac = 4;
					}
					if (ac == 2) {
						ac = 9;
					}
					if (ac == 3) {
						ac = 10;
					}
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
				 
				if ((k == 7 && !st.contains(8)) || k == 8) {
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
            if (s.get(i) == '$' && !st.contains(7) && !st.contains(8)) {
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
			
			if ((st.contains(7) || st.contains(8)) && s.get(i) == '<') {
				res = res + "&lt;";
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

    private static ArrayList<Character> processImages2(ArrayList<Character> in) {
		ArrayList<Character> arr = new ArrayList<Character>(in);
        ArrayList<Character> result = new ArrayList<Character>();
		
		if (in.size() < 6) {
			return in;
		}
		
		for (int i = 0; i < arr.size(); i++) {
            if (i > 0 && i+1 < arr.size() && arr.get(i) == '[' && arr.get(i+1) == '[' && arr.get(i-1) == '!' && arr.get(Math.max(i-2,0)) != '\\') {
                String imageName = "";
                String imageSize = "";
                int j = i + 2;
                
                while (j < arr.size() && (arr.get(j) != '|')) {
                    imageName = imageName + arr.get(j);
                    j++;
                }

                if (j >= arr.size()) {
                    result.add(arr.get(i));
                    continue;
                }
                
                j++;

                while (j < arr.size() && arr.get(j) != ']') {
                    imageSize = imageSize + arr.get(j);
                    j++;
                }
				
				j++;
                
                
                if (j >= arr.size()) {
                    result.add(arr.get(i));
                    continue;
                }
				
				if (arr.get(j) != ']') {
                    result.add(arr.get(i));
                    continue;
                }
                
                result.remove(i-1);
 
				String url = Find.find("./notes", imageName);
				
				char[] curl = url.toCharArray();
                String newUrl = "";
                for (char c : curl) {
                    if (c == '\\' || c == '_') {
                        newUrl = newUrl + '\\';
                    }
                    newUrl = newUrl + c;
                }
                
                String str = "<img src='"+newUrl+"' width='"+imageSize+"'>";
                
                char[] imgt = str.toCharArray();
                for (char c : imgt) {
                    result.add(c);
                }
                i = j;
                continue;
            }
            result.add(arr.get(i));
        }
        return result;

	}	
	
	
	private static ArrayList<Character> processImages22(ArrayList<Character> in) {
		ArrayList<Character> arr = new ArrayList<Character>(in);
        ArrayList<Character> result = new ArrayList<Character>();
		
		if (in.size() < 6) {
			return in;
		}
		
		for (int i = 0; i < arr.size(); i++) {
            if (i > 0 && i+1 < arr.size() && arr.get(i) == '[' && arr.get(i+1) == '[' && arr.get(i-1) == '!' && arr.get(Math.max(i-2,0)) != '\\') {
                String imageName = "";
                String imageSize = "";
                int j = i + 2;
                
                while (j < arr.size() && (arr.get(j) != '|')) {
                    imageName = imageName + arr.get(j);
                    j++;
                }

                if (j >= arr.size()) {
                    result.add(arr.get(i));
                    continue;
                }
                
                j++;

                while (j < arr.size() && arr.get(j) != ']') {
                    imageSize = imageSize + arr.get(j);
                    j++;
                }
				
				j++;
                
                
                if (j >= arr.size()) {
                    result.add(arr.get(i));
                    continue;
                }
				
				if (arr.get(j) != ']') {
                    result.add(arr.get(i));
                    continue;
                }
                
                result.remove(i-1);
 
				String url = Find.find("./notes", imageName);
                
                String str = "<img src='"+url+"' width='"+imageSize+"'>";
                
                char[] imgt = str.toCharArray();
                for (char c : imgt) {
                    result.add(c);
                }
                i = j;
                continue;
            }
            result.add(arr.get(i));
        }
        return result;

	}	
	
	
	
	
	

	private static ArrayList<Character> processLinks2(ArrayList<Character> in) {
		ArrayList<Character> arr = new ArrayList<Character>(in);
        ArrayList<Character> result = new ArrayList<Character>();
		
		if (in.size() < 5) {
			return in;
		}
		
		for (int i = 0; i < arr.size(); i++) {
            if (i+1 < arr.size() && arr.get(i) == '[' && arr.get(i+1) == '[' && arr.get(Math.max(i-1,0)) != '\\' && arr.get(Math.max(i-1,0)) != '!' ) {
                String fileName = "";
                String displayText = "";
                int j = i + 2;
                
                while (j < arr.size() && (arr.get(j) != '|')) {
                    fileName = fileName + arr.get(j);
                    j++;
                }

                if (j >= arr.size()) {
                    result.add(arr.get(i));
                    continue;
                }
                
                j++;

                while (j < arr.size() && (arr.get(j) != ']' || arr.get(j-1) == '\\')) {
                    if (arr.get(j) == '\\') {
                        j++;
                    }
                    displayText = displayText + arr.get(j);
                    j++;
                }
				
				j++;
                
                
                if (j >= arr.size()) {
                    result.add(arr.get(i));
                    continue;
                }
				
				String url = Find.find("./mainnotes", fileName);
				
				char[] curl = url.toCharArray();
                String newUrl = "";
                for (char c : curl) {
                    if (c == '\\' || c == '_') {
                        newUrl = newUrl + '\\';
                    }
                    newUrl = newUrl + c;
                }
				
                
				String str = "<a href='"+newUrl+"'>"+displayText+"</a>";
                
                char[] imgt = str.toCharArray();
                for (char c : imgt) {
                    result.add(c);
                }
                i = j;
                continue;
            }
            result.add(arr.get(i));
        }
        return result;

	}	
}
