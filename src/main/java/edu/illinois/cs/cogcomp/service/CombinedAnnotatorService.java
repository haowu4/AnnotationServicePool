package edu.illinois.cs.cogcomp.service;

import edu.illinois.cs.cogcomp.annotation.AnnotatorException;
import edu.illinois.cs.cogcomp.annotation.AnnotatorService;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotationUtilities;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import edu.illinois.cs.cogcomp.curator.CuratorFactory;
import edu.illinois.cs.cogcomp.pipeline.main.PipelineFactory;
import java.util.Set;

/**
 * Created by haowu4 on 6/29/17.
 */
public class CombinedAnnotatorService {

  AnnotatorService localAnnotator;
  AnnotatorService remoteAnnotator;

  Set<String> localViews;
  Set<String> remoteViews;

  public CombinedAnnotatorService(ResourceManager localConf, ResourceManager remoteConf,
      Set<String> localViews, Set<String> remoteViews)
      throws Exception {
    this.localAnnotator = PipelineFactory.buildPipeline(localConf);
    this.remoteAnnotator = CuratorFactory.buildCuratorClient(remoteConf);
    this.localViews = localViews;
    this.remoteViews = remoteViews;
  }

  public AnnotatorService getLocalAnnotator() {
    return localAnnotator;
  }

  public AnnotatorService getRemoteAnnotator() {
    return remoteAnnotator;
  }

  public void annotateDocumentBySentence(TextAnnotation textAnnotation, String[] viewNames,
      FailureObserver observer) {
    for (int sentenceId = 0; sentenceId < textAnnotation.sentences().size(); ++sentenceId) {
      TextAnnotation sentTa =
          TextAnnotationUtilities.getSubTextAnnotation(textAnnotation, sentenceId);
      for (String viewName : viewNames) {
        try {
          annotateTextAnnotation(sentTa, viewName);
          int start = textAnnotation.getSentence(sentenceId).getStartSpan();
          int end = textAnnotation.getSentence(sentenceId).getEndSpan();
          TextAnnotationUtilities.copyViewFromTo(viewName, sentTa,
              textAnnotation, start, end, start);
        } catch (Exception e) {
          e.printStackTrace();
          observer.observe(
              textAnnotation, sentenceId, viewName, e);
        }
      }
    }
  }

  public void annotateDocument(TextAnnotation textAnnotation, String[] viewNames,
      FailureObserver observer) {
    for (String viewName : viewNames) {
      try {
        annotateTextAnnotation(textAnnotation, viewName);
      } catch (Exception e) {
        e.printStackTrace();
        observer.observe(
            textAnnotation, -1, viewName, e);
      }
    }
  }

  private void annotateTextAnnotation(TextAnnotation ta, String view) throws AnnotatorException {
    if (localViews.contains(view)) {
      localAnnotator.addView(ta, view);
    } else {
      if (remoteViews.contains(view)) {
        remoteAnnotator.addView(ta, view);
      } else {
        throw new AnnotatorException(
            String.format("Annotator for view [%s] cannot be found.", view));
      }
    }
  }

}
