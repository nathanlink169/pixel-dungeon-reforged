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

import com.shatteredpixel.shatteredpixeldungeon.Badges;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.combat.AttackContext;
import com.shatteredpixel.shatteredpixeldungeon.combat.CombatModifier;
import com.shatteredpixel.shatteredpixeldungeon.combat.DamageType;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.ShadowParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite.Glowing;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.utils.Random;

public class Grim extends Weapon.Enchantment implements CombatModifier.PostArmorDamageModifier {
	
	private static ItemSprite.Glowing BLACK = new ItemSprite.Glowing( 0x000000 );

	@Override
	public int modifyPostArmorDamage(AttackContext context, int currentDamage) {
		if (context.defender.isImmune(Grim.class)) {
			return currentDamage;
		}

		int level = Math.max(0, context.attacker.getWeapon().buffedLvl());

		// Scales from 0 - 50% based on how low hp the enemy is, plus 0-5% per level
		float maxChance = 0.5f + .05f * level;
		maxChance *= procChanceMultiplier(context.attacker);

		float finalChance = maxChance * (float)Math.pow( ((context.defender.GetMaxHP() - context.defender.HP) / (float)context.defender.GetMaxHP()), 2);
		if (Random.Float() < finalChance) {
			int extraDmg = Math.round(context.defender.GetMaxHP() * context.defender.resist(Grim.class));
			currentDamage += extraDmg;

			context.defender.sprite.emitter().burst( ShadowParticle.UP, 5 );
			if (currentDamage >= context.defender.HP && context.attacker instanceof Hero){
				Badges.validateGrimWeapon();
			}
		}

		return currentDamage;
	}

	@Override
	public int priority() {
		return Priority.LOWEST; // Go last
	}

	@Override
	public boolean appliesTo(AttackContext context) {
		return context.attacker.getWeapon() != null && context.attacker.getWeapon().enchantment == this;
	}
	
	@Override
	public Glowing glowing() {
		return BLACK;
	}
}
