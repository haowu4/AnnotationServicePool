package edu.illinois.cs.cogcomp.coldstart;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import edu.illinois.cs.cogcomp.annotation.AnnotatorService;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotationUtilities;
import edu.illinois.cs.cogcomp.service.Document;

/**
 * Created by haowu4 on 6/27/17.
 */
public class Utils {

  public static Gson pretty_gson = new GsonBuilder().setPrettyPrinting().create();
  public static Gson ugly_gson = new GsonBuilder().create();

  public static class AnnotationError {

    String id;
    String view;
    int sentence;
    int totalSentence;
    String text;
    String error;

    public AnnotationError(String id, String view, int sentence, int totalSentence, String text,
        Exception e) {
      this.id = id;
      this.view = view;
      this.sentence = sentence;
      this.totalSentence = totalSentence;
      this.text = text;
      this.error = e.toString();
    }


    @Override
    public String toString() {
      return pretty_gson.toJson(this);
    }

    public String toJson() {
      return ugly_gson.toJson(this);
    }
  }

  public interface AnnotationErrorHandler {

    void handle(AnnotationError error);
  }


  public static TextAnnotation createTextAnnotation(Document document, AnnotatorService local,
      AnnotatorService remote) {
    return null;
  }

  public static boolean AnnotateBySentence(TextAnnotation textAnnotation,
      AnnotatorService annotatorServices, String[] viewNames, AnnotationErrorHandler errorHandler) {
    boolean noError = true;
    for (int sentenceId = 0; sentenceId < textAnnotation.sentences().size(); ++sentenceId) {
      TextAnnotation sentTa =
          TextAnnotationUtilities.getSubTextAnnotation(textAnnotation, sentenceId);
      for (String viewName : viewNames) {
        try {
          annotatorServices.addView(sentTa, viewName);
          int start = textAnnotation.getSentence(sentenceId).getStartSpan();
          int end = textAnnotation.getSentence(sentenceId).getEndSpan();
          TextAnnotationUtilities.copyViewFromTo(viewName, sentTa,
              textAnnotation, start, end, start);
        } catch (Exception e) {
          e.printStackTrace();
          errorHandler.handle(
              new AnnotationError(textAnnotation.getId(), viewName, sentenceId,
                  textAnnotation.sentences().size(), sentTa.text, e));
          noError = false;
        }
      }
    }

//    TextAnnotationUtilities.mapTransformedTextAnnotationToSource(ta, st);

    return noError;
  }


  public static boolean Annotate(TextAnnotation textAnnotation,
      AnnotatorService annotatorServices, String[] viewNames, AnnotationErrorHandler errorHandler) {
    boolean noError = true;
    for (String viewName : viewNames) {
      try {
        annotatorServices.addView(textAnnotation, viewName);
      } catch (Exception e) {
        e.printStackTrace();
        errorHandler.handle(new AnnotationError(textAnnotation.getId(), viewName, -1,
            textAnnotation.sentences().size(),
            textAnnotation.text, e));
        noError = false;
      }
    }
    return noError;
  }
}
