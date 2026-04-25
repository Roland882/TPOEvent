package me.roland882.tpoevent;

import me.roland882.tpoevent.commands.MinionCommand;
import me.roland882.tpoevent.managers.MinionManager;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.Objects;

public final class TPOEvent extends JavaPlugin {
    public MinionManager mmm;
    public MiniMessage mm = MiniMessage.miniMessage();
    private MinionCommand minionCommand;

    @Override
    public void onEnable() {
        getServer().getConsoleSender().sendMessage(mm.deserialize("<green>TPOEvent is initializing..."));
        getServer().getScheduler().runTaskLater(this, () -> {
            try {
                this.mmm = new MinionManager(this);
                getServer().getConsoleSender().sendMessage(mm.deserialize("MinionManager initialized successfully."));
                this.minionCommand = new MinionCommand(this, mmm);
                Objects.requireNonNull(getCommand("mmm")).setExecutor(minionCommand);
                getServer().getConsoleSender().sendMessage(mm.deserialize(""));
                getServer().getConsoleSender().sendMessage(mm.deserialize("<red> _____ ____   ___  _____                 _   "));
                getServer().getConsoleSender().sendMessage(mm.deserialize("<red>|_   _|  _ \\ / _ \\| ____|_   _____ _ __ | |_ "));
                getServer().getConsoleSender().sendMessage(mm.deserialize("<red>  | | | |_) | | | |  _| \\ \\ / / _ \\ '_ \\| __|"));
                getServer().getConsoleSender().sendMessage(mm.deserialize("<red>  | | |  __/| |_| | |___ \\ V /  __/ | | | |_ "));
                getServer().getConsoleSender().sendMessage(mm.deserialize("<red> |_| |_|    \\___/|_____| \\_/ \\___|_| |_|\\__|"));
                getServer().getConsoleSender().sendMessage(mm.deserialize(""));
                getServer().getConsoleSender().sendMessage(mm.deserialize("<white>Plugin made by: <aqua>Roland882 <gray>| <white>Version: <aqua>2025.12"));
            } catch (Exception ex) {
                getServer().getConsoleSender().sendMessage(mm.deserialize("<red>Failed to initialize TPOEvent: " + ex.getMessage()));
                getServer().getPluginManager().disablePlugin(this);
            }
        }, 1L);
    }

    @Override
    public void onDisable() {
        mmm.desAllMin();
    }
}