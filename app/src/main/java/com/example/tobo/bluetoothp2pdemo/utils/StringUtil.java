package com.example.tobo.bluetoothp2pdemo.utils;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by tobo on 17/1/17.
 */

public class StringUtil {
    public static String stream2String(InputStream in) throws IOException {
        StringBuffer out = new StringBuffer();
        byte[] b = new byte[4096];
        for (int n; (n = in.read(b)) != -1;) {
            out.append(new String(b, 0, n));
        }
        return out.toString();
    }

}
