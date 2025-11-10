/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2025 Evan Debenham
 *
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

package com.shatteredpixel.shatteredpixeldungeon.combat.genericmodifiers;

import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FlavourBuff;
import com.shatteredpixel.shatteredpixeldungeon.combat.AttackContext;
import com.shatteredpixel.shatteredpixeldungeon.combat.CombatModifier;

public class GenericPreArmourDamageBonus extends FlavourBuff implements CombatModifier.PreArmorDamageModifier {
    private int m_Bonus = 0;
    private boolean m_Attacker = false;

    private GenericPreArmourDamageBonus() {}

    public static GenericPreArmourDamageBonus AttackerModifier(int bonus) {
        GenericPreArmourDamageBonus toReturn = new GenericPreArmourDamageBonus();
        toReturn.m_Attacker = true;
        toReturn.m_Bonus = bonus;
        return toReturn;
    }

    public static GenericPreArmourDamageBonus DefenseModifier(int bonus) {
        GenericPreArmourDamageBonus toReturn = new GenericPreArmourDamageBonus();
        toReturn.m_Attacker = false;
        toReturn.m_Bonus = bonus;
        return toReturn;
    }

    @Override
    public int modifyPreArmorDamage(AttackContext context, int currentDamage) {
        return currentDamage + m_Bonus;
    }

    @Override
    public int priority() {
        return Priority.HIGHEST - 1;
    } // Apply AFTER multiplier

    @Override
    public boolean appliesTo(AttackContext context) {
        return m_Attacker ? (context.attacker == target) : (context.defender == target);
    }
}
