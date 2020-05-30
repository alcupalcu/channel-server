import java.io.IOException;

public class ChannelAdminController {

    private ChannelAdminModel adminModel;

    public ChannelAdminController(String serverName, int serverPortNumber) {

        this.adminModel = new ChannelAdminModel(serverName, serverPortNumber);

        try {
            adminModel.connect();
        } catch (IOException exc) {
            System.out.println("Unable to connect to the server.");
            System.exit(1);
        }

        try {
            adminModel.goAsAdmin();
            adminModel.addTopic("UNEXPECTED");
            Thread.sleep(1000);
            adminModel.addTopic("EXPECTED");
        } catch (Exception exc) {
            System.out.println("Unable to connect to the server");
            System.exit(2);
        }

        while(true) {

        }
    }
}
