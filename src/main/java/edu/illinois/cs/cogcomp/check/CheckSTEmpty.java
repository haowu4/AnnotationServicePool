package edu.illinois.cs.cogcomp.check;

import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotationUtilities;
import edu.illinois.cs.cogcomp.core.utilities.StringTransformation;
import edu.illinois.cs.cogcomp.core.utilities.StringTransformationCleanup;
import edu.illinois.cs.cogcomp.service.CombinedAnnotatorService;
import edu.illinois.cs.cogcomp.utils.AnnotationUtils;
import edu.illinois.cs.cogcomp.utils.MyStringTransformationCleanup;

import java.util.HashSet;
import java.util.Set;

import static edu.illinois.cs.cogcomp.utils.AnnotationUtils.getRemoteViews;

/**
 * Created by haowu4 on 7/20/17.
 */
public class CheckSTEmpty {
    public static void checkToken(TextAnnotation ta) {
        System.out.println("------------------------------------");
        System.out.println(ta.text);
        for (int i = 0; i < ta.getTokens().length; i++) {
            IntPair offsets = ta.getTokenCharacterOffset(i);
            String t1 = ta.getToken(i);
            String t2 = ta.getText().substring(offsets.getFirst(), offsets.getSecond());
            System.out.println(String.format("%d-[%s]-[%s]", i, t1, t2));

        }
    }


    public static void main(String[] args) throws Exception {
        StringTransformation transformation = new StringTransformation("hushèd Casket");

//        StringTransformationCleanup.normalizeToAscii(transformation);
        MyStringTransformationCleanup.normalizeToAscii(transformation);

        CombinedAnnotatorService combinedService = new CombinedAnnotatorService(
                AnnotationUtils.getLocalConfig(), AnnotationUtils.getRemoteConfig(),
                toSet(AnnotationUtils.getLocalViews()), toSet(getRemoteViews()));

        TextAnnotation textAnnotation = combinedService.getRemoteAnnotator()
                .createBasicTextAnnotation("test", "test", transformation.getTransformedText());
        checkToken(textAnnotation);
        TextAnnotation original = TextAnnotationUtilities
                .mapTransformedTextAnnotationToSource(textAnnotation, transformation);
        checkToken(original);
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
