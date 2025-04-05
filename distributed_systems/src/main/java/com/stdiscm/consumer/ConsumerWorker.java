package com.stdiscm.consumer;

import com.stdiscm.shared.VideoPacket;

import java.io.FileOutputStream;
import java.util.concurrent.BlockingQueue;

public class ConsumerWorker implements Runnable {
    private final BlockingQueue<VideoPacket> queue;

    public ConsumerWorker(BlockingQueue<VideoPacket> queue) {
        this.queue = queue;
    }

    @Override
    public void run() {
        while (true) {
            try {
                VideoPacket packet = queue.take();
                String filename = "received/" + packet.getFilename();
                try (FileOutputStream out = new FileOutputStream(filename)) {
                    out.write(packet.getData());
                    System.out.println("Saved: " + filename);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
