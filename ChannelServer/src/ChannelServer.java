import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class ChannelServer {

    private final int[] ACCEPT_MODE = new int[] { SelectionKey.OP_ACCEPT };
    private final int[] READ_WRITE_MODE = new int[] { SelectionKey.OP_READ, SelectionKey.OP_WRITE };
    private final int BUFFER_SIZE = 1024;

    private ServerSocketChannel serverSocketChannel = null;
    private Selector channelSelector = null;
    private Charset requestsCharset = StandardCharsets.UTF_8;
    private ByteBuffer communicationChannelBuffer = ByteBuffer.allocate(BUFFER_SIZE);
    private StringBuffer requestFromClient;

    private HashMap<String, String> topicsAvailable;
    private HashMap<SocketChannel, ArrayList<String>> subscribers;

    public String hostName;
    public int serverPort;

    public ChannelServer(String hostName, int serverPort) {
        this.hostName = hostName;
        this.serverPort = serverPort;
        this.subscribers = new HashMap<>();
        this.topicsAvailable = new HashMap<>();
        this.requestFromClient = new StringBuffer();

        try {
            configureServerSocketChannel();
            bindServerSocketChannel();
            openSelector();
            registerChannelInSelector(serverSocketChannel, ACCEPT_MODE);
        } catch (IOException exc) {
            System.out.println("The server channel was not opened.\nAn error has occurred.");
            System.exit(1);
        }

        System.out.println("Server started ready for requests.");

        handleConnections();
    }

    private void handleConnections() {
        while (true) {
            try {
                waitForEventsFromRegisteredChannels();
                handleEventsOnRegisteredChannels();
            } catch (IOException exc) {
                System.out.println("Client disconnected unexpectedly.");
                System.exit(2);
            }
        }
    }

    private void waitForEventsFromRegisteredChannels() throws IOException {
        channelSelector.select();
    }

    private void handleEventsOnRegisteredChannels() throws IOException {
        Set<SelectionKey> selectorKeys = channelSelector.selectedKeys();

        Iterator<SelectionKey> selectorKeysIterator = selectorKeys.iterator();
        while (selectorKeysIterator.hasNext()) {

            SelectionKey selectorKey = selectorKeysIterator.next();
            selectorKeysIterator.remove();

            if (selectorKey.isAcceptable()) {
                SocketChannel acceptedChanel = serverSocketChannel.accept();
                configureSocketChannelBlocking(acceptedChanel, false);
                registerChannelInSelector(acceptedChanel, READ_WRITE_MODE);
                subscribers.put(acceptedChanel, new ArrayList<>());
            }

            if (selectorKey.isReadable()) {
                SocketChannel communicationChannel = (SocketChannel) selectorKey.channel();
                handleRequest(communicationChannel);
            }
        }
    }

    private void handleRequest(SocketChannel communicationChannel) throws IOException {

        readRequest(communicationChannel);

        String[] requestContent = requestFromClient.toString().split("\t");
        String command = requestContent[0];

        switch (command) {
            case "ADMIN":
                this.subscribers.remove(communicationChannel);
                break;
            case "SUBSCRIBE":
                System.out.println("Request for subscription from " + communicationChannel.socket().getPort());
                String topicForSubscription = requestContent[1];
                assignSubscription(communicationChannel, topicForSubscription);
                break;
            case "UNSUBSCRIBE":
                System.out.println("Request for stopping subscription from " + communicationChannel.socket().getPort());
                String topicToUnsubscribe = requestContent[1];
                deleteSubscription(communicationChannel, topicToUnsubscribe);
                break;
            case "ADD_TOPIC":
                String topicAdded = requestContent[1];
                String color = requestContent[2];
                System.out.println("Admin adding topic " + topicAdded + ".");
                this.topicsAvailable.put(topicAdded, color);
                sendTopicToClients(topicAdded, color);
                break;
            case "UPDATE_NEWS":
                String topicToUpdate = requestContent[1];
                String newsContent = requestContent[2];
                System.out.println("Admin updating topic " + topicToUpdate +" with " + newsContent + ".");
                updateTopic(topicToUpdate, newsContent);
                break;
            case "DELETE_TOPIC":
                String topicToDelete = requestContent[1];
                System.out.println("Admin deleted topic " + topicToDelete + ".");
                deleteTopic(topicToDelete);
                break;
            case "SEND_TOPICS":
                sendTopics(communicationChannel);
                break;
            default:
                System.out.println("Request incorrect.");
        }
    }

    private void readRequest(SocketChannel communicationChannel) throws IOException {
        requestFromClient = new StringBuffer();
        requestFromClient.setLength(0);
        readRequestLoop:
        while (true) {
            int sizeOfChannelData = communicationChannel.read(communicationChannelBuffer);
            if (sizeOfChannelData > 0) {
                communicationChannelBuffer.flip();
                CharBuffer decodedRequestBuffer = requestsCharset.decode(communicationChannelBuffer);
                while (decodedRequestBuffer.hasRemaining()) {
                    char bufferContentChar = decodedRequestBuffer.get();
                    if (bufferContentChar == '\n') {
                        communicationChannelBuffer.clear();
                        break readRequestLoop;
                    }
                    requestFromClient.append(bufferContentChar);
                }
            }
        }
    }

    private void configureServerSocketChannel() throws IOException {
        serverSocketChannel = ServerSocketChannel.open();
        configureSocketChannelBlocking(serverSocketChannel, false);
    }

    private void configureSocketChannelBlocking(SelectableChannel channel, boolean isBlocked)
            throws IOException {
        channel.configureBlocking(isBlocked);
    }

    private void bindServerSocketChannel() throws IOException {
        InetSocketAddress serverAddress = new InetSocketAddress(hostName, serverPort);
        serverSocketChannel.socket().bind(serverAddress);
    }

    private void openSelector() throws IOException {
        channelSelector = Selector.open();
    }

    private void registerChannelInSelector(SelectableChannel channel, int[] modes) throws ClosedChannelException {
        if (modes.length == 1) {
            channel.register(channelSelector, modes[0]);
        }
        else if (modes.length == 2) {
            channel.register(channelSelector, modes[0] | modes[1]);
        }
    }

    private void sendTopics(SocketChannel subscriberChannel) throws IOException {
        if (!topicsAvailable.isEmpty()) {
            for (String topic : topicsAvailable.keySet()) {
                sendTopic(subscriberChannel, topic, topicsAvailable.get(topic));
                System.out.println("Topic sent: " + topic);
            }
        }
        endResponse(subscriberChannel);
        System.out.println("Topics sent to the client listening on port: " + subscriberChannel.socket().getPort());
    }

    private void assignSubscription(SocketChannel subscriberChannel, String topic) {
        if (this.subscribers.containsKey(subscriberChannel)) {
            if (this.subscribers.get(subscriberChannel).isEmpty()) {
                ArrayList<String> topicsForNewSubscriber = new ArrayList<>();
                topicsForNewSubscriber.add(topic);
                this.subscribers.put(subscriberChannel, topicsForNewSubscriber);
            }
            if (!this.subscribers.get(subscriberChannel).contains(topic)) {
                this.subscribers.get(subscriberChannel).add(topic);
            }
        }
        System.out.println(subscriberChannel.socket().getPort() + " subscribing: " + topic);
    }

    private void deleteSubscription(SocketChannel subscriberChannel, String topic) {
        if (this.subscribers.containsKey(subscriberChannel)) {
            this.subscribers.get(subscriberChannel).remove(topic);
        }
        System.out.println(subscriberChannel.socket().getPort() + " unsubscribing: " + topic);
    }

    private void sendTopic(SocketChannel subscriberChannel, String topic, String color) throws IOException {
        StringBuffer responseForClient = new StringBuffer();
        responseForClient.setLength(0);
        responseForClient.append("TOPIC");
        responseForClient.append("\t");
        responseForClient.append(topic);
        responseForClient.append("\t");
        responseForClient.append(color);
        responseForClient.append("\t");
        System.out.println("Response: " + responseForClient);
        ByteBuffer bufferForEncoding = requestsCharset.encode(CharBuffer.wrap(responseForClient));
        subscriberChannel.write(bufferForEncoding);
    }

    private void updateTopic(String topic, String newsContent) throws IOException {
        for (var subscriber : subscribers.entrySet()) {
            if (subscriber.getValue().contains(topic)) {
                sendUpdateOfTopic(subscriber.getKey(), topic, newsContent);
                endResponse(subscriber.getKey());
            }
        }
    }

    private void deleteTopic(String topic) throws IOException {
        this.topicsAvailable.remove(topic);

        for (var subscriber : subscribers.keySet()) {
            sendTopicDeletion(subscriber, topic);
            endResponse(subscriber);
        }
    }

    private void sendUpdateOfTopic(SocketChannel subscriberChannel, String topic, String newsContent) throws IOException {
        StringBuffer responseForClient = new StringBuffer();
        responseForClient = new StringBuffer();
        responseForClient.setLength(0);
        responseForClient.append("NEWS");
        responseForClient.append("\t");
        responseForClient.append(topic);
        responseForClient.append("\t");
        responseForClient.append(newsContent);
        responseForClient.append("\t");
        ByteBuffer bufferForEncoding = requestsCharset.encode(CharBuffer.wrap(responseForClient));
        subscriberChannel.write(bufferForEncoding);
    }

    private void sendTopicDeletion(SocketChannel subscriberChannel, String topic) throws IOException {
        StringBuffer responseForClient = new StringBuffer();
        responseForClient = new StringBuffer();
        responseForClient.setLength(0);
        responseForClient.append("DELETE_TOPIC");
        responseForClient.append("\t");
        responseForClient.append(topic);
        responseForClient.append("\t");
        System.out.println(responseForClient.toString() + " sent.");
        ByteBuffer bufferForEncoding = requestsCharset.encode(CharBuffer.wrap(responseForClient));
        subscriberChannel.write(bufferForEncoding);
    }

    private void sendTopicToClients(String topic, String color) throws IOException {
        for (SocketChannel subscriber : subscribers.keySet()) {
            sendTopic(subscriber, topic, color);
            endResponse(subscriber);
        }
    }

    private void endResponse(SocketChannel subscriberChannel) throws IOException {
        StringBuffer responseForClient = new StringBuffer();
        responseForClient.setLength(0);
        responseForClient.append("END\n");
        ByteBuffer bufferForEncoding = requestsCharset.encode(CharBuffer.wrap(responseForClient));
        subscriberChannel.write(bufferForEncoding);
    }
}
