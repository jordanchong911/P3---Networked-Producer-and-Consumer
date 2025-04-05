package com.stdiscm.consumer;

import com.stdiscm.shared.VideoPacket;

import java.util.concurrent.BlockingQueue;

public class QueueProcessor implements Runnable {
    private final BlockingQueue<VideoPacket> inputQueue;
    private final BlockingQueue<VideoPacket> processingQueue;

    public QueueProcessor(BlockingQueue<VideoPacket> inputQueue, BlockingQueue<VideoPacket> processingQueue) {
        this.inputQueue = inputQueue;
        this.processingQueue = processingQueue;
    }

    @Override
    public void run() {
        while (true) {
            try {
                VideoPacket packet = inputQueue.take();       // simulate filtering/delay
                processingQueue.put(packet);                  // hand off to consumers
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
