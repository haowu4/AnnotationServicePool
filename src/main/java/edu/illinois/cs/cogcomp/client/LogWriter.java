package edu.illinois.cs.cogcomp.client;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by haowu4 on 7/4/17.
 */
public class LogWriter {
    private String file = "";
    BufferedWriter ERROR_LOG;

    public LogWriter() throws IOException {
        new BufferedWriter(new FileWriter("error.log"));
    }

    synchronized void write(String out) throws IOException {
        ERROR_LOG.write(out);
        ERROR_LOG.flush();
    }
}
