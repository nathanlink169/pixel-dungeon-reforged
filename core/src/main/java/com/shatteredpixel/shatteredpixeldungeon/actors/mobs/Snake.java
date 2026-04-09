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

import com.shatteredpixel.shatteredpixeldungeon.Badges;
import com.shatteredpixel.shatteredpixeldungeon.Constants;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.Randomizer;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Amok;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.AscensionChallenge;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Charm;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Poison;
import com.shatteredpixel.shatteredpixeldungeon.combat.AttackContext;
import com.shatteredpixel.shatteredpixeldungeon.combat.CombatModifier;
import com.shatteredpixel.shatteredpixeldungeon.combat.DamageType;
import com.shatteredpixel.shatteredpixeldungeon.journal.Document;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.watabou.utils.Random;

import java.util.EnumSet;
import java.util.HashSet;

public class Snake extends Mob implements CombatModifier.OnDamageEffect {
	@Override
	public Constants.mobs.mobsBase GetConstants() { return Constants.mobs.snake; }

	@Override
	public int GetMaxHP() {
		if (getRandomizerEnabled(RandomTraits.PAPER_THIN)) return 1;
		return super.GetMaxHP();
	}

	@Override
	protected HashSet<Char> getPotentialAttackTargets() {
		if (!getRandomizerEnabled(RandomTraits.RAT_HUNTERS)) {
			return super.getPotentialAttackTargets();
		}
		// exact same as super, but we want to also target rats
		HashSet<Char> enemies = new HashSet<>();

		//if we are amoked...
		if ( buff(Amok.class) != null) {
			//try to find an enemy mob to attack first.
			for (Mob mob : Dungeon.level.mobs)
				if (mob.alignment == Alignment.ENEMY && mob != this
						&& fieldOfView[mob.pos] && mob.invisible <= 0) {
					enemies.add(mob);
				}

			if (enemies.isEmpty()) {
				//try to find ally mobs to attack second.
				for (Mob mob : Dungeon.level.mobs)
					if (mob.alignment == Alignment.ALLY && mob != this
							&& fieldOfView[mob.pos] && mob.invisible <= 0) {
						enemies.add(mob);
					}

				if (enemies.isEmpty()) {
					//try to find the hero third
					if (fieldOfView[Dungeon.hero.pos] && Dungeon.hero.invisible <= 0) {
						enemies.add(Dungeon.hero);
					}
				}
			}

			//if we are an ally...
		} else if ( alignment == Alignment.ALLY ) {
			//look for hostile mobs to attack
			for (Mob mob : Dungeon.level.mobs)
				if (mob.alignment == Alignment.ENEMY && fieldOfView[mob.pos]
						&& mob.invisible <= 0 && !mob.isInvulnerable(getClass()))
					//do not target passive mobs
					//intelligent allies also don't target mobs which are wandering or asleep
					if (mob.state != mob.PASSIVE &&
							(!intelligentAlly || (mob.state != mob.SLEEPING && mob.state != mob.WANDERING))) {
						enemies.add(mob);
					}

			//if we are an enemy...
		} else if (alignment == Alignment.ENEMY) {
			//look for ally mobs to attack
			for (Mob mob : Dungeon.level.mobs)
				if ((mob.alignment == Alignment.ALLY && fieldOfView[mob.pos] && mob.invisible <= 0) ||
					(mob instanceof Rat && fieldOfView[mob.pos] && mob.invisible <= 0))
					enemies.add(mob);

			//and look for the hero
			if (fieldOfView[Dungeon.hero.pos] && Dungeon.hero.invisible <= 0) {
				enemies.add(Dungeon.hero);
			}

		}

		//do not target anything that's charming us
		Charm charm = buff( Charm.class );
		if (charm != null){
			Char source = (Char) Actor.findById( charm.object );
			if (source != null && enemies.contains(source) && enemies.size() > 1){
				enemies.remove(source);
			}
		}
		return enemies;
	}

	@Override

	public int drRoll(EnumSet<DamageType> damageType) {
		int dr = super.drRoll(damageType);

		if(getRandomizerEnabled(RandomTraits.HARDENED_HIDE)) {
			dr += 2;
		}

		return dr;
	}

	@Override
	public boolean surprisedBy( Char enemy, boolean attacking ) {
		boolean surprised = super.surprisedBy(enemy, attacking);

		if (getRandomizerEnabled(RandomTraits.PHANTOM_SCALES)) {
			if (surprised) {
				if (Math.random() > 0.5f) {
					surprised = false;
				}
			}
		}

		return surprised;
	}

	
	@Override
	public int attackSkill() {
		return 10;
	}

	private static int dodges = 0;

	@Override
	public String defenseVerb() {
		if (Dungeon.level.heroFOV[pos]) {
		dodges++;
		}
		if ((dodges >= 2 && !Document.ADVENTURERS_GUIDE.isPageRead(Document.GUIDE_SURPRISE_ATKS))
				|| (dodges >= 4 && !Badges.isUnlocked(Badges.Badge.BOSS_SLAIN_GOO))){
			GameScene.flashForDocument(Document.ADVENTURERS_GUIDE, Document.GUIDE_SURPRISE_ATKS);
			dodges = 0;
		}
		return super.defenseVerb();
	}

	@Override
	public void onDamage(AttackContext context, int damageDealt) {
		if (getRandomizerEnabled(RandomTraits.VENOMOUS_BITE)) {
			if (Random.Int(3) == 0) {
				int duration = Random.IntRange(1, 3);
				if (Math.random() > 0.8f) {
					++duration; // really rare chance to get 4 turns
				}
				//we only use half the ascension modifier here as total poison dmg doesn't scale linearly
				duration = Math.round(duration * (AscensionChallenge.statModifier(this) / 2f + 0.5f));
				Buff.affect(enemy, Poison.class).set(duration);
				state = FLEEING;
			}
		}
	}

	@Override
	public float GetLootChance(int slot){
		if (getRandomizerEnabled(RandomTraits.SEED_HOARDERS)) {
			return 1;
		}
		return super.GetLootChance(slot);
	}

	@Override
	public int priority() {
		return Priority.NORMAL;
	}

	@Override
	public boolean appliesTo(AttackContext context) {
		return context.attacker == this;
	}

	public enum RandomTraits {
		VENOMOUS_BITE, PHANTOM_SCALES, HARDENED_HIDE, PAPER_THIN, SEED_HOARDERS, RAT_HUNTERS
	}

	public static boolean getRandomizerEnabled(RandomTraits r) {
		switch (r) {
			case VENOMOUS_BITE: return Randomizer.getCreatureBuff(Snake.class) == 1;
			case PHANTOM_SCALES: return Randomizer.getCreatureBuff(Snake.class) == 2;
			case HARDENED_HIDE: return Randomizer.getCreatureBuff(Snake.class) == 3;
			case PAPER_THIN: return Randomizer.getCreatureNerf(Snake.class) == 1;
			case SEED_HOARDERS: return Randomizer.getCreatureNerf(Snake.class) == 2;
			case RAT_HUNTERS: return Randomizer.getCreatureNerf(Snake.class) == 3;
		}
		return false;
	}
}
