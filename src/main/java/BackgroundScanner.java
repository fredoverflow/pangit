import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class BackgroundScanner {
    private static final Font FONT_PROGRESS = new Font("SansSerif", Font.PLAIN, 48);

    public static void scan(File selectedDirectory, Consumer<GitBlob[]> processGitBlobs) {
        JDialog dialog = new JDialog((JDialog) null, "Scanning...", false);

        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setStringPainted(true);
        progressBar.setString("0");
        progressBar.setFont(FONT_PROGRESS);
        dialog.add(progressBar);

        dialog.pack();
        dialog.setLocationRelativeTo(null); // center
        dialog.setVisible(true);

        new SwingWorker<GitBlob[], GitBlob>() {
            private int countGitBlobs;

            @Override
            protected GitBlob[] doInBackground() throws Exception {
                try (Stream<GitBlob> gitBlobs = GitBlob.findGitBlobs(selectedDirectory.toPath(), this::publish)) {
                    return gitBlobs.toArray(GitBlob[]::new);
                }
            }

            @Override
            protected void process(List<GitBlob> recentGitBlobs) {
                countGitBlobs += recentGitBlobs.size();
                progressBar.setString("" + countGitBlobs);
            }

            @Override
            protected void done() {
                dialog.dispose();
                try {
                    processGitBlobs.accept(get());
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(null,
                            ex.getMessage(),
                            ex.getClass().getName(),
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }
}
