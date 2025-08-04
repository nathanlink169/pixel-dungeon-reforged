/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2025 Evan Debenham
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

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.Ratmogrify;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.RatSprite;
import com.watabou.utils.Bundle;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import java.util.Collections;

public class Rat extends Mob {

	{
		spriteClass = RatSprite.class;
		
		HP = HT = 8;
		defenseSkill = 2;

		maxLvl = 5;
	}

	@Override
	protected boolean act() {
		if (Dungeon.level.heroFOV[pos] && Dungeon.hero.armorAbility instanceof Ratmogrify){
			alignment = Alignment.ALLY;
			if (state == SLEEPING) state = WANDERING;
		}
		return super.act();
	}

	@Override
	public int damageRoll(boolean isMaxDamage) {
		if (isMaxDamage) return 4;
		return Random.NormalIntRange( 1, 4 );
	}
	
	@Override
	public int attackSkill( Char target ) {
		return 8;
	}
	
	@Override
	public int drRoll() {
		return super.drRoll() + Random.NormalIntRange(0, 1);
	}

	private static final String RAT_ALLY = "rat_ally";

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		if (alignment == Alignment.ALLY) bundle.put(RAT_ALLY, true);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		if (bundle.contains(RAT_ALLY)) alignment = Alignment.ALLY;
	}

	public static void spawnAround(int pos) {
		int[] neighbours = GetRandomNeighbours();

		for (int n : neighbours) {
			int cell = pos + n;
			if (Dungeon.level.passable[cell] && Actor.findChar(cell) == null) {
				Rat r = new Rat();
				r.pos = cell;
				r.state = r.HUNTING;
				GameScene.add( r );
				Dungeon.level.occupyCell(r);
				return;
			}
		}

		// if we get here, no place around works. Spawn it randomly
		Rat r = new Rat();
		r.pos = -1;
		int tries = 30;
		do {
			r.pos = Dungeon.level.randomRespawnCell(r);
			if (r.pos != -1) {
				r.state = r.HUNTING;
				GameScene.add( r );
				Dungeon.level.occupyCell(r);
				return;
			}

			tries--;
		} while (tries > 0);
	}

	private static int[] GetRandomNeighbours() {
		int[] neighbours = PathFinder.NEIGHBOURS8;
		int index;
		for (int i = neighbours.length - 1; i > 0; i--)
		{
			index = Random.Int(i + 1);
			if (index != i)
			{
				neighbours[index] ^= neighbours[i];
				neighbours[i] ^= neighbours[index];
				neighbours[index] ^= neighbours[i];
			}
		}
		return neighbours;
	}
}
