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
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MagicImmune;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.Imp;
import com.shatteredpixel.shatteredpixeldungeon.combat.DamageType;
import com.shatteredpixel.shatteredpixeldungeon.effects.FloatingText;
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfTeleportation;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.GolemSprite;
import com.shatteredpixel.shatteredpixeldungeon.utils.BundleableProperty;
import com.watabou.utils.BArray;
import com.watabou.utils.Bundle;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import java.util.EnumSet;

public class Golem extends Mob {
	
	{
		WANDERING = new Wandering();
		HUNTING = new Hunting();
	}

	@Override
	public Constants.mobs.mobsBase GetConstants() { return Constants.mobs.golem; }

	@Override
	protected void onAdd(){
		boolean previousFirstAdded = firstAdded;
		super.onAdd();
		if (previousFirstAdded && getRandomizerEnabled(RandomTraits.BATTLE_WORN)) {
			// 50%-100% health
			float multiplier = Random.Float(0.5f, 1.0f);
			HP = (int) (GetMaxHP() * multiplier);
		}
	}

	@Override
	public float attackDelay() {
		if (getRandomizerEnabled(RandomTraits.DOUBLE_STRIKE)) {
			return super.attackDelay() * 0.5f;
		}
		return super.attackDelay();
	}

	@Override
	protected int getMinDR(EnumSet<DamageType> damageType) {
		if (getRandomizerEnabled(RandomTraits.IMMUNITY)) {
			return 6;
		}
		return super.getMinDR(damageType);
	}

	@Override
	public float GetLootChance(int slot) {
		//each drop makes future drops 1/3 as likely
		// so loot chance looks like: 1/5, 1/15, 1/45, 1/135, etc.
		return super.GetLootChance(slot) * (float)Math.pow(1/3f, Dungeon.LimitedDrops.GOLEM_EQUIP.count);
	}

	@Override
	public void rollToDropLoot() {
		Imp.Quest.process( this );
		super.rollToDropLoot();
	}

	public Item createLoot(int slot) {
		Dungeon.LimitedDrops.GOLEM_EQUIP.count++;
		Object loot = null;

		switch(slot) {
			case 0:
				loot = GetConstants().getLoot().getLoot1();
				break;
			case 1:
				loot = GetConstants().getLoot().getLoot2();
				break;
			case 2:
				loot = GetConstants().getLoot().getLoot3();
				break;
		}
		//uses probability tables for demon halls
		if (loot == Generator.Category.WEAPON){
			return Generator.randomWeapon(5, true);
		} else {
			return Generator.randomArmor(5);
		}
	}

	private BundleableProperty.Bool m_Teleporting = new BundleableProperty.Bool("teleporting", false);
	private BundleableProperty.Int m_SelfTeleportingCooldown = new BundleableProperty.Int("self_cooldown", 0);
	private BundleableProperty.Int m_EnemyTeleportingCooldown = new BundleableProperty.Int("enemy_cooldown", 0);

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		m_Teleporting.Store(bundle);
		m_SelfTeleportingCooldown.Store(bundle);
		m_EnemyTeleportingCooldown.Store(bundle);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		m_Teleporting.Restore(bundle);
		m_SelfTeleportingCooldown.Restore(bundle);
		m_EnemyTeleportingCooldown.Restore(bundle);
	}

	@Override
	protected boolean act() {
		m_SelfTeleportingCooldown.Decrement();
		m_EnemyTeleportingCooldown.Decrement();
		if (getRandomizerEnabled(RandomTraits.SPATIAL_LOCK)) {
			m_SelfTeleportingCooldown.Set(1000);
		}
		if (getRandomizerEnabled(RandomTraits.PROJECTILE_BLOCKING)) {
			m_EnemyTeleportingCooldown.Set(1000);
		}
		if (m_Teleporting.Get()) {
			((GolemSprite)sprite).teleParticles(false);
			if (Actor.findChar(m_Target.Get()) == null && Dungeon.level.openSpace[m_Target.Get()]) {
				ScrollOfTeleportation.appear(this, m_Target.Get());
				m_SelfTeleportingCooldown.Set(30);
			} else {
				m_Target.Set(Dungeon.level.randomDestination(this));
			}
			m_Teleporting.Set(false);
			spend(TICK);
			return true;
		}
		return super.act();
	}

	public void onZapComplete(){
		teleportEnemy();
		next();
	}

	public void teleportEnemy(){
		spend(TICK);

		int bestPos = enemy.pos;
		for (int i : PathFinder.NEIGHBOURS8){
			if (Dungeon.level.passable[pos + i]
				&& Actor.findChar(pos+i) == null
				&& Dungeon.level.trueDistance(pos+i, enemy.pos) > Dungeon.level.trueDistance(bestPos, enemy.pos)){
				bestPos = pos+i;
			}
		}

		if (enemy.buff(MagicImmune.class) != null){
			bestPos = enemy.pos;
		}

		if (bestPos != enemy.pos){
			ScrollOfTeleportation.appear(enemy, bestPos);
			if (enemy instanceof Hero){
				((Hero) enemy).interrupt();
				Dungeon.observe();
				GameScene.updateFog();
			}
		}

		m_EnemyTeleportingCooldown.Set(20);
	}

	private boolean canTele(int target){
		if (m_EnemyTeleportingCooldown.Get() > 0) return false;
		PathFinder.buildDistanceMap(target, BArray.not(Dungeon.level.solid, null), Dungeon.level.distance(pos, target)+1);
		//zaps can go around blocking terrain, but not through it
		if (PathFinder.distance[pos] == Integer.MAX_VALUE){
			return false;
		}
		return true;
	}

	@Override
	//Always spends exactly the specified amount of time, regardless of time-influencing factors
	protected void spendConstant( float time ){
		int oldTime = (int)this.getTime(); // cut it off
		super.spendConstant(time);
		if (HP > 0 && HP < GetMaxHP() && getRandomizerEnabled(RandomTraits.RAPID_REGENERATION) && (int)this.getTime() > oldTime) { // we go up one turn
			int oldHP = HP;
			HP = Math.min(GetMaxHP(), HP + 2);
			sprite.showStatusWithIcon(CharSprite.POSITIVE, Integer.toString(HP - oldHP), FloatingText.HEALING);
		}
	}

	private static class Wandering extends Mob.Wandering{

		@Override
		protected boolean continueWandering(Mob mob) {
			mob.m_EnemySeen.Set(false);

			int oldPos = mob.pos;
			if (mob.m_Target.Get() != -1 && mob.getCloser( mob.m_Target.Get() )) {
				mob.spend( 1 / mob.speed() );
				return mob.moveSprite( oldPos, mob.pos );
			} else if (!Dungeon.bossLevel() && mob.m_Target.Get() != -1 && mob.m_Target.Get() != mob.pos && ((Golem)mob).m_SelfTeleportingCooldown.Get() <= 0) {
				((GolemSprite)mob.sprite).teleParticles(true);
				((Golem)mob).m_Teleporting.Set(true);
				mob.spend( 2*TICK );
			} else {
				mob.m_Target.Set(randomDestination(mob));
				mob.spend( TICK );
			}

			return true;
		}
	}

	private static class Hunting extends Mob.Hunting{

		@Override
		public boolean act(Mob mob, boolean enemyInFOV, boolean justAlerted) {
			if (!enemyInFOV || mob.canAttack(mob.enemy)) {
				return super.act(mob, enemyInFOV, justAlerted);
			} else {
				mob.m_EnemySeen.Set(true);
				mob.m_Target.Set(mob.enemy.pos);

				int oldPos = mob.pos;

				if (mob.distance(mob.enemy) >= 1 && Random.Int(100/mob.distance(mob.enemy)) == 0
						&& !Char.hasProp(mob.enemy, Property.IMMOVABLE) && ((Golem)mob).canTele(mob.m_Target.Get())){
					if (mob.sprite != null && (mob.sprite.visible || mob.enemy.sprite.visible)) {
						mob.sprite.zap( mob.enemy.pos );
						return false;
					} else {
						((Golem)mob).teleportEnemy();
						return true;
					}

				} else if (mob.getCloser( mob.m_Target.Get() )) {
					mob.spend( 1 / mob.speed() );
					return mob.moveSprite( oldPos,  mob.pos );

				} else if (!Char.hasProp(mob.enemy, Property.IMMOVABLE) && ((Golem)mob).canTele(mob.m_Target.Get())) {
					if (mob.sprite != null && (mob.sprite.visible || mob.enemy.sprite.visible)) {
						mob.sprite.zap( mob.enemy.pos );
						return false;
					} else {
						((Golem)mob).teleportEnemy();
						return true;
					}

				} else {
					mob.spend( TICK );
					return true;
				}

			}
		}
	}

	public enum RandomTraits {
		RAPID_REGENERATION, IMMUNITY, DOUBLE_STRIKE, SPATIAL_LOCK, PROJECTILE_BLOCKING, BATTLE_WORN
	}

	public static boolean getRandomizerEnabled(RandomTraits r) {
		switch (r) {
			case RAPID_REGENERATION: return Randomizer.getCreatureBuff(Golem.class) == 1;
			case IMMUNITY: return Randomizer.getCreatureBuff(Golem.class) == 2;
			case DOUBLE_STRIKE: return Randomizer.getCreatureBuff(Golem.class) == 3;
			case SPATIAL_LOCK: return Randomizer.getCreatureNerf(Golem.class) == 1;
			case PROJECTILE_BLOCKING: return Randomizer.getCreatureNerf(Golem.class) == 2;
			case BATTLE_WORN: return Randomizer.getCreatureNerf(Golem.class) == 3;
		}
		return false;
	}
}