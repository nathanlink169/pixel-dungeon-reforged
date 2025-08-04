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

package com.shatteredpixel.shatteredpixeldungeon.items.rings;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

public class RingOfTenacity extends Ring {

	{
		icon = ItemSpriteSheet.Icons.RING_TENACITY;
		buffClass = Tenacity.class;
	}

	public String statsInfo() {
		if (isIdentified()){
			String info = Messages.get(this, "stats",
					Messages.decimalFormat("#", 10 - getDamageTaken(10, soloBuffedBonus())),
					Messages.decimalFormat("#", 25 - getDamageTaken(25, soloBuffedBonus())),
					Messages.decimalFormat("#", 50 - getDamageTaken(50, soloBuffedBonus())));
			if (isEquipped(Dungeon.hero) && soloBuffedBonus() != combinedBuffedBonus(Dungeon.hero)){
				info += "\n\n" + Messages.get(this, "combined_stats",
						Messages.decimalFormat("#", 10 - getDamageTaken(10, combinedBuffedBonus(Dungeon.hero))),
						Messages.decimalFormat("#", 25 - getDamageTaken(25, combinedBuffedBonus(Dungeon.hero))),
						Messages.decimalFormat("#", 50 - getDamageTaken(50, combinedBuffedBonus(Dungeon.hero))));
			}
			return info;
		} else {
			return Messages.get(this, "typical_stats", "0", "2", "6");
		}
	}

	public String upgradeStat1(int level){
		return upgradeStatForDamage(level, 10);
	}

	public String upgradeStat2(int level) {
		return upgradeStatForDamage(level, 25);
	}

	public String upgradeStat3(int level) {
		return upgradeStatForDamage(level, 50);
	}

	private String upgradeStatForDamage(int level, int damage) {
		if (cursed && cursedKnown) level = Math.min(-1, level-3);
		++level; // for some reason, we need to do this? I don't know why but the level comes in one level down
		return Messages.decimalFormat("#", damage - getDamageTaken(damage, level));
	}

	@Override
	protected RingBuff buff( ) {
		return new Tenacity();
	}
	
	public static int getDamageTaken( Char t, int initialDamage) {
		if (t.buff(Tenacity.class) == null) {
			return initialDamage;
		}
		return getDamageTaken(initialDamage, getBuffedBonus(t, Tenacity.class));
	}

	private static int getDamageTaken( int initialDamage, float tier) {
		float r = 0.2f * (tier + 1);
		float percentage;
		if (r >= 0.0f) {
			percentage = (100 + initialDamage) / (100 + initialDamage + r * initialDamage);
		}
		else {
			percentage = (100 + initialDamage * Math.abs(r) * initialDamage) / (100 + initialDamage);
		}

		return Math.round(initialDamage * percentage);
	}

	public class Tenacity extends RingBuff {
	}
}

