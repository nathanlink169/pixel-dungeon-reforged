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
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.SacrificialFire;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Vulnerable;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Weakness;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.duelist.Challenge;
import com.shatteredpixel.shatteredpixeldungeon.effects.FloatingText;
import com.shatteredpixel.shatteredpixeldungeon.effects.Pushing;
import com.shatteredpixel.shatteredpixeldungeon.items.Gold;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.Chasm;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.GhoulSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.RatSprite;
import com.watabou.utils.Bundle;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class Ghoul extends Mob {
	
	{
		HP = HT = 45;
		defenseSkill = 20;
		
		EXP = 5;
		maxLvl = 20;
		
		SLEEPING = new Sleeping();
		WANDERING = new Wandering();
		state = SLEEPING;

		loot = Gold.class;
		lootChance = 0.2f;
		
		properties.add(Property.UNDEAD);
	}
	@Override
	public Class<? extends CharSprite> GetSpriteClass() {

		return GhoulSprite.class;
	}

	@Override
	public int damageRoll(boolean isMaxDamage) {
		if (isMaxDamage) return 22;
		return Random.NormalIntRange( 16, 22 );
	}

	@Override
	public int attackSkill( Char target ) {
		return 24;
	}

	@Override
	public int drRoll() {
		return super.drRoll() + Random.NormalIntRange(0, 4);
	}

	@Override
	public float spawningWeight() {
		return 0.5f;
	}

	private int timesDowned = 0;
	protected int partnerID = -1;
	private boolean isSolo = false;

	private static final String PARTNER_ID = "partner_id";
	private static final String TIMES_DOWNED = "times_downed";
	private static final String IS_SOLO = "is_solo";
	
	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
		bundle.put( PARTNER_ID, partnerID );
		bundle.put( TIMES_DOWNED, timesDowned );
		bundle.put( IS_SOLO, isSolo );
	}
	
	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle( bundle );
		partnerID = bundle.getInt( PARTNER_ID );
		timesDowned = bundle.getInt( TIMES_DOWNED );
		isSolo = bundle.getBoolean( IS_SOLO );
	}
	
	@Override
	protected boolean act() {
		//create a child
		if (partnerID == -1 && !isSolo){
			
			ArrayList<Integer> candidates = new ArrayList<>();
			
			int[] neighbours = {pos + 1, pos - 1, pos + Dungeon.level.width(), pos - Dungeon.level.width()};
			for (int n : neighbours) {
				if (Dungeon.level.passable[n]
						&& Actor.findChar( n ) == null
						&& (!Char.hasProp(this, Property.LARGE) || Dungeon.level.openSpace[n])) {
					candidates.add( n );
				}
			}
			
			if (!candidates.isEmpty()){
				Ghoul child = new Ghoul();
				child.partnerID = this.id();
				this.partnerID = child.id();
				if (state != SLEEPING) {
					child.state = child.WANDERING;
				}
				
				child.pos = Random.element( candidates );

				GameScene.add( child );
				Dungeon.level.occupyCell(child);
				
				if (sprite.visible) {
					Actor.add( new Pushing( child, pos, child.pos ) );
				}

				//champion buff, mainly
				for (Buff b : buffs()){
					if (b.revivePersists) {
						Buff.affect(child, b.getClass());
					}
				}

			}
			
		}
		return super.act();
	}

	@Override
	public int attackProc( Char enemy, int damage ) {
		damage = super.attackProc( enemy, damage );

		if (getRandomizerEnabled(RandomTraits.DRAINING_CLAWS)) {
			if (Random.Int(10) == 0) {
				Buff.affect(enemy, Weakness.class);
				Buff.affect(enemy, Vulnerable.class);
			}
		}

		return damage;
	}

	@Override
	public void damage(int dmg, Object src, int damageType) {
		if (getRandomizerEnabled(RandomTraits.SHARED_PAIN) && !(src instanceof Ghoul)) {
			for (Mob m : Dungeon.level.mobs) {
				if (m instanceof Ghoul) {
					if (distance(m) < 5) {
						m.damage(dmg / 10, this);
					}
				}
			}
		}
		super.damage(dmg, src, damageType);
	}

	private boolean beingLifeLinked = false;

	@Override
	public void die(Object cause) {
		if (cause != Chasm.class && cause != GhoulLifeLink.class && !Dungeon.level.pit[pos]){
			Ghoul nearby = GhoulLifeLink.searchForHost(this);
			if (nearby != null){
				beingLifeLinked = true;
				timesDowned++;
				Actor.remove(this);
				Dungeon.level.mobs.remove( this );
				int timeToRespawn;
				if (getRandomizerEnabled(RandomTraits.RAPID_REVIVAL)) {
					timeToRespawn = timesDowned * 3;
				} else if (getRandomizerEnabled(RandomTraits.SLUGGISH_REVIVAL)) {
					timeToRespawn = timesDowned * 15;
				} else {
					timeToRespawn = timesDowned * 5;
				}

				Buff.append(nearby, GhoulLifeLink.class).set(timeToRespawn, this);
				((GhoulSprite)sprite).crumple();
				return;
			}
		}

		super.die(cause);
	}

	@Override
	public boolean isAlive() {
		return super.isAlive() || beingLifeLinked;
	}

	@Override
	public boolean isActive() {
		return !beingLifeLinked && isAlive();
	}

	@Override
	protected void onAdd(){
		boolean previousFirstAdded = firstAdded;
		super.onAdd();
		if (previousFirstAdded && getRandomizerEnabled(RandomTraits.LONE_WANDERER)) {
			isSolo = true;
		}
	}

	@Override
	protected synchronized void onRemove() {
		if (beingLifeLinked) {
			for (Buff buff : buffs()) {
				if (buff instanceof SacrificialFire.Marked){
					//don't remove and postpone so marked stays on
					Buff.prolong(this, SacrificialFire.Marked.class, timesDowned*5);
				} else if (buff.revivePersists) {
					//don't remove
				} else {
					buff.detach();
				}
			}
		} else {
			super.onRemove();
		}
	}

	private class Sleeping extends Mob.Sleeping {
		@Override
		public boolean act( boolean enemyInFOV, boolean justAlerted ) {
			Ghoul partner = (Ghoul) Actor.findById( partnerID );
			if (partner != null && partner.state != partner.SLEEPING){
				state = WANDERING;
				target = partner.pos;
				return true;
			} else {
				return super.act( enemyInFOV, justAlerted );
			}
		}
	}
	
	private class Wandering extends Mob.Wandering {
		
		@Override
		protected boolean continueWandering() {
			enemySeen = false;
			
			Ghoul partner = (Ghoul) Actor.findById( partnerID );
			if (partner != null && (partner.state != partner.WANDERING || Dungeon.level.distance( pos,  partner.target) > 1)){
				target = partner.pos;
				int oldPos = pos;
				if (getCloser( target )){
					spend( 1 / speed() );
					return moveSprite( oldPos, pos );
				} else {
					spend( TICK );
					return true;
				}
			} else {
				return super.continueWandering();
			}
		}
	}

	public static class GhoulLifeLink extends Buff{

		private Ghoul ghoul;
		private int turnsToRevive;

		@Override
		public boolean act() {
			if (target.alignment != ghoul.alignment){
				detach();
				return true;
			}

			if (target.fieldOfView == null){
				target.fieldOfView = new boolean[Dungeon.level.length()];
				Dungeon.level.updateFieldOfView( target, target.fieldOfView );
			}

			if (!target.fieldOfView[ghoul.pos] && Dungeon.level.distance(ghoul.pos, target.pos) >= 4){
				detach();
				return true;
			}

			if (Dungeon.level.pit[ghoul.pos]){
				super.detach();
				ghoul.beingLifeLinked = false;
				ghoul.die(this);
				return true;
			}

			//have to delay this manually here are a downed ghouls can't be directly frozen otherwise
			if (target.buff(Challenge.DuelParticipant.class) == null) {
				turnsToRevive--;
			}
			if (turnsToRevive <= 0){
				if (Actor.findChar( ghoul.pos ) != null) {
					ArrayList<Integer> candidates = new ArrayList<>();
					for (int n : PathFinder.NEIGHBOURS8) {
						int cell = ghoul.pos + n;
						if (Dungeon.level.passable[cell]
								&& Actor.findChar( cell ) == null
								&& (!Char.hasProp(ghoul, Property.LARGE) || Dungeon.level.openSpace[cell])) {
							candidates.add( cell );
						}
					}
					if (candidates.size() > 0) {
						int newPos = Random.element( candidates );
						Actor.add( new Pushing( ghoul, ghoul.pos, newPos ) );
						ghoul.pos = newPos;

					} else {
						spend(TICK);
						return true;
					}
				}
				if (getRandomizerEnabled(RandomTraits.FULL_RESURRECTION)) {
					ghoul.HP = ghoul.HT;
				} else {
					ghoul.HP = Math.round(ghoul.HT / 10f);
				}
				ghoul.beingLifeLinked = false;
				Actor.add(ghoul);
				ghoul.timeToNow();
				Dungeon.level.mobs.add(ghoul);
				Dungeon.level.occupyCell( ghoul );
				ghoul.sprite.idle();
				if (getRandomizerEnabled(RandomTraits.FULL_RESURRECTION)) {
					ghoul.sprite.showStatusWithIcon(CharSprite.POSITIVE, Integer.toString(ghoul.HT), FloatingText.HEALING);
				} else {
					ghoul.sprite.showStatusWithIcon(CharSprite.POSITIVE, Integer.toString(Math.round(ghoul.HT/10f)), FloatingText.HEALING);
				}
				super.detach();
				return true;
			}

			spend(TICK);
			return true;
		}

		public void updateVisibility(){
			if (ghoul != null && ghoul.sprite != null){
				ghoul.sprite.visible = Dungeon.level.heroFOV[ghoul.pos];
			}
		}

		public void set(int turns, Ghoul ghoul){
			this.ghoul = ghoul;
			turnsToRevive = turns;
		}

		@Override
		public void fx(boolean on) {
			if (on && ghoul != null && ghoul.sprite == null){
				GameScene.addSprite(ghoul);
				((GhoulSprite)ghoul.sprite).crumple();
			}
		}

		@Override
		public void detach() {
			super.detach();
			Ghoul newHost = searchForHost(ghoul);
			if (newHost != null){
				attachTo(newHost);
				timeToNow();
			} else {
				ghoul.beingLifeLinked = false;
				ghoul.die(this);
			}
		}

		private static final String GHOUL = "ghoul";
		private static final String LEFT  = "left";

		@Override
		public void storeInBundle(Bundle bundle) {
			super.storeInBundle(bundle);
			bundle.put(GHOUL, ghoul);
			bundle.put(LEFT, turnsToRevive);
		}

		@Override
		public void restoreFromBundle(Bundle bundle) {
			super.restoreFromBundle(bundle);
			ghoul = (Ghoul) bundle.get(GHOUL);
			ghoul.beingLifeLinked = true;
			turnsToRevive = bundle.getInt(LEFT);
		}

		public static Ghoul searchForHost(Ghoul dieing){

			for (Char ch : Actor.chars()){
				//don't count hero ally ghouls or duel frozen ghouls
				if (ch != dieing && ch instanceof Ghoul
						&& ch.alignment == dieing.alignment
						&& ch.buff(Challenge.SpectatorFreeze.class) == null){
					if (ch.fieldOfView == null){
						ch.fieldOfView = new boolean[Dungeon.level.length()];
						Dungeon.level.updateFieldOfView( ch, ch.fieldOfView );
					}
					if (ch.fieldOfView[dieing.pos] || Dungeon.level.distance(ch.pos, dieing.pos) < 4){
						return (Ghoul) ch;
					}
				}
			}
			return null;
		}
	}

	public enum RandomTraits {
		RAPID_REVIVAL, FULL_RESURRECTION, DRAINING_CLAWS, LONE_WANDERER, SLUGGISH_REVIVAL, SHARED_PAIN
	}

	public static boolean getRandomizerEnabled(RandomTraits r) {
		switch (r) {
			case RAPID_REVIVAL: return Randomizer.getCreatureBuff(Ghoul.class) == 1;
			case FULL_RESURRECTION: return Randomizer.getCreatureBuff(Ghoul.class) == 2;
			case DRAINING_CLAWS: return Randomizer.getCreatureBuff(Ghoul.class) == 3;
			case LONE_WANDERER: return Randomizer.getCreatureNerf(Ghoul.class) == 1;
			case SLUGGISH_REVIVAL: return Randomizer.getCreatureNerf(Ghoul.class) == 2;
			case SHARED_PAIN: return Randomizer.getCreatureNerf(Ghoul.class) == 3;
		}
		return false;
	}
}