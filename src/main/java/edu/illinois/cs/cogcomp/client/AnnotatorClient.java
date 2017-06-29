package edu.illinois.cs.cogcomp.client;

import static edu.illinois.cs.cogcomp.utils.CuratorUtils.getLocalViews;
import static edu.illinois.cs.cogcomp.utils.CuratorUtils.getRemoteViews;

import edu.illinois.cs.cogcomp.annotation.AnnotatorException;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.utilities.SerializationHelper;
import edu.illinois.cs.cogcomp.service.Document;
import edu.illinois.cs.cogcomp.service.message.AnnotationRequest;
import edu.illinois.cs.cogcomp.service.message.AnnotationResponse;
import edu.illinois.cs.cogcomp.utils.JsonUtils;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Serializer;

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

    client = new OkHttpClient();
  }

  public TextAnnotation annotate(String corporaId, String docId, String text, String[] localView,
      String[] remoteView) throws Exception {
    AnnotationRequest req = new AnnotationRequest(new Document(corporaId, docId, text), localView,
        remoteView);

    RequestBody body = RequestBody.create(JSON, JsonUtils.UGLY_GSON.toJson(req));
    String url = String.format("http://%s:%d/annotate", hostname, port);
    Request request = new Request.Builder()
        .url(url)
        .post(body)
        .build();

    Response response = client.newCall(request).execute();
    response.body().string();

    if (response.code() != 200) {
      throw new AnnotatorException(String.format("Status : %d", response.code()));
    }
    String results = response.body().string();
    AnnotationResponse annotationResponse = JsonUtils.UGLY_GSON
        .fromJson(results, AnnotationResponse.class);
    byte[] blob = Base64.decodeBase64(annotationResponse.getAnnotation());

    TextAnnotation textAnnotation = SerializationHelper.deserializeTextAnnotationFromBytes(blob);
    return textAnnotation;
  }

  public static void main(String[] args) throws IOException {
    AnnotatorClient client = new AnnotatorClient("34.206.71.226", 4567);
    final String BASE = "/home/haowu4/data/codestart/need_to_annotate/";

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

    DB db = DBMaker.fileDB("/tmp/result1.db").closeOnJvmShutdown()
        .transactionEnable().make();
    ConcurrentMap<String, byte[]> map;
    map = db.hashMap("cache", Serializer.STRING, Serializer.BYTE_ARRAY).createOrOpen();

    for (Document d : documents) {
      try {
        TextAnnotation r = client
            .annotate(d.getCorpora(), d.getId(), d.getText(), getLocalViews(), getRemoteViews());
        map.put(r.getId(), SerializationHelper.serializeTextAnnotationToBytes(r));
        db.commit();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

    db.close();

  }
}
