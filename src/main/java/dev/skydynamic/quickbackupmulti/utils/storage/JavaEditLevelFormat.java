package dev.skydynamic.quickbackupmulti.utils.storage;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class JavaEditLevelFormat extends DimensionFormat{
    private HashMap<String, String> root;
    private HashMap<String, String> playerdata;
    private HashMap<String, String> stats;
    private HashMap<String, String> advancements;
    private HashMap<String, String> datapacks;
    private DimensionFormat DIM1;
    private DimensionFormat DIM_1;

    public static List<String> saveFormatDirs = Arrays.asList(".", "playerdata", "stats", "advancements", "datapacks", "DIM1", "DIM-1");
    public static List<String> dimFormatDirs = Arrays.asList("region", "data", "entities", "poi");

    public HashMap<String, String> getDatapacks() {
        return datapacks != null ? datapacks : new HashMap<>();
    }

    public void setDatapacks(HashMap<String, String> datapacks) {
        this.datapacks = datapacks;
    }

    public HashMap<String, String> getRoot() {
        return root != null ? root : new HashMap<>();
    }

    public void setRoot(HashMap<String, String> root) {
        this.root = root;
    }

    public HashMap<String, String> getPlayerdata() {
        return playerdata != null ? playerdata : new HashMap<>();
    }

    public void setPlayerdata(HashMap<String, String> playerdata) {
        this.playerdata = playerdata;
    }

    public HashMap<String, String> getStats() {
        return stats != null ? stats : new HashMap<>();
    }

    public void setStats(HashMap<String, String> stats) {
        this.stats = stats;
    }

    public HashMap<String, String> getAdvancements() {
        return advancements != null ? advancements : new HashMap<>();
    }

    public void setAdvancements(HashMap<String, String> advancements) {
        this.advancements = advancements;
    }

    public DimensionFormat getDIM1() {
        return DIM1;
    }

    public void setDIM1(DimensionFormat DIM1) {
        this.DIM1 = DIM1;
    }

    public DimensionFormat getDIM_1() {
        return DIM_1;
    }

    public void setDIM_1(DimensionFormat DIM_1) {
        this.DIM_1 = DIM_1;
    }

    public DimensionFormat getDim(String s) {
        switch (s) {
            case "DIM1" -> {
                return getDIM1();
            }
            case "DIM-1" -> {
                return getDIM_1();
            }
            default -> {
                return null;
            }
        }
    }

    public void setDim(String s, DimensionFormat value) {
        switch (s) {
            case "DIM1" -> setDIM1(value);
            case "DIM-1" -> setDIM_1(value);
        }
    }

    @Override
    public HashMap<String, String> get(String s) {
        switch (s) {
            case "data" -> {
                return getData();
            }
            case "datapacks" -> {
                return getDatapacks();
            }
            case "poi" -> {
                return getPoi();
            }
            case "entities" -> {
                return getEntities();
            }
            case "region" -> {
                return getRegion();
            }
            case "." -> {
                return getRoot();
            }
            case "playerdata" -> {
                return getPlayerdata();
            }
            case "stats" -> {
                return getStats();
            }
            case "advancements" -> {
                return getAdvancements();
            }
            default -> {
                return null;
            }
        }
    }

    @Override
    public void set(String s, HashMap<String, String> value) {
        switch (s) {
            case "data" -> setData(value);
            case "poi" -> setPoi(value);
            case "entities" -> setEntities(value);
            case "region" -> setRegion(value);
            case "root" -> setRoot(value);
            case "playerdata" -> setPlayerdata(value);
            case "stats" -> setStats(value);
            case "advancements" -> setAdvancements(value);
            case "datapacks" -> setDatapacks(value);
        }
    }
}
