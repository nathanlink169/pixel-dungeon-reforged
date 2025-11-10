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

package com.shatteredpixel.shatteredpixeldungeon.actors.mobs;

import com.shatteredpixel.shatteredpixeldungeon.Constants;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Burning;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Ooze;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Poison;
import com.shatteredpixel.shatteredpixeldungeon.combat.AttackContext;
import com.shatteredpixel.shatteredpixeldungeon.combat.CombatModifier;
import com.shatteredpixel.shatteredpixeldungeon.combat.DamageType;
import com.shatteredpixel.shatteredpixeldungeon.effects.Pushing;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.Door;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.utils.BundleableProperty;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class DemonGoo extends Mob implements CombatModifier.OnDamageEffect {
	private static final float SPLIT_DELAY = 1f;
	@Override
	public Constants.mobs.mobsBase GetConstants() { return Constants.mobs.demongoo; }


	@Override
	protected boolean act() {
		boolean result = super.act();

		if (Dungeon.level.water[pos] && HP < GetMaxHP()) {
			sprite.emitter().burst( Speck.factory( Speck.HEALING ), 1 );
			HP++;
		}
		return result;
	}

	private BundleableProperty.Int m_DemonGooGeneration = new BundleableProperty.Int("demonGooGeneration", 0);

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		m_DemonGooGeneration.Store(bundle);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		m_DemonGooGeneration.Restore(bundle);
	}

	@Override
	public float GetLootChance(int slot) {
		float lootChance = (5f - Dungeon.LimitedDrops.DEMON_GOO.count) / 5f;
		return super.GetLootChance(slot) * lootChance;
	}

	@Override
	public Item createLoot(int itemSlot){
		Dungeon.LimitedDrops.DEMON_GOO.count++;
		return super.createLoot(itemSlot);
	}

	@Override
	public void onDamage(AttackContext context, int damageDealt) {
		if (context.defender == this) {
			// Check if we survived with enough HP to split
			if (!isAlive() || HP < 2) {
				return;
			}

			// Find valid adjacent positions
			ArrayList<Integer> candidates = new ArrayList<>();
			boolean[] passable = Dungeon.level.passable;

			int[] neighbours = {
					pos + 1, pos - 1,
					pos + Dungeon.level.width(),
					pos - Dungeon.level.width()
			};

			for (int n : neighbours) {
				if (passable[n] && Actor.findChar(n) == null) {
					candidates.add(n);
				}
			}

			if (candidates.isEmpty()) {
				return;
			}

			// Create and position the clone
			DemonGoo clone = createClone();
			clone.pos = Random.element(candidates);
			clone.state = clone.HUNTING;

			if (Dungeon.level.map[clone.pos] == Terrain.DOOR) {
				Door.enter(clone.pos);
			}

			// Add to game
			GameScene.add(clone, SPLIT_DELAY);
			Actor.add(new Pushing(clone, pos, clone.pos));

			// Split HP between original and clone
			int splitHP = HP / 2;
			clone.HP = splitHP;
			this.HP -= splitHP;
		} else if (context.attacker == this) {
			if (Random.Int(3) == 0) {
				Buff.affect(enemy, Ooze.class).set(Ooze.DURATION);
				enemy.sprite.burst(0x000000, 5);
			}
		}
	}

	private DemonGoo createClone() {
		DemonGoo clone = new DemonGoo();
		clone.m_DemonGooGeneration.Set(this.m_DemonGooGeneration.Get() + 1);

		// Copy persistent buffs
		if (buff(Burning.class) != null) {
			Buff.affect(clone, Burning.class).reignite(clone);
		}
		if (buff(Poison.class) != null) {
			Buff.affect(clone, Poison.class).set(2);
		}

		return clone;
	}

	@Override
	public int priority() {
		return Priority.NORMAL;
	}

	@Override
	public boolean appliesTo(AttackContext context) {
		return context.defender == this || context.attacker == this;
	}
}