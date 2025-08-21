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
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Poison;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.Ratmogrify;
import com.shatteredpixel.shatteredpixeldungeon.items.food.MysteryMeat;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.RatSprite;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

public class Rat extends Mob {
	@Override
	public Constants.mobs.mobsBase GetConstants() { return Constants.mobs.rat; }

	@Override
	public int GetMaxHP() {
		return super.GetMaxHP() / (getRandomizerEnabled(RandomTraits.FRAIL_VERMIN) ? 2 : 1);
	}

	@Override
	public int defenseSkill(Char enemy) {
		return super.defenseSkill(enemy) * (getRandomizerEnabled(RandomTraits.EVASIVE_PESTS) ? 5 : 1);
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
	public int minDamage(AttackType type) {
		if (type == AttackType.MELEE && getRandomizerEnabled(RandomTraits.NIBBLING_NUISANCES)) {
			return super.minDamage(type) / 2;
		}
		return super.minDamage(type);
	}

	@Override
	public int maxDamage(AttackType type) {
		if (type == AttackType.MELEE && getRandomizerEnabled(RandomTraits.NIBBLING_NUISANCES)) {
			return super.maxDamage(type) / 2;
		}
		return super.maxDamage(type);
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

	@Override
	public void rollToDropLoot() {
		super.rollToDropLoot();
		if (Dungeon.hero.lvl > GetMaxLevel() + 2) return;

		if (getRandomizerEnabled(RandomTraits.MEATY_RATS) && Random.Float() > 0.5f) {
			Dungeon.level.drop(new MysteryMeat(), pos).sprite.drop();
		}
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
