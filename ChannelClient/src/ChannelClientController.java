import java.io.IOException;
import java.util.ArrayList;

public class ChannelClientController {

    private ChannelClientModel clientModel;

    public ChannelClientController(String serverName, int serverPortNumber) {

        clientModel = new ChannelClientModel(serverName, serverPortNumber);

        try {
            clientModel.connect();
        } catch (IOException ioException) {
            System.out.println("UNABLE TO CONNECT TO THE SERVER.");
            System.exit(3);
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


    }
}
