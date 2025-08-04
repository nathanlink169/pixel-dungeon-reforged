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
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.AscensionChallenge;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.effects.FloatingText;
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.PlateArmor;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.ScaleArmor;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.RatSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ShieldedSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.SpawnerSprite;
import com.watabou.utils.Random;

public class ArmoredBrute extends Brute {

	{
		//see rollToDropLoot
		loot = Generator.Category.ARMOR;
		lootChance = 1f;
	}
	@Override
	public Class<? extends CharSprite> GetSpriteClass() {
		return ShieldedSprite.class;
	}
	
	@Override
	public int drRoll() {
		return super.drRoll() + 4; //4-12 DR total
	}
	
	@Override
	protected void triggerEnrage () {
		if (Brute.getRandomizerEnabled(RandomTraits.STAND_YOUR_GROUND)) {
			rooted = true;
		}

		Buff.affect(this, ArmoredRage.class).setShield(HT/2 + 1);
		sprite.showStatusWithIcon( CharSprite.POSITIVE, Integer.toString(HT/2 + 1), FloatingText.SHIELDING );
		if (Dungeon.level.heroFOV[pos]) {
			sprite.showStatus( CharSprite.WARNING, Messages.get(this, "enraged") );
		}
		spend( TICK );
		hasRaged = true;
	}
	
	@Override
	public Item createLoot() {
		if (Random.Int( 4 ) == 0) {
			return new PlateArmor().random();
		}
		return new ScaleArmor().random();
	}
	
	//similar to regular brute rate, but deteriorates much slower. 60 turns to death total.
	public static class ArmoredRage extends Brute.BruteRage {
		
		@Override
		public boolean act() {
			
			if (target.HP > 0){
				detach();
				return true;
			}

			if (Brute.getRandomizerEnabled(RandomTraits.STAND_YOUR_GROUND)) {
				target.rooted = true;
			}
			
			absorbDamage( Math.round(AscensionChallenge.statModifier(target)) );
			
			if (shielding() <= 0){
				target.die(null);
			}

			if (Brute.getRandomizerEnabled(RandomTraits.EXTENDED_FURY)) {
				spend (5 * TICK);
			} else {
				spend(3 * TICK);
			}
			
			return true;
		}
		
	}
}
