public class Main {

    public static final String hostName = "localhost";
    public static final int portNumber = 5555;

    public static void main(String[] args) {
        ChannelServer server = new ChannelServer(hostName, portNumber);
    }
}
