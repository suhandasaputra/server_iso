/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.asac.test;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import org.jpos.iso.ISOException;

/**
 *
 * @author herry.suganda
 */
public final class ABDServer {

    public static ABDServer getInstance() throws ISOException {
        if (ref == null) {
            setRef(new ABDServer());

        }
        return ref;
    }

    /**
     * @param aRef the ref to set
     */
    public static void setRef(ABDServer aRef) {
        ref = aRef;
    }
    private ServerSocket providerSocket;
    private Socket connection;
    private DataOutputStream out;
    private DataInputStream in;

    public ABDServer() throws ISOException {
        TurnOnConnection();
    }

    public void TurnOnConnection() throws ISOException {
        try {
            System.out.println("ABD socket");
            //1. creating a server socket
            //untuk port kiblat
            setProviderSocket(new ServerSocket(11100, 10));
//            //2. Wait for connection
            System.out.println("Waiting for connection");
            connection = getProviderSocket().accept();
            System.out.println("Connection received from " + getConnection().getPort() + " " + getConnection().getInetAddress().getHostName());
            //3. get Input and Output streams
            setOut(new DataOutputStream(getConnection().getOutputStream()));
            getOut().flush();
            setIn(new DataInputStream(getConnection().getInputStream()));
            System.out.println("ABD Server Ready : " + this.getClass().getName());
        } catch (IOException ioException) {
        }
    }

    public void sendMessage(String msg) {
        try {
            getOut().write(HeaderMessage.hexaDigitHeader(false, msg).getBytes());
            getOut().flush();
            System.out.println("ABD SERVER SEND MSG>" + msg);
        } catch (IOException ioException) {
        }
    }
    private static ABDServer ref;
    private String message;

    /**
     * @return the in
     */
    public DataInputStream getIn() {
        return in;
    }

    /**
     * @param out the out to set
     */
    public void setOut(DataOutputStream out) {
        this.out = out;
    }

    /**
     * @param in the in to set
     */
    public void setIn(DataInputStream in) {
        this.in = in;
    }

    public void destroy() {
        try {
//            System.out.println("masuk destroy server");
            if (getIn() != null) {
                getIn().close();
            }
            if (getOut() != null) {
                getOut().close();
            }
            if (getConnection() != null) {
                getConnection().close();
            }
            if (getProviderSocket() != null) {
                getProviderSocket().close();
            }
            setRef(null);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * @return the providerSocket
     */
    public ServerSocket getProviderSocket() {
        return providerSocket;
    }

    /**
     * @param providerSocket the providerSocket to set
     */
    public void setProviderSocket(ServerSocket providerSocket) {
        this.providerSocket = providerSocket;
    }

    /**
     * @return the connection
     */
    public Socket getConnection() {
        return connection;
    }

    /**
     * @return the out
     */
    public DataOutputStream getOut() {
        return out;
    }
}
