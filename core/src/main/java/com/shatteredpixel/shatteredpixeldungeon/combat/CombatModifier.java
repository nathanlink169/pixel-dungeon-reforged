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

/**
 * Base interface for anything that modifies combat calculations.
 * All combat modifiers are defined as nested interfaces for organization.
 */
public interface CombatModifier {
    /**
     * @return Priority order (higher = earlier)
     */
    int priority();

    /**
     * @return true if this modifier should apply to this attack
     */
    boolean appliesTo(AttackContext context);

    // Nested interfaces - all related modifiers in one place

    interface AccuracyModifier extends CombatModifier {
        float modifyAccuracy(AttackContext context, float currentAccuracy);
    }

    interface EvasionModifier extends CombatModifier {
        float modifyEvasion(AttackContext context, float currentEvasion);
    }

    interface PreArmorDamageModifier extends CombatModifier {
        int modifyPreArmorDamage(AttackContext context, int currentDamage);
    }

    interface ArmorModifier extends CombatModifier {
        int modifyArmor(AttackContext context, int currentArmor);
    }

    interface PostArmorDamageModifier extends CombatModifier {
        int modifyPostArmorDamage(AttackContext context, int currentDamage);
    }

    interface OnHitEffect extends CombatModifier {
        void onHit(AttackContext context, int finalDamage);
    }

    interface OnMissEffect extends CombatModifier {
        void onMiss(AttackContext context);
    }

    interface OnDamageEffect extends CombatModifier {
        void onDamage(AttackContext context, int damageDealt);
    }

    class Priority {
        public static final int HIGHEST = 1000;
        public static final int HIGH = 100;
        public static final int NORMAL = 0;
        public static final int LOW = -100;
        public static final int LOWEST = -1000;
    }
}