package edu.illinois.cs.cogcomp.coldstart;

import edu.illinois.cs.cogcomp.annotation.AnnotatorService;
import edu.illinois.cs.cogcomp.annotation.AnnotatorServiceConfigurator;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.utilities.SerializationHelper;
import edu.illinois.cs.cogcomp.core.utilities.configuration.Configurator;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import edu.illinois.cs.cogcomp.curator2.AnnotationServicePool;
import edu.illinois.cs.cogcomp.curator2.CuratorConfigurator;
import edu.illinois.cs.cogcomp.curator2.CuratorFactory;
import edu.illinois.cs.cogcomp.service.Document;
import org.apache.commons.io.FileUtils;
import org.h2.mvstore.ConcurrentArrayList;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Created by haowu4 on 6/27/17.
 */
public class DocumentReader {


  public static final String[] REQUIRED_VIEWS = new String[]{
      ViewNames.SRL_NOM,
      ViewNames.SRL_VERB,
  };

  public static final String[] LOCAL_VIEWS = new String[]{
      ViewNames.LEMMA,
      ViewNames.POS,
      ViewNames.SHALLOW_PARSE,
      ViewNames.NER_CONLL,
      ViewNames.DEPENDENCY_STANFORD,
      ViewNames.PARSE_STANFORD,
  };


  public static Properties getProp() {
    Properties props = new Properties();
    props.setProperty("usePos", Configurator.TRUE);
    props.setProperty("useLemma",
        Configurator.TRUE);
    props.setProperty("useShallowParse",
        Configurator.TRUE);

    props.setProperty("useNerConll",
        Configurator.FALSE);
    props.setProperty("useNerOntonotes",
        Configurator.TRUE);
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

    return props;
  }

  public static AnnotationServicePool getAnnotationService() throws IOException {
    List<AnnotatorService> services = new ArrayList<>();
    List<String> ips = FileUtils.readLines(new File("conf/service_list.txt"));
    for (String ip : ips) {
      Properties properties = getProp();
      String hostname = ip;
      String port = "9010";
      if (ip.contains(":")) {
        String[] parts = ip.split(":");
        hostname = parts[0];
        port = parts[1];
      }
      properties.setProperty(CuratorConfigurator.CURATOR_HOST.key, hostname);
      properties.setProperty(CuratorConfigurator.CURATOR_PORT.key, port);
      properties.setProperty(CuratorConfigurator.RESPECT_TOKENIZATION.key, Configurator.TRUE);
      properties.setProperty(CuratorConfigurator.CURATOR_FORCE_UPDATE.key, Configurator.TRUE);

      ResourceManager rm = new ResourceManager(properties);
      try {
        AnnotatorService as = CuratorFactory.buildCuratorClient(rm);
        services.add(as);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    return new AnnotationServicePool(services);
  }

  public static class DiskDB {

    String baseFolder;

    public DiskDB(String baseFolder) {
      this.baseFolder = baseFolder;
    }

    public synchronized boolean containsKey(String k) {
      return new File(baseFolder, k + ".bin").exists();
    }

    public synchronized void put(String key, byte[] d) {
      try {
        FileUtils.writeByteArrayToFile(new File(baseFolder, key + ".bin"), d);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

  }

  public static void main(String[] args) throws IOException, InterruptedException {
//        DB db = DBMaker.fileDB("/home/haowu4/data/coldstart_results/result.db").closeOnJvmShutdown().transactionEnable().make();
//        ConcurrentMap<String, byte[]> map;
//        map = db.hashMap("cache", Serializer.STRING, Serializer.BYTE_ARRAY).createOrOpen();

    DiskDB map = new DiskDB("/home/haowu4/data/coldstart_results");

    final String BASE = "/home/haowu4/data/codestart/need_to_annotate/";

    List<Path> docPaths = Files.walk(Paths.get(BASE))
        .filter(Files::isRegularFile)
        .collect(Collectors.toList());

    System.out.println(docPaths.size() + " documents found.");

    Collections.shuffle(docPaths);

    BlockingQueue<Document> documents = new LinkedBlockingDeque<>();

    for (Path p : docPaths) {
      String id = p.toString().replaceFirst(BASE, "");
      String text = FileUtils.readFileToString(p.toFile());
      documents.add(new Document("", id, text));
//            if (documents.size() == 100) {
//                break;
//            }
    }

    final int total = documents.size();

    final AnnotationServicePool as = getAnnotationService();

    List<Thread> threads = new ArrayList<>();

    ConcurrentArrayList<String> failedDocuments = new ConcurrentArrayList<>();

    AtomicInteger counter = new AtomicInteger();
    AtomicInteger failedCounter = new AtomicInteger();

    for (int i = 0; i < as.getSize(); i++) {
      Thread t = new Thread(new Runnable() {
        @Override
        public void run() {
          while (!documents.isEmpty()) {
            Document d = null;
            TextAnnotation ta = null;
            try {
              d = documents.take();
            } catch (InterruptedException e) {
              e.printStackTrace();
            }

            boolean taDone = false;

            if (!map.containsKey(d.getId())) {
              try {
                ta = as.createBasicTextAnnotation("KBP_COLDSTART", d.getId(), d.getText());
//                                try {
                as.addView(ta, ViewNames.LEMMA);
//                                } catch (AnnotatorException e) {
//                                    failedDocuments.add(d.id + "#_" + ViewNames.LEMMA + "#__" + e.getMessage());
//                                    failedCounter.incrementAndGet();
//
//                                }
//                                try {
                as.addView(ta, ViewNames.POS);
//                                } catch (AnnotatorException e) {
//                                    failedDocuments.add(d.id + "#_" + ViewNames.POS + "#__" + e.getMessage());
//                                    failedCounter.incrementAndGet();
//
//                                }
//                                try {
                as.addView(ta, ViewNames.SHALLOW_PARSE);
//                                } catch (AnnotatorException e) {
//                                    failedDocuments.add(d.id + "#_" + ViewNames.SHALLOW_PARSE + "#__" + e.getMessage());
//                                    failedCounter.incrementAndGet();
//
//                                }
//                                try {
                as.addView(ta, ViewNames.NER_CONLL);
//                                } catch (AnnotatorException e) {
//                                    failedDocuments.add(d.id + "#_" + ViewNames.NER_CONLL + "#__" + e.getMessage());
//                                    failedCounter.incrementAndGet();
//
//                                }

                //                                try {
                as.addView(ta, ViewNames.SRL_NOM);
//                                } catch (AnnotatorException e) {
//                                    failedDocuments.add(d.id + "#_" + ViewNames.SRL_NOM + "#__" + e.getMessage());
//                                    failedCounter.incrementAndGet();
//
//                                }

                //                                try {
                as.addView(ta, ViewNames.DEPENDENCY_STANFORD);
//                                } catch (AnnotatorException e) {
//                                    failedDocuments.add(d.id + "#_" + ViewNames.DEPENDENCY_STANFORD + "#__" + e.getMessage());
//                                    failedCounter.incrementAndGet();
//
//                                }

//                                try {
//                                as.addView(ta, ViewNames.PARSE_STANFORD);
//                                } catch (AnnotatorException e) {
//                                    failedDocuments.add(d.id + "#_" + ViewNames.PARSE_STANFORD + "#__" + e.getMessage());
//                                    failedCounter.incrementAndGet();
//
//                                }
//                                try {
//                                as.addView(ta, ViewNames.SRL_VERB);
//                                } catch (AnnotatorException e) {
//                                    failedDocuments.add(d.id + "#_" + ViewNames.SRL_VERB + "#__" + e.getMessage());
//                                    failedCounter.incrementAndGet();
//
//                                }
                taDone = true;
              } catch (Exception e) {
                e.printStackTrace();
                failedDocuments.add(d.getId());
                failedCounter.incrementAndGet();
              }

              if (taDone) {
                byte[] blob = null;
                try {
                  blob = SerializationHelper.serializeTextAnnotationToBytes(ta);
                } catch (IOException e) {
                  e.printStackTrace();
                  failedDocuments.add(d.getId());
                }
                if (blob != null) {
                  map.put(d.getId(), blob);
//                                    db.commit();
                }
              }
            }

            int progress = counter.incrementAndGet();
            DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            Date date = new Date();

            System.out.print(
                String.format("%s Processed %d document, failed %d, remains %d. Open Conn %d/%d \r",
                    dateFormat.format(date),
                    progress,
                    failedCounter.get(),
                    (total - progress),
                    as.getNumAvailable(),
                    as.getSize()));
          }

        }
      });
      threads.add(t);
    }

    DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    Date date = new Date();

    System.out.println(dateFormat.format(date));
    System.out.println("Starting " + threads.size() + " threads..");

    for (Thread t : threads) {
      t.start();
    }

    for (Thread t : threads) {
      t.join();
    }

    System.out.println("Finishing " + threads.size() + " threads..");
    System.out.println(dateFormat.format(new Date()));
//        System.out.println("Starting " + threads.size() + " threads..");

    List<String> failedLogs = new ArrayList<>();

    Iterator<String> it = failedDocuments.iterator();

    while (it.hasNext()) {
      failedLogs.add(it.next());
    }

    Collections.sort(failedLogs);

    FileUtils.writeLines(new File(String.format("log_%d", System.currentTimeMillis())), failedLogs);
//
//        db.commit();
//        db.close();

  }
}
