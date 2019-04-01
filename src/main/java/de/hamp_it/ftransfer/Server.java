/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.hamp_it.ftransfer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.FileAlreadyExistsException;
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
            System.out.println("Verbindung aufgebaut mit " + socket.getInetAddress().getHostAddress());
            String message;
            boolean connect = true;
            while (connect) {
                message = dataIn.readUTF();
                if (message.startsWith("msg:")) {
                    System.out.println("Nachricht von " + socket.getInetAddress().getHostAddress() + ":" + socket.getPort() + "\r\n" + message.substring(4));
                    JOptionPane.showMessageDialog(null, "Nachricht von " + socket.getInetAddress().getHostAddress() + ":\r\n" + message.substring(4));
                    dataOut.writeUTF("y");
                } else if (message.startsWith("file:")) {
                    String filename = message.substring(5);
                    System.out.println("Datei von " + socket.getInetAddress().getHostAddress() + ":" + socket.getPort() + "\r\n" + filename);
                    int accept = JOptionPane.showConfirmDialog(null, "Datei annehmen von " + socket.getInetAddress().getHostAddress() + "?\r\n" + filename);
                    if (accept == JOptionPane.YES_OPTION) {
                        dataOut.writeUTF("y");
                        int length = dataIn.readInt();
                        System.out.println("Dateigröße " + length);
                        try {
                            byte[] fileArray = new byte[length];
                            dataIn.read(fileArray);
                            System.out.println("Bytestream empfangen");
                            writeBytesToFile(fileArray, filename);
                            System.out.println("Datei erstellt.");
                            dataOut.writeUTF("y");
                        } catch (FileNotFoundException ex) {
                            System.out.println("Konnte datei nicht finden.");
                            dataOut.writeUTF("n");
                        } catch (NegativeArraySizeException ex) {
                            System.out.println("Dateifehler!");
                            dataOut.writeUTF("n");
                        } catch (FileAlreadyExistsException ex) {
                            System.out.println("Datei existiert bereits!");
                            dataOut.writeUTF("n");
                        }
                    } else {
                        dataOut.writeUTF("n");
                    }
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
        System.out.println("Server gestartet " + serverSocket.getLocalSocketAddress().toString() + ":" + port);
        while (true) {
            openConnection();
            try {
                Thread.sleep(500);
            } catch (InterruptedException ex) {
                //
            }
        }
    }

    private void writeBytesToFile(byte[] fileArray, String path) throws IOException {
        File file = new File(path);
        if (file.exists()) {
            throw new FileAlreadyExistsException("Datei existiert bereits");
        } else {
            file.createNewFile();
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(fileArray);
            fos.close();
        }
    }
}
