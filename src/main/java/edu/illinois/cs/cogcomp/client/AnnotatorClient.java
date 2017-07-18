package edu.illinois.cs.cogcomp.client;

import edu.illinois.cs.cogcomp.annotation.AnnotatorException;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.utilities.SerializationHelper;
import edu.illinois.cs.cogcomp.service.Document;
import edu.illinois.cs.cogcomp.service.message.AnnotationRequest;
import edu.illinois.cs.cogcomp.service.message.AnnotationResponse;
import edu.illinois.cs.cogcomp.utils.JsonUtils;
import okhttp3.*;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Serializer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Created by haowu4 on 6/29/17.
 */
public class AnnotatorClient {

    public static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");

    protected String hostname;
    protected int port;
    OkHttpClient client;

    public AnnotatorClient(String hostname, int port) {
        this.hostname = hostname;
        this.port = port;

        client = new OkHttpClient().
                newBuilder()
                .readTimeout(75, TimeUnit.SECONDS)
                .connectTimeout(75, TimeUnit.SECONDS)
                .writeTimeout(75, TimeUnit.SECONDS)
                .build();
        ;
    }

    public AnnotationResponse annotate(String corporaId, String docId, String text, List<AnnotationRequest.AnnotationView> views) throws Exception {
        AnnotationRequest req = new AnnotationRequest(new Document(corporaId, docId, text), views);

        RequestBody body = RequestBody.create(JSON, JsonUtils.UGLY_GSON.toJson(req));
        String url = String.format("http://%s:%d/annotate", hostname, port);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        Response response = client.newCall(request).execute();

        if (response.code() != 200) {
            throw new AnnotatorException(String.format("Status : %d", response.code()));
        }
        String results = response.body().string();
        return JsonUtils.UGLY_GSON
                .fromJson(results, AnnotationResponse.class);
    }

    public static TextAnnotation getTextAnnotationFromResponse(AnnotationResponse annotationResponse) {
        byte[] blob = Base64.decodeBase64(annotationResponse.getAnnotation());
        return SerializationHelper.deserializeTextAnnotationFromBytes(blob);
    }

    public static List<AnnotationRequest.AnnotationView> getViews() {
//        return Arrays.asList(
//                new AnnotationRequest.AnnotationView(ViewNames.POS, true, true),
//                new AnnotationRequest.AnnotationView(ViewNames.NER_CONLL, true, true),
//                new AnnotationRequest.AnnotationView(ViewNames.SHALLOW_PARSE, true, true),
//                new AnnotationRequest.AnnotationView(ViewNames.SRL_NOM, true, true),
//                new AnnotationRequest.AnnotationView(ViewNames.SRL_VERB, true, true),
//                new AnnotationRequest.AnnotationView(ViewNames.LEMMA, false, true),
//                new AnnotationRequest.AnnotationView(ViewNames.DEPENDENCY_STANFORD, false, true),
//                new AnnotationRequest.AnnotationView(ViewNames.PARSE_STANFORD, false, true)
//        );

        return Arrays.asList(
                new AnnotationRequest.AnnotationView(ViewNames.POS, true, false),
                new AnnotationRequest.AnnotationView(ViewNames.NER_CONLL, true, false),
                new AnnotationRequest.AnnotationView(ViewNames.SHALLOW_PARSE, true, false),
                new AnnotationRequest.AnnotationView(ViewNames.SRL_NOM, true, false),
                new AnnotationRequest.AnnotationView(ViewNames.SRL_VERB, true, false),
                new AnnotationRequest.AnnotationView(ViewNames.LEMMA, false, false),
                new AnnotationRequest.AnnotationView(ViewNames.DEPENDENCY_STANFORD, false, false),
                new AnnotationRequest.AnnotationView(ViewNames.PARSE_STANFORD, false, false)
        );
    }

    public static void main(String[] args) throws IOException {
//        AnnotatorClient client = new AnnotatorClient("127.0.0.1", 4567);
        AnnotatorClient client = new AnnotatorClient("52.3.234.102", 4567);
        final String BASE = "/home/haowu4/data/coldstart/need_to_annotate/";

        List<Path> docPaths = Files.walk(Paths.get(BASE))
                .filter(Files::isRegularFile)
                .collect(Collectors.toList());

        System.out.println(docPaths.size() + " documents found.");

        Collections.shuffle(docPaths);

        List<Document> documents = new ArrayList<>();

        for (Path p : docPaths) {
            String id = p.toString().replaceFirst(BASE, "");
            String text = FileUtils.readFileToString(p.toFile());
            documents.add(new Document("", id, text));
            if (documents.size() == 100) {
                break;
            }
        }

        String DBFile = "/tmp/result.db";

        if (new File(DBFile).exists()) {
            new File(DBFile).delete();
        }

        DB db = DBMaker.fileDB(DBFile).closeOnJvmShutdown()
                .transactionEnable().make();
        ConcurrentMap<String, byte[]> map;
        map = db.hashMap("cache", Serializer.STRING, Serializer.BYTE_ARRAY).createOrOpen();

        for (Document d_old : documents) {
//            Document d = new Document(d_old.getCorpora(), d_old.getId(), d_old.getText().trim());
            Document d = d_old;
            try {
                AnnotationResponse response = client
                        .annotate(d.getCorpora(), d.getId(), d.getText(), getViews());
                TextAnnotation r = getTextAnnotationFromResponse(response);
                map.put(r.getId(), SerializationHelper.serializeTextAnnotationToBytes(r));
                db.commit();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        db.close();

    }
}
