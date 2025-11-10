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
import com.shatteredpixel.shatteredpixeldungeon.effects.Splash;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfBlastWave;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.SpiritBow;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Crossbow;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.MissileWeapon;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.watabou.utils.Random;

public class Elastic extends Weapon.Enchantment implements CombatModifier.OnHitEffect {
	
	private static ItemSprite.Glowing PINK = new ItemSprite.Glowing( 0xFF00FF );

	@Override
	public void onHit(AttackContext context, int finalDamage) {
		int level = Math.max(0, context.attacker.getWeapon().buffedLvl());

		boolean chargedShot = Dungeon.hero.buff(Crossbow.ChargedShot.class) != null;

		// lvl 0 - 20%
		// lvl 1 - 33%
		// lvl 2 - 43%
		float procChance = (level + 1f) / (level + 5f) * procChanceMultiplier(context.attacker);

		if (Random.Float() < procChance || chargedShot) {
			float powerMulti = Math.max(1f, procChance);

			// Trace a ballistica to our target (which will also extend past them)
			Ballistica trajectory = new Ballistica(
					context.attacker.pos,
					context.defender.pos,
					Ballistica.STOP_TARGET
			);

			// Trim it to just be the part that goes past them
			trajectory = new Ballistica(
					trajectory.collisionPos,
					trajectory.path.get(trajectory.path.size() - 1),
					Ballistica.PROJECTILE
			);

			// Knock them back along that ballistica
			WandOfBlastWave.throwChar(
					context.defender,
					trajectory,
					Math.round(2 * powerMulti * (chargedShot ? 2 : 1)),
					!(context.attacker.getWeapon() instanceof MissileWeapon || context.attacker.getWeapon() instanceof SpiritBow),
					true,
					this
			);
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
	public ItemSprite.Glowing glowing() {
		return PINK;
	}

}
