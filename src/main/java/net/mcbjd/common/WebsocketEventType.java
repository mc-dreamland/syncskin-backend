package net.mcbjd.common;

public enum WebsocketEventType {
    /**
     * Sent once we successfully connected to the server
     */
    SUBSCRIBER_CREATED(0),
    /**
     * Sent every time a subscriber got added or disconnected
     */
    SUBSCRIBER_COUNT(1),
    /**
     * Sent once the creator disconnected. After this packet the server will automatically close the
     * connection once the queue size (sent in {@link #ADDED_TO_QUEUE} and {@link #SKIN_UPLOADED}
     * reaches 0.
     */
    CREATOR_DISCONNECTED(4),

    /**
     * Sent every time a skin got added to the upload queue
     */
    ADDED_TO_QUEUE(2),
    /**
     * Sent every time a skin got successfully uploaded
     */
    SKIN_UPLOADED(3),
    SKIN_SYNC(7),

    /**
     * Sent every time a news item was added
     */
    NEWS_ADDED(6),

    /**
     * Sent when the server wants you to know something. Currently used for violations that aren't
     * bad enough to close the connection
     */
    LOG_MESSAGE(5);

    private static final WebsocketEventType[] VALUES;

    static {
        WebsocketEventType[] values = values();
        VALUES = new WebsocketEventType[values.length];
        for (WebsocketEventType value : values) {
            VALUES[value.id] = value;
        }
    }

    /**
     * The ID is based of the time it got added. However, to keep the enum organized as time goes on,
     * it looks nicer to sort the events based of categories.
     */
    private final int id;

    WebsocketEventType(int id) {
        this.id = id;
    }

    public static WebsocketEventType fromId(int id) {
        return VALUES.length > id ? VALUES[id] : null;
    }

    public int id() {
        return id;
    }
}
