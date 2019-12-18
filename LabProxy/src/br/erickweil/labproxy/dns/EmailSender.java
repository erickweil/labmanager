/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.erickweil.labproxy.dns;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import javax.mail.*;
import javax.mail.internet.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EmailSender {

    final String senderEmail = "registroacessos.guajara@ifro.edu.br";
    final String senderPassword = "naoireicontarparaninguem";
    final String emailSMTPserver = "smtp.gmail.com";
    final String emailServerPort = "465";
    String receiverEmail = null;
    String emailSubject = null;
    String emailBody = null;
    
    public static volatile String last_site = "www.google.com";

    public EmailSender(String receiverEmail, String Subject, String message) {
        this.receiverEmail = receiverEmail;
        this.emailSubject = Subject;
        this.emailBody = message;

        // Get system properties
        //Properties props = System.getProperties();
        Properties props = new Properties();
        props.put("mail.smtp.user", senderEmail);
        props.put("mail.smtp.host", emailSMTPserver);
        props.put("mail.smtp.port", emailServerPort);
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.socketFactory.port", emailServerPort);
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");

        SecurityManager security = System.getSecurityManager();

        try {
            Authenticator auth = new SMTPAuthenticator();
            Session session = Session.getInstance(props, auth);

            Message msg = new MimeMessage(session);
            //msg.setText(emailBody);
            msg.setContent(emailBody, "text/html; charset=utf-8");
            msg.setSubject(emailSubject);
            msg.setFrom(new InternetAddress(senderEmail));
            
            msg.addRecipient(Message.RecipientType.TO,
                    new InternetAddress(receiverEmail));
            Transport.send(msg);
            System.out.println("send successfully");
        } catch (Exception ex) {
            ex.printStackTrace();
            System.err.println("Error occurred while sending.!");
        }

    }

    private class SMTPAuthenticator extends javax.mail.Authenticator {

        public PasswordAuthentication getPasswordAuthentication() {
            return new PasswordAuthentication(senderEmail, senderPassword);
        }
    }

    public static void main(String[] args) throws UnknownHostException {
        registerBlock("www.test.com",InetAddress.getLoopbackAddress(),"Realtek","","OK","","erickweil2@gmail.com");
        //EmailSender send = new EmailSender("erickweil2@gmail.com", "TESTE", "funfou");
    }
    
    public static void registerBlock(String site,InetAddress _ip,String interface_id,String room_name,String reason, String started_time, final String email) {
        if(site.equals(last_site)) return;
        
        last_site = site;
        try {
            String IP = _ip.getHostAddress();
            
            String MAC;
            byte[] mac = null;
            if(_ip.isLoopbackAddress())
            {
                Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
                while(networkInterfaces.hasMoreElements())
                {
                    NetworkInterface net = networkInterfaces.nextElement();
                    if(net.isVirtual() || !net.isUp() || net.isLoopback()) continue;
                    System.out.println(net.getDisplayName()+":"+net.getInetAddresses().nextElement().getHostAddress());
                    if(net.getDisplayName().toLowerCase().contains(interface_id.toLowerCase()))
                    {
                        IP = net.getInetAddresses().nextElement().getHostAddress();
                        mac = net.getHardwareAddress();
                    }
                }
                if(mac == null)
                {
                    networkInterfaces = NetworkInterface.getNetworkInterfaces();
                    while(networkInterfaces.hasMoreElements())
                    {
                        NetworkInterface net = networkInterfaces.nextElement();
                        if(net.isVirtual() || !net.isUp() || net.isLoopback()) continue;
                        String disp = net.getDisplayName().toLowerCase();
                        if(disp.contains("hamachi") || disp.contains("virtualbox")) continue;
                        System.out.println(net.getDisplayName()+":"+net.getInetAddresses().nextElement().getHostAddress());
                        IP = net.getInetAddresses().nextElement().getHostAddress();
                        mac = net.getHardwareAddress();
                        break;
                    }
                }
            }
            else
            {
                
            }
            
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < mac.length; i++) {
                sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : ""));
            }

            MAC = sb.toString();
            
            final String title =IP+":"+site;
            
            final String msg = "Site bloqueado: <b>"+site+"</b><br/>"
                    +"Motivo: <b>"+reason+"</b><br/>"
                    +"IP <b>"+IP+"</b> MAC <b>"+MAC+"</b><br/>"
                    +"Local: <b>"+room_name+"</b>"
                    +"Computador ligado desde: <b>"+started_time+"</b>"
                    ;
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    EmailSender send = new EmailSender(email, title, msg);
                    //System.out.println(msg);
                }
            });
            t.start();
        } catch (SocketException ex) {
            Logger.getLogger(EmailSender.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }

}