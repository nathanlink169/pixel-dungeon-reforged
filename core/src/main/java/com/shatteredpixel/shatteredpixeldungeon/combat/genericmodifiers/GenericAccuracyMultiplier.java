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

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FlavourBuff;
import com.shatteredpixel.shatteredpixeldungeon.combat.AttackContext;
import com.shatteredpixel.shatteredpixeldungeon.combat.CombatModifier;

public class GenericAccuracyMultiplier extends FlavourBuff implements CombatModifier.AccuracyModifier {

    private float m_Multiplier = 1.0f;
    private boolean m_Attacker = false;

    private GenericAccuracyMultiplier() {}

    public static GenericAccuracyMultiplier AttackerModifier(float multiplier) {
        GenericAccuracyMultiplier toReturn = new GenericAccuracyMultiplier();
        toReturn.m_Attacker = true;
        toReturn.m_Multiplier = multiplier;
        return toReturn;
    }

    public static GenericAccuracyMultiplier DefenseModifier(float multiplier) {
        GenericAccuracyMultiplier toReturn = new GenericAccuracyMultiplier();
        toReturn.m_Attacker = false;
        toReturn.m_Multiplier = multiplier;
        return toReturn;
    }

    @Override
    public float modifyAccuracy(AttackContext context, float currentAccuracy) {
        return currentAccuracy * m_Multiplier;
    }

    @Override
    public int priority() {
        return Priority.HIGHEST;
    }

    @Override
    public boolean appliesTo(AttackContext context) {
        return m_Attacker ? (context.attacker == target) : (context.defender == target);
    }
}
