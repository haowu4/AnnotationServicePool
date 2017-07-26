package edu.illinois.cs.cogcomp.ere;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.io.FileUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;


/**
 * Created by haowu4 on 7/26/17.
 */
public class CheckERE {

    public static final AtomicInteger wrongLengthMentionCount = new AtomicInteger();
    public static final AtomicInteger wrongLengthFileCount = new AtomicInteger();

    public static class CheckEREHandler extends DefaultHandler {

        boolean fileIsFaulty = false;

        public void startElement(String uri, String localName, String qName,
                                 Attributes attributes) throws SAXException {
            if (qName.equalsIgnoreCase("entity_mention")) {
                String mention = attributes.getValue("mention_text");
                int length = mention.length();
                int recoredLength = Integer.parseInt(attributes.getValue("offset"));
                if (length != recoredLength) {
                    fileIsFaulty = true;
                    wrongLengthMentionCount.incrementAndGet();
                }
            }
        }
    }

    public static void main(String[] args) throws IOException, ParserConfigurationException, SAXException {
        String BASE = args[0];
        List<Path> docPaths = Files.walk(Paths.get(BASE))
                .filter(Files::isRegularFile)
                .collect(Collectors.toList());

        System.out.println(docPaths.size() + " documents found.");

        int counter = 0;
        for (Path p : docPaths) {
            System.out.printf("%d/%d, %d files has the problem", counter, docPaths.size(), wrongLengthFileCount.get());
            String fileName = p.toAbsolutePath().toString();
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();

            CheckEREHandler handler = new CheckEREHandler();
            saxParser.parse(fileName, handler);

            if (handler.fileIsFaulty) {
                wrongLengthFileCount.incrementAndGet();
            }
            System.out.print("\r");
            counter++;
        }
    }


}
