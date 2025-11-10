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

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Burning;
import com.shatteredpixel.shatteredpixeldungeon.combat.AttackContext;
import com.shatteredpixel.shatteredpixeldungeon.combat.CombatModifier;
import com.shatteredpixel.shatteredpixeldungeon.combat.DamageType;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.FlameParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.watabou.utils.Random;

public class Blazing extends Weapon.Enchantment implements CombatModifier.OnHitEffect {

	private static ItemSprite.Glowing ORANGE = new ItemSprite.Glowing(0xFF4400);

	@Override
	public ItemSprite.Glowing glowing() {
		return ORANGE;
	}

	@Override
	public void onHit(AttackContext context, int finalDamage) {
		int level = Math.max(0, context.attacker.getWeapon().buffedLvl());

		// lvl 0 - 33%
		// lvl 1 - 50%
		// lvl 2 - 60%
		float procChance = (level + 1f) / (level + 3f) * procChanceMultiplier(context.attacker);

		if (Random.Float() < procChance) {
			// If proc chance exceeds 100%, the excess boosts the effect strength
			float powerMulti = Math.max(1f, procChance);

			// Apply Burning buff if not already burning
			if (context.defender.buff(Burning.class) == null) {
				Buff.affect(context.defender, Burning.class).reignite(context.defender, 8f);
				powerMulti -= 1; // Reduce power if we applied the buff
			}

			// Deal immediate burn damage with remaining power
			if (powerMulti > 0) {
				int burnDamage = Random.NormalIntRange(1, 3 + Dungeon.scalingDepth() / 4);
				burnDamage = Math.round(burnDamage * 0.67f * powerMulti);
				if (burnDamage > 0) {
					context.defender.Damage(burnDamage, this, DamageType.of(DamageType.FIRE));
				}
			}

			// Visual effect - more particles at higher levels
			context.defender.sprite.emitter().burst(FlameParticle.FACTORY, level + 1);
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