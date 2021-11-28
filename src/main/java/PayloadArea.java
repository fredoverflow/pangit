import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultCaret;
import javax.swing.text.Highlighter;
import java.awt.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import static javax.swing.text.DefaultHighlighter.DefaultHighlightPainter;

public class PayloadArea extends JTextArea {
    public static final String GETTING_STARTED = "Please select a blob on the left!";
    public static final int ROWS = 25;
    public static final int COLUMNS = 80;

    private static final DefaultHighlightPainter highlightPainter = new DefaultHighlightPainter(Color.CYAN);

    public PayloadArea() {
        super(GETTING_STARTED, ROWS, COLUMNS);

        this.setFont(Fonts.PAYLOAD);
        this.setEditable(false);
        DefaultCaret caret = (DefaultCaret) this.getCaret();
        caret.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);

        this.addMouseWheelListener(event -> {
            if (Platform.isControlRespectivelyCommandDown(event)) {
                Font oldFont = this.getFont();
                int oldSize = oldFont.getSize();
                int newSize = oldSize - event.getWheelRotation();
                if (newSize > 0) {
                    Font newFont = oldFont.deriveFont((float) newSize);
                    this.setFont(newFont);
                }
            } else {
                this.getParent().dispatchEvent(event);
            }
        });
    }

    public void updateHighlights(Pattern pattern) {
        try {
            Highlighter highlighter = this.getHighlighter();
            highlighter.removeAllHighlights();

            if (pattern.pattern().isEmpty()) {
                return;
            }

            String payload = this.getText();
            Matcher matcher = pattern.matcher(payload);
            if (matcher.find()) {
                this.getCaret().setDot(matcher.start());
                do {
                    highlighter.addHighlight(matcher.start(), matcher.end(), highlightPainter);
                } while (matcher.find());
            }
        } catch (PatternSyntaxException ignored) {
        } catch (BadLocationException bug) {
            throw new RuntimeException(bug);
        }
    }
}
