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
import com.shatteredpixel.shatteredpixeldungeon.Randomizer;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.AscensionChallenge;
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.items.food.MysteryMeat;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CrabSprite;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

public class Crab extends Mob {
	private boolean movedLastTurn = false;

	private static final String MOVED_LAST_TURN = "moved_last_turn";

	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
		bundle.put( MOVED_LAST_TURN, movedLastTurn );
	}

	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle( bundle );
		movedLastTurn = bundle.getBoolean( MOVED_LAST_TURN );
	}

	@Override
	public Constants.mobs.mobsBase GetConstants() { return Constants.mobs.crab; }

	@Override
	public void rollToDropLoot() {
		super.rollToDropLoot();

		if (getRandomizerEnabled(RandomTraits.HERMIT_TREASURES) && Dungeon.hero.lvl <= GetMaxLevel() + 2 && Random.Float() > 0.5f){
			Dungeon.level.drop(Generator.randomArmor(), pos).sprite.drop();
		}
	}

	@Override
	public float speed() {
		return super.speed() * (getRandomizerEnabled(RandomTraits.LIGHTNING_LEGS) ? 1.75f : 1f);
	}
	
	@Override
	public int attackSkill( Char target ) {
		int skill = super.attackSkill(target);
		if (getRandomizerEnabled(RandomTraits.CLUMSY_CLAWS)) {
			skill *= 2;
			skill /= 3;
		}
		return skill;
	}
	
	@Override
	public int drRoll() {
		if (getRandomizerEnabled(RandomTraits.MOLTING_SEASON)) {
			return 0;
		}
		return super.drRoll();
	}

	@Override
	public float attackDelay() {
		if (!movedLastTurn && getRandomizerEnabled(RandomTraits.FLURRY_CLAWS)) {
			return super.attackDelay() * 0.5f;
		}
		return super.attackDelay();
	}

	@Override
	protected boolean act() {
		movedLastTurn = false;
		return super.act();
	}

	@Override
	public void move( int step, boolean travelling ) {
		int oldPos = pos;
		super.move(step, travelling);
		movedLastTurn = oldPos != pos;
	}

	public enum RandomTraits {
		HERMIT_INVASION, FLURRY_CLAWS, LIGHTNING_LEGS, HERMIT_TREASURES, CLUMSY_CLAWS, MOLTING_SEASON
	}

	public static boolean getRandomizerEnabled(RandomTraits r) {
		switch (r) {
			case HERMIT_INVASION: return Randomizer.getCreatureBuff(Crab.class) == 1;
			case FLURRY_CLAWS: return Randomizer.getCreatureBuff(Crab.class) == 2;
			case LIGHTNING_LEGS: return Randomizer.getCreatureBuff(Crab.class) == 3;
			case HERMIT_TREASURES: return Randomizer.getCreatureNerf(Crab.class) == 1;
			case CLUMSY_CLAWS: return Randomizer.getCreatureNerf(Crab.class) == 2;
			case MOLTING_SEASON: return Randomizer.getCreatureNerf(Crab.class) == 3;
		}
		return false;
	}
}