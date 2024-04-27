package dev.skydynamic.quickbackupmulti.utils.storage;

import java.util.HashMap;

public class DimensionFormat {
    private HashMap<String, String> poi;
    private HashMap<String, String> entities;
    private HashMap<String, String> region;
    private HashMap<String, String> data;
    public void setPoi(HashMap<String, String> poi) {
        this.poi = poi;
    }
    public void setEntities(HashMap<String, String> entities) {
        this.entities = entities;
    }
    public void setRegion(HashMap<String, String> region) {
        this.region = region;
    }
    public void setData(HashMap<String, String> data) {
        this.data = data;
    }
    public HashMap<String, String> getPoi() {
        return poi != null ? poi : new HashMap<>();
    }
    public HashMap<String, String> getEntities() {
        return entities != null ? entities : new HashMap<>();
    }
    public HashMap<String, String> getRegion() {
        return region != null ? region : new HashMap<>();
    }
    public HashMap<String, String> getData() {
        return data != null ? data : new HashMap<>();
    }

    public DimensionFormat() {}

    public <T extends HashMap<String, String>> DimensionFormat(T poi, T entities, T region, T data) {
        this.poi = poi;
        this.entities = entities;
        this.region = region;
        this.data = data;
    }

    public HashMap<String, String> get(String s) {
        switch (s) {
            case "data" -> {
                return getData();
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
            default -> {
                return null;
            }
        }
    }

    public void set(String s, HashMap<String, String> value) {
        switch (s) {
            case "data" -> setData(value);
            case "poi" -> setPoi(value);
            case "entities" -> setEntities(value);
            case "region" -> setRegion(value);
        }
    }
}
