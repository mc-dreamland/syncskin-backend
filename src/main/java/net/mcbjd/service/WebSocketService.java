package net.mcbjd.service;

import javax.websocket.*;

public interface WebSocketService {
    void connect(Session session);
    void close(Session session);
    void error(Session session, Throwable throwable);
    void message(String message);
}
