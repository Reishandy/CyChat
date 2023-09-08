package manager;

import data.Contact;

import java.util.ArrayList;

public class ContactManager {
    private final ArrayList<Contact> contacts;

    public ContactManager() {
        contacts = new ArrayList<>();
    }

    public ContactManager(ArrayList<Contact> contacts) {
        this.contacts = contacts;
    }

    public boolean findContact(Contact contact) {
        return contacts.contains(contact);
    }

    public void addContact(Contact contact) {
        contacts.add(contact);
    }

    public ArrayList<Contact> getContacts() {
        return contacts;
    }
}
