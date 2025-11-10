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

package com.shatteredpixel.shatteredpixeldungeon.items.armor.glyphs;

import com.shatteredpixel.shatteredpixeldungeon.Badges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.combat.AttackContext;
import com.shatteredpixel.shatteredpixeldungeon.combat.CombatModifier;
import com.shatteredpixel.shatteredpixeldungeon.combat.DamageType;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor.Glyph;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite.Glowing;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.utils.Bundle;

public class Viscosity extends Glyph implements CombatModifier.PostArmorDamageModifier {
	
	private static ItemSprite.Glowing PURPLE = new ItemSprite.Glowing( 0x8844CC );

	@Override
	public Glowing glowing() {
		return PURPLE;
	}

	@Override
	public int modifyPostArmorDamage(AttackContext context, int currentDamage) {
		int level = Math.max( 0, context.defender.getArmor().buffedLvl() );

		float percent = (level+1)/(float)(level+6);
		percent *= genericProcChanceMultiplier(context.defender);

		int amount;
		if (percent > 1f){
			currentDamage = Math.round(currentDamage / percent);
			amount = currentDamage;
		} else {
			amount = (int)Math.ceil(currentDamage * percent);
		}

		if (amount > 0){
			DeferedDamage deferred = Buff.affect( context.defender, DeferedDamage.class );
			deferred.extend( amount );

			context.defender.sprite.showStatus( CharSprite.WARNING, Messages.get(Viscosity.class, "deferred", amount) );
		}

		return currentDamage - amount;
	}

	@Override
	public int priority() {
		return Priority.LOWEST;
	}

	@Override
	public boolean appliesTo(AttackContext context) {
		return context.defender.getArmor() != null && context.defender.getArmor().glyph == this;
	}

	public static class DeferedDamage extends Buff {
		
		{
			type = buffType.NEGATIVE;
		}
		
		protected int damage = 0;
		
		private static final String DAMAGE	= "damage";
		
		@Override
		public void storeInBundle( Bundle bundle ) {
			super.storeInBundle( bundle );
			bundle.put( DAMAGE, damage );
			
		}
		
		@Override
		public void restoreFromBundle( Bundle bundle ) {
			super.restoreFromBundle( bundle );
			damage = bundle.getInt( DAMAGE );
		}
		
		public void extend( float damage ) {
			if (this.damage == 0){
				//wait 1 turn before damaging if this is freshly applied
				postpone(TICK);
			}
			this.damage += damage;
		}
		
		@Override
		public int icon() {
			return BuffIndicator.DEFERRED;
		}

		@Override
		public String iconTextDisplay() {
			return Integer.toString(damage);
		}
		
		@Override
		public boolean act() {
			if (target.isAlive()) {

				int damageThisTick = Math.max(1, (int)(damage*0.1f));
				target.Damage( damageThisTick, this, DamageType.of(DamageType.DEFERRED));
				if (target == Dungeon.hero && !target.isAlive()) {

					Badges.validateDeathFromFriendlyMagic();

					Dungeon.fail( this );
					GLog.n( Messages.get(this, "ondeath") );
				}
				spend( TICK );

				damage -= damageThisTick;
				if (damage <= 0) {
					detach();
				}
				
			} else {
				
				detach();
				
			}
			
			return true;
		}

		@Override
		public String desc() {
			return Messages.get(this, "desc", damage);
		}
	}
}
