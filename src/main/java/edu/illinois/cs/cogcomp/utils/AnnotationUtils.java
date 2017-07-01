package edu.illinois.cs.cogcomp.utils;

import edu.illinois.cs.cogcomp.annotation.AnnotatorServiceConfigurator;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.utilities.StringTransformation;
import edu.illinois.cs.cogcomp.core.utilities.StringTransformationCleanup;
import edu.illinois.cs.cogcomp.core.utilities.configuration.Configurator;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import edu.illinois.cs.cogcomp.curator.CuratorConfigurator;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;

/**
 * Created by haowu4 on 6/28/17.
 */
public class AnnotationUtils {

    public static ResourceManager getLocalConfig() {
        Properties props = new Properties();
        props.setProperty("usePos", Configurator.TRUE);
        props.setProperty("useLemma",
                Configurator.FALSE);
        props.setProperty("useShallowParse",
                Configurator.FALSE);
        props.setProperty("useNerConll",
                Configurator.FALSE);
        props.setProperty("useNerOntonotes",
                Configurator.FALSE);
        props.setProperty("useStanfordParse",
                Configurator.TRUE);
        props.setProperty("useStanfordDep",
                Configurator.TRUE);

        props.setProperty("useSrlVerb",
                Configurator.FALSE);
        props.setProperty("useSrlNom",
                Configurator.FALSE);
        props.setProperty(
                "throwExceptionOnFailedLengthCheck",
                Configurator.FALSE);
        props.setProperty(
                "useJson",
                Configurator.FALSE);
        props.setProperty(
                "isLazilyInitialized",
                Configurator.FALSE);
//        props.setProperty(
//                PipelineConfigurator.USE_SRL_INTERNAL_PREPROCESSOR.key,
//                Configurator.FALSE);

        props.setProperty(AnnotatorServiceConfigurator.DISABLE_CACHE.key,
                Configurator.TRUE);
        props.setProperty(AnnotatorServiceConfigurator.CACHE_DIR.key,
                "/tmp/aswdtgffasdfasd");
        props.setProperty(
                AnnotatorServiceConfigurator.THROW_EXCEPTION_IF_NOT_CACHED.key,
                Configurator.FALSE);
        props.setProperty(
                AnnotatorServiceConfigurator.FORCE_CACHE_UPDATE.key,
                Configurator.TRUE);
        return new ResourceManager(props);
    }

    public static ResourceManager getRemoteConfig() {
        Properties props = new Properties();
        props.setProperty("usePos", Configurator.FALSE);
        props.setProperty("useLemma",
                Configurator.FALSE);
        props.setProperty("useShallowParse",
                Configurator.FALSE);
        props.setProperty("useNerConll",
                Configurator.FALSE);
        props.setProperty("useNerOntonotes",
                Configurator.FALSE);
        props.setProperty("useStanfordParse",
                Configurator.FALSE);
        props.setProperty("useStanfordDep",
                Configurator.FALSE);

        props.setProperty("useSrlVerb",
                Configurator.FALSE);
        props.setProperty("useSrlNom",
                Configurator.FALSE);
        props.setProperty(
                "throwExceptionOnFailedLengthCheck",
                Configurator.FALSE);
        props.setProperty(
                "useJson",
                Configurator.FALSE);
        props.setProperty(
                "isLazilyInitialized",
                Configurator.FALSE);
//        props.setProperty(
//                PipelineConfigurator.USE_SRL_INTERNAL_PREPROCESSOR.key,
//                Configurator.FALSE);

        props.setProperty(AnnotatorServiceConfigurator.DISABLE_CACHE.key,
                Configurator.TRUE);
        props.setProperty(AnnotatorServiceConfigurator.CACHE_DIR.key,
                "/tmp/aswdtgffasdfasd");
        props.setProperty(
                AnnotatorServiceConfigurator.THROW_EXCEPTION_IF_NOT_CACHED.key,
                Configurator.FALSE);
        props.setProperty(
                AnnotatorServiceConfigurator.FORCE_CACHE_UPDATE.key,
                Configurator.TRUE);

        String hostname = null;
        try {
            hostname = FileUtils.readFileToString(new File("conf/hostname"));
        } catch (IOException e) {
            hostname = "127.0.0.1";
        }

        String port = "9010";

        if (hostname.contains(":")) {
            port = hostname.split(":")[1];
            hostname = hostname.split(":")[0];
        }

        props.setProperty(CuratorConfigurator.CURATOR_HOST.key, hostname);
        props.setProperty(CuratorConfigurator.CURATOR_PORT.key, port);
        props.setProperty(CuratorConfigurator.RESPECT_TOKENIZATION.key, Configurator.TRUE);
        props.setProperty(CuratorConfigurator.CURATOR_FORCE_UPDATE.key, Configurator.FALSE);

        return new ResourceManager(props);
    }

    public static String[] getLocalViews() {
        return new String[]{
                ViewNames.DEPENDENCY_STANFORD,
                ViewNames.PARSE_STANFORD,
        };
    }

    public static String[] getRemoteViews() {
        return new String[]{
                ViewNames.SRL_NOM,
                ViewNames.SRL_VERB,
                ViewNames.NER_CONLL,
                ViewNames.LEMMA,
                ViewNames.POS,
                ViewNames.SHALLOW_PARSE,
                ViewNames.NER_ONTONOTES,};
    }

    public static void cleanUpByPattern(StringTransformation transformation, String pattern,
                                        Function<String, String> mapper) {

        Pattern p = Pattern.compile(pattern);
        Matcher matcher = p.matcher(transformation.getOrigText());

        while (matcher.find()) {
            int start = matcher.start();
            int end = matcher.end();
            String oldText = transformation.getOrigText().substring(start, end);
//      System.err.println(String.format("%d-%d [%s]", start , end, oldText));
            transformation.transformString(start, end, mapper.apply(oldText));
//      System.out
//          .println(matcher.group() + ":" + "start =" + matcher.start() + " end = " + matcher.end());
        }

    }

    public static void cleanUp(StringTransformation transformation) {
//    StringTransformationCleanup.normalizeToLatin1(transformation);
        StringTransformationCleanup.removeDiacritics(transformation);
        // unescape XML elements.

        cleanUpByPattern(transformation, "&quot;", old -> "\"");
        cleanUpByPattern(transformation, "&apos;", old -> "'");
        cleanUpByPattern(transformation, "&#39;", old -> "'");
        cleanUpByPattern(transformation, "&lt;", old -> "<");
        cleanUpByPattern(transformation, "&gt;", old -> ">");
        cleanUpByPattern(transformation, "&amp;", old -> "&");
        cleanUpByPattern(transformation, "\\p{IsHan}+", old -> "John");

        cleanUpByPattern(transformation, "\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\s+", old -> "");
        cleanUpByPattern(transformation, "\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\s\\d{2}:\\s\\d{2}\\s+",
                old -> "");


    }

    public static void main(String[] args) throws IOException {
        String all = FileUtils.readFileToString(new File("/tmp/errors.txt"));
        String[] examples = all.split("------");
        Set<String> uniqueExamples = new HashSet<>();
        uniqueExamples.addAll(Arrays.asList(examples));
        for (String example : uniqueExamples) {
            System.out.println("--------------------");
            System.out.println(example);
            StringTransformation transformation = new StringTransformation(example);
            cleanUp(transformation);
            System.out.println("                ");
            System.out.println(transformation.getTransformedText());
            System.out.println("====================");
        }
    }

}
