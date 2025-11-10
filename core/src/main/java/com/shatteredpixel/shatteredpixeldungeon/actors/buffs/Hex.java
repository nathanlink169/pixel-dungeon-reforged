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

package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.combat.AttackContext;
import com.shatteredpixel.shatteredpixeldungeon.combat.CombatModifier;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;

public class Hex extends FlavourBuff implements CombatModifier.AccuracyModifier, CombatModifier.EvasionModifier {
	
	public static final float DURATION	= 30f;
	
	{
		type = buffType.NEGATIVE;
		announced = true;
	}
	
	@Override
	public int icon() {
		return BuffIndicator.HEX;
	}

	@Override
	public float iconFadePercent() {
		return Math.max(0, (DURATION - visualcooldown()) / DURATION);
	}

	@Override
	public float modifyAccuracy(AttackContext context, float currentAccuracy) {
		if (context.attacker == target) {
			return currentAccuracy * 0.8f;
		}
		return currentAccuracy;
	}

	@Override
	public int priority() {
		return Priority.NORMAL;
	}

	@Override
	public boolean appliesTo(AttackContext context) {
		return context.attacker == target || context.defender == target;
	}

	@Override
	public float modifyEvasion(AttackContext context, float currentEvasion) {
		if (context.defender == target) {
			return currentEvasion * 0.8f;
		}
		return currentEvasion;
	}
}
