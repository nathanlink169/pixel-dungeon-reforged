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
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.shatteredpixel.shatteredpixeldungeon.combat.AttackContext;
import com.shatteredpixel.shatteredpixeldungeon.combat.AttackResult;
import com.shatteredpixel.shatteredpixeldungeon.combat.CombatModifier;
import com.shatteredpixel.shatteredpixeldungeon.combat.CombatResolver;
import com.shatteredpixel.shatteredpixeldungeon.combat.DamageType;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.PurpleParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.Dewdrop;
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.stones.StoneOfAggression;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfDisintegration;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.DisintegrationTrap;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.EyeSprite;
import com.shatteredpixel.shatteredpixeldungeon.utils.BundleableProperty;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.utils.Bundle;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

public class Eye extends Mob implements CombatModifier.AccuracyModifier, CombatModifier.PostArmorDamageModifier {
	{
		HUNTING = new Hunting();
	}

	@Override
	public Constants.mobs.mobsBase GetConstants() { return Constants.mobs.eye; }

	@Override
	public int GetMaxHP() {
		return (int) (super.GetMaxHP() * (getRandomizerEnabled(RandomTraits.DURABLE_EYE) ? 15.0f / 11.0f : 1.0f));
	}
	
	private Ballistica beam;

	@Override
	protected boolean canAttack( Char enemy ) {

		if (m_BeamCooldown.Get() == 0) {
			Ballistica aim = new Ballistica(pos, enemy.pos, Ballistica.STOP_SOLID);

			if (enemy.invisible == 0 && !isCharmedBy(enemy) && fieldOfView[enemy.pos]
					&& (super.canAttack(enemy) || aim.subPath(1, aim.dist).contains(enemy.pos)) &&
					!(getRandomizerEnabled(RandomTraits.MYOPIC_VISION) && distance(enemy) > 3)){
				beam = aim;
				m_BeamTarget.Set(enemy.pos);
				return true;
			} else {
				//if the beam is charged, it has to attack, will aim at previous location of target.
				return m_BeamCharged.Get();
			}
		} else {
			return super.canAttack(enemy);
		}
	}

	@Override
	protected boolean act() {
		if (m_BeamCharged.Get() && state != HUNTING){
			m_BeamCharged.Set(false);
			sprite.idle();
		}
		if (beam == null && m_BeamTarget.Get() != -1) {
			beam = new Ballistica(pos, m_BeamTarget.Get(), Ballistica.STOP_SOLID);
			sprite.turnTo(pos, m_BeamTarget.Get());
		}
		if (m_BeamCooldown.Get() > 0)
			m_BeamCooldown.Decrement();
		return super.act();
	}

	@Override
    public boolean doAttack(Char enemy) {

		beam = new Ballistica(pos, m_BeamTarget.Get(), Ballistica.STOP_SOLID);
		if (m_BeamCooldown.Get() > 0 || (!m_BeamCharged.Get() && !beam.subPath(1, beam.dist).contains(enemy.pos))) {
			return super.doAttack(enemy);
		} else if (!m_BeamCharged.Get()){
			((EyeSprite)sprite).charge( enemy.pos );
			if (getRandomizerEnabled(RandomTraits.RAPID_CHARGE)) {
				spend (attackDelay());
			} else if (getRandomizerEnabled(RandomTraits.SLUGGISH_CHARGE)) {
				spend (attackDelay() * 4f);
			} else {
				spend(attackDelay() * 2f);
			}
			m_BeamCharged.Set(true);
			return true;
		} else {

			spend( attackDelay() );
			
			if (Dungeon.level.heroFOV[pos] || Dungeon.level.heroFOV[beam.collisionPos] ) {
				sprite.zap( beam.collisionPos );
				return false;
			} else {
				sprite.idle();
				deathGaze();
				return true;
			}
		}

	}

	@Override
	public void die(Object cause) {
		flying = false;
		super.die(cause);

		//generates an average of 1 dew, 0.25 seeds, and 0.25 stones
		Item loot;
		switch(Random.Int(4)){
			case 0: case 1: default:
				loot = new Dewdrop();
				int ofs;
				do {
					ofs = PathFinder.NEIGHBOURS8[Random.Int(8)];
				} while (Dungeon.level.solid[pos + ofs] && !Dungeon.level.passable[pos + ofs]);
				if (Dungeon.level.heaps.get(pos+ofs) == null) {
					Dungeon.level.drop(new Dewdrop(), pos + ofs).sprite.drop(pos);
				} else {
					Dungeon.level.drop(new Dewdrop(), pos + ofs).sprite.drop(pos + ofs);
				}
				break;
			case 2:
				loot = Generator.randomUsingDefaults(Generator.Category.SEED);
				break;
			case 3:
				loot = Generator.randomUsingDefaults(Generator.Category.STONE);
				break;
		}
		if (loot != null) {
			Dungeon.level.drop(loot, pos).sprite.drop();
		}
	}

	@Override
	public float modifyAccuracy(AttackContext context, float currentAccuracy) {
		if (context.attacker == this) {
			if (getRandomizerEnabled(RandomTraits.UNDODGEABLE_BEAM)) {
				return Char.INFINITE_ACCURACY;
			} else if (getRandomizerEnabled(RandomTraits.PREDICTABLE_BEAM)) {
				return currentAccuracy * (2.0f / 3.0f);
			}
		}
		return currentAccuracy;
	}

	@Override
	public int priority() {
		return Priority.HIGH;
	}

	@Override
	public boolean appliesTo(AttackContext context) {
		return context.attacker == this || context.defender == this;
	}

	@Override
	public int modifyPostArmorDamage(AttackContext context, int currentDamage) {
		if (context.defender == this) {
			if (m_BeamCharged.Get()) {
				return currentDamage / 4;
			}
		}
		else if (context.attacker == this) {
			if ( context.defender.buff(StoneOfAggression.Aggression.class) != null
					&& context.defender.alignment == alignment
					&& (Char.hasProp(context.defender, Property.BOSS) || Char.hasProp(context.defender, Property.MINIBOSS))) {
				if (context.defender instanceof YogDzewa){
					return currentDamage / 4;
				}
				return currentDamage / 2;
			}
		}
		return currentDamage;
	}

	//used so resistances can differentiate between melee and magical attacks
	public static class DeathGaze{}

	public void deathGaze(){
		if (!m_BeamCharged.Get() || m_BeamCooldown.Get() > 0 || beam == null)
			return;

		m_BeamCharged.Set(false);
		m_BeamCooldown.Set(Random.IntRange(4, 6));

		boolean terrainAffected = false;

		Invisibility.dispel(this);
		for (int pos : beam.subPath(1, beam.dist)) {

			if (Dungeon.level.flamable[pos]) {

				Dungeon.level.destroy( pos );
				GameScene.updateMap( pos );
				terrainAffected = true;

			}

			Char ch = Actor.findChar( pos );
			if (ch == null) {
				continue;
			}

			// Build attack context
			AttackContext context = new AttackContext.Builder(this, ch)
					.attackType(AttackContext.AttackType.RANGED)
					.damageType(GetRangedDamageType())
					.build();

			// Resolve attack - this handles everything internally
			AttackResult result = CombatResolver.resolve(context);

			if (result.result == AttackResult.ResultType.HIT) {
				if (Dungeon.level.heroFOV[pos]) {
					ch.sprite.flash();
					CellEmitter.center( pos ).burst( PurpleParticle.BURST, Random.IntRange( 1, 2 ) );
				}

				if (result.killed && ch == Dungeon.hero) {
					Badges.validateDeathFromEnemyMagic();
					Dungeon.fail( this );
					GLog.n( Messages.get(this, "deathgaze_kill") );
				}
			} else {
				ch.sprite.showStatus( CharSprite.NEUTRAL,  ch.defenseVerb() );
			}
		}

		if (terrainAffected) {
			Dungeon.observe();
		}

		beam = null;
		m_BeamTarget.Set(-1);
	}

	public boolean GetBeamCharged() {
		return m_BeamCharged.Get();
	}

	private BundleableProperty.Int m_BeamTarget = new BundleableProperty.Int("beamTarget", -1);
	private BundleableProperty.Int m_BeamCooldown = new BundleableProperty.Int("beamCooldown", 0);
	private BundleableProperty.Bool m_BeamCharged = new BundleableProperty.Bool("beamCharged", false);

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		m_BeamTarget.Store(bundle);
		m_BeamCooldown.Store(bundle);
		m_BeamCharged.Store(bundle);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		m_BeamTarget.Restore(bundle);
		m_BeamCooldown.Restore(bundle);
		m_BeamCharged.Restore(bundle);
	}

	{
		resistances.add( WandOfDisintegration.class );
		resistances.add( DeathGaze.class );
		resistances.add( DisintegrationTrap.class );
	}

	private static class Hunting extends Mob.Hunting{
		@Override
		public boolean act(Mob mob, boolean enemyInFOV, boolean justAlerted) {
			Eye e = (Eye)mob;
			//even if enemy isn't seen, attack them if the beam is charged
			if (e.m_BeamCharged.Get() && e.enemy != null && e.canAttack(e.enemy)) {
				e.m_EnemySeen.Set(enemyInFOV);
				return e.doAttack(e.enemy);
			}
			return super.act(e, enemyInFOV, justAlerted);
		}
	}

	public enum RandomTraits {
		UNDODGEABLE_BEAM, RAPID_CHARGE, DURABLE_EYE, MYOPIC_VISION, SLUGGISH_CHARGE, PREDICTABLE_BEAM
	}

	public static boolean getRandomizerEnabled(RandomTraits r) {
		switch (r) {
			case UNDODGEABLE_BEAM: return Randomizer.getCreatureBuff(Eye.class) == 1;
			case RAPID_CHARGE: return Randomizer.getCreatureBuff(Eye.class) == 2;
			case DURABLE_EYE: return Randomizer.getCreatureBuff(Eye.class) == 3;
			case MYOPIC_VISION: return Randomizer.getCreatureNerf(Eye.class) == 1;
			case SLUGGISH_CHARGE: return Randomizer.getCreatureNerf(Eye.class) == 2;
			case PREDICTABLE_BEAM: return Randomizer.getCreatureNerf(Eye.class) == 3;
		}
		return false;
	}
}