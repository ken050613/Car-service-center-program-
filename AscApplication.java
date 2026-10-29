package apu;

import apu.bootstrap.AscBootstrap;
import apu.ui.ConsoleUi;

public final class AscApplication {
    public static void main(String[] args) {
        try {
            var services = AscBootstrap.init();
            new ConsoleUi(services).run();
        } catch (Exception e) {
            System.err.println("Failed to start: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
