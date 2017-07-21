package edu.illinois.cs.cogcomp.coldstart;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import edu.illinois.cs.cogcomp.client.AnnotatorClient;
import edu.illinois.cs.cogcomp.client.PoolOfClient;
import edu.illinois.cs.cogcomp.core.utilities.StringTransformation;
import edu.illinois.cs.cogcomp.service.Document;
import edu.illinois.cs.cogcomp.service.message.AnnotationFailures;
import edu.illinois.cs.cogcomp.service.message.AnnotationResponse;
import edu.illinois.cs.cogcomp.utils.AnnotationUtils;
import edu.illinois.cs.cogcomp.utils.JsonUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jetty.util.ConcurrentHashSet;

import java.io.File;
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
 * Created by haowu4 on 7/20/17.
 */
public class CheckTransformation {

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


        List<Document> documents = new ArrayList<>();

        for (Path p : docPaths) {
            String id = p.toString().replaceFirst(BASE, "");
            if (id.startsWith("/")) {
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

        for (Document doc : documents) {
            StringTransformation transformation = new StringTransformation(doc.getText());
            try {
                AnnotationUtils.cleanUp(transformation);
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println(doc.getId());
            }
        }
    }
}
