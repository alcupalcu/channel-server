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

    private enum ResponseType {
        TOPICS,
        UPDATE_TOPIC,
        DELETE_TOPIC,
        UNKNOWN_COMMAND
    }

    private ServerSocketChannel serverSocketChannel = null;
    private Selector channelSelector = null;
    private Charset requestsCharset = StandardCharsets.UTF_8;
    private ByteBuffer communicationChannelBuffer = ByteBuffer.allocate(BUFFER_SIZE);
    private StringBuffer responseForClient;

    private ArrayList<String> topicsAvailable;
    private HashMap<SocketChannel, ArrayList<String>> subscribers;

    public String hostName;
    public int serverPort;

    public ChannelServer(String hostName, int serverPort) {
        this.hostName = hostName;
        this.serverPort = serverPort;
        this.subscribers = new HashMap<>();
        this.topicsAvailable = new ArrayList<>();

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

        /*** TO DELETE AT SOME POINT ***/
        topicsAvailable.add("SPORT");
        topicsAvailable.add("MUSIC");
        topicsAvailable.add("NEWS");

        handleConnections();
    }

    private void handleConnections() {
        while (true) {
            try {
                waitForEventsFromRegisteredChannels();
                handleEventsOnRegisteredChannels();
            } catch (IOException exc) {
                exc.printStackTrace();
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
                System.out.println("New connection.");
                SocketChannel acceptedChanel = serverSocketChannel.accept();
                configureSocketChannelBlocking(acceptedChanel, false);
                registerChannelInSelector(acceptedChanel, READ_WRITE_MODE);
                sendTopics(acceptedChanel);
            }

            if (selectorKey.isReadable()) {
                SocketChannel communicationChannel = (SocketChannel) selectorKey.channel();
                handleRequest(communicationChannel);
            }
        }
    }

    private void handleRequest(SocketChannel communicationChannel) throws IOException {
        if (!communicationChannel.isOpen()) {
            return;
        }

        readRequest(communicationChannel);

        String[] requestContent = responseForClient.toString().split(" ");
        String command = requestContent[0];

        switch (command) {
            case "SUBSCRIBE":
                String topicForSubscription = requestContent[1];
                assignSubscription(communicationChannel, topicForSubscription);
                response(ResponseType.UPDATE_TOPIC, communicationChannel);
                break;
                /* ADD NEW CASES */
                /* <--- ADD --->*/
                /* <--- ADD --->*/
                /* <--- ADD --->*/
            case "UNSUBSCRIBE":
                String topicToUnsubscribe = requestContent[1];
                deleteSubscription(communicationChannel, topicToUnsubscribe);
                response(ResponseType.DELETE_TOPIC, communicationChannel);
            default:
                response(ResponseType.UNKNOWN_COMMAND, communicationChannel);
        }
    }

    private void readRequest(SocketChannel communicationChannel) throws IOException {
        responseForClient.setLength(0);
        readRequestLoop:
        while (true) {
            int sizeOfChannelData = communicationChannel.read(communicationChannelBuffer);
            if (sizeOfChannelData > 0) {
                communicationChannelBuffer.flip();
                CharBuffer decodedRequestBuffer = requestsCharset.decode(communicationChannelBuffer);
                while (decodedRequestBuffer.hasRemaining()) {
                    char bufferContentChar = decodedRequestBuffer.get();
                    if (bufferContentChar == '\n') {
                        break readRequestLoop;
                    }
                    responseForClient.append(bufferContentChar);
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
        response(ResponseType.TOPICS, subscriberChannel);
        System.out.println("Topics sent to the client listening on port: " + subscriberChannel.socket().getPort());
    }

    private void assignSubscription(SocketChannel subscriberChannel, String topic) {
        if (this.subscribers.containsKey(subscriberChannel)) {
            if (!this.subscribers.get(subscriberChannel).contains(topic)) {
                this.subscribers.get(subscriberChannel).add(topic);
            }
        } else {
            ArrayList<String> topicsForNewSubscriber = new ArrayList<>();
            topicsForNewSubscriber.add(topic);
            this.subscribers.put(subscriberChannel, topicsForNewSubscriber);
        }
    }

    private void deleteSubscription(SocketChannel subscriberChannel, String topic) {
        if (this.subscribers.containsKey(subscriberChannel)) {
            this.subscribers.get(subscriberChannel).remove(topic);
        }
    }

    private void response(ResponseType responseType, SocketChannel subscriberChannel) throws IOException {
        switch (responseType) {
            case TOPICS:
                for (String topic : topicsAvailable) {
                    sendTopic(subscriberChannel, topic);
                    System.out.println("Topic sent: " + topic);
                }
                endResponse(subscriberChannel);
                break;
            case UPDATE_TOPIC:
                //SEND NEWS RELATED TO TOPIC
                break;
            case DELETE_TOPIC:
                //SEND CONFIRMATION OF DELETION
                //subscriberChannel.write(confirmation);
            case UNKNOWN_COMMAND:
                //SEND UNKNOWN COMMAND
                //subscriberChannel.write("UNKNOWN COMMAND");
            default:
                System.out.println("THE RESPONSE WAS NOT SEND.");
        }
    }

    private void sendTopic(SocketChannel subscriberChannel, String topic) throws IOException {
        responseForClient = new StringBuffer();
        responseForClient.setLength(0);
        responseForClient.append("TOPIC");
        responseForClient.append(" ");
        responseForClient.append(topic);
        ByteBuffer bufferForEncoding = requestsCharset.encode(CharBuffer.wrap(responseForClient));
        subscriberChannel.write(bufferForEncoding);
    }

    private void endResponse(SocketChannel subscriberChannel) throws IOException {
        responseForClient.setLength(0);
        responseForClient.append("END");
        ByteBuffer bufferForEncoding = requestsCharset.encode(CharBuffer.wrap(responseForClient));
        subscriberChannel.write(bufferForEncoding);
    }
}
