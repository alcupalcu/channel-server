import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;

public class ChannelAdminController {

    private ChannelAdminModel adminModel;
    private ChannelAdminMainFrame mainFrame;
    private ChannelAdminAddTopicFrame addTopicFrame;
    private ChannelAdminDeleteTopicFrame deleteTopicFrame;
    private ChannelAdminAddNewsFrame addNewsFrame;

    public ChannelAdminController(String serverName, int serverPortNumber) {

        this.adminModel = new ChannelAdminModel(serverName, serverPortNumber);
        this.mainFrame = new ChannelAdminMainFrame();
        this.addTopicFrame = new ChannelAdminAddTopicFrame();
        this.addNewsFrame = new ChannelAdminAddNewsFrame();
        this.deleteTopicFrame = new ChannelAdminDeleteTopicFrame();
        mainFrame.addTopicsCreationListener(new TopicCreationListener());
        mainFrame.addTopicsDeletionListener(new TopicDeletionListener());
        mainFrame.addNewsAdditionListener(new NewsAdditionListener());
        addTopicFrame.addTopicAddListener(new TopicAddListener());
        addTopicFrame.addBackListener(new BackListener());
        addNewsFrame.addNewsAddListener(new NewsAddListener());
        addNewsFrame.addBackListener(new BackListener());
        deleteTopicFrame.addBackListener(new BackListener());

        try {
            adminModel.connect();
        } catch (IOException exc) {
            System.out.println("Unable to connect to the server.");
            System.exit(1);
        }

        try {
            adminModel.goAsAdmin();
        } catch (Exception exc) {
            System.out.println("Unable to connect to the server");
            System.exit(2);
        }

        addTopicFrame.initializeView();
        addNewsFrame.initializeView();

    }

    class TopicCreationListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            mainFrame.setVisible(false);
            addTopicFrame.setVisible(true);
        }
    }

    class TopicDeletionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            mainFrame.setVisible(false);
            deleteTopicFrame = new ChannelAdminDeleteTopicFrame();
            deleteTopicFrame.addTopicDeleteListener(new TopicDeleteListener());
            deleteTopicFrame.addBackListener(new BackListener());
            deleteTopicFrame.initializeView();
            deleteTopicFrame.setTopics(adminModel.getTopics());
            deleteTopicFrame.setVisible(true);
        }
    }

    class TopicAddListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            String topicField = addTopicFrame.getTopicFromField();
            String color = addTopicFrame.getColor();
            addTopicFrame.getTfTopicAddition().setText("");
            try {
                adminModel.addTopic(topicField, color);
                addTopicFrame.setVisible(false);
                mainFrame.setVisible(true);
            } catch (Exception exception) {
                System.out.println("Unable to add new topic.");
            }
        }
    }

    class TopicDeleteListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            ArrayList<String> topicsForDeletion = deleteTopicFrame.getTopicsSelected();
            try {
                for (String topic : topicsForDeletion) {
                    System.out.println("Deleting " + topic);
                    adminModel.deleteTopic(topic);
                }
                deleteTopicFrame.setVisible(false);
                deleteTopicFrame = new ChannelAdminDeleteTopicFrame();
                deleteTopicFrame.addTopicDeleteListener(new TopicDeletionListener());
                deleteTopicFrame.initializeView();
                mainFrame.setVisible(true);
            } catch (Exception exception) {
                System.out.println("Unable to delete topics.");
            }
        }
    }

    class NewsAdditionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            mainFrame.setVisible(false);
            addNewsFrame.setTopics(adminModel.getTopics());
            addNewsFrame.setVisible(true);
        }
    }

    class NewsAddListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            String topic = addNewsFrame.getTopic();
            String newsField = addNewsFrame.getNewsFromField();
            addNewsFrame.getTfNewsAddition().setText("");
            try {
                adminModel.addNews(topic, newsField);
                addNewsFrame.setVisible(false);
                mainFrame.setVisible(true);
            } catch (Exception exception) {
                System.out.println("Unable to add new topic.");
            }
        }
    }

    class BackListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            addTopicFrame.setVisible(false);
            addNewsFrame.setVisible(false);
            deleteTopicFrame.setVisible(false);
            mainFrame.setVisible(true);
        }
    }
}
