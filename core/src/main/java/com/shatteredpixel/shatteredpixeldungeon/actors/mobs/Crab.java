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
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.food.MysteryMeat;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CrabSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.RatSprite;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

public class Crab extends Mob {

	{
		HP = HT = 15;
		defenseSkill = 5;
		baseSpeed = getRandomizerEnabled(RandomTraits.LIGHTNING_LEGS) ? 3.5f : 2f;
		
		EXP = 4;
		maxLvl = 9;
		
		loot = MysteryMeat.class;
		lootChance = 0.167f;
	}

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
	public Class<? extends CharSprite> GetSpriteClass() {

		return CrabSprite.class;
	}
	
	@Override
	public int damageRoll(boolean isMaxDamage) {
		if (isMaxDamage) return 7;
		return Random.NormalIntRange( 1, 7 );
	}

	@Override
	public void rollToDropLoot() {
		super.rollToDropLoot();

		if (getRandomizerEnabled(RandomTraits.HERMIT_TREASURES) && Dungeon.hero.lvl <= maxLvl + 2 && Random.Float() > 0.5f){
			Dungeon.level.drop(Generator.randomArmor(), pos).sprite.drop();
		}
	}
	
	@Override
	public int attackSkill( Char target ) {
		if (getRandomizerEnabled(RandomTraits.CLUMSY_CLAWS)) {
			return 8;
		}
		return 12;
	}
	
	@Override
	public int drRoll() {
		if (getRandomizerEnabled(RandomTraits.MOLTING_SEASON)) {
			return 0;
		}
		return super.drRoll() + Random.NormalIntRange(0, 4);
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