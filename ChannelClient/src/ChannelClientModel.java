import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChannelClientModel {

    private final int CHANNEL_WAITING = 0;
    private final int CHANNEL_CLOSED = -1;
    private final int BUFF_SIZE = 1024;

    private String serverName;
    private int serverPortNumber;
    private SocketChannel channel;

    private Charset charset = StandardCharsets.UTF_8;
    private ByteBuffer inBuffer = ByteBuffer.allocateDirect(BUFF_SIZE);
    private StringBuffer responseBuffer;

    private Matcher matchEnd = Pattern.compile("END").matcher("");

    private ArrayList<String> topics;

    ChannelClientModel(String serverName, int serverPortNumber) {
        setServerName(serverName);
        setServerPortNumber(serverPortNumber);
        this.topics = new ArrayList<>();

        try {
            channel = SocketChannel.open();
            channel.configureBlocking(false);
        } catch (UnknownHostException exc) {
            exc.printStackTrace();
            System.exit(1);
        } catch (IOException exc) {
            exc.printStackTrace();
            System.exit(2);
        }
    }

    public void connect() throws UnknownHostException, IOException {
        if (!channel.isOpen()) {
            channel = SocketChannel.open();
        }

        channel.connect(new InetSocketAddress(getServerName(), getServerPortNumber()));
        System.out.println("CONNECTING...");

        while(!channel.finishConnect()) {
            try {
                Thread.sleep(200);
            } catch (Exception exc) {
                return;
            }
            System.out.print(".");
        }
        System.out.println("CONNECTED.");
    }

    public void saveTopics(ArrayList<String> topicsFromServer) {
        for (String topic : topicsFromServer) {
            if (!this.topics.contains(topic)) {
                topics.add(topic);
            }
        }
    }

    public ArrayList<String> getTopicsFromServer() {
        ArrayList<String> topicsFromServer = new ArrayList<>();

        try {
            CharBuffer charBuffer;
            while(true) {
                clearBuffers();
                int bytesRead = channel.read(inBuffer);
                if (bytesRead == CHANNEL_WAITING) {
                    System.out.print(".");
                }
                else if (bytesRead == CHANNEL_CLOSED) {
                    channel.close();
                    break;
                } else {
                    inBuffer.flip();
                    charBuffer = charset.decode(inBuffer);
                    responseBuffer.append(charBuffer);
                    charBuffer.clear();
                    matchEnd.reset(charBuffer);
                    if (matchEnd.find()) {
                        System.out.println("ENDED RECEIVING.");
                        break;
                    } else {
                        String[] response = responseBuffer.toString().split(" ");
                        if (response.length < 2) {
                            break;
                        }
                        String label = response[0];
                        String topic = response[1];
                        if (label.equals("TOPIC")) {
                            topicsFromServer.add(topic);
                        }
                    }
                }
            }
        } catch (Exception exc) {
            exc.printStackTrace();
        }

        return topicsFromServer;
    }

    private void clearBuffers() {
        this.inBuffer.clear();
        this.responseBuffer = new StringBuffer();
    }

    private void setServerName(String serverName) {
        this.serverName = serverName;
    }

    private void setServerPortNumber(int serverPortNumber) {
        this.serverPortNumber = serverPortNumber;
    }

    private String getServerName() {
        return this.serverName;
    }

    private int getServerPortNumber() {
        return this.serverPortNumber;
    }

    public ArrayList<String> getTopics() {
        return this.topics;
    }

    public SocketChannel getChannel() {
        return this.channel;
    }
}
