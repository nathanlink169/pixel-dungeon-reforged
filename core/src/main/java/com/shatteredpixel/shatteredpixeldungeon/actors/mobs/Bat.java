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
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Weakness;
import com.shatteredpixel.shatteredpixeldungeon.combat.AttackContext;
import com.shatteredpixel.shatteredpixeldungeon.combat.CombatModifier;
import com.shatteredpixel.shatteredpixeldungeon.combat.DamageType;
import com.shatteredpixel.shatteredpixeldungeon.effects.FloatingText;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.BundleableProperty;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

public class Bat extends Mob implements CombatModifier.AccuracyModifier, CombatModifier.OnHitEffect {
	@Override
	public Constants.mobs.mobsBase GetConstants() { return Constants.mobs.bat; }

	@Override
	public float speed() {
		return super.speed() * (getRandomizerEnabled(RandomTraits.SUPERSONIC_SPEED) ? 1.75f : 1f);
	}

	private BundleableProperty.Int m_Attached = new BundleableProperty.Int("attached_char_id", -1);

	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
		m_Attached.Store(bundle);
	}

	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle( bundle );
		m_Attached.Restore(bundle);
	}

	
	@Override
	public int damageRoll(AttackContext context) {
		return super.damageRoll(context) / (getRandomizerEnabled(RandomTraits.BLUNTED_FANGS) ? 2 : 1);
	}

	@Override
	public void die(Object cause) {
		flying = false;
		super.die(cause);
	}

	@Override
	public void move( int step, boolean travelling ) {
		if (m_Attached.Get() != -1) {
			Char e = null;
			for (Mob m : Dungeon.level.mobs) {
				if (m.id() == m_Attached.Get()) {
					e = m;
					break;
				}
			}
			if (e != null) {
				if (distance(e) > 1) {
					m_Attached.Reset();
				}
			} else {
				m_Attached.Reset();
			}
		}
		super.move(step, travelling);
		if (m_Attached.Get() != -1) {
			Char e = null;
			for (Mob m : Dungeon.level.mobs) {
				if (m.id() == m_Attached.Get()) {
					e = m;
					break;
				}
			}
			if (e != null) {
				if (distance(e) > 1) {
					m_Attached.Reset();
				}
			} else {
				m_Attached.Reset();
			}
		}
	}
	
	@Override
	public float GetLootChance(int slot){
		if (getRandomizerEnabled(RandomTraits.MEMBRANE_CARRIER)) {
			return 1.0f;
		}
		return super.GetLootChance(slot) * ((7f - Dungeon.LimitedDrops.BAT_HP.count) / 7f);
	}
	
	@Override
	public Item createLoot(int itemSlot){
		if (getRandomizerEnabled(RandomTraits.MEMBRANE_CARRIER)) {
			if (Random.Float() < super.GetLootChance(0) * ((7f - Dungeon.LimitedDrops.BAT_HP.count) / 7f)) {
				Dungeon.LimitedDrops.BAT_HP.count++;
				return super.createLoot(itemSlot);
			}
			// we didn't drop a health potion, drop a membrane instead
			return new Membrane();
		}

		Dungeon.LimitedDrops.BAT_HP.count++;
		return super.createLoot(itemSlot);
	}

	@Override
	public float modifyAccuracy(AttackContext context, float currentAccuracy) {
		if (m_Attached.Get() == context.defender.id()) {
			return INFINITE_ACCURACY;
		}
		return currentAccuracy;
	}

	@Override
	public void onHit(AttackContext context, int finalDamage) {
		int reg = Math.min( finalDamage - 4, GetMaxHP() - HP );
		if (getRandomizerEnabled(RandomTraits.WEAK_REGENERATION)) {
			reg /= 4;
		}

		if (reg > 0) {
			HP += reg;
			sprite.showStatusWithIcon(CharSprite.POSITIVE, Integer.toString(reg), FloatingText.HEALING);

			if (getRandomizerEnabled(RandomTraits.DRAINING_BITE)) {
				Buff.affect(enemy, Weakness.class, Weakness.DURATION);
			}
		}

		if (getRandomizerEnabled(RandomTraits.BLOOD_LOCK)) {
			m_Attached.Set(enemy.isAlive() ? enemy.id() : -1);
		}
	}

	@Override
	public int priority() {
		return Priority.NORMAL;
	}

	@Override
	public boolean appliesTo(AttackContext context) {
		return context.attacker == this;
	}

	public enum RandomTraits {
		DRAINING_BITE, SUPERSONIC_SPEED, BLOOD_LOCK, WEAK_REGENERATION, BLUNTED_FANGS, MEMBRANE_CARRIER
	}

	public static boolean getRandomizerEnabled(RandomTraits r) {
		switch (r) {
			case DRAINING_BITE: return Randomizer.getCreatureBuff(Bat.class) == 1;
			case SUPERSONIC_SPEED: return Randomizer.getCreatureBuff(Bat.class) == 2;
			case BLOOD_LOCK: return Randomizer.getCreatureBuff(Bat.class) == 3;
			case WEAK_REGENERATION: return Randomizer.getCreatureNerf(Bat.class) == 1;
			case BLUNTED_FANGS: return Randomizer.getCreatureNerf(Bat.class) == 2;
			case MEMBRANE_CARRIER: return Randomizer.getCreatureNerf(Bat.class) == 3;
		}
		return false;
	}

	public static class Membrane extends Item {

		{
			image = ItemSpriteSheet.BAT_MEMBRANE;
			stackable = true;
		}

		@Override
		public boolean isUpgradable() {
			return false;
		}

		@Override
		public boolean isIdentified() {
			return true;
		}

		@Override
		public int energyVal() {
			return 2 * quantity;
		}

		public Membrane() {
			reset();
		}
	}
}