package com.envyful.battle.enforcement;

import com.envyful.api.concurrency.UtilLogger;
import com.envyful.api.config.yaml.YamlConfigFactory;
import com.envyful.api.forge.command.ForgeCommandFactory;
import com.envyful.api.forge.gui.factory.ForgeGuiFactory;
import com.envyful.api.forge.platform.ForgePlatformHandler;
import com.envyful.api.forge.player.ForgePlayerManager;
import com.envyful.api.gui.factory.GuiFactory;
import com.envyful.api.platform.PlatformProxy;
import com.envyful.battle.enforcement.config.BattleEnforcementConfig;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
    public void onServerStarting(FMLServerStartingEvent event) {
        this.reloadConfig();
    }

    public void reloadConfig() {
        try {
            this.config = YamlConfigFactory.getInstance(BattleEnforcementConfig.class);
        } catch (Exception e) {
            getLogger().error("Failed to load config", e);
        }
    }

    public static Logger getLogger() {
        return LOGGER;
    }
}