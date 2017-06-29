package edu.illinois.cs.cogcomp.service;

import com.sun.xml.internal.messaging.saaj.util.ByteInputStream;

/**
 * Created by haowu4 on 6/27/17.
 */
public class Document {

  String corpora;
  String id;
  String text;

  public Document(String corpora, String id, String text) {
    this.corpora = corpora;
    this.id = id;
    this.text = text;
  }

  public String getCorpora() {
    return corpora;
  }

  public String getId() {
    return id;
  }

  public String getText() {
    return text;
  }
}
