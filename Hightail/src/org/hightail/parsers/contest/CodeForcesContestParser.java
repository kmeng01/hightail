package org.hightail.parsers.contest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.net.URL;
import java.net.URLConnection;
import org.hightail.parsers.task.CodeForcesTaskParser;
import org.hightail.parsers.task.TaskParser;
import org.htmlparser.Node;
import org.htmlparser.Parser;
import org.htmlparser.filters.LinkRegexFilter;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.util.ParserException;
import org.htmlparser.visitors.TagFindingVisitor;

import java.io.IOException;
import java.net.MalformedURLException;

public class CodeForcesContestParser implements ContestParser {

    final static private String taskUrlRegExp = "/(contest|gym)/(.*)/problem/(.*)";
    final static private TaskParser taskParser = new CodeForcesTaskParser();
        
    @Override
    public ArrayList<String> getProblemURLListFromURL(String myURL) throws ParserException, InterruptedException {
        
        myURL = myURL.trim();
        
        if (myURL.contains("contestRegistrants")) {
            // swap that to "contest"
            myURL = myURL.replaceFirst("contestRegistrants", "contest");
        }
        if (myURL.contains("contests")) {
            // swap that to "contest"
            myURL = myURL.replaceFirst("contests", "contest");
        }
        
        Parser parser;
        
        try {
            URL url = new URL(myURL);
            URLConnection con = url.openConnection();
            con.setRequestProperty("Cookie", "RCPC=2410eb50f427b51e1dab0380761323bf");
            
            parser = new Parser(con);
        } catch (MalformedURLException e) {
            throw new ParserException("Parsing failed, URL Connection input is malformed.");
        } catch (IOException e) {
            throw new ParserException("Parsing failed, URL Connection cannot be sustained.");
        }
        
        // get all <a> tags
        String[] tagsToVisit = {"a"};
        TagFindingVisitor visitor = new TagFindingVisitor(tagsToVisit);
        parser.visitAllNodesWith(visitor);
        Node[] aNodes = visitor.getTags(0);
        
        // filter link tags for those that link to problems
        // (we recognize that on the base of link itself, by using regexp)
        LinkRegexFilter filter = new LinkRegexFilter(taskUrlRegExp);
        ArrayList<String> links = new ArrayList<>();
        for (Node node: aNodes) {
            if (filter.accept(node)) {
                String linkUrl = ((LinkTag) node).extractLink();
                links.add(linkUrl);
            }
        }
        
        // remove link duplicates
        Set<String> s = new HashSet<>(links);
        links = new ArrayList<>(s);
        Collections.sort(links);
        
        System.out.println(links);
        
        return links;
    }

    @Override
    public boolean isCorrectURL(String URL) {
        return URL.contains("codeforces.");
    }
    
    @Override
    public TaskParser getTaskParser () {
        return taskParser;
    }
}
