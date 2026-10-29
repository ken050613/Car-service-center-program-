package apu.gui;

/**
 * Combo box entry: stable {@link #id()} for services, {@link #label()} for display.
 */
public record IdLabelOption(String id, String label) {
    @Override
    public String toString() {
        return label;
    }
}
