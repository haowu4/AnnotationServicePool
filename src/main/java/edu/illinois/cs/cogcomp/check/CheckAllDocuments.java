package edu.illinois.cs.cogcomp.check;

import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.service.Document;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import static edu.illinois.cs.cogcomp.check.CheckTaAndPreprocessingDoc.loadTAFromMe;

/**
 * Created by haowu4 on 7/21/17.
 */
public class CheckAllDocuments {
    public static void main(String[] args) throws IOException {

        final String BASE = "/home/haowu4/annotation/fix_tokenization/";

        List<Path> docPaths = Files.walk(Paths.get(BASE))
                .filter(Files::isRegularFile)
                .collect(Collectors.toList());


        Set<String> disagree = new HashSet<>();
        int count = 0;
        for (Path p : docPaths) {
            System.out.print(count + "/" + docPaths.size());
            String id = p.toString().replaceFirst(BASE, "");
            if (id.startsWith("/")) {
                id = id.substring(1);
            }
            TextAnnotation ta = loadTAFromMe(p.toAbsolutePath().toString());
            for (int i = 0; i < ta.getTokens().length; i++) {
                IntPair offsets = ta.getTokenCharacterOffset(i);
                String t1 = ta.getToken(i);
                String t2 = ta.getText().substring(offsets.getFirst(), offsets.getSecond());
                if (!t1.equals(t2)) {
                    System.out.println(ta.getId());
                    disagree.add(t1);
                    System.out.println(disagree.size());
//                    break;
                }
            }

            System.out.print("\r");
            count++;
        }


    }
}
