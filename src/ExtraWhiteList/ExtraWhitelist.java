package ExtraWhiteList;

import arc.Events;
import arc.util.CommandHandler;
import arc.util.Log;
import mindustry.game.EventType;
import mindustry.game.Team;
import mindustry.gen.Groups;
import mindustry.gen.Player;
import mindustry.mod.Plugin;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class ExtraWhitelist extends Plugin{
    pluginConfig config = new pluginConfig();
    @Override
    public void init(){
        config.load();
        Events.on(EventType.PlayerJoin.class, event -> {
            List<String> Members;
            Player newPlayer = event.player;
            Path path = Paths.get(config.PlayerlistPath);
            try {
                if(!Files.exists(path)){
                    Files.createFile(path);
                }
                Members = Files.readAllLines(path);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            switch (config.listMode){
                case 0 -> {
                }
                case 1 -> {
                    if(!Members.contains(newPlayer.uuid())){
                        newPlayer.team(Team.derelict);
                    }
                }
                case 2 -> {
                    if(Members.contains(newPlayer.uuid())){
                        newPlayer.team(Team.derelict);
                    }
                }
                default -> Log.err("Can't determine action with new player. List ignored.");
            }
        });
    }
    private int getTeamIndexByName(String teamName) {
        return switch (teamName) {
            case "blue" -> Team.blue.id;
            case "crux" -> Team.crux.id;
            case "green" -> Team.green.id;
            case "malis" -> Team.malis.id;
            case "neoplastic" -> Team.neoplastic.id;
            case "sharded" -> Team.sharded.id;
            default -> -1;
        };
    }
    @Override
    public void registerServerCommands(CommandHandler handler){
        handler.register("team", "<uuid> <team>","Changes team for player.", args -> {
            if(args.length < 2){
                Log.info("Invalid syntax. Usage: /team UUID team");
                return;
            }
            String uuid = args[0];
            String teamInput = args[1];
            Player target = null;
            for(Player p : Groups.player){
                if(p.uuid().equals(uuid)){
                    target = p;
                    break;
                }
            }
            if(target == null){
                Log.info("Player with UUID "+uuid+" not found or offline for now.");
                return;
            }
            int teamIndex = getTeamIndexByName(teamInput);
            if(teamIndex == -1){
                Log.info("Team '" + teamInput + "' not found or inactive.");
            }
            Team newTeam = Team.get(getTeamIndexByName(teamInput));

            String oldTeam = target.team().name;
            target.team(newTeam);
            Log.info("Team "+ teamInput+ " successfully assigned to player UUID "+ uuid + " from "+ oldTeam + " team.");
        });
        handler.register("ewlconf", "[option] [value]", "Manage and configure ExtraWhitelist mode", args -> {
            if(args.length == 0) {
                System.out.println("=== ACTIVE MOD CONFIGURATION ===");
                System.out.println("listmode:       " + config.listMode);
                System.out.println("playerlistpath: " + config.PlayerlistPath);
                System.out.println("================================");
                System.out.println();
                System.out.println("Extra features >>");
                System.out.println();
                System.out.println("save - forcibly saves config;");
                System.out.println("load - reload config file from config/extrawhitelist.json;");
                return;
            }
            switch (args[0]){
                case "listmode" -> {
                    if(args.length == 1){
                        Log.info("Actual listmode: "+ config.listMode);
                        return;
                    }
                    try {
                        if(Integer.parseInt(args[1])> 2){
                            Log.info("This mode doesn't exists. Use 0, 1, or 2 mode.");
                            return;
                        }
                        config.listMode = Integer.parseInt(args[1]);
                        config.save();
                        Log.info("Listmode in mode "+ args[1] + " now, setting successfully saved.");
                    } catch (NumberFormatException e) {
                        Log.err("Mode is not changed: invalid syntax. " + e);
                    }
                }

                case "playerlistpath" -> {
                    if(args.length == 1){
                        Log.info("Actual path: "+ config.PlayerlistPath);
                        return;
                    }

                    try {
                        config.PlayerlistPath = args[1];
                        config.save();
                        Log.info("Playerlist path is "+ args[1] + " now, setting successfully saved.");
                    } catch (NumberFormatException e) {
                        Log.err("Playerlist path is not changed: invalid syntax. " + e);
                    }
                }
                case "reload" -> {
                    config.load();
                    Log.info("Config reloaded. Write 'ewlconf' if you wanna check it.");
                }
                case "save" -> {
                    config.save();
                    Log.info("Сonfig was saved forcibly.");
                }
                default -> {
                    Log.info("Can't found that command. Nothing happen.");
                }
            }
        });
    }
    @Override
    public void registerClientCommands(CommandHandler handler){
        handler.<Player>register("team","Shows your team.", (args, player) -> player.sendMessage("[pink]Your team - []"+ player.team().name));
    }
}
