import javax.swing.*;
import java.io.File;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BackgroundScanner {
    public static void scan(File selectedDirectory, Consumer<List<GitBlob>> gitBlobsConsumer) {
        JDialog dialog = new JDialog((JDialog) null, "Scanning...", false);

        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setStringPainted(true);
        progressBar.setString("0");
        progressBar.setFont(Fonts.PROGRESS);
        dialog.add(progressBar);

        dialog.pack();
        dialog.setLocationRelativeTo(null); // center
        dialog.setVisible(true);

        new SwingWorker<List<GitBlob>, GitBlob>() {
            private int countGitBlobs;

            @Override
            protected List<GitBlob> doInBackground() throws Exception {
                try (Stream<GitBlob> gitBlobs = GitBlob.findGitBlobs(selectedDirectory.toPath(), this::publish)) {
                    return gitBlobs.collect(Collectors.toList());
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
                    gitBlobsConsumer.accept(get());
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
