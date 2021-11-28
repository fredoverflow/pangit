import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class SearchPanel extends JPanel {
    private final JTextField searchField;
    private final JCheckBox optionCaseSensitive;
    private final JCheckBox optionRegex;

    public SearchPanel(Runnable onChange, Runnable onEnter) {
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        searchField = new JTextField();
        searchField.setFont(Fonts.EXPLORER);
        this.add(searchField);

        JPanel searchOptions = new JPanel();
        optionCaseSensitive = new JCheckBox("case sensitive");
        optionCaseSensitive.setFont(Fonts.EXPLORER);
        searchOptions.add(optionCaseSensitive);
        optionCaseSensitive.addActionListener(event -> {
            searchField.requestFocusInWindow();
            onChange.run();
        });

        optionRegex = new JCheckBox("regex");
        optionRegex.setFont(Fonts.EXPLORER);
        searchOptions.add(optionRegex);
        optionRegex.addActionListener(event -> {
            searchField.requestFocusInWindow();
            onChange.run();
        });

        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent event) {
                onChange.run();
            }

            @Override
            public void removeUpdate(DocumentEvent event) {
                onChange.run();
            }

            @Override
            public void changedUpdate(DocumentEvent event) {
                onChange.run();
            }
        });

        searchField.addActionListener(event -> onEnter.run());

        this.add(searchOptions);
    }

    public boolean isEmpty() {
        return searchField.getText().isEmpty();
    }

    public Pattern compilePattern() throws PatternSyntaxException {
        try {
            Pattern pattern = Pattern.compile(searchField.getText(), searchFlags());
            searchField.setBackground(Color.WHITE);
            searchField.setToolTipText("");
            return pattern;
        } catch (PatternSyntaxException ex) {
            searchField.setBackground(Color.PINK);
            searchField.setToolTipText(ex.getMessage());
            throw ex;
        }
    }

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

    public void setEnabled(boolean enabled) {
        searchField.setEnabled(enabled);
        optionCaseSensitive.setEnabled(enabled);
        optionRegex.setEnabled(enabled);
    }

    public boolean requestFocusInWindow() {
        return searchField.requestFocusInWindow();
    }
}
