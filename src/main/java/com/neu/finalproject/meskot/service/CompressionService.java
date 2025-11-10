package com.neu.finalproject.meskot.service;

import com.github.luben.zstd.ZstdInputStream;
import com.github.luben.zstd.ZstdOutputStream;

import java.io.*;

public class CompressionService {

    public File compress(File input) throws IOException {
        File out = new File(input.getParentFile(), input.getName() + ".zst");
        try (FileInputStream fis = new FileInputStream(input);
             FileOutputStream fos = new FileOutputStream(out);
             BufferedOutputStream bos = new BufferedOutputStream(fos);
             ZstdOutputStream zos = new ZstdOutputStream(bos)) {

            byte[] buffer = new byte[8192];
            int read;
            while ((read = fis.read(buffer)) != -1) {
                zos.write(buffer, 0, read);
            }
        }
        return out;
    }

    public File decompress(File compressed) throws IOException {
        String name = compressed.getName();
        String outName = name.endsWith(".zst") ? name.substring(0, name.length() - 4) : name + ".decompressed";
        File out = new File(compressed.getParentFile(), outName);

        try (FileInputStream fis = new FileInputStream(compressed);
             BufferedInputStream bis = new BufferedInputStream(fis);
             ZstdInputStream zis = new ZstdInputStream(bis);
             FileOutputStream fos = new FileOutputStream(out)) {

            byte[] buffer = new byte[8192];
            int read;
            while ((read = zis.read(buffer)) != -1) {
                fos.write(buffer, 0, read);
            }
        }
        return out;
    }
}
