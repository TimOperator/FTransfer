/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.hamp_it.ftransfer;

/**
 *
 * @author Tim
 */
public class Client {
    private static MainFrame mainFrame;
    
    public static void main(String args[]) {
        mainFrame = new MainFrame();
        mainFrame.setVisible(true);
    }
}
