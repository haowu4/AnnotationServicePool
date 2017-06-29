package edu.illinois.cs.cogcomp.service;

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;

/**
 * Created by haowu4 on 6/29/17.
 */
public interface FailureObserver {
  void observe(TextAnnotation textAnnotation, int sentence, String viewName, Throwable err);
}
