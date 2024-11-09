package io.github.skydynamic.quickbackupmulti.utils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.github.skydynamic.quickbackupmulti.QuickBackupMulti;
import io.github.skydynamic.quickbackupmulti.config.Config;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

public class UpdateChecker extends Thread {

    private static final HttpClient CLIENT = HttpClients.createDefault();
    private static final String RELEASE_API_URL = "https://api.github.com/repos/QuickBackupMultiMod-Dev/QuickBackupM-Fabric/releases/latest";

    public String latestVersion;

    public UpdateChecker() {
        super("QuickBackupM-Fabric-Update-Checker");
    }

    @Override
    public void run() {
        try {
            if (Config.TEMP_CONFIG.modVersion == null) {
                QuickBackupMulti.LOGGER.warn("Current mod version is not found.");
                return;
            }
            HttpResponse httpResponse = CLIENT.execute(new HttpGet(RELEASE_API_URL));
            String body = EntityUtils.toString(httpResponse.getEntity());

            JsonObject jsonObject = JsonParser.parseString(body).getAsJsonObject();
            latestVersion = jsonObject.get("tag_name").getAsString();
            String tag = latestVersion
                .replaceAll("\\+.*", "")
                .replaceFirst("^v", "");
            String currentVersion = Config.TEMP_CONFIG.modVersion
                .replaceAll("\\+.*", "")
                .replaceFirst("^v", "");

            if (tag.compareTo(currentVersion) > 0) {
                QuickBackupMulti.LOGGER.info("{} has new version {}", QuickBackupMulti.modName, latestVersion);
            }

        } catch (IOException e) {
            QuickBackupMulti.LOGGER.error(e.getMessage(), e);
        }
    }
}
