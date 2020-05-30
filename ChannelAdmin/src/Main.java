public class Main {

    public static final String serverName = "localhost";
    public static final int serverPortNumber = 5555;

    public static void main(String[] args) {
        ChannelAdminController client = new ChannelAdminController(serverName, serverPortNumber);
    }
}