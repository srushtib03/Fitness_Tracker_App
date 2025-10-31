package fitness;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        // Set Metal look and feel to enable colored buttons on macOS
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        
        SwingUtilities.invokeLater(() -> {
           
            new LoginPanel(); 
        });
    }
}
