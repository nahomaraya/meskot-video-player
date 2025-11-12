package com.neu.finalproject.meskot.service;

import com.github.luben.zstd.ZstdInputStream;
import com.github.luben.zstd.ZstdOutputStream;
import org.springframework.stereotype.Service;

import java.io.*;

@Service
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

    public File decompressZst(File compressedFile) throws IOException {
        File tempFile = File.createTempFile("decompressed-", ".mp4");
        try (ZstdInputStream zin = new ZstdInputStream(new FileInputStream(compressedFile));
             FileOutputStream out = new FileOutputStream(tempFile)) {
            zin.transferTo(out);
        }
        return tempFile;
    }
}
