import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;

public class ChannelClientController {

    private ChannelClientModel clientModel;
    private ChannelClientView clientView;
    private TopicCheckBoxListener topicCheckBoxListener;

    public enum Updates {
        UPDATE_NEWS,
        ADD_TOPIC,
        DELETE_TOPIC
    }

    public ChannelClientController(String serverName, int serverPortNumber) {

        this.topicCheckBoxListener = new TopicCheckBoxListener();

        this.clientModel = new ChannelClientModel(serverName, serverPortNumber, this);
        try {
            this.clientView = new ChannelClientView(topicCheckBoxListener);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            clientModel.connect();
        } catch (IOException ioException) {
            System.out.println("UNABLE TO CONNECT TO THE SERVER.");
            System.exit(3);
        }

        try {
            clientModel.askForTopics();
        } catch (Exception exception) {
            System.out.println("Server is not available.");
        }

        if (clientModel.getChannel().isConnected()) {
            ArrayList<String> topicsFromServer = clientModel.getTopicsFromServer();
            if (!topicsFromServer.isEmpty()) {
                clientModel.saveTopics(topicsFromServer);
            }
        }

        System.out.println("TOPICS RECEIVED FROM SERVER:");
        for (String topic : clientModel.getTopics()) {
            System.out.println(topic);
        }

        clientView.feedView(clientModel.getTopics());
        clientView.showView();

        clientModel.getUpdater().start();
    }

    public void notifyController(Updates code, String data) {
        switch (code) {
            case ADD_TOPIC:
                clientView.feedView(data);
                break;
            default:
                System.out.println("Unrecognized update");
        }
    }

    class TopicCheckBoxListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            JCheckBox topicCheckBox = (JCheckBox) e.getSource();
            if (topicCheckBox.isSelected()) {
                try {
                    clientModel.subscribe(topicCheckBox.getText());
                    System.out.println("Subscribe: " + topicCheckBox.getText());
                } catch (Exception exception) {
                    System.out.println("Unable to reach the server");
                }
            }
            else {
                try {
                    clientModel.unsubscribe(topicCheckBox.getText());
                    System.out.println("Unsubscribe: " + topicCheckBox.getText());
                } catch (Exception exception) {
                    System.out.println("Unable to reach the server.");
                }
            }
        }
    }
}
