/*
 * Copyright 2010 Aalto University, ComNet
 * Released under GPLv3. See LICENSE.txt for details.
 */
package input;

import core.DTNHost;
import core.Message;
import core.World;

/**
 * External event for creating a message.
 */
public class MessageCreateEvent extends MessageEvent {
    private int size;
    private int responseSize;

    /**
     * Creates a message creation event with a optional response request
     * 
     * @param from
     *            The creator of the message
     * @param to
     *            Where the message is destined to
     * @param id
     *            ID of the message
     * @param size
     *            Size of the message
     * @param responseSize
     *            Size of the requested response message or 0 if no response is
     *            requested
     * @param time
     *            Time, when the message is created
     * @param prio
     *            Priority of the messsage
     */
    public MessageCreateEvent(int from, int to, String id, int size, int responseSize, double time, int prio) {
        super(from, to, id, time, prio);
        this.size = size;
        this.responseSize = responseSize;
    }

    /**
     * Creates a message event. Using the same parameters as the previous
     * constructor, but the priority. Used for MulticastCreateEvents without
     * explicit priority. Therefore, gets the INVALID_PRIORITY. Calls the
     * previous constructor.
     */
    public MessageCreateEvent(int from, int to, String id, int size, int responseSize, double time) {
        this(from, to, id, size, responseSize, time, INVALID_PRIORITY);
    }

    /**
     * Creates the message this event represents.
     */
    @Override
    public void processEvent(World world) {
        DTNHost to = world.getNodeByAddress(this.toAddr);
        DTNHost from = world.getNodeByAddress(this.fromAddr);

        Message m = new Message(from, to, this.id, this.size, this.priority);
        m.setResponseSize(this.responseSize);
        from.createNewMessage(m);
    }

    @Override
    public String toString() {
        return super.toString() + " [" + fromAddr + "->" + toAddr + "] " + "size:" + size + " CREATE";
    }
}
