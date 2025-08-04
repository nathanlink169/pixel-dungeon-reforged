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

package com.shatteredpixel.shatteredpixeldungeon.items.stones;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Adrenaline;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Bless;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Doom;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Weakness;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfTeleportation;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.PathFinder;

public class StoneOfBlight extends Runestone {
	
	{
		image = ItemSpriteSheet.STONE_BLIGHT;
	}
	
	private static Ballistica throwPath;
	
	@Override
	public int throwPos(Hero user, int dst) {
		throwPath = new Ballistica( user.pos, dst, Ballistica.PROJECTILE );
		return throwPath.collisionPos;
	}
	@Override
	protected void activate(int cell) {
		GameScene.flash(0x707070);
		Sample.INSTANCE.play( Assets.Sounds.PUFF );

		for (int neighbour : PathFinder.NEIGHBOURS9) {
			if (Dungeon.level.passable[cell+neighbour]) {
				Char ch = Actor.findChar(cell+neighbour);

				if (ch != null) {
					if (ch != curUser) {
						if (ch.properties().contains(Char.Property.UNDEAD)) {
							Buff.affect(ch, Bless.class, Bless.DURATION);
							Buff.affect(ch, Adrenaline.class, Adrenaline.DURATION);
						} else if (!ch.isImmune(getClass())) {
							int damage = (int)Math.max(0.3f * ch.HP, 0.05f * ch.HT);
							if (damage >= ch.HP) {
								damage = ch.HP - 1;
							}
							ch.damage(damage, this);
						}
					} else /*if (ch == curUser)*/ {
						int damage = (int)Math.max(0.15f * ch.HP, 0.025f * ch.HT);
						if (damage >= ch.HP) {
							damage = ch.HP - 1;
						}
						ch.damage(damage, this);
					}
				}


				if (Dungeon.level.flamable[cell+neighbour]) {
					Level.set(cell+neighbour, Terrain.EMBERS);
				}
			}
		}
		GameScene.updateMap();

		Dungeon.observe();
	}
}
