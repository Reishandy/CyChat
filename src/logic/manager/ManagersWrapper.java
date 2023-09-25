package logic.manager;

public class ManagersWrapper {
    private final ContactManager contactManager;
    private final PeerManager peerManager;

    public ManagersWrapper(ContactManager contactManager, PeerManager peerManager) {
        this.contactManager = contactManager;
        this.peerManager = peerManager;
    }

    public ContactManager getContactManager() {
        return contactManager;
    }

    public PeerManager getPeerManager() {
        return peerManager;
    }
}
