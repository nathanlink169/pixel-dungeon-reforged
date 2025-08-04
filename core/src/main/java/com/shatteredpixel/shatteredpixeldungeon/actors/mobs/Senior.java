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
import com.shatteredpixel.shatteredpixeldungeon.items.food.Pasty;
import com.shatteredpixel.shatteredpixeldungeon.sprites.AcidicSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.RatSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.SeniorSprite;
import com.watabou.utils.Random;

public class Senior extends Monk {

	{
		loot = Pasty.class;
		lootChance = 1f;
	}
	@Override
	public Class<? extends CharSprite> GetSpriteClass() {

		return SeniorSprite.class;
	}
	
	@Override
	public void move( int step, boolean travelling) {
		// on top of the existing move bonus, senior monks get a further 1.66 cooldown reduction
		// for a total of 3.33, double the normal 1.67 for regular monks
		if (travelling) {
			focusCooldown -= 1.66f;
			if (getRandomizerEnabled(RandomTraits.RAPID_MEDITATION)) {
				focusCooldown = -1.0f; // Moving immediate focus
			}
		}
		super.move( step, travelling);
	}
	
	@Override
	public int damageRoll(boolean isMaxDamage) {
		if (isMaxDamage) return 25;
		return Random.NormalIntRange( 16, 25 );
	}
	
}
