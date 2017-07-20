package edu.illinois.cs.cogcomp.check;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import edu.illinois.cs.cogcomp.client.PoolOfClient;
import edu.illinois.cs.cogcomp.core.utilities.StringTransformation;
import edu.illinois.cs.cogcomp.service.Document;
import edu.illinois.cs.cogcomp.utils.AnnotationUtils;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.stream.Collectors;

/**
 * Created by haowu4 on 7/20/17.
 */
public class GenerateST {


    private static class Args {
        @Parameter(names = {"-in"}, description = "Input folder")
        String inputFolder = "/home/haowu4/annotation/coldstart_2017/need_to_annotate/";

        @Parameter(names = {"-out"}, description = "Output folder")
        String outputFolder = "/home/haowu4/annotation/coldstart_2017_sts/";

        @Override
        public String toString() {
            return "Args{" +
                    "inputFolder='" + inputFolder + '\'' +
                    ", outputFolder='" + outputFolder + '\'' +
                    '}';
        }
    }


    public static void main(String[] argv) throws IOException {

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
        int i = 0;
        for (Document doc : documents) {
            System.out.print(i);
            StringTransformation transformation = new StringTransformation(doc.getText());
            AnnotationUtils.cleanUp(transformation);
            File output = new File(OUTPUT_BASE, doc.getId() + ".st");
            try (ObjectOutputStream oos =
                         new ObjectOutputStream(new FileOutputStream(output))) {
                oos.writeObject(transformation);
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            try (ObjectInputStream oos =
                         new ObjectInputStream(new FileInputStream(output))) {
                StringTransformation st = (StringTransformation) oos.readObject();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            System.out.print("\r");
            i++;
        }

    }
}
