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
import com.shatteredpixel.shatteredpixeldungeon.Randomizer;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.AscensionChallenge;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Poison;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.Ratmogrify;
import com.shatteredpixel.shatteredpixeldungeon.items.food.MysteryMeat;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.AcidicSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.RatSprite;
import com.watabou.utils.Bundle;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import java.util.Collections;

public class Rat extends Mob {

	{
		HP = HT = getRandomizerEnabled(RandomTraits.FRAIL_VERMIN) ? 4 : 8;
		defenseSkill = getRandomizerEnabled(RandomTraits.EVASIVE_PESTS) ? 25 : 2;

		maxLvl = 5;

		loot = getRandomizerEnabled(RandomTraits.MEATY_RATS) ? MysteryMeat.class : null;
		lootChance = getRandomizerEnabled(RandomTraits.MEATY_RATS) ? 0.5f : 0.0f;
	}
	@Override
	public Class<? extends CharSprite> GetSpriteClass() {
		return RatSprite.class;
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
		int minDamage = getRandomizerEnabled(RandomTraits.NIBBLING_NUISANCES) ? 0 : 1;
		int maxDamage = getRandomizerEnabled(RandomTraits.NIBBLING_NUISANCES) ? 2 : 4;

		if (isMaxDamage) return maxDamage;
		return Random.NormalIntRange( minDamage, maxDamage );
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

	@Override
	public int attackProc(Char enemy, int damage) {
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
		return super.attackProc(enemy, damage);
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

	public enum RandomTraits {
		ALBINO_INFESTATION, EVASIVE_PESTS, TOXIC_FANGS, MEATY_RATS, NIBBLING_NUISANCES, FRAIL_VERMIN
	}

	public static boolean getRandomizerEnabled(RandomTraits r) {
		switch (r) {
			case ALBINO_INFESTATION: return Randomizer.getCreatureBuff(Rat.class) == 1;
			case EVASIVE_PESTS: return Randomizer.getCreatureBuff(Rat.class) == 2;
			case TOXIC_FANGS: return Randomizer.getCreatureBuff(Rat.class) == 3;
			case MEATY_RATS: return Randomizer.getCreatureNerf(Rat.class) == 1;
			case NIBBLING_NUISANCES: return Randomizer.getCreatureNerf(Rat.class) == 2;
			case FRAIL_VERMIN: return Randomizer.getCreatureNerf(Rat.class) == 3;
		}
		return false;
	}
}
