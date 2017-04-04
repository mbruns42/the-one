package core;

/**
 * Message which should be delivered to a certain group of nodes
 *
 * Created by Marius Meyer on 08.03.17.
 */
public class MulticastMessage extends Message {

    /**
     * the group this message is dedicated to
     */
    private Group group;

    /**
     * Creates a new Message.
     *
     * @param from Who the message is (originally) from
     * @param to   Who the message is (originally) to
     * @param id   Message identifier (must be unique for message but
     *             will be the same for all replicates of the message)
     * @param size Size of the message (in bytes)
     */
    public MulticastMessage(DTNHost from, Group to, String id, int size) {
        this(from, to, id, size, INVALID_PRIORITY);
    }
    
    /**
     * Creates a new Message that also has a priority.
     *
     * @param from Who the message is (originally) from
     * @param to   Who the message is (originally) to
     * @param id   Message identifier (must be unique for message but
     *             will be the same for all replicates of the message)
     * @param size Size of the message (in bytes)
     * @param prio Priority of the message
     */
    public MulticastMessage(DTNHost from, Group to, String id, int size, int prio) {
        super(from, null, id, size, prio);
        if  (!to.contains(from.getAddress())){
            throw new SimError("Sender must be in same group as the destination group," +
                    " but host "+ from + " is not " + to);
        }
        this.group = to;
    }

    /**
     * Returns the node this message is originally to
     * @return the node this message is originally to
     */
    @Override
    public DTNHost getTo() {
        throw new UnsupportedOperationException(
                "Cannot call getTo on MulticastMessage because it has no single recipient");
    }

    /**
     * Returns the group this message is addressed to
     *
     * @return the group this message is addressed to
     */
    public Group getGroup(){
        return group;
    }

    /**
     * Determines whether the provided node is a final recipient of the message.
     * @param host Node to check.
     * @return Whether the node is a final recipient of the message.
     */
    @Override
    public boolean isFinalRecipient(DTNHost host) {
        return group.contains(host.getAddress());
    }

    /**
     * Checks whether a successful sending to the provided DTNHost would mean a completed message delivery.
     * @param receiver The node that the message is sent to.
     * @return Whether or not delivery will be completed by a successful send operation.
     */
    @Override
    public boolean completesDelivery(DTNHost receiver) {
        return false;
    }

    /**
     * Returns a string representation of the message's recipient(s).
     * @return a string representation of the message's recipient(s).
     */
    @Override
    public String recipientsToString() {
        return group.toString();
    }

    /**
     * Returns a replicate of this message (identical except for the unique id)
     * @return A replicate of the message
     */
    @Override
    public Message replicate() {
        Message m = new MulticastMessage(from, group, id, size);
        m.copyFrom(this);
        return m;
    }


    /**
     * Copies the parameters from a different MulticastMessage instance
     * @param m the other MulticastMessage instance
     */
    public void copyFrom(MulticastMessage m){
        super.copyFrom(m);
        this.group = m.group;
    }

    /**
     * Gets the message type.
     * @return The message type.
     */
    @Override
    public MessageType getType() {
        return MessageType.MULTICAST;
    }
}
