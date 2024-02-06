package com.envyful.battle.enforcement.config;

import com.envyful.api.config.yaml.AbstractYamlConfig;
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

import java.util.List;
import java.util.Set;

@ConfigSerializable
public class BattleType extends AbstractYamlConfig {

    private String id;
    private int requiredPokemon;
    private List<ConfigBattleRule> rules;
    private List<String> blacklistPokemon;
    private transient List<PokemonSpecification> blacklistPokemonCache;

    public BattleType() {
        super();
    }

    public String id() {
        return this.id;
    }

    public int requiredPokemon() {
        return this.requiredPokemon;
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
                this.blacklistPokemonCache.add(PokemonSpecificationProxy.create(pokemonName).get());
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
        private List<ConfigBattleRule> rules = Lists.newArrayList();
        private List<String> blacklistPokemon = Lists.newArrayList();

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

        public Builder addBlacklistPokemon(String pokemon) {
            this.blacklistPokemon.add(pokemon);
            return this;
        }

        public BattleType build() {
            BattleType battleType = new BattleType();
            battleType.id = this.id;
            battleType.requiredPokemon = this.requiredPokemon;
            battleType.rules = this.rules;
            battleType.blacklistPokemon = this.blacklistPokemon;
            return battleType;
        }
    }
}
