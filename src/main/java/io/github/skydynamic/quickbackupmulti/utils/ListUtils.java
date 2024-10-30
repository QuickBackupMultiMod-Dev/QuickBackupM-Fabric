package io.github.skydynamic.quickbackupmulti.utils;

import io.github.skydynamic.increment.storage.lib.database.index.type.StorageInfo;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static io.github.skydynamic.quickbackupmulti.QuickBackupMulti.LOGGER;
import static io.github.skydynamic.quickbackupmulti.QuickBackupMulti.getDataBase;
import static io.github.skydynamic.quickbackupmulti.QuickBackupMulti.getStorager;
import static io.github.skydynamic.quickbackupmulti.i18n.Translate.tr;
import static io.github.skydynamic.quickbackupmulti.utils.QbmManager.getBackupDir;
import static io.github.skydynamic.quickbackupmulti.utils.QbmManager.getBackupsList;

public class ListUtils {
    private static long getDirSize(File dir) {
        return FileUtils.sizeOf(dir);
    }

    public static String truncateString(String str, int maxLength) {
        if (str.length() > maxLength) {
            return str.substring(0, maxLength - 3) + "...";
        } else {
            return str;
        }
    }

    private static int getPageCount(List<?> backupsDirList, int page) {
        int size = backupsDirList.size();
        if (!(size < 5 * page)) {
            return 5;
        } else if (size < 5 * page && (size < 5 && size > 0)) {
            return size;
        } else {
            return Math.max(size - 5 * (page - 1), 0);
        }
    }

    public static int getTotalPage(List<?> backupsList) {
        return (int) Math.ceil(backupsList.size() / 5.0);
    }

    private static MutableText getBackPageText(int page, int totalPage) {
        MutableText backPageText;
        backPageText = Messenger.literal("[<-]");
        backPageText.styled(style ->
            style.withHoverEvent(
                new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.of(tr("quickbackupmulti.list_backup.back_page")))
            )
        );
        if (page != 1 && totalPage > 1) {
            backPageText.styled(style ->
                style.withClickEvent(
                    new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/qb list " + (page - 1))
                )
            ).styled(style -> style.withColor(Formatting.AQUA));
        } else if (page == 1) {
            backPageText.styled(style -> style.withColor(Formatting.DARK_GRAY));
        }
        return backPageText;
    }

    private static MutableText getNextPageText(int page, int totalPage) {
        MutableText nextPageText;
        nextPageText = Messenger.literal("[->]");
        nextPageText.styled(style ->
            style.withHoverEvent(
                new HoverEvent(
                    HoverEvent.Action.SHOW_TEXT,
                    Text.of(tr("quickbackupmulti.list_backup.next_page")))
            )
        );
        if (page != totalPage && totalPage > 1) {
            nextPageText.styled(style ->
                style.withClickEvent(
                    new ClickEvent(
                        ClickEvent.Action.RUN_COMMAND,
                        "/qb list " + (page + 1))
                )
            ).styled(style -> style.withColor(Formatting.AQUA));
        } else if (page == totalPage) {
            nextPageText.styled(style -> style.withColor(Formatting.DARK_GRAY));
        }
        return nextPageText;
    }

    // private static MutableText getSlotText(String name, int page, int num, long backupSizeB) throws IOException {
    private static MutableText getSlotText(Map.Entry<String, StorageInfo> entry, int page, int num, long backupSizeB) throws IOException {
        String name = entry.getKey();
        MutableText backText = Messenger.literal("§2[▷] ");
        MutableText deleteText = Messenger.literal("§c[×] ");
        MutableText nameText = Messenger.literal("§6" + truncateString(name, 8) + "§r ");
        MutableText resultText = Messenger.literal("");
        // StorageInfo result = getDataBase().getStorageInfo(name);
        StorageInfo result = entry.getValue();

        backText.styled(style ->
            style.withClickEvent(
                new ClickEvent(
                    ClickEvent.Action.SUGGEST_COMMAND,
                    "/qb back \"%s\"".formatted(name))
            )
        ).styled(style ->
            style.withHoverEvent(
                new HoverEvent(
                    HoverEvent.Action.SHOW_TEXT,
                    Text.of(tr("quickbackupmulti.list_backup.slot.restore", name)))
            )
        );

        deleteText.styled(style ->
            style.withClickEvent(
                new ClickEvent(
                    ClickEvent.Action.SUGGEST_COMMAND,
                    "/qb delete \"%s\"".formatted(name))
            )
        ).styled(style ->
            style.withHoverEvent(
                new HoverEvent(
                    HoverEvent.Action.SHOW_TEXT,
                    Text.of(tr("quickbackupmulti.list_backup.slot.delete", name)))
            )
        );

        nameText.styled(style ->
            style.withClickEvent(
                new ClickEvent(
                    ClickEvent.Action.SUGGEST_COMMAND,
                    "/qb show \"%s\"".formatted(name))
            )
        ).styled(style ->
            style.withHoverEvent(
                new HoverEvent(
                    HoverEvent.Action.SHOW_TEXT,
                    Text.of(tr("quickbackupmulti.list_backup.slot.show", name)))
            )
        );

        String desc = result.getDesc();
        if (desc.isEmpty()) desc = tr("quickbackupmulti.empty_comment");
        double backupSizeMB = (double) backupSizeB / FileUtils.ONE_MB;
        double backupSizeGB = (double) backupSizeB / FileUtils.ONE_GB;
        String sizeString = (backupSizeMB >= 1000) ? String.format("%.2fGB", backupSizeGB) : String.format("%.2fMB", backupSizeMB);
        resultText.append("\n" + tr("quickbackupmulti.list_backup.slot.header", num + (5 * (page - 1))) + " ")
            .append(nameText)
            .append(backText)
            .append(deleteText)
            .append("§a" + sizeString)
            .append(
                String.format(
                    " §b%s§7: §r%s",
                    new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(result.getTimestamp()),
                    truncateString(desc, 10)
                )
            );
        return resultText;
    }

    public static MutableText list(int page) {
        long totalBackupSizeB = 0;
        Path backupDir = getBackupDir();
        // List<String> backupsList = getBackupsList();
        List<Map.Entry<String, StorageInfo>> backupsInfoList = new ArrayList<>(getBackupsList().stream()
            .map(name -> Map.entry(name, getDataBase().getStorageInfo(name)))
            .toList());
        backupsInfoList.sort((c1, c2) -> -Long.compare(c1.getValue().getTimestamp(), c2.getValue().getTimestamp()));
        // if (backupsList.isEmpty() || getPageCount(backupsList, page) == 0) {
        if (backupsInfoList.isEmpty() || getPageCount(backupsInfoList, page) == 0) {
            return Messenger.literal(tr("quickbackupmulti.list_empty"));
        }
        // int totalPage = getTotalPage(backupsList);
        int totalPage = getTotalPage(backupsInfoList);

        MutableText resultText = Messenger.literal(tr("quickbackupmulti.list_backup.title", page));
        MutableText backPageText = getBackPageText(page, totalPage);
        MutableText nextPageText = getNextPageText(page, totalPage);
        resultText.append("\n")
            .append(backPageText)
            .append("  ")
            .append(tr("quickbackupmulti.list_backup.page_msg", page, totalPage))
            .append("  ")
            .append(nextPageText);
        // for (int j = 1; j <= getPageCount(backupsList, page); j++) {
        for (int j = 1; j <= getPageCount(backupsInfoList, page); j++) {
            try {
                // String name = backupsList.get(((j - 1) + 5 * (page - 1)));
                Map.Entry<String, StorageInfo> entry = backupsInfoList.get(((j - 1) + 5 * (page - 1)));
                String name = entry.getKey();
                long backupSizeB = getDirSize(backupDir.resolve(name).toFile());
                totalBackupSizeB += backupSizeB;
                // resultText.append(getSlotText(name, page, j, backupSizeB));
                resultText.append(getSlotText(entry, page, j, backupSizeB));
            } catch (IOException e) {
                LOGGER.error("", e);
            }
        }
        double totalBackupSizeMB = (double) totalBackupSizeB / FileUtils.ONE_MB;
        double totalBackupSizeGB = (double) totalBackupSizeB / FileUtils.ONE_GB;
        String sizeString =
            (totalBackupSizeMB >= 1000)
                ? String.format("%.2fGB", totalBackupSizeGB)
                : String.format("%.2fMB", totalBackupSizeMB);
        resultText.append("\n" + tr("quickbackupmulti.list_backup.slot.total_space", sizeString));
        return resultText;
    }

    public static MutableText search(List<String> searchResultList) {
        MutableText resultText = Messenger.literal(tr("quickbackupmulti.search.success"));
        Path backupDir = getBackupDir();
        for (int i = 1; i <= searchResultList.size(); i++) {
            try {
                String name = searchResultList.get(i - 1);
                StorageInfo result = getDataBase().getStorageInfo(name);
                long backupSizeB = getDirSize(backupDir.resolve(name).toFile());
                resultText.append(getSlotText(Map.entry(name, result), 1, i, backupSizeB));
            } catch (IOException e) {
                LOGGER.error("", e);
            }
        }
        return resultText;
    }

    public static MutableText show(String name) {
        MutableText resultText;
        if (getStorager().storageExists(name)) {
            StorageInfo backupInfo = getDataBase().getStorageInfo(name);
            resultText = Messenger.literal(tr("quickbackupmulti.show.header"));
            String desc = backupInfo.getDesc();
            if (desc.isEmpty()) desc = tr("quickbackupmulti.empty_comment");

            MutableText backText = Messenger.literal(tr("quickbackupmulti.show.back_button"));
            MutableText deleteText = Messenger.literal(tr("quickbackupmulti.show.delete_button"));
            backText.styled(style ->
                style.withClickEvent(
                    new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/qb back \"%s\"".formatted(name))
                )
            ).styled(style ->
                style.withHoverEvent(
                    new HoverEvent(
                        HoverEvent.Action.SHOW_TEXT,
                        Text.of(tr("quickbackupmulti.list_backup.slot.restore", name)))
                )
            );
            deleteText.styled(style ->
                style.withClickEvent(
                    new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/qb delete \"%s\"".formatted(name)))
            ).styled(style ->
                style.withHoverEvent(
                    new HoverEvent(
                        HoverEvent.Action.SHOW_TEXT,
                        Text.of(tr("quickbackupmulti.list_backup.slot.delete", name)))
                )
            );

            resultText.append("\n")
                .append(tr("quickbackupmulti.show.name") + ": §r" + backupInfo.getName() + "\n")
                .append(tr("quickbackupmulti.show.desc") + ": §r" + desc + "\n")
                .append(
                    tr("quickbackupmulti.show.time")
                        + ": §r"
                        + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(backupInfo.getTimestamp())
                )
                .append("\n")
                .append(backText)
                .append(" ")
                .append(deleteText);

        } else {
            resultText = Messenger.literal(tr("quickbackupmulti.show.fail"));
            resultText.styled(style -> style.withColor(Formatting.RED));
        }
        return resultText;
    }
}
