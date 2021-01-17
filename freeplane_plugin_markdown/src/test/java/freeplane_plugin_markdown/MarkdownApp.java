package freeplane_plugin_markdown;

import java.awt.Dimension;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Locale;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.WindowConstants;

import io.github.gitbucket.markedj.Marked;

public class MarkdownApp {
    public static void main(String args[]) throws IOException, URISyntaxException  
    {  
        JFrame f= new JFrame("Label Example");  
        String markdown = new String(Files.readAllBytes(Paths.get(MarkdownApp.class.getResource("/example.md").toURI())));
        String html = "<html>" + Marked.marked(markdown);
        JLabel htmlView =new JLabel(html);  
        JScrollPane s = new JScrollPane(htmlView);
        s.setPreferredSize(new Dimension(800, 800));
        f.getContentPane().add(s);  
        f.pack();
        f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        f.setVisible(true);  
    }  
}  