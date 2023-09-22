package logic.manager;

import logic.data.Contact;

import java.util.ArrayList;

public class ContactManager {
    private ArrayList<Contact> contacts;

    public ContactManager() {
        contacts = new ArrayList<>();
    }

    public boolean checkContactExist(String id) {
        for (Contact contact: contacts) {
            if (contact.getId().equals(id)) return true;
        }
        return false;
    }

    public void addContact(Contact contact) {
        contacts.add(contact);
    }

    public void addContact(ArrayList<Contact> contact) {
        contacts = contact;
    }

    public void updateIpAddress(String id, String ipAddress) {
        for (Contact contact: contacts) {
            if (contact.getId().equals(id)) {
                contact.setIp(ipAddress);
                return;
            }
        }
    }

    public Contact getContact(String id) {
        for (Contact contact: contacts) {
            if (contact.getId().equals(id)) return contact;
        }
        return null;
    }

    public ArrayList<Contact> getContacts() {
        return contacts;
    }
}
