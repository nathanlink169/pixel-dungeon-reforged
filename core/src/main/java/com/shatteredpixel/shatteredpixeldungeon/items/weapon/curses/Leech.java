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

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.combat.AttackContext;
import com.shatteredpixel.shatteredpixeldungeon.combat.CombatModifier;
import com.shatteredpixel.shatteredpixeldungeon.combat.DamageType;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;

public class Leech extends Weapon.Enchantment implements Hero.Doom, CombatModifier.OnDamageEffect {

	private static ItemSprite.Glowing BLACK = new ItemSprite.Glowing( 0x000000 );

	private static final int TURNS_TO_ACTIVATE = 50;
	private static final int DAMAGE_INTERVAL = 20;

	private static float turnsWithoutDamage = 0.0f;

	@Override
	public void onDamage (AttackContext context, int finalDamage) {
		turnsWithoutDamage = 0.0f;
	}

	@Override
	public int priority() {
		return CombatModifier.Priority.NORMAL;
	}

	@Override
	public boolean appliesTo(AttackContext context) {
		return context.attacker.getWeapon() != null && context.attacker.getWeapon().enchantment == this;
	}

	// 20 turns to activate, triggers 1 damage, then damage every 5 turns, squares the damage (1, 2, 4, 16, etc.)
	public void triggerDamage( Char owner, float time ) {
		turnsWithoutDamage += time;
		int intTurns = (int)(turnsWithoutDamage);
		if (intTurns < TURNS_TO_ACTIVATE || (intTurns - TURNS_TO_ACTIVATE) % DAMAGE_INTERVAL != 0) {
			return;
		}

		int damage = (int) Math.pow(2, (intTurns - TURNS_TO_ACTIVATE) / (float)DAMAGE_INTERVAL);
		owner.Damage(damage, this, DamageType.of(DamageType.NONE));
	}

	@Override
	public boolean curse() {
		return true;
	}

	@Override
	public ItemSprite.Glowing glowing() {
		return BLACK;
	}

	@Override
	public void onDeath() {
		Dungeon.fail( this );
		GLog.n( Messages.get(this, "ondeath") );
	}
}
