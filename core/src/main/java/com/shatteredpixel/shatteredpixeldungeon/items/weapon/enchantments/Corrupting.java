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
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Adrenaline;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.AllyBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Chill;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Corruption;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.combat.AttackContext;
import com.shatteredpixel.shatteredpixeldungeon.combat.CombatModifier;
import com.shatteredpixel.shatteredpixeldungeon.effects.Splash;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.watabou.utils.Random;

public class Corrupting extends Weapon.Enchantment implements CombatModifier.PostArmorDamageModifier {
	
	private static ItemSprite.Glowing BLACK = new ItemSprite.Glowing( 0x440066 );

	@Override
	public int modifyPostArmorDamage(AttackContext context, int currentDamage) {
		int level = Math.max( 0, context.attacker.getWeapon().buffedLvl() );

		// lvl 0 - 20%
		// lvl 1 ~ 23%
		// lvl 2 ~ 26%
		float procChance = (level+5f)/(level+25f) * procChanceMultiplier(context.attacker);
		if (context.defender.isAlive()
				&& context.defender.HP <= currentDamage
				&& Random.Float() < procChance
				&& !context.defender.isImmune(Corruption.class)
				&& context.defender.buff(Corruption.class) == null
				&& context.defender instanceof Mob) {

			Mob enemy = (Mob) context.defender;
			Hero hero = (context.attacker instanceof Hero) ? (Hero) context.attacker : Dungeon.hero;

			Corruption.corruptionHeal(enemy);

			AllyBuff.affectAndLoot(enemy, hero, Corruption.class);

			float powerMulti = Math.max(1f, procChance);
			if (powerMulti > 1.1f){
				// 1 turn of adrenaline for each 20% above 100% proc rate
				Buff.affect(enemy, Adrenaline.class, Math.round(5*(powerMulti-1f)));
			}
			return 0;
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
	public ItemSprite.Glowing glowing() {
		return BLACK;
	}
}
