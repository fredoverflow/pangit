import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.io.IOException;

import static javax.swing.ScrollPaneConstants.*;

public class Main {
    private static final String GIT_DIRECTORY = System.getProperty("user.home") + "/git";

    private static final String GETTING_STARTED = "Please select a blob on the left!";
    private static final int ROWS = 25;
    private static final int COLUMNS = 80;

    private static final Font FONT_DATE = new Font("SansSerif", Font.PLAIN, 20);
    private static final Font FONT_PAYLOAD = new Font("Monospaced", Font.PLAIN, 24);

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Main::selectDirectory);
    }

    private static void selectDirectory() {
        JFileChooser chooser = new JFileChooser(GIT_DIRECTORY);
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int chosenOption = chooser.showOpenDialog(null);
        if (chosenOption == JFileChooser.APPROVE_OPTION) {
            BackgroundScanner.scan(chooser.getSelectedFile(), Main::createUi);
        }
    }

    private static void createUi(GitBlob[] listData) {
        JFrame frame = new JFrame("Pangit");

        JList<GitBlob> gitBlobs = new JList<>(listData);
        gitBlobs.setFont(FONT_DATE);
        frame.add(new JScrollPane(gitBlobs, VERTICAL_SCROLLBAR_AS_NEEDED, HORIZONTAL_SCROLLBAR_NEVER), BorderLayout.WEST);

        JTextArea payload = new JTextArea(GETTING_STARTED, ROWS, COLUMNS);
        payload.setFont(FONT_PAYLOAD);
        payload.setEditable(false);
        DefaultCaret caret = (DefaultCaret) payload.getCaret();
        caret.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
        frame.add(new JScrollPane(payload, VERTICAL_SCROLLBAR_AS_NEEDED, HORIZONTAL_SCROLLBAR_AS_NEEDED), BorderLayout.CENTER);

        gitBlobs.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        gitBlobs.addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) {
                GitBlob gitBlob = gitBlobs.getSelectedValue();
                frame.setTitle(gitBlob.path.toString());
                try {
                    payload.setText(gitBlob.payload());
                } catch (IOException ex) {
                    payload.setText(ex.toString());
                }
            }
        });

        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }
}
