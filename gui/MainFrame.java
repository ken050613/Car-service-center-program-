package com.apu.asc.gui;

import com.apu.asc.bootstrap.AscServices;
import com.apu.asc.model.Role;
import com.apu.asc.model.User;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {
    private final AscServices services;
    private User currentUser;
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel deck = new JPanel(cardLayout);

    private final LoginPanel loginPanel;
    private final ManagerPanel managerPanel;
    private final CounterPanel counterPanel;
    private final TechnicianPanel technicianPanel;
    private final CustomerPanel customerPanel;

    public MainFrame(AscServices services) {
        super("APU Automotive Service Centre (ASC)");
        this.services = services;

        loginPanel = new LoginPanel(this);
        managerPanel = new ManagerPanel(this);
        counterPanel = new CounterPanel(this);
        technicianPanel = new TechnicianPanel(this);
        customerPanel = new CustomerPanel(this);

        deck.add(loginPanel, "LOGIN");
        deck.add(managerPanel, "MANAGER");
        deck.add(counterPanel, "COUNTER");
        deck.add(technicianPanel, "TECH");
        deck.add(customerPanel, "CUSTOMER");

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(920, 620));
        setLocationRelativeTo(null);
        add(deck, BorderLayout.CENTER);
        cardLayout.show(deck, "LOGIN");
    }

    public AscServices getServices() {
        return services;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
    }

    public void showLogin() {
        currentUser = null;
        cardLayout.show(deck, "LOGIN");
        loginPanel.clearFields();
    }

    public void showDashboardFor(User user) {
        this.currentUser = user;
        Role r = user.getRole();
        switch (r) {
            case MANAGER -> {
                managerPanel.onShow();
                cardLayout.show(deck, "MANAGER");
            }
            case COUNTER_STAFF -> {
                counterPanel.onShow();
                cardLayout.show(deck, "COUNTER");
            }
            case TECHNICIAN -> {
                technicianPanel.onShow();
                cardLayout.show(deck, "TECH");
            }
            case CUSTOMER -> {
                customerPanel.onShow();
                cardLayout.show(deck, "CUSTOMER");
            }
        }
    }
}
