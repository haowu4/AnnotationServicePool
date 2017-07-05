package edu.illinois.cs.cogcomp.coldstart;

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.utilities.SerializationHelper;
import edu.illinois.cs.cogcomp.service.message.AnnotationResponse;
import edu.illinois.cs.cogcomp.utils.JsonUtils;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by haowu4 on 6/27/17.
 */
public class CheckResults {


    public static TextAnnotation getTextAnnotationFromResponse(AnnotationResponse annotationResponse) {
        byte[] blob = Base64.decodeBase64(annotationResponse.getAnnotation());
        return SerializationHelper.deserializeTextAnnotationFromBytes(blob);
    }


    public static void main(String[] args) throws IOException {
        final String BASE = "/home/haowu4/data/coldstart/results/";

        List<Path> docPaths = Files.walk(Paths.get(BASE))
                .filter(Files::isRegularFile)
                .collect(Collectors.toList());

        System.out.println(docPaths.size() + " documents found.");

        List<String> goodFiles = new ArrayList<>();
        List<String> badFilesList = new ArrayList<>();

        int counter = 0;
        int badFiles = 0;
        for (Path p : docPaths) {
            counter++;
            File f = p.toFile();
            String content = FileUtils.readFileToString(f);
            AnnotationResponse response = JsonUtils.UGLY_GSON.fromJson(content, AnnotationResponse.class);
//            TextAnnotation r = getTextAnnotationFromResponse(response);
            if (response.getFailures().size() > 4) {
                System.out.print(String.format(" %d/%d \r", badFiles, counter));
                badFiles++;
                badFilesList.add(f.getAbsolutePath());
            } else {
                goodFiles.add(f.getAbsolutePath());
            }
        }

        FileUtils.writeLines(new File("/tmp/annotation.result"), goodFiles);
        FileUtils.writeLines(new File("/tmp/bad_file_list.txt"), goodFiles);

    }
}