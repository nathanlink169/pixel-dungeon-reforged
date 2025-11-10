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
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mimic;
import com.shatteredpixel.shatteredpixeldungeon.combat.AttackContext;
import com.shatteredpixel.shatteredpixeldungeon.combat.CombatModifier;
import com.shatteredpixel.shatteredpixeldungeon.effects.FloatingText;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite.Glowing;
import com.watabou.utils.Random;

public class Vampiric extends Weapon.Enchantment implements CombatModifier.OnDamageEffect {

	private static ItemSprite.Glowing RED = new ItemSprite.Glowing(0x660022);

	@Override
	public ItemSprite.Glowing glowing() {
		return RED;
	}

	@Override
	public void onDamage(AttackContext context, int damageDealt) {
		int level = Math.max(0, context.attacker.getWeapon().buffedLvl());

		// lvl 0 - 6.67%
		// lvl 1 ~ 7.69%
		// lvl 2 ~ 8.33%
		float procChance = (level + 2f) / (level + 30f) * procChanceMultiplier(context.attacker);

		if (Random.Float() < procChance) {
			float powerMulti = Math.max(1f, procChance);

			// Heal based on damage dealt
			int toHeal = Math.round(damageDealt * 0.5f * powerMulti);
			toHeal = Math.min(toHeal, context.attacker.GetMaxHP() - context.attacker.HP);

			if (toHeal > 0) {
				context.attacker.HP += toHeal;
				context.attacker.sprite.showStatusWithIcon(
						CharSprite.POSITIVE,
						Integer.toString(toHeal),
						FloatingText.HEALING
				);
			}
		}
	}

	@Override
	public int priority() {
		return Priority.NORMAL;
	}

	@Override
	public boolean appliesTo(AttackContext context) {
		return context.attacker.getWeapon() != null && context.attacker.getWeapon().enchantment == this;
	}
}