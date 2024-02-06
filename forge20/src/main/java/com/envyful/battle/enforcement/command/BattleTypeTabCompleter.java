package com.envyful.battle.enforcement.command;

import com.envyful.api.command.injector.TabCompleter;
import com.envyful.battle.enforcement.BattleEnforcement;
import com.envyful.battle.enforcement.config.BattleType;
import net.minecraft.commands.CommandSource;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.stream.Collectors;

public class BattleTypeTabCompleter implements TabCompleter<CommandSource> {
    @Override
    public List<String> getCompletions(CommandSource iCommandSource, String[] strings, Annotation... annotations) {
        return BattleEnforcement.getConfig().getBattleTypes().stream().map(BattleType::id).collect(Collectors.toList());
    }
}
