//import java.nio.file.Files;
//import java.nio.file.Paths;
//import java.nio.file.Path;

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
        Stack<Integer> st = new Stack<>(); // FOR FORMATTING
        Stack<Integer> sbq = new Stack<>(); // FOR BLOCK QUOTE PROCESSING
        boolean formatable = st.contains(5) || st.contains(6) || st.contains(7) || st.contains(8);
        
        // Conversion magic
        
        for (int j = 0; j < lines.size(); j++) {
            ArrayList<Character> s = new ArrayList<>(lines.get(j)); 

            if (s.size() == 0) {
                converts.add("");
                continue;    
            } 
            
            
			boolean notFormattable = st.contains(5) || st.contains(6) || st.contains(7) || st.contains(8);  // no headers inside math or code!

            if (!notFormattable) { // BEGIN FORMATTABLE BLOCK

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
                if (threeOrMoreLineCharacters(s)) {
                    converts.add("<hr>");
                    continue;
                }
                
                // links
                s = processLinks(s);
                s = processLinks2(s);
                
        
                // process blockquotes
                if (s.size() >= 1 && s.get(0) == '>') {
                    s = processBlockQuotes(lines, s, j, sbq);
                }

            } // END OF FORMATTABLE BLOCK
    
            // formatting
            s = lineFormatting(s,st);


            if (!notFormattable) { // BEGIN FORMATTABLE BLOCK
            
                // images
                s = processImages(s); 
                s = processImages2(s); 

                

                // add line breaks
                
                if (shouldInsertBreak(s, j, lines, st)) {
                    s.add('<');s.add('b');s.add('r');s.add('>');
                }

                s = processParas(lines, s, j);


            } // END FORMATTABLE BLOCK


            
            
            String rrr = "";
            for (char c : s) {
                rrr = rrr + c;
            }

            converts.add(rrr);
        }

        return converts;
    }

    private static ArrayList<Character> processParas(ArrayList<ArrayList<Character>> lines, ArrayList<Character> s, int index) {
        ArrayList<Character> result = new ArrayList<Character>();
        
         if (s.size() == 0) {
            return s;
        }


        String[] tags = {
            "$$", "<h1>", "<h2>", "<h3>", "<h4>", "<h5>", "<h6>",
            "<hr>", "<blockquote>", "<!--", "<div>", ">", "<pre>"
        }; 
        String[] tagsClose = {
            "$$", "</h1>", "</h2>", "</h3>", "</h4>", "</h5>", "</h6>",
            "</hr>", "</blockquote>", "-->", "</div>", "</pre>"
        }; 
        HashSet<String> blacklistedTags = new HashSet<String>(Arrays.asList(tags));
        HashSet<String> blacklistedTagsClose = new HashSet<String>(Arrays.asList(tagsClose));
        
        boolean addPtag = true;
        String strTest = "";
        for (int i = 0; i < s.size(); i++) {
            if (blacklistedTags.contains(strTest)) {
                addPtag = false;
                break;
            }
            strTest = strTest + s.get(i);
        }

        if (addPtag) {
            ArrayList<Character> prev = new ArrayList<Character>();
            if (index - 1 >= 0) {
                prev = lines.get(index - 1);
            }    
            if (prev.size() == 0) {
                // <p>
                result.add('<');result.add('p');result.add('>');
            }

        }

        for (char chr : s) {
            result.add(chr);
        }

        boolean addPtagClose = true;
        String strTest2 = "";
        for (int ii = s.size() - 1; ii >= 0; ii--) {
            if (blacklistedTagsClose.contains(strTest2)) {
                addPtagClose = false;
                break;
            }
            strTest2 = s.get(ii) + strTest2;
        }

        if (addPtagClose) {
            ArrayList<Character> next = new ArrayList<Character>();
            if (index + 1 < lines.size()) {
               next = lines.get(index + 1);
            }

            if (next.size() == 0) {
                // </p>
                result.add('<');result.add('/');result.add('p');result.add('>');
            }
        }
        return result;
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
            result = result + s.get(j);
        }
        return result;
    }
    
    private static boolean shouldInsertBreak(ArrayList<Character> s, int j, ArrayList<ArrayList<Character>> lines, Stack<Integer> st) {
        
        String res = "";
        for (char c : s) {
            res = res + c;
        }
        
        if (j >= lines.size() - 1) {
            return false;
        }

        ArrayList<Character> nextLine = lines.get(j + 1);

        if (nextLine.size() == 0) {
            return false;
        }
        
        // No breaks before, after, and within MathJax math and code blocks

        if (st.contains(5) || st.contains(6) || st.contains(8)) {
            return false;
        }

        if (res.endsWith("$$")) {
            return false;
        }

        if (nextLine.size() >= 2 && nextLine.get(0) == '$' && nextLine.get(1) == '$') {
            return false;
        }
        
        if (res.endsWith("</code></pre>")) {
            return false;
        }
        
        // No breaks before and after block quotes
        if (res.endsWith("</blockquote>")) {
            return false;
        }
        
        if (lenofgr(lines.get(j)) < lenofgr(nextLine)) {
            return false;
        }
		
		// no breaks after headers
		if (res.endsWith("</h1>") || res.endsWith("</h2>") || res.endsWith("</h3>") || res.endsWith("</h4>") || res.endsWith("</h5>") || res.endsWith("</h6>")) {
            return false;
        }

        return true;
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
            {"", ""},                           // 0
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
            if (i+1 < arr.size() && arr.get(i) == '[' && arr.get(i+1) == '[' && arr.get(Math.max(i-1,0)) != '\\') {
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

                while (j < arr.size() && arr.get(j) != ']') {
                    displayText = displayText + arr.get(j);
                    j++;
                }
				
				j++;
                
                
                if (j >= arr.size()) {
                    result.add(arr.get(i));
                    continue;
                }
                
                result.remove(i-1);
				/*
				String query = "";
				char[] cquery = fileName.toCharArray();
				for (char c : cquery) {
					if (c == '\\') {
						continue;
					}
					query = query + c;
				}*/
				
				String url = Find.find("./notes", fileName);
				
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
