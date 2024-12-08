package rgborgeaud_Svelva.ch.dai.lab.smtp.Client;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;

/**
 * This class handles the connection to an SMTP server through in and out streams of data.
 * It generates messages according to the SMTPMessagesHandler object it receives as an argument.
 */
public class SMTPConnectionHandler {

    BufferedReader in;
    BufferedWriter out;
    SMTPMessagesHandler SMTPMessages;

    static final String REQUESTED_ACTION_OK = "250";
    static final String END_OF_OPTION_LIST = "250 ";
    static final String SERVICE_READY = "220";
    static final String START_MAIL_INPUT = "354";
    static final String BYE = "221";

    public SMTPConnectionHandler(BufferedReader in, BufferedWriter out, SMTPMessagesHandler SMTPMessages) {
        this.out = out;
        this.in = in;
        this.SMTPMessages = SMTPMessages;
    }

    /**
     * Sends message to server, with SMTP formatting
     * @param message the message to send
     * @throws IOException when write() fails
     */
    private void sendMessage(String message) throws IOException {
        out.write(message);
        out.flush();
    }

    /**
     * Checks whether the server answers with the expected confirmation code
     * @param expectedCode the expected code
     * @throws IOException if readLine() fails or if the server's response is not the expected confirmation output
     */
    private void checkResponse(String expectedCode, String errorMessage) throws IOException {
        String serverResponse = in.readLine();
        if (!serverResponse.startsWith(expectedCode)) {
            throw new IOException(errorMessage + " : " + serverResponse);
        }
        System.out.println("Server response : " + serverResponse);
    }

    /**
     * Reads the first line sent by SMTP server after the connection is first established, and greets the server
     * with adequate message from SMTPMessagesHandler
     * @throws IOException on checkResponse()
     */
    private void greetingsManagement() throws IOException {
        sendMessage(SMTPMessages.writeGreetings());
        checkResponse(REQUESTED_ACTION_OK, "Server refused EHLO");

        System.out.println("Connection established.");
    }

    /**
     * Reads options displayed by the SMTP server after initial EHLO command.
     * For the purpose of this Lab, the function completely ignores given options, but could be modified to store them.
     * @throws IOException when server sends unexpected message or on readLine() error
     */
    private void readOptions() throws IOException {
        String serverResponse;
        while (!(serverResponse = in.readLine()).startsWith(END_OF_OPTION_LIST)) {
            if (!serverResponse.startsWith(REQUESTED_ACTION_OK)) throw new IOException("Error when displaying options : "
                    + serverResponse);
        }
        System.out.println("Options read");
    }

    /**
     * Sets sender for current email
     * @throws IOException on checkResponse()
     */
    private void setSender() throws IOException {
        sendMessage(SMTPMessages.writeSender());
        checkResponse(REQUESTED_ACTION_OK, "Error when setting sender");

        System.out.println("Sender in set");
    }

    /**
     * Sets recipients for current email
     * @throws IOException on checkResponse()
     */
    private void setRecipient() throws IOException {

        String[] recipientsList = SMTPMessages.writeRecipients();

        for (String recipient : recipientsList) {
            sendMessage(recipient);
            checkResponse(REQUESTED_ACTION_OK, "Error when setting recipients");
        }

        System.out.println("Recipients set");
    }

    /**
     * Sends quit message to server
     * @throws IOException if server fails to end the connection
     */
    private void quit() throws IOException {
        sendMessage(SMTPMessages.writeQuit());

        checkResponse(BYE, "Server failed to end connection");
        System.out.println("Connection closed");
    }

    /**
     * Sets the data field for current email
     * @throws IOException if beginning or end of data transfer fails
     */
    private void setData() throws IOException {
        sendMessage(SMTPMessages.writeDataMessage());
        checkResponse(START_MAIL_INPUT, "Error when asking for data input");

        sendMessage(SMTPMessages.writeDataContent());
        checkResponse(REQUESTED_ACTION_OK, "Error when reading data information");

        System.out.println("Data is set");
    }

    /**
     * Handles whole SMTP protocol for sending one email to multiple addresses
     * @throws IOException if server fails at any point, or when read()/write() fails
     */
    public void handle() throws IOException {
        checkResponse(SERVICE_READY, "Service not ready");
        while (this.SMTPMessages.prepareNext()) {
            greetingsManagement();
            readOptions();
            setSender();
            setRecipient();
            setData();
        }
        quit();
    }
}

