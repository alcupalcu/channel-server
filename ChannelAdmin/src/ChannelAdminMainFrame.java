import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class ChannelAdminMainFrame extends JFrame  {

    private JPanel pMain;
    private JButton bAddTopic;
    private JButton bDeleteTopic;
    private JButton bAddNews;
    private ChannelAdminController.TopicCreationListener topicCreationListener;

    public ChannelAdminMainFrame() {
        pMain = new JPanel();
        bAddTopic = new JButton("Add topic");
        bDeleteTopic = new JButton("Delete topic");
        bAddNews = new JButton("Add news");

        initializeMainFrame();
    }

    private void initializeMainFrame() {

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception exc) {
            System.out.println("Unable to create look and feel.");
        }

        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
        getContentPane().setPreferredSize(new Dimension(200, 150));
        getContentPane().setBackground(new Color(30, 39, 44));

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        initializePanel();

        pMain.add(Box.createRigidArea(new Dimension(0, 20)));
        pMain.add(bAddTopic);
        pMain.add(Box.createRigidArea(new Dimension(0, 20)));
        pMain.add(bDeleteTopic);
        pMain.add(Box.createRigidArea(new Dimension(0, 20)));
        pMain.add(bAddNews);

        pMain.setAlignmentX(Component.CENTER_ALIGNMENT);
        pMain.setAlignmentY(Component.CENTER_ALIGNMENT);

        getContentPane().add(pMain);

        repaint();
        revalidate();
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void initializePanel() {
        pMain.setLayout(new BoxLayout(pMain, BoxLayout.Y_AXIS));
        pMain.setBackground(new Color(30, 39, 44));
    }

    void addTopicsCreationListener(ActionListener topicsCreationListener) {
        bAddTopic.addActionListener(topicsCreationListener);
    }

    void addTopicsDeletionListener(ActionListener topicsDeletionListener) {
        bDeleteTopic.addActionListener(topicsDeletionListener);
    }

    void addNewsAdditionListener(ActionListener newsAdditionListener) {
        bAddNews.addActionListener(newsAdditionListener);
    }
}
