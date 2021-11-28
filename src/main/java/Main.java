import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultCaret;
import javax.swing.text.Highlighter;
import java.awt.*;
import java.awt.event.InputEvent;
import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import static javax.swing.ScrollPaneConstants.*;
import static javax.swing.text.DefaultHighlighter.DefaultHighlightPainter;

public class Main {
    private static final String GIT_DIRECTORY = System.getProperty("user.home") + "/git";

    private static final String GETTING_STARTED = "Please select a blob on the left!";
    private static final int ROWS = 25;
    private static final int COLUMNS = 80;

    private static final DefaultHighlightPainter highlightPainter = new DefaultHighlightPainter(Color.CYAN);

    private static SearchPanel searchPanel;
    private static List<GitBlob> allGitBlobs;
    private static DefaultListModel<GitBlob> filteredGitBlobsModel;
    private static JList<GitBlob> filteredGitBlobs;
    private static JTextArea payloadArea;

    public static void main(String[] args) {
        EventQueue.invokeLater(Main::selectDirectory);
    }

    private static void selectDirectory() {
        JFileChooser chooser = new JFileChooser(GIT_DIRECTORY);
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int chosenOption = chooser.showOpenDialog(null);
        if (chosenOption == JFileChooser.APPROVE_OPTION) {
            BackgroundScanner.scan(chooser.getSelectedFile(), Main::createUi);
        }
    }

    private static void createUi(List<GitBlob> gitBlobs) {
        JFrame frame = new JFrame("Pangit");
        JPanel explorerPanel = new JPanel(new BorderLayout());
        searchPanel = new SearchPanel(Main::updateHighlights, Main::filterGitBlobs);
        explorerPanel.add(searchPanel, BorderLayout.NORTH);

        allGitBlobs = gitBlobs;
        filteredGitBlobsModel = new DefaultListModel<>();
        // filteredGitBlobsModel.addAll(allGitBlobs);
        allGitBlobs.forEach(filteredGitBlobsModel::addElement);
        filteredGitBlobs = new JList<>(filteredGitBlobsModel);
        filteredGitBlobs.setFont(Fonts.EXPLORER);
        explorerPanel.add(new JScrollPane(filteredGitBlobs, VERTICAL_SCROLLBAR_AS_NEEDED, HORIZONTAL_SCROLLBAR_NEVER), BorderLayout.CENTER);
        frame.add(explorerPanel, BorderLayout.WEST);

        payloadArea = new JTextArea(GETTING_STARTED, ROWS, COLUMNS);
        payloadArea.setFont(Fonts.PAYLOAD);
        payloadArea.setEditable(false);
        DefaultCaret caret = (DefaultCaret) payloadArea.getCaret();
        caret.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
        frame.add(new JScrollPane(payloadArea, VERTICAL_SCROLLBAR_AS_NEEDED, HORIZONTAL_SCROLLBAR_AS_NEEDED), BorderLayout.CENTER);

        filteredGitBlobs.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        filteredGitBlobs.addListSelectionListener(event -> {
            GitBlob gitBlob = filteredGitBlobs.getSelectedValue();
            if (event.getValueIsAdjusting() || gitBlob == null) {
                return;
            }

            try {
                String payload = gitBlob.payload();

                frame.setTitle(gitBlob.path.toString());
                payloadArea.setText(payload);
                updateHighlights();
            } catch (IOException ex) {
                payloadArea.setText(ex.toString());
            }
        });

        payloadArea.addMouseWheelListener(event -> {
            if (isControlRespectivelyCommandDown(event)) {
                Font oldFont = payloadArea.getFont();
                int oldSize = oldFont.getSize();
                int newSize = oldSize - event.getWheelRotation();
                if (newSize > 0) {
                    Font newFont = oldFont.deriveFont((float) newSize);
                    payloadArea.setFont(newFont);
                }
            } else {
                payloadArea.getParent().dispatchEvent(event);
            }
        });

        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

    private static void updateHighlights() {
        try {
            Highlighter highlighter = payloadArea.getHighlighter();
            highlighter.removeAllHighlights();

            if (searchPanel.isEmpty()) {
                return;
            }

            Pattern pattern = searchPanel.compilePattern();
            String payload = payloadArea.getText();
            Matcher matcher = pattern.matcher(payload);
            if (matcher.find()) {
                payloadArea.getCaret().setDot(matcher.start());
                do {
                    highlighter.addHighlight(matcher.start(), matcher.end(), highlightPainter);
                } while (matcher.find());
            }
        } catch (PatternSyntaxException ignored) {
        } catch (BadLocationException bug) {
            throw new RuntimeException(bug);
        }
    }

    private static void filterGitBlobs() {
        Pattern pattern;
        try {
            pattern = searchPanel.compilePattern();
        } catch (PatternSyntaxException ex) {
            payloadArea.setText(ex.getMessage());
            return;
        }

        filteredGitBlobsModel.clear();

        if (searchPanel.isEmpty()) {
            // filteredGitBlobsModel.addAll(allGitBlobs);
            allGitBlobs.forEach(filteredGitBlobsModel::addElement);
            return;
        }

        searchPanel.setEnabled(false);

        new SwingWorker<Void, GitBlob>() {
            @Override
            protected Void doInBackground() {
                for (GitBlob gitBlob : allGitBlobs) {
                    try {
                        if (pattern.matcher(gitBlob.payload()).find()) {
                            publish(gitBlob);
                        }
                    } catch (IOException ex) {
                        EventQueue.invokeLater(() -> {
                            payloadArea.setText(ex.getMessage());
                        });
                    }
                }
                return null;
            }

            @Override
            protected void process(List<GitBlob> matchingGitBlobs) {
                // filteredGitBlobsModel.addAll(matchingGitBlobs);
                matchingGitBlobs.forEach(filteredGitBlobsModel::addElement);
            }

            @Override
            protected void done() {
                searchPanel.setEnabled(true);
                searchPanel.requestFocusInWindow();
            }
        }.execute();
    }

    private static boolean isControlRespectivelyCommandDown(InputEvent event) {
        return (event.getModifiersEx() & CTRL_RESPECTIVELY_META) != 0;
    }

    private static final int CTRL_RESPECTIVELY_META = Platform.isMacintosh ? InputEvent.META_DOWN_MASK : InputEvent.CTRL_DOWN_MASK;
}
