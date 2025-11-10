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

package com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.darts;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.ChampionEnemy;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Chill;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FlavourBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.combat.AttackContext;
import com.shatteredpixel.shatteredpixeldungeon.combat.CombatModifier;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.exotic.PotionOfCleansing;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Crossbow;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

public class CleansingDart extends TippedDart implements CombatModifier.PostArmorDamageModifier {
	
	{
		image = ItemSpriteSheet.CLEANSING_DART;
	}

	@Override
	public int modifyPostArmorDamage(AttackContext context, int currentDamage) {
		return context.attacker.alignment == context.defender.alignment ? 0 : currentDamage;
	}

	@Override
	protected void applyDartEffect(Char attacker, Char defender) {
		// Don't affect hero during charged shot AoE
		if (processingChargedShot && defender == attacker) {
			return;
		}

		if (attacker.alignment == defender.alignment) {
			// Cleanse allies
			PotionOfCleansing.cleanse(defender, PotionOfCleansing.Cleanse.DURATION*2f);
		} else {
			// Remove positive buffs from enemies
			for (Buff b : defender.buffs()) {
				if (!(b instanceof ChampionEnemy)
						&& b.type == Buff.buffType.POSITIVE
						&& !(b instanceof Crossbow.ChargedShot)) {
					b.detach();
				}
			}

			// Handle mobs that die when cleansed (raging brutes)
			if (!defender.isAlive()) {
				defender.die(attacker);
			}

			// Make cleansed mobs wander away
			if (defender instanceof Mob) {
				new FlavourBuff() {
					{ actPriority = VFX_PRIO; }
					public boolean act() {
						Mob mob = (Mob)defender;
						if (mob.state == mob.HUNTING || mob.state == mob.FLEEING) {
							mob.state = mob.WANDERING;
						}
						mob.beckon(Dungeon.level.randomDestination(defender));
						defender.sprite.showLost();
						return super.act();
					}
				}.attachTo(defender);
			}
		}
	}
}
