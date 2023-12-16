package dev.skydynamic.quickbackupmulti.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import dev.skydynamic.quickbackupmulti.utils.config.Config;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.WorldSavePath;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.NameFileFilter;
import org.apache.commons.io.filefilter.NotFileFilter;

import java.io.*;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import static dev.skydynamic.quickbackupmulti.i18n.Translate.tr;

public class QbmManager {

    public static Path backupDir = Path.of(System.getProperty("user.dir") + "/QuickBackupMulti/");
    public static Path savePath = Config.TEMP_CONFIG.server.getSavePath(WorldSavePath.ROOT);
    public static IOFileFilter fileFilter = new NotFileFilter(new NameFileFilter(Config.INSTANCE.getIgnoredFiles()));

    static Gson gson = new GsonBuilder().serializeNulls().setPrettyPrinting().create();

    private static class SlotInfoStorage {
        String desc;
        long timestamp;

        public String getDesc() {
            return this.desc;
        }

        public long getTimestamp() {
            return this.timestamp;
        }

        @Override
        public int hashCode() {
            return Objects.hash(getDesc(), getTimestamp());
        }
    }

    private static void writeBackupInfo(int slot, String desc) {
        try {
            ConcurrentHashMap<String, Object> data = new ConcurrentHashMap<>();
            data.put("desc", desc);
            data.put("timestamp", System.currentTimeMillis());
            var writer = new FileWriter(backupDir.resolve("Slot" + slot + "_info.json").toFile());
            gson.toJson(data, writer);
            writer.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    private static long getDirSize(File dir) {
        return FileUtils.sizeOf(dir);
    }

    private static boolean checkSlotExist(int slot) {
        return backupDir.resolve("Slot" + slot + "_info.json").toFile().exists();
    }

    public static void restoreClient(int slot) {
        File targetBackupSlot = backupDir.resolve("Slot" + slot).toFile();
        try {
            savePath.resolve("level.dat").toFile().delete();
            savePath.resolve("level.dat_old").toFile().delete();
            File[] fileList = savePath.toFile().listFiles((FilenameFilter) fileFilter);
            if (fileList != null) {
                Arrays.sort(fileList, ((o1, o2) -> {
                    if (o1.isDirectory() && o2.isDirectory()) {
                        return -1;
                    } else if (!o1.isDirectory() && o2.isDirectory()) {
                        return 1;
                    } else {
                        return o1.compareTo(o2);
                    }
                }));
                for (File file : fileList) {
                    FileUtils.forceDelete(file);
                }
            }
            FileUtils.copyDirectory(targetBackupSlot, savePath.toFile());
        } catch (IOException e) {
            restoreClient(slot);
        }
    }

    public static void restore(int slot) {
        File targetBackupSlot = backupDir.resolve("Slot" + slot).toFile();
        try {
//            var it = Files.walk(savePath,5).sorted(Comparator.reverseOrder()).iterator();
//            while (it.hasNext()){
//                Files.delete(it.next());
//            }
           for (File file : Objects.requireNonNull(savePath.toFile().listFiles((FilenameFilter) fileFilter))) {
                FileUtils.forceDelete(file);
            }
            FileUtils.copyDirectory(targetBackupSlot, savePath.toFile());
        } catch (IOException e) {
            restore(slot);
        }
    }

    public static int make(ServerCommandSource commandSource, int slot, String desc) {
        long startTime = System.currentTimeMillis();
        if (slot == -1) {
            for (int j=1;j<=Config.INSTANCE.getNumOfSlot();j++) {
                if (!checkSlotExist(j)) {
                    slot = j;
                    break;
                }
            }
            if (slot == -1) slot = 1;
        }
        if (slot > Config.INSTANCE.getNumOfSlot() || slot < 1) {
            Messenger.sendMessage(commandSource, Text.of(tr("quickbackupmulti.make.no_slot")));
            return 0;
        }
        try {
            Messenger.sendMessage(commandSource, Text.of(tr("quickbackupmulti.make.start")));
            MinecraftServer server = commandSource.getServer();
            //#if MC>11800
            server.saveAll(true, true, true);
            //#else
            //$$ server.save(true, true, true);
            //#endif
            for (ServerWorld serverWorld : server.getWorlds()) {
                if (serverWorld == null || serverWorld.savingDisabled) continue;
                serverWorld.savingDisabled = true;
            }
            if (!backupDir.resolve("Slot" + slot).toFile().exists()) backupDir.resolve("Slot" + slot).toFile().mkdir();
            if (Objects.requireNonNull(backupDir.resolve("Slot" + slot).toFile().listFiles()).length > 0) FileUtils.deleteDirectory(backupDir.resolve("Slot" + slot).toFile());
            FileUtils.copyDirectory(savePath.toFile(), backupDir.resolve("Slot" + slot).toFile(), fileFilter);
            long endTime = System.currentTimeMillis();
            double intervalTime = (endTime - startTime) / 1000.0;
            Messenger.sendMessage(commandSource, Text.of(tr("quickbackupmulti.make.success", intervalTime)));
            writeBackupInfo(slot, desc);
            for (ServerWorld serverWorld : server.getWorlds()) {
                if (serverWorld == null || !serverWorld.savingDisabled) continue;
                serverWorld.savingDisabled = false;
            }
        } catch (IOException e) {
            Messenger.sendMessage(commandSource, Text.of(tr("quickbackupmulti.make.fail", e.getMessage())));
        }
        return 1;
    }

    public static MutableText list() {
        MutableText resultText = Messenger.literal(tr("quickbackupmulti.list_backup.title"));
        long totalBackupSizeB = 0;
        for (int j=1;j<=Config.INSTANCE.getNumOfSlot();j++) {
            try {
                MutableText backText = Messenger.literal("§2[▷] ");
                MutableText deleteText = Messenger.literal("§c[×] ");
                var reader = new FileReader(backupDir.resolve("Slot" + j + "_info.json").toFile());
                var result = gson.fromJson(reader, SlotInfoStorage.class);
                reader.close();
                int finalJ = j;
                backText.styled(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/qb back " + finalJ)))
                    .styled(style -> style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.of(tr("quickbackupmulti.list_backup.slot.restore", finalJ)))));
                deleteText.styled(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/qb delete " + finalJ)))
                    .styled(style -> style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.of(tr("quickbackupmulti.list_backup.slot.delete", finalJ)))));
                String desc = result.desc;
                if (Objects.equals(result.desc, "")) desc = tr("quickbackupmulti.empty_comment");
                long backupSizeB = getDirSize(backupDir.resolve("Slot" + j).toFile());
                totalBackupSizeB += backupSizeB;
                double backupSizeMB = (double) backupSizeB / FileUtils.ONE_MB;
                double backupSizeGB = (double) backupSizeB / FileUtils.ONE_GB;
                String sizeString = (backupSizeMB >= 1000) ? String.format("%.2f GB", backupSizeGB) : String.format("%.2fMB", backupSizeMB);
                resultText.append("\n" + tr("quickbackupmulti.list_backup.slot.header", finalJ) + " ")
                    .append(backText)
                    .append(deleteText)
                    .append("§2§l" + sizeString)
                    .append(tr("quickbackupmulti.list_backup.slot.info", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(result.timestamp), desc));
            } catch (IOException e) {
                resultText.append(Messenger.literal("\n"+ tr("quickbackupmulti.list_backup.slot.header", j) + " §2[▷] §c[×] §rNone"));
            }
        }
        double totalBackupSizeMB = (double) totalBackupSizeB / FileUtils.ONE_MB;
        double totalBackupSizeGB = (double) totalBackupSizeB / FileUtils.ONE_GB;
        String sizeString = (totalBackupSizeMB >= 1000) ? String.format("%.2fGB", totalBackupSizeGB) : String.format("%.2fMB", totalBackupSizeMB);
        resultText.append("\n" + tr("quickbackupmulti.list_backup.slot.total_space", sizeString));
        return resultText;
    }

    public static boolean delete(int slot) {
        if (backupDir.resolve("Slot" + slot + "_info.json").toFile().exists() || backupDir.resolve("Slot" + slot).toFile().exists()) {
            try {
                backupDir.resolve("Slot" + slot + "_info.json").toFile().delete();
                FileUtils.deleteDirectory(backupDir.resolve("Slot" + slot).toFile());
                return true;
            } catch (SecurityException | IOException e) {
                return false;
            }
        } else return false;
    }

}
