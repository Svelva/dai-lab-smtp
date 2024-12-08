package rgborgeaud_Svelva.ch.dai.lab.smtp.Client;


import java.io.*;
import java.net.Socket;

import static java.nio.charset.StandardCharsets.UTF_8;

public class Client {

    static final int MAIL_DEV_PORT = 1025;
    static final String MAIL_DEV_HOST = "localhost";

    public static void main(String[] args) {
        // Instantiate message and mails manager
        final MailsManager mailsManager = new MailsManager(null, null);

        try (Socket socket = new Socket(MAIL_DEV_HOST, MAIL_DEV_PORT);
             BufferedReader in = new BufferedReader(
                     new InputStreamReader(socket.getInputStream(), UTF_8));
             BufferedWriter out = new BufferedWriter(
                     new OutputStreamWriter(socket.getOutputStream(), UTF_8))
        ) {
            SMTPMessagesHandler smtpMessages = new SMTPMessagesHandler(mailsManager);
            SMTPConnectionHandler smtpConnection = new SMTPConnectionHandler(in, out, smtpMessages);

            smtpConnection.handle();

        } catch (IOException e) {
            System.out.println("Error : " + e);
        }
    }
}