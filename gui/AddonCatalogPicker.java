package com.apu.asc.gui;

import com.apu.asc.model.ServiceItem;
import com.apu.asc.model.ServiceKind;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Multi-select add-ons from the price catalogue (excludes NORMAL / MAJOR tier rows).
 */
public class AddonCatalogPicker extends JPanel {
    private final JPanel checksHost = new JPanel();
    private final List<JCheckBox> boxes = new ArrayList<>();

    public AddonCatalogPicker() {
        super(new BorderLayout(4, 4));
        checksHost.setLayout(new BoxLayout(checksHost, BoxLayout.Y_AXIS));
        JScrollPane sp = new JScrollPane(checksHost);
        sp.setPreferredSize(new Dimension(360, 120));
        sp.setBorder(BorderFactory.createTitledBorder("Optional add-ons (tick any)"));
        add(sp, BorderLayout.CENTER);
    }

    /** Reload from manager catalogue. Safe to call on every dashboard show. */
    public void reload(MainFrame frame) {
        checksHost.removeAll();
        boxes.clear();
        String normal = ServiceKind.NORMAL.catalogCode();
        String major = ServiceKind.MAJOR.catalogCode();
        for (ServiceItem it : frame.getServices().pricing().listPrices()) {
            String code = it.getCode();
            if (code.equalsIgnoreCase(normal) || code.equalsIgnoreCase(major)) {
                continue;
            }
            JCheckBox cb = new JCheckBox(
                    code + " — " + it.getDescription() + "  (" + it.getUnitPrice() + ")");
            cb.putClientProperty("code", code);
            boxes.add(cb);
            checksHost.add(cb);
        }
        if (boxes.isEmpty()) {
            checksHost.add(new JLabel("No add-ons in catalogue. Manager can add codes under Prices."));
        }
        checksHost.revalidate();
        checksHost.repaint();
    }

    public List<String> getSelectedCodes() {
        List<String> out = new ArrayList<>();
        for (JCheckBox cb : boxes) {
            if (cb.isSelected()) {
                Object c = cb.getClientProperty("code");
                if (c instanceof String s && !s.isBlank()) {
                    out.add(s);
                }
            }
        }
        return out;
    }
}
