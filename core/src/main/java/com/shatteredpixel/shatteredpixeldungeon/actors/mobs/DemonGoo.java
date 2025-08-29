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

import com.shatteredpixel.shatteredpixeldungeon.Constants;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Burning;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Light;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Ooze;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Poison;
import com.shatteredpixel.shatteredpixeldungeon.effects.Pushing;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.Door;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.DemonGooSprite;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class DemonGoo extends Mob {
	private int demonGooGeneration = 0;

	private static final float SPLIT_DELAY = 1f;
	private static final String DEMONGOOGENERATION = "demonGooGeneration";
	@Override
	public Constants.mobs.mobsBase GetConstants() { return Constants.mobs.demongoo; }


	@Override
	protected boolean act() {
		boolean result = super.act();

		if (Dungeon.level.water[pos] && HP < GetMaxHP()) {
			sprite.emitter().burst( Speck.factory( Speck.HEALING ), 1 );
			HP++;
		}
		return result;
	}

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put(DEMONGOOGENERATION, demonGooGeneration);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		demonGooGeneration = bundle.getInt(DEMONGOOGENERATION);
	}

	@Override
	public int defenseProc(Char enemy, int damage) {

		if (HP >= damage + 2) {
			ArrayList<Integer> candidates = new ArrayList<Integer>();
			boolean[] passable = Dungeon.level.passable;

			int[] neighbours = { pos + 1, pos - 1, pos + Dungeon.level.width(),
					pos - Dungeon.level.width() };
			for (int n : neighbours) {
				if (passable[n] && Actor.findChar(n) == null) {
					candidates.add(n);
				}
			}

			if (candidates.size() > 0) {
				DemonGoo clone = split();
				clone.pos = Random.element(candidates);
				clone.state = clone.HUNTING;

				if (Dungeon.level.map[clone.pos] == Terrain.DOOR) {
					Door.enter(clone.pos);
				}

				GameScene.add(clone, SPLIT_DELAY);
				clone.HP = (HP - damage) / 2;
				Actor.add(new Pushing(clone, pos, clone.pos));

				HP -= clone.HP;
			}
		}

		return damage;
	}

	private DemonGoo split() {
		DemonGoo clone = new DemonGoo();
		clone.demonGooGeneration = demonGooGeneration + 1;
		if (buff(Burning.class) != null) {
			Buff.affect(clone, Burning.class).reignite(clone);
		}
		if (buff(Poison.class) != null) {
			Buff.affect(clone, Poison.class).set(2);
		}
		return clone;
	}

	@Override
	public int attackProc(Char enemy, int damage) {
		if (Random.Int(3) == 0) {
			Buff.affect(enemy, Ooze.class);
			enemy.sprite.burst(0x000000, 5);
		}
		return damage;
	}
}