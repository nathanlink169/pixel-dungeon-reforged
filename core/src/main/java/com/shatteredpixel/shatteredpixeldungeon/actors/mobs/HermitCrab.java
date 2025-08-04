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
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.sprites.AcidicSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.HermitCrabSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.RatSprite;

public class HermitCrab extends Crab {

	{
		HP = HT = 25; //+67% HP
		baseSpeed = getRandomizerEnabled(RandomTraits.HERMIT_INVASION) ? (getRandomizerEnabled(RandomTraits.LIGHTNING_LEGS) ? 3.5f : 2f) : (getRandomizerEnabled(RandomTraits.LIGHTNING_LEGS) ? 1.75f : 1f); //-50% speed

		//3x more likely to drop meat, and drops a guaranteed armor
		lootChance = 0.5f;
	}
	@Override
	public Class<? extends CharSprite> GetSpriteClass() {
		return HermitCrabSprite.class;
	}

	@Override
	public void rollToDropLoot() {
		super.rollToDropLoot();

		if (Dungeon.hero.lvl <= maxLvl + 2){
			Dungeon.level.drop(Generator.randomArmor(), pos).sprite.drop();
		}
	}

	@Override
	public int drRoll() {
		return super.drRoll() + 2; //2-6 DR total, up from 0-4
	}

}
