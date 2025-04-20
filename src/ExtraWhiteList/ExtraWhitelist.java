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
            Path path = Paths.get(config.PlayerlistDir);
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
        handler.register("listmode", "[0,1,2]", "Set list mode: 1 - white, 2 - black, 0 - off.", args -> {
            if(args.length == 0){
                Log.info("Current mode: " + config.listMode);
                return;
            }
            try {
                if(Integer.parseInt(args[0]) > 2){
                    Log.info("List mode is not changed: invalid mode. Valid modes: 0, 1, 2. Check Wiki for more details.");
                }
                config.listMode = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                Log.err("Mode is not changed: invalid syntax. " + e);
            }
        });
    }
    @Override
    public void registerClientCommands(CommandHandler handler){
        handler.<Player>register("team","Shows your team.", (args, player) -> player.sendMessage("[pink]Your team - []"+ player.team().name));
    }
}
