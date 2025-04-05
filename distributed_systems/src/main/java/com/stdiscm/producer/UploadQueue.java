package com.stdiscm.producer;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class UploadQueue {
    private static final int MAX_UPLOADS = 5; // adjust as needed
    private static final BlockingQueue<Runnable> queue = new LinkedBlockingQueue<>(MAX_UPLOADS);

    public static void submitUpload(Runnable task) {
        try {
            queue.put(task);
            new Thread(() -> {
                try {
                    task.run();
                } finally {
                    queue.remove(task);
                }
            }).start();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
