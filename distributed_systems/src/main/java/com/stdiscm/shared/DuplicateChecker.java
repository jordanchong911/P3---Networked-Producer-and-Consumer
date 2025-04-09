package com.stdiscm.shared;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.Set;

public class DuplicateChecker {
    private final Set<String> knownVideoHashes = new HashSet<>();

    public String computeFileHash(File file) {
        try (InputStream fis = new FileInputStream(file)) {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] buffer = new byte[4096];
            int n;
            while ((n = fis.read(buffer)) != -1) {
                digest.update(buffer, 0, n);
            }
            byte[] hashBytes = digest.digest();
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (IOException | NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public boolean isDuplicate(String fileHash) {
        synchronized (knownVideoHashes) {
            return knownVideoHashes.contains(fileHash);
        }
    }
    
    public void register(String fileHash) {
        synchronized (knownVideoHashes) {
            knownVideoHashes.add(fileHash);
        }
    }
    
}
