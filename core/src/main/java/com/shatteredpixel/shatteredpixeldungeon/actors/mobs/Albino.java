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

package com.shatteredpixel.shatteredpixeldungeon.actors.mobs;

import com.shatteredpixel.shatteredpixeldungeon.Challenges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.AscensionChallenge;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Bleeding;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Poison;
import com.shatteredpixel.shatteredpixeldungeon.items.food.MysteryMeat;
import com.shatteredpixel.shatteredpixeldungeon.sprites.AlbinoSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.RatSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.SpawnerSprite;
import com.watabou.utils.Random;

public class Albino extends Rat {

	{
		HP = HT = getRandomizerEnabled(RandomTraits.FRAIL_VERMIN) ? 6 : 15;
		EXP = 2;
		
		loot = MysteryMeat.class;
		lootChance = 1f;
	}
	@Override
	public Class<? extends CharSprite> GetSpriteClass() {
		return AlbinoSprite.class;
	}
	
	@Override
	public int attackProc( Char enemy, int damage ) {
		damage = super.attackProc( enemy, damage );
		if (damage > 0 && Random.Int( 2 ) == 0) {
			Buff.affect( enemy, Bleeding.class ).set( damage );
		}
		if (getRandomizerEnabled(RandomTraits.TOXIC_FANGS)) {
			if (Random.Int(3) == 0) {
				int duration = Random.IntRange(1, 3);
				if (Math.random() > 0.8f) {
					++duration; // really rare chance to get 4 turns
				}
				//we only use half the ascension modifier here as total poison dmg doesn't scale linearly
				duration = Math.round(duration * (AscensionChallenge.statModifier(this) / 2f + 0.5f));
				Buff.affect(enemy, Poison.class).set(duration);
			}
		}
		
		return damage;
	}
}
