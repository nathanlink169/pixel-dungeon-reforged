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
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.AscensionChallenge;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.ShieldBuff;
import com.shatteredpixel.shatteredpixeldungeon.effects.FloatingText;
import com.shatteredpixel.shatteredpixeldungeon.effects.SpellSprite;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.PlateArmor;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.ScaleArmor;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.Chasm;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

public class Brute extends Mob {
	@Override
	public Constants.mobs.mobsBase GetConstants() { return Constants.mobs.brute; }
	
	protected boolean hasRaged = false;

	@Override
	public int minDamage(AttackType type) {
		int damageMultiplier = 1;
		if (buff(BruteRage.class) != null) {
			if (getRandomizerEnabled(RandomTraits.DYING_BREATH)) {
				damageMultiplier = 0;
			} else {
				damageMultiplier = 3;
			}
		}

		return super.minDamage(type) * damageMultiplier;
	}

	@Override
	public int maxDamage(AttackType type) {
		float damageMultiplier = 1;
		if (buff(BruteRage.class) != null) {
			if (getRandomizerEnabled(RandomTraits.DYING_BREATH)) {
				damageMultiplier = 0.6f;
			} else {
				damageMultiplier = 1.6f;
			}
		}

		return (int) (super.minDamage(type) * damageMultiplier);
	}

	@Override
	public float speed() {
		float speed = super.speed();

		if (getRandomizerEnabled(RandomTraits.BERSERKER_SPEED) && this.buff(BruteRage.class) != null) {
			speed *= 3;
		}

		return speed;
	}

	@Override
	public void die(Object cause) {
		super.die(cause);

		if (cause == Chasm.class){
			hasRaged = true; //don't let enrage trigger for chasm deaths
		}
	}

	@Override
	public synchronized boolean isAlive() {
		if (super.isAlive()){
			return true;
		} else {
			if (!hasRaged){
				triggerEnrage();
			}
			return !buffs(BruteRage.class).isEmpty();
		}
	}
	
	protected void triggerEnrage(){
		if (Brute.getRandomizerEnabled(RandomTraits.STAND_YOUR_GROUND)) {
			rooted = true;
		}

		Buff.affect(this, BruteRage.class).setShield(GetMaxHP()/2 + 4);
		sprite.showStatusWithIcon( CharSprite.POSITIVE, Integer.toString(GetMaxHP()/2 + 4), FloatingText.SHIELDING );
		if (Dungeon.level.heroFOV[pos]) {
			SpellSprite.show( this, SpellSprite.BERSERK);
		}
		spend( TICK );
		hasRaged = true;
	}

	@Override
	public Item createLoot(int itemSlot) {
		if (getRandomizerEnabled(RandomTraits.ARMORED_DROPS)) {
			if (Random.Int(4) == 0) {
				return new PlateArmor().random();
			}
			return new ScaleArmor().random();
		}
		return super.createLoot(itemSlot);
	}


	private static final String HAS_RAGED = "has_raged";
	
	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put(HAS_RAGED, hasRaged);
	}
	
	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		hasRaged = bundle.getBoolean(HAS_RAGED);
	}
	
	public static class BruteRage extends ShieldBuff {
		
		{
			type = buffType.POSITIVE;
		}
		
		@Override
		public boolean act() {
			
			if (target.HP > 0){
				detach();
				return true;
			}

			if (Brute.getRandomizerEnabled(RandomTraits.STAND_YOUR_GROUND)) {
				target.rooted = true;
			}
			
			absorbDamage( Math.round(4*AscensionChallenge.statModifier(target)));
			
			if (shielding() <= 0){
				target.die(null);
			}

			if (Brute.getRandomizerEnabled(RandomTraits.EXTENDED_FURY)) {
				spend (2 * TICK);
			} else {
				spend(TICK);
			}
			
			return true;
		}
		
		@Override
		public int icon () {
			return BuffIndicator.FURY;
		}
		
		@Override
		public String desc () {
			return Messages.get(this, "desc", shielding());
		}
	}
	public enum RandomTraits {
		EXTENDED_FURY, BERSERKER_SPEED, ARMORED_LEGION, STAND_YOUR_GROUND, DYING_BREATH, ARMORED_DROPS
	}

	public static boolean getRandomizerEnabled(RandomTraits r) {
		switch (r) {
			case EXTENDED_FURY: return Randomizer.getCreatureBuff(Brute.class) == 1;
			case BERSERKER_SPEED: return Randomizer.getCreatureBuff(Brute.class) == 2;
			case ARMORED_LEGION: return Randomizer.getCreatureBuff(Brute.class) == 3;
			case STAND_YOUR_GROUND: return Randomizer.getCreatureNerf(Brute.class) == 1;
			case DYING_BREATH: return Randomizer.getCreatureNerf(Brute.class) == 2;
			case ARMORED_DROPS: return Randomizer.getCreatureNerf(Brute.class) == 3;
		}
		return false;
	}
}