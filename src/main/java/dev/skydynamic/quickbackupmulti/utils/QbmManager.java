package dev.skydynamic.quickbackupmulti.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import dev.skydynamic.quickbackupmulti.QbmConstant;
import dev.skydynamic.quickbackupmulti.utils.config.Config;

import net.fabricmc.api.EnvType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.WorldSavePath;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.NameFileFilter;
import org.apache.commons.io.filefilter.NotFileFilter;
import org.quartz.SchedulerException;

import java.io.*;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import static dev.skydynamic.quickbackupmulti.QuickBackupMulti.LOGGER;
import static dev.skydynamic.quickbackupmulti.i18n.Translate.tr;
import static dev.skydynamic.quickbackupmulti.utils.schedule.CronUtil.buildScheduler;
import static dev.skydynamic.quickbackupmulti.utils.schedule.CronUtil.getNextExecutionTime;

public class QbmManager {
    public static Path backupDir = Path.of(QbmConstant.gameDir + "/QuickBackupMulti/");
    public static Path savePath = Config.TEMP_CONFIG.server.getSavePath(WorldSavePath.ROOT);
    public static IOFileFilter fileFilter = new NotFileFilter(new NameFileFilter(Config.INSTANCE.getIgnoredFiles()));

    static Gson gson = new GsonBuilder().serializeNulls().setPrettyPrinting().create();

    public static Path getBackupDir() {
        if (Config.TEMP_CONFIG.env == EnvType.SERVER) {
            return backupDir;
        } else {
            return backupDir.resolve(Config.TEMP_CONFIG.worldName);
        }
    }

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

    private static void writeBackupInfo(String name, String desc) {
        try {
            ConcurrentHashMap<String, Object> data = new ConcurrentHashMap<>();
            data.put("desc", desc);
            data.put("timestamp", System.currentTimeMillis());
            var writer = new FileWriter(getBackupDir().resolve(name + "_info.json").toFile());
            gson.toJson(data, writer);
            writer.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    private static long getDirSize(File dir) {
        return FileUtils.sizeOf(dir);
    }

    private static boolean checkSlotExist(String name) {
        return getBackupDir().resolve(name + "_info.json").toFile().exists() && getBackupDir().resolve(name).toFile().exists();
    }

    public static void restoreClient(String slot) {
        File targetBackupSlot = getBackupDir().resolve(slot).toFile();
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

    public static void restore(String slot) {
        File targetBackupSlot = getBackupDir().resolve(slot).toFile();
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

    private static boolean getSlotExist(String name) {
        return checkSlotExist(name);
    }

    public static boolean scheduleMake(ServerCommandSource commandSource, String name) {
        if (!checkSlotExist(name)) return false;
        try {
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
            if (!getBackupDir().resolve(name).toFile().exists()) getBackupDir().resolve(name).toFile().mkdir();
            if (Objects.requireNonNull(getBackupDir().resolve(name).toFile().listFiles()).length > 0) FileUtils.deleteDirectory(getBackupDir().resolve(name).toFile());
            FileUtils.copyDirectory(savePath.toFile(), getBackupDir().resolve(name).toFile(), fileFilter);
            writeBackupInfo(name, "Scheduled Backup");
            for (ServerWorld serverWorld : server.getWorlds()) {
                if (serverWorld == null || !serverWorld.savingDisabled) continue;
                serverWorld.savingDisabled = false;
            }
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public static int make(ServerCommandSource commandSource, String name, String desc) {
        long startTime = System.currentTimeMillis();
        if (getSlotExist(name)) {
            Messenger.sendMessage(commandSource, Text.of(tr("quickbackupmulti.make.fail_exists")));
        }
        try {
            Messenger.sendMessage(commandSource, Text.of(tr("quickbackupmulti.make.start")));
            MinecraftServer server = commandSource.getServer();
            //#if MC>11800
            server.saveAll(true, true, true);
            // I dont know how create after this
            server.session.close();
            //#else
            //$$ server.save(true, true, true);
            //#endif
            for (ServerWorld serverWorld : server.getWorlds()) {
                if (serverWorld == null || serverWorld.savingDisabled) continue;
                serverWorld.savingDisabled = true;
            }
            if (!getBackupDir().resolve(name).toFile().exists()) getBackupDir().resolve(name).toFile().mkdir();
            if (Objects.requireNonNull(getBackupDir().resolve(name).toFile().listFiles()).length > 0) FileUtils.deleteDirectory(getBackupDir().resolve(name).toFile());
            FileUtils.copyDirectory(savePath.toFile(), getBackupDir().resolve(name).toFile(), fileFilter);
            long endTime = System.currentTimeMillis();
            double intervalTime = (endTime - startTime) / 1000.0;
            Messenger.sendMessage(commandSource, Text.of(tr("quickbackupmulti.make.success", intervalTime)));
            writeBackupInfo(name, desc);
            startSchedule(commandSource);
            for (ServerWorld serverWorld : server.getWorlds()) {
                if (serverWorld == null || !serverWorld.savingDisabled) continue;
                serverWorld.savingDisabled = false;
            }
        } catch (IOException e) {
            Messenger.sendMessage(commandSource, Text.of(tr("quickbackupmulti.make.fail", e.getMessage())));
            backupDir.resolve(name).toFile().deleteOnExit();
        }
        return 1;
    }

    private static int getPageCount(List<String>backupsDirList, int page) {
        int size = backupsDirList.size();
        if (!(size < 5*page)) {
            return 5;
        } else if (size < 5*page && (size < 5 && size > 0)){
            return size;
        } else {
            return Math.max(size - 5 * (page - 1), 0);
        }
    }

    public static List<String> getBackupsList(Path backupDir) {
        List<String> backupsDirList = new ArrayList<>();
        for (File file : getBackupDir().toFile().listFiles()) {
            if (file.isDirectory() && backupDir.resolve(file.getName()).toFile().exists() && backupDir.resolve(file.getName() + "_info.json").toFile().exists()) {
                backupsDirList.add(file.getName());
            }
        }
        return backupsDirList;
    }

    public static int getTotalPage(List<String> backupsList) {
        return (int) Math.ceil(backupsList.size() / 5.0);
    }

    private static MutableText getBackPageText(int page, int totalPage) {
        MutableText backPageText;
        backPageText = Messenger.literal("[<-]");
        backPageText.styled(style -> style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.of(tr("quickbackupmulti.list_backup.back_page")))));
        if (page != 1 && totalPage > 1) {
            backPageText.styled(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/qb list " + (page - 1))))
                .styled(style -> style.withColor(Formatting.AQUA));
        } else if (page == 1) {
            backPageText.styled(style -> style.withColor(Formatting.DARK_GRAY));
        }
        return backPageText;
    }

    private static MutableText getNextPageText(int page, int totalPage) {
        MutableText nextPageText;
        nextPageText = Messenger.literal("[->]");
        nextPageText.styled(style -> style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.of(tr("quickbackupmulti.list_backup.next_page")))));
        if (page != totalPage && totalPage > 1) {
            nextPageText.styled(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/qb list " + (page + 1))))
                .styled(style -> style.withColor(Formatting.AQUA));
        } else if (page == totalPage) {
            nextPageText.styled(style -> style.withColor(Formatting.DARK_GRAY));
        }
        return nextPageText;
    }

    private static MutableText getSlotText(Path backupDir,String name, int page, int num, long backupSizeB) throws IOException {
        MutableText backText = Messenger.literal("§2[▷] ");
        MutableText deleteText = Messenger.literal("§c[×] ");
        MutableText resultText = Messenger.literal("");
        var reader = new FileReader(backupDir.resolve(name + "_info.json").toFile());
        var result = gson.fromJson(reader, SlotInfoStorage.class);
        reader.close();
        backText.styled(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/qb back " + name)))
            .styled(style -> style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.of(tr("quickbackupmulti.list_backup.slot.restore", name)))));
        deleteText.styled(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/qb delete " + name)))
            .styled(style -> style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.of(tr("quickbackupmulti.list_backup.slot.delete", name)))));
        String desc = result.desc;
        if (Objects.equals(result.desc, "")) desc = tr("quickbackupmulti.empty_comment");
        double backupSizeMB = (double) backupSizeB / FileUtils.ONE_MB;
        double backupSizeGB = (double) backupSizeB / FileUtils.ONE_GB;
        String sizeString = (backupSizeMB >= 1000) ? String.format("%.2fGB", backupSizeGB) : String.format("%.2fMB", backupSizeMB);
        resultText.append("\n" + tr("quickbackupmulti.list_backup.slot.header", num + (5 * (page - 1))) + " ")
            .append("§6" + name + "§r ")
            .append(backText)
            .append(deleteText)
            .append("§a" + sizeString)
            .append(String.format(" §b%s§7: §r%s", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(result.timestamp), desc));
        return resultText;
    }

    public static MutableText list(int page) {
        long totalBackupSizeB = 0;
        Path backupDir = getBackupDir();
        List<String> backupsList = getBackupsList(backupDir);
        if (backupsList.isEmpty() || getPageCount(backupsList, page) == 0) {
            return Messenger.literal(tr("quickbackupmulti.list_empty"));
        }
        int totalPage = getTotalPage(backupsList);

        MutableText resultText = Messenger.literal(tr("quickbackupmulti.list_backup.title", page));
        MutableText backPageText = getBackPageText(page, totalPage);
        MutableText nextPageText = getNextPageText(page, totalPage);
        resultText.append("\n")
            .append(backPageText)
            .append("  ")
            .append(tr("quickbackupmulti.list_backup.page_msg", page, totalPage))
            .append("  ")
            .append(nextPageText);

        for (int j=1;j<=getPageCount(backupsList, page);j++) {
            try {
                String name = backupsList.get(((j-1)+5*(page-1)));
                long backupSizeB = getDirSize(backupDir.resolve(name).toFile());
                totalBackupSizeB += backupSizeB;
                resultText.append(getSlotText(backupDir, name, page, j, backupSizeB));
            } catch (IOException e) {
                LOGGER.error("FileNotFoundException: " + e.getMessage());
            }
        }
        double totalBackupSizeMB = (double) totalBackupSizeB / FileUtils.ONE_MB;
        double totalBackupSizeGB = (double) totalBackupSizeB / FileUtils.ONE_GB;
        String sizeString = (totalBackupSizeMB >= 1000) ? String.format("%.2fGB", totalBackupSizeGB) : String.format("%.2fMB", totalBackupSizeMB);
        resultText.append("\n" + tr("quickbackupmulti.list_backup.slot.total_space", sizeString));
        return resultText;
    }

    public static MutableText search(List<String> searchResultList) {
        MutableText resultText = Messenger.literal(tr("quickbackupmulti.search.success"));
        Path backupDir = getBackupDir();
        for (int i=1;i<=searchResultList.size();i++) {
            try {
                String name = searchResultList.get(i-1);
                long backupSizeB = getDirSize(backupDir.resolve(name).toFile());
                resultText.append(getSlotText(backupDir, name, 1, i, backupSizeB));
            } catch (IOException e) {
                LOGGER.error("FileNotFoundException: " + e.getMessage());
            }
        }
        return resultText;
    }

    public static boolean delete(String name) {
        if (getBackupDir().resolve(name + "_info.json").toFile().exists() || getBackupDir().resolve(name).toFile().exists()) {
            try {
                getBackupDir().resolve(name + "_info.json").toFile().delete();
                FileUtils.deleteDirectory(getBackupDir().resolve(name).toFile());
                return true;
            } catch (SecurityException | IOException e) {
                return false;
            }
        } else return false;
    }

    public static void createBackupDir(Path path) {
        if (!path.toFile().exists()) path.toFile().mkdirs();
    }

    public static void startSchedule(ServerCommandSource commandSource) {
        String nextBackupTimeString = "";
        try {
            switch (Config.INSTANCE.getScheduleMode()) {
                case "cron" -> nextBackupTimeString = getNextExecutionTime(Config.INSTANCE.getScheduleCron(), false);
                case "interval" -> nextBackupTimeString = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(System.currentTimeMillis() + Config.INSTANCE.getScheduleInrerval() * 1000L);
            }
            buildScheduler();
            Config.TEMP_CONFIG.scheduler.start();
            Messenger.sendMessage(commandSource, Messenger.literal(tr("quickbackupmulti.schedule.enable.success", nextBackupTimeString)));
        } catch (SchedulerException e) {
            Messenger.sendMessage(commandSource, Messenger.literal(tr("quickbackupmulti.schedule.enable.fail", e)));
        }
    }
}
