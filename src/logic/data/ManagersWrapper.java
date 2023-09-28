package logic.data;

import logic.manager.ContactManager;
import logic.manager.PeerManager;

public record ManagersWrapper(ContactManager contactManager, PeerManager peerManager) {
}
