package com.mongodb.replication.gui;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;

public class MongoConnectionDialog extends JDialog implements ActionListener {
    
    private String host;
    private String port;
    
    JTextField hostnameField;
    JTextField portField;
    JButton saveButton;

    public MongoConnectionDialog(JFrame parentFrame) {
        super(parentFrame, "Variable Selection");
        setLayout(new GridBagLayout());
        JLabel label1 = new JLabel("hostname");
        JLabel label2 = new JLabel("Password");
        hostnameField = new JTextField(20);
        portField = new JTextField(10);
        saveButton = new JButton("Save");
        add(label1, 0, 0, 1, 1);
        add(hostnameField, 1, 0, 1, 1);
        add(label2, 0, 1, 1, 1);

        add(portField, 1, 1, 1, 1);
        add(saveButton, 1, 2, 1, 1);
        setBounds(100, 100, 300, 100);
    }

    public void add(Component c, int x, int y, int w, int h) {
        GridBagConstraints gbc = new GridBagConstraints();
        // gbc.weightx = 1;
        gbc.gridx = x;
        gbc.gridy = y;
        gbc.gridwidth = w;
        gbc.gridheight = h;
        add(c, gbc);
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        if (ae.getSource() == saveButton) {
            host = hostnameField.getText();
            port = portField.getText();
        } else {
            host = null;
            port = null;
        }
        setVisible(false);
        dispose();
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

}
