package edu.illinois.cs.cogcomp.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Created by haowu4 on 6/28/17.
 */
public class JsonUtils {
  public static final Gson UGLY_GSON = new GsonBuilder().create();
  public static final Gson PRETTY_GSON = new GsonBuilder().setPrettyPrinting().create();
}
