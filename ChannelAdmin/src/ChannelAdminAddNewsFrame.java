import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.ArrayList;

public class ChannelAdminAddNewsFrame extends JFrame {

    private JPanel pMain;
    private JTextField tfNewsAddition;
    private JButton bAddNews;
    private JButton bBack;
    private JComboBox<String> cbTopic;

    public ChannelAdminAddNewsFrame() {
        this.pMain = new JPanel();
        this.tfNewsAddition = new JTextField();
        this.bAddNews = new JButton("Add news");
        this.bBack = new JButton("Back");
        this.cbTopic = new JComboBox<>();
    }

    public void initializeView() {
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
        getContentPane().setPreferredSize(new Dimension(300, 250));
        getContentPane().setBackground(new Color(30, 39, 44));

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        initializeMainPanel();

        pMain.setAlignmentX(Component.CENTER_ALIGNMENT);
        pMain.setAlignmentY(Component.CENTER_ALIGNMENT);

        getContentPane().add(pMain);

        repaint();
        revalidate();
        pack();
        setLocationRelativeTo(null);
    }

    private void initializeMainPanel() {
        pMain.setSize(300, 250);
        pMain.setLayout(new BoxLayout(pMain, BoxLayout.Y_AXIS));

        pMain.add(cbTopic);
        pMain.add(Box.createRigidArea(new Dimension(0, 20)));

        initializeNewsField();
        pMain.add(tfNewsAddition);
        pMain.add(Box.createRigidArea(new Dimension(0, 20)));

        pMain.add(bAddNews);
        pMain.add(Box.createRigidArea(new Dimension(0, 20)));

        pMain.add(bBack);
        pMain.add(Box.createRigidArea(new Dimension(0, 20)));

        pMain.setBackground(new Color(30, 39, 44));

        pMain.repaint();
        pMain.revalidate();
    }

    public void setTopics(ArrayList<String> topics) {
        cbTopic.removeAllItems();
        for (String topic : topics) {
            cbTopic.addItem(topic);
        }
    }

    public String getTopic() {
        return cbTopic.getSelectedItem().toString();
    }

    private void initializeNewsField() {
        tfNewsAddition.setSize(pMain.getWidth(), 100);
    }

    public void addNewsAddListener(ActionListener newsAddListener) {
        bAddNews.addActionListener(newsAddListener);
    }

    public String getNewsFromField() {
        return tfNewsAddition.getText();
    }

    public JTextField getTfNewsAddition() {
        return this.tfNewsAddition;
    }

    public void addBackListener(ActionListener addBackListener) {
        bBack.addActionListener(addBackListener);
    }
}
