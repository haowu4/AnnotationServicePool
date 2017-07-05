package edu.illinois.cs.cogcomp.client;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.service.Document;
import edu.illinois.cs.cogcomp.service.message.AnnotationRequest;
import edu.illinois.cs.cogcomp.service.message.AnnotationResponse;
import edu.illinois.cs.cogcomp.utils.JsonUtils;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static edu.illinois.cs.cogcomp.client.AnnotatorClient.getViews;

/**
 * Created by haowu4 on 7/1/17.
 */
public class PoolOfClientSelectiveAnnotation {

    private static class Args {

        // This is a file that in the following formats:
        // |absolute_path of json dump\tview1 view2 view3 ...
        @Parameter(names = {"-in"}, description = "Input folder")
        String inputList = "/home/haowu4/data/coldstart/need_to_annotate_2nd.lst";


        @Parameter(names = {"-out"}, description = "Output folder")
        String outputFolder = "/home/haowu4/data/coldstart/results/";

        @Parameter(names = {"-shuffle"}, description = "Random order")
        boolean shuffleOrder = false;
    }


    public static List<AnnotatorClient> getAllClients() throws IOException {
        List<String> lines = FileUtils.readLines(new File("conf/ips.txt"));

        return lines.stream().map(x -> new AnnotatorClient(x, 4567)).collect(Collectors.toList());
    }

    public static void main(String[] argv) throws IOException, InterruptedException {

        Args args = new Args();
        JCommander.newBuilder()
                .addObject(args)
                .build()
                .parse(argv);

        if (1 == 1)
            throw new RuntimeException("");


        final String OUTPUT_BASE = args.outputFolder;

        Map<String, List<String>> requestMap = new HashMap<>();
        Map<String, TextAnnotation> existingAnnotations = new HashMap<>();


        BlockingDeque<Document> documents = new LinkedBlockingDeque<>();

//        for (Path p : docPaths) {
//            String id = p.toString().replaceFirst(BASE, "");
//            String text = FileUtils.readFileToString(p.toFile());
//            documents.add(new Document("data", id, text));
//        }

        final int docSize = documents.size();

        List<AnnotatorClient> clients = getAllClients();

        Thread[] threads = new Thread[clients.size()];

        AtomicInteger numOfFailure = new AtomicInteger();
        AtomicInteger finished = new AtomicInteger();

        Thread reporter = new Thread(new Runnable() {
            @Override
            public void run() {
                while (!documents.isEmpty()) {
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    System.out.print(String.format("Status: %d/%d finished. Average failure sentence-view count %d\r", finished.get(), docSize, numOfFailure.get()));
                }
            }
        });

        System.out.println(threads.length + " annotator running..");

        reporter.start();


        for (int i = 0; i < threads.length; i++) {
            int finalI = i;
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    AnnotatorClient client = clients.get(finalI);
                    while (!documents.isEmpty()) {
                        try {
                            Document d = documents.take();
                            System.err.println(String.format("Annotation doc %s", d.getId()));
                            AnnotationResponse response = client
                                    .annotate(d.getCorpora(), d.getId(), d.getText(),
                                            requestMap.get(d.getId())
                                                    .stream()
                                                    .map(x -> new AnnotationRequest.AnnotationView(x, true, true))
                                                    .collect(Collectors.toList()));
                            for (int j = 0; j < response.getFailures().size(); j++) {
                                numOfFailure.incrementAndGet();
                            }
                            String result = JsonUtils.UGLY_GSON.toJson(response);
                            FileUtils.writeStringToFile(new File(OUTPUT_BASE, d.getId().replace("/", ".") + ".json"), result);
                            finished.incrementAndGet();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
            threads[i] = t;
            t.start();
        }

        for (Thread t : threads) {
            t.join();
        }

        System.out.println("Finished..");


    }
}
