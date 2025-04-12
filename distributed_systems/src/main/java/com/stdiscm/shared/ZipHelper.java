package com.stdiscm.shared;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.zip.*;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;

public class ZipHelper {

    // Compress a single file into a zip archive
    public static boolean compressFile(File inputFile, File zipFile) {
        try (FileOutputStream fos = new FileOutputStream(zipFile);
             ZipOutputStream zos = new ZipOutputStream(fos);
             FileInputStream fis = new FileInputStream(inputFile)) {

            ZipEntry zipEntry = new ZipEntry(inputFile.getName());
            zos.putNextEntry(zipEntry);

            byte[] buffer = new byte[4096];
            int len;
            while ((len = fis.read(buffer)) > 0) {
                zos.write(buffer, 0, len);
            }

            zos.closeEntry();
            return true;
        } catch (IOException e) {
            System.err.println("Compression failed: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private static final List<String> VIDEO_EXTS = Arrays.asList(".mp4", ".avi", ".mov", ".mkv");
    
        public static File tryExtract(File video) {
        if (video.getName().toLowerCase().endsWith(".zip")) {
            File tempDir = null;
            try {
                tempDir = Files.createTempDirectory("extractedZip").toFile();
                // Validate the ZIP up front
                validateZip(video);
                // Extract the first video entry
                File extracted = extractVideoFromZip(video, tempDir);
                if (extracted != null) {
                    return extracted;
                } else {
                    System.err.println("No video file found in the zip archive: " + video.getName());
                }
            } catch (IOException e) {
                System.err.println("Error handling ZIP file " + video.getName() + ": " + e.getMessage());
                e.printStackTrace();
            }
        }
        return video;
    }

    /**
     * Validates that the ZIP file can be fully traversed without truncation.
     * Throws IOException (or EOFException) if the archive is corrupt or incomplete.
     */
    private static void validateZip(File zipFile) throws IOException {
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile))) {
            while (zis.getNextEntry() != null) {
                // just advance through entries
            }
        }
    }

    /**
     * Extracts the first video file found in the ZIP archive using ZipFile
     * (which reads the central directory up-front for better error reporting).
     * Returns the extracted File, or null if none found.
     */
    public static File extractVideoFromZip(File zipFile, File destDir) throws IOException {
        File videoFile = null;

        try (ZipFile zf = new ZipFile(zipFile)) {
            Enumeration<? extends ZipEntry> entries = zf.entries();

            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                if (entry.isDirectory()) {
                    continue;
                }

                String name = entry.getName().toLowerCase();
                boolean isVideo = VIDEO_EXTS.stream().anyMatch(name::endsWith);
                if (!isVideo) {
                    continue;
                }

                // Prepare output file
                File outFile = new File(destDir, Paths.get(entry.getName()).getFileName().toString());
                outFile.getParentFile().mkdirs();

                // Copy entry contents
                try (InputStream in = zf.getInputStream(entry);
                     OutputStream out = new FileOutputStream(outFile)) {

                    byte[] buffer = new byte[8192];
                    int bytesRead;
                    while ((bytesRead = in.read(buffer)) != -1) {
                        out.write(buffer, 0, bytesRead);
                    }
                }

                videoFile = outFile;
                break;  // stop after first video
            }
        } catch (EOFException eof) {
            // Enhanced logging for truncated archives
            System.err.println("ZIP appears truncated or corrupt: " + zipFile.getAbsolutePath());
            throw eof;
        }

        return videoFile;
    }
}
