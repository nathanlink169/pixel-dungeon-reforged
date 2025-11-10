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

package com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Chill;
import com.shatteredpixel.shatteredpixeldungeon.combat.AttackContext;
import com.shatteredpixel.shatteredpixeldungeon.combat.CombatModifier;
import com.shatteredpixel.shatteredpixeldungeon.combat.DamageType;
import com.shatteredpixel.shatteredpixeldungeon.effects.Splash;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite.Glowing;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class Chilling extends Weapon.Enchantment implements CombatModifier.OnHitEffect {

	private static ItemSprite.Glowing TEAL = new ItemSprite.Glowing( 0x00FFFF );

	@Override
	public void onHit(AttackContext context, int finalDamage) {
		int level = Math.max(0, context.attacker.getWeapon().buffedLvl());

		// lvl 0 - 25%
		// lvl 1 - 40%
		// lvl 2 - 50%
		float procChance = (level + 1f) / (level + 4f) * procChanceMultiplier(context.attacker);

		if (Random.Float() < procChance) {
			float powerMulti = Math.max(1f, procChance);

			// Adds 3 turns of chill per proc, with a cap of 6 turns
			float durationToAdd = 3f * powerMulti;
			Chill existing = context.defender.buff(Chill.class);
			if (existing != null) {
				durationToAdd = Math.min(durationToAdd, (6f * powerMulti) - existing.cooldown());
			}

			if (durationToAdd > 0) {
				Buff.affect(context.defender, Chill.class, durationToAdd);
			}

			Splash.at(context.defender.sprite.center(), 0xFFB2D6FF, 5);
		}
	}

	@Override
	public int priority() {
		return CombatModifier.Priority.NORMAL;
	}

	@Override
	public boolean appliesTo(AttackContext context) {
		return context.attacker.getWeapon() != null && context.attacker.getWeapon().enchantment == this;
	}

	@Override
	public Glowing glowing() {
		return TEAL;
	}

}
