package io.github.skydynamic.quickbackupmulti.utils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.github.skydynamic.quickbackupmulti.QuickBackupMulti;


import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class UpdateChecker extends Thread {

    private static final HttpClient CLIENT = HttpClient.newHttpClient();
    private static final String RELEASE_API_URL = "https://api.github.com/repos/QuickBackupMultiMod-Dev/QuickBackupM-Fabric/releases/latest";

    public String latestVersion;
    public String latestVersionHtmUrl;
    public boolean needUpdate = false;

    public UpdateChecker() {
        super("QuickBackupM-Fabric-Update-Checker");
    }

    @Override
    public void run() {
        try {
            if (QuickBackupMulti.TEMP_CONFIG.modVersion == null) {
                QuickBackupMulti.LOGGER.warn("Current mod version is not found.");
                return;
            }
            HttpResponse<String> response = CLIENT.send(
                HttpRequest.newBuilder().uri(new URI(RELEASE_API_URL)).build(),
                HttpResponse.BodyHandlers.ofString()
            );

            // Get Meta data
            JsonObject jsonObject = JsonParser.parseString(response.body()).getAsJsonObject();
            latestVersion = jsonObject.get("tag_name").getAsString();
            latestVersionHtmUrl = jsonObject.get("html_url").getAsString();

            String tag = latestVersion
                .replaceAll("\\+.*", "")
                .replaceFirst("^v", "");
            String currentVersion = QuickBackupMulti.TEMP_CONFIG.modVersion
                .replaceAll("\\+.*", "")
                .replaceFirst("^v", "");

            if (tag.compareTo(currentVersion) > 0) {
                needUpdate = true;
                QuickBackupMulti.LOGGER.info(
                    "{} has new version {}. You can see: {}", QuickBackupMulti.modName, latestVersion, latestVersionHtmUrl
                );
            }
        } catch (IOException | InterruptedException | URISyntaxException e) {
            QuickBackupMulti.LOGGER.error("Check update failed", e);
        }
    }
}
