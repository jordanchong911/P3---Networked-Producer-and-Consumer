package com.stdiscm.shared;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class UploadStatus {
    private final StringProperty fileName;
    private final DoubleProperty progress;
    private final StringProperty status;
    
    public UploadStatus(String fileName) {
        this.fileName = new SimpleStringProperty(fileName);
        this.progress = new SimpleDoubleProperty(0.0);
        this.status = new SimpleStringProperty("");
    }

    public UploadStatus(String fileName, String status) {
        this.fileName = new SimpleStringProperty(fileName);
        this.progress = new SimpleDoubleProperty(0.0);
        this.status = new SimpleStringProperty(status);
    }
    
    public String getFileName() {
        return fileName.get();
    }
    
    public StringProperty fileNameProperty() {
        return fileName;
    }

        // Add a property getter for binding
        public StringProperty statusProperty() {
            return status;
        }
    
    public double getProgress() {
        return progress.get();
    }
    
    public DoubleProperty progressProperty() {
        return progress;
    }
    
    public void setProgress(double value) {
        progress.set(value);
    }

    public void setFilename(String value) {
        fileName.set(value);
    }

    public String getStatus() {
        return status.get();
    }
    
    public void setStatus(String status) {
        this.status.set(status);
    }
}
