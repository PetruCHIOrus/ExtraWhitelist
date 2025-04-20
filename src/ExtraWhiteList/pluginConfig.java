package ExtraWhiteList;

import arc.files.Fi;
import arc.util.Log;
import arc.util.serialization.Json;
import mindustry.Vars;

public class pluginConfig {
    public int listMode = 0;
    private static final String configFileName = "extrawhitelist.json";
    private static final Fi configDir = Vars.dataDirectory;
    private static final Fi configFile = configDir.child(configFileName);
    public String PlayerlistPath = "config/playerlist.txt";

    public void load() {
        if(configFile.exists()){
            try {
                Json json = new Json();
                pluginConfig loaded = json.fromJson(pluginConfig.class, configFile.readString());
                this.listMode = loaded.listMode;
                this.PlayerlistPath = loaded.PlayerlistPath;
            } catch (Exception e){
                Log.err("Can't load extrawhitelist.json: " + e);
            }
        } else {
            save();
        }
    }
    public void save() {
        try {
            Json json   = new Json();
            configFile.writeString(json.prettyPrint(this));
        } catch (Exception e) {
            Log.err("Can't save extrawhitelist.json: " + e);
        }
    }
}
