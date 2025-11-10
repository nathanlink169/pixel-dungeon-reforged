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

package com.shatteredpixel.shatteredpixeldungeon.items.armor.glyphs;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Charm;
import com.shatteredpixel.shatteredpixeldungeon.combat.AttackContext;
import com.shatteredpixel.shatteredpixeldungeon.combat.CombatModifier;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfBlastWave;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.watabou.utils.Random;

public class Repulsion extends Armor.Glyph implements CombatModifier.OnHitEffect {

	private static ItemSprite.Glowing WHITE = new ItemSprite.Glowing( 0xFFFFFF );

	@Override
	public void onHit(AttackContext context, int finalDamage) {
		int level = Math.max( 0, context.defender.getArmor().buffedLvl() );

		// lvl 0 - 20%
		// lvl 1 - 33%
		// lvl 2 - 43%
		float procChance = (level+1f)/(level+5f) * procChanceMultiplier(context.defender);
		if (Dungeon.level.adjacent(context.attackerPosition, context.defenderPosition) && Random.Float() < procChance){

			float powerMulti = Math.max(1f, procChance);

			int oppositeHero = context.attackerPosition + (context.attackerPosition - context.defenderPosition);
			Ballistica trajectory = new Ballistica(context.attackerPosition, oppositeHero, Ballistica.MAGIC_BOLT);
			WandOfBlastWave.throwChar(context.attacker,
					trajectory,
					Math.round(2 * powerMulti),
					true,
					true,
					this);
		}
	}

	@Override
	public int priority() {
		return CombatModifier.Priority.NORMAL;
	}

	@Override
	public boolean appliesTo(AttackContext context) {
		return context.defender.getArmor() != null && context.defender.getArmor().glyph == this;
	}

	@Override
	public ItemSprite.Glowing glowing() {
		return WHITE;
	}
}
