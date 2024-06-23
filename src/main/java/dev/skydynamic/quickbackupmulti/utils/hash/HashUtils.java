package dev.skydynamic.quickbackupmulti.utils.hash;

import dev.skydynamic.quickbackupmulti.config.Config;
import dev.skydynamic.quickbackupmulti.storage.FileHashes;
import net.jpountz.xxhash.XXHashFactory;

import java.io.File;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;

import static dev.skydynamic.quickbackupmulti.QuickBackupMulti.LOGGER;

public class HashUtils {

    private static final String SHA_256 = "SHA-256";
    private final static XXHashFactory XX_HASH_FACTORY = XXHashFactory.fastestInstance();
    private static Boolean useFastHash;

    public static String getFileHash(Path file, String algorithm) throws Exception {
        if (useFastHash == null) {
            useFastHash = Config.INSTANCE.getUseFastHash();
            LOGGER.info("Backup UseFastHash => {}", useFastHash);
        }
        if (algorithm == null) {
            algorithm = SHA_256;
        }
        byte[] fileData;
        byte[] digest;
        if (useFastHash) {
            String fileName = file.getFileName().toString();
            boolean isAnvil = fileName.startsWith("r.") && fileName.endsWith(".mca");
            if (isAnvil) {
                /*
                   对于(Anvil)区块文件来说，只需要校验8kB文件头即可得知区块文件是否有变动
                   区块位置：0 - 4095 每个区块占用4个字节，存储区块位置，未生成则为0填充
                   区块更新时间戳：4096 - 8191 每个区块占用4个字节，存储区块更新的时间戳
                 */
                try (InputStream is = Files.newInputStream(file)) {
                    fileData = new byte[8192];
                    is.read(fileData, 0, fileData.length);
                }
            } else {
                fileData = Files.readAllBytes(file);
            }
            // 使用高性能非加密哈希算法
            digest = longToBytes(XX_HASH_FACTORY.hash64().hash(fileData, 0, fileData.length, 0));
            return bytesToHex(digest);
        }
        fileData = Files.readAllBytes(file);
        MessageDigest md = MessageDigest.getInstance(algorithm);
        md.update(fileData);
        digest = md.digest();
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
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    public static byte[] longToBytes(long l) {
        //分配缓冲区，单位为字节，一个long类型占8个字节，所以分配为8
        ByteBuffer byteBuffer = ByteBuffer.allocate(Long.SIZE / Byte.SIZE);
        //参数一为起始位置（不指定默认为0），参数二为所放的值
        byteBuffer.putLong(0, l);
        return byteBuffer.array();
    }
}
