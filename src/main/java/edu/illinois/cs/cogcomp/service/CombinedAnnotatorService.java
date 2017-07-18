package edu.illinois.cs.cogcomp.service;

import edu.illinois.cs.cogcomp.annotation.AnnotatorService;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotationUtilities;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import edu.illinois.cs.cogcomp.curator2.CuratorFactory;
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

    public void annotateDocumentBySentence(TextAnnotation textAnnotation,
                                           String viewName, AnnotatorService service,
                                           FailureObserver observer) {
        for (int sentenceId = 0; sentenceId < textAnnotation.sentences().size(); ++sentenceId) {
            TextAnnotation sentTa =
                    TextAnnotationUtilities.getSubTextAnnotation(textAnnotation, sentenceId);
            try {
                service.addView(sentTa, viewName);
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

    public void annotateDocument(TextAnnotation textAnnotation, String viewName, AnnotatorService service,
                                 FailureObserver observer) {
        try {
            service.addView(textAnnotation, viewName);
        } catch (Exception e) {
            e.printStackTrace();
            observer.observe(
                    textAnnotation, -1, viewName, e);
        }
    }

    public void annotateOneView(TextAnnotation textAnnotation, String viewName, boolean useCurator, boolean bySentence, FailureObserver observer) {
        AnnotatorService service = useCurator ? remoteAnnotator : localAnnotator;
        if (bySentence) {
            annotateDocumentBySentence(textAnnotation, viewName, service, observer);
        } else {
            annotateDocument(textAnnotation, viewName, service, observer);
        }
    }

}
