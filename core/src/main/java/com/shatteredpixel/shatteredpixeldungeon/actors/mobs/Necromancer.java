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
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Adrenaline;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.combat.DamageType;
import com.shatteredpixel.shatteredpixeldungeon.effects.Beam;
import com.shatteredpixel.shatteredpixeldungeon.effects.FloatingText;
import com.shatteredpixel.shatteredpixeldungeon.effects.Pushing;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfTeleportation;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.NecromancerSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.SkeletonSprite;
import com.shatteredpixel.shatteredpixeldungeon.utils.BundleableProperty;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.utils.BArray;
import com.watabou.utils.Bundle;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;
import com.watabou.utils.Reflection;

public class Necromancer extends Mob {
	{
		HUNTING = new Hunting();
	}

	@Override
	public Constants.mobs.mobsBase GetConstants() { return Constants.mobs.necromancer; }

	@Override
	public int GetMaxHP() {
		return super.GetMaxHP() / (getRandomizerEnabled(RandomTraits.FRAIL_FORM) ? 4 : 1);
	}

	private NecroSkeleton mySkeleton;
	private int storedSkeletonID = -1;

	@Override
	protected boolean act() {
		if (m_Summoning.Get() && state != HUNTING){
			m_Summoning.Set(false);
			if (sprite instanceof NecromancerSprite) ((NecromancerSprite) sprite).cancelSummoning();
		}
		return super.act();
	}

	@Override
	public void aggro(Char ch) {
		super.aggro(ch);
		if (mySkeleton != null && mySkeleton.isAlive()
				&& Dungeon.level.mobs.contains(mySkeleton)
				&& mySkeleton.alignment == alignment){
			mySkeleton.aggro(ch);
		}
	}
	
	@Override
	public float GetLootChance(int slot) {
		return super.GetLootChance(slot) * ((6f - Dungeon.LimitedDrops.NECRO_HP.count) / 6f);
	}
	
	@Override
	public Item createLoot(int itemSlot){
		Dungeon.LimitedDrops.NECRO_HP.count++;
		return super.createLoot(itemSlot);
	}
	
	@Override
	public void die(Object cause) {
		if (!getRandomizerEnabled(RandomTraits.PERSISTENT_UNDEAD)) {
			if (storedSkeletonID != -1) {
				Actor ch = Actor.findById(storedSkeletonID);
				storedSkeletonID = -1;
				if (ch instanceof NecroSkeleton) {
					mySkeleton = (NecroSkeleton) ch;
				}
			}

			if (mySkeleton != null && mySkeleton.isAlive() && mySkeleton.alignment == alignment) {
				mySkeleton.die(null);
			}
		}
		super.die(cause);
	}

	@Override
	protected boolean canAttack(Char enemy) {
		return false;
	}

	private static final String MY_SKELETON = "my_skeleton";

	public boolean GetIsSummoning() {
		return m_Summoning.Get();
	}

	public int GetSummoningPosition() {
		return m_SummoningPosition.Get();
	}

	protected BundleableProperty.Bool m_Summoning = new BundleableProperty.Bool("summoning", false);
	protected BundleableProperty.Bool m_FirstSummon = new BundleableProperty.Bool("first_summon", true);
	protected BundleableProperty.Int m_SummoningPosition = new BundleableProperty.Int("summoning_pos", -1);
	
	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		m_Summoning.Store(bundle);
		m_FirstSummon.Store(bundle);
		m_SummoningPosition.Store(bundle);
		if (mySkeleton != null){
			bundle.put( MY_SKELETON, mySkeleton.id() );
		} else if (storedSkeletonID != -1){
			bundle.put( MY_SKELETON, storedSkeletonID );
		}
	}
	
	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		m_Summoning.Restore(bundle);
		m_FirstSummon.Restore(bundle);
		m_SummoningPosition.Restore(bundle);
		if (bundle.contains( MY_SKELETON )){
			storedSkeletonID = bundle.getInt( MY_SKELETON );
		}
	}
	
	public void onZapComplete(){
		if (mySkeleton == null || mySkeleton.sprite == null || !mySkeleton.isAlive()){
			return;
		}
		
		//heal skeleton first
		if (mySkeleton.HP < mySkeleton.GetMaxHP()){

			if (sprite.visible || mySkeleton.sprite.visible) {
				sprite.parent.add(new Beam.HealthRay(sprite.center(), mySkeleton.sprite.center()));
			}
			
			mySkeleton.HP = Math.min(mySkeleton.HP + mySkeleton.GetMaxHP()/5, mySkeleton.GetMaxHP());
			if (mySkeleton.sprite.visible) {
				mySkeleton.sprite.showStatusWithIcon( CharSprite.POSITIVE, Integer.toString( mySkeleton.GetMaxHP()/5 ), FloatingText.HEALING );
			}
			
		//otherwise give it adrenaline
		} else if (mySkeleton.buff(Adrenaline.class) == null) {

			if (!getRandomizerEnabled(RandomTraits.APPRENTICE)) {
				if (sprite.visible || mySkeleton.sprite.visible) {
					sprite.parent.add(new Beam.HealthRay(sprite.center(), mySkeleton.sprite.center()));
				}

				Buff.affect(mySkeleton, Adrenaline.class, 3f);
			}
		}
		
		next();
	}

	public void summonMinion(){
		if (Actor.findChar(m_SummoningPosition.Get()) != null) {

			int pushPos = pos;
			for (int c : PathFinder.NEIGHBOURS8) {
				if (Actor.findChar(m_SummoningPosition.Get() + c) == null
						&& Dungeon.level.passable[m_SummoningPosition.Get() + c]
						&& (Dungeon.level.openSpace[m_SummoningPosition.Get() + c] || !hasProp(Actor.findChar(m_SummoningPosition.Get()), Property.LARGE))
						&& Dungeon.level.trueDistance(pos, m_SummoningPosition.Get() + c) > Dungeon.level.trueDistance(pos, pushPos)) {
					pushPos = m_SummoningPosition.Get() + c;
				}
			}

			//no push if char is immovable
			if (Char.hasProp(Actor.findChar(m_SummoningPosition.Get()), Property.IMMOVABLE)){
				pushPos = pos;
			}

			//push enemy, or wait a turn if there is no valid pushing position
			if (pushPos != pos) {
				Char ch = Actor.findChar(m_SummoningPosition.Get());
				Actor.add( new Pushing( ch, ch.pos, pushPos ) );

				ch.pos = pushPos;
				Dungeon.level.occupyCell(ch );

			} else {

				Char blocker = Actor.findChar(m_SummoningPosition.Get());
				if (blocker.alignment != alignment){
					blocker.Damage( Random.NormalIntRange(2, 10), new SummoningBlockDamage(), DamageType.of(DamageType.BLUDGEONING) );
					if (blocker == Dungeon.hero && !blocker.isAlive()){
						Badges.validateDeathFromEnemyMagic();
						Dungeon.fail(this);
						GLog.n( Messages.capitalize(Messages.get(Char.class, "kill", name(false))) );
					}
				}

				spend(TICK);
				return;
			}
		}

		m_FirstSummon.Set(false);
		m_Summoning.Set(false);

		mySkeleton = new NecroSkeleton();
		mySkeleton.pos = m_SummoningPosition.Get();
		GameScene.add( mySkeleton );
		Dungeon.level.occupyCell( mySkeleton );
		((NecromancerSprite)sprite).finishSummoning();

		for (Buff b : buffs()){
			if (b.revivePersists) {
				Buff.affect(mySkeleton, b.getClass());
			}
		}
	}

	public static class SummoningBlockDamage{}
	
	private static class Hunting extends Mob.Hunting{
		
		@Override
		public boolean act(Mob mob, boolean enemyInFOV, boolean justAlerted) {
			Necromancer n = (Necromancer)mob;
			n.m_EnemySeen.Set(enemyInFOV);

			if (n.m_EnemySeen.Get()){
				n.m_Target.Set(n.enemy.pos);
			}
			
			if (n.storedSkeletonID != -1){
				Actor ch = Actor.findById(n.storedSkeletonID);
				n.storedSkeletonID = -1;
				if (ch instanceof NecroSkeleton){
					n.mySkeleton = (NecroSkeleton) ch;
				}
			}
			
			if (n.m_Summoning.Get()){
				n.summonMinion();
				return true;
			}
			
			if (n.mySkeleton != null &&
					(!n.mySkeleton.isAlive()
					|| !Dungeon.level.mobs.contains(n.mySkeleton)
					|| n.mySkeleton.alignment != n.alignment)){
				n.mySkeleton = null;
			}
			
			//if enemy is seen, and enemy is within range, and we have no skeleton, summon a skeleton!
			if (n.m_EnemySeen.Get() && Dungeon.level.distance(n.pos, n.enemy.pos) <= 4 && n.mySkeleton == null){

				n.m_SummoningPosition.Set(-1);

				//we can summon around blocking terrain, but not through it, except unlocked doors
				boolean[] passable = BArray.not(Dungeon.level.solid, null);
				BArray.or(Dungeon.level.passable, passable, passable);
				PathFinder.buildDistanceMap(n.pos, passable, Dungeon.level.distance(n.pos, n.enemy.pos)+3);

				for (int c : PathFinder.NEIGHBOURS8){
					if (Actor.findChar(n.enemy.pos+c) == null
							&& PathFinder.distance[n.enemy.pos+c] != Integer.MAX_VALUE
							&& Dungeon.level.passable[n.enemy.pos+c]
							&& (!hasProp(n, Property.LARGE) || Dungeon.level.openSpace[n.enemy.pos+c])
							&& n.fieldOfView[n.enemy.pos+c]
							&& Dungeon.level.trueDistance(n.pos, n.enemy.pos+c) < Dungeon.level.trueDistance(n.pos, n.m_SummoningPosition.Get())){
						n.m_SummoningPosition.Set(n.enemy.pos+c);
					}
				}
				
				if (n.m_SummoningPosition.Get() != -1){

					n.m_Summoning.Set(true);
					if (getRandomizerEnabled(RandomTraits.BONE_ARMY)) {
						n.sprite.zap(n.m_SummoningPosition.Get());
						n.summonMinion();
					} else {
						n.sprite.zap(n.m_SummoningPosition.Get());

						if (Dungeon.level.heroFOV[n.pos] || Dungeon.level.heroFOV[n.m_SummoningPosition.Get()]) {
							Dungeon.hero.interrupt();
						}

						n.spend(n.m_FirstSummon.Get() ? TICK : 2 * TICK);
					}
				} else {
					//wait for a turn
					n.spend(TICK);
				}
				
				return true;
			//otherwise, if enemy is seen, and we have a skeleton...
			} else if (n.m_EnemySeen.Get() && n.mySkeleton != null){

				n.spend(TICK);
				
				if (!n.fieldOfView[n.mySkeleton.pos]){
					
					//if the skeleton is not next to the enemy
					//teleport them to the closest spot next to the enemy that can be seen
					if (!Dungeon.level.adjacent(n.mySkeleton.pos, n.enemy.pos)){
						int telePos = -1;
						for (int c : PathFinder.NEIGHBOURS8){
							if (Actor.findChar(n.enemy.pos+c) == null
									&& Dungeon.level.passable[n.enemy.pos+c]
									&& n.fieldOfView[n.enemy.pos+c]
									&& (Dungeon.level.openSpace[n.enemy.pos+c] || !Char.hasProp(n.mySkeleton, Property.LARGE))
									&& Dungeon.level.trueDistance(n.pos, n.enemy.pos+c) < Dungeon.level.trueDistance(n.pos, telePos)){
								telePos = n.enemy.pos+c;
							}
						}
						
						if (telePos != -1){
							
							ScrollOfTeleportation.appear(n.mySkeleton, telePos);
							n.mySkeleton.teleportSpend();
							
							if (n.sprite != null && n.sprite.visible){
								n.sprite.zap(telePos);
								return false;
							} else {
								n.onZapComplete();
							}
						}
					}
					
					return true;
					
				} else {
					//zap skeleton
					boolean shouldZapSkeleton;
					if (getRandomizerEnabled(RandomTraits.APPRENTICE)) {
						shouldZapSkeleton = n.mySkeleton.HP < n.mySkeleton.GetMaxHP();
					} else {
						shouldZapSkeleton = n.mySkeleton.HP < n.mySkeleton.GetMaxHP() || n.mySkeleton.buff(Adrenaline.class) == null;
					}

					if (shouldZapSkeleton) {
						if (n.sprite != null && n.sprite.visible){
							n.sprite.zap(n.mySkeleton.pos);
							return false;
						} else {
							n.onZapComplete();
						}
					}
					else if (getRandomizerEnabled(RandomTraits.COWARDLY) && n.fieldOfView[Dungeon.hero.pos]) {
						int oldPos = n.pos;
						n.getFurther(Dungeon.hero.pos);
						n.moveSprite( oldPos, n.pos );
					}
				}
				
				return true;
				
			//otherwise, default to regular hunting behaviour
			} else {
				return super.act(n, enemyInFOV, justAlerted);
			}
		}
	}
	
	public static class NecroSkeleton extends Skeleton {
		
		{
			state = WANDERING;
		}

		@Override
		protected void onAdd(){
			boolean previousFirstAdded = firstAdded;
			super.onAdd();
			if (previousFirstAdded) {
				HP = (int) (GetMaxHP() * 0.8f);
			}
		}

		@Override
		public CharSprite sprite() {
			return Reflection.newInstance(NecroSkeletonSprite.class);
		}

		@Override
		public int GetMaxLevel() {
			return -5;
		}

		@Override
		public float spawningWeight() {
			return 0;
		}

		private void teleportSpend(){
			spend(TICK);
		}
		
		public static class NecroSkeletonSprite extends SkeletonSprite{
			
			public NecroSkeletonSprite(){
				super();
				brightness(0.75f);
			}
			
			@Override
			public void resetColor() {
				super.resetColor();
				brightness(0.75f);
			}
		}
		
	}

	public enum RandomTraits {
		BONE_ARMY, COWARDLY, PERSISTENT_UNDEAD, SHAMBLING_BONES, FRAIL_FORM, APPRENTICE
	}

	public static boolean getRandomizerEnabled(RandomTraits r) {
		switch (r) {
			case BONE_ARMY: return Randomizer.getCreatureBuff(Necromancer.class) == 1;
			case COWARDLY: return Randomizer.getCreatureBuff(Necromancer.class) == 2;
			case PERSISTENT_UNDEAD: return Randomizer.getCreatureBuff(Necromancer.class) == 3;
			case SHAMBLING_BONES: return Randomizer.getCreatureNerf(Necromancer.class) == 1;
			case FRAIL_FORM: return Randomizer.getCreatureNerf(Necromancer.class) == 2;
			case APPRENTICE: return Randomizer.getCreatureNerf(Necromancer.class) == 3;
		}
		return false;
	}
}