import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

public class GitBlob implements Comparable<GitBlob> {
    private static final Pattern GIT_OBJECT = Pattern.compile(".*[/\\\\]\\.git[/\\\\]objects[/\\\\][0-9a-f]{2}[/\\\\][0-9a-f]{38}");
    private static final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd  HH:mm:ss");

    public final long lastModified;
    public final Path path;
    public final int payloadStart;
    public final int payloadSize;

    private GitBlob(Path path, int payloadStart, int payloadSize) {
        this.lastModified = path.toFile().lastModified();
        this.path = path;
        this.payloadStart = payloadStart;
        this.payloadSize = payloadSize;
    }

    public static Stream<GitBlob> findGitBlobs(Path root) throws IOException {
        return Files.walk(root)
                .filter(GitBlob::isGitObject)
                .map(GitBlob::gitBlobOrNull)
                .filter(Objects::nonNull)
                .sorted();
    }

    private static boolean isGitObject(Path path) {
        return GIT_OBJECT.matcher(path.toString()).matches();
    }

    private static GitBlob gitBlobOrNull(Path path) {
        try {
            FileInputStream inputStream = new FileInputStream(path.toFile());
            byte[] zipped = new byte[4096]; // NTFS cluster size
            inputStream.read(zipped);

            Inflater inflater = new Inflater();
            inflater.setInput(zipped);

            byte[] header = new byte[16]; // "blob 2147483647\0"
            inflater.inflate(header);
            return isBlobHeader(header) ? parseBlobHeader(path, header) : null;
        } catch (IOException | DataFormatException ex) {
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
        try {
            byte[] zipped = Files.readAllBytes(path);
            Inflater inflater = new Inflater();
            inflater.setInput(zipped);

            byte[] unzipped = new byte[payloadStart + payloadSize];
            inflater.inflate(unzipped);
            return unzipped;
        } catch (DataFormatException ex) {
            throw new IOException(ex);
        }
    }

    public String payloadAsUtf8() throws IOException {
        return new String(unzip(), payloadStart, payloadSize, StandardCharsets.UTF_8);
    }

    @Override
    public int compareTo(GitBlob that) {
        // most recently modified first
        return Long.compare(that.lastModified, this.lastModified);
    }

    @Override
    public String toString() {
        // JList displays toString representation
        return dateFormat.format(new Date(lastModified));
    }
}
