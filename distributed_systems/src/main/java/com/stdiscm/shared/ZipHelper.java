package com.stdiscm.shared;

import java.io.*;
import java.util.zip.*;
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

    public static File extractVideoFromZip(File zipFile, File destDir, List<String> videoExts) throws IOException {
           File videoFile = null;
           try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile))) {
               ZipEntry entry;
               while ((entry = zis.getNextEntry()) != null) {
                   if (entry.isDirectory()) continue;
   
                   final String entryName = entry.getName().toLowerCase();
                   boolean isVideo = videoExts.stream().anyMatch(entryName::endsWith);
   
                   if (isVideo) {
                       File outFile = new File(destDir, new File(entry.getName()).getName());
                       outFile.getParentFile().mkdirs();
   
                       try (FileOutputStream fos = new FileOutputStream(outFile)) {
                           byte[] buffer = new byte[1024];
                           int len;
                           while ((len = zis.read(buffer)) > 0) {
                               fos.write(buffer, 0, len);
                           }
                       }
   
                       videoFile = outFile;
                       break; // Stop after extracting the first video file.
                   }
               }
           }
           return videoFile;
       }
}
