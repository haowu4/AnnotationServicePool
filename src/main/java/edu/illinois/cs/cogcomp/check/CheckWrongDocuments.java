package edu.illinois.cs.cogcomp.check;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.service.message.AnnotationResponse;
import edu.illinois.cs.cogcomp.utils.JsonUtils;
import edu.illinois.cs.cogcomp.utils.MyStringTransformationCleanup;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import static edu.illinois.cs.cogcomp.check.CheckTaAndPreprocessingDoc.loadTAFromMe;
import static edu.illinois.cs.cogcomp.coldstart.CheckResults.getTextAnnotationFromResponse;
import static edu.illinois.cs.cogcomp.utils.MyStringTransformationCleanup.normalizeCharacter;

/**
 * Created by haowu4 on 7/20/17.
 */
public class CheckWrongDocuments {

    private static class Args {
        @Parameter(names = {"-left"}, description = "Preprocessed document")
        String inputFolder = "/home/haowu4/annotation/coldstart_spa_2017/annotated";

        @Parameter(names = {"-right"}, description = "output file")
        String outputFile = "/home/haowu4/annotation/coldstart_2017_spa_results_good.txt";

        @Override
        public String toString() {
            return "Args{" +
                    "inputFolder='" + inputFolder + '\'' +
                    ", outputFile='" + outputFile + '\'' +
                    '}';
        }
    }


    public static Set<Character> isAffected(TextAnnotation ta, Charset encoding) {
        String originalString = ta.text;
        Set<Character> ret = new HashSet<>();
        String startStr = originalString;

        CharsetEncoder encoder = encoding.newEncoder();

        if (!encoder.canEncode(startStr)) {
            final int length = startStr.length();

            int charNum = 0;

            for (int offset = 0; offset < length; ) {
                // do something with the codepoint
                MyStringTransformationCleanup.BooleanCharPair replacement =
                        normalizeCharacter(startStr, encoding, offset);

                char replacedChar = replacement.getSecond();

                if (0 != replacedChar) {
                    charNum++;
                } else {
                    // This will cause problem.
                    ret.add(originalString.charAt(offset));
                }

                offset += Character.charCount(replacedChar);
            }
        }

        return ret;
    }

    public static void main(String[] argv) throws Exception {

        Args args = new Args();
        JCommander.newBuilder()
                .addObject(args)
                .build()
                .parse(argv);

        System.out.println(args);


        final String BASE = args.inputFolder;

        List<Path> docPaths = Files.walk(Paths.get(BASE))
                .filter(Files::isRegularFile)
                .collect(Collectors.toList());


        System.out.println(docPaths.size());
        Set<Character> affected = new HashSet<>();
        int goodDocuments = 0;
        int allDocument = 0;
        List<String> filds = new ArrayList<>();
        for (Path p : docPaths) {
            System.out.print(goodDocuments + "/" + allDocument);
            String id = p.toString().replaceFirst(BASE, "");
            if (id.startsWith("/")) {
                id = id.substring(1);
            }

            TextAnnotation ta = loadTAFromMe(p.toAbsolutePath().toString());
            Set<Character> latin1 = isAffected(ta, Charset.forName("ISO-8859-1"));
            Set<Character> ascii = isAffected(ta, Charset.forName("ascii"));
            affected.addAll(latin1);
            affected.addAll(ascii);
            if (latin1.isEmpty() && ascii.isEmpty()) {
                goodDocuments++;
                filds.add(id);
            }
            System.out.print("\r");
            allDocument++;
        }

        System.out.println(StringUtils.join(affected, "\n"));
        FileUtils.writeLines(new File(args.outputFile), filds);

    }
}
