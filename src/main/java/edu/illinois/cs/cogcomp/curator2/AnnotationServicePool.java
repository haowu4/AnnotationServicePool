package edu.illinois.cs.cogcomp.curator2;

import edu.illinois.cs.cogcomp.annotation.Annotator;
import edu.illinois.cs.cogcomp.annotation.AnnotatorException;
import edu.illinois.cs.cogcomp.annotation.AnnotatorService;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.nlp.tokenizer.Tokenizer;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

/**
 * Created by haowu4 on 6/27/17.
 */
public class AnnotationServicePool implements AnnotatorService {

  BlockingQueue<AnnotatorService> available;
  List<AnnotatorService> all;
  int maxConcurrentAllowed;

  public int getNumAvailable() {
    return available.size();
  }

  public int getSize() {
    return all.size() * maxConcurrentAllowed;
  }

  public AnnotationServicePool(List<AnnotatorService> services) {
    this(services, 1);
  }

  public AnnotationServicePool(List<AnnotatorService> services, int maxConcurrentAllowed) {
    all = services;
    this.available = new LinkedBlockingDeque<>();
    for (int i = 0; i < maxConcurrentAllowed; i++) {
      for (AnnotatorService as : services) {
        available.add(as);
      }
    }
    this.maxConcurrentAllowed = maxConcurrentAllowed;
  }

  public TextAnnotation createBasicTextAnnotation(String s, String s1, String s2)
      throws AnnotatorException {
    AnnotatorService as;
    try {
      as = available.poll(60, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      throw new AnnotatorException("Timeout: Failed to get an available annotation service.");
    }
    if (as == null) {
      throw new AnnotatorException("Timeout: Failed to get an available annotation service.");
    }
    TextAnnotation ta = null;
    try {
      ta = as.createBasicTextAnnotation(s, s1, s2);
    } catch (AnnotatorException e) {
      available.add(as);
      throw e;
    }

    available.add(as);
    return ta;
  }

  public TextAnnotation createBasicTextAnnotation(String s, String s1, String s2,
      Tokenizer.Tokenization tokenization) throws AnnotatorException {
    AnnotatorService as;
    try {
      as = available.poll(60, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      throw new AnnotatorException("Timeout: Failed to get an available annotation service.");
    }
    if (as == null) {
      throw new AnnotatorException("Timeout: Failed to get an available annotation service.");
    }
    TextAnnotation ta = null;
    try {
      ta = as.createBasicTextAnnotation(s, s1, s2, tokenization);
    } catch (AnnotatorException e) {
      available.add(as);
      throw e;
    }

    available.add(as);
    return ta;
  }

  public TextAnnotation createAnnotatedTextAnnotation(String s, String s1, String s2)
      throws AnnotatorException {
    AnnotatorService as;
    try {
      as = available.poll(60, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      throw new AnnotatorException("Timeout: Failed to get an available annotation service.");
    }
    if (as == null) {
      throw new AnnotatorException("Timeout: Failed to get an available annotation service.");
    }
    TextAnnotation ta = null;
    try {
      ta = as.createAnnotatedTextAnnotation(s, s1, s2);
    } catch (AnnotatorException e) {
      available.add(as);
      throw e;
    }

    available.add(as);
    return ta;
  }

  public TextAnnotation createAnnotatedTextAnnotation(String s, String s1, String s2,
      Tokenizer.Tokenization tokenization) throws AnnotatorException {
    AnnotatorService as;
    try {
      as = available.poll(60, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      throw new AnnotatorException("Timeout : Failed to get an available annotation service.");
    }
    if (as == null) {
      throw new AnnotatorException("Timeout : Failed to get an available annotation service.");
    }
    TextAnnotation ta = null;
    try {
      ta = as.createAnnotatedTextAnnotation(s, s1, s2, tokenization);
    } catch (AnnotatorException e) {
      available.add(as);
      throw e;
    }

    available.add(as);
    return ta;
  }

  public TextAnnotation createAnnotatedTextAnnotation(String s, String s1, String s2,
      Set<String> set) throws AnnotatorException {
    AnnotatorService as;
    try {
      as = available.poll(60, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      throw new AnnotatorException("Timeout: Failed to get an available annotation service.");
    }
    if (as == null) {
      throw new AnnotatorException("Timeout: Failed to get an available annotation service.");
    }
    TextAnnotation ta = null;
    try {
      ta = as.createAnnotatedTextAnnotation(s, s1, s2, set);
    } catch (AnnotatorException e) {
      available.add(as);
      throw e;
    }

    available.add(as);
    return ta;

  }

  public TextAnnotation createAnnotatedTextAnnotation(String s, String s1, String s2,
      Tokenizer.Tokenization tokenization, Set<String> set) throws AnnotatorException {
    AnnotatorService as;
    try {
      as = available.poll(60, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      throw new AnnotatorException("Timeout: Failed to get an available annotation service.");
    }
    if (as == null) {
      throw new AnnotatorException("Timeout: Failed to get an available annotation service.");
    }
    TextAnnotation ta = null;
    try {
      ta = as.createAnnotatedTextAnnotation(s, s1, s2, tokenization, set);
    } catch (AnnotatorException e) {
      available.add(as);
      throw e;
    }

    available.add(as);
    return ta;

  }

  public boolean addView(TextAnnotation textAnnotation, String s) throws AnnotatorException {
    AnnotatorService as;
    try {
      as = available.poll(60, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      throw new AnnotatorException("Timeout: Failed to get an available annotation service.");
    }
    if (as == null) {
      throw new AnnotatorException("Timeout: Failed to get an available annotation service.");
    }
    boolean ret = false;
    try {
      ret = as.addView(textAnnotation, s);
    } catch (AnnotatorException e) {
      available.add(as);
      throw e;
    }

    available.add(as);
    return ret;

  }

  public void addAnnotator(Annotator annotator) throws AnnotatorException {
    for (AnnotatorService as : all) {
      as.addAnnotator(annotator);
    }
  }

  public Set<String> getAvailableViews() {
    if (all.isEmpty()) {
      return new HashSet<>();
    }
    return all.get(0).getAvailableViews();
  }

  public TextAnnotation annotateTextAnnotation(TextAnnotation textAnnotation, boolean b)
      throws AnnotatorException {
    AnnotatorService as;
    try {
      as = available.poll(60, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      throw new AnnotatorException("Timeout: Failed to get an available annotation service.");
    }
    if (as == null) {
      throw new AnnotatorException("Timeout: Failed to get an available annotation service.");
    }

    TextAnnotation ta = null;
    try {
      ta = as.annotateTextAnnotation(textAnnotation, b);
    } catch (AnnotatorException e) {
      available.add(as);
      throw e;
    }

    available.add(as);
    return ta;
  }
}
