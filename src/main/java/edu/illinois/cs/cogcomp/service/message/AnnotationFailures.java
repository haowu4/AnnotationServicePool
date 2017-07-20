package edu.illinois.cs.cogcomp.service.message;

import static edu.illinois.cs.cogcomp.utils.JsonUtils.UGLY_GSON;

import edu.illinois.cs.cogcomp.utils.JsonUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

/**
 * Created by haowu4 on 6/29/17.
 */
public class AnnotationFailures {

    String id;
    String view;
    int sentence;
    String error;
    String message;

    public AnnotationFailures(String id, String view, int sentence, Throwable e) {
        this.id = id;
        this.view = view;
        this.sentence = sentence;
        this.error = ExceptionUtils.getStackTrace(e);
        this.message = e.getMessage();
        if (this.message == null) {
            this.message = e.getLocalizedMessage();
        }

        if (this.message == null) {
            this.message = e.toString();
        }
    }


    @Override
    public String toString() {
        return JsonUtils.PRETTY_GSON.toJson(this);
    }

    public String toJson() {
        return UGLY_GSON.toJson(this);
    }

    public String getId() {
        return id;
    }

    public String getView() {
        return view;
    }

    public int getSentence() {
        return sentence;
    }

    public String getError() {
        return error;
    }

    public String getMessage() {
        return message;
    }
}
