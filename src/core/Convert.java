package src.core;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Stack;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Map;

import java.nio.file.Path;
import java.nio.file.Paths; // FOR INTERNAL LINKS

public final class Convert {

    private Convert() {
        
    }
    
    private static String[] tags = {
        "\\[", "<h1>", "<h2>", "<h3>", "<h4>", "<h5>", "<h6>", "<blockquote>", "<div>", "<pre>", "<ul>", "<ol>", "<table>"
    }; 
    private static String[] tagsClose = {
        "\\]", "</h1>", "</h2>", "</h3>", "</h4>", "</h5>", "</h6>", "</blockquote>", "</div>", "</pre>", "</ul>", "</ol>", "</table>"
    }; 
	
	 
	
    private static HashSet<String> blacklistedTags = new HashSet<String>(Arrays.asList(tags));
    private static HashSet<String> blacklistedTagsClose = new HashSet<String>(Arrays.asList(tagsClose));
    
	private static ArrayList<String> alignments = new ArrayList<String>(); // TABLE ALIGNMENTS
   
    public static final HashMap<String, String[]> forms = new HashMap<String, String[]>(Map.ofEntries(
        Map.entry("*", new String[]{"<i>", "</i>"}),
        Map.entry("**", new String[]{"<b>", "</b>"}),
        Map.entry("***", new String[]{"<i><b>", "</b></i>"}),
        Map.entry("_", new String[]{"<em>", "</em>"}),
        Map.entry("__", new String[]{"<strong>", "</strong>"}),
        Map.entry("___", new String[]{"<em><strong>", "</strong></em>"}),
        Map.entry("`", new String[]{"<code>", "</code>"}),
        Map.entry("```", new String[]{"<pre><code>", "</code></pre>"}),
        Map.entry("$", new String[]{"\\(", "\\)"}),
        Map.entry("$$", new String[]{"\\[", "\\]"})
    ));

    public static ArrayList<String> convertNote(ArrayList<ArrayList<Character>> lines, Path currPath, Path root, int IND) {
        ArrayList<String> result = new ArrayList<String>();

        ArrayList<ArrayList<Character>> in0 = conv(lines, currPath, root, IND);
        ArrayList<ArrayList<Character>> in = processParagraphs(in0);
        for (ArrayList<Character> clist : in) {
            String test = "";
            for (char c : clist) {
                test = test + c;
            }
            result.add(test);
        }
        return result;
    }
    
    public static ArrayList<ArrayList<Character>> processParagraphs(ArrayList<ArrayList<Character>> lines) {

        // MAKE SURE that paragraphs have space ABOVE AND BELOW.

        Stack<String> stg = new Stack<String>();
        ArrayList<ArrayList<Character>> result  = new ArrayList<ArrayList<Character>>();

        for (int i = 0; i < lines.size(); i++) {
            ArrayList<Character> currentLine = lines.get(i);
			
			String hrtest = "";
			
			for (char c : currentLine) {
				hrtest = hrtest + c;
			}
			
            if (currentLine.size() == 0 || hrtest.equals("<hr>")) {
                result.add(currentLine);
                continue;
            }

            String test = "";
            for (int j = 0; j < currentLine.size(); j++) {
				if (j+1 < currentLine.size()) {
					if (currentLine.get(j) == '<' && currentLine.get(j+1) != '/') {
						while (j < currentLine.size() && currentLine.get(j) != '>') {
							test = test + currentLine.get(j);
							j++;
						}
						test = test + '>';
					} else if (currentLine.get(j) == '\\' && currentLine.get(j+1) == '[') {
						test = "\\[";
						j++;
					}
                }
                if (blacklistedTags.contains(test)) {
                    stg.push(test);
                    test = ""; 
                }
            }

            ArrayList<Character> prevLine = (i > 0) ? lines.get(i-1) : new ArrayList<Character>();
            ArrayList<Character> nextLine = (i < lines.size()-1) ? lines.get(i+1) : new ArrayList<Character>();
			
			if (stg.empty() && ((prevLine.size() == 0) || endsWithClosingTag(prevLine))) {
				currentLine.add(0, '>');
                currentLine.add(0, 'p');
                currentLine.add(0, '<');
			}
			
			boolean condition1 = stg.empty() && ((nextLine.size() == 0) || startsWithTag(nextLine));
			boolean condition2 = (nextLine.size() != 0) && (stg.empty() || stg.contains("<blockquote>")) && !endsWithClosingTag(currentLine);
			
			if (condition1) {
                currentLine.add('<');
                currentLine.add('/');
                currentLine.add('p');
                currentLine.add('>');
			} else if (condition2) {
				currentLine.add('<');
				currentLine.add('b');
				currentLine.add('r');
				currentLine.add('>');  
			}
			
            String testend = "";
            for (int j = 0; j < currentLine.size(); j++) {
				if (j+1 < currentLine.size()) {
					if (currentLine.get(j) == '<' && currentLine.get(j+1) == '/') {
						while (j < currentLine.size() && currentLine.get(j) != '>') {
							testend = testend + currentLine.get(j);
							j++;
						}
						testend = testend + '>';
					} else if (currentLine.get(j) == '\\' && currentLine.get(j+1) == ']') {
						testend = "\\]";
						j++;
					}
                }

                if (blacklistedTagsClose.contains(testend) && !stg.empty()) {
                    stg.pop();
                }
                testend = "";
            }
		
            result.add(currentLine);
        }
        return result;
    }
	
	public static boolean startsWithMathOrCode(ArrayList<Character> list) {
		String test = "";
		for (char c : list) {
			test =  test + c;
			if (test.equals("$$") || test.equals("```")) {
				return true;
			}
		}
		return false;
	}

    public static ArrayList<ArrayList<Character>> conv(ArrayList<ArrayList<Character>> lines, Path currPath, Path root, int IND) {
        
        ArrayList<ArrayList<Character>> converts = new ArrayList<ArrayList<Character>>();
        
        Stack<String> sst = new Stack<String>(); // FOR FORMATTING
        Stack<Integer> sbq = new Stack<Integer>(); // FOR BLOCK QUOTE PROCESSING
        Stack<Integer> suo = new Stack<Integer>(); // FOR UNORDERED LISTS
        Stack<Integer> sor = new Stack<Integer>(); // FOR ORDERED LISTS
        Stack<Integer> stb = new Stack<Integer>(); // FOR TABLES
        
        for (int j = 0; j < lines.size(); j++) {
            ArrayList<Character> s = new ArrayList<>(lines.get(j)); 

            if (s.size() == 0) {
                converts.add(new ArrayList<Character>());
                continue;    
            } 
            
            // Stack lookups are not O(1) but O(n) in time, but I think these stacks usually don't stack high.
            // Thus, I think I can get away with this. 
            boolean notFormattable = sst.contains("$") || sst.contains("$$") || sst.contains("`") || sst.contains("```");

            if (!notFormattable && !startsWithMathOrCode(s)) { 

                // headings
                if (s.get(0) == '#') {
                    s = processHeader1(s); // hash style
                }
                
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
                    Character[] hrtag = {'<', 'h', 'r', '>'};
                    ArrayList<Character> hrl = new ArrayList<Character>(Arrays.asList(hrtag));
                    converts.add(hrl);
                    continue;
                }
                
                // links
                s = processLinks(s);
                s = processLinks2(s, currPath, root);
                
				// images
                s = processImages(s); 
                s = processImages2(s, currPath, root); 
        
        
                if (s.get(0) == '|') {
                    s = processTables(lines, s, j, stb);
                }
                 
                // process blockquotes
                if (s.size() >= 1 && s.get(0) == '>') {
                    s = processBlockQuotes(lines, s, j, sbq);
                }
                
                if (s.get(0) == '-' || !suo.empty()) {
                    s = processLists(lines, s, j, suo, false, IND);
                }
                // ORDERED LISTS MUST START WITH "1."
                
                if ((1 < s.size() && Character.isDigit(s.get(0)) && s.get(1) == '.') || !sor.empty()) { 
                    s = processLists(lines, s, j, sor, true, IND);
                }

            } 
    
            // formatting
            s = lineFormatting(s, sst);

            converts.add(s);
        }
        
        // FIX CODEBLOCKS TYPED IN A CERTAIN WAY
        
        for (int j = 0; j < converts.size(); j++) {
            String test = "";
            for (char c : converts.get(j)) {
                test = test + c;
            }

            if (test.equals("<pre><code>") && j+1 < converts.size()) {
                ArrayList<Character> next = converts.get(j+1);
                //next = "<pre><code>" + next;
                next.add(0, '>');
                next.add(0, 'e');
                next.add(0, 'd');
                next.add(0, 'o');
                next.add(0, 'c');
                next.add(0, '<');
                
                next.add(0, '>');
                next.add(0, 'e');
                next.add(0, 'r');
                next.add(0, 'p');
                next.add(0, '<');

                converts.set(j+1, next);
                converts.set(j, new ArrayList<Character>());
            }
        }
        
        return converts;
    }

    private static ArrayList<Character> processTables(ArrayList<ArrayList<Character>> lines, ArrayList<Character> s, int index, Stack<Integer> stb) {
		if (stb.empty()) {
			alignments = new ArrayList<String>();
		}
		
        if (s.size() == 0) {
            return s;
        }
        
        if (s.get(0) != '|') {
            return s;
        }
        
        String result = "";
        ArrayList<String> rows = new ArrayList<String>();
        
        String th_or_td = "<td";
        String th_or_td_close = "</td>";
		boolean hasAlignment = false;
        
        ArrayList<Character> prev = (index - 1 >= 0) ? (new ArrayList<Character>(lines.get(index-1))) : (new ArrayList<Character>());
        ArrayList<Character> next = (index + 1 < lines.size()) ? (new ArrayList<Character>(lines.get(index+1))) : (new ArrayList<Character>());
        
        String entry = "";
        for (int i = 1; i < s.size(); i++) {
            if (s.get(i) == '|') {
                rows.add(entry);
                entry = "";
                continue;
            }
            entry = entry + s.get(i);
        }
        
        if (prev.size() == 0) {
            th_or_td = "<th";
            th_or_td_close = "</th>";
            result = result + "<table>";
            
            // CHECK VALIDITY OF HEADERS
            if (next.size() == 0) {
                return s;
            }
            int numRows = 0;
            int dashCounts = 0;
			boolean hasLeftColon = false;
			boolean hasRightColon = false;
			
			// padding to not make Java angry with IndexOutOfBoundsException stuff
			next.add((char)0);
			
            for (int n = 1; n < next.size() - 1; n++) {
                if (next.get(n) == '|') {
                    if (dashCounts < 3) {
                        return s;
                    }
                    dashCounts = 0;
					if (hasLeftColon && hasRightColon) {
						alignments.add(" style='text-align: center;'");
					} else if (hasLeftColon && !hasRightColon) {
						alignments.add(" style='text-align: left;'");
					} else if (!hasLeftColon && hasRightColon) {
						alignments.add(" style='text-align: right;'");
					}
					hasLeftColon = false;
					hasRightColon = false;
                    numRows++;
                    continue;
                }

				if (next.get(n) == ' ') {
					continue;
				}
				if (next.get(n) == ':') {
					boolean leftCheck = next.get(n+1) == '-';
					boolean rightCheck = next.get(n-1) == '-';
					if (!leftCheck && !rightCheck) {
						return s;
					}
					if (leftCheck) {
						hasLeftColon = true;
						continue;
					}
					if (rightCheck) {
						hasRightColon = true;
						continue;
					}
				}
                dashCounts++;
            }
            stb.push(1);

            lines.remove(index+1);
        }
        
        if (stb.empty()) {
            return s;
        }

        result = result + "<tr>";

		for (int m = 0; m < rows.size(); m++) {
			String dataTag = "";
			if (alignments.size() > 0) {
				dataTag = th_or_td + alignments.get(m) + ">";
			} else {
				dataTag = th_or_td + ">";
			}
			result = result + dataTag + rows.get(m) + th_or_td_close;
		}
        
        result = result + "</tr>";
        
        if (next.size() == 0) {
            result = result + "</table>";
            stb.pop();
        }
        
        ArrayList<Character> out = new ArrayList<Character>();
        char[] chrarr = result.toCharArray();
        for (char c : chrarr) {
            out.add(c);
        }
        return out;
    }

    private static boolean startsWithTag(ArrayList<Character> list) {
        String test = "";
        for (int i = 0; i < list.size(); i++) {
            test = test + list.get(i);
            if (blacklistedTags.contains(test)) {
                return true;
            }
        }
        return false;
    }
	
	private static boolean endsWithClosingTag(ArrayList<Character> list) {
		String test = "";
		for (int i = list.size() - 1; i >= 0; i--) {
			test = list.get(i) + test;
			if (blacklistedTagsClose.contains(test)) {
				return true;
			}
		}
		return false;
	}

    private static ArrayList<Character> processLists(ArrayList<ArrayList<Character>> lines, ArrayList<Character> s, int index, Stack<Integer> stack, boolean ordered, int IND) {
        
        //final int IND = 2; // Indentation
        
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
        
        int prevprevCount = countSpacesAtStart(prevprev) / IND;
        int prevCount = countSpacesAtStart(prev) / IND;
        int nextCount = countSpacesAtStart(next) / IND;
        int currCount = countSpacesAtStart(s) / IND;
        int nextnextCount = countSpacesAtStart(nextnext) / IND;
        
        boolean ppl = listable(prevprev, ordered);
        boolean pl = listable(prev, ordered);
        boolean cl = listable(s, ordered);
        boolean nl = listable(next, ordered);
        boolean nnl = listable(nextnext, ordered);
        
        if (!cl) {
            if (!stack.empty()) {
                return new ArrayList<Character>();
            }
            return s;
        }
        
        if (IND*currCount >= s.size()) {
            return s;
        }
        
        if (cl && !pl && (!ppl || (ppl && prevprevCount < currCount))) {
            arr.add(xl);
            stack.push(currCount);
        }
        
        
        if (prevCount < currCount && pl && cl) {
            arr.add(xl);
            stack.push(currCount);
        }

        if (!stack.empty() && cl) {
            arr.add("<li>");
        }
        
        boolean outContentForOrderedLists = false;
            
        for (int i = IND*currCount+1; i < s.size(); i++) {
            if (ordered) {
                if (s.get(i) == '.' && !outContentForOrderedLists) {
                    outContentForOrderedLists = true;
                    continue;
                }
                if (Character.isDigit(s.get(i)) && !outContentForOrderedLists) {
                    continue;
                }
            }
            arr.add(String.valueOf(s.get(i)));
        }
        
        if (!nl && nextCount == currCount+1) {
            arr.add("<p>");
            for (int i = IND*nextCount; i < next.size(); i++) {
                arr.add(String.valueOf(next.get(i)));
            }
            arr.add("</p>");
            
            if (!nnl) {
                lines.remove(index+1); // linear-time operation, but this is to address the case where there is text added below the last item of a list.
            } 
        }
        
        if (cl && ((nl && (nextCount <= currCount)) || (!nl && nnl && nextnextCount <= currCount))) {
            arr.add("</li>");
        }
        
        if (nl && nextCount < currCount) {
            for (int i = 0 ; i < currCount - nextCount; i++) {
                if (!stack.empty()) {
                    arr.add(xlc);
                    stack.pop();
                    arr.add("</li>");
                }
            }
        } 
        
        if (!nl && nextnextCount < currCount) {
            for (int i = 0 ; i < currCount - nextnextCount; i++) {
                if (!stack.empty()) {
                    arr.add(xlc);
                    stack.pop();
                    arr.add("</li>");
                }
            }
        }
        
        if (!nl && !nnl && !stack.empty()) {
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
    
    private static boolean listable(ArrayList<Character> line, boolean ordered) {
        int spaces = countSpacesAtStart(line);
        
        if (ordered) {
            if (spaces < line.size() && Character.isDigit(line.get(spaces))) {
                int j = spaces;
                while (j < line.size() && Character.isDigit(line.get(j))) {
                    j++;
                }
                if (j < line.size() && line.get(j) == '.') {
                    return true;
                }
            }
        } else {
            if (spaces < line.size() && line.get(spaces) == '-') {
                return true;
            }
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
                
                String str = "<a href=\""+url+"\">"+linkText+"</a>";
                 
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
                 
                String str = "<img src=\""+newUrl+"\" alt='"+linkText+"'>";
                
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
        
        out.add('<');
		out.add('/');
		out.add('h');
		out.add(hcchar);
		out.add('>');
        
        return out;
    }

    private static ArrayList<Character> processImages2(ArrayList<Character> in, Path currPath, Path root) {
        ArrayList<Character> arr = new ArrayList<Character>(in);
        ArrayList<Character> result = new ArrayList<Character>();
        
        if (in.size() < 6) {
            return in;
        }
        
        for (int i = 0; i < arr.size(); i++) {
            if (i > 0 && i+1 < arr.size() && arr.get(i) == '[' && arr.get(i+1) == '[' && arr.get(i-1) == '!' && arr.get(Math.max(i-2,0)) != '\\') {
                String imageName = ""; 
                String imageSize = "";
                boolean hasPipe = true;
                int j = i + 2;
                
                while (j < arr.size() && (arr.get(j) != '|')) {
                    if (arr.get(j) == ']' && j+1 < arr.size() && arr.get(j+1) == ']') { // PLEASE DO NOT PUT ']]' IN FILE NAMES THEN!
                        hasPipe = false;
                        j++;
                        break;
                    } 
					if (arr.get(j) == '\\') { // Just in case escaping might be inevitable.
						j++;
					}
                    imageName = imageName + arr.get(j);
                    j++;
                }
                
                if (hasPipe) {
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
                }
                
                result.remove(i-1);
                
                // Exploit how the file structure of the directory 
                // of converted notes is identical to that of the unconverted notes
				Path target = Find.findFile(root, imageName);
				
                String url = "";
                if (target != null) {
                    Path relative = (currPath.getParent()).relativize(target);
                    url = relative.toString();
                }

                char[] curl = url.toCharArray();
                String newUrl = "";
                for (char c : curl) {
                    if (c == '\\' || c == '_') {
                        newUrl = newUrl + '\\';
                    }
                    newUrl = newUrl + c;
                }
                
                String widthAttribute = "";
                if (hasPipe) {
                    widthAttribute = " width='" + imageSize + "'";
                }
                
                String str = "<img src='"+newUrl+"'" + widthAttribute + ">";
                
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
    
    
    private static ArrayList<Character> processLinks2(ArrayList<Character> in, Path currPath, Path root) {
        ArrayList<Character> arr = new ArrayList<Character>(in);
        ArrayList<Character> result = new ArrayList<Character>();
        
        if (in.size() < 5) {
            return in;
        }
        
        for (int i = 0; i < arr.size(); i++) {
            if (i+1 < arr.size() && arr.get(i) == '[' && arr.get(i+1) == '[' && arr.get(Math.max(i-1,0)) != '\\' && arr.get(Math.max(i-1,0)) != '!' ) {
                String fileName = ""; 
                String displayText = "";
                String section = "";
                boolean hasPipe = true;
                boolean addToSection = false;
                
                int j = i + 2;
                
                while (j < arr.size() && (arr.get(j) != '|')) {
                    if (arr.get(j) == ']' && j+1 < arr.size() && arr.get(j+1) == ']') { // PLEASE DO NOT PUT ']]' IN FILE NAMES THEN!
                        displayText = fileName;
                        hasPipe = false;
                        j++;
                        break;
                    } 
                    if (arr.get(j) == '#') {
                        addToSection = true;
                    }
                    if (addToSection) {
                        section = section + arr.get(j);
                    } else {
                        fileName = fileName + arr.get(j);
                    }
                    j++;
                }
 
                if (hasPipe) {
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
                }
				
				Path target = Find.findFile(root, fileName + ".md"); 
				
                String url22 = "";
                if (target != null) {
                    Path relative = (currPath.getParent()).relativize(target);
                    url22 = relative.toString();
                }
                
                char[] curl = url22.toCharArray();
                String newUrl = "";
                for (char c : curl) {
                    if (c == '\\' || c == '_') {
                        newUrl = newUrl + '\\';
                    }
                    newUrl = newUrl + c;
                }
                

                String str = "<a href=\""+newUrl.replace(".md", ".html")+section+"\">"+displayText+"</a>";
                
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
    
    private static ArrayList<Character> lineFormatting(ArrayList<Character> s1, Stack<String> sst) {
        ArrayList<Character> s = new ArrayList<>(s1);
        
        String res = "";
        
        // padding to not make Java angry with out-of-bounds exceptions
        s.add((char)0);
        s.add((char)0);
        s.add((char)0);
        
        for (int i = 0; i < s.size()-3; i++) {
            boolean noMath = !sst.contains("$") && !sst.contains("$$");
            boolean noCode = !sst.contains("`") && !sst.contains("```");
            boolean noMathNorCode = noMath && noCode;
            
            // Escape
            if (s.get(i) == '\\' && noMathNorCode) {
                res = res + s.get(i+1);
                i++;
                continue;
            } 

            // Bold and Italics
            if ((s.get(i) == '*' || s.get(i) == '_') && noMathNorCode) {
                char emph = s.get(i);
                int j = 0;
                String mod = "";
                
                while (j < 3 && s.get(i+j) == emph) {
                    mod = mod + s.get(i+j);
                    j++;
                }
                
                i += j - 1;
                
                if (sst.contains(mod)) {
                    sst.pop();
                    res = res + forms.get(mod)[1];
                } else {
                    sst.push(mod);
                    res = res + forms.get(mod)[0];
                }
                continue; 
            }

            // Code Blocks
            if (s.get(i) == '`' && noMath) {
                String mod = "`";
                int m = 0;
                if ((s.get(i+1) == '`') && (s.get(i+2) == '`')) {
                    m = 2;
                    mod = "```";
                }
                i += m; 
                
                if ((m == 0 && !sst.contains(8)) || m == 2) {
                    if (sst.contains(mod)) {
                        sst.pop();
                        res = res + forms.get(mod)[1];
                    } else {
                        sst.push(mod);
                        res = res + forms.get(mod)[0];
                    }
                    continue;
                }
            }

            // MathJax 
            if (s.get(i) == '$' && noCode) {
                String mod = "$";
                int m = 0; 
                if (s.get(i+1) == '$') {
                    m = 1;
                    mod = "$$";
                }
                
                i += m;
                
                if (sst.contains(mod)) {
                    sst.pop();
                    res = res + forms.get(mod)[1];
                } else {
                    sst.push(mod);
                    res = res + forms.get(mod)[0];
                }
                continue;
            } 

			if (s.get(i) == '<') {
				if (sst.contains("`") || sst.contains("```")) {
					res = res + "&lt;";
					continue;
				} else if (sst.contains("$") || sst.contains("$$")) {
					res = res + "\\lt ";
					continue;
				}
			}
			
			if (s.get(i) == '>') {
				if (sst.contains("`") || sst.contains("```")) {
					res = res + "&gt;";
					continue;
				} else if (sst.contains("$") || sst.contains("$$")) {
					res = res + "\\gt ";
					continue;
				}
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
