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

package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.combat.AttackContext;
import com.shatteredpixel.shatteredpixeldungeon.combat.CombatModifier;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

public class Paralysis extends FlavourBuff implements CombatModifier.EvasionModifier, CombatModifier.OnDamageEffect {

	public static final float DURATION	= 10f;

	{
		type = buffType.NEGATIVE;
		announced = true;
	}
	
	@Override
	public boolean attachTo( Char target ) {
		if (super.attachTo( target )) {
			target.paralysed++;
			return true;
		} else {
			return false;
		}
	}
	
	@Override
	public void detach() {
		super.detach();
		if (target.paralysed > 0)
			target.paralysed--;
	}
	
	@Override
	public int icon() {
		return BuffIndicator.PARALYSIS;
	}

	@Override
	public float iconFadePercent() {
		return Math.max(0, (DURATION - visualcooldown()) / DURATION);
	}

	@Override
	public void fx(boolean on) {
		if (on)                         target.sprite.add(CharSprite.State.PARALYSED);
		else if (target.paralysed <= 1) target.sprite.remove(CharSprite.State.PARALYSED);
	}

	@Override
	public float modifyEvasion(AttackContext context, float currentEvasion) {
		return currentEvasion * 0.5f;
	}

	@Override
	public int priority() {
		return Priority.NORMAL;
	}

	@Override
	public boolean appliesTo(AttackContext context) {
		return context.defender == target;
	}

	@Override
	public void onDamage(AttackContext context, int damageDealt) {
		ParalysisResist resist = target.buff(ParalysisResist.class);
		if (resist == null){
			resist = Buff.affect(target, ParalysisResist.class);
		}
		resist.damage += damageDealt;
		if (Random.NormalIntRange(0, resist.damage) >= Random.NormalIntRange(0, target.HP)){
			detach();
		}
	}

	public static class ParalysisResist extends Buff {
		
		{
			type = buffType.POSITIVE;
		}
		
		private int damage;
		
		@Override
		public boolean act() {
			if (target.buff(Paralysis.class) == null) {
				damage -= Math.ceil(damage / 10f);
				if (damage <= 0) detach();
			}
			spend(TICK);
			return true;
		}
		
		private static final String DAMAGE = "damage";
		
		@Override
		public void storeInBundle(Bundle bundle) {
			super.storeInBundle(bundle);
			bundle.put( DAMAGE, damage );
		}
		
		@Override
		public void restoreFromBundle(Bundle bundle) {
			super.restoreFromBundle(bundle);
			damage = bundle.getInt(DAMAGE);
		}
	}
}
