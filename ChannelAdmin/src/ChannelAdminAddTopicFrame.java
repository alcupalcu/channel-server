import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class ChannelAdminAddTopicFrame extends JFrame {

    private JPanel pMain;
    private JTextField tfTopicAddition;
    private JButton bAddTopic;
    private JButton bBack;
    private JComboBox<String> colorChooser;

    public ChannelAdminAddTopicFrame() {
        this.pMain = new JPanel();
        this.tfTopicAddition = new JTextField();
        this.bAddTopic = new JButton("Add topic");
        this.bBack = new JButton("Back");
        this.colorChooser = new JComboBox<>();
    }

    public void initializeView() {
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
        getContentPane().setPreferredSize(new Dimension(200, 150));
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
        pMain.setSize(600, 400);
        pMain.setLayout(new BoxLayout(pMain, BoxLayout.Y_AXIS));

        initializeColorChooser();
        pMain.add(colorChooser);
        pMain.add(Box.createRigidArea(new Dimension(0, 20)));

        initializeTopicField();
        pMain.add(tfTopicAddition);
        pMain.add(Box.createRigidArea(new Dimension(0, 20)));

        pMain.add(bAddTopic);
        pMain.add(Box.createRigidArea(new Dimension(0, 20)));

        pMain.add(bBack);
        pMain.add(Box.createRigidArea(new Dimension(0, 20)));

        pMain.setBackground(new Color(30, 39, 44));

        pMain.repaint();
        pMain.revalidate();
    }

    private void initializeColorChooser() {
        colorChooser.addItem("RED");
        colorChooser.addItem("BLUE");
        colorChooser.addItem("GREEN");
        colorChooser.addItem("BLACK");
        colorChooser.addItem("YELLOW");
    }

    public String getColor() {
        return colorChooser.getSelectedItem().toString();
    }

    private void initializeTopicField() {
        tfTopicAddition.setSize(pMain.getWidth(), 100);
    }

    public void addTopicAddListener(ActionListener topicAddListener) {
        bAddTopic.addActionListener(topicAddListener);
    }

    public String getTopicFromField() {
        return tfTopicAddition.getText();
    }

    public JTextField getTfTopicAddition() {
        return this.tfTopicAddition;
    }

    public void addBackListener(ActionListener addBackListener) {
        bBack.addActionListener(addBackListener);
    }
}
