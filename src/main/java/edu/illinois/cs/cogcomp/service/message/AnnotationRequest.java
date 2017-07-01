package edu.illinois.cs.cogcomp.service.message;

import edu.illinois.cs.cogcomp.service.Document;

import java.util.List;

/**
 * Created by haowu4 on 6/29/17.
 */
public class AnnotationRequest {

    public static class AnnotationView {
        String viewName;
        boolean useCurator;
        boolean processAtSentenceLevel;

        public AnnotationView(String viewName, boolean useCurator, boolean processAtSentenceLevel) {
            this.viewName = viewName;
            this.useCurator = useCurator;
            this.processAtSentenceLevel = processAtSentenceLevel;
        }

        public AnnotationView(String viewName) {
            this(viewName, false, false);
        }


        public String getViewName() {
            return viewName;
        }

        public boolean isUseCurator() {
            return useCurator;
        }

        public boolean isProcessAtSentenceLevel() {
            return processAtSentenceLevel;
        }
    }

    Document document;
    List<AnnotationView> views;

    public AnnotationRequest(Document document, List<AnnotationView> views) {
        this.document = document;
        this.views = views;
    }

    public Document getDocument() {
        return document;
    }

    public List<AnnotationView> getViews() {
        return views;
    }
}
