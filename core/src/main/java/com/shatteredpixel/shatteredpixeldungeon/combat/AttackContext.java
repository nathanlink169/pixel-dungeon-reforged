/*
 * Pixel Dungeon Reforged
 * Copyright (C) 2024-2025 Nathan Pringle
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.shatteredpixel.shatteredpixeldungeon.combat;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.SPDSettings;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.effects.Surprise;
import com.watabou.noosa.audio.Sample;

import java.util.EnumSet;
import java.util.function.Consumer;

/**
 * AttackContext is an immutable context object containing all information about an attack.
 * It is a single source of truth for the basics of the attack, passed through the combat pipeline.
 */
public class AttackContext {
    public enum AttackType { MELEE, RANGED }

    // Participants
    public final Char attacker;
    public final Char defender;

    // Attack Properties
    public final AttackType attackType;
    public final EnumSet<DamageType> damageType;

    // Base Stats (from attacker / defender at time of attack)
    public final int baseAccuracy;
    public final int baseEvasion;
    public final int baseDamage;
    public final int baseArmour;
    public final int startingAttackerHP;
    public final int startingDefenderHP;

    // Flags
    public final boolean isSurpriseAttack;
    public final boolean isMaxDamage;

    // Position data (for range-based effects)
    public final int attackerPosition;
    public final int defenderPosition;
    public final int distance;

    // Visual Callbacks
    public final Runnable playHitSound;
    public final Consumer<Char> showSurpriseVisual;

    private AttackContext(Builder builder) {
        this.attacker = builder.attacker;
        this.defender = builder.defender;
        this.attackType = builder.attackType;
        this.damageType = builder.damageType;
        this.baseAccuracy = builder.attacker.attackSkill();
        this.baseEvasion = (builder.defender != null ? builder.defender.defenseSkill() : 0);
        this.startingAttackerHP = builder.attacker.HP;
        this.startingDefenderHP = builder.defender != null ? builder.defender.HP : 0;
        this.isSurpriseAttack = builder.defender instanceof Mob && ((Mob) builder.defender).surprisedBy(builder.attacker);
        this.isMaxDamage = builder.forceMaxDamage || (this.isSurpriseAttack && SPDSettings.difficulty() == 1);
        this.baseArmour = builder.defender != null ? builder.defender.drRoll(this.damageType) : 0;
        this.attackerPosition = builder.attacker.pos;
        this.defenderPosition = builder.defender != null ? builder.defender.pos : 0;
        this.distance = Dungeon.level.distance(attackerPosition, defenderPosition);
        this.playHitSound = builder.attacker.hitSound();
        this.showSurpriseVisual = builder.attacker.surpriseVisual();

        // This must be last, as the damage needs information from this
        this.baseDamage = builder.attacker.damageRoll(this);
    }

    public static class Builder {
        private final Char attacker;
        private final Char defender;
        private AttackType attackType = AttackType.MELEE;
        private EnumSet<DamageType> damageType = EnumSet.of(DamageType.NONE);
        private boolean forceMaxDamage = false;

        public Builder(Char attacker, Char defender) {
            this.attacker = attacker;
            this.defender = defender;
        }

        public Builder attackType(AttackType type) {
            this.attackType = type;
            return this;
        }

        public Builder damageType(EnumSet<DamageType> type) {
            this.damageType = type;
            return this;
        }

        public Builder forceMaxDamage() {
            this.forceMaxDamage = true;
            return this;
        }

        public AttackContext build() {
            return new AttackContext(this);
        }
    }
}
