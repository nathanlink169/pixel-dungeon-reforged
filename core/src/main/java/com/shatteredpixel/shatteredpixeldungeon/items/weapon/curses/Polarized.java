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

package com.shatteredpixel.shatteredpixeldungeon.items.weapon.curses;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.combat.AttackContext;
import com.shatteredpixel.shatteredpixeldungeon.combat.CombatModifier;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.watabou.utils.Random;

public class Polarized extends Weapon.Enchantment implements CombatModifier.PreArmorDamageModifier {
	
	private static ItemSprite.Glowing BLACK = new ItemSprite.Glowing( 0x000000 );

	@Override
	public boolean curse() {
		return true;
	}
	
	@Override
	public ItemSprite.Glowing glowing() {
		return BLACK;
	}

	@Override
	public int modifyPreArmorDamage(AttackContext context, int currentDamage) {
		float procChance = 0.5f * procChanceMultiplier(context.attacker);
		if (Random.Float() < procChance) {
			return Math.round(1.5f * currentDamage);
		} else {
			return 0;
		}
	}

	@Override
	public int priority() {
		return Priority.HIGHEST;
	}

	@Override
	public boolean appliesTo(AttackContext context) {
		return context.attacker.getWeapon() != null && context.attacker.getWeapon().enchantment == this;
	}
}
