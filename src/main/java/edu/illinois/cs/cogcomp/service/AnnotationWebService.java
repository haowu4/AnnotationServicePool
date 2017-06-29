package edu.illinois.cs.cogcomp.service;

import static edu.illinois.cs.cogcomp.utils.CuratorUtils.getRemoteViews;
import static edu.illinois.cs.cogcomp.utils.JsonUtils.UGLY_GSON;
import static spark.Spark.get;
import static spark.Spark.init;

import edu.illinois.cs.cogcomp.annotation.AnnotatorException;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotationUtilities;
import edu.illinois.cs.cogcomp.core.utilities.SerializationHelper;
import edu.illinois.cs.cogcomp.core.utilities.StringTransformation;
import edu.illinois.cs.cogcomp.core.utilities.StringTransformationCleanup;
import edu.illinois.cs.cogcomp.service.message.AnnotationFailures;
import edu.illinois.cs.cogcomp.service.message.AnnotationRequest;
import edu.illinois.cs.cogcomp.service.message.AnnotationResponse;
import edu.illinois.cs.cogcomp.utils.CuratorUtils;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.codec.binary.Base64;
import spark.Request;
import spark.Response;

/**
 * Created by haowu4 on 6/28/17.
 */
public class AnnotationWebService {

  public static Set<String> toSet(String[] ss) {
    Set<String> ret = new HashSet<>();
    Collections.addAll(ret, ss);
    return ret;
  }

  public static CombinedAnnotatorService combinedService;

  public static void initService() throws Exception {
    combinedService = new CombinedAnnotatorService(
        CuratorUtils.getLocalConfig(), CuratorUtils.getRemoteConfig(),
        toSet(CuratorUtils.getLocalViews()), toSet(getRemoteViews()));
  }

  public static String annotate(AnnotationRequest request, Response response) {
    Document doc = request.getDocument();
    TextAnnotation textAnnotation;
    final List<AnnotationFailures> failures = new ArrayList<>();

    StringTransformation transformation = new StringTransformation(doc.getText());
    CuratorUtils.cleanUp(transformation);

    try {
      textAnnotation = combinedService.getLocalAnnotator()
          .createBasicTextAnnotation(doc.getCorpora(), doc
              .getId(), transformation.getTransformedText());
    } catch (AnnotatorException e) {
      e.printStackTrace();
      failures.add(new AnnotationFailures(doc.getId(), "createBasicTextAnnotation", -1, e));
      response.status(500);
      AnnotationResponse annotationResponse = new AnnotationResponse(
          "", failures);
      return UGLY_GSON.toJson(annotationResponse);
    }

    FailureObserver observer = (ta, sentence, viewName, err) -> {
      failures.add(new AnnotationFailures(ta.getId(), viewName, sentence, err));
    };

    combinedService.annotateDocument(textAnnotation,
        request.getViewsShouldAnnotateAtDocLevel(),
        observer);

    combinedService.annotateDocumentBySentence(textAnnotation,
        request.getViewsShouldAnnotateAtSentLevel(),
        observer);

    TextAnnotation original = TextAnnotationUtilities
        .mapTransformedTextAnnotationToSource(textAnnotation, transformation);

    byte[] blob = null;
    try {
      blob = SerializationHelper.serializeTextAnnotationToBytes(original);
    } catch (IOException e) {
      e.printStackTrace();
      failures.add(new AnnotationFailures(doc.getId(), "Serialization", -1, e));
      response.status(500);
      AnnotationResponse annotationResponse = new AnnotationResponse(
          "", failures);
      return UGLY_GSON.toJson(annotationResponse);
    }

    AnnotationResponse annotationResponse = new AnnotationResponse(
        Base64.encodeBase64String(blob), failures);

    return UGLY_GSON.toJson(annotationResponse);
  }


  public static String annotate(Request request, Response response) {
    String body = request.body();
    return annotate(UGLY_GSON.fromJson(body, AnnotationRequest.class), response);
  }

  public static void main(String[] args) throws Exception {
    initService();
    init();
    get("/annotate", AnnotationWebService::annotate);
  }


}
