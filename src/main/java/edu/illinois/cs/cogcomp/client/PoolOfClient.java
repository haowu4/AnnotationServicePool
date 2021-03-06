package edu.illinois.cs.cogcomp.client;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import edu.illinois.cs.cogcomp.service.Document;
import edu.illinois.cs.cogcomp.service.message.AnnotationFailures;
import edu.illinois.cs.cogcomp.service.message.AnnotationResponse;
import edu.illinois.cs.cogcomp.utils.JsonUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jetty.util.ConcurrentHashSet;
import org.h2.mvstore.ConcurrentArrayList;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static edu.illinois.cs.cogcomp.client.AnnotatorClient.getViews;

/**
 * Created by haowu4 on 7/1/17.
 */
public class PoolOfClient {

    private static class Args {
        @Parameter(names = {"-in"}, description = "Input folder")
        String inputFolder = "/home/haowu4/annotation/coldstart_2017/need_to_annotate_df/";

        @Parameter(names = {"-out"}, description = "Output folder")
        String outputFolder = "/home/haowu4/annotation/coldstart_2017_result/";

        @Parameter(names = {"-shuffle"}, description = "Random order")
        boolean shuffleOrder = false;

        @Parameter(names = {"-exclude_list"}, description = "Random order")
        String exclude = "";

        @Override
        public String toString() {
            return "Args{" +
                    "inputFolder='" + inputFolder + '\'' +
                    ", outputFolder='" + outputFolder + '\'' +
                    ", exclude='" + exclude + '\'' +
                    ", shuffleOrder=" + shuffleOrder +
                    '}';
        }
    }


    public static List<AnnotatorClient> getAllClients() throws IOException {
        List<String> lines = FileUtils.readLines(new File("conf/ips.txt"));

        return lines.stream().map(x -> new AnnotatorClient(x, 4567)).collect(Collectors.toList());
    }

    public static void main(String[] argv) throws IOException, InterruptedException {

//        LogWriter log = new LogWriter();

        Args args = new Args();
        JCommander.newBuilder()
                .addObject(args)
                .build()
                .parse(argv);

        System.out.println(args);


        final String BASE = args.inputFolder;
        final String OUTPUT_BASE = args.outputFolder;

        List<Path> docPaths = Files.walk(Paths.get(BASE))
                .filter(Files::isRegularFile)
                .collect(Collectors.toList());

        System.out.println(docPaths.size() + " documents found.");
        Set<String> avoids = new HashSet<>();
        if (!args.exclude.isEmpty()) {
            List<String> lines = FileUtils.readLines(new File(args.exclude));
            for (String line : lines) {
                avoids.add(line.trim().replace(".json", ""));
            }
        }


        if (args.shuffleOrder) {
            Collections.shuffle(docPaths);
        }

        BlockingDeque<Document> documents = new LinkedBlockingDeque<>();

        for (Path p : docPaths) {
            String id = p.toString().replaceFirst(BASE, "");
            if (id.startsWith("/")){
                id = id.substring(1);
            }
            if (avoids.contains(id)) {
                continue;
            }
            String text = FileUtils.readFileToString(p.toFile());
            documents.add(new Document("data", id, text));
        }

        final int docSize = documents.size();

        System.out.println(docSize + " documents need to annotate.");

        Scanner scanner = new Scanner(System.in);
        System.out.println("Press enter to continue ...");
        scanner.nextLine();

        List<AnnotatorClient> clients = getAllClients();

        Thread[] threads = new Thread[clients.size()];

        AtomicInteger numOfFailure = new AtomicInteger();
        AtomicInteger finished = new AtomicInteger();
        AtomicInteger hostDead = new AtomicInteger();
        ConcurrentHashSet<String> deadHosts = new ConcurrentHashSet<>();

        Thread reporter = new Thread(new Runnable() {
            @Override
            public void run() {
                long it = 0;
                while (!documents.isEmpty()) {
                    it++;
                    if (it % 20 == 0) {
                        try {
                            FileUtils.writeStringToFile(new File("deadhosts.lst"), StringUtils.join(deadHosts.stream().collect(Collectors.toList()), "\n"));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    System.out.print(String.format("Status: %d/%d finished. Average failure sentence-view count %d, host dead %d", finished.get(), docSize, numOfFailure.get(), hostDead.get()));
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    System.out.print("\r");
                }
                System.out.print(String.format("Status: %d/%d finished. Average failure sentence-view count %d", finished.get(), docSize, numOfFailure.get()));

            }
        });

        System.out.println(threads.length + " annotator running..");

        reporter.start();


        for (int i = 0; i < threads.length; i++) {
            int finalI = i;
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    int SUE_COUNT = 0;

                    AnnotatorClient client = clients.get(finalI);
                    while (!documents.isEmpty()) {
                        try {
                            Document d = documents.take();
//                            System.err.println(String.format("Annotation doc %s", d.getId()));
                            AnnotationResponse response = client
                                    .annotate(d.getCorpora(), d.getId(), d.getText(), getViews());
                            for (int j = 0; j < response.getFailures().size(); j++) {
                                numOfFailure.incrementAndGet();
                            }

//                            log.write(JsonUtils.PRETTY_GSON.toJson(response.getFailures()));
                            for (AnnotationFailures failures : response.getFailures()) {
                                if (failures.getMessage().startsWith("ServiceUnavailableException")) {
                                    SUE_COUNT++;
                                }
                            }

                            if (SUE_COUNT > 3) {
                                System.out.println("1.Annotator " + client.hostname + " dead...");
                                System.out.println("2.Annotator " + client.hostname + " dead...");
                                System.out.println("3.Annotator " + client.hostname + " dead...");
                                hostDead.incrementAndGet();
                                deadHosts.add(client.hostname);
                                Thread.sleep(1000 * 60);
                                hostDead.decrementAndGet();
                                deadHosts.remove(client.hostname);
                                SUE_COUNT = 0;
                            }

                            String result = JsonUtils.UGLY_GSON.toJson(response);
                            String filename = d.getId().replace("/", ".") + ".json";
                            if (filename.startsWith(".")) {
                                filename = filename.substring(1);
                            }
                            FileUtils.writeStringToFile(new File(OUTPUT_BASE, filename), result);
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
        System.out.println(String.format("Status: %d/%d finished. Average failure sentence-view count %d", finished.get(), docSize, numOfFailure.get()));

    }

}
