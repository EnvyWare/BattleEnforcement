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
import com.envyful.battle.enforcement.config.BattleType;
import com.pixelmonmod.pixelmon.api.battles.BattleResults;
import com.pixelmonmod.pixelmon.api.events.battles.BattleEndEvent;
import com.pixelmonmod.pixelmon.api.storage.PlayerPartyStorage;
import com.pixelmonmod.pixelmon.api.storage.StorageProxy;
import com.pixelmonmod.pixelmon.battles.api.BattleBuilder;
import com.pixelmonmod.pixelmon.battles.api.rules.teamselection.TeamSelectionRegistry;
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
        var partyOne = StorageProxy.getParty(targetOne.getParent());
        var partyTwo = StorageProxy.getParty(targetTwo.getParent());

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

        partyOne.heal();
        partyTwo.heal();

        var rules = type.createRules();

        BattleBuilder.builder()
                .rules(rules)
                .teamSelection(false)
                .teamOne(new PlayerParticipant(targetOne.getParent(), partyOne.getAll()))
                .teamTwo(new PlayerParticipant(targetTwo.getParent(), partyTwo.getAll()))
                .disableExp()
                .allowSpectators()
                .teamSelection(type.showTeamSelect())
                .teamSelectionBuilder(TeamSelectionRegistry.builder().battleRules(rules).showRules(false).hideOpponentTeam().notCloseable())
                .start().whenComplete((battleController, throwable) -> {
                    PlatformProxy.executeConsoleCommands(type.getStartCommands(),
                            Placeholder.simple("%player_one%", targetOne.getName()),
                            Placeholder.simple("%player_two%", targetTwo.getName()));

                    battleController.addTaskAtEvent(BattleEndEvent.class, (event, controller) -> {
                        var finishCommands = type.finishCommands();
                        var winner = event.getResult(targetOne.getParent()).orElseThrow() == BattleResults.VICTORY ? targetOne : targetTwo;
                        var loser = event.getResult(targetTwo.getParent()).orElseThrow() == BattleResults.VICTORY ? targetOne : targetTwo;

                        for (var command : finishCommands.getRandomRewards()) {
                            PlatformProxy.executeConsoleCommands(command.getCommands(),
                                    Placeholder.simple("%winner%",  winner.getName()),
                                    Placeholder.simple("%loser%", loser.getName()));
                        }
                    });
                });
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
