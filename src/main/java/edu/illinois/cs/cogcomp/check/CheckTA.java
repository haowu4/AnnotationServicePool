package edu.illinois.cs.cogcomp.check;

import edu.illinois.cs.cogcomp.annotation.AnnotatorException;
import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.utilities.StringTransformation;
import edu.illinois.cs.cogcomp.service.CombinedAnnotatorService;
import edu.illinois.cs.cogcomp.service.Document;
import edu.illinois.cs.cogcomp.service.message.AnnotationFailures;
import edu.illinois.cs.cogcomp.service.message.AnnotationResponse;
import edu.illinois.cs.cogcomp.utils.AnnotationUtils;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import static edu.illinois.cs.cogcomp.utils.AnnotationUtils.getRemoteViews;
import static edu.illinois.cs.cogcomp.utils.JsonUtils.UGLY_GSON;

/**
 * Created by haowu4 on 7/20/17.
 */
public class CheckTA {
    public static boolean checkToken(TextAnnotation ta, int i) {
        IntPair offsets = ta.getTokenCharacterOffset(i);
        String t1 = ta.getToken(i);
        String t2 = ta.getText().substring(offsets.getFirst(), offsets.getSecond());
//        System.out.println(String.format("T:[%s]", t1));
//        System.out.println(String.format("O:[%s]", t2));
//        if (t1.length() == t2.length()) {
        if (t1.equals(t2)) {
            return true;
        } else {
            return false;
        }
    }

    public static void main(String[] args) throws Exception {
        String docname = "NYT_ENG_20131025.0160";

        String text = FileUtils.readFileToString(new File("/home/haowu4/annotation/coldstart_2017/need_to_annotate", docname));
        Document doc = new Document("test", "test", text);

        TextAnnotation preprocessed =
                CheckTaAndPreprocessingDoc
                        .loadTAFromMe(
                                "/home/haowu4/annotation/coldstart_2017_result/" + docname + ".json");

        System.out.println(preprocessed);

        for (int i = 0; i < preprocessed.getTokens().length; i++) {
            if (!checkToken(preprocessed, i)) {
                System.out.println(i);
                continue;
            }
        }




        StringTransformation transformation = new StringTransformation(doc.getText());
        AnnotationUtils.cleanUp(transformation);
        CombinedAnnotatorService combinedService = new CombinedAnnotatorService(
                AnnotationUtils.getLocalConfig(), AnnotationUtils.getRemoteConfig(),
                toSet(AnnotationUtils.getLocalViews()), toSet(getRemoteViews()));

        TextAnnotation textAnnotation = combinedService.getRemoteAnnotator()
                .createBasicTextAnnotation(doc.getCorpora(), doc
                        .getId(), transformation.getTransformedText());

        System.out.println(textAnnotation);

    }

    public static Set<String> toSet(String[] localViews) {
        HashSet<String> k = new HashSet<>();
        for (String x : localViews) {
            k.add(x);
        }
        return k;
    }
}
