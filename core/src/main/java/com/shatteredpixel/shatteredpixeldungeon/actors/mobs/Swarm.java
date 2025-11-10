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
import com.shatteredpixel.shatteredpixeldungeon.Randomizer;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Bleeding;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Burning;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Poison;
import com.shatteredpixel.shatteredpixeldungeon.combat.AttackContext;
import com.shatteredpixel.shatteredpixeldungeon.combat.CombatModifier;
import com.shatteredpixel.shatteredpixeldungeon.effects.Pushing;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.combat.DamageType;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.utils.BundleableProperty;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class Swarm extends Mob implements CombatModifier.OnDamageEffect, CombatModifier.PreArmorDamageModifier {
	@Override
	public Constants.mobs.mobsBase GetConstants() { return Constants.mobs.swarm; }
	
	private static final float SPLIT_DELAY	= 1f;

	private BundleableProperty.Int m_Generation = new BundleableProperty.Int("generation", 0);
	
	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
		m_Generation.Store(bundle);
	}
	
	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle( bundle );
		m_Generation.Restore(bundle);
	}

	@Override
	protected void onAdd(){
		boolean previousFirstAdded = firstAdded;
		super.onAdd();
		if (previousFirstAdded && getRandomizerEnabled(RandomTraits.WEAKENED_SWARM)) {
			// 50%-100% health
			float multiplier = Random.Float(0.5f, 1.0f);
			HP = (int) (GetMaxHP() * multiplier);
		}
	}

	@Override
	public void die(Object cause) {
		flying = false;
		super.die(cause);
	}

	@Override
	public int GetXP() {
		if (m_Generation.Get() == 0) return super.GetXP();
		return 0;
	}
	
	private Swarm split() {
		Swarm clone = new Swarm();
		clone.m_Generation.Set(m_Generation.Get() + 1);
		if (buff( Burning.class ) != null) {
			Buff.affect( clone, Burning.class ).reignite( clone );
		}
		if (buff( Poison.class ) != null) {
			Buff.affect( clone, Poison.class ).set(2);
		}
		for (Buff b : buffs()){
			if (b.revivePersists) {
				Buff.affect(clone, b.getClass());
			}
		}
		return clone;
	}

	@Override
	public float GetLootChance(int slot) {
		float lootChance = (5f - Dungeon.LimitedDrops.SWARM_HP.count) / 5f;
		return super.GetLootChance(slot) * lootChance;
	}
	
	@Override
	public Item createLoot(int itemSlot){
		Dungeon.LimitedDrops.SWARM_HP.count++;
		return super.createLoot(itemSlot);
	}

	@Override
	public void onDamage(AttackContext context, int damageDealt) {
		if (context.defender == this) {
			// Swarms have generation limit and randomizer checks
			if (!isAlive() || HP < 2 || m_Generation.Get() > 5) {
				return;
			}

			// Randomizer can prevent splitting
			if (getRandomizerEnabled(RandomTraits.DEPLETED_NUMBERS)
					&& Random.Float() > 0.5f) {
				return;
			}

			// Find valid positions (swarms can fly over gaps)
			ArrayList<Integer> candidates = new ArrayList<>();
			int[] neighbours = {
					pos + 1, pos - 1,
					pos + Dungeon.level.width(),
					pos - Dungeon.level.width()
			};

			for (int n : neighbours) {
				if (!Dungeon.level.solid[n] && Actor.findChar(n) == null
						&& (Dungeon.level.passable[n] || Dungeon.level.avoid[n])) {
					candidates.add(n);
				}
			}

			if (candidates.isEmpty()) {
				return;
			}

			// Create and split
			Swarm clone = createClone();
			clone.pos = Random.element(candidates);
			clone.state = clone.HUNTING;

			GameScene.add(clone, SPLIT_DELAY);

			int splitHP = HP / 2;
			clone.HP = splitHP;
			this.HP -= splitHP;

			Actor.add(new Pushing(clone, pos, clone.pos));
			Dungeon.level.occupyCell(clone);
		}
		else if (context.attacker == this) {
			if (getRandomizerEnabled(RandomTraits.BLOODSUCKERS)) {
				if (damageDealt > 0 && Random.Int(2) == 0) {
					Buff.affect(enemy, Bleeding.class).set(3);
				}
			}
		}
	}

	private Swarm createClone() {
		Swarm clone = new Swarm();
		clone.m_Generation.Set(this.m_Generation.Get() + 1);

		// Copy buffs
		if (buff(Burning.class) != null) {
			Buff.affect(clone, Burning.class).reignite(clone);
		}
		if (buff(Poison.class) != null) {
			Buff.affect(clone, Poison.class).set(2);
		}

		// Copy persistent buffs
		for (Buff b : buffs()) {
			if (b.revivePersists) {
				Buff.affect(clone, b.getClass());
			}
		}

		return clone;
	}

	@Override
	public int modifyPreArmorDamage(AttackContext context, int currentDamage) {
		if (context.defender == this) {
			float damage = 0.0f;
			float damagePerType = (float) currentDamage / context.damageType.size();

			for (DamageType type : context.damageType) {
				if (getRandomizerEnabled(RandomTraits.DAMAGE_RESISTANCE)) {
					if (type == DamageType.PIERCING || type == DamageType.SLASHING) {
						damage += damagePerType / 2;
					}
				} else if (getRandomizerEnabled(RandomTraits.MAGIC_VULNERABILITY)) {
					if (DamageType.IsDamageEnergy(type)) {
						damage += damagePerType * 3.0f;
					}
				} else {
					damage += damagePerType;
				}
			}

			return (int) damage;
		}
		return currentDamage;
	}

	@Override
	public int priority() {
		return Priority.NORMAL;
	}

	@Override
	public boolean appliesTo(AttackContext context) {
		return context.attacker == this || context.defender == this;
	}

	public enum RandomTraits {
		DAMAGE_RESISTANCE, CRAB_SPEED, BLOODSUCKERS, WEAKENED_SWARM, MAGIC_VULNERABILITY, DEPLETED_NUMBERS
	}

	public static boolean getRandomizerEnabled(RandomTraits r) {
		switch (r) {
			case DAMAGE_RESISTANCE: return Randomizer.getCreatureBuff(Swarm.class) == 1;
			case CRAB_SPEED: return Randomizer.getCreatureBuff(Swarm.class) == 2;
			case BLOODSUCKERS: return Randomizer.getCreatureBuff(Swarm.class) == 3;
			case WEAKENED_SWARM: return Randomizer.getCreatureNerf(Swarm.class) == 1;
			case MAGIC_VULNERABILITY: return Randomizer.getCreatureNerf(Swarm.class) == 2;
			case DEPLETED_NUMBERS: return Randomizer.getCreatureNerf(Swarm.class) == 3;
		}
		return false;
	}
}