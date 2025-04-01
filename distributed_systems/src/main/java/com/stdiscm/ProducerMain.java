package com.stdiscm;
import java.io.File;
import java.util.Scanner;
import java.util.List;
import java.util.ArrayList;

public class ProducerMain {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // Step 1: Prompt for the number of producer threads
        System.out.print("Enter the number of producer threads (p): ");
        int numProducers = scanner.nextInt();
        scanner.nextLine(); // Consume newline

        // Step 2: Create a list to store producer threads
        List<Thread> producerThreads = new ArrayList<>();

        // Step 3: For each producer thread, prompt for the video directory
        for (int i = 1; i <= numProducers; i++) {
            System.out.print("Enter the path to the directory containing videos for producer " + i + ": ");
            String videoFolderPath = scanner.nextLine();

            // Validate the directory
            File folder = new File(videoFolderPath);
            if (!folder.exists() || !folder.isDirectory()) {
                System.err.println("Invalid directory: " + videoFolderPath);
                continue;
            }

            // Create and start a producer thread
            Producer producer = new Producer(videoFolderPath);
            Thread producerThread = new Thread(producer);
            producerThreads.add(producerThread);
            producerThread.start();
        }

        // Step 4: Wait for all producer threads to finish
        for (Thread thread : producerThreads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                System.err.println("Producer thread interrupted: " + e.getMessage());
            }
        }

        System.out.println("All producers have finished.");
        scanner.close();
    }
}