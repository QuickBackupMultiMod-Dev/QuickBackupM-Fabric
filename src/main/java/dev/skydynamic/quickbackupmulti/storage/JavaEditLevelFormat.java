package dev.skydynamic.quickbackupmulti.storage;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class JavaEditLevelFormat extends DimensionFormat {
    private HashMap<String, String> root;
    private HashMap<String, String> playerdata;
    private HashMap<String, String> stats;
    private HashMap<String, String> advancements;
    private DimensionFormat DIM1;
    private DimensionFormat DIM_1;

    public static List<String> saveFormatDirs = Arrays.asList(".", "playerdata", "stats", "advancements", "DIM1", "DIM-1");
    public static List<String> dimFormatDirs = Arrays.asList("region", "data", "entities", "poi");

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

    @Nullable
    public DimensionFormat getDim(String s) {
        switch (s) {
            case "DIM1" : {
                return getDIM1();
            }
            case "DIM-1" : {
                return getDIM_1();
            }
            default : {
                return null;
            }
        }
    }

    public void setDim(String s, DimensionFormat value) {
        switch (s) {
            case "DIM1" : {
                setDIM1(value);
                break;
            }
            case "DIM-1" : {
                setDIM_1(value);
                break;
            }
        }
    }

    @Override
    public HashMap<String, String> get(String s) {
        switch (s) {
            case "data" : {
                return getData();
            }
            case "poi" : {
                return getPoi();
            }
            case "entities" : {
                return getEntities();
            }
            case "region" : {
                return getRegion();
            }
            case "." : {
                return getRoot();
            }
            case "playerdata" : {
                return getPlayerdata();
            }
            case "stats" : {
                return getStats();
            }
            case "advancements" : {
                return getAdvancements();
            }
            default : {
                return new HashMap<>();
            }
        }
    }

    @Override
    public void set(String s, HashMap<String, String> value) {
        switch (s) {
            case "data" : {
                setData(value);
                break;
            }
            case "poi" : {
                setPoi(value);
                break;
            }
            case "entities" : {
                setEntities(value);
                break;
            }
            case "region" : {
                setRegion(value);
                break;
            }
            case "root" : {
                setRoot(value);
                break;
            }
            case "playerdata" : {
                setPlayerdata(value);
                break;
            }
            case "stats" : {
                setStats(value);
                break;
            }
            case "advancements" : {
                setAdvancements(value);
                break;
            }
        }
    }
}
