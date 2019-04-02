/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.hamp_it.ftransfer;

import java.io.IOException;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

/**
 *
 * @author Tim
 */
public class Client {
    private static MainFrame mainFrame;
    private static Server server;
    private static int port = 50471;
    
    public static void main(String args[]) {
        if (args.length > 0) {
            Integer tmp = new Integer(args[0]);
            port = tmp;
            System.out.println("Using port " + port);
        }

        // Start server
        try {
            server = new Server(port);
        } catch (IOException ex) {
            System.out.println("Unable to start server: " + ex.toString());
        }
        server.start();
        
        // Set look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
            //Doesn't matter
        }
        mainFrame = new MainFrame();
        mainFrame.setLocationRelativeTo(null);
        mainFrame.setVisible(true);
        
        
    }
}
