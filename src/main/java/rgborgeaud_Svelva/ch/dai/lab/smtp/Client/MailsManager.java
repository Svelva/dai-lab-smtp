package rgborgeaud_Svelva.ch.dai.lab.smtp.Client;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.json.JSONArray;
import org.json.JSONObject;

public class MailsManager {
  private static final String MESSAGES_PATH_DEFAULT_STR =
      "./src/main/java/rgborgeaud_Svelva/ch/dai/lab/smtp/Client/Config/messages.json";
  private static final String EMAILS_PATH_DEFAULT_STR =
      "./src/main/java/rgborgeaud_Svelva/ch/dai/lab/smtp/Client/Config/emails.json";

  private final int MIN_GROUP_SIZE = 2;
  private final int MAX_GROUP_SIZE = 5;

  private final JSONArray messages;
  private final JSONArray emails;
  private final int nbGroups;

  private final List<List<String>> groupsList;

  MailsManager(String messagesPath, String emailsPath) throws RuntimeException {
    // Are the parameters null ?
    File messagesFile;
    File emailsFile;
    try {
      messagesFile = setFile(messagesPath, MESSAGES_PATH_DEFAULT_STR);
      emailsFile = setFile(emailsPath, EMAILS_PATH_DEFAULT_STR);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    // Check validity and process the files' data if all is well
    if (isValidFile(messagesFile)) {
      // Extract the messages from the file
      this.messages = parseFileToArr(messagesFile);
    } else {
      throw new RuntimeException("Messages file error.");
    }
    if (isValidFile(emailsFile)) {
      // Extract the emails from the file
      final JSONObject emailsData = parseFileToObj(emailsFile);
      this.emails = emailsData.getJSONArray("emails");
      // Integrity check on emails
      for (Object email : this.emails) {
        if (!(email instanceof String) || !((String) email).contains("@") || !((String) email).contains(".") || ((String) email).isBlank()) {
          throw new RuntimeException("Wrong email found in emailsFile : " + email);
        }
      }
      this.nbGroups = emailsData.getInt("nbGroups");
      // Integrity check on groups count
      if (this.nbGroups < 1) {
        throw new RuntimeException("Number of groups must be greater than 0");
      }

      // Instantiate groupsList
      this.groupsList = new ArrayList<>();
      // Generate the groups
      this.generateGroups();

    } else {
        throw new RuntimeException("Emails file error.");
    }
  }

  private void generateGroups() throws RuntimeException {
    // Instantiate random generator
    final Random random = new Random();

    // Generate the groups
    for (int group = 0; group < nbGroups; group++) {
      // Select the first mail address, which will be the sender
      final String sender = emails.getString(random.nextInt(emails.length()));

      // Define the group's size
      final int randomSize = random.nextInt(MIN_GROUP_SIZE, MAX_GROUP_SIZE + 1);

      final int groupSize = Math.min(randomSize, emails.length());

      // Insert data
      final List<String> groupMembers = new ArrayList<>(groupSize);
      groupMembers.add(sender);

      while (groupMembers.size() < groupSize) {
        final String member = emails.getString(random.nextInt(groupSize));
        if (!member.equals(sender) && !groupMembers.contains(member)) {
          groupMembers.add(member);
        }
      }

      groupsList.add(groupMembers);
    }
  }

  public List<String> getRandomGroup() {
    final List<String> selectedGroup = this.groupsList.get(new Random().nextInt(this.groupsList.size()));
    this.groupsList.remove(selectedGroup);
    return selectedGroup;
  }

  public List<String> getRandomMessage() {
    final List<String> headerAndMsg = new ArrayList<>();
    final JSONObject selectedMessage = this.messages.getJSONObject(new Random().nextInt(this.messages.length()));
    headerAndMsg.add(selectedMessage.getString("subject"));
    headerAndMsg.add(selectedMessage.getString("body"));
    return headerAndMsg;
  }

  public boolean noGroupLeft() {
    return this.groupsList.isEmpty();
  }

  private JSONObject parseFileToObj(File dataFile) {
    try {
      return new JSONObject(Files.readString(dataFile.toPath()));
    } catch (Exception e) {
      throw new RuntimeException("Something went wrong when parsing file " + dataFile, e);
    }
  }

  private JSONArray parseFileToArr(File dataFile) {
    try {
      return new JSONArray(Files.readString(dataFile.toPath()));
    } catch (Exception e) {
      throw new RuntimeException("Something went wrong when parsing file " + dataFile, e);
    }
  }

  private boolean isValidFile(File filePath) throws RuntimeException {
    // Path must exist and be valid
    return filePath != null && filePath.exists();
  }

  private File setFile(String filePath, String defaultPath) throws IOException {
    if (filePath == null) {
      return new File(defaultPath).getCanonicalFile();
    } else {
      return new File(filePath).getCanonicalFile();
    }
  }
}
