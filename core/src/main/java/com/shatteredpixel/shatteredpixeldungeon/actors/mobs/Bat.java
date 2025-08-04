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

import com.shatteredpixel.shatteredpixeldungeon.Challenges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.Randomizer;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Weakness;
import com.shatteredpixel.shatteredpixeldungeon.effects.FloatingText;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfHealing;
import com.shatteredpixel.shatteredpixeldungeon.sprites.BatSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.sprites.RatSprite;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

public class Bat extends Mob {

	{
		HP = HT = 30;
		defenseSkill = 15;
		baseSpeed = getRandomizerEnabled(RandomTraits.SUPERSONIC_SPEED) ? 3.5f : 2f;
		
		EXP = 7;
		maxLvl = 15;
		
		flying = true;
		
		loot = PotionOfHealing.class;
		lootChance = 0.1667f; //by default, see lootChance()
	}
	@Override
	public Class<? extends CharSprite> GetSpriteClass() {

		return BatSprite.class;
	}

	private static final String ATTACHED = "attached_char_id";
	private int attached = -1;

	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
		bundle.put( ATTACHED, attached );
	}

	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle( bundle );
		attached = bundle.getInt( ATTACHED );
	}

	
	@Override
	public int damageRoll(boolean isMaxDamage) {
		int dmg;
		if (isMaxDamage) dmg = 18;
		else dmg = Random.NormalIntRange( 5, 18 );

		if (getRandomizerEnabled(RandomTraits.BLUNTED_FANGS)) {
			dmg /= 2;
		}

		return dmg;
	}
	
	@Override
	public int attackSkill( Char target ) {
		if (attached == target.id()) {
			return INFINITE_ACCURACY;
		}
		return 16;
	}
	
	@Override
	public int drRoll() {
		return super.drRoll() + Random.NormalIntRange(0, 4);
	}

	@Override
	public void die(Object cause) {
		flying = false;
		super.die(cause);
	}

	@Override
	public int attackProc( Char enemy, int damage ) {
		damage = super.attackProc( enemy, damage );
		int reg = Math.min( damage - 4, HT - HP );
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
			attached = enemy.isAlive() ? enemy.id() : -1;
		}
		
		return damage;
	}

	@Override
	public void move( int step, boolean travelling ) {
		if (attached != -1) {
			Char e = null;
			for (Mob m : Dungeon.level.mobs) {
				if (m.id() == attached) {
					e = m;
					break;
				}
			}
			if (e != null) {
				if (distance(e) > 1) {
					attached = -1;
				}
			} else {
				attached = -1;
			}
		}
		super.move(step, travelling);
		if (attached != -1) {
			Char e = null;
			for (Mob m : Dungeon.level.mobs) {
				if (m.id() == attached) {
					e = m;
					break;
				}
			}
			if (e != null) {
				if (distance(e) > 1) {
					attached = -1;
				}
			} else {
				attached = -1;
			}
		}
	}
	
	@Override
	public float lootChance(){
		if (getRandomizerEnabled(RandomTraits.MEMBRANE_CARRIER)) {
			return 1.0f;
		}
		return super.lootChance() * ((7f - Dungeon.LimitedDrops.BAT_HP.count) / 7f);
	}
	
	@Override
	public Item createLoot(){
		if (getRandomizerEnabled(RandomTraits.MEMBRANE_CARRIER)) {
			if (Random.Float() < super.lootChance() * ((7f - Dungeon.LimitedDrops.BAT_HP.count) / 7f)) {
				Dungeon.LimitedDrops.BAT_HP.count++;
				return super.createLoot();
			}
			// we didn't drop a health potion, drop a membrane instead
			return new Membrane();
		}

		Dungeon.LimitedDrops.BAT_HP.count++;
		return super.createLoot();
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