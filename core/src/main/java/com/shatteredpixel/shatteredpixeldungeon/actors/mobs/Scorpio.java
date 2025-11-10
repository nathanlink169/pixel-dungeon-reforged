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
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Cripple;
import com.shatteredpixel.shatteredpixeldungeon.combat.AttackContext;
import com.shatteredpixel.shatteredpixeldungeon.combat.CombatModifier;
import com.shatteredpixel.shatteredpixeldungeon.combat.DamageType;
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.Potion;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfExperience;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfHealing;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfStrength;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.utils.BundleableProperty;
import com.watabou.utils.Bundle;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Point;
import com.watabou.utils.Random;
import com.watabou.utils.Reflection;

public class Scorpio extends Mob implements CombatModifier.OnDamageEffect {
	
	{
		WANDERING = new Wandering();
	}

	@Override
	public Constants.mobs.mobsBase GetConstants() { return Constants.mobs.scorpio; }

	@Override
	public int GetMaxHP() {
		return super.GetMaxHP() / (getRandomizerEnabled(RandomTraits.BRITTLE_SHELLS) ? 3 : 1);
	}

	@Override
	public float speed() {
		float speed = super.speed();
		if (getRandomizerEnabled(RandomTraits.LIGHTNING_FAST)) {
			speed *= 2.0f;
		} else if (getRandomizerEnabled(RandomTraits.SLUGGISH_CRAWL)) {
			speed *= 0.25f;
		}
		return speed;
	}

	private BundleableProperty.Int m_LastEnemyPosition = new BundleableProperty.Int("last_enemy_position", -1);

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		m_LastEnemyPosition.Store(bundle);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		m_LastEnemyPosition.Restore(bundle);
	}
	@Override
	protected boolean act() {
		if (enemy != null && fieldOfView != null && fieldOfView[enemy.pos]) {
			m_LastEnemyPosition.Set(enemy.pos);
		}
		return super.act();
	}
	
	@Override
	protected boolean canAttack( Char enemy ) {
		return !Dungeon.level.adjacent( pos, enemy.pos )
				&& (super.canAttack(enemy) || new Ballistica( pos, enemy.pos, Ballistica.PROJECTILE).collisionPos == enemy.pos);
	}
	
	@Override
    public boolean getCloser(int target) {
		if (state == HUNTING) {
			return m_EnemySeen.Get() && getFurther( target );
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

	@Override
	public void onDamage(AttackContext context, int damageDealt) {
		if (Random.Int( 2 ) == 0) {
			Buff.prolong( enemy, Cripple.class, Cripple.DURATION );
		}
	}

	@Override
	public int priority() {
		return Priority.NORMAL;
	}

	@Override
	public boolean appliesTo(AttackContext context) {
		return context.attacker == this;
	}

	public static class Wandering extends Mob.Wandering {
		@Override
		protected int randomDestination(Mob mob) {
			Scorpio s = (Scorpio) mob;
			if (!getRandomizerEnabled(RandomTraits.TERRITORIAL_HUNTERS) || s.m_LastEnemyPosition.Get() == -1) {
				return super.randomDestination(mob);
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
			Point lastSeenEnemyPosition = Dungeon.level.cellToPoint(s.m_LastEnemyPosition.Get());
			boolean validPath = false;
			int tries = 0;
			do {
				destination = super.randomDestination(s);
				validPath = true;
				PathFinder.Path newpath = Dungeon.findPath(s, destination, passable, s.fieldOfView, true);
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
	public Item createLoot(int itemSlot) {
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