import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import static javax.swing.ScrollPaneConstants.*;

public class Main {
    private static final String GIT_DIRECTORY = System.getProperty("user.home") + "/git";

    private static final String GETTING_STARTED = "Please select a blob on the left!";
    private static final int ROWS = 25;
    private static final int COLUMNS = 80;

    private static final Font FONT_EXPLORER = new Font("SansSerif", Font.PLAIN, 20);
    private static final Font FONT_PAYLOAD = new Font("Monospaced", Font.PLAIN, 24);

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

    private static void createUi(List<GitBlob> allGitBlobs) {
        JFrame frame = new JFrame("Pangit");
        JPanel explorerPanel = new JPanel(new BorderLayout());
        JPanel searchPanel = new JPanel();
        searchPanel.setLayout(new BoxLayout(searchPanel, BoxLayout.Y_AXIS));

        JTextField searchField = new JTextField();
        searchField.setFont(FONT_EXPLORER);
        searchPanel.add(searchField);

        JPanel searchOptions = new JPanel();
        JCheckBox optionCaseSensitive = new JCheckBox("case sensitive");
        optionCaseSensitive.setFont(FONT_EXPLORER);
        searchOptions.add(optionCaseSensitive);
        optionCaseSensitive.addActionListener(event -> {
            searchField.requestFocusInWindow();
        });

        JCheckBox optionRegex = new JCheckBox("regex");
        optionRegex.setFont(FONT_EXPLORER);
        searchOptions.add(optionRegex);
        optionRegex.addActionListener(event -> {
            searchField.requestFocusInWindow();
        });
        searchPanel.add(searchOptions);
        explorerPanel.add(searchPanel, BorderLayout.NORTH);

        DefaultListModel<GitBlob> filteredGitBlobsModel = new DefaultListModel<>();
        // filteredGitBlobsModel.addAll(allGitBlobs);
        allGitBlobs.forEach(filteredGitBlobsModel::addElement);
        JList<GitBlob> filteredGitBlobs = new JList<>(filteredGitBlobsModel);
        filteredGitBlobs.setFont(FONT_EXPLORER);
        explorerPanel.add(new JScrollPane(filteredGitBlobs, VERTICAL_SCROLLBAR_AS_NEEDED, HORIZONTAL_SCROLLBAR_NEVER), BorderLayout.CENTER);
        frame.add(explorerPanel, BorderLayout.WEST);

        JTextArea payloadArea = new JTextArea(GETTING_STARTED, ROWS, COLUMNS);
        payloadArea.setFont(FONT_PAYLOAD);
        payloadArea.setEditable(false);
        DefaultCaret caret = (DefaultCaret) payloadArea.getCaret();
        caret.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
        frame.add(new JScrollPane(payloadArea, VERTICAL_SCROLLBAR_AS_NEEDED, HORIZONTAL_SCROLLBAR_AS_NEEDED), BorderLayout.CENTER);

        searchField.addActionListener(new ActionListener() {
            private String lastSearchText;
            private int lastSearchFlags;

            private int searchFlags() {
                int flags = Pattern.UNICODE_CHARACTER_CLASS;
                if (!optionCaseSensitive.isSelected()) {
                    flags |= Pattern.CASE_INSENSITIVE;
                }
                if (!optionRegex.isSelected()) {
                    flags |= Pattern.LITERAL;
                }
                return flags;
            }

            private void searchPanelComponentsSetEnabled(boolean enabled) {
                searchField.setEnabled(enabled);
                optionCaseSensitive.setEnabled(enabled);
                optionRegex.setEnabled(enabled);
            }

            @Override
            public void actionPerformed(ActionEvent event) {
                String searchText = searchField.getText();
                int searchFlags = searchFlags();
                if (searchText.equals(lastSearchText) && searchFlags == lastSearchFlags) {
                    return;
                }

                Pattern pattern;
                try {
                    pattern = Pattern.compile(searchText, searchFlags);
                } catch (PatternSyntaxException ex) {
                    payloadArea.setText(ex.getMessage());
                    return;
                }

                lastSearchText = searchText;
                lastSearchFlags = searchFlags;
                filteredGitBlobsModel.clear();

                if (searchText.isEmpty()) {
                    // filteredGitBlobsModel.addAll(allGitBlobs);
                    allGitBlobs.forEach(filteredGitBlobsModel::addElement);
                    return;
                }

                searchPanelComponentsSetEnabled(false);

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
                        searchPanelComponentsSetEnabled(true);
                        searchField.requestFocusInWindow();
                    }
                }.execute();
            }
        });

        filteredGitBlobs.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        filteredGitBlobs.addListSelectionListener(event -> {
            GitBlob gitBlob = filteredGitBlobs.getSelectedValue();
            if (event.getValueIsAdjusting() || gitBlob == null) {
                return;
            }

            frame.setTitle(gitBlob.path.toString());
            try {
                payloadArea.setText(gitBlob.payload());
            } catch (IOException ex) {
                payloadArea.setText(ex.getMessage());
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

    private static boolean isControlRespectivelyCommandDown(InputEvent event) {
        return (event.getModifiersEx() & CTRL_RESPECTIVELY_META) != 0;
    }

    private static final int CTRL_RESPECTIVELY_META = OperatingSystem.isMacintosh ? InputEvent.META_DOWN_MASK : InputEvent.CTRL_DOWN_MASK;
}
