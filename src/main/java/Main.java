import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import static javax.swing.ScrollPaneConstants.*;

public class Main {
    private static final String GIT_DIRECTORY = System.getProperty("user.home") + "/git";

    private static JFrame frame;
    private static SearchPanel searchPanel;
    private static List<GitBlob> allGitBlobs;
    private static DefaultListModel<GitBlob> filteredGitBlobsModel;
    private static JList<GitBlob> filteredGitBlobs;
    private static PayloadArea payloadArea;

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
        frame = new JFrame("Pangit");
        JPanel explorerPanel = new JPanel(new BorderLayout());
        searchPanel = new SearchPanel(Main::updateHighlights, Main::filterGitBlobs);
        explorerPanel.add(searchPanel, BorderLayout.NORTH);

        allGitBlobs = gitBlobs;
        filteredGitBlobsModel = new DefaultListModel<>();
        filteredGitBlobsModel.addAll(allGitBlobs);
        filteredGitBlobs = new JList<>(filteredGitBlobsModel);
        filteredGitBlobs.setFont(Fonts.EXPLORER);
        explorerPanel.add(new JScrollPane(filteredGitBlobs, VERTICAL_SCROLLBAR_AS_NEEDED, HORIZONTAL_SCROLLBAR_NEVER), BorderLayout.CENTER);
        frame.add(explorerPanel, BorderLayout.WEST);

        payloadArea = new PayloadArea();
        frame.add(new JScrollPane(payloadArea, VERTICAL_SCROLLBAR_AS_NEEDED, HORIZONTAL_SCROLLBAR_AS_NEEDED), BorderLayout.CENTER);

        filteredGitBlobs.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        filteredGitBlobs.addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) {
                GitBlob gitBlob = filteredGitBlobs.getSelectedValue();
                if (gitBlob != null) {
                    onGitBlobSelected(gitBlob);
                }
            }
        });

        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

    private static void onGitBlobSelected(GitBlob gitBlob) {
        // JTextArea.setText is a UX bottleneck on large blobs when executed in the UI thread.
        // (In particular, selecting a blob did not immediately highlight it as selected,
        // and the user was left uncertain for a short period whether they actually selected it.)
        // Fortunately, JTextArea.setText uses PlainDocument.replace, which is thread-safe.
        gitBlobExecutor.execute(() -> {
            try {
                payloadArea.setText(gitBlob.payload());
                EventQueue.invokeLater(() -> {
                    frame.setTitle(gitBlob.path.toString());
                    updateHighlights();
                });
            } catch (IOException ex) {
                payloadArea.setText(ex.toString());
            }
        });
    }

    // Selecting a large blob and immediately selecting a small blob thereafter
    // could display the outdated large blob instead of the actually selected small blob.
    // SingleThreadExecutor stalls the small blob until the large blob is displayed.
    private static final Executor gitBlobExecutor = Executors.newSingleThreadExecutor();

    private static void updateHighlights() {
        try {
            payloadArea.updateHighlights(searchPanel.compilePattern());
        } catch (PatternSyntaxException ignored) {
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

        if (pattern.pattern().isEmpty()) {
            filteredGitBlobsModel.addAll(allGitBlobs);
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
                        payloadArea.setText(ex.getMessage());
                    }
                }
                return null;
            }

            @Override
            protected void process(List<GitBlob> matchingGitBlobs) {
                filteredGitBlobsModel.addAll(matchingGitBlobs);
            }

            @Override
            protected void done() {
                searchPanel.setEnabled(true);
                searchPanel.requestFocusInWindow();
            }
        }.execute();
    }
}
