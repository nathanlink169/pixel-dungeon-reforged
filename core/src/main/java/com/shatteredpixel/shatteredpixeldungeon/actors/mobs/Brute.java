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
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.AscensionChallenge;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Charm;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Dread;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.ShieldBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.SnipersMark;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Terror;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.HalfRipper;
import com.shatteredpixel.shatteredpixeldungeon.combat.AttackContext;
import com.shatteredpixel.shatteredpixeldungeon.combat.CombatModifier;
import com.shatteredpixel.shatteredpixeldungeon.combat.DamageType;
import com.shatteredpixel.shatteredpixeldungeon.effects.FloatingText;
import com.shatteredpixel.shatteredpixeldungeon.effects.SpellSprite;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.PlateArmor;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.ScaleArmor;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.Chasm;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.utils.BundleableProperty;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

public class Brute extends Mob {
	@Override
	public Constants.mobs.mobsBase GetConstants() { return Constants.mobs.brute; }

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
		if (cause == Chasm.class){
			m_HasRaged.Set(true); //don't let enrage trigger for chasm deaths
		}

		if (m_HasRaged.Get()) {
			super.die(cause);
		}
		else {
			triggerEnrage();
		}
	}

    // This is real hacky but I can't figure out why the brute will sometimes remain alive even
    // though the dungeon thinks it's dead.
    @Override
    public boolean act() {
        if (!isAlive()) {
            die(null);
        }
        return super.act();
    }

    public void destroy() {
        m_HasRaged.Set(true);
        if (buff(BruteRage.class) != null) {
            buff(BruteRage.class).detach();
        }
        if (buff(ArmoredBrute.ArmoredRage.class) != null) {
            buff(ArmoredBrute.ArmoredRage.class).detach();
        }
        super.destroy();
    }

	@Override
	public synchronized boolean isAlive() {
		if (super.isAlive()){
			return true;
		} else {
			if (!m_HasRaged.Get()){
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
		m_HasRaged.Set(true);
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

	protected BundleableProperty.Bool m_HasRaged = new BundleableProperty.Bool("has_raged", false);
	
	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		m_HasRaged.Store(bundle);
	}
	
	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		m_HasRaged.Restore(bundle);
	}

	public static class BruteRage extends ShieldBuff implements CombatModifier.PreArmorDamageModifier {
		
		{
			type = buffType.POSITIVE;
		}
		
		@Override
		public boolean act() {

			// Force HP to stay at 0 while enraged
			if (target.HP > 0){
				target.HP = 0;
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

		@Override
		public int modifyPreArmorDamage(AttackContext context, int currentDamage) {
			float damageMultiplier = 1.0f;
			if (getRandomizerEnabled(RandomTraits.DYING_BREATH)) {
				damageMultiplier = 0.5f;
			} else {
				damageMultiplier = 3.0f;
			}
			return (int) (currentDamage * damageMultiplier);
		}

		@Override
		public int priority() {
			return Priority.NORMAL;
		}

		@Override
		public boolean appliesTo(AttackContext context) {
			return context.attacker == target;
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