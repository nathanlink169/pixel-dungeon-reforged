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
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.Randomizer;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Electricity;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.AscensionChallenge;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Charm;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Chill;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Cripple;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Frost;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Roots;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Sleep;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Slow;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Terror;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Vertigo;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.SparkParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfLivingEarth;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.plants.Earthroot;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.DM100Sprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.RatSprite;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

public class DM100 extends Mob implements Callback {

	private static final float TIME_TO_ZAP	= 1f;
	
	{
		HP = HT = 20;
		defenseSkill = 8;
		
		EXP = 6;
		maxLvl = 13;
		
		loot = Generator.Category.SCROLL;
		lootChance = 0.25f;
		
		properties.add(Property.ELECTRIC);
		properties.add(Property.INORGANIC);
	}
	@Override
	public Class<? extends CharSprite> GetSpriteClass() {

		return DM100Sprite.class;
	}

	private boolean seenPlayer = false;
	private boolean hasZapped = false;

	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
		bundle.put( SEEN_PLAYER, seenPlayer);
		bundle.put( HAS_ZAPPED, hasZapped );
	}

	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle( bundle );
		seenPlayer = bundle.getBoolean( SEEN_PLAYER );
		hasZapped = bundle.getBoolean( HAS_ZAPPED );
	}

	public void notice() {
		super.notice();
		if (!seenPlayer && getRandomizerEnabled(RandomTraits.SECURITY_NETWORK)) {
			if (fieldOfView[Dungeon.hero.pos] && Dungeon.hero.invisible <= 0) {
				seenPlayer = true;
				CellEmitter.center( pos ).start( Speck.factory( Speck.SCREAM ), 0.3f, 3 );
				Sample.INSTANCE.play( Assets.Sounds.ALERT );

				for (Mob mob : Dungeon.level.mobs) {
					mob.beckon( pos );
				}
			}
		}
	}

	private static final String SEEN_PLAYER = "seen_player";
	private static final String HAS_ZAPPED = "has_zapped";

	{
		if (getRandomizerEnabled(RandomTraits.COMBAT_READY)) {
			immunities.add(Sleep.class);
		}
	}
	
	@Override
	public int damageRoll(boolean isMaxDamage) {
		if (isMaxDamage) return 8;
		return Random.NormalIntRange( 2, 8 );
	}
	
	@Override
	public int attackSkill( Char target ) {
		return 11;
	}
	
	@Override
	public int drRoll() {
		return super.drRoll() + Random.NormalIntRange(0, 4);
	}

	@Override
	protected boolean canAttack( Char enemy ) {
		if (getRandomizerEnabled(RandomTraits.PASSIVE_PROTOCOL)) {
			if (HP == HT) return false;
		}

		boolean canRanged = new Ballistica( pos, enemy.pos, Ballistica.MAGIC_BOLT).collisionPos == enemy.pos;
		if (getRandomizerEnabled(RandomTraits.POWER_CONSERVATION) && hasZapped) {
			canRanged = false;
		}

		return super.canAttack(enemy)
				|| canRanged;
	}
	
	//used so resistances can differentiate between melee and magical attacks
	public static class LightningBolt{}
	
	@Override
	protected boolean doAttack( Char enemy ) {

		if (Dungeon.level.adjacent( pos, enemy.pos )
				|| new Ballistica( pos, enemy.pos, Ballistica.MAGIC_BOLT).collisionPos != enemy.pos) {
			
			return super.doAttack( enemy );
			
		} else {
			if (getRandomizerEnabled(RandomTraits.POWER_CONSERVATION) && hasZapped) {
				return false;
			}
			
			spend( TIME_TO_ZAP );

			if (getRandomizerEnabled(RandomTraits.FAULTY_BATTERIES) && HP > 1) {
				HP /= 2;
			}
			hasZapped = true;

			Invisibility.dispel(this);
			if (hit( this, enemy, true )) {
				int dmg = Random.NormalIntRange(3, 10);
				dmg = Math.round(dmg * AscensionChallenge.statModifier(this));
				enemy.damage( dmg, new LightningBolt() );

				if (enemy.sprite.visible) {
					enemy.sprite.centerEmitter().burst(SparkParticle.FACTORY, 3);
					enemy.sprite.flash();
				}
				
				if (enemy == Dungeon.hero) {
					
					PixelScene.shake( 2, 0.3f );
					
					if (!enemy.isAlive()) {
						Badges.validateDeathFromEnemyMagic();
						Dungeon.fail( this );
						GLog.n( Messages.get(this, "zap_kill") );
					}
				}
			} else {
				enemy.sprite.showStatus( CharSprite.NEUTRAL,  enemy.defenseVerb() );
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
		if (getRandomizerEnabled(RandomTraits.ELECTRICAL_AURA) && this.getTime() > oldTime) { // we go up one turn
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

				int damage = Math.round(Random.NormalIntRange(1, 8));
				enemy.damage( damage, new LightningBolt() );
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