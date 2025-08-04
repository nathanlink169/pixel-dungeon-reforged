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

package com.shatteredpixel.shatteredpixeldungeon.items.remains;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.effects.FloatingText;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfBlastWave;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

public class CrackedGun extends RemainsItem {

	{
		image = ItemSpriteSheet.CRACKED_GUN;
	}

	@Override
	protected void doEffect(Hero hero) {

		for (int neighbourOffset : PathFinder.NEIGHBOURS8) {

			Char ch = Dungeon.level.findMob(hero.pos + neighbourOffset);
			if (ch != null) {
				ch.damage(Random.Int(Math.max((int) (ch.HT * 0.05f), 1), Math.max((int) (ch.HT * 0.15f), 2)), hero);

				//trace a ballistica to our target (which will also extend past them)
				Ballistica trajectory = new Ballistica(hero.pos, ch.pos, Ballistica.STOP_TARGET);
				//trim it to just be the part that goes past them
				trajectory = new Ballistica(trajectory.collisionPos, trajectory.path.get(trajectory.path.size() - 1), Ballistica.PROJECTILE);
				//knock them back along that ballistica
				WandOfBlastWave.throwChar(ch,
						trajectory,
						2,
						false,
						true,
						hero);
			}
		}
	}

}
