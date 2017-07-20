package edu.illinois.cs.cogcomp.check;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.utilities.SerializationHelper;
import edu.illinois.cs.cogcomp.core.utilities.StringTransformation;
import edu.illinois.cs.cogcomp.service.Document;
import edu.illinois.cs.cogcomp.service.message.AnnotationResponse;
import edu.illinois.cs.cogcomp.utils.JsonUtils;
import org.apache.commons.io.FileUtils;

import javax.xml.soap.Text;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import static edu.illinois.cs.cogcomp.coldstart.CheckResults.getTextAnnotationFromResponse;
import static edu.illinois.cs.cogcomp.core.utilities.SerializationHelper.deserializeTextAnnotationFromBytes;

/**
 * Created by haowu4 on 7/20/17.
 */
public class CheckTaAndPreprocessingDoc {

    private static class Args {
        @Parameter(names = {"-left"}, description = "Preprocessed document")
        String inputFolder = "/home/haowu4/annotation/coldstart_2017_result/";

        @Parameter(names = {"-right"}, description = "Event output")
        String outputFolder = "/home/haowu4/annotation/event_out/deft_2017_eng/";

        @Override
        public String toString() {
            return "Args{" +
                    "inputFolder='" + inputFolder + '\'' +
                    ", outputFolder='" + outputFolder + '\'' +
                    '}';
        }
    }


    public static boolean isTheSame(TextAnnotation left, TextAnnotation right) {
        return Arrays.equals(left.getTokens(), right.getTokens());
    }

    public static TextAnnotation loadTAFromPeng(String filePath) throws Exception {
        ObjectInputStream oos =
                new ObjectInputStream(new FileInputStream(filePath));
        TextAnnotation st = (TextAnnotation) oos.readObject();
        return st;

    }

    public static TextAnnotation loadTAFromMe(String filePath) throws IOException {
        String content = FileUtils.readFileToString(new File(filePath));
        AnnotationResponse response = JsonUtils.UGLY_GSON.fromJson(content, AnnotationResponse.class);
        TextAnnotation r = getTextAnnotationFromResponse(response);
        return r;
    }

    public static void main(String[] argv) throws Exception {

        Args args = new Args();
        JCommander.newBuilder()
                .addObject(args)
                .build()
                .parse(argv);

        System.out.println(args);


        final String BASE = args.inputFolder;
        final String OUTPUT_BASE = args.outputFolder;

        List<Path> docPaths = Files.walk(Paths.get(OUTPUT_BASE))
                .filter(Files::isRegularFile)
                .collect(Collectors.toList());


        Map<String, TextAnnotation> documents = new HashMap<>();

        for (Path p : docPaths) {
            System.out.print(documents.size() + "/" + docPaths.size());
            String id = p.toString().replaceFirst(BASE, "");
            if (id.startsWith("/")) {
                id = id.substring(1);
            }
            documents.put(id, loadTAFromPeng(p.toAbsolutePath().toString()));
            System.out.print("\r");
        }
        System.out.println("---------------------");
        docPaths = Files.walk(Paths.get(BASE))
                .filter(Files::isRegularFile)
                .collect(Collectors.toList());

        int i = 0;
        int checked = 0;

        for (Path p : docPaths) {
            System.out.print(documents.size() + "/" + checked + "/" + docPaths.size());

            String id = p.toString().replaceFirst(BASE, "");
            if (id.startsWith("/")) {
                id = id.substring(1);
            }

            if (documents.containsKey(id)) {

                TextAnnotation ta = documents.get(id);
                TextAnnotation r = loadTAFromMe(p.toAbsolutePath().toString());
                if (isTheSame(ta, r)) {
                    continue;
                } else {
                    System.out.println(id);
                    System.out.println(id);
                }
                System.out.print("\r");
                documents.remove(id);
            }
            i++;

        }
    }
}
