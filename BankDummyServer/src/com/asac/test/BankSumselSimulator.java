/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.asac.test;

import com.function.StringFunction;
import static com.function.StringFunction.generateHexFromMsgLength;
import com.muamalat.iso8583.PackagerFactory;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.codec.binary.Hex;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;

/**
 *
 * @author herry.suganda
 */
public class BankSumselSimulator extends Thread {

    private String message;

    @Override
    @SuppressWarnings("static-access")
    public void run() {
        // mendengarkan message yang akan dikirimkan oleh server
        int intData;
        boolean bRec = false;
        StringBuffer s = new StringBuffer("");
        int jmlMsg = 0;
        while (true) {
            try {
                while (true) {
                    System.out.println("(CLIENT)s : " + s);
                    message = "";
                    if (BankServer.getInstance().getIn() != null) {
                        bRec = false;
                        s.delete(0, s.length());
                        System.out.println("(CLIENT)ready for listener message from client");
                        BankServer.getInstance();
                        message = grabMessageHeaderLengthHexaByte();
                        jmlMsg++;
                        System.out.print("(CLIENT)From ASAC Socket Bank " + StringFunction.getCurrentDateForLog() + " : " + jmlMsg + " : " + message);
                        System.out.println(message);
                        if (message.equals("connection down")) {
                            BankServer.getInstance().destroy();
                        } else {
                            if (!message.equals("")) {
                                if (message.substring(0, 3).equals("ISO")) {
                                    message = message.substring(12);
                                }
                                ISOMsg isoMsg = unpackRequest(message);
                                if (isoMsg.getMTI().equals("0200")) {
                                    if (isoMsg.getString(3).equals("300000")) {
                                        isoMsg.setMTI("0210");
                                        isoMsg.set(39, "00");
                                        isoMsg.set(48, "160                    ADI WIBOWO          JL. PANGLIMA POLIM CIPONDOH PORIS PLAWAD                     TANGERANG                  NAMA WP       NAMA JENIS SETORAN");
                                        isoMsg.set(54, "1000360C000100000000");
                                        isoMsg.set(121, isoMsg.getString(121));
//                                    isoMsg.set(112, "F042C310T240886234008000C407T008001C907T000001C104T001CB0BT411121|110");
                                    } else if (isoMsg.getString(3).equals("200000")) {
                                        isoMsg.setMTI("0210");
                                        isoMsg.set(39, "00");
                                        isoMsg.set(48, "0642408862340080000411121110040520161234567890123450987654321098765");
                                        isoMsg.set(54, "1000360C000100000000");
                                        isoMsg.set(121, isoMsg.getString(121));
                                    } else if (isoMsg.getString(3).equals("200001")) {
                                        isoMsg.setMTI("0210");
                                        isoMsg.set(39, "00");
                                        isoMsg.set(48, "HERRY SUGANDA");
                                        isoMsg.set(54, "1000360C000100000000");
                                    } else if (isoMsg.getString(3).equals("500000")) {
                                        isoMsg.setMTI("0210");
                                        isoMsg.set(39, "00");
                                        isoMsg.set(48, "HERRY SUGANDA");
                                        isoMsg.set(54, "1000360C000100000000");
                                    }
                                    String txt = new String(isoMsg.pack());
                                    sendMessage("ISO011000017" + txt);
                                }
                                if (isoMsg.getMTI().equals("0800")) {
                                    isoMsg.setMTI("0810");
                                    isoMsg.set(39, "00");
                                    String txt = "ISO005000070" + new String(isoMsg.pack());
                                    System.out.println("(CLIENT)resp : " + txt);
                                    sendMessage(txt);
                                }
                            }
                        }
                    }
                }
            } catch (Exception ex) {
                Logger.getLogger(BankSumselSimulator.class.getName()).log(Level.SEVERE, null, ex);
            } finally {

                BankServer.getInstance().destroy();
            }

        }

    }

    public ISOMsg unpackRequest(String message) throws ISOException, Exception {
        ISOMsg isoMsg = new ISOMsg();
        isoMsg.setPackager(PackagerFactory.getPackager());
        isoMsg.unpack(message.getBytes());
        return isoMsg;
    }

    public ISOMsg unpackRequest(byte[] message) throws ISOException, Exception {
        ISOMsg isoMsg = new ISOMsg();
        isoMsg.setPackager(PackagerFactory.getPackager());
        isoMsg.unpack(message);
        return isoMsg;
    }

    private void sendMessage(String msg) {
        boolean sendingSuccess = false;
        try {
            if (BankServer.getInstance().getOut() != null) {
                System.out.println(msg);
//                BankServer.getInstance().getOut().write((digitHeader(msg)).getBytes());
                BankServer.getInstance().getOut().write(HeaderMessage.hexaDigitHeader(false, msg).getBytes());
                BankServer.getInstance().getOut().flush();
            }
        } catch (IOException ioException) {
        }
    }

    public static String digitHeader(String msg) {
        return StringFunction.pad(String.valueOf(msg.length()), 4, "0", "Right") + msg;
    }

    private String grabMessageHeaderLengthByte() {
        int intData;
        boolean bRec = false;
        StringBuffer s = new StringBuffer("");
        try {
            s.delete(0, s.length());
//            System.out.println("listen data from socket");
            intData = BankServer.getInstance().getIn().read();
//            System.out.println("get listen data from socket");
            if (intData < 0) {
//                RebuildSocketConnection.rebuildConnection(sce);
                return "connection down";
            } else {
                s.append((char) intData);
                for (int i = 0; i < 3; i++) {
                    intData = BankServer.getInstance().getIn().read();
                    s.append((char) intData);
                }
                int msgLength = Integer.valueOf(s.toString());
                s.delete(0, s.length());
                for (int i = 0; i < msgLength; i++) {
                    intData = BankServer.getInstance().getIn().read();
                    s.append((char) intData);
                }
            }
        } catch (IOException ex) {
//            log.error("IOException grabMessageHeaderLengthByte : " + ex.getMessage());
//            if (!sce.isStatusStart()) {
//                killThread = true;
//            }else{
//                killThread = false;
//            }
//            System.out.println("killthread : " + killThread);
//            ex.printStackTrace();
//            return "connection down";
//        } catch (Exception ex) {
//            log.error("Exception grabMessageHeaderLengthByte : " + ex.getMessage());
            ex.printStackTrace();
//            return "connection down";
        }
//        log.info(Thread.currentThread().getId() + " - port " + port + " - " + termID + " grabMessageHeaderLengthByte : " + s.toString());
//        System.out.println(Thread.currentThread().getId() + " - " + port + " - " + termID + " grabMessageHeaderLengthByte : " + s.toString());
        return s.toString();
    }

    private String grabMessageHeaderLengthHexaByte() {
        int intData;
        int msgLength = 0;
        StringBuffer s = new StringBuffer("");
        BigInteger value;
        try {
            s.delete(0, s.length());
//            System.out.println("listen data from siskohat");
            intData = BankServer.getInstance().getIn().read();
            System.out.println("(CLIENT)intData : " + intData + " : -" + String.valueOf((char) intData) + "-");
            msgLength += intData;
            intData = BankServer.getInstance().getIn().read();
            System.out.println("(CLIENT)intData : " + intData + " : -" + String.valueOf((char) intData) + "-");
            msgLength += intData;

            if (intData < 0) {
                return "connection down";
            } else {
                value = new BigInteger(Hex.encodeHexString(String.valueOf((char) msgLength).getBytes()), 16);
                msgLength = value.intValue();
                System.out.println("(CLIENT)hexa message length : " + msgLength);
                byte[] msgbyte = new byte[msgLength];
                for (int i = 0; i < msgLength; i++) {
                    msgbyte[i] = BankServer.getInstance().getIn().readByte();
                }
                System.out.println("(CLIENT)after header : " + new String(msgbyte));
                String Messgg = new String(msgbyte);
                if (new String(msgbyte).substring(0, 3).equals("ISO")) {
                    Messgg = new String(msgbyte).substring(12);
                }
                try {

                    System.out.println(new String(msgbyte));
                    System.out.println(msgbyte.length);
                    ISOMsg iso = unpackRequest(Messgg);
                    System.out.println(iso.getMTI());
                    System.out.println(iso.getString(7));
                    System.out.println(iso.getString(11));
                    System.out.println(iso.getString(48));
                    System.out.println(iso.getString(70));
                    return new String(msgbyte);
                } catch (Exception ex) {
                }

            }
        } catch (IOException ex) {
            return "connection down";
        } finally {
        }
        System.out.println("(CLIENT)grabMessageHeaderLengthHexaByte : " + s.toString());
        return s.toString();
    }
}
