package com.stdiscm.shared;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;

public class VideoPacket implements Serializable {
    private String filename;
    private byte[] data;

    public VideoPacket(String filename, byte[] data) {
        this.filename = filename;
        this.data = data;
    }

    public VideoPacket(File file) throws IOException {
        this.filename = file.getName();
        this.data = readFileBytes(file);
    }

    private byte[] readFileBytes(File file) throws IOException {
        try (FileInputStream fis = new FileInputStream(file);
            ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                baos.write(buffer, 0, bytesRead);
            }
            return baos.toByteArray();
        }
    }

    // Getter for the file name
    public String getFilename() {
        return filename;
    }

    // Optionally, override toString() for easier debugging/logging.
    @Override
    public String toString() {
        return "VideoPacket [filename=" + filename + "]";
    }

    public byte[] getData() {
        return data;
    }
}
