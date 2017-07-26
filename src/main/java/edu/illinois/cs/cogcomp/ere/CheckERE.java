package edu.illinois.cs.cogcomp.ere;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.io.FileUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
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

        boolean inMention;
        int curRecoredLength;
        StringBuilder stringBuilder = null;


        public void startElement(String uri, String localName, String qName,
                                 Attributes attributes) throws SAXException {
            if (qName.equalsIgnoreCase("entity_mention")) {
                curRecoredLength = Integer.parseInt(attributes.getValue("length"));
            }

            if (qName.equalsIgnoreCase("mention_text")) {
                inMention = true;
                stringBuilder = new StringBuilder();
            }
        }

        public void endElement(String uri, String localName,
                               String qName) throws SAXException {

            if (qName.equalsIgnoreCase("entity_mention")) {
                curRecoredLength = -1;
            }

            if (qName.equalsIgnoreCase("mention_text")) {
                inMention = false;
                String mention = stringBuilder.toString();
                int length = mention.length();
                stringBuilder = null;
                if (length != curRecoredLength) {
                    if (curRecoredLength == -1) {
                        throw new IllegalStateException("enetity_mention does not have mention_text node inside..");
                    } else {
                        wrongLengthMentionCount.incrementAndGet();
                        fileIsFaulty = true;
                    }
                }
            }

        }

        public void characters(char ch[], int start, int length) throws SAXException {

            if (inMention) {
                stringBuilder.append(new String(ch, start, length));
            }
        }

    }

    public static void main(String[] args) throws IOException, ParserConfigurationException, SAXException {
        String BASE = args[0];
        List<Path> docPaths = Files.walk(Paths.get(BASE))
                .filter(Files::isRegularFile)
                .collect(Collectors.toList());

        System.out.println(docPaths.size() + " documents found.");

        List<String> fs = new ArrayList<>();

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
                fs.add(fileName);
            }
            System.out.print("\r");
            counter++;

            if (!fs.isEmpty()) {
                if (fs.size() % 100 == 0)
                    FileUtils.writeLines(new File("ere_err_files.txt"), fs);
            }

        }
        System.out.println("Results: ");
        System.out.printf("%d/%d, %d files has the problem \n", counter, docPaths.size(), wrongLengthFileCount.get());


    }


}
