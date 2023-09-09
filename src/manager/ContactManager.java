package manager;

import data.Contact;

import java.util.ArrayList;

public class ContactManager {
    private final ArrayList<Contact> contacts;

    public ContactManager() {
        contacts = new ArrayList<>();
    }

    public boolean checkContactExist(String userName) {
        for (Contact contact: contacts) {
            if (contact.getUserName().equals(userName)) return true;
        }
        return false;
    }

    public void addContact(Contact contact) {
        contacts.add(contact);
    }

    public void updateIpAddress(String userName, String ipAddress) {
        for (Contact contact: contacts) {
            if (contact.getUserName().equals(userName)) {
                contact.setIp(ipAddress);
                return;
            }
        }
    }

    public Contact getContact(String userName) {
        for (Contact contact: contacts) {
            if (contact.getUserName().equals(userName)) return contact;
        }
        return null;
    }

    public ArrayList<Contact> getContacts() {
        return contacts;
    }
}
