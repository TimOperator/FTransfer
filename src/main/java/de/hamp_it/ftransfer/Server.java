/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.hamp_it.ftransfer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import javax.swing.JOptionPane;

/**
 *
 * @author Tim
 */
public class Server extends Thread {
    private int port;
    private ServerSocket serverSocket;
    private Socket socket;
    
    public Server(int port) throws IOException {
        this.port = port;
        this.serverSocket = new ServerSocket(port); 
        
    }
    
    public void openConnection() {
        try {
            // Ask user first!
            socket = serverSocket.accept();
            
            int choise = JOptionPane.showConfirmDialog(null, socket.getInetAddress().getHostAddress() + " möchte Verbindung aufbauen, akzeptieren?");
            if (choise != JOptionPane.YES_OPTION) {
                serverSocket.close();
                return;
            }
            
            DataInputStream dataIn = new DataInputStream(socket.getInputStream());
            DataOutputStream dataOut = new DataOutputStream(socket.getOutputStream());
            dataOut.writeUTF("y");
            String message;
            boolean connect = true;
            while (connect) {
                message = dataIn.readUTF();
                if (message.startsWith("msg:")) {
                    System.out.println("Nachricht von " + socket.getInetAddress().getHostAddress() + ":" + socket.getPort() + "\r\n" + message.substring(4));
                    JOptionPane.showMessageDialog(null, "Nachricht von " + socket.getInetAddress().getHostAddress() + ":\r\n" + message.substring(4));
                    dataOut.writeUTF("y");
                } else if (message.equals("disconnect")) {
                    connect = false;
                }
            }
            socket.close();
        } catch (IOException ex) {
        }
    }
    
    @Override
    public void run() {
        while (true) {
            openConnection();
            try {
                Thread.sleep(500);
            } catch (InterruptedException ex) {
                //
            }
        }
    }
}
