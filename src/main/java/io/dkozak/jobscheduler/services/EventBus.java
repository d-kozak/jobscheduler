package io.dkozak.jobscheduler.services;

import lombok.extern.log4j.Log4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Log4j
public class EventBus {
    private final List<EventBusListener> listeners = new ArrayList<>();

    public interface EventBusListener {
        void onMessage(String messageId, Optional<Object> content);
    }

    public void sendMessage(String messageId) {
        sendMessage(messageId, Optional.empty());
    }

    public void sendMessage(String messageId, Optional<Object> content) {
        log.info("sending message " + messageId);
        listeners.forEach(listener -> listener.onMessage(messageId, content));
    }

    public void register(EventBusListener listener) {
        listeners.add(listener);
    }

    public void unregister(EventBusListener listener) {
        listeners.remove(listener);
    }
}
