package com.stdiscm.shared;

import java.io.Serializable;

public class VideoPacket implements Serializable {
    private String filename;
    private byte[] data;

    public VideoPacket(String filename, byte[] data) {
        this.filename = filename;
        this.data = data;
    }

    public String getFilename() {
        return filename;
    }

    public byte[] getData() {
        return data;
    }
}
