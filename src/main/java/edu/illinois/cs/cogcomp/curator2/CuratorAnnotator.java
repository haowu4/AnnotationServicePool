/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 * <p>
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.cs.illinois.edu/
 */
package edu.illinois.cs.cogcomp.curator2;

import edu.illinois.cs.cogcomp.annotation.AnnotatorException;
import edu.illinois.cs.cogcomp.annotation.Annotator;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import edu.illinois.cs.cogcomp.thrift.base.AnnotationFailedException;
import edu.illinois.cs.cogcomp.thrift.base.ServiceUnavailableException;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.thrift.TException;

import java.net.SocketException;

/**
 * A single annotator object, corresponding to a
 * {@link edu.illinois.cs.cogcomp.thrift.curator.Curator.Client}'s annotator. Multiple instances of
 * this class will be used as {@link Annotator}s in {@link CuratorAnnotatorService}.
 *
 * The {@link #viewName} and {@link #requiredViews} fields are defined in Curator's configuration
 * file ({@code dist/configs/annotators.xml}).
 *
 * @author Christos Christodoulopoulos
 */
public class CuratorAnnotator extends Annotator {
    private edu.illinois.cs.cogcomp.curator2.CuratorClient curatorClient;


    public CuratorAnnotator(edu.illinois.cs.cogcomp.curator2.CuratorClient curatorClient, String viewName, String[] requiredViews) {
        super(viewName, requiredViews);
        this.curatorClient = curatorClient;
    }


    /**
     * noop
     *
     * @param rm configuration parameters
     */
    @Override
    public void initialize(ResourceManager rm) {
    }

    @Override
    public void addView(TextAnnotation ta) throws AnnotatorException {
        try {
            ta.addView(viewName, curatorClient.getTextAnnotationView(ta, viewName));
        } catch (TException | AnnotationFailedException | SocketException
                | ServiceUnavailableException e) {
            e.printStackTrace();
            throw new AnnotatorException(ExceptionUtils.getStackTrace(e) + "|" +e.getMessage());
        }
    }

}
