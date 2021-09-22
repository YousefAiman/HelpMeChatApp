package app.alhamad.helpmechat;

public class SavedMessage {

    private String messagingUserId;
    private String latestMessage;

    public String getMessagingUserId() {
        return messagingUserId;
    }

    public void setMessagingUserId(String messagingUserId) {
        this.messagingUserId = messagingUserId;
    }

    public String getLatestMessage() {
        return latestMessage;
    }

    public void setLatestMessage(String latestMessage) {
        this.latestMessage = latestMessage;
    }
}
