package com.stdiscm.consumer;

import java.net.Socket;
import java.util.concurrent.atomic.AtomicBoolean;

import com.stdiscm.shared.UploadStatus;

public class QueuedUpload {
    private final Socket socket;
    private final UploadStatus status;
    private final String fileName;
    private final AtomicBoolean ready = new AtomicBoolean(false);
    

    public QueuedUpload(Socket socket, UploadStatus status, String fileName) {
        this.socket = socket;
        this.status = status;
        this.fileName = fileName;
    }

    public Socket getSocket() {
        return socket;
    }

    public UploadStatus getStatus() {
        return status;
    }

    public String getFileName() {
        return fileName;
    }

    
    public void setReady() {
        ready.set(true);
    }

    public boolean isReady() {
        return ready.get();
    }

}
