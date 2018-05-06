package com.eiff.framework.common.utils.txt;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * 
 */
public class UnicodeFileUtil {
    public static List<String> readFile(InputStream in) throws IOException {
        BufferedReader br = new BufferedReader(new UnicodeReader(in, Charset.defaultCharset()
            .name()));
        List<String> list = new ArrayList<String>();
        try {

            String line = br.readLine();

            while (line != null) {
                list.add(line);
                line = br.readLine();
            }

        } finally {
            br.close();
            in.close();
        }
        return list;
    }

    public static List<String> readFile(String filePath) throws IOException {
        FileInputStream in = new FileInputStream(filePath);
        return readFile(in);
    }

}
