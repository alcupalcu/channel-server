import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
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

    private Matcher matchEnd = Pattern.compile("END\n").matcher("");

    private HashMap<String, String> topics;

    private Updater updater;

    ChannelClientModel(String serverName, int serverPortNumber, ChannelClientController controller) {
        setServerName(serverName);
        setServerPortNumber(serverPortNumber);
        this.topics = new HashMap<>();

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

        this.updater = new Updater(this.channel, controller);
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

    public void saveTopics(HashMap<String, String> topicsFromServer) {
        for (String topic : topicsFromServer.keySet()) {
            if (!this.topics.containsKey(topic)) {
                topics.put(topic, topicsFromServer.get(topic));
            }
        }
    }

    public HashMap<String, String> getTopicsFromServer() {
        HashMap<String, String> topicsFromServer = new HashMap<>();

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
                        System.out.println("ENDED RECEIVING TOPICS.");
                        break;
                    } else {
                        String[] response = responseBuffer.toString().split("\t");
                        if (response.length < 2) {
                            break;
                        }
                        String label = response[0];
                        String topic = response[1];
                        String color = response[2];
                        if (label.equals("TOPIC")) {
                            topicsFromServer.put(topic, color);
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

    public void askForTopics() throws Exception {
        if (!channel.isOpen()) {
            throw new Exception("Channel is closed.");
        }
        responseBuffer = new StringBuffer();
        responseBuffer.setLength(0);
        responseBuffer.append("SEND_TOPICS");
        responseBuffer.append("\n");
        ByteBuffer outBuffer = charset.encode(CharBuffer.wrap(responseBuffer));
        channel.write(outBuffer);
    }

    public void subscribe(String topic) throws Exception {
        if (!channel.isOpen()) {
            throw new Exception("Channel is closed.");
        }
        responseBuffer = new StringBuffer();
        responseBuffer.setLength(0);
        responseBuffer.append("SUBSCRIBE");
        responseBuffer.append("\t");
        responseBuffer.append(topic);
        responseBuffer.append("\n");
        ByteBuffer outBuffer = charset.encode(CharBuffer.wrap(responseBuffer));
        channel.write(outBuffer);
    }

    public void unsubscribe(String topic) throws Exception {
        if (!channel.isOpen()) {
            throw new Exception("Channel is closed.");
        }
        responseBuffer = new StringBuffer();
        responseBuffer.setLength(0);
        responseBuffer.append("UNSUBSCRIBE");
        responseBuffer.append("\t");
        responseBuffer.append(topic);
        responseBuffer.append("\n");
        ByteBuffer outBuffer = charset.encode(CharBuffer.wrap(responseBuffer));
        channel.write(outBuffer);
    }

    public String getColorOf(String topic) {
        if (topics.containsKey(topic)) {
            return topics.get(topic);
        }
        return "WHITE";
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

    public HashMap<String, String> getTopics() {
        return this.topics;
    }

    public SocketChannel getChannel() {
        return this.channel;
    }

    public Updater getUpdater() {
        return updater;
    }

    class Updater extends Thread {

        private Charset charset = StandardCharsets.UTF_8;
        private ByteBuffer inBuffer = ByteBuffer.allocate(BUFF_SIZE);
        private StringBuffer updateContentBuffer;
        private Matcher matchEnd = Pattern.compile("END\n").matcher("");

        private SocketChannel communicationChannelForUpdate;
        private ChannelClientController controller;

        public Updater(SocketChannel communicationChannel, ChannelClientController controller) {
            communicationChannelForUpdate = communicationChannel;
            this.controller = controller;
        }

        public void run() {

            try {
                while (true) {
                    inBuffer.clear();
                    int readBytes = channel.read(inBuffer);
                    if (readBytes == 0) {
                        Thread.sleep(200);
                    }
                    else if (readBytes == -1) {
                        System.out.println("Channel is closed");
                        channel.close();
                    }
                    else {
                        updateContentBuffer = new StringBuffer();
                        inBuffer.flip();
                        CharBuffer inCharBuffer = charset.decode(inBuffer);
                        updateContentBuffer.append(inCharBuffer);
                        inCharBuffer.clear();
                        matchEnd.reset(inCharBuffer);
                        if (matchEnd.find()) {
                            String[] updates = updateContentBuffer.toString().split("\tEND\n");
                            for (String update : updates) {
                                String[] oneUpdate = update.split("\t");
                                if (oneUpdate.length < 2) {
                                    break;
                                }
                                String command = oneUpdate[0];
                                if (command.equals("NEWS")) {
                                    if (oneUpdate.length < 3) {
                                        break;
                                    }
                                }

                                for (String word : oneUpdate) {
                                    System.out.print(word + " ");
                                }
                                System.out.println();

                                switch (command) {
                                    case "TOPIC":
                                        String topicToAdd = oneUpdate[1];
                                        String color = oneUpdate[2];
                                        topics.put(topicToAdd, color);
                                        controller.notifyController(ChannelClientController.Updates.ADD_TOPIC, topicToAdd, null);
                                        break;
                                    case "DELETE_TOPIC":
                                        String topicForDeletion = oneUpdate[1];
                                        topics.remove(topicForDeletion);
                                        controller.notifyController(ChannelClientController.Updates.DELETE_TOPIC, topicForDeletion, null);
                                        break;
                                    case "NEWS":
                                        String topicOfNews = oneUpdate[1];
                                        String news = oneUpdate[2];
                                        controller.notifyController(ChannelClientController.Updates.NEWS, topicOfNews, news);
                                    default:
                                        System.out.println("Update request unrecognized.");
                                }
                            }
                        }
                    }
                }
            } catch (Exception exc) {
                exc.printStackTrace();
            }
        }
    }
}
