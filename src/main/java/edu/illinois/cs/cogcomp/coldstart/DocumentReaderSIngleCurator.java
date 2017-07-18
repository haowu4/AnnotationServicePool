package edu.illinois.cs.cogcomp.coldstart;

import edu.illinois.cs.cogcomp.annotation.AnnotatorException;
import edu.illinois.cs.cogcomp.annotation.AnnotatorService;
import edu.illinois.cs.cogcomp.annotation.AnnotatorServiceConfigurator;
import edu.illinois.cs.cogcomp.coldstart.Utils.AnnotationError;
import edu.illinois.cs.cogcomp.coldstart.Utils.AnnotationErrorHandler;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.utilities.SerializationHelper;
import edu.illinois.cs.cogcomp.core.utilities.configuration.Configurator;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import edu.illinois.cs.cogcomp.curator2.CuratorConfigurator;
import edu.illinois.cs.cogcomp.curator2.CuratorFactory;
import edu.illinois.cs.cogcomp.pipeline.main.PipelineFactory;
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
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static edu.illinois.cs.cogcomp.coldstart.DocumentReader.REQUIRED_VIEWS;

/**
 * Created by haowu4 on 6/27/17.
 */
public class DocumentReaderSIngleCurator {

  public static Properties getProp() {
    Properties props = new Properties();
    props.setProperty("usePos", Configurator.TRUE);
    props.setProperty("useLemma",
        Configurator.TRUE);
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

    return props;
  }

  public static List<AnnotatorService> getAnnotationService() throws IOException {
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
    return services;
  }

  public static class DiskDB {

    String baseFolder;

    public DiskDB(String baseFolder) {
      this.baseFolder = baseFolder;
    }

    public synchronized boolean containsKey(String k) {
      return false;
//      return new File(baseFolder, k.replace("/", ".") + ".bin").exists();
    }

    public synchronized void put(String key, byte[] d) {
      try {
        FileUtils.writeByteArrayToFile(new File(baseFolder, key.replace("/", ".") + ".bin"), d);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

  }

  public static void main(String[] args)
      throws IOException, InterruptedException, AnnotatorException {

    final AnnotatorService service = PipelineFactory.buildPipeline(new ResourceManager(getProp()));

//    new BasicAnnotatorService(new ResourceManager(getProp()));

    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH:mm:ss");

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
//      if (!id.startsWith("eng/nw/")) {
//        continue;
//      }
      String text = FileUtils.readFileToString(p.toFile());
      documents.add(new Document("", id, text));
      if (documents.size() == 300) {
        break;
      }
    }

    final int total = documents.size();

    final List<AnnotatorService> curators = getAnnotationService();

    List<Thread> threads = new ArrayList<>();

    ConcurrentArrayList<String> failedDocuments = new ConcurrentArrayList<>();

    AtomicInteger counter = new AtomicInteger();
    AtomicInteger failedCounter = new AtomicInteger();

    final Logger logger = Logger.getLogger("DocumentReader");

    FileHandler fileHandler = new FileHandler("log-" + dateFormat.format(new Date()) + ".xml");
    logger.addHandler(fileHandler);
    logger.setUseParentHandlers(false);

    ConcurrentArrayList<AnnotationError> errors = new ConcurrentArrayList<>();

    for (int i = 0; i < curators.size(); i++) {
      int finalI = i;

      AnnotationErrorHandler errorHandler = new AnnotationErrorHandler() {
        @Override
        public void handle(AnnotationError error) {
          logger.log(Level.WARNING, error.toString());
          errors.add(error);
        }
      };

      Thread t = new Thread(new Runnable() {
        @Override
        public void run() {
          AnnotatorService as = curators.get(finalI);
//                    DB db = DBMaker.fileDB("/home/haowu4/data/coldstart_results/result" + finalI + ".db").closeOnJvmShutdown().transactionEnable().make();
//                    ConcurrentMap<String, byte[]> map;
//                    map = db.hashMap("cache", Serializer.STRING, Serializer.BYTE_ARRAY).createOrOpen();
          DiskDB map = new DiskDB("/home/haowu4/data/coldstart_results");
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
                ta = service.createAnnotatedTextAnnotation("KBP_COLDSTART", d.getId(), d.getText());
                boolean noError =
                    Utils.AnnotateBySentence(ta, as, REQUIRED_VIEWS, errorHandler);
//                  Utils.Annotate(ta, as, REQUIRED_VIEWS, errorHandler);
                taDone = true;

                if (!noError) {
                  throw new AnnotatorException("At least one error when annotating.");
                }

              } catch (Exception e) {
//                System.out.println(e.getMessage());
//                System.out.println(e.getMessage());
//                e.printStackTrace();
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
                    -1,
                    -1));
          }

//                    db.commit();
//                    db.close();

        }
      });
      threads.add(t);
    }

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

//    List<String> failedLogs = new ArrayList<>();

//    Iterator<String> it = failedDocuments.iterator();

//    while (it.hasNext()) {
//      failedLogs.add(it.next());
//    }

//    Collections.sort(failedLogs);

//    FileUtils.writeLines(new File(String.format("log_%d", System.currentTimeMillis())), failedLogs);

    List<String> errorList = new ArrayList<>();

    Iterator<AnnotationError> ite = errors.iterator();
    while (ite.hasNext()) {
      errorList.add(ite.next().toJson());
    }

    FileUtils
        .writeLines(new File(String.format("failed_log_%s.jsons", dateFormat.format(new Date()))),
            errorList);


  }
}
