package edu.illinois.cs.cogcomp.coldstart;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by haowu4 on 7/4/17.
 */
public class ColdStartReaderCheck {

    public static String readDoc(File f) throws IOException {
        String content = FileUtils.readFileToString(f);
        String contentWithoutEnter = content.replaceAll("\n", " ");
        String contentRemovingTags = contentWithoutEnter;
        while (contentRemovingTags.contains("<")) {
            int p = contentRemovingTags.indexOf('<');
            int q = contentRemovingTags.indexOf('>');
            contentRemovingTags = contentRemovingTags.substring(0, p)
                    + contentRemovingTags.substring(q + 1, contentRemovingTags.length());
        }

        return contentRemovingTags.trim();
    }

    public static void main(String[] args) throws IOException {
        String lefts = "/home/haowu4/annotation/coldstart_2017/eng/nw";
        String rights = "/home/haowu4/annotation/coldstart_2017/need_to_annotate/";


        List<Path> docPaths = Files.walk(Paths.get(lefts))
                .filter(Files::isRegularFile)
                .collect(Collectors.toList());


        List<Path> targetPaths = Files.walk(Paths.get(rights))
                .filter(Files::isRegularFile)
                .collect(Collectors.toList());

        Map<String, String> leftDocs = new HashMap<>();

        for (Path p : docPaths) {
            String c = readDoc(p.toFile());
            String key = p.getFileName().toString().replace(".xml", "");
            leftDocs.put(key, c);
        }

        for (Path p : targetPaths) {
            String c = FileUtils.readFileToString(p.toFile());
            String key = p.getFileName().toString();
            String oc = leftDocs.get(key);
            if (!c.equals(oc)) {
                System.out.println(key);
            }
        }


    }
}
