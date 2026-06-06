package net.mcbjd.websocket;

import net.mcbjd.service.AbstractWebSocket;

import javax.enterprise.context.ApplicationScoped;
import javax.websocket.server.ServerEndpoint;

@ServerEndpoint("/floodgate")
@ApplicationScoped
public class FloodgateWebSocket extends AbstractWebSocket {
    public FloodgateWebSocket() {
        super("Floodgate");
    }
}
