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

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.combat.AttackContext;
import com.shatteredpixel.shatteredpixeldungeon.combat.CombatModifier;
import com.shatteredpixel.shatteredpixeldungeon.combat.DamageType;
import com.shatteredpixel.shatteredpixeldungeon.effects.Lightning;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.SparkParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.watabou.utils.BArray;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class Shocking extends Weapon.Enchantment implements CombatModifier.OnHitEffect {
	private static ItemSprite.Glowing WHITE = new ItemSprite.Glowing(0xFFFFFF, 0.5f);

	// Reusable lists to avoid allocations
	private ArrayList<Char> affected = new ArrayList<>();
	private ArrayList<Lightning.Arc> arcs = new ArrayList<>();

	@Override
	public void onHit(AttackContext context, int finalDamage) {
		int level = Math.max(0, context.attacker.getWeapon().buffedLvl());

		// lvl 0 - 25%
		// lvl 1 - 40%
		// lvl 2 - 50%
		float procChance = (level + 1f) / (level + 4f) * procChanceMultiplier(context.attacker);

		if (Random.Float() < procChance) {
			float powerMulti = Math.max(1f, procChance);

			affected.clear();
			arcs.clear();

			// Arc lightning to nearby enemies (2 tile range)
			arc(context.attacker, context.defender, 2, affected, arcs);

			// Defender isn't hurt by lightning (they already took main damage)
			affected.remove(context.defender);

			// Deal 40% damage to chained targets
			for (Char ch : affected) {
				if (ch.alignment != context.attacker.alignment) {
					ch.Damage(Math.round(finalDamage * 0.4f * powerMulti), this, DamageType.of(DamageType.ELECTRICITY));
				}
			}

			// Visual lightning effect
			context.attacker.sprite.parent.addToFront(new Lightning(arcs, null));
			Sample.INSTANCE.play(Assets.Sounds.LIGHTNING);
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

	/**
	 * Recursive arc method - chains lightning to nearby enemies
	 */
	public static void arc(Char attacker, Char defender, int dist,
						   ArrayList<Char> affected, ArrayList<Lightning.Arc> arcs) {
		defender.sprite.centerEmitter().burst(SparkParticle.FACTORY, 3);
		defender.sprite.flash();

		ArrayList<Char> hitThisArc = new ArrayList<>();
		PathFinder.buildDistanceMap(defender.pos, BArray.not(Dungeon.level.solid, null), dist);

		for (int i = 0; i < PathFinder.distance.length; i++) {
			if (PathFinder.distance[i] < Integer.MAX_VALUE) {
				Char n = Actor.findChar(i);
				if (n != null && n != attacker && !affected.contains(n)) {
					hitThisArc.add(n);
				}
			}
		}

		affected.addAll(hitThisArc);
		for (Char hit : hitThisArc) {
			arcs.add(new Lightning.Arc(defender.sprite.center(), hit.sprite.center()));
			// Chain further if in water (and not flying)
			arc(attacker, hit, (Dungeon.level.water[hit.pos] && !hit.flying) ? 2 : 1, affected, arcs);
		}
	}

	@Override
	public ItemSprite.Glowing glowing() {
		return WHITE;
	}
}