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
import com.shatteredpixel.shatteredpixeldungeon.Constants;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.Randomizer;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Electricity;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Sleep;
import com.shatteredpixel.shatteredpixeldungeon.combat.AttackContext;
import com.shatteredpixel.shatteredpixeldungeon.combat.AttackResult;
import com.shatteredpixel.shatteredpixeldungeon.combat.CombatResolver;
import com.shatteredpixel.shatteredpixeldungeon.combat.DamageType;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.SparkParticle;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.utils.BundleableProperty;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;
import com.watabou.utils.PathFinder;

public class DM100 extends Mob implements Callback {

	private static final float TIME_TO_ZAP	= 1f;

	@Override
	public Constants.mobs.mobsBase GetConstants() { return Constants.mobs.dm100; }

	private BundleableProperty.Bool m_SeenPlayer = new BundleableProperty.Bool("seen_player", false);
	private BundleableProperty.Bool m_HasZapped = new BundleableProperty.Bool("has_zapped", false);

	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
		m_SeenPlayer.Store(bundle);
		m_HasZapped.Store(bundle);
	}

	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle( bundle );
		m_SeenPlayer.Restore(bundle);
		m_HasZapped.Restore(bundle);
	}

	public void notice() {
		super.notice();
		if (!m_SeenPlayer.Get() && getRandomizerEnabled(RandomTraits.SECURITY_NETWORK)) {
			if (fieldOfView[Dungeon.hero.pos] && Dungeon.hero.invisible <= 0) {
				m_SeenPlayer.Set(true);
				CellEmitter.center( pos ).start( Speck.factory( Speck.SCREAM ), 0.3f, 3 );
				Sample.INSTANCE.play( Assets.Sounds.ALERT );

				for (Mob mob : Dungeon.level.mobs) {
					mob.beckon( pos );
				}
			}
		}
	}

	{
		if (getRandomizerEnabled(RandomTraits.COMBAT_READY)) {
			immunities.add(Sleep.class);
		}
	}

	@Override
	protected boolean canAttack( Char enemy ) {
		if (getRandomizerEnabled(RandomTraits.PASSIVE_PROTOCOL)) {
			if (HP == GetMaxHP()) return false;
		}

		boolean canRanged = new Ballistica( pos, enemy.pos, Ballistica.MAGIC_BOLT).collisionPos == enemy.pos;
		if (getRandomizerEnabled(RandomTraits.POWER_CONSERVATION) && m_HasZapped.Get()) {
			canRanged = false;
		}

		return super.canAttack(enemy)
				|| canRanged;
	}
	
	//used so resistances can differentiate between melee and magical attacks
	public static class LightningBolt{}
	
	@Override
    public boolean doAttack(Char enemy) {

		if (Dungeon.level.adjacent( pos, enemy.pos )
				|| new Ballistica( pos, enemy.pos, Ballistica.MAGIC_BOLT).collisionPos != enemy.pos) {
			
			return super.doAttack( enemy );
			
		} else {
			if (getRandomizerEnabled(RandomTraits.POWER_CONSERVATION) && m_HasZapped.Get()) {
				return false;
			}
			
			spend( TIME_TO_ZAP );

			if (getRandomizerEnabled(RandomTraits.FAULTY_BATTERIES) && HP > 1) {
				HP /= 2;
			}
			m_HasZapped.Set(true);
			Invisibility.dispel(this);

			// Build attack context
			AttackContext context = new AttackContext.Builder(this, enemy)
					.attackType(AttackContext.AttackType.RANGED)
					.damageType(GetRangedDamageType())
					.build();

			// Resolve attack - this handles everything internally
			AttackResult result = CombatResolver.resolve(context);

			// Check result type for UI/feedback
			if (result.result == AttackResult.ResultType.MISS) {
				enemy.sprite.showStatus( CharSprite.NEUTRAL,  enemy.defenseVerb() );
			}
			else if (result.result == AttackResult.ResultType.HIT) {
				if (enemy.sprite.visible) {
					enemy.sprite.centerEmitter().burst(SparkParticle.FACTORY, 3);
					enemy.sprite.flash();
				}
				if (enemy == Dungeon.hero) {
					PixelScene.shake( 2, 0.3f );

					if (result.killed) {
						Badges.validateDeathFromEnemyMagic();
						Dungeon.fail( this );
						GLog.n( Messages.get(this, "zap_kill") );
					}
				}
			}

			if (sprite != null && (sprite.visible || enemy.sprite.visible)) {
				sprite.zap( enemy.pos );
				return false;
			} else {
				return true;
			}
		}
	}

	@Override
	//Always spends exactly the specified amount of time, regardless of time-influencing factors
	protected void spendConstant( float time ){
		int oldTime = (int)this.getTime(); // cut it off
		super.spendConstant(time);
		if (getRandomizerEnabled(RandomTraits.ELECTRICAL_AURA) && (int)(this.getTime()) > oldTime) { // we go up one turn
			auraDamage();
		}
	}

	private void auraDamage() {
		for (int i = 0; i < PathFinder.NEIGHBOURS8.length; i++) {
			if (pos + PathFinder.NEIGHBOURS8[i] < 0) continue;

			Char ch = findChar( pos + PathFinder.NEIGHBOURS8[i] );
			if (ch != null && ch.isAlive()) {
				if (ch.isImmune(Electricity.class)) {
					continue;
				}

				int damage = damageRoll(AttackContext.AttackType.RANGED, false);
				enemy.Damage( damage, new LightningBolt(), DamageType.of(DamageType.ELECTRICITY));
			}
		}
	}
	
	@Override
	public void call() {
		next();
	}

	public enum RandomTraits {
		ELECTRICAL_AURA, COMBAT_READY, SECURITY_NETWORK, FAULTY_BATTERIES, PASSIVE_PROTOCOL, POWER_CONSERVATION
	}

	public static boolean getRandomizerEnabled(RandomTraits r) {
		switch (r) {
			case ELECTRICAL_AURA: return Randomizer.getCreatureBuff(DM100.class) == 1;
			case COMBAT_READY: return Randomizer.getCreatureBuff(DM100.class) == 2;
			case SECURITY_NETWORK: return Randomizer.getCreatureBuff(DM100.class) == 3;
			case FAULTY_BATTERIES: return Randomizer.getCreatureNerf(DM100.class) == 1;
			case PASSIVE_PROTOCOL: return Randomizer.getCreatureNerf(DM100.class) == 2;
			case POWER_CONSERVATION: return Randomizer.getCreatureNerf(DM100.class) == 3;
		}
		return false;
	}
}