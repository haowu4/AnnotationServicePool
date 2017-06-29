package edu.illinois.cs.cogcomp.utils;

import edu.illinois.cs.cogcomp.annotation.AnnotatorServiceConfigurator;
import edu.illinois.cs.cogcomp.core.utilities.configuration.Configurator;
import java.util.Properties;

/**
 * Created by haowu4 on 6/28/17.
 */
public class LocalAnnotatorUtils {

  public static Properties getProp() {
    Properties props = new Properties();
    props.setProperty("usePos", Configurator.TRUE);
    props.setProperty("useLemma",
        Configurator.TRUE);
    props.setProperty("useShallowParse",
        Configurator.TRUE);

    props.setProperty("useNerConll",
        Configurator.TRUE);
    props.setProperty("useNerOntonotes",
        Configurator.TRUE);
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

    return props;
  }

}
