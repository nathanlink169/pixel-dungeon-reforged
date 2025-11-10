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
import com.shatteredpixel.shatteredpixeldungeon.Constants;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.Randomizer;
import com.shatteredpixel.shatteredpixeldungeon.Statistics;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Blob;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Fire;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Freezing;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Amok;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Blindness;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Burning;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Charm;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Chill;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.shatteredpixel.shatteredpixeldungeon.combat.AttackContext;
import com.shatteredpixel.shatteredpixeldungeon.combat.AttackResult;
import com.shatteredpixel.shatteredpixeldungeon.combat.CombatModifier;
import com.shatteredpixel.shatteredpixeldungeon.combat.CombatResolver;
import com.shatteredpixel.shatteredpixeldungeon.combat.DamageType;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.Lightning;
import com.shatteredpixel.shatteredpixeldungeon.effects.Splash;
import com.shatteredpixel.shatteredpixeldungeon.effects.TargetedCell;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.ElmoParticle;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.SparkParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.quest.Embers;
import com.shatteredpixel.shatteredpixeldungeon.items.trinkets.RatSkull;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.CursedWand;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.Shocking;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.utils.BundleableProperty;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.Game;
import com.watabou.noosa.audio.Music;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.BArray;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;
import com.watabou.utils.GameMath;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;

public abstract class Elemental extends Mob {
	@Override
	public int GetMaxHP() {
		if (!m_SummonedAlly.Get()) {
			return (int) (super.GetMaxHP() * (getRandomizerEnabled(RandomTraits.EMPOWERED_FORM, this) ? 1.5f : 1.0f));
		} else {
			return 15 * Math.max(2, (1 + Dungeon.scalingDepth()/5));
		}
	}

	@Override
	public int damageRoll(AttackContext.AttackType type, boolean isMaxDamage) {
		if (!m_SummonedAlly.Get()) {
			return super.damageRoll(type, isMaxDamage);
		} else {
			int regionScale = Math.max(2, (1 + Dungeon.scalingDepth()/5));
			if (isMaxDamage) return 5 + 5*regionScale;
			return Random.NormalIntRange(5*regionScale, 5 + 5*regionScale);
		}
	}

	@Override
	public int attackSkill() {
		if (!m_SummonedAlly.Get()) {
			return super.attackSkill();
		} else {
			int regionScale = Math.max(2, (1 + Dungeon.scalingDepth()/5));
			return 5 + 5*regionScale;
		}
	}

	public void setSummonedALly(){
		m_SummonedAlly.Set(true);
	}

	@Override
	public int defenseSkill() {
		if (!m_SummonedAlly.Get()) {
			return super.defenseSkill();
		} else {
			return Math.max(2, (1 + Dungeon.scalingDepth()/5)) * 5;
		}
	}
	
	@Override
	public int drRoll(EnumSet<DamageType> damageType) {
		return super.drRoll(damageType) + Random.NormalIntRange(0, 5);
	}
	
	@Override
	protected boolean act() {
		if (state == HUNTING){
			m_RangedCooldown.Decrement();
			if (getRandomizerEnabled(RandomTraits.RANGED_MASTERY, this)) {
				m_RangedCooldown.Set(0);
			}
		}
		
		return super.act();
	}

	@Override
	public void die(Object cause) {
		flying = false;
		super.die(cause);
	}
	
	@Override
	protected boolean canAttack( Char enemy ) {
		if (super.canAttack(enemy)){
			return true;
		} else {
			return m_RangedCooldown.Get() < 0 && new Ballistica( pos, enemy.pos, Ballistica.MAGIC_BOLT ).collisionPos == enemy.pos;
		}
	}
	
	public boolean doAttack(Char enemy) {
		
		if (Dungeon.level.adjacent( pos, enemy.pos )
				|| m_RangedCooldown.Get() > 0
				|| new Ballistica( pos, enemy.pos, Ballistica.MAGIC_BOLT ).collisionPos != enemy.pos) {
			
			return super.doAttack( enemy );
			
		} else {
			
			if (sprite != null && (sprite.visible || enemy.sprite.visible)) {
				sprite.zap( enemy.pos );
				return false;
			} else {
				zap();
				return true;
			}
		}
	}
	
	protected void zap() {
		spend( 1f );

		Invisibility.dispel(this);
		Char enemy = this.enemy;
		// Build attack context
		AttackContext context = new AttackContext.Builder(this, enemy)
				.attackType(AttackContext.AttackType.RANGED)
				.damageType(GetDamageType(AttackContext.AttackType.RANGED))
				.build();

		// Resolve attack - this handles EVERYTHING internally
		AttackResult result = CombatResolver.resolve(context);

		if (result.result != AttackResult.ResultType.HIT) {
			enemy.sprite.showStatus( CharSprite.NEUTRAL,  enemy.defenseVerb() );
		}

		m_RangedCooldown.Set(damageRoll(AttackContext.AttackType.RANGED, false));
		if (getRandomizerEnabled(RandomTraits.RANGED_MASTERY, this)) {
			m_RangedCooldown.Set(0);
		}
	}
	
	public void onZapComplete() {
		zap();
		next();
	}
	
	@Override
	public boolean add( Buff buff ) {
		if (harmfulBuffs.contains( buff.getClass() )) {
			Damage( Random.NormalIntRange( GetMaxHP()/2, GetMaxHP() * 3/5 ), buff, GetHarmfulBuffDamageType() );
			return false;
		} else {
			return super.add( buff );
		}
	}

	protected abstract EnumSet<DamageType> GetHarmfulBuffDamageType();
	
	protected ArrayList<Class<? extends Buff>> harmfulBuffs = new ArrayList<>();

	protected BundleableProperty.Int m_RangedCooldown = new BundleableProperty.Int("cooldown", 0);
	protected BundleableProperty.Bool m_SummonedAlly = new BundleableProperty.Bool("summoned_ally", false);
	
	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
		m_RangedCooldown.Store(bundle);
		m_SummonedAlly.Store(bundle);
	}
	
	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle( bundle );
		m_RangedCooldown.Restore(bundle);
		m_SummonedAlly.Restore(bundle);
		if (m_SummonedAlly.Get()){
			setSummonedALly();
		}
	}
	@Override
	protected HashSet<Char> getPotentialAttackTargets() {
		if (!getRandomizerEnabled(RandomTraits.CHAOTIC_RAMPAGE, this)) {
			return super.getPotentialAttackTargets();
		}
		// exact same as super, but we want to target anything that's not neutral
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
			//look for mobs to attack
			for (Mob mob : Dungeon.level.mobs)
				if ((mob.alignment != Alignment.NEUTRAL && fieldOfView[mob.pos] && mob.invisible <= 0))
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
	
	public static class FireElemental extends Elemental implements CombatModifier.OnDamageEffect {
		
		{
			harmfulBuffs.add( com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Frost.class );
			harmfulBuffs.add( Chill.class );
			immunities.add(Burning.class);
		}
		@Override
		public Constants.mobs.mobsBase GetConstants() { return Constants.mobs.fireelemental; }

		@Override
		public void onDamage(AttackContext context, int damageDealt) {
			if (context.distance == 1) { // Melee
				if (getRandomizerEnabled(RandomTraits.INSULATION, this)) {
					return;
				}

				if (Random.Int( 2 ) == 0 && !Dungeon.level.water[context.defenderPosition] || getRandomizerEnabled(RandomTraits.ELEMENTAL_OVERLOAD, this)) {
					Buff.affect( context.defender, Burning.class ).reignite( context.defender );
					if (context.defender.sprite.visible) Splash.at( context.defender.sprite.center(), sprite.blood(), 5);
				}
			} else {
				if (!Dungeon.level.water[context.defenderPosition]) {
					Buff.affect( context.defender, Burning.class ).reignite( context.defender, 4f );
				}
				if (context.defender.sprite.visible) Splash.at( context.defender.sprite.center(), sprite.blood(), 5);
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

		@Override
		protected EnumSet<DamageType> GetHarmfulBuffDamageType() {
			return DamageType.of(DamageType.COLD);
		}
	}
	
	//used in wandmaker quest, a fire elemental with lower ACC/EVA/DMG, no on-hit fire
	// and a unique 'fireball' style ranged attack, which can be dodged
	public static class NewbornFireElemental extends FireElemental {
		@Override
		public Constants.mobs.mobsBase GetConstants() { return Constants.mobs.newbornfireelemental; }

		@Override
		protected boolean act() {
			//fire a charged attack instead of any other action, as long as it is possible to do so
			if (m_TargetingPosition.Get() != -1 && state == HUNTING){
				//account for bolt hitting walls, in case position suddenly changed
				m_TargetingPosition.Set(new Ballistica( pos, m_TargetingPosition.Get(), Ballistica.STOP_SOLID | Ballistica.STOP_TARGET ).collisionPos);
				if (sprite != null && (sprite.visible || Dungeon.level.heroFOV[m_TargetingPosition.Get()])) {
					sprite.zap( m_TargetingPosition.Get() );
					return false;
				} else {
					zap();
					return true;
				}
			} else {

				if (state != HUNTING){
					m_TargetingPosition.Set(-1);
				}

				return super.act();
			}
		}

		@Override
		protected boolean canAttack( Char enemy ) {
			if (super.canAttack(enemy)){
				return true;
			} else {
				return m_RangedCooldown.Get() < 0 && new Ballistica( pos, enemy.pos, Ballistica.STOP_SOLID | Ballistica.STOP_TARGET ).collisionPos == enemy.pos;
			}
		}

		public boolean doAttack(Char enemy) {

			if (m_RangedCooldown.Get() > 0) {

				return super.doAttack( enemy );

			} else if (new Ballistica( pos, enemy.pos, Ballistica.STOP_SOLID | Ballistica.STOP_TARGET ).collisionPos == enemy.pos) {

				//set up an attack for next turn
				ArrayList<Integer> candidates = new ArrayList<>();
				for (int i : PathFinder.NEIGHBOURS8){
					int target = enemy.pos + i;
					if (target != pos && new Ballistica(pos, target, Ballistica.STOP_SOLID | Ballistica.STOP_TARGET).collisionPos == target){
						candidates.add(target);
					}
				}

				if (!candidates.isEmpty()){
					m_TargetingPosition.Set(Random.element(candidates));

					for (int i : PathFinder.NEIGHBOURS9){
						if (!Dungeon.level.solid[m_TargetingPosition.Get() + i]) {
							sprite.parent.addToBack(new TargetedCell(m_TargetingPosition.Get() + i, 0xFF0000));
						}
					}

					GLog.n(Messages.get(this, "charging"));
					spend(GameMath.gate(attackDelay(), (int)Math.ceil(Dungeon.hero.cooldown()), 3*attackDelay()));
					Dungeon.hero.interrupt();
					return true;
				} else {
					m_RangedCooldown.Set(1);
					return super.doAttack(enemy);
				}


			} else {

				if (sprite != null && (sprite.visible || Dungeon.level.heroFOV[m_TargetingPosition.Get()])) {
					sprite.zap( m_TargetingPosition.Get() );
					return false;
				} else {
					zap();
					return true;
				}

			}
		}

		@Override
		protected void zap() {
			if (m_TargetingPosition.Get() != -1) {
				spend(1f);

				Invisibility.dispel(this);

				for (int i : PathFinder.NEIGHBOURS9) {
					if (!Dungeon.level.solid[m_TargetingPosition.Get() + i]) {
						CellEmitter.get(m_TargetingPosition.Get() + i).burst(ElmoParticle.FACTORY, 5);
						if (Dungeon.level.water[m_TargetingPosition.Get() + i]) {
							GameScene.add(Blob.seed(m_TargetingPosition.Get() + i, 2, Fire.class));
						} else {
							GameScene.add(Blob.seed(m_TargetingPosition.Get() + i, 8, Fire.class));
						}

						Char target = Actor.findChar(m_TargetingPosition.Get() + i);
						if (target != null && target != this) {
							Buff.affect(target, Burning.class).reignite(target);
							if (target == Dungeon.hero){
								Statistics.questScores[1] -= 200;
							}
						}
					}
				}
				Sample.INSTANCE.play(Assets.Sounds.BURNING);
			}

			m_TargetingPosition.Set(-1);
			m_RangedCooldown.Set(damageRoll(AttackContext.AttackType.RANGED, false));
		}

		@Override
		public int attackSkill() {
			if (!m_SummonedAlly.Get()) {
				return 15;
			} else {
				return super.attackSkill();
			}
		}

		@Override
		public int damageRoll(AttackContext.AttackType type, boolean isMaxDamage) {
			if (!m_SummonedAlly.Get()) {
				return super.damageRoll(AttackContext.AttackType.MELEE, isMaxDamage);
			} else {
				return super.damageRoll(type, isMaxDamage);
			}
		}

		@Override
		public void die(Object cause) {
			super.die(cause);
			if (alignment == Alignment.ENEMY) {
				Dungeon.level.drop( new Embers(), pos ).sprite.drop();
				//assign score here as player may choose to keep the embers
				Statistics.questScores[1] += 2000;
				Game.runOnRenderThread(new Callback() {
					@Override
					public void call() {
						Music.INSTANCE.fadeOut(1f, new Callback() {
							@Override
							public void call() {
								if (Dungeon.level != null) {
									Dungeon.level.playLevelMusic();
								}
							}
						});
					}
				});
			}
		}

		@Override
		public boolean reset() {
			return !m_SummonedAlly.Get();
		}

		@Override
		public String description(boolean forceNoMonsterUnknown) {
			String desc = super.description(forceNoMonsterUnknown);

			if (m_SummonedAlly.Get()){
				desc += " " + Messages.get(this, "desc_ally");
			} else {
				desc += " " + Messages.get(this, "desc_boss");
			}

			return desc;
		}

		private BundleableProperty.Int m_TargetingPosition = new BundleableProperty.Int("targeting_pos", -1);

		@Override
		public void storeInBundle(Bundle bundle) {
			super.storeInBundle(bundle);
			m_TargetingPosition.Store(bundle);
		}

		@Override
		public void restoreFromBundle(Bundle bundle) {
			super.restoreFromBundle(bundle);
			m_TargetingPosition.Restore(bundle);
		}
	}

	//not a miniboss, no ranged attack, otherwise a newborn elemental
	public static class AllyNewBornElemental extends NewbornFireElemental {

		{
			m_RangedCooldown.Set(Integer.MAX_VALUE);
		}

		@Override
		public Constants.mobs.mobsBase GetConstants() { return Constants.mobs.allynewbornelemental; }
	}
	
	public static class FrostElemental extends Elemental implements CombatModifier.OnDamageEffect {
		
		{
			harmfulBuffs.add( Burning.class );
		}
		@Override
		public Constants.mobs.mobsBase GetConstants() { return Constants.mobs.frostelemental; }

		@Override
		public float GetLootChance(int slot) {
			float chance = super.GetLootChance(slot);

			if (getRandomizerEnabled(RandomTraits.GENEROUS_SPIRIT, this))
				return chance * 6.0f;
			return chance;
		}

		@Override
		public void onDamage(AttackContext context, int damageDealt) {
			if (context.distance == 1) { // Melee
				if (getRandomizerEnabled(RandomTraits.INSULATION, this)) {
					return;
				}

				if (Random.Int( 3 ) == 0 || Dungeon.level.water[enemy.pos] || getRandomizerEnabled(RandomTraits.ELEMENTAL_OVERLOAD, this)) {
					Freezing.freeze( enemy.pos );
					if (enemy.sprite.visible) Splash.at( enemy.sprite.center(), sprite.blood(), 5);
				}
			} else {
				Freezing.freeze( enemy.pos );
				if (enemy.sprite.visible) Splash.at( enemy.sprite.center(), sprite.blood(), 5);
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

		@Override
		protected EnumSet<DamageType> GetHarmfulBuffDamageType() {
			return DamageType.of(DamageType.FIRE);
		}
	}
	
	public static class ShockElemental extends Elemental implements CombatModifier.OnDamageEffect {

		@Override
		public Constants.mobs.mobsBase GetConstants() { return Constants.mobs.shockelemental; }

		@Override
		protected EnumSet<DamageType> GetHarmfulBuffDamageType() {
			return DamageType.of(DamageType.NONE);
		}

		@Override
		public float GetLootChance(int slot) {
			float chance = super.GetLootChance(slot);

			if (getRandomizerEnabled(RandomTraits.GENEROUS_SPIRIT, this))
				return chance * 4.0f;
			return chance;
		}

		@Override
		public void onDamage(AttackContext context, int damageDealt) {
			if (context.distance == 1) { // Melee
				ArrayList<Char> affected = new ArrayList<>();
				ArrayList<Lightning.Arc> arcs = new ArrayList<>();
				arc( this, enemy, 2, affected, arcs );

				if (!Dungeon.level.water[enemy.pos]) {
					affected.remove( enemy );
				}

				for (Char ch : affected) {
					ch.Damage( Math.round( damageDealt * 0.4f ), new Shocking(), GetDamageType(AttackContext.AttackType.RANGED) );
					if (ch == Dungeon.hero && !ch.isAlive()){
						Dungeon.fail(this);
						GLog.n( Messages.capitalize(Messages.get(Char.class, "kill", name(false))) );
					}
				}

				boolean visible = sprite.visible || enemy.sprite.visible;
				for (Char ch : affected){
					if (ch.sprite.visible) visible = true;
				}

				if (visible) {
					sprite.parent.addToFront(new Lightning(arcs, null));
					Sample.INSTANCE.play(Assets.Sounds.LIGHTNING);
				}
			} else {
				Buff.affect( enemy, Blindness.class, Blindness.DURATION/2f );
				if (enemy == Dungeon.hero) {
					GameScene.flash(0x80FFFFFF);
				}
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

		private void arc( Char attacker, Char defender, int dist, ArrayList<Char> affected, ArrayList<Lightning.Arc> arcs ) {

			defender.sprite.centerEmitter().burst(SparkParticle.FACTORY, 3);
			defender.sprite.flash();

			ArrayList<Char> hitThisArc = new ArrayList<>();
			PathFinder.buildDistanceMap( defender.pos, BArray.not( Dungeon.level.solid, null ), dist );
			for (int i = 0; i < PathFinder.distance.length; i++) {
				if (PathFinder.distance[i] < Integer.MAX_VALUE) {
					Char n = Actor.findChar(i);
					if (n != null && n != attacker && !affected.contains(n)) {
						hitThisArc.add(n);
					}
				}
			}

			affected.addAll(hitThisArc);
			for (Char hit : hitThisArc){
				arcs.add(new Lightning.Arc(defender.sprite.center(), hit.sprite.center()));
				arc(attacker, hit, (Dungeon.level.water[hit.pos] && !hit.flying) ? 2 : 1, affected, arcs);
			}
		}
	}
	
	public static class ChaosElemental extends Elemental implements CombatModifier.OnDamageEffect, CombatModifier.AccuracyModifier {

		@Override
		public Constants.mobs.mobsBase GetConstants() { return Constants.mobs.chaoselemental; }

		@Override
		protected EnumSet<DamageType> GetHarmfulBuffDamageType() {
			return DamageType.of(DamageType.NONE);
		}

		@Override
		public void onZapComplete() {
			zap();
			//next(); triggers after wand effect
		}

		@Override
		public void onDamage(AttackContext context, int damageDealt) {
			if (context.distance == 1) { // Melee
				Ballistica aim = new Ballistica(pos, enemy.pos, Ballistica.STOP_TARGET);
				//TODO shortcutting the fx seems fine for now but may cause problems with new cursed effects
				//of course, not shortcutting it means actor ordering issues =S
				CursedWand.randomValidEffect(null, this, aim, false).effect(null, this, aim, false);
			} else {
				CursedWand.cursedZap(null, this, new Ballistica(pos, enemy.pos, Ballistica.STOP_TARGET), new Callback() {
					@Override
					public void call() {
						next();
					}
				});
			}
		}

		@Override
		public float modifyAccuracy(AttackContext context, float currentAccuracy) {
			if (context.distance > 1) {
				// Ranged attacks always hit
				return Char.INFINITE_ACCURACY;
			}
			return currentAccuracy;
		}

		@Override
		public int priority() {
			return Priority.NORMAL;
		}

		@Override
		public boolean appliesTo(AttackContext context) {
			return context.attacker == this;
		}
	}
	
	public static Class<? extends Elemental> random(){
		float altChance = 1/50f * RatSkull.exoticChanceMultiplier();
		if (Random.Float() < altChance){
			return ChaosElemental.class;
		}
		
		float roll = Random.Float();
		if (roll < 0.4f){
			return FireElemental.class;
		} else if (roll < 0.8f){
			return FrostElemental.class;
		} else {
			return ShockElemental.class;
		}
	}

	public enum RandomTraits {
		ELEMENTAL_OVERLOAD, RANGED_MASTERY, EMPOWERED_FORM, GENEROUS_SPIRIT, CHAOTIC_RAMPAGE, INSULATION
	}

	public static boolean getRandomizerEnabled(RandomTraits r, Elemental e) {
		if (e instanceof NewbornFireElemental) {
			return false;
		}
		switch (r) {
			case ELEMENTAL_OVERLOAD: return Randomizer.getCreatureBuff(Elemental.class) == 1;
			case RANGED_MASTERY: return Randomizer.getCreatureBuff(Elemental.class) == 2;
			case EMPOWERED_FORM: return Randomizer.getCreatureBuff(Elemental.class) == 3;
			case GENEROUS_SPIRIT: return Randomizer.getCreatureNerf(Elemental.class) == 1;
			case CHAOTIC_RAMPAGE: return Randomizer.getCreatureNerf(Elemental.class) == 2;
			case INSULATION: return Randomizer.getCreatureNerf(Elemental.class) == 3;
		}
		return false;
	}
}