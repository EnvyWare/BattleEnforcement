package com.envyful.battle.enforcement.command;

import com.envyful.api.command.annotate.Command;
import com.envyful.api.command.annotate.executor.CommandProcessor;
import com.envyful.api.command.annotate.executor.Sender;
import com.envyful.api.command.annotate.permission.Permissible;
import com.envyful.battle.enforcement.BattleEnforcement;
import net.minecraft.command.ICommandSource;

@Command(
        value = {
                "reload"
        }
)
@Permissible("battle.enforce.reload")
public class ReloadCommand {

    @CommandProcessor
    public void onCommand(@Sender ICommandSource source, String[] args) {
        BattleEnforcement.getInstance().reloadConfig();
    }
}
