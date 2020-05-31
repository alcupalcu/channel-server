import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.ArrayList;

public class ChannelAdminDeleteTopicFrame extends JFrame {

    private JPanel pMain;
    private JPanel pTopics;
    private JButton bDeleteTopic;
    private JButton bBack;
    private ArrayList<JCheckBox> topicsForDeletion;

    public ChannelAdminDeleteTopicFrame() {
        this.pMain = new JPanel();
        this.pTopics = new JPanel();
        this.bDeleteTopic = new JButton("Delete topic");
        this.bBack = new JButton("Back");
        this.topicsForDeletion = new ArrayList<>();
    }

    public void initializeView() {
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
        getContentPane().setPreferredSize(new Dimension(200, 150));
        getContentPane().setBackground(new Color(30, 39, 44));

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        initializeTopicsPanel();
        initializeMainPanel();

        pMain.setAlignmentX(Component.CENTER_ALIGNMENT);
        pMain.setAlignmentY(Component.CENTER_ALIGNMENT);
        pTopics.setAlignmentX(Component.CENTER_ALIGNMENT);
        pTopics.setAlignmentY(Component.CENTER_ALIGNMENT);

        getContentPane().add(pTopics);
        getContentPane().add(pMain);

        repaint();
        revalidate();
        pack();
        setLocationRelativeTo(null);
    }

    private void initializeMainPanel() {
        pMain.setSize(200, 100);
        pMain.setLayout(new BoxLayout(pMain, BoxLayout.Y_AXIS));

        pMain.add(bDeleteTopic);
        pMain.add(Box.createRigidArea(new Dimension(0, 20)));

        pMain.add(bBack);
        pMain.add(Box.createRigidArea(new Dimension(0, 20)));

        pMain.setBackground(new Color(30, 39, 44));

        pMain.repaint();
        pMain.revalidate();
    }

    private void initializeTopicsPanel() {
        pTopics.setSize(200, 50);
        pTopics.setLayout(new BoxLayout(pTopics, BoxLayout.X_AXIS));
        pTopics.setBackground(new Color(30, 39, 44));
        pTopics.setForeground(Color.CYAN);
        pTopics.repaint();
        pTopics.revalidate();
    }

    public void addTopicDeleteListener(ActionListener topicDeleteListener) {
        bDeleteTopic.addActionListener(topicDeleteListener);
    }

    public void setTopics(ArrayList<String> topics) {
        for (String topic : topics) {
            JCheckBox newCheckBox = new JCheckBox(topic);
            topicsForDeletion.add(newCheckBox);
        }
        for (JCheckBox box : topicsForDeletion) {
            pTopics.add(box);
            pTopics.revalidate();
            pTopics.repaint();
        }
    }

    public ArrayList<String> getTopicsSelected() {
        ArrayList<String> topicsSelected = new ArrayList<>();
        for (JCheckBox box : topicsForDeletion) {
            if (box.isSelected()) {
                topicsSelected.add(box.getText());
            }
        }
        return topicsSelected;
    }

    public void addBackListener(ActionListener addBackListener) {
        bBack.addActionListener(addBackListener);
    }
}
