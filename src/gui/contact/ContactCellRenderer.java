package gui.contact;

import logic.data.Contact;

import javax.swing.*;
import java.awt.*;

import static logic.data.Constant.*;

class ContactCellRenderer extends JPanel implements ListCellRenderer<Contact> {
    private final JLabel idLabel;
    private final JLabel usernameLabel;
    private final JLabel ipLabel;

    public ContactCellRenderer() {
        setOpaque(true);
        setLayout(new GridBagLayout());
        setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        idLabel = new JLabel();
        usernameLabel = new JLabel();
        ipLabel = new JLabel();

        idLabel.setFont(DETAILS_FONT);
        usernameLabel.setFont(NAME_FONT);
        ipLabel.setFont(DETAILS_FONT);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 0, 0, 10);
        add(idLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.weightx = 1.0;
        add(usernameLabel, gbc);

        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.weightx = 0.0;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.insets = new Insets(0, 10, 0, 0);
        add(ipLabel, gbc);
    }

    public Component getListCellRendererComponent(JList list, Contact contact, int index, boolean isSelected, boolean cellHasFocus) {
        idLabel.setText(contact.getId());
        usernameLabel.setText(contact.getUserName());
        ipLabel.setText(contact.getIp());

        if (isSelected) {
            setBackground(SECONDARY_ACCENT_COLOR);
            idLabel.setForeground(Color.WHITE);
            usernameLabel.setForeground(Color.WHITE);
            ipLabel.setForeground(Color.WHITE);
        } else {
            setBackground(Color.WHITE);
            idLabel.setForeground(SECONDARY_ACCENT_COLOR);
            usernameLabel.setForeground(MAIN_ACCENT_COLOR);
            ipLabel.setForeground(SECONDARY_ACCENT_COLOR);
        }

        return this;
    }
}