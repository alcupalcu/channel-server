import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class ChannelClientView extends JFrame {

    private JPanel topicsPanel;
    private JPanel allNewsContainer;

    private ArrayList<JCheckBox> topicsBoxes;

    private ChannelClientController.TopicCheckBoxListener topicCheckBoxListener;

    public ChannelClientView(ChannelClientController.TopicCheckBoxListener topicCheckBoxListener) {

        this.topicCheckBoxListener = topicCheckBoxListener;

        this.topicsPanel = new JPanel();
        this.allNewsContainer = new JPanel();

        this.topicsBoxes = new ArrayList<>();

        initializeView();
    }

    public void feedView(ArrayList<String> topics) {
        for (String topic : topics) {
            JCheckBox newCheckBox = new JCheckBox(topic);
            newCheckBox.addActionListener(topicCheckBoxListener);
            topicsBoxes.add(newCheckBox);
        }
        for (JCheckBox box : topicsBoxes) {
            topicsPanel.add(box);
        }
    }

    public void feedView(String topic) {
        JCheckBox newCheckBox = new JCheckBox(topic);
        newCheckBox.addActionListener(topicCheckBoxListener);
        topicsBoxes.add(newCheckBox);
        topicsPanel.add(newCheckBox);
        topicsPanel.revalidate();
        topicsPanel.repaint();
    }

    public void showView() {
        revalidate();
        repaint();
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void initializeView() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception exc) {
            System.out.println("Error loading a view.");
        }
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
        getContentPane().setPreferredSize(new Dimension(600, 400));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("ChannelClientView");

        initializeTopicsPanel();
        initializeAllNewsContainer();

        getContentPane().add(topicsPanel);
        getContentPane().add(new JScrollPane(allNewsContainer));
    }

    private void initializeAllNewsContainer() {
        allNewsContainer.setLayout(new BoxLayout(allNewsContainer, BoxLayout.Y_AXIS));
    }

    public void createNewsPanel(Color color, String text) {
        JPanel newsPanel = new JPanel();
        newsPanel.setSize(600, 25);
        newsPanel.setLayout(new BoxLayout(newsPanel, BoxLayout.X_AXIS));
        newsPanel.setBackground(color);

        JLabel newsColorLabel = createNewsColorLabel(color, newsPanel);
        newsPanel.add(newsColorLabel);

        JTextField newsContentField = createNewsContentField(newsPanel, text);
        newsPanel.add(newsContentField);

        this.getContentPane().add(newsPanel);
    }

    private JLabel createNewsColorLabel(Color color, JPanel newsPanel) {
        JLabel newsColorLabel = new JLabel();
        newsColorLabel.setPreferredSize(new Dimension(50, newsPanel.getHeight()));
        return newsColorLabel;
    }

    private JTextField createNewsContentField(JPanel newsPanel, String text) {
        JTextField newsContentField = new JTextField();
        newsContentField.setSize(newsPanel.getWidth() - 50,
                newsPanel.getHeight());
        newsContentField.setText(text);
        return newsContentField;
    }

    private void initializeTopicsPanel() {
        topicsPanel.setSize(600, 50);
        topicsPanel.setBackground(Color.BLACK);
    }
}
