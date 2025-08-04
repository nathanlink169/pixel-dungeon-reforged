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

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Challenges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.Randomizer;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.AscensionChallenge;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Bleeding;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Light;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Weakness;
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfLivingEarth;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.Chasm;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.plants.Earthroot;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.RatSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.SkeletonSprite;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

public class Skeleton extends Mob {
	
	{
		HP = HT = 25;
		defenseSkill = 9;
		
		EXP = 5;
		maxLvl = 10;

		loot = Generator.Category.WEAPON;
		lootChance = 0.1667f; //by default, see lootChance()

		properties.add(Property.UNDEAD);
		properties.add(Property.INORGANIC);
	}
	@Override
	public Class<? extends CharSprite> GetSpriteClass() {

		return SkeletonSprite.class;
	}
	
	@Override
	public int damageRoll(boolean isMaxDamage) {
		if (isMaxDamage) return 10;
		return Random.NormalIntRange( 2, 10 );
	}
	
	@Override
	public void die( Object cause ) {
		
		super.die( cause );
		
		if (cause == Chasm.class) return;
		
		boolean heroKilled = false;
		int[] neighbours;
		if (getRandomizerEnabled(RandomTraits.BONE_BOMB)) {
			int width = Dungeon.level.width();
			neighbours = new int[] {
					(-width)*3-3,(-width)*3-2,(-width)*3-1,(-width)*3,(-width)*3+1,(-width)*3+2,(-width)*3+3,
					(-width)*2-3,(-width)*2-2,(-width)*2-1,(-width)*2,(-width)*2+1,(-width)*2+2,(-width)*2+3,
					(-width)*1-3,(-width)*1-2,(-width)*1-1,(-width)*1,(-width)*1+1,(-width)*1+2,(-width)*1+3,
					          -3,          -2,          -1,/*no      ,*/         1,           2,           3,
					 (width)*1-3, (width)*1-2, (width)*1-1, (width)*1, (width)*1+1, (width)*1+2, (width)*1+3,
					 (width)*2-3, (width)*2-2, (width)*2-1, (width)*2, (width)*2+1, (width)*2+2, (width)*2+3,
					 (width)*3-3, (width)*3-2, (width)*3-1, (width)*3, (width)*3+1, (width)*3+2, (width)*3+3
			};

		} else {
			neighbours = PathFinder.NEIGHBOURS8;
		}

		for (int i = 0; i < neighbours.length; i++) {
			if (pos + neighbours[i] < 0) continue;

			Char ch = findChar( pos + neighbours[i] );
			if (ch != null && ch.isAlive()) {
				int damage = Math.round(Random.NormalIntRange(6, 12));

				if (cause == this && getRandomizerEnabled(RandomTraits.KAMIKAZE_BONES)) {
					damage *= 3;
				}

				if (getRandomizerEnabled(RandomTraits.FIZZLED_EXPLOSION)) {
					damage /= 4;
				}

				damage = Math.round( damage * AscensionChallenge.statModifier(this));

				//all sources of DR are 2x effective vs. bone explosion
				//this does not consume extra uses of rock armor and earthroot armor

				WandOfLivingEarth.RockArmor rockArmor = ch.buff(WandOfLivingEarth.RockArmor.class);
				if (rockArmor != null) {
					int preDmg = damage;
					damage = rockArmor.absorb(damage);
					damage *= Math.round(damage/(float)preDmg); //apply the % reduction twice
				}

				Earthroot.Armor armor = ch.buff( Earthroot.Armor.class );
				if (damage > 0 && armor != null) {
					int preDmg = damage;
					damage = armor.absorb( damage );
					damage -= (preDmg - damage); //apply the flat reduction twice
				}

				//apply DR twice (with 2 rolls for more consistency)
				damage = Math.max( 0,  damage - (ch.drRoll() + ch.drRoll()) );
				ch.damage( damage, this );
				if (ch == Dungeon.hero && !ch.isAlive()) {
					heroKilled = true;
				}
			}
		}
		
		if (Dungeon.level.heroFOV[pos]) {
			Sample.INSTANCE.play( Assets.Sounds.BONES );
		}
		
		if (heroKilled) {
			Dungeon.fail( this );
			GLog.n( Messages.get(this, "explo_kill") );
		}
	}

	@Override
	public int attackProc( Char enemy, int damage ) {
		if (getRandomizerEnabled(RandomTraits.KAMIKAZE_BONES)) {
			die(this);
			return 0;
		}

		damage = super.attackProc( enemy, damage );

		if (getRandomizerEnabled(RandomTraits.DRAINING_TOUCH)) {
			if (damage > 0 && Random.Int(2) == 0) {
				Buff.affect(enemy, Weakness.class);
			}
		}

		return damage;
	}

	@Override
	public float lootChance() {
		//each drop makes future drops 1/3 as likely
		// so loot chance looks like: 1/6, 1/18, 1/54, 1/162, etc.
		return super.lootChance() * (float)Math.pow(1/3f, Dungeon.LimitedDrops.SKELE_WEP.count);
	}

	@Override
	public Item createLoot() {
		Dungeon.LimitedDrops.SKELE_WEP.count++;
		return super.createLoot();
	}

	@Override
	public int attackSkill( Char target ) {
		if (getRandomizerEnabled(RandomTraits.BRITTLE_JOINTS)) return 2;
		return 12;
	}
	
	@Override
	public int drRoll() {
		return super.drRoll() + Random.NormalIntRange(0, 5);
	}


	public enum RandomTraits {
		BONE_BOMB, DRAINING_TOUCH, KAMIKAZE_BONES, FIZZLED_EXPLOSION, HOLLOW_SOCKETS, BRITTLE_JOINTS
	}

	public static boolean getRandomizerEnabled(RandomTraits r) {
		switch (r) {
			case BONE_BOMB: return Randomizer.getCreatureBuff(Skeleton.class) == 1;
			case DRAINING_TOUCH: return Randomizer.getCreatureBuff(Skeleton.class) == 2;
			case KAMIKAZE_BONES: return Randomizer.getCreatureBuff(Skeleton.class) == 3;
			case FIZZLED_EXPLOSION: return Randomizer.getCreatureNerf(Skeleton.class) == 1;
			case HOLLOW_SOCKETS: return Randomizer.getCreatureNerf(Skeleton.class) == 2;
			case BRITTLE_JOINTS: return Randomizer.getCreatureNerf(Skeleton.class) == 3;
		}
		return false;
	}
}