package com.envyful.battle.enforcement;

import com.envyful.api.concurrency.UtilLogger;
import com.envyful.api.config.yaml.YamlConfigFactory;
import com.envyful.api.forge.command.ForgeCommandFactory;
import com.envyful.api.forge.gui.factory.ForgeGuiFactory;
import com.envyful.api.forge.platform.ForgePlatformHandler;
import com.envyful.api.forge.player.ForgePlayerManager;
import com.envyful.api.forge.player.util.UtilPlayer;
import com.envyful.api.gui.factory.GuiFactory;
import com.envyful.api.platform.PlatformProxy;
import com.envyful.battle.enforcement.command.BattleSpectateCommand;
import com.envyful.battle.enforcement.command.BattleStartCommand;
import com.envyful.battle.enforcement.command.BattleTypeTabCompleter;
import com.envyful.battle.enforcement.config.BattleEnforcementConfig;
import com.envyful.battle.enforcement.config.BattleType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.List;

@Mod(BattleEnforcement.MOD_ID)
public class BattleEnforcement {

    public static final String MOD_ID = "battleenforcement";

    private static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    private static BattleEnforcement instance;

    private ForgePlayerManager playerManager = new ForgePlayerManager();
    private ForgeCommandFactory commandFactory = new ForgeCommandFactory(playerManager);

    private BattleEnforcementConfig config;

    public BattleEnforcement() {
        instance = this;

        UtilLogger.setLogger(LOGGER);
        PlatformProxy.setPlayerManager(this.playerManager);
        PlatformProxy.setHandler(ForgePlatformHandler.getInstance());
        GuiFactory.setPlatformFactory(new ForgeGuiFactory());

        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onServerStarting(ServerAboutToStartEvent event) {
        LOGGER.info("Loading config...");
        this.reloadConfig();
    }

    public void reloadConfig() {
        try {
            this.config = YamlConfigFactory.getInstance(BattleEnforcementConfig.class);
        } catch (IOException e) {
            getLogger().error("Failed to load config", e);
        }
    }

    @SubscribeEvent
    public void registerCommands(RegisterCommandsEvent event) {
        this.commandFactory.registerCompleter(new BattleTypeTabCompleter());
        this.commandFactory.registerInjector(BattleType.class, (sender, args) -> {
            var aura = this.config.typeFromId(args[0]);

            if (aura == null) {
                PlatformProxy.sendMessage(sender, List.of("&c&l(!) &cNo type found with ID!"));
            }

            return aura;
        });

        this.commandFactory.registerCommand(event.getDispatcher(), this.commandFactory.parseCommand(new BattleStartCommand()));
        this.commandFactory.registerCommand(event.getDispatcher(), this.commandFactory.parseCommand(new BattleSpectateCommand()));
    }

    public static BattleEnforcement getInstance() {
        return instance;
    }

    public static Logger getLogger() {
        return LOGGER;
    }

    public static BattleEnforcementConfig getConfig() {
        return instance.config;
    }
}