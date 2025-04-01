package com.stdiscm;
import java.util.Date;

public class VideoInfo {
    private final String fileName;
    private final Date uploadTime;

    public VideoInfo(String fileName, Date uploadTime) {
        this.fileName = fileName;
        this.uploadTime = uploadTime;
    }

    public String getFileName() {
        return fileName;
    }

    public Date getUploadTime() {
        return uploadTime;
    }

    @Override
    public String toString() {
        // Format the upload time for display
        return fileName + " (Uploaded: " + uploadTime.toString() + ")";
    }
}