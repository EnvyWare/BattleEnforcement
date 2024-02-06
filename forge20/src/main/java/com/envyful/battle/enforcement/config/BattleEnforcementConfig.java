package com.envyful.battle.enforcement.config;

import com.envyful.api.config.data.ConfigPath;
import com.envyful.api.config.yaml.AbstractYamlConfig;
import com.envyful.api.config.yaml.DefaultConfig;
import com.envyful.api.config.yaml.YamlConfigFactory;
import com.envyful.api.reforged.battle.ConfigBattleRule;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

import java.io.IOException;
import java.util.List;

@ConfigSerializable
@ConfigPath("config/BattleEnforcement/config.yml")
public class BattleEnforcementConfig extends AbstractYamlConfig {

    private transient List<BattleType> battleTypes;

    public BattleEnforcementConfig() throws IOException {
        super();

        this.battleTypes = YamlConfigFactory.getInstances(BattleType.class,
                "config/BattleEnforcement/battleTypes",
                DefaultConfig.onlyNew("example.yml", BattleType.builder("example")
                                .requiredPokemon(6)
                                .addBlacklistPokemon("mewtwo")
                                .addRule(new ConfigBattleRule("FullHeal", "true"))
                        .build()));
    }

    public List<BattleType> getBattleTypes() {
        return this.battleTypes;
    }

    public BattleType typeFromId(String id) {
        for (BattleType battleType : this.battleTypes) {
            if (battleType.id().equalsIgnoreCase(id)) {
                return battleType;
            }
        }

        return null;
    }
}
