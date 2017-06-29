package edu.illinois.cs.cogcomp.service.message;

import edu.illinois.cs.cogcomp.service.Document;

/**
 * Created by haowu4 on 6/29/17.
 */
public class AnnotationRequest {

  Document document;

  private String[] viewsShouldAnnotateAtDocLevel;
  private String[] viewsShouldAnnotateAtSentLevel;

  public AnnotationRequest(Document document, String[] viewsShouldAnnotateAtDocLevel,
      String[] viewsShouldAnnotateAtSentLevel) {
    this.document = document;
    this.viewsShouldAnnotateAtDocLevel = viewsShouldAnnotateAtDocLevel;
    this.viewsShouldAnnotateAtSentLevel = viewsShouldAnnotateAtSentLevel;
  }

  public AnnotationRequest(Document document, String[] views) {
    this(document, views, new String[0]);
  }

  public Document getDocument() {
    return document;
  }

  public String[] getViewsShouldAnnotateAtDocLevel() {
    return viewsShouldAnnotateAtDocLevel;
  }

  public String[] getViewsShouldAnnotateAtSentLevel() {
    return viewsShouldAnnotateAtSentLevel;
  }
}
