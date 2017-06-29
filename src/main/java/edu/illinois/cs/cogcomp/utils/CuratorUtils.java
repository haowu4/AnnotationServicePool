package edu.illinois.cs.cogcomp.utils;

import edu.illinois.cs.cogcomp.annotation.AnnotatorServiceConfigurator;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.utilities.configuration.Configurator;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import edu.illinois.cs.cogcomp.curator.CuratorConfigurator;
import java.io.File;
import java.io.IOException;
import java.util.Properties;
import org.apache.commons.io.FileUtils;

/**
 * Created by haowu4 on 6/28/17.
 */
public class CuratorUtils {

  public static ResourceManager getLocalConfig() {
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
    props.setProperty(CuratorConfigurator.CURATOR_FORCE_UPDATE.key, Configurator.TRUE);

    return new ResourceManager(props);
  }

  public static String[] getLocalViews() {
    return new String[]{
        ViewNames.NER_CONLL,
        ViewNames.NER_ONTONOTES,
        ViewNames.DEPENDENCY_STANFORD,
        ViewNames.PARSE_STANFORD,
        ViewNames.LEMMA,
        ViewNames.POS,
        ViewNames.SHALLOW_PARSE,
    };
  }

  public static String[] getRemoteViews() {
    return new String[]{ViewNames.SRL_NOM, ViewNames.SRL_VERB};
  }
}
