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
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.AscensionChallenge;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Degrade;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Slow;
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfHealing;
import com.shatteredpixel.shatteredpixeldungeon.items.stones.StoneOfAggression;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.RatSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.WarlockSprite;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;
import com.watabou.utils.Random;

public class Warlock extends Mob implements Callback {
	
	private static final float TIME_TO_ZAP	= 1f;
	
	{
		HP = HT = 70;
		defenseSkill = 18;
		
		EXP = 11;
		maxLvl = 21;
		
		loot = Generator.Category.POTION;
		lootChance = 0.5f;

		properties.add(Property.UNDEAD);

		WANDERING = new Wandering();
	}

	private boolean dancing = false;
	private static final String DANCING = "dancing";

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put( DANCING, dancing);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		dancing = bundle.getBoolean(DANCING);
	}

	@Override
	public Class<? extends CharSprite> GetSpriteClass() {

		return WarlockSprite.class;
	}
	
	@Override
	public int damageRoll(boolean isMaxDamage) {
		if (isMaxDamage) return 18;
		return Random.NormalIntRange( 12, 18 );
	}
	
	@Override
	public int attackSkill( Char target ) {
		return 25;
	}
	
	@Override
	public int drRoll() {
		return super.drRoll() + Random.NormalIntRange(0, 8);
	}
	
	@Override
	protected boolean canAttack( Char enemy ) {
		if (getRandomizerEnabled(RandomTraits.SHORT_RANGE)) {
			if (distance(enemy) > 3) {
				return false;
			}
		}
		return super.canAttack(enemy)
				|| new Ballistica( pos, enemy.pos, Ballistica.MAGIC_BOLT).collisionPos == enemy.pos;
	}
	
	protected boolean doAttack( Char enemy ) {

		if (Dungeon.level.adjacent( pos, enemy.pos )
				|| new Ballistica( pos, enemy.pos, Ballistica.MAGIC_BOLT).collisionPos != enemy.pos) {
			
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
	
	//used so resistances can differentiate between melee and magical attacks
	public static class DarkBolt{}
	
	protected void zap() {
		if (getRandomizerEnabled(RandomTraits.SPELL_FATIGUE)) {
			spend(TIME_TO_ZAP * 2.0f);
		} else {
			spend(TIME_TO_ZAP);
		}

		Invisibility.dispel(this);
		Char enemy = this.enemy;
		if (hit( this, enemy, true )) {
			//TODO would be nice for this to work on ghost/statues too
			if (enemy == Dungeon.hero && Random.Int( 2 ) == 0) {
				Buff.prolong( enemy, Degrade.class, Degrade.DURATION ).poweredUp = getRandomizerEnabled(RandomTraits.ARCANE_MASTERY);
				if (getRandomizerEnabled(RandomTraits.HINDERING_HEX)) {
					Buff.prolong( enemy, Slow.class, Slow.DURATION );
				}
				Sample.INSTANCE.play( Assets.Sounds.DEGRADE );
			}
			
			int dmg = Random.NormalIntRange( 12, 18 );
			dmg = Math.round(dmg * AscensionChallenge.statModifier(this));

			//logic for DK taking 1/2 damage from aggression stoned minions
			if ( enemy.buff(StoneOfAggression.Aggression.class) != null
					&& enemy.alignment == alignment
					&& (Char.hasProp(enemy, Property.BOSS) || Char.hasProp(enemy, Property.MINIBOSS))){
				dmg *= 0.5f;
			}

			enemy.damage( dmg, new DarkBolt() );
			
			if (enemy == Dungeon.hero && !enemy.isAlive()) {
				Badges.validateDeathFromEnemyMagic();
				Dungeon.fail( this );
				GLog.n( Messages.get(this, "bolt_kill") );
			}
		} else {
			enemy.sprite.showStatus( CharSprite.NEUTRAL,  enemy.defenseVerb() );
		}
	}

	@Override
	public int defenseProc(Char enemy, int damage) {
		if (dancing) {
			setIsDancing(false);
		}

		return super.defenseProc(enemy, damage);
	}

	private void setIsDancing(boolean isDancing) {
		if (isDancing) {
			((WarlockSprite)sprite).dance();
			dancing = true;
		} else {
			sprite.idle();
			dancing = false;
		}
	}

	@Override
	public void beckon( int cell ) {
		if (!dancing) {
			super.beckon(cell);
		}
	}

	@Override
	public void notice() {
		if (!dancing) {
			super.notice();
		}
	}
	
	public void onZapComplete() {
		zap();
		next();
	}
	
	@Override
	public void call() {
		next();
	}

	@Override
	public Item createLoot(){

		// 1/6 chance for healing, scaling to 0 over 8 drops
		if (Random.Int(3) == 0 && Random.Int(8) > Dungeon.LimitedDrops.WARLOCK_HP.count ){
			Dungeon.LimitedDrops.WARLOCK_HP.count++;
			return new PotionOfHealing();
		} else {
			Item i;
			do {
				i = Generator.randomUsingDefaults(Generator.Category.POTION);
			} while (i instanceof PotionOfHealing);
			return i;
		}
	}

	@Override
	protected boolean getCloser( int target ) {
		if (state == HUNTING && getRandomizerEnabled(RandomTraits.COWARDLY_CASTER)) {
			return enemySeen && getFurther( target );
		} else {
			return super.getCloser( target );
		}
	}

	private class Wandering extends Mob.Wandering{

		@Override
		public boolean act(boolean enemyInFOV, boolean justAlerted) {
			if (!dancing) {
				if (getRandomizerEnabled(RandomTraits.DANCE_FEVER)) {
					if (!enemyInFOV && !justAlerted && Random.Int(50) == 0) {
						setIsDancing(true);
						spend( TICK );
						return true;
					}
					return super.act(enemyInFOV, justAlerted);
				}
				return super.act(enemyInFOV, justAlerted);
			}
			if (!((WarlockSprite)sprite).isDancing()) {
				((WarlockSprite) sprite).dance();
			}
			spend( TICK );
			return true;
		}
	}

	public enum RandomTraits {
		HINDERING_HEX, COWARDLY_CASTER, ARCANE_MASTERY, SHORT_RANGE, SPELL_FATIGUE, DANCE_FEVER
	}

	public static boolean getRandomizerEnabled(RandomTraits r) {
		switch (r) {
			case HINDERING_HEX: return Randomizer.getCreatureBuff(Warlock.class) == 1;
			case COWARDLY_CASTER: return Randomizer.getCreatureBuff(Warlock.class) == 2;
			case ARCANE_MASTERY: return Randomizer.getCreatureBuff(Warlock.class) == 3;
			case SHORT_RANGE: return Randomizer.getCreatureNerf(Warlock.class) == 1;
			case SPELL_FATIGUE: return Randomizer.getCreatureNerf(Warlock.class) == 2;
			case DANCE_FEVER: return Randomizer.getCreatureNerf(Warlock.class) == 3;
		}
		return false;
	}
}