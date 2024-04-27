package dev.skydynamic.quickbackupmulti.utils.hash;

import dev.skydynamic.quickbackupmulti.utils.storage.FileHashes;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;

public class HashUtils {

    public static String getFileHash(Path file) throws Exception {
        byte[] fileData = Files.readAllBytes(file);

        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(fileData);
        byte[] digest = md.digest();

        return bytesToHex(digest);
    }

    public static boolean compareFileHash(FileHashes fileHashes, File sourceFilePath, File sourceFile, String sourceFileHash) {
        String fileHash = fileHashes.getFileHashes(sourceFilePath, sourceFile);
        return sourceFileHash.equals(fileHash);
    }

    private static String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder(2 * hash.length);
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if(hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
