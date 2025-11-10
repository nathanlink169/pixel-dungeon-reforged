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

package com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments;

import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.combat.AttackContext;
import com.shatteredpixel.shatteredpixeldungeon.combat.CombatModifier;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.watabou.noosa.Image;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

public class Kinetic extends Weapon.Enchantment implements CombatModifier.OnHitEffect {

	private static ItemSprite.Glowing YELLOW = new ItemSprite.Glowing(0xFFFF00);

	@Override
	public ItemSprite.Glowing glowing() {
		return YELLOW;
	}

	@Override
	public void onHit(AttackContext context, int finalDamage) {
		int level = Math.max(0, context.attacker.getWeapon().buffedLvl());

		// lvl 0 - 25%
		// lvl 1 ~ 33%
		// lvl 2 ~ 40%
		float procChance = (level + 1f) / (level + 4f) * procChanceMultiplier(context.attacker);

		// Store damage for next hit
		if (Random.Float() < procChance) {
			float powerMulti = Math.max(1f, procChance);

			int conserved = Math.round(finalDamage * 0.5f * powerMulti) - context.startingDefenderHP;
			if (conserved > 0) {
				ConservedDamage newBuff = Buff.affect(
						context.attacker,
						ConservedDamage.class
				);
				newBuff.setBonus(conserved);
			}
		}
	}

	@Override
	public int priority() {
		return Priority.LOWEST; // Go last
	}

	@Override
	public boolean appliesTo(AttackContext context) {
		return context.attacker.getWeapon() != null && context.attacker.getWeapon().enchantment == this;
	}

	public static class ConservedDamage extends Buff implements CombatModifier.PreArmorDamageModifier {

		{
			type = buffType.POSITIVE;
		}

		@Override
		public int icon() {
			return BuffIndicator.WEAPON;
		}

		@Override
		public void tintIcon(Image icon) {
			if (preservedDamage >= 10){
				icon.hardlight(1f, 0f, 0f);
			} else if (preservedDamage >= 5) {
				icon.hardlight(1f, 1f - (preservedDamage - 5f)*.2f, 0f);
			} else {
				icon.hardlight(1f, 1f, 1f - preservedDamage*.2f);
			}
		}

		@Override
		public String iconTextDisplay() {
			return Integer.toString(damageBonus());
		}

		private float preservedDamage;

		public void setBonus(int bonus){
			preservedDamage = bonus;
		}

		public int damageBonus(){
			return (int)Math.ceil(preservedDamage);
		}

		@Override
		public boolean act() {
			preservedDamage -= Math.max(preservedDamage*.025f, 0.1f);
			if (preservedDamage <= 0) detach();

			spend(TICK);
			return true;
		}

		public void delay( float value ){
			spend(value);
		}

		@Override
		public String desc() {
			return Messages.get(this, "desc", damageBonus());
		}

		private static final String PRESERVED_DAMAGE = "preserve_damage";

		@Override
		public void storeInBundle(Bundle bundle) {
			super.storeInBundle(bundle);
			bundle.put(PRESERVED_DAMAGE, preservedDamage);
		}

		@Override
		public void restoreFromBundle(Bundle bundle) {
			super.restoreFromBundle(bundle);
			if (bundle.contains(PRESERVED_DAMAGE)){
				preservedDamage = bundle.getFloat(PRESERVED_DAMAGE);
			} else {
				preservedDamage = cooldown()/10;
				spend(cooldown());
			}
		}

		@Override
		public int modifyPreArmorDamage(AttackContext context, int currentDamage) {
			currentDamage += (int) preservedDamage;
			detach();
			return currentDamage;
		}

		@Override
		public int priority() {
			return Priority.HIGHEST; // This should essentially be part of damage roll, apply immediately
		}

		@Override
		public boolean appliesTo(AttackContext context) {
			return context.attacker == target;
		}
	}
}
