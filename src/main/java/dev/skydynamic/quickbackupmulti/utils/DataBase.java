package dev.skydynamic.quickbackupmulti.utils;

import com.mongodb.client.MongoCollection;
import de.bwaldvogel.mongo.MongoServer;
import de.bwaldvogel.mongo.backend.h2.H2Backend;

import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoClient;

import dev.morphia.Datastore;
import dev.morphia.InsertOneOptions;
import dev.morphia.DeleteOptions;
import dev.morphia.Morphia;
import dev.morphia.mapping.MapperOptions;
import dev.morphia.query.Query;
import dev.morphia.query.filters.Filters;
import dev.skydynamic.quickbackupmulti.QbmConstant;
import dev.skydynamic.quickbackupmulti.utils.config.Config;
import dev.skydynamic.quickbackupmulti.utils.storage.BackupInfo;
import dev.skydynamic.quickbackupmulti.utils.storage.IndexFile;
import dev.skydynamic.quickbackupmulti.utils.storage.codec.DimensionFormatCodec;
import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistries;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static dev.skydynamic.quickbackupmulti.QuickBackupMulti.LOGGER;
import static com.mongodb.client.model.Filters.eq;
import static dev.skydynamic.quickbackupmulti.utils.QbmManager.*;

public final class DataBase {
    private MongoServer server;
    private Datastore datastore;

    private static final InsertOneOptions INSERT_OPTIONS = new InsertOneOptions();
    private static final DeleteOptions DELETE_OPTIONS = new DeleteOptions();

    public DataBase(String worldName) {
        File qbmDir = new File(QbmConstant.gameDir + "/QuickBackupMulti/");
        if (!qbmDir.exists()) qbmDir.mkdirs();

        String connectionString = Config.INSTANCE.getMongoDBUri();
        if (Config.INSTANCE.getUseInternalDataBase()) {
            connectionString = startInternalMongoServer();
            LOGGER.info("Started local MongoDB server at " + server.getConnectionString());
        }

        MongoClient mongoClient;
        mongoClient = MongoClients.create(connectionString);

        var codecProvider = CodecRegistries.fromCodecs(
            new DimensionFormatCodec()
        );

        MapperOptions mapperOptions = MapperOptions.builder()
            .storeEmpties(true)
            .storeNulls(false)
            .codecProvider(codecProvider)
            .build();

        datastore = Morphia.createDatastore(mongoClient, "QuickBackupMulti-" + worldName.replace(" ", ""), mapperOptions);
        // database = mongoClient.getDatabase("QuickBackupMulti");
    }

    public void deleteSlot(String name) {
        for (Enums.Type type : Enums.Type.values()) {
            MongoCollection<Document> collection = datastore.getDatabase().getCollection(type.type);
            collection.findOneAndDelete(eq("name", name));
        }
    }

    public <T> void save (T obj) {
        getDatastore().save(obj, INSERT_OPTIONS);
    }

    public <T> void delete(T obj) {
        getDatastore().delete(obj, DELETE_OPTIONS);
    }

    public IndexFile getIndexFile(String name) {
        return datastore.find(IndexFile.class).filter(Filters.eq("name", name)).first();
    }

    public boolean getSlotExists(String name) {
//        MongoCollection<Document> collection = database.getCollection("BackupInfo");
//        Document document = queryAndGetDocument(collection, new Document("name", name));
        Object query = datastore.find(BackupInfo.class).filter(Filters.eq("name", name)).first();
        return query != null;
    }

    public BackupInfo getSlotInfo(String name) {
//        MongoCollection<Document> collection = database.getCollection("BackupInfo");
//        Document document = queryAndGetDocument(collection, new Document("name", name));
        return datastore.find(BackupInfo.class).filter(Filters.eq("name", name)).first();
    }

    public void reIndex(String name) throws IOException {
        Query<BackupInfo> query = datastore.find(BackupInfo.class).filter(Filters.in("indexBackup", Collections.singletonList(name)));

        List<String> reIndexBackupList = new ArrayList<>();

        String reIndexTargetName = null;
        long timestamp = 9999999999999L;
        for (BackupInfo backupInfo : query) {
            reIndexBackupList.add(backupInfo.getName());
            if (backupInfo.getTimestamp() < timestamp) {
                timestamp = backupInfo.getTimestamp();
                reIndexTargetName = backupInfo.getName();
            }
        }
        if (reIndexTargetName != null) {
            Path reIndexTarget = getBackupDir().resolve(reIndexTargetName);
            copyIndexFiles(reIndexTargetName, reIndexTarget);
            IndexFile sourceIndexFile = datastore.find(IndexFile.class).filter(Filters.eq("name", reIndexTargetName)).first();
            reIndexFile(sourceIndexFile, name, reIndexTargetName, true);
            Query<IndexFile> indexQuery = datastore.find(IndexFile.class).filter(Filters.in("name", reIndexBackupList));
            Query<BackupInfo> backupQuery = datastore.find(BackupInfo.class).filter(Filters.in("name", reIndexBackupList));
            for (BackupInfo backupInfo : backupQuery) {
                List<String> indexBackupList = backupInfo.getIndexBackup();
                indexBackupList.remove(name);
                if (!indexBackupList.contains(reIndexTargetName) && !reIndexTargetName.equals(backupInfo.getName())) indexBackupList.add(reIndexTargetName);
                backupInfo.setIndexBackup(indexBackupList);
                backupInfo.save();
            }
            for (IndexFile indexFile : indexQuery) {
                reIndexFile(indexFile, name, reIndexTargetName, false);
            }
        }

    }

    private String startInternalMongoServer() {
        server = new MongoServer(new H2Backend(QbmConstant.gameDir + "/QuickBackupMulti/qbm.mv"));
        server.bind();
        return server.getConnectionString();
    }

    public void stopInternalMongoServer() {
        if (server != null) server.shutdownNow();
        server = null;
    }

    public Datastore getDatastore() {
        return datastore;
    }
}
