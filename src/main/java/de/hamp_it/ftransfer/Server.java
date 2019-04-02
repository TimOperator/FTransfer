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
import java.util.Locale;
import java.util.ResourceBundle;
import javax.swing.JOptionPane;

/**
 *
 * @author Tim
 */
public class Server extends Thread {
    private final int port;
    private final ServerSocket serverSocket;
    private Socket socket;
    private final ResourceBundle message_bundle;
    private final ResourceBundle string_bundle;
    
    public Server(int port) throws IOException {
        this.port = port;
        this.serverSocket = new ServerSocket(port); 
        message_bundle = ResourceBundle.getBundle("messages", Locale.getDefault());
        string_bundle = ResourceBundle.getBundle("strings", Locale.getDefault());
        
    }
    
    public void openConnection() {
        try {
            // Ask user first!
            socket = serverSocket.accept();
            
            int choise = JOptionPane.showConfirmDialog(null, socket.getInetAddress().getHostAddress() + " " + message_bundle.getString("server_client_request"));
            if (choise != JOptionPane.YES_OPTION) {
                serverSocket.close();
                return;
            }
            
            DataInputStream dataIn = new DataInputStream(socket.getInputStream());
            DataOutputStream dataOut = new DataOutputStream(socket.getOutputStream());
            dataOut.writeBoolean(true);
            System.out.println(message_bundle.getString("server_connected_to") + " " + socket.getInetAddress().getHostAddress());
            String message;
            boolean connect = true;
            while (connect) {
                message = dataIn.readUTF();
                if (message.startsWith("msg:")) {
                    System.out.println(message_bundle.getString("server_message_from") + " " + socket.getInetAddress().getHostAddress() + ":" + socket.getPort() + "\r\n" + message.substring(4));
                    JOptionPane.showMessageDialog(null, message_bundle.getString("server_message_from") + " " + socket.getInetAddress().getHostAddress() + ":\r\n" + message.substring(4));
                    dataOut.writeBoolean(true);
                } else if (message.startsWith("file:")) {
                    String filename = message.substring(5);
                    
                    // Get file size
                    long fileSize = dataIn.readLong();
                    
                    // Ask user for file accept
                    System.out.println(message_bundle.getString("server_file_from") + " " + socket.getInetAddress().getHostAddress() + ":" + socket.getPort() + " (" + fileSize + " " + message_bundle.getString("file_bytes") + "):");
                    System.out.println(filename);
                    int accept = JOptionPane.showConfirmDialog(null, message_bundle.getString("server_confirm_file") + " " + socket.getInetAddress().getHostAddress() + "? (" + fileSize + " " + message_bundle.getString("file_bytes") + ")\r\n" + filename);
                    if (accept == JOptionPane.YES_OPTION) {
                        dataOut.writeBoolean(true);
                        
                        // Receive file size
                        int length = dataIn.readInt();
                        try {
                            byte[] fileArray = new byte[length];
                            
                            // Receive file
                            dataIn.read(fileArray);
                            
                            System.out.println(message_bundle.getString("server_receive_file"));
                            writeBytesToFile(fileArray, filename);
                            System.out.println(message_bundle.getString("server_file_saved"));
                            
                            // Send success to client
                            dataOut.writeBoolean(true);
                            
                        } catch (FileNotFoundException ex) {
                            System.out.println(message_bundle.getString("server_could_not_find_file"));
                            dataOut.writeBoolean(false);
                        } catch (NegativeArraySizeException ex) {
                            System.out.println(message_bundle.getString("server_file_error"));
                            dataOut.writeBoolean(false);
                        } catch (FileAlreadyExistsException ex) {
                            System.out.println(message_bundle.getString("server_file_save_error") + ", " + message_bundle.getString("server_file_already_exists"));
                            JOptionPane.showMessageDialog(null, message_bundle.getString("server_file_save_error") + " (" + filename + ")\n" + message_bundle.getString("server_file_already_exists") + "!");
                            dataOut.writeBoolean(false);
                        }
                    } else {
                        dataOut.writeBoolean(false);
                    }
                } else if (message.equals("disconnect")) {
                    connect = false;
                }
            }
            socket.close();
        } catch (IOException ex) {
            try {
                socket.close();
            } catch (IOException ex1) {
                System.out.println(message_bundle.getString("server_could_not_close_socket"));
            }
            System.out.println(ex.toString());
            System.out.println(message_bundle.getString("server_restarted"));
        }
    }
    
    @Override
    public void run() {
        System.out.println(message_bundle.getString("server_started"));
        System.out.println(message_bundle.getString("server_address") + ": " + serverSocket.getLocalSocketAddress().toString());
        System.out.println(message_bundle.getString("server_port") + ": " + port);
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
            throw new FileAlreadyExistsException(message_bundle.getString("server_file_already_exists"));
        } else {
            file.createNewFile();
            file.setExecutable(true);
            file.setReadable(true);
            file.setWritable(true);
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(fileArray);
            fos.close();
        }
    }
}
