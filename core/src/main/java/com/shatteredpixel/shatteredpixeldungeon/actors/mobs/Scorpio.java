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
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Cripple;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Light;
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.Potion;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfExperience;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfHealing;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfStrength;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.RatSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ScorpioSprite;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.utils.Bundle;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Point;
import com.watabou.utils.Random;
import com.watabou.utils.Reflection;

public class Scorpio extends Mob {
	
	{
		HP = HT = getRandomizerEnabled(RandomTraits.BRITTLE_SHELLS) ? 40 : 120;
		defenseSkill = 24;
		viewDistance = Light.DISTANCE - 1;

		if (getRandomizerEnabled(RandomTraits.LIGHTNING_FAST)) {
			baseSpeed = 2.0f;
		} else if (getRandomizerEnabled(RandomTraits.SLUGGISH_CRAWL)) {
			baseSpeed = 0.25f;
		}
		
		EXP = 14;
		maxLvl = 27;
		
		loot = Generator.Category.POTION;
		lootChance = 0.5f;

		properties.add(Property.DEMONIC);

		WANDERING = new Wandering();
	}
	@Override
	public Class<? extends CharSprite> GetSpriteClass() {

		return ScorpioSprite.class;
	}

	private int lastEnemyPosition = -1;

	private static final String LAST_ENEMY_POSITION     = "last_enemy_position";

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put( LAST_ENEMY_POSITION, lastEnemyPosition);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		lastEnemyPosition = bundle.getInt(LAST_ENEMY_POSITION);
	}
	@Override
	protected boolean act() {
		if (enemy != null && fieldOfView[enemy.pos]) {
			lastEnemyPosition = enemy.pos;
		}
		return super.act();
	}

	@Override
	public int damageRoll(boolean isMaxDamage) {
		if (isMaxDamage) return 40;
		return Random.NormalIntRange( 30, 40 );
	}
	
	@Override
	public int attackSkill( Char target ) {
		return 36;
	}
	
	@Override
	public int drRoll() {
		return super.drRoll() + Random.NormalIntRange(0, 16);
	}
	
	@Override
	protected boolean canAttack( Char enemy ) {
		return !Dungeon.level.adjacent( pos, enemy.pos )
				&& (super.canAttack(enemy) || new Ballistica( pos, enemy.pos, Ballistica.PROJECTILE).collisionPos == enemy.pos);
	}
	
	@Override
	public int attackProc( Char enemy, int damage ) {
		damage = super.attackProc( enemy, damage );
		if (Random.Int( 2 ) == 0) {
			Buff.prolong( enemy, Cripple.class, Cripple.DURATION );
		}
		
		return damage;
	}
	
	@Override
	protected boolean getCloser( int target ) {
		if (state == HUNTING) {
			return enemySeen && getFurther( target );
		} else {
			return super.getCloser( target );
		}
	}
	
	@Override
	public void aggro(Char ch) {
		//cannot be aggroed to something it can't see
		//skip this check if FOV isn't initialized
		if (ch == null || fieldOfView == null
				|| fieldOfView.length != Dungeon.level.length() || fieldOfView[ch.pos]) {
			super.aggro(ch);
		}
	}

	public class Wandering extends Mob.Wandering {
		@Override
		protected int randomDestination() {
			if (!getRandomizerEnabled(RandomTraits.TERRITORIAL_HUNTERS) || lastEnemyPosition == -1) {
				return super.randomDestination();
			}

			int len = Dungeon.level.length();
			boolean[] p = Dungeon.level.passable;
			boolean[] v = Dungeon.level.visited;
			boolean[] m = Dungeon.level.mapped;
			boolean[] passable = new boolean[len];
			for (int i = 0; i < len; i++) {
				passable[i] = p[i] && (v[i] || m[i]);
			}

			int destination;
			Point lastSeenEnemyPosition = Dungeon.level.cellToPoint(lastEnemyPosition);
			boolean validPath = false;
			int tries = 0;
			do {
				destination = super.randomDestination();
				validPath = true;
				PathFinder.Path newpath = Dungeon.findPath(Scorpio.this, destination, passable, fieldOfView, true);
				for (int step : newpath) {
					Point currentStepPosition = Dungeon.level.cellToPoint(step);
					if ((currentStepPosition.x - lastSeenEnemyPosition.x) * (currentStepPosition.x - lastSeenEnemyPosition.x) + (currentStepPosition.y - lastSeenEnemyPosition.y) * (currentStepPosition.y - lastSeenEnemyPosition.y) < 3) {
						validPath = false;
						break;
					}
				}

			} while (++tries < 100 && !validPath);

			return destination;
		}
	}

	@Override
	public Item createLoot() {
		if (getRandomizerEnabled(RandomTraits.ACIDIC_CARRIERS) && Random.Int(10) == 0) {
			return new PotionOfExperience();
		}

		Class<?extends Potion> loot;
		do{
			loot = (Class<? extends Potion>) Random.oneOf(Generator.Category.POTION.classes);
		} while (loot == PotionOfHealing.class || loot == PotionOfStrength.class);

		return Reflection.newInstance(loot);
	}

	public enum RandomTraits {
		ACIDIC_INFESTATION, LIGHTNING_FAST, TERRITORIAL_HUNTERS, SLUGGISH_CRAWL, ACIDIC_CARRIERS, BRITTLE_SHELLS
	}

	public static boolean getRandomizerEnabled(RandomTraits r) {
		switch (r) {
			case ACIDIC_INFESTATION: return Randomizer.getCreatureBuff(Scorpio.class) == 1;
			case LIGHTNING_FAST: return Randomizer.getCreatureBuff(Scorpio.class) == 2;
			case TERRITORIAL_HUNTERS: return Randomizer.getCreatureBuff(Scorpio.class) == 3;
			case SLUGGISH_CRAWL: return Randomizer.getCreatureNerf(Scorpio.class) == 1;
			case ACIDIC_CARRIERS: return Randomizer.getCreatureNerf(Scorpio.class) == 2;
			case BRITTLE_SHELLS: return Randomizer.getCreatureNerf(Scorpio.class) == 3;
		}
		return false;
	}
}