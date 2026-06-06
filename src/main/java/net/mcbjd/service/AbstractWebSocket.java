package net.mcbjd.service;

import lombok.SneakyThrows;
import org.jboss.logging.Logger;

import javax.websocket.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class AbstractWebSocket implements WebSocketService {
    private final String name;

    public AbstractWebSocket(String name) {
        this.name = name;
    }

    public static final Logger LOG = Logger.getLogger(AbstractWebSocket.class);
    private List<Session> sessions = Collections.synchronizedList(new ArrayList<>());

    public List<Session> getSessions() {
        return sessions;
    }

    @OnOpen
    public void connect(Session session){
        this.sessions.add(session);
        LOG.info(name+ " connect: "+session +" total: "+this.sessions.size());
    }

    @SneakyThrows
    @OnClose
    public void close(Session session){
        LOG.debug(name+ " close: "+session);
        this.sessions.remove(session);
        if (session.isOpen()){
            session.close(new CloseReason(CloseReason.CloseCodes.NORMAL_CLOSURE,"close"));
        }
    }

    @OnError
    public void error(Session session, Throwable throwable) {
        this.close(session);
        LOG.error(name+ " Error: ",throwable);
    }

    @Override
    @OnMessage
    public void message(String message) {


    }
}
