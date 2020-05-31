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

public class ChannelAdminModel {

    private final int BUFF_SIZE = 1024;

    private String serverName;
    private int serverPortNumber;
    private SocketChannel channel;

    private Charset charset = StandardCharsets.UTF_8;
    private ByteBuffer inBuffer = ByteBuffer.allocateDirect(BUFF_SIZE);
    private StringBuffer responseBuffer;

    private Matcher matchEnd = Pattern.compile("END").matcher("");

    private ArrayList<String> topics;

    ChannelAdminModel(String serverName, int serverPortNumber) {
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

    public void goAsAdmin() throws Exception {
        if (!channel.isOpen()) {
            throw new Exception("Channel is closed.");
        }
        responseBuffer = new StringBuffer();
        responseBuffer.setLength(0);
        responseBuffer.append("ADMIN");
        responseBuffer.append("\n");
        ByteBuffer outBuffer = charset.encode(CharBuffer.wrap(responseBuffer));
        channel.write(outBuffer);
        System.out.println("Presented as admin.");
    }

    public void addTopic(String topic, String color) throws Exception {
        if (!channel.isOpen()) {
            throw new Exception("Channel is closed.");
        }
        if (!topics.contains(topic)) {
            topics.add(topic);
            responseBuffer = new StringBuffer();
            responseBuffer.setLength(0);
            responseBuffer.append("ADD_TOPIC");
            responseBuffer.append("\t");
            responseBuffer.append(topic);
            responseBuffer.append("\t");
            responseBuffer.append(color);
            responseBuffer.append("\n");
            ByteBuffer outBuffer = charset.encode(CharBuffer.wrap(responseBuffer));
            channel.write(outBuffer);
            System.out.println("Send topic: " + topic);
        }
    }

    public void deleteTopic(String topic) throws Exception {
        if (!channel.isOpen()) {
            throw new Exception("Channel is closed.");
        }
        if (topics.contains(topic)) {
            responseBuffer = new StringBuffer();
            responseBuffer.setLength(0);
            responseBuffer.append("DELETE_TOPIC");
            responseBuffer.append("\t");
            responseBuffer.append(topic);
            responseBuffer.append("\n");
            ByteBuffer outBuffer = charset.encode(CharBuffer.wrap(responseBuffer));
            channel.write(outBuffer);
            System.out.println("Send topic deletion: " + topic);
        }
        topics.remove(topic);
    }

    public void addNews(String topic, String news) throws Exception {
        if (!channel.isOpen()) {
            throw new Exception("Channel is closed.");
        }
        if (topics.contains(topic)) {
            responseBuffer = new StringBuffer();
            responseBuffer.setLength(0);
            responseBuffer.append("UPDATE_NEWS");
            responseBuffer.append("\t");
            responseBuffer.append(topic);
            responseBuffer.append("\t");
            responseBuffer.append(news);
            responseBuffer.append("\n");
            ByteBuffer outBuffer = charset.encode(CharBuffer.wrap(responseBuffer));
            channel.write(outBuffer);
            System.out.println("Send news: " + topic + " " + news);
        }
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
}
