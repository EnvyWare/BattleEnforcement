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
import com.envyful.battle.enforcement.config.BattleType;
import com.pixelmonmod.pixelmon.api.storage.PlayerPartyStorage;
import com.pixelmonmod.pixelmon.api.storage.StorageProxy;
import com.pixelmonmod.pixelmon.battles.api.BattleBuilder;
import com.pixelmonmod.pixelmon.battles.controller.participants.PlayerParticipant;
import net.minecraft.command.ICommandSource;

import java.util.List;

@Command(
        value = {
                "battlebegin",
                "startenforcedbattle"
        }
)
@Permissible("battle.enforce.start")
@SubCommands(ReloadCommand.class)
public class BattleStartCommand {

    @CommandProcessor
    public void onCommand(@Sender ICommandSource source,
                          @Completable(BattleTypeTabCompleter.class) @Argument BattleType type,
                          @Completable (PlayerTabCompleter.class) @Argument ForgeEnvyPlayer targetOne,
                          @Completable (PlayerTabCompleter.class) @Argument ForgeEnvyPlayer targetTwo) {
        var partyOne = StorageProxy.getPartyNow(targetOne.getParent());
        var partyTwo = StorageProxy.getPartyNow(targetTwo.getParent());

        if (partyOne == null || partyTwo == null) {
            PlatformProxy.sendMessage(source, List.of("Could not find one of the player's parties"));
            return;
        }

        if (partyOne.countAblePokemon() < type.requiredPokemon() || partyTwo.countAblePokemon() < type.requiredPokemon()) {
            PlatformProxy.sendMessage(source, List.of("One of the players does not have enough battle-able Pokemon"));
            return;
        }

        if (!this.isPartyValid(type, partyOne)) {
            PlatformProxy.sendMessage(source, List.of("Player One's party contains blacklisted Pokemon"));
            return;
        }

        if (!this.isPartyValid(type, partyTwo)) {
            PlatformProxy.sendMessage(source, List.of("Player Two's party contains blacklisted Pokemon"));
            return;
        }

        BattleBuilder.builder()
                .rules(type.createRules())
                .teamSelection(false)
                .teamOne(new PlayerParticipant(targetOne.getParent(), partyOne.getAll()))
                .teamTwo(new PlayerParticipant(targetTwo.getParent(), partyTwo.getAll()))
                .disableExp()
                .allowSpectators()
                .start();
    }

    private boolean isPartyValid(BattleType type, PlayerPartyStorage party) {
        for (var pokemon : party.getAll()) {
            if (type.isBlacklisted(pokemon)) {
                return false;
            }
        }

        return true;
    }
}
