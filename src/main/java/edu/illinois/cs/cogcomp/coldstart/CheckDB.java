package edu.illinois.cs.cogcomp.coldstart;

import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Serializer;

import java.util.concurrent.ConcurrentMap;

/**
 * Created by haowu4 on 6/27/17.
 */
public class CheckDB {
    public static void main(String[] args) {
        DB db = DBMaker.fileDB("/home/haowu4/data/coldstart_results/result.db").closeOnJvmShutdown().transactionEnable().make();
        ConcurrentMap<String, byte[]> map;
        map = db.hashMap("cache", Serializer.STRING, Serializer.BYTE_ARRAY).createOrOpen();

        for (String did : map.keySet()){
            System.out.println(did);
        }

    }
}
