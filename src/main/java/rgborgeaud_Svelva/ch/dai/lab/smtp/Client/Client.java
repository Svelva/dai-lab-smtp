package rgborgeaud_Svelva.ch.dai.lab.smtp.Client;


import java.io.*;
import java.net.Socket;

import static java.nio.charset.StandardCharsets.UTF_8;

public class Client {

    static final int MAIL_DEV_PORT = 1025;
    static final String MAIL_DEV_HOST = "localhost";

    public static void main(String[] args) {

        try (Socket socket = new Socket(MAIL_DEV_HOST, MAIL_DEV_PORT);
             BufferedReader in = new BufferedReader(
                     new InputStreamReader(socket.getInputStream(), UTF_8));
             BufferedWriter out = new BufferedWriter(
                     new OutputStreamWriter(socket.getOutputStream(), UTF_8))
        ) {

            String sender = "gael.borgeaud@gmail.com";
            String[] recipients = {"mylene.farmer@baguette.fr", "johnny.hallyday@pinard.com", "roger.federer@fondue.ch",};
            String subjet = "Truite de compagnie à vendre";
            String text = "Contactez-moi au plus vite et évitez, à partir d'aujourd'hui, les caractères spéciaux " +
                    "tels que : ééààèèüüääöö!!££$$¨^^@##|¬&, merci d'avance";

            var smtpMessages = new SMTPMessagesHandler(sender, recipients, subjet, text);
            var smtpConnection = new SMTPConnectionHandler(in, out, smtpMessages);

            smtpConnection.handle();

        } catch (IOException e) {
            System.out.println("Error : " + e);
        }
    }
}