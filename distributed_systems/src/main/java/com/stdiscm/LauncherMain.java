package com.stdiscm;
import java.io.IOException;

import java.io.IOException;

public class LauncherMain {
    public static void main(String[] args) {
        try {
            // Start the ConsumerMain process
            ProcessBuilder consumerProcessBuilder = new ProcessBuilder("java", "-cp", "distributed_systems-1.0-SNAPSHOT-jar-with-dependencies.jar", "com.stdiscm.ConsumerMain");
            consumerProcessBuilder.inheritIO(); // Redirect output to the console
            Process consumerProcess = consumerProcessBuilder.start();

            // Start the ProducerMain process
            ProcessBuilder producerProcessBuilder = new ProcessBuilder("java", "-cp", "distributed_systems-1.0-SNAPSHOT-jar-with-dependencies.jar", "com.stdiscm.ProducerMain");
            producerProcessBuilder.inheritIO(); // Redirect output to the console
            Process producerProcess = producerProcessBuilder.start();

            // Wait for both processes to finish
            consumerProcess.waitFor();
            producerProcess.waitFor();
        } catch (IOException | InterruptedException e) {
            System.err.println("Error launching processes: " + e.getMessage());
        }
    }
}