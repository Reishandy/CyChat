package gui.contact;

import gui.bootup.Register;

import javax.swing.*;

public class Contact {
    private JPanel contactPanel;

    public Contact() {

    }
    public static JPanel getContact() {
        return new Contact().contactPanel;
    }
}
