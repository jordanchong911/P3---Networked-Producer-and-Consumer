package com.stdiscm.consumer;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class UploadStatus {
    private final StringProperty fileName;
    private final DoubleProperty progress;
    
    public UploadStatus(String fileName) {
        this.fileName = new SimpleStringProperty(fileName);
        this.progress = new SimpleDoubleProperty(0.0);
    }
    
    public String getFileName() {
        return fileName.get();
    }
    
    public StringProperty fileNameProperty() {
        return fileName;
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
}
