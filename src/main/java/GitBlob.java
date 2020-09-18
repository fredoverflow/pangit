import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.zip.InflaterInputStream;

public class GitBlob implements Comparable<GitBlob> {
    private static final Pattern GIT_OBJECT = Pattern.compile(".*[/\\\\]\\.git[/\\\\]objects[/\\\\][0-9a-f]{2}[/\\\\][0-9a-f]{38}");
    private static final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd  HH:mm:ss");

    public final long lastModified;
    public final Path path;
    public final int payloadStart;
    public final int payloadSize;
    private CharacterEncoding characterEncoding;

    private GitBlob(Path path, int payloadStart, int payloadSize) {
        this.lastModified = path.toFile().lastModified();
        this.path = path;
        this.payloadStart = payloadStart;
        this.payloadSize = payloadSize;
    }

    public static Stream<GitBlob> findGitBlobs(Path root, Consumer<GitBlob> processGitBlob) throws IOException {
        return Files.walk(root)
                .filter(GitBlob::isGitObject)
                .map(GitBlob::gitBlobOrNull)
                .filter(Objects::nonNull)
                .peek(processGitBlob)
                .sorted();
    }

    private static boolean isGitObject(Path path) {
        return GIT_OBJECT.matcher(path.toString()).matches();
    }

    private static GitBlob gitBlobOrNull(Path path) {
        try (InputStream inputStream = new InflaterInputStream(new FileInputStream(path.toFile()))) {
            byte[] header = new byte[16]; // "blob 2147483647\0"
            inputStream.read(header);
            return isBlobHeader(header) ? parseBlobHeader(path, header) : null;
        } catch (IOException ex) {
            return null;
        }
    }

    private static boolean isBlobHeader(byte[] header) {
        return header.length >= 6 &&
                header[0] == 'b' &&
                header[1] == 'l' &&
                header[2] == 'o' &&
                header[3] == 'b' &&
                header[4] == ' ' &&
                header[5] >= '0' && header[5] <= '9';
        // We could check for additional digits and the NUL terminator
        // just to be safe, but that would probably be overkill.
    }

    private static GitBlob parseBlobHeader(Path path, byte[] header) {
        int payloadStart = 5; // "blob "
        int payloadSize = 0;
        byte b;
        while ((b = header[payloadStart++]) != 0) {
            payloadSize = payloadSize * 10 + (b & 0b1111);
        }
        return new GitBlob(path, payloadStart, payloadSize);
    }

    public byte[] unzip() throws IOException {
        try (InputStream inputStream = new InflaterInputStream(new FileInputStream(path.toFile()))) {
            byte[] unzipped = new byte[payloadStart + payloadSize];
            int offset = 0;
            int chunkSize = inputStream.read(unzipped);
            while (chunkSize > 0) {
                offset += chunkSize;
                chunkSize = inputStream.read(unzipped, offset, unzipped.length - offset);
            }
            return unzipped;
        }
    }

    public String payload() throws IOException {
        byte[] unzipped = unzip();
        if (characterEncoding == null) {
            characterEncoding = CharacterEncoding.guess(unzipped, payloadStart);
        }
        return characterEncoding.decode(unzipped, payloadStart);
    }

    @Override
    public int compareTo(GitBlob that) {
        // most recently modified first
        return Long.compare(that.lastModified, this.lastModified);
    }

    @Override
    public String toString() {
        // JList displays toString representation
        return dateFormat.format(new Date(lastModified)) + "   " + payloadSize;
    }
}
