package com.envyful.battle.enforcement.config;

import com.envyful.api.config.yaml.AbstractYamlConfig;
import com.envyful.api.forge.config.ConfigReward;
import com.envyful.api.forge.config.ConfigRewardPool;
import com.envyful.api.reforged.battle.ConfigBattleRule;
import com.envyful.battle.enforcement.BattleEnforcement;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.pixelmonmod.api.pokemon.PokemonSpecification;
import com.pixelmonmod.api.pokemon.PokemonSpecificationProxy;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.battles.api.rules.BattleRuleRegistry;
import com.pixelmonmod.pixelmon.battles.api.rules.BattleRules;
import com.pixelmonmod.pixelmon.battles.api.rules.clauses.BattleClause;
import com.pixelmonmod.pixelmon.battles.api.rules.clauses.BattleClauseRegistry;
import com.pixelmonmod.pixelmon.battles.api.rules.value.ClausesValue;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;

import java.util.List;
import java.util.Set;

@ConfigSerializable
public class BattleType extends AbstractYamlConfig {

    @Comment("The unique identifier for this type of battle")
    private String id;
    @Comment("The number of Pokemon required for this battle type")
    private int requiredPokemon;
    @Comment("Whether to show the team select screen")
    private boolean showTeamSelect;
    @Comment("The rules for this battle type")
    private List<ConfigBattleRule> rules;
    @Comment("The Pokemon that are blacklisted from this battle type")
    private List<String> blacklistPokemon;
    private transient List<PokemonSpecification> blacklistPokemonCache;
    @Comment("The commands to run when the battle starts")
    private List<String> startCommands;
    @Comment("The commands to execute when the battle finishes")
    private ConfigRewardPool<ConfigReward> finishCommands;

    public BattleType() {
        super();
    }

    protected BattleType(Builder builder) {
        this.id = builder.id;
        this.requiredPokemon = builder.requiredPokemon;
        this.showTeamSelect = builder.showTeamSelect;
        this.rules = builder.rules;
        this.blacklistPokemon = builder.blacklistPokemon;
        this.startCommands = builder.startCommands;
        this.finishCommands = builder.finishCommands;
    }

    public String id() {
        return this.id;
    }

    public int requiredPokemon() {
        return this.requiredPokemon;
    }

    public boolean showTeamSelect() {
        return this.showTeamSelect;
    }

    public List<String> getStartCommands() {
        return this.startCommands;
    }

    public ConfigRewardPool<ConfigReward> finishCommands() {
        return this.finishCommands;
    }

    public BattleRules createRules() {
        BattleRules battleRules = new BattleRules().set(BattleRuleRegistry.BATTLE_TYPE, com.pixelmonmod.pixelmon.api.battles.BattleType.SINGLE);
        Set<BattleClause> clauses = Sets.newHashSet();

        for (var rule : this.rules) {
            var property = BattleRuleRegistry.getProperty(rule.getBattleRuleType());

            if (property != null) {
                battleRules.set(property, rule.getBattleRuleValue());
            } else {
                var clause = BattleClauseRegistry.getClause(rule.getBattleRuleType());
                if (clause != null) {
                    clauses.add(clause);
                } else {
                    BattleEnforcement.getLogger().error("Invalid battle rule or clause found `{}` in gym: {}", rule.getBattleRuleType(), this.id());
                }
            }
        }

        battleRules.set(BattleRuleRegistry.CLAUSES, new ClausesValue(clauses));

        return battleRules;
    }

    public boolean isBlacklisted(Pokemon pokemon) {
        if (this.blacklistPokemonCache == null) {
            this.blacklistPokemonCache = Lists.newArrayList();

            for (var pokemonName : this.blacklistPokemon) {
                this.blacklistPokemonCache.add(PokemonSpecificationProxy.create(pokemonName));
            }
        }

        for (var spec : this.blacklistPokemonCache) {
            if (spec.matches(pokemon)) {
                return true;
            }
        }

        return false;
    }

    public static Builder builder(String id) {
        return new Builder().id(id);
    }

    public static class Builder {

        private String id;
        private int requiredPokemon;
        private boolean showTeamSelect;
        private List<ConfigBattleRule> rules = Lists.newArrayList();
        private List<String> startCommands = Lists.newArrayList();
        private List<String> blacklistPokemon = Lists.newArrayList();
        private ConfigRewardPool<ConfigReward> finishCommands;

        public Builder() {}

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder requiredPokemon(int requiredPokemon) {
            this.requiredPokemon = requiredPokemon;
            return this;
        }

        public Builder addRule(ConfigBattleRule rule) {
            this.rules.add(rule);
            return this;
        }

        public Builder addStartCommand(String command) {
            this.startCommands.add(command);
            return this;
        }

        public Builder addBlacklistPokemon(String pokemon) {
            this.blacklistPokemon.add(pokemon);
            return this;
        }

        public Builder showTeamSelect() {
            return this.showTeamSelect(true);
        }

        public Builder noTeamSelect() {
            return this.showTeamSelect(false);
        }

        public Builder showTeamSelect(boolean showTeamSelect) {
            this.showTeamSelect = showTeamSelect;
            return this;
        }

        public Builder finishCommands(ConfigRewardPool<ConfigReward> finishCommands) {
            this.finishCommands = finishCommands;
            return this;
        }

        public BattleType build() {
            return new BattleType(this);
        }
    }
}
