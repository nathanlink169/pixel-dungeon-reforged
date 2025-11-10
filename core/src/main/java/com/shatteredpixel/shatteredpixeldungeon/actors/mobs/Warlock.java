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
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.AscensionChallenge;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Degrade;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Slow;
import com.shatteredpixel.shatteredpixeldungeon.combat.AttackContext;
import com.shatteredpixel.shatteredpixeldungeon.combat.AttackResult;
import com.shatteredpixel.shatteredpixeldungeon.combat.CombatModifier;
import com.shatteredpixel.shatteredpixeldungeon.combat.CombatResolver;
import com.shatteredpixel.shatteredpixeldungeon.combat.DamageType;
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfHealing;
import com.shatteredpixel.shatteredpixeldungeon.items.stones.StoneOfAggression;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.WarlockSprite;
import com.shatteredpixel.shatteredpixeldungeon.utils.BundleableProperty;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;
import com.watabou.utils.Random;

public class Warlock extends Mob implements Callback, CombatModifier.OnHitEffect {
	
	private static final float TIME_TO_ZAP	= 1f;
	
	{
		WANDERING = new Wandering();
	}
	private BundleableProperty.Bool m_Dancing = new BundleableProperty.Bool("dancing", false);

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		m_Dancing.Store(bundle);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		m_Dancing.Restore(bundle);
	}

	@Override
	public Constants.mobs.mobsBase GetConstants() { return Constants.mobs.warlock; }

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
	
	public boolean doAttack(Char enemy) {

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

	@Override
	public void onHit(AttackContext context, int finalDamage) {
		if (m_Dancing.Get()) {
			setIsDancing(false);
		}
	}

	@Override
	public int priority() {
		return Priority.NORMAL;
	}

	@Override
	public boolean appliesTo(AttackContext context) {
		return context.defender == this;
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
		// Build attack context
		AttackContext context = new AttackContext.Builder(this, enemy)
				.attackType(AttackContext.AttackType.RANGED)
				.damageType(GetRangedDamageType())
				.build();

		// Resolve attack - this handles EVERYTHING internally
		AttackResult result = CombatResolver.resolve(context);
		if (result.result == AttackResult.ResultType.HIT) {
			//TODO would be nice for this to work on ghost/statues too
			if (enemy == Dungeon.hero && Random.Int( 2 ) == 0) {
				Buff.prolong( enemy, Degrade.class, Degrade.DURATION ).poweredUp = getRandomizerEnabled(RandomTraits.ARCANE_MASTERY);
				if (getRandomizerEnabled(RandomTraits.HINDERING_HEX)) {
					Buff.prolong( enemy, Slow.class, Slow.DURATION );
				}
				Sample.INSTANCE.play( Assets.Sounds.DEGRADE );
			}
			
			int dmg = damageRoll(AttackContext.AttackType.RANGED, false);
			dmg = Math.round(dmg * AscensionChallenge.statModifier(this));

			//logic for DK taking 1/2 damage from aggression stoned minions
			if ( enemy.buff(StoneOfAggression.Aggression.class) != null
					&& enemy.alignment == alignment
					&& (Char.hasProp(enemy, Property.BOSS) || Char.hasProp(enemy, Property.MINIBOSS))){
				dmg *= 0.5f;
			}

			enemy.Damage( dmg, new DarkBolt(), GetRangedDamageType() );
			
			if (enemy == Dungeon.hero && !enemy.isAlive()) {
				Badges.validateDeathFromEnemyMagic();
				Dungeon.fail( this );
				GLog.n( Messages.get(this, "bolt_kill") );
			}
		} else {
			enemy.sprite.showStatus( CharSprite.NEUTRAL,  enemy.defenseVerb() );
		}
	}

	private void setIsDancing(boolean isDancing) {
		m_Dancing.Set(isDancing);
		if (isDancing) {
			((WarlockSprite)sprite).dance();
		} else {
			sprite.idle();
		}
	}

	@Override
	public void beckon( int cell ) {
		if (!m_Dancing.Get()) {
			super.beckon(cell);
		}
	}

	@Override
	public void notice() {
		if (!m_Dancing.Get()) {
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
	public Item createLoot(int itemSlot){

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
    public boolean getCloser(int target) {
		if (state == HUNTING && getRandomizerEnabled(RandomTraits.COWARDLY_CASTER)) {
			return m_EnemySeen.Get() && getFurther( target );
		} else {
			return super.getCloser( target );
		}
	}

	private static class Wandering extends Mob.Wandering{

		@Override
		public boolean act(Mob mob, boolean enemyInFOV, boolean justAlerted) {
			Warlock w = (Warlock) mob;
			if (!w.m_Dancing.Get()) {
				if (getRandomizerEnabled(RandomTraits.DANCE_FEVER)) {
					if (!enemyInFOV && !justAlerted && Random.Int(50) == 0) {
						w.setIsDancing(true);
						w.spend( TICK );
						return true;
					}
					return super.act(w, enemyInFOV, justAlerted);
				}
				return super.act(w, enemyInFOV, justAlerted);
			}
			if (!((WarlockSprite)w.sprite).isDancing()) {
				((WarlockSprite) w.sprite).dance();
			}
			w.spend( TICK );
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