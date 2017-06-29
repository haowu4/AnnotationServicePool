package edu.illinois.cs.cogcomp.client;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import edu.illinois.cs.cogcomp.annotation.AnnotatorException;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.utilities.SerializationHelper;
import edu.illinois.cs.cogcomp.service.Document;
import edu.illinois.cs.cogcomp.service.message.AnnotationRequest;
import edu.illinois.cs.cogcomp.service.message.AnnotationResponse;
import edu.illinois.cs.cogcomp.utils.JsonUtils;
import org.apache.commons.codec.binary.Base64;

/**
 * Created by haowu4 on 6/29/17.
 */
public class AnnotatorClient {

  protected String hostname;
  protected int port;

  public TextAnnotation annotate(String corporaId, String docId, String text, String[] localView,
      String[] remoteView) throws Exception {
    AnnotationRequest req = new AnnotationRequest(new Document(corporaId, docId, text), localView,
        remoteView);
    HttpResponse<String> response = Unirest.post("http://httpbin.org/post")
        .body(JsonUtils.UGLY_GSON.toJson(req))
        .asString();
    if (response.getStatus() != 200) {
      throw new AnnotatorException(String.format("Status : %d", response.getStatus()));
    }
    String results = response.getBody();
    AnnotationResponse annotationResponse = JsonUtils.UGLY_GSON
        .fromJson(results, AnnotationResponse.class);
    byte[] blob = Base64.decodeBase64(annotationResponse.getAnnotation());

    TextAnnotation textAnnotation = SerializationHelper.deserializeTextAnnotationFromBytes(blob);
    return textAnnotation;
  }

  public static void main(String[] args) throws UnirestException {
  }
}
