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

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.combat.AttackContext;
import com.shatteredpixel.shatteredpixeldungeon.combat.CombatModifier;
import com.shatteredpixel.shatteredpixeldungeon.combat.CombatResolver;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.watabou.utils.Random;
import com.watabou.utils.Reflection;

public class Unstable extends Weapon.Enchantment implements CombatModifier.AccuracyModifier, CombatModifier.PostArmorDamageModifier, CombatModifier.OnHitEffect, CombatModifier.OnDamageEffect {

	private static ItemSprite.Glowing GREY = new ItemSprite.Glowing( 0x999999 );

	private static Class<?extends Weapon.Enchantment>[] randomEnchants = new Class[]{
			Blazing.class,
			Blocking.class,
			Blooming.class,
			Chilling.class,
			Kinetic.class,
			Corrupting.class,
			Elastic.class,
			Grim.class,
			Lucky.class,
			//projecting not included, no on-hit effect
			Shocking.class,
			Vampiric.class
	};

	private Weapon.Enchantment randomEnch = null;

	@Override
	public ItemSprite.Glowing glowing() {
		return GREY;
	}

	// Modify Accuracy is the first modifier called. Randomize enchantment here
	@Override
	public float modifyAccuracy(AttackContext context, float currentAccuracy) {
		Weapon.Enchantment randomEnch = (Weapon.Enchantment) Reflection.newInstance(Random.oneOf(randomEnchants));
		return randomEnch instanceof AccuracyModifier ? ((AccuracyModifier)randomEnch).modifyAccuracy(context, currentAccuracy) : currentAccuracy;
	}

	@Override
	public void onHit(AttackContext context, int finalDamage) {
		if (randomEnch instanceof OnHitEffect)
			((OnHitEffect)randomEnch).onHit(context, finalDamage);
	}

	@Override
	public int modifyPostArmorDamage(AttackContext context, int currentDamage) {
		return randomEnch instanceof PostArmorDamageModifier ? ((PostArmorDamageModifier)randomEnch).modifyPostArmorDamage(context, currentDamage) : currentDamage;
	}

	@Override
	public void onDamage(AttackContext context, int damageDealt) {
		if (randomEnch instanceof OnDamageEffect)
			((OnDamageEffect)randomEnch).onDamage(context, damageDealt);
	}

	@Override
	public int priority() {
		return randomEnch != null ? ((CombatModifier)randomEnch).priority() : Priority.HIGHEST;
	}

	@Override
	public boolean appliesTo(AttackContext context) {
		return context.attacker.getWeapon() != null && context.attacker.getWeapon().enchantment == this;
	}
}
