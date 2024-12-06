package rgborgeaud_Svelva.ch.dai.lab.smtp.Client;

import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;

/**
 * This class handles the creation of SMTP messages used to send emails through a TCP connection.
 * It is used to create all required commands (mail from, rcpt to, data) for one email with one or multiple recipients
 * Email addresses format should be checked before being passed to the class
 *
 * The whole class uses UTF-8 encoding whenever necessary
 */
public class SMTPMessagesHandler {

    private final String sender;
    private final String[] recipients;
    private final String subject;
    private final String text;

    //SMTP uses \r\n as EOL
    static final String EOL = "\r\n";
    static final String EHLO = "EHLO ";
    static final String MAIL_FROM = "MAIL FROM:";
    static final String RCPT_TO = "RCPT TO:";
    static final String DATA = "DATA";
    static final String DATA_TERMINATION = "\r\n.\r\n";
    static final String QUIT = "QUIT";
    static final String DATA_ENCODING = "Content-Type: text/plain; charset=utf-8" + EOL;

    /**
     *
     * @param sender the sender from which the fake email is sent
     * @param recipients list of recipients
     * @param subject subject of the email
     * @param text main text of the email
     */
    public SMTPMessagesHandler(String sender, String[] recipients, String subject, String text) {
        this.sender = sender;
        this.recipients = recipients;
        this.subject = subject;
        this.text = text;
    }

    /**
     * @return the number of recipients
     */
    private int recipientsLength() {
        return recipients.length;
    }

    /**
     * Creates greeting message
     * @return EHLO command with domain name of the sender's address
     */
    public String writeGreetings() {
        return EHLO + sender.substring(sender.indexOf('@') + 1) + EOL;
    }

    /**
     * Creates sender message
     * @return Mail from command with the sender's address
     */
    public String writeSender() {
        return MAIL_FROM + "<" + sender + ">" + EOL;
    }

    /**
     * Create an array of commands specifying the recipient(s) of the email
     * @return the array of 'rcpt to' commands
     */
    public String[] writeRecipients() {
        String[] result = new String[recipientsLength()];

        for(int i = 0; i < recipientsLength(); ++i) {
            result[i] = RCPT_TO + "<" + recipients[i] + ">" + EOL;
        }
        return result;
    }

    /**
     * Writes message to initiate data input in the smtp transaction
     * @return the correctly formatted message
     */
    public String writeDataMessage() {
        return DATA + EOL;
    }

    /**
     * Creates the String to be sent as the 'data' field of the smtp input
     * @return the correctly formatted String
     */
    public String writeDataContent() {
        return String.format(
                "From:<%s>" + EOL
                + createDataRecipients()
                + "Date:%s" + EOL
                + "Subject:%s" + EOL
                + DATA_ENCODING + EOL
                + text + DATA_TERMINATION
                , sender, new SimpleDateFormat("dd-MM-yyyy").format(new Date()), encodeTextToUTF8(subject)
        );
    }

    /**
     * Create the string to add in the 'To:<>' field depending on the number of recipients
     * @return the correctly formatted string
     */
    private String createDataRecipients() {
        StringBuilder result = new StringBuilder();

        for (String recipient : recipients) {
            result.append("To:<").append(recipient).append(">").append(EOL);
        }
        return result.toString();
    }

    /**
     * Writes quit message to end smtp connection
     * @return the correctly formatted message
     */
    public String writeQuit() {
        return QUIT + EOL;
    }

    /**
     * Adds correct formatting and encoding to given text, allowing for correct decoding when an email containing
     * special characters is displayed.
     * @param text the text to encode
     * @return the encoded text as a string
     */
    private String encodeTextToUTF8(String text) {
        return "=?utf-8?B?" + Base64.getEncoder().encodeToString(text.getBytes()) + "?=";
    }
}
