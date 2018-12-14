package com.glavsoft.rfb.protocol;

import com.glavsoft.rfb.client.ClientToServerMessage;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class MessageQueue {
    private final BlockingQueue queue = new LinkedBlockingQueue();

    public MessageQueue() {
    }

    public void put(ClientToServerMessage message) {
        this.queue.offer(message);
    }

    public ClientToServerMessage get() throws InterruptedException {
        return (ClientToServerMessage) this.queue.take();
    }
}
