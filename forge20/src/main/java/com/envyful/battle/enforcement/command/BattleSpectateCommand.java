package com.envyful.battle.enforcement.command;

import com.envyful.api.command.annotate.Command;
import com.envyful.api.command.annotate.SubCommands;
import com.envyful.api.command.annotate.executor.Argument;
import com.envyful.api.command.annotate.executor.CommandProcessor;
import com.envyful.api.command.annotate.executor.Completable;
import com.envyful.api.command.annotate.executor.Sender;
import com.envyful.api.command.annotate.permission.Permissible;
import com.envyful.api.forge.command.completion.player.PlayerTabCompleter;
import com.envyful.api.forge.player.ForgeEnvyPlayer;
import com.envyful.api.platform.PlatformProxy;
import com.envyful.api.text.Placeholder;
import com.pixelmonmod.pixelmon.api.battles.BattleResults;
import com.pixelmonmod.pixelmon.api.battles.BattleType;
import com.pixelmonmod.pixelmon.api.events.battles.BattleEndEvent;
import com.pixelmonmod.pixelmon.api.storage.PlayerPartyStorage;
import com.pixelmonmod.pixelmon.api.storage.StorageProxy;
import com.pixelmonmod.pixelmon.api.util.helpers.NetworkHelper;
import com.pixelmonmod.pixelmon.battles.BattleRegistry;
import com.pixelmonmod.pixelmon.battles.api.BattleBuilder;
import com.pixelmonmod.pixelmon.battles.api.rules.BattleRuleRegistry;
import com.pixelmonmod.pixelmon.battles.api.rules.teamselection.TeamSelectionRegistry;
import com.pixelmonmod.pixelmon.battles.controller.participants.PixelmonWrapper;
import com.pixelmonmod.pixelmon.battles.controller.participants.PlayerParticipant;
import com.pixelmonmod.pixelmon.battles.controller.participants.Spectator;
import com.pixelmonmod.pixelmon.client.gui.battles.PixelmonClientData;
import com.pixelmonmod.pixelmon.comm.packetHandlers.battles.*;
import com.pixelmonmod.pixelmon.command.impl.SpectateCommand;
import net.minecraft.commands.CommandSource;

import java.util.Arrays;
import java.util.List;

@Command(
        value = {
                "spectatebattle",
                "battlespectate"
        }
)
@Permissible("battle.enforce.spectate")
@SubCommands(ReloadCommand.class)
public class BattleSpectateCommand {

    @CommandProcessor
    public void onCommand(@Sender ForgeEnvyPlayer source,
                          @Completable(PlayerTabCompleter.class) @Argument ForgeEnvyPlayer target) {
        var battle = BattleRegistry.getBattle(target.getParent());

        if (battle == null) {
            PlatformProxy.sendMessage(source, List.of("&c&l(!) &cThat player is not currently in a battle"));
            return;
        }

        var watchedPlayer = battle.getPlayer(target.getParent());

        NetworkHelper.sendPacket(new StartBattlePacket(battle.battleIndex, battle.getBattleType(watchedPlayer), battle.rules), source.getParent());
        NetworkHelper.sendPacket(new SetAllBattlingPokemonPacket(PixelmonClientData.convertToGUI(Arrays.asList(watchedPlayer.allPokemon)), true), source.getParent());
        List<PixelmonWrapper> teamList = watchedPlayer.getTeamPokemonList();
        NetworkHelper.sendPacket(new SetBattlingPokemonPacket(teamList), source.getParent());
        NetworkHelper.sendPacket(new SetPokemonBattleDataPacket(PixelmonClientData.convertToGUI(teamList), false), source.getParent());
        NetworkHelper.sendPacket(new SetPokemonBattleDataPacket(watchedPlayer.getOpponentData(), true), source.getParent());
        if (battle.getTeam(watchedPlayer).size() > 1) {
            NetworkHelper.sendPacket(new SetPokemonTeamDataPacket(watchedPlayer.getAllyData()), source.getParent());
        }

        NetworkHelper.sendPacket(new StartSpectatePacket(target.getUniqueId(), battle.rules.getOrDefault(BattleRuleRegistry.BATTLE_TYPE)), source.getParent());

        battle.addSpectator(new Spectator(source.getParent(), target.getName()));
        PlatformProxy.sendMessage(source, List.of("&a&l(!) &aYou are now spectating " + target.getName()));
    }
}
