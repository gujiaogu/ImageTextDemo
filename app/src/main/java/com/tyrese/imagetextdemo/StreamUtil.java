package com.tyrese.imagetextdemo;

import java.io.Closeable;
import java.io.IOException;

/**
 * Created by Tyrese on 2016/5/31.
 */
public class StreamUtil {
    public static void close(Closeable is) {
        if (is != null) {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
