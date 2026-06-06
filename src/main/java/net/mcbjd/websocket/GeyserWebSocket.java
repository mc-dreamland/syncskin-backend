package net.mcbjd.websocket;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.websocket.server.ServerEndpoint;

import net.mcbjd.service.AbstractWebSocket;
import net.mcbjd.service.impl.GeyserWebSocketService;
import io.smallrye.common.annotation.NonBlocking;
import org.jboss.logging.Logger;

@ServerEndpoint("/geyser")
@ApplicationScoped
public class GeyserWebSocket extends AbstractWebSocket {
    private static final Logger LOG = Logger.getLogger(GeyserWebSocket.class);
    @Inject
    GeyserWebSocketService geyserService;

    public GeyserWebSocket() {
        super("Geyser");
    }

    @Override
    @NonBlocking
    public void message(String message) {
        LOG.debug("Message: "+message);
        this.geyserService.convertBedrockClientData(message);
    }
}
