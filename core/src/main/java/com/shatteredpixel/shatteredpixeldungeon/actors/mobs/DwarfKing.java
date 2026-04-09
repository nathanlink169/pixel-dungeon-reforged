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


import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Badges;
import com.shatteredpixel.shatteredpixeldungeon.Challenges;
import com.shatteredpixel.shatteredpixeldungeon.Constants;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.Statistics;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Barrier;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Degrade;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Doom;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.LifeLink;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.LockedFloor;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.Sheep;
import com.shatteredpixel.shatteredpixeldungeon.combat.DamageType;
import com.shatteredpixel.shatteredpixeldungeon.effects.Beam;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.Pushing;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.ElmoParticle;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.ShadowParticle;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.SparkParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.Heap;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.KingsCrown;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.glyphs.Viscosity;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.DriedRose;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfForce;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfTeleportation;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfLightning;
import com.shatteredpixel.shatteredpixeldungeon.journal.Bestiary;
import com.shatteredpixel.shatteredpixeldungeon.levels.CityBossLevel;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.BossHealthBar;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.utils.BundleableProperty;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.Game;
import com.watabou.noosa.audio.Music;
import com.watabou.noosa.audio.Sample;
import com.watabou.noosa.particles.Emitter;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;
import com.watabou.utils.Reflection;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;

public class DwarfKing extends Mob {
	@Override
	public Constants.mobs.mobsBase GetConstants() { return Constants.mobs.dwarfking; }

	@Override
	public int GetMaxHP() {
		return (int) (super.GetMaxHP() * (Dungeon.isChallenged(Challenges.STRONGER_BOSSES) ? 1.5f : 1.0f));
	}

	private final int MIN_COOLDOWN = Dungeon.isChallenged(Challenges.STRONGER_BOSSES) ? 8 : 10;
	private final int MAX_COOLDOWN = Dungeon.isChallenged(Challenges.STRONGER_BOSSES) ? 10 : 14;

	private static final int NONE = 0;
	private static final int LINK = 1;
	private static final int TELE = 2;

	private BundleableProperty.Int m_Phase = new BundleableProperty.Int("phase", 1);
	private BundleableProperty.Int m_SummonsMade = new BundleableProperty.Int("summons_made", 0);
	private BundleableProperty.Float m_SummonCooldown = new BundleableProperty.Float("summon_cd", 0);
	private BundleableProperty.Float m_AbilityCooldown = new BundleableProperty.Float("ability_cd", 0);
	private BundleableProperty.Int m_LastAbility = new BundleableProperty.Int("last_ability", 0);

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		m_Phase.Store(bundle);
		m_SummonsMade.Store(bundle);
		m_SummonCooldown.Store(bundle);
		m_AbilityCooldown.Store(bundle);
		m_LastAbility.Store(bundle);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		m_Phase.Restore(bundle);
		m_SummonsMade.Restore(bundle);
		m_SummonCooldown.Restore(bundle);
		m_AbilityCooldown.Restore(bundle);
		m_LastAbility.Restore(bundle);

		if (m_Phase.Get() == 2) properties.add(Property.IMMOVABLE);

		BossHealthBar.assignBoss(this);
		if (m_Phase.Get() == 3) BossHealthBar.bleed(true);
	}

	@Override
	protected boolean act() {
		if (pos == CityBossLevel.throne){
			throwItems();
		}

		if (m_Phase.Get() == 1) {

			if (m_SummonCooldown.Get() <= 0 && summonSubject(Dungeon.isChallenged(Challenges.STRONGER_BOSSES) ? 2 : 3)){
				m_SummonsMade.Increment();
				m_SummonCooldown.Set(m_SummonCooldown.Get() + Random.NormalIntRange(MIN_COOLDOWN, MAX_COOLDOWN));
			} else if (m_SummonCooldown.Get() > 0){
				m_SummonCooldown.Decrement();
			}

			if (paralysed > 0){
				spend(TICK);
				return true;
			}

			if (m_AbilityCooldown.Get() <= 0){

				if (m_LastAbility.Get() == NONE) {
					//50/50 either ability
					m_LastAbility.Set(Random.Int(2) == 0 ? LINK : TELE);
				} else if (m_LastAbility.Get() == LINK) {
					//more likely to use tele
					m_LastAbility.Set(Random.Int(8) == 0 ? LINK : TELE);
				} else {
					//more likely to use link
					m_LastAbility.Set(Random.Int(8) != 0 ? LINK : TELE);
				}

				if (m_LastAbility.Get() == LINK && lifeLinkSubject()){
					m_AbilityCooldown.Set(m_AbilityCooldown.Get() + Random.NormalIntRange(MIN_COOLDOWN, MAX_COOLDOWN));
					spend(TICK);
					return true;
				} else if (teleportSubject()) {
					m_LastAbility.Set(TELE);
					m_AbilityCooldown.Set(m_AbilityCooldown.Get() + Random.NormalIntRange(MIN_COOLDOWN, MAX_COOLDOWN));
					spend(TICK);
					return true;
				}

			} else {
				m_AbilityCooldown.Decrement();
			}

		} else if (m_Phase.Get() == 2){

			if (Dungeon.isChallenged(Challenges.STRONGER_BOSSES)){
				//challenge logic
				if (m_SummonsMade.Get() < 6){
					if (m_SummonsMade.Get() == 0) {
						sprite.centerEmitter().start(Speck.factory(Speck.SCREAM), 0.4f, 2);
						Sample.INSTANCE.play(Assets.Sounds.CHALLENGE);
						yell(Messages.get(this, "wave_1"));
					}
					summonSubject(3, DKGhoul.class);
					summonSubject(3, DKGhoul.class);
					spend(3 * TICK);
					m_SummonsMade.Add(2);
					return true;
				} else if (shielding() <= 300 && m_SummonsMade.Get() < 12){
					if (m_SummonsMade.Get() == 6) {
						sprite.centerEmitter().start(Speck.factory(Speck.SCREAM), 0.4f, 2);
						Sample.INSTANCE.play(Assets.Sounds.CHALLENGE);
						yell(Messages.get(this, "wave_2"));
					}
					summonSubject(3, DKGhoul.class);
					summonSubject(3, DKGhoul.class);
					if (m_SummonsMade.Get() == 6) {
						summonSubject(3, DKMonk.class);
					} else {
						summonSubject(3, DKWarlock.class);
					}
					m_SummonsMade.Add(3);
					spend(3*TICK);
					return true;
				} else if (shielding() <= 150 && m_SummonsMade.Get() < 18) {
					if (m_SummonsMade.Get() == 12) {
						sprite.centerEmitter().start(Speck.factory(Speck.SCREAM), 0.4f, 2);
						Sample.INSTANCE.play(Assets.Sounds.CHALLENGE);
						yell(Messages.get(this, "wave_3"));
						summonSubject(3, DKWarlock.class);
						summonSubject(3, DKMonk.class);
						summonSubject(3, DKGhoul.class);
						summonSubject(3, DKGhoul.class);
						m_SummonsMade.Add(4);
						spend(3*TICK);
					} else {
						summonSubject(3, DKGolem.class);
						summonSubject(3, DKGolem.class);
						m_SummonsMade.Add(2);
						spend(TICK);
					}
					return true;
				} else {
					spend(TICK);
					return true;
				}
			} else {
				//non-challenge logic
				if (m_SummonsMade.Get() < 4) {
					if (m_SummonsMade.Get() == 0) {
						sprite.centerEmitter().start(Speck.factory(Speck.SCREAM), 0.4f, 2);
						Sample.INSTANCE.play(Assets.Sounds.CHALLENGE);
						yell(Messages.get(this, "wave_1"));
					}
					summonSubject(3, DKGhoul.class);
					spend(3 * TICK);
					m_SummonsMade.Increment();
					return true;
				} else if (shielding() <= 200 && m_SummonsMade.Get() < 8) {
					if (m_SummonsMade.Get() == 4) {
						sprite.centerEmitter().start(Speck.factory(Speck.SCREAM), 0.4f, 2);
						Sample.INSTANCE.play(Assets.Sounds.CHALLENGE);
						yell(Messages.get(this, "wave_2"));
					}
					if (m_SummonsMade.Get() == 7) {
						summonSubject(3, Random.Int(2) == 0 ? DKMonk.class : DKWarlock.class);
					} else {
						summonSubject(3, DKGhoul.class);
					}
					m_SummonsMade.Increment();
					spend(TICK);
					return true;
				} else if (shielding() <= 100 && m_SummonsMade.Get() < 12) {
					sprite.centerEmitter().start(Speck.factory(Speck.SCREAM), 0.4f, 2);
					Sample.INSTANCE.play(Assets.Sounds.CHALLENGE);
					yell(Messages.get(this, "wave_3"));
					summonSubject(4, DKWarlock.class);
					summonSubject(4, DKMonk.class);
					summonSubject(4, DKGhoul.class);
					summonSubject(4, DKGhoul.class);
					m_SummonsMade.Set(12);
					spend(TICK);
					return true;
				} else {
					spend(TICK);
					return true;
				}
			}
		} else if (m_Phase.Get() == 3 && buffs(Summoning.class).size() < 4){
			if (summonSubject(Dungeon.isChallenged(Challenges.STRONGER_BOSSES) ? 2 : 3)) m_SummonsMade.Increment();
		}

		return super.act();
	}

	private boolean summonSubject( int delay ){
		if (Dungeon.isChallenged(Challenges.STRONGER_BOSSES)) {
			//every 3rd summon is always a monk or warlock, otherwise ghoul
			//except every 9th summon, which is a golem!
			if (m_SummonsMade.Get() % 3 == 2) {
				if (m_SummonsMade.Get() % 9 == 8){
					return summonSubject(delay, DKGolem.class);
				} else {
					return summonSubject(delay, Random.Int(2) == 0 ? DKMonk.class : DKWarlock.class);
				}
			} else {
				return summonSubject(delay, DKGhoul.class);
			}

		} else {
			//every 4th summon is always a monk or warlock, otherwise ghoul
			if (m_SummonsMade.Get() % 4 == 3) {
				return summonSubject(delay, Random.Int(2) == 0 ? DKMonk.class : DKWarlock.class);
			} else {
				return summonSubject(delay, DKGhoul.class);
			}
		}
	}

	private boolean summonSubject( int delay, Class<?extends Mob> type ){
		Summoning s = new Summoning();
		s.m_Position.Set(((CityBossLevel)Dungeon.level).getSummoningPos());
		if (s.m_Position.Get() == -1) return false;
		s.m_Summon.Set(type);
		s.m_Delay.Set(delay);
		s.attachTo(this);
		return true;
	}

	private HashSet<Mob> getSubjects(){
		HashSet<Mob> subjects = new HashSet<>();
		for (Mob m : Dungeon.level.mobs){
			if (m.alignment == alignment && (m instanceof Ghoul || m instanceof Monk || m instanceof Warlock || m instanceof Golem)){
				subjects.add(m);
			}
		}
		return subjects;
	}

	private boolean lifeLinkSubject(){
		Mob furthest = null;

		for (Mob m : getSubjects()){
			boolean alreadyLinked = false;
			for (LifeLink l : m.buffs(LifeLink.class)){
				if (l.object == id()) alreadyLinked = true;
			}
			if (!alreadyLinked) {
				if (furthest == null || Dungeon.level.distance(pos, furthest.pos) < Dungeon.level.distance(pos, m.pos)){
					furthest = m;
				}
			}
		}

		if (furthest != null) {
			Buff.append(furthest, LifeLink.class, 100f).object = id();
			Buff.append(this, LifeLink.class, 100f).object = furthest.id();
			yell(Messages.get(this, "lifelink_" + Random.IntRange(1, 2)));
			sprite.parent.add(new Beam.HealthRay(sprite.destinationCenter(), furthest.sprite.destinationCenter()));
			return true;

		}
		return false;
	}

	private boolean teleportSubject(){
		if (enemy == null) return false;

		Mob furthest = null;

		for (Mob m : getSubjects()){
			if (furthest == null || Dungeon.level.distance(pos, furthest.pos) < Dungeon.level.distance(pos, m.pos)){
				furthest = m;
			}
		}

		if (furthest != null){

			float bestDist;
			int bestPos = pos;

			Ballistica trajectory = new Ballistica(enemy.pos, pos, Ballistica.STOP_TARGET);
			int targetCell = trajectory.path.get(trajectory.dist+1);
			//if the position opposite the direction of the hero is open, go there
			if (Actor.findChar(targetCell) == null && !Dungeon.level.solid[targetCell]){
				bestPos = targetCell;

			//Otherwise go to the neighbour cell that's open and is furthest
			} else {
				bestDist = Dungeon.level.trueDistance(pos, enemy.pos);

				for (int i : PathFinder.NEIGHBOURS8){
					if (Actor.findChar(pos+i) == null
							&& !Dungeon.level.solid[pos+i]
							&& Dungeon.level.trueDistance(pos+i, enemy.pos) > bestDist){
						bestPos = pos+i;
						bestDist = Dungeon.level.trueDistance(pos+i, enemy.pos);
					}
				}
			}

			Actor.add(new Pushing(this, pos, bestPos));
			pos = bestPos;

			//find closest cell that's adjacent to enemy, place subject there
			bestDist = Dungeon.level.trueDistance(enemy.pos, pos);
			bestPos = enemy.pos;
			for (int i : PathFinder.NEIGHBOURS8){
				if (Actor.findChar(enemy.pos+i) == null
						&& !Dungeon.level.solid[enemy.pos+i]
						&& Dungeon.level.trueDistance(enemy.pos+i, pos) < bestDist){
					bestPos = enemy.pos+i;
					bestDist = Dungeon.level.trueDistance(enemy.pos+i, pos);
				}
			}

			if (bestPos != enemy.pos) ScrollOfTeleportation.appear(furthest, bestPos);
			yell(Messages.get(this, "teleport_" + Random.IntRange(1, 2)));
			return true;
		}
		return false;
	}

	@Override
	public void notice() {
		super.notice();
		if (!BossHealthBar.isAssigned()) {
			BossHealthBar.assignBoss(this);
			yell(Messages.get(this, "notice"));
			for (Char ch : Actor.chars()){
				if (ch instanceof DriedRose.GhostHero){
					((DriedRose.GhostHero) ch).sayBoss();
				}
			}
		}
	}

	@Override
	public boolean isInvulnerable(Class effect) {
		if (effect == KingDamager.class){
			return false;
		} else {
			return m_Phase.Get() == 2 || super.isInvulnerable(effect);
		}
	}

	@Override
	public int Damage(int dmg, Object src, EnumSet<DamageType> damageType) {
		//hero counts as unarmed if they aren't attacking with a weapon and aren't benefiting from force
		if (src == Dungeon.hero && (!RingOfForce.fightingUnarmed(Dungeon.hero) || Dungeon.hero.buff(RingOfForce.Force.class) != null)){
			Statistics.qualifiedForBossChallengeBadge = false;
		//Corrosion, corruption, and regrowth do no direct damage and so have their own custom logic
		//Transfusion damages DK and so doesn't need custom logic
		//Lightning has custom logic so that chaining it doesn't DQ for the badge
		} else if (src instanceof Wand && !(src instanceof WandOfLightning)){
			Statistics.qualifiedForBossChallengeBadge = false;
		}

		if (isInvulnerable(src.getClass())){
			return super.Damage(dmg, src, damageType);
		} else if (m_Phase.Get() == 3 && !(src instanceof Viscosity.DeferedDamage)){
			if (dmg >= 0) {
				Viscosity.DeferedDamage deferred = Buff.affect( this, Viscosity.DeferedDamage.class );
				deferred.extend( dmg );

				sprite.showStatus( CharSprite.WARNING, Messages.get(Viscosity.class, "deferred", dmg) );
			}
			return 0;
		}
		int preHP = HP;
		super.Damage(dmg, src, damageType);

		LockedFloor lock = Dungeon.hero.buff(LockedFloor.class);
		if (lock != null && !isImmune(src.getClass()) && !isInvulnerable(src.getClass())){
			if (Dungeon.isChallenged(Challenges.STRONGER_BOSSES))   lock.addTime(dmg/5f);
			else                                                    lock.addTime(dmg/3f);
		}

		if (m_Phase.Get() == 1) {
			int dmgTaken = preHP - HP;
			m_AbilityCooldown.Subtract(dmgTaken/8f);
			m_SummonCooldown.Subtract(dmgTaken/8f);
			if (HP <= (Dungeon.isChallenged(Challenges.STRONGER_BOSSES) ? 100 : 50)) {
				HP = (Dungeon.isChallenged(Challenges.STRONGER_BOSSES) ? 100 : 50);
				sprite.showStatus(CharSprite.POSITIVE, Messages.get(this, "invulnerable"));
				ScrollOfTeleportation.appear(this, CityBossLevel.throne);
				properties.add(Property.IMMOVABLE);
				m_Phase.Set(2);
				m_SummonsMade.Set(0);
				sprite.idle();
				Buff.affect(this, DKBarrier.class).setShield(GetMaxHP());
				for (Summoning s : buffs(Summoning.class)) {
					s.detach();
				}
				Bestiary.skipCountingEncounters = true;
				for (Mob m : getSubjects()) {
					m.die(null);
				}
				Bestiary.skipCountingEncounters = false;
				for (Buff b: buffs()){
					if (b instanceof LifeLink){
						b.detach();
					}
				}
			}
		} else if (m_Phase.Get() == 2 && shielding() == 0) {
			properties.remove(Property.IMMOVABLE);
			m_Phase.Set(3);
			m_SummonsMade.Set(1); //monk/warlock on 3rd summon
			sprite.centerEmitter().start( Speck.factory( Speck.SCREAM ), 0.4f, 2 );
			Sample.INSTANCE.play( Assets.Sounds.CHALLENGE );
			yell(  Messages.get(this, "enraged", Dungeon.hero.name(false)) );
			BossHealthBar.bleed(true);
			Game.runOnRenderThread(new Callback() {
				@Override
				public void call() {
					Music.INSTANCE.fadeOut(0.5f, new Callback() {
						@Override
						public void call() {
							Music.INSTANCE.play(Assets.Music.CITY_BOSS_FINALE, true);
						}
					});
				}
			});
		} else if (m_Phase.Get() == 3 && preHP > 20 && HP < 20 && isAlive()){
			yell( Messages.get(this, "losing") );
		}
		return preHP - HP;
	}

	@Override
	public boolean isAlive() {
		return super.isAlive() || m_Phase.Get() != 3;
	}

	@Override
	public void die(Object cause) {

		if (m_Phase.Get() < 3) {
			return;
		}

		GameScene.bossSlain();

		super.die( cause );

		Heap h = Dungeon.level.heaps.get(CityBossLevel.throne);
		if (h != null) {
			for (Item i : h.items) {
				Dungeon.level.drop(i, CityBossLevel.throne + Dungeon.level.width());
			}
			h.destroy();
		}

		if (pos == CityBossLevel.throne){
			Dungeon.level.drop(new KingsCrown(), pos + Dungeon.level.width()).sprite.drop(pos);
		} else {
			Dungeon.level.drop(new KingsCrown(), pos).sprite.drop();
		}

		Badges.validateBossSlain(this);
		if (Statistics.qualifiedForBossChallengeBadge){
			Badges.validateBossChallengeCompleted();
		}
		Statistics.bossScores[3] += 4000;

		Dungeon.level.unseal();

		Bestiary.skipCountingEncounters = true;
		for (Mob m : getSubjects()){
			m.die(null);
		}
		Bestiary.skipCountingEncounters = false;

		//cleanses degrade that may have been applied by a DK warlock, mainly for convenience
		if (Dungeon.hero.buff(Degrade.class) != null){
			Dungeon.hero.buff(Degrade.class).detach();
		}

		yell( Messages.get(this, "defeated") );
	}

	@Override
	public boolean isImmune(Class effect) {
		//immune to damage amplification from doomed in 2nd phase or later, but it can still be applied
		if (m_Phase.Get() > 1 && effect == Doom.class && buff(Doom.class) != null ){
			return true;
		}
		return super.isImmune(effect);
	}

	public static class DKGhoul extends Ghoul {
		{
			properties.add(Property.BOSS_MINION);
			state = HUNTING;
		}
		@Override
		public int GetMaxLevel() { return -2; }

		@Override
		protected boolean act() {
			m_PartnerID.Set(-2); // no partners
			return super.act();
		}
	}

	public static class DKMonk extends Monk {
		{
			properties.add(Property.BOSS_MINION);
			state = HUNTING;
		}
		@Override
		public int GetMaxLevel() { return -2; }
	}

	public static class DKWarlock extends Warlock {
		{
			properties.add(Property.BOSS_MINION);
			state = HUNTING;
		}
		@Override
		public int GetMaxLevel() { return -2; }

		@Override
		protected void zap() {
			if (enemy == Dungeon.hero){
				Statistics.bossScores[3] -= 400;
			}
			super.zap();
		}
	}

	public static class DKGolem extends Golem {
		{
			properties.add(Property.BOSS_MINION);
			state = HUNTING;
		}

		@Override
		public int GetMaxLevel() { return -2; }
	}

	public static class Summoning extends Buff {
		private Emitter particles;

		public int getPos() {
			return m_Position.Get();
		}

		@Override
		public boolean act() {
			m_Delay.Decrement();

			if (m_Delay.Get() <= 0){

				if (m_Summon.Get() == DKGolem.class){
					particles.burst(SparkParticle.FACTORY, 10);
					Sample.INSTANCE.play(Assets.Sounds.CHARGEUP);
				} else if (m_Summon.Get() == DKWarlock.class){
					particles.burst(ShadowParticle.CURSE, 10);
					Sample.INSTANCE.play(Assets.Sounds.CURSED);
				} else if (m_Summon.Get() == DKMonk.class){
					particles.burst(ElmoParticle.FACTORY, 10);
					Sample.INSTANCE.play(Assets.Sounds.BURNING);
				} else {
					particles.burst(Speck.factory(Speck.BONE), 10);
					Sample.INSTANCE.play(Assets.Sounds.BONES);
				}
				particles = null;

				if (Actor.findChar(m_Position.Get()) != null){
					ArrayList<Integer> candidates = new ArrayList<>();
					for (int i : PathFinder.NEIGHBOURS8){
						if (Dungeon.level.passable[m_Position.Get()+i] && Actor.findChar(m_Position.Get()+i) == null){
							candidates.add(m_Position.Get()+i);
						}
					}
					if (!candidates.isEmpty()){
						m_Position.Set(Random.element(candidates));
					}
				}

				//kill sheep that are right on top of the spawner instead of failing to spawn
				if (Actor.findChar(m_Position.Get()) instanceof Sheep){
					Actor.findChar(m_Position.Get()).die(null);
				}

				if (Actor.findChar(m_Position.Get()) == null) {
					Mob m = (Mob) Reflection.newInstance(m_Summon.Get());
					m.pos = m_Position.Get();
					GameScene.add(m);
					Dungeon.level.occupyCell(m);
					m.state = m.HUNTING;
					if (((DwarfKing)target).m_Phase.Get() == 2){
						Buff.affect(m, KingDamager.class);
					}
				} else {
					Char ch = Actor.findChar(m_Position.Get());
					ch.Damage(Random.NormalIntRange(20, 40), this, DamageType.of(DamageType.BLUDGEONING));
					if (((DwarfKing)target).m_Phase.Get() == 2){
						if (Dungeon.isChallenged(Challenges.STRONGER_BOSSES)){
							target.Damage(target.GetMaxHP()/18, new KingDamager(), DamageType.of(DamageType.BLUDGEONING));
						} else {
							target.Damage(target.GetMaxHP()/12, new KingDamager(), DamageType.of(DamageType.BLUDGEONING));
						}
					}
					if (!ch.isAlive() && ch == Dungeon.hero) {
						Dungeon.fail(DwarfKing.class);
						GLog.n( Messages.capitalize(Messages.get(Char.class, "kill", Messages.get(DwarfKing.class, "name"))));
					}
				}

				detach();
			}

			spend(TICK);
			return true;
		}

		@Override
		public void fx(boolean on) {
			if (on && (particles == null || particles.parent == null)) {
				particles = CellEmitter.get(m_Position.Get());

				if (m_Summon.Get() == DKGolem.class){
					particles.pour(SparkParticle.STATIC, 0.05f);
				} else if (m_Summon.Get() == DKWarlock.class){
					particles.pour(ShadowParticle.UP, 0.1f);
				} else if (m_Summon.Get() == DKMonk.class){
					particles.pour(ElmoParticle.FACTORY, 0.1f);
				} else {
					particles.pour(Speck.factory(Speck.RATTLE), 0.1f);
				}

			} else if (!on && particles != null) {
				particles.on = false;
			}
		}

		private BundleableProperty.Int m_Delay = new BundleableProperty.Int("delay", 0);
		private BundleableProperty.Int m_Position = new BundleableProperty.Int("pos", 0);
		private BundleableProperty.Clazz m_Summon = new BundleableProperty.Clazz("summon");

		@Override
		public void storeInBundle(Bundle bundle) {
			super.storeInBundle(bundle);
			m_Delay.Store(bundle);
			m_Position.Store(bundle);
			m_Summon.Store(bundle);
		}

		@Override
		public void restoreFromBundle(Bundle bundle) {
			super.restoreFromBundle(bundle);
			m_Delay.Restore(bundle);
			m_Position.Restore(bundle);
			m_Summon.Restore(bundle);
		}
	}

	public static class KingDamager extends Buff {

		{
			revivePersists = true;
		}

		@Override
		public boolean act() {
			if (target.alignment != Alignment.ENEMY){
				detach();
			}
			spend( TICK );
			return true;
		}

		@Override
		public void detach() {
			super.detach();
			for (Mob m : Dungeon.level.mobs){
				if (m instanceof DwarfKing){
					int damage = m.GetMaxHP() / (Dungeon.isChallenged(Challenges.STRONGER_BOSSES) ? 18 : 12);
					m.Damage(damage, this, DamageType.of(DamageType.NONE));
				}
			}
		}
	}

	public static class DKBarrier extends Barrier{

		@Override
		public boolean act() {
			incShield();
			return super.act();
		}

		@Override
		public int icon() {
			return BuffIndicator.NONE;
		}
	}

}
