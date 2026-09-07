import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/** Checks the final APK: Android AssetManager.openFd cannot open deflated audio. */
public final class VerifyAndroidAudio {
    private static final Set<String> AUDIO_EXTENSIONS = Set.of(
        "mp3", "wav", "ogg", "m4a", "aac", "flac", "mid", "midi");

    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            throw new IllegalArgumentException("Usage: java VerifyAndroidAudio.java APK ASSETS_DIRECTORY");
        }
        Path assetsDirectory = Path.of(args[1]);
        List<String> failures = new ArrayList<>();
        int checked = 0;
        try (ZipFile apk = new ZipFile(args[0]); var assetPaths = Files.walk(assetsDirectory)) {
            for (Path asset : assetPaths.filter(Files::isRegularFile).toList()) {
                String name = asset.getFileName().toString().toLowerCase(Locale.ROOT);
                int extensionStart = name.lastIndexOf('.') + 1;
                if (!AUDIO_EXTENSIONS.contains(name.substring(extensionStart))) {
                    continue;
                }
                checked++;
                String entryName = "assets/" + assetsDirectory.relativize(asset)
                    .toString().replace(File.separatorChar, '/');
                ZipEntry entry = apk.getEntry(entryName);
                if (entry == null) {
                    failures.add(entryName + " is missing from the APK");
                } else if (entry.getMethod() != ZipEntry.STORED) {
                    failures.add(entryName + " is compressed (ZIP method " + entry.getMethod()
                        + "); Android AssetManager.openFd requires STORED audio");
                } else if (entry.getSize() != Files.size(asset)) {
                    failures.add(entryName + " has a different size from the source asset");
                }
            }
        }
        if (checked == 0) {
            throw new IOException("No source audio assets found below " + assetsDirectory);
        }
        if (!failures.isEmpty()) {
            throw new IOException("Android audio packaging failed:\n" + String.join("\n", failures));
        }
        System.out.println("Android audio packaging: " + checked + " assets present and stored uncompressed");
    }
}
