package edu.illinois.cs.cogcomp.service.message;

import java.util.List;

/**
 * Created by haowu4 on 6/29/17.
 */
public class AnnotationResponse {

  String annotation;
  List<AnnotationFailures> failures;

  public AnnotationResponse(String annotation,
      List<AnnotationFailures> failures) {
    this.annotation = annotation;
    this.failures = failures;
  }

  public String getAnnotation() {
    return annotation;
  }

  public List<AnnotationFailures> getFailures() {
    return failures;
  }
}
