package com.apu.asc.gui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Text field plus button to open a calendar (month grid) and clock (hour/minute spinners).
 */
public class ScheduleDateTimePicker extends JPanel {
    public static final DateTimeFormatter DISPLAY = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final JTextField field = new JTextField(18);

    public ScheduleDateTimePicker() {
        LocalDateTime now = LocalDateTime.now().withSecond(0).withNano(0);
        field.setText(DISPLAY.format(now));
        field.setToolTipText("You can type yyyy-MM-dd HH:mm or use the picker");
        JButton pick = new JButton("Pick date & time…");
        pick.addActionListener(e -> {
            Window w = SwingUtilities.getWindowAncestor(this);
            if (w == null) {
                w = JOptionPane.getRootFrame();
            }
            SchedulePickerDialog.open(w, field);
        });
        setLayout(new FlowLayout(FlowLayout.LEFT, 8, 0));
        add(field);
        add(pick);
    }

    public LocalDateTime getDateTime() throws DateTimeParseException {
        return LocalDateTime.parse(field.getText().trim(), DISPLAY);
    }

    public void setDateTime(LocalDateTime t) {
        field.setText(DISPLAY.format(t.withSecond(0).withNano(0)));
    }
}

final class SchedulePickerDialog extends JDialog {
    private final JTextField targetField;
    private final JComboBox<Month> monthBox = new JComboBox<>(Month.values());
    private final JSpinner yearSpin = new JSpinner(new SpinnerNumberModel(2026, 2020, 2040, 1));
    private final JSpinner hourSpin = new JSpinner(new SpinnerNumberModel(9, 0, 23, 1));
    private final JSpinner minuteSpin = new JSpinner(new SpinnerNumberModel(0, 0, 59, 1));
    private final JPanel gridPanel = new JPanel(new GridLayout(7, 7, 3, 3));
    private int selectedDay;

    private SchedulePickerDialog(Window owner, JTextField targetField) {
        super(owner, "Choose date and time", ModalityType.APPLICATION_MODAL);
        this.targetField = targetField;
        LocalDateTime initial;
        try {
            initial = LocalDateTime.parse(targetField.getText().trim(), ScheduleDateTimePicker.DISPLAY);
        } catch (Exception e) {
            initial = LocalDateTime.now().withSecond(0).withNano(0);
        }
        monthBox.setSelectedItem(initial.getMonth());
        yearSpin.setValue(initial.getYear());
        selectedDay = initial.getDayOfMonth();
        hourSpin.setValue(initial.getHour());
        minuteSpin.setValue(initial.getMinute());

        JPanel root = new JPanel(new BorderLayout(8, 8));
        root.setBorder(new EmptyBorder(12, 12, 12, 12));

        JPanel nav = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        JButton prev = new JButton("◀ Month");
        JButton next = new JButton("Month ▶");
        nav.add(prev);
        nav.add(monthBox);
        nav.add(yearSpin);
        nav.add(next);

        JPanel calWrap = new JPanel(new BorderLayout());
        calWrap.add(nav, BorderLayout.NORTH);
        calWrap.add(gridPanel, BorderLayout.CENTER);

        JPanel timeRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 4));
        timeRow.add(new JLabel("Clock (24-hour)"));
        timeRow.add(new JLabel("Hour:"));
        timeRow.add(hourSpin);
        timeRow.add(new JLabel("Minute:"));
        timeRow.add(minuteSpin);

        JPanel south = new JPanel(new BorderLayout(0, 8));
        south.add(timeRow, BorderLayout.NORTH);
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton ok = new JButton("OK");
        JButton cancel = new JButton("Cancel");
        ok.addActionListener(e -> applyAndClose());
        cancel.addActionListener(e -> dispose());
        buttons.add(cancel);
        buttons.add(ok);
        south.add(buttons, BorderLayout.SOUTH);

        root.add(calWrap, BorderLayout.CENTER);
        root.add(south, BorderLayout.SOUTH);
        setContentPane(root);

        Runnable rebuild = this::rebuildDayGrid;
        monthBox.addActionListener(e -> rebuild.run());
        yearSpin.addChangeListener(e -> rebuild.run());
        prev.addActionListener(e -> shiftMonth(-1));
        next.addActionListener(e -> shiftMonth(1));

        pack();
        setMinimumSize(new Dimension(380, 400));
        setLocationRelativeTo(owner);
        rebuild.run();
    }

    private void shiftMonth(int delta) {
        Month m = (Month) monthBox.getSelectedItem();
        int y = (Integer) yearSpin.getValue();
        LocalDate d = LocalDate.of(y, m, 1).plusMonths(delta);
        if (d.getYear() < 2020) {
            d = LocalDate.of(2020, 1, 1);
        }
        if (d.getYear() > 2040) {
            d = LocalDate.of(2040, 12, 1);
        }
        yearSpin.setValue(d.getYear());
        monthBox.setSelectedItem(d.getMonth());
        int max = d.lengthOfMonth();
        if (selectedDay > max) {
            selectedDay = max;
        }
        rebuildDayGrid();
    }

    private void rebuildDayGrid() {
        Month month = (Month) monthBox.getSelectedItem();
        int year = (Integer) yearSpin.getValue();
        LocalDate first = LocalDate.of(year, month, 1);
        int maxDay = first.lengthOfMonth();
        if (selectedDay > maxDay) {
            selectedDay = maxDay;
        }

        gridPanel.removeAll();
        String[] dow = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
        for (String h : dow) {
            JLabel lab = new JLabel(h, SwingConstants.CENTER);
            lab.setFont(lab.getFont().deriveFont(Font.BOLD));
            gridPanel.add(lab);
        }

        int gap = first.getDayOfWeek().getValue() - java.time.DayOfWeek.MONDAY.getValue();
        if (gap < 0) {
            gap += 7;
        }

        for (int i = 0; i < 42; i++) {
            int dayNum = i - gap + 1;
            if (i < gap || dayNum < 1 || dayNum > maxDay) {
                gridPanel.add(new JLabel());
            } else {
                final int d = dayNum;
                JButton b = new JButton(String.valueOf(d));
                b.setMargin(new Insets(6, 6, 6, 6));
                if (d == selectedDay) {
                    b.setBackground(new Color(160, 200, 255));
                }
                b.addActionListener(e -> {
                    selectedDay = d;
                    rebuildDayGrid();
                });
                gridPanel.add(b);
            }
        }
        gridPanel.revalidate();
        gridPanel.repaint();
    }

    private void applyAndClose() {
        try {
            Month month = (Month) monthBox.getSelectedItem();
            int year = (Integer) yearSpin.getValue();
            LocalDate date = LocalDate.of(year, month, selectedDay);
            int h = (Integer) hourSpin.getValue();
            int mi = (Integer) minuteSpin.getValue();
            LocalDateTime ldt = LocalDateTime.of(date, java.time.LocalTime.of(h, mi));
            targetField.setText(ScheduleDateTimePicker.DISPLAY.format(ldt));
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Invalid date: " + ex.getMessage(), "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        dispose();
    }

    static void open(Window owner, JTextField targetField) {
        SchedulePickerDialog d = new SchedulePickerDialog(owner, targetField);
        d.setVisible(true);
    }
}
