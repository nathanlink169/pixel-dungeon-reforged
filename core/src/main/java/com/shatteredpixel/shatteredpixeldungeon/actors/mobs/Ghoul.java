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
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.SacrificialFire;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.AscensionChallenge;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Vulnerable;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Weakness;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.duelist.Challenge;
import com.shatteredpixel.shatteredpixeldungeon.combat.AttackContext;
import com.shatteredpixel.shatteredpixeldungeon.combat.CombatModifier;
import com.shatteredpixel.shatteredpixeldungeon.combat.DamageType;
import com.shatteredpixel.shatteredpixeldungeon.effects.FloatingText;
import com.shatteredpixel.shatteredpixeldungeon.effects.Pushing;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.Chasm;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.GhoulSprite;
import com.shatteredpixel.shatteredpixeldungeon.utils.BundleableProperty;
import com.watabou.utils.Bundle;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;

public class Ghoul extends Mob implements CombatModifier.OnDamageEffect {
	{
		SLEEPING = new Sleeping();
		WANDERING = new Wandering();
		state = SLEEPING;
	}
	@Override
	public Constants.mobs.mobsBase GetConstants() { return Constants.mobs.ghoul; }

	@Override
	public float spawningWeight() {
		return 0.5f;
	}

	private BundleableProperty.Int m_TimesDowned = new BundleableProperty.Int("times_downed", 0);
	protected BundleableProperty.Int m_PartnerID = new BundleableProperty.Int("partner_id", -1);
	private BundleableProperty.Bool m_IsSolo = new BundleableProperty.Bool("is_solo", false);
	
	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
		m_TimesDowned.Store(bundle);
		m_PartnerID.Store(bundle);
		m_IsSolo.Store(bundle);
	}
	
	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle( bundle );
		m_TimesDowned.Restore(bundle);
		m_PartnerID.Restore(bundle);
		m_IsSolo.Restore(bundle);
	}
	
	@Override
	protected boolean act() {
		//create a child
		if (m_PartnerID.Get() == -1 && !m_IsSolo.Get()){
			
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
				child.m_PartnerID.Set(this.id());
				this.m_PartnerID.Set(child.id());
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

	private boolean beingLifeLinked = false;

	@Override
	public void die(Object cause) {
		if (cause != Chasm.class && cause != GhoulLifeLink.class && !Dungeon.level.pit[pos]){
			Ghoul nearby = GhoulLifeLink.searchForHost(this);
			if (nearby != null){
				beingLifeLinked = true;
				m_TimesDowned.Increment();
				Actor.remove(this);
				Dungeon.level.mobs.remove( this );
				int timeToRespawn;
				if (getRandomizerEnabled(RandomTraits.RAPID_REVIVAL)) {
					timeToRespawn = m_TimesDowned.Get() * 3;
				} else if (getRandomizerEnabled(RandomTraits.SLUGGISH_REVIVAL)) {
					timeToRespawn = m_TimesDowned.Get() * 15;
				} else {
					timeToRespawn = m_TimesDowned.Get() * 5;
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
			m_IsSolo.Set(true);
		}
	}

	@Override
	protected synchronized void onRemove() {
		if (beingLifeLinked) {
			for (Buff buff : buffs()) {
				if (buff instanceof SacrificialFire.Marked){
					//don't remove and postpone so marked stays on
					Buff.prolong(this, SacrificialFire.Marked.class, m_TimesDowned.Get() * 5);
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

	@Override
	public int Damage(int dmg, Object src, EnumSet<DamageType> damageType ) {
		if (getRandomizerEnabled(RandomTraits.SHARED_PAIN) && !(src instanceof Ghoul)) {
			HashSet<Mob> allMobs = Dungeon.level.mobs;
			for (Mob m : allMobs) {
				if (m instanceof Ghoul && m != this) {
					if (distance(m) < 10) {
						m.Damage(dmg / 10, this, DamageType.of(DamageType.NONE));
					}
				}
			}
		}
		return super.Damage(dmg, src, damageType);
	}

	@Override
	public void onDamage(AttackContext context, int damageDealt) {
		if (context.attacker == this) {
			if (getRandomizerEnabled(RandomTraits.DRAINING_CLAWS)) {
				if (Random.Int(10) == 0) {
					Buff.affect(context.defender, Weakness.class);
					Buff.affect(context.defender, Vulnerable.class);
				}
			}
		}
	}

	@Override
	public int priority() {
		return Priority.NORMAL;
	}

	@Override
	public boolean appliesTo(AttackContext context) {
		return context.attacker == this || context.defender == this;
	}

	private static class Sleeping extends Mob.Sleeping {
		@Override
		public boolean act( Mob mob, boolean enemyInFOV, boolean justAlerted ) {
			Ghoul partner = (Ghoul) Actor.findById( ((Ghoul)mob).m_PartnerID.Get() );
			if (partner != null && partner.state != partner.SLEEPING){
				mob.state = mob.WANDERING;
				mob.m_Target.Set(partner.pos);
				return true;
			} else {
				return super.act( mob, enemyInFOV, justAlerted );
			}
		}
	}
	
	private static class Wandering extends Mob.Wandering {
		
		@Override
		protected boolean continueWandering(Mob mob) {
			mob.m_EnemySeen.Set(false);
			
			Ghoul partner = (Ghoul) Actor.findById( ((Ghoul)mob).m_PartnerID.Get() );
			if (partner != null && (partner.state != partner.WANDERING || Dungeon.level.distance( mob.pos,  partner.m_Target.Get()) > 1)){
				mob.m_Target.Set(partner.pos);
				int oldPos = mob.pos;
				if (mob.getCloser( mob.m_Target.Get() )){
					mob.spend( 1 / mob.speed() );
					return mob.moveSprite( oldPos, mob.pos );
				} else {
					mob.spend( TICK );
					return true;
				}
			} else {
				return super.continueWandering(mob);
			}
		}
	}

	public static class GhoulLifeLink extends Buff{

		@Override
		public boolean act() {
			if (target.alignment != m_Ghoul.Get().alignment){
				detach();
				return true;
			}

			if (target.fieldOfView == null){
				target.fieldOfView = new boolean[Dungeon.level.length()];
				Dungeon.level.updateFieldOfView( target, target.fieldOfView );
			}

			if (!target.fieldOfView[m_Ghoul.Get().pos] && Dungeon.level.distance(m_Ghoul.Get().pos, target.pos) >= 4){
				detach();
				return true;
			}

			if (Dungeon.level.pit[m_Ghoul.Get().pos]){
				super.detach();
				m_Ghoul.Get().beingLifeLinked = false;
				m_Ghoul.Get().die(this);
				return true;
			}

			//have to delay this manually here are a downed ghouls can't be directly frozen otherwise
			if (target.buff(Challenge.DuelParticipant.class) == null) {
				m_TurnsToRevive.Decrement();
			}
			if (m_TurnsToRevive.Get() <= 0){
				if (Actor.findChar( m_Ghoul.Get().pos ) != null) {
					ArrayList<Integer> candidates = new ArrayList<>();
					for (int n : PathFinder.NEIGHBOURS8) {
						int cell = m_Ghoul.Get().pos + n;
						if (Dungeon.level.passable[cell]
								&& Actor.findChar( cell ) == null
								&& (!Char.hasProp(m_Ghoul.Get(), Property.LARGE) || Dungeon.level.openSpace[cell])) {
							candidates.add( cell );
						}
					}
					if (candidates.size() > 0) {
						int newPos = Random.element( candidates );
						Actor.add( new Pushing( m_Ghoul.Get(), m_Ghoul.Get().pos, newPos ) );
						m_Ghoul.Get().pos = newPos;

					} else {
						spend(TICK);
						return true;
					}
				}

				m_Ghoul.Get().beingLifeLinked = false;
				Actor.add(m_Ghoul.Get());
				m_Ghoul.Get().timeToNow();
				Dungeon.level.mobs.add(m_Ghoul.Get());
				Dungeon.level.occupyCell( m_Ghoul.Get() );
				m_Ghoul.Get().sprite.idle();
				if (getRandomizerEnabled(RandomTraits.FULL_RESURRECTION)) {
					m_Ghoul.Get().sprite.showStatusWithIcon(CharSprite.POSITIVE, Integer.toString(m_Ghoul.Get().GetMaxHP()), FloatingText.HEALING);
				} else {
					m_Ghoul.Get().sprite.showStatusWithIcon(CharSprite.POSITIVE, Integer.toString(Math.round(m_Ghoul.Get().GetMaxHP()/10f)), FloatingText.HEALING);
				}
				if (getRandomizerEnabled(RandomTraits.FULL_RESURRECTION)) {
					m_Ghoul.Get().HP = m_Ghoul.Get().GetMaxHP();
				} else {
					m_Ghoul.Get().HP = Math.round(m_Ghoul.Get().GetMaxHP() / 10f);
				}
				super.detach();
				return true;
			}

			spend(TICK);
			return true;
		}

		public void updateVisibility(){
			if (m_Ghoul.Get() != null && m_Ghoul.Get().sprite != null){
				m_Ghoul.Get().sprite.visible = Dungeon.level.heroFOV[m_Ghoul.Get().pos];
			}
		}

		public void set(int turns, Ghoul ghoul){
			this.m_Ghoul.Set(ghoul);
			m_TurnsToRevive.Set(turns);
		}

		@Override
		public void fx(boolean on) {
			if (on && m_Ghoul.Get() != null && m_Ghoul.Get().sprite == null){
				GameScene.addSprite(m_Ghoul.Get());
				((GhoulSprite)m_Ghoul.Get().sprite).crumple();
			}
		}

		@Override
		public void detach() {
			super.detach();
			Ghoul newHost = searchForHost(m_Ghoul.Get());
			if (newHost != null){
				attachTo(newHost);
				timeToNow();
			} else {
				m_Ghoul.Get().beingLifeLinked = false;
				m_Ghoul.Get().die(this);
			}
		}

		private BundleableProperty.Object<Ghoul> m_Ghoul = new BundleableProperty.Object<>("ghoul", null);
		private BundleableProperty.Int m_TurnsToRevive = new BundleableProperty.Int("left", 0);

		@Override
		public void storeInBundle(Bundle bundle) {
			super.storeInBundle(bundle);
			m_Ghoul.Store(bundle);
			m_TurnsToRevive.Store(bundle);
		}

		@Override
		public void restoreFromBundle(Bundle bundle) {
			super.restoreFromBundle(bundle);
			m_Ghoul.Restore(bundle);
			m_Ghoul.Get().beingLifeLinked = true;
			m_TurnsToRevive.Restore(bundle);
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