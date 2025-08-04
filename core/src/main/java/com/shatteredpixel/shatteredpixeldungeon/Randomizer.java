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

package com.shatteredpixel.shatteredpixeldungeon;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Ballista;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Bat;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Brute;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Crab;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.DM100;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.DM200;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.DemonGoo;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Elemental;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Eye;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Ghoul;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Gnoll;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Golem;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Guard;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Monk;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Necromancer;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Rat;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.RipperDemon;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Scorpio;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Shaman;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Skeleton;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Slime;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Snake;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Spinner;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Spitter;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Succubus;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Swarm;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Thief;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.UnholyPriest;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Warlock;
import com.shatteredpixel.shatteredpixeldungeon.items.Dewdrop;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.journal.Notes;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.BatSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.BruteSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CrabSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.DM100Sprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.DM200Sprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ElementalSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.EyeSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.GhoulSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.GnollSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.GolemSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.GuardSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.MonkSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.NecromancerSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.RatSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.RipperSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ScorpioSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ShamanSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.SkeletonSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.SlimeSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.SnakeSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.SpinnerSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.SuccubusSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.SwarmSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ThiefSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.WarlockSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndRandomizerDisplay;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class Randomizer {
	// Randomizer is bitwise. Each region is 12 bits
	// 0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0000
	//|______________|
	// First Region
	//
 	// Leftmost 4 bits is the buffed creature ID
	// Second 4 bits is the nerfed creature ID
	// Next 2 bits is the buff ID
	// Last 2 bits is the nerf ID
	//
 	// For example: 0001 0101 1011   1253
	// This means creature ID 1 is buffed with buff ID 2.
	//			  creature ID 5 is nerfed with nerf ID 3.
	//
 	// Last four bits represent which is the highest region we've seen the window for.

	public static void initialize() {
		Dungeon.randomizer = 0;

		if (!Dungeon.randomizerEnabled) return;

		setRandomCreaturesForRegion(1);
		setRandomCreaturesForRegion(2);
		setRandomCreaturesForRegion(3);
		setRandomCreaturesForRegion(4);
		setRandomCreaturesForRegion(5);
	}

	public static void showRandomizerWindowIfRequired() {
		if (!Dungeon.randomizerEnabled) return;

		int region = 1;
		if (Dungeon.depth >= 6) region = 2;
		if (Dungeon.depth >= 11) region = 3;
		if (Dungeon.depth >= 16) region = 4;
		if (Dungeon.depth >= 21) region = 5;

		int highestRegion = getBits(0, 4);

		if (highestRegion < region) {
			ShatteredPixelDungeon.scene().add(new WndRandomizerDisplay(region));
			setBits(0, 4, region);
			Notes.add(Notes.Landmark.RANDOMIZER);
		}
	}

	public static int getCreatureBuff(Class<? extends Char> creature) {
		if (!Dungeon.randomizerEnabled) return 0;

		int region = m_creatureData.get(creature).region;
		int bitForRegion = (-12 * region) + 72;
		int buffedCreatureId = getBits(bitForRegion, 4);
		if (buffedCreatureId == m_creatureData.get(creature).id) {
			return getBits(bitForRegion - 6, 2);
		}
		return 0;
	}

	public static int getCreatureNerf(Class<? extends Char> creature) {
		if (!Dungeon.randomizerEnabled) return 0;

		int region = m_creatureData.get(creature).region;
		int bitForRegion = (-12 * region) + 72;
		int nerfedCreatureId = getBits(bitForRegion - 4, 4);
		if (nerfedCreatureId == m_creatureData.get(creature).id) {
			return getBits(bitForRegion - 8, 2);
		}
		return 0;
	}

	public static Class<? extends Char> getBuffedCreature(int region) {
		if (!Dungeon.randomizerEnabled) return null;

		int bitForRegion = (-12 * region) + 72;
		int buffedCreatureId = getBits(bitForRegion, 4);

		Class<? extends Char> foundClass = null;
		for (Map.Entry<Class<? extends Char>, CreatureData> entry : m_creatureData.entrySet()) {
			if (entry.getValue().region == region && entry.getValue().id == buffedCreatureId) {
				foundClass = entry.getKey();
				break;
			}
		}

		return foundClass;
	}

	public static Class<? extends Char> getNerfedCreature(int region) {
		if (!Dungeon.randomizerEnabled) return null;

		int bitForRegion = (-12 * region) + 72;
		int nerfedCreatureId = getBits(bitForRegion - 4, 4);

		Class<? extends Char> foundClass = null;
		for (Map.Entry<Class<? extends Char>, CreatureData> entry : m_creatureData.entrySet()) {
			if (entry.getValue().region == region && entry.getValue().id == nerfedCreatureId) {
				foundClass = entry.getKey();
				break;
			}
		}

		return foundClass;
	}

	private static void setRandomCreaturesForRegion(int region) {
		if (!Dungeon.randomizerEnabled) return;

		List<CreatureData> regionCreatures = m_creatureData.values().stream()
				.filter(data -> data.region == region)
				.collect(Collectors.toList());

		if (regionCreatures.size() >= 2) {
			CreatureData randomCreature1 = regionCreatures.get((int) (Math.random() * regionCreatures.size()));
			regionCreatures.remove(randomCreature1);
			CreatureData randomCreature2 = regionCreatures.get((int) (Math.random() * regionCreatures.size()));
			int bitForRegion = (-12 * region) + 72;
			setBits(bitForRegion, 4, randomCreature1.id);
			setBits(bitForRegion - 4, 4, randomCreature2.id);

			int rand1 = (int)(Math.random() * 3)+1;
			int rand2 = (int)(Math.random() * 3)+1;
			setBits(bitForRegion - 6, 2, rand1);
			setBits(bitForRegion - 8, 2, rand2);
		} else {
			throw new Error("Randomizer could not find creatures!");
		}
	}

	public static void setBits(int startBit, int numBits, int newValue) {
		// Create a mask with the specified number of bits set to 1
		long mask = (1L << numBits) - 1;

		// Clear the target bits in the original value
		long cleared = Dungeon.randomizer & ~(mask << startBit);

		// Set the new bits
		Dungeon.randomizer = cleared | ((long)newValue << startBit);
	}

	private static int getBits(int startBit, int numBits) {
		// Create a mask with the specified number of bits set to 1
		long mask = (1L << numBits) - 1;
		long value = Dungeon.randomizer;
		// Shift the value right to move target bits to the rightmost position
		// Then mask to keep only the bits we want
		return (int)((value >> startBit) & mask);
	}

	private static HashMap<Class<? extends Char>, CreatureData> m_creatureData;
	static {
		m_creatureData = new HashMap<>();
		m_creatureData.put(Rat.class, new CreatureData(1, 1));
		m_creatureData.put(Snake.class, new CreatureData(1, 2));
		m_creatureData.put(Gnoll.class, new CreatureData(1, 3));
		m_creatureData.put(Crab.class, new CreatureData(1, 4));
		// m_creatureData.put(Spitter.class, new CreatureData(1, 5));
		m_creatureData.put(Swarm.class, new CreatureData(1, 6));
		m_creatureData.put(Slime.class, new CreatureData(1, 7));

		m_creatureData.put(Skeleton.class, new CreatureData(2, 1));
		m_creatureData.put(Thief.class, new CreatureData(2, 2));
		m_creatureData.put(DM100.class, new CreatureData(2, 3));
		// m_creatureData.put(UnholyPriest.class, new CreatureData(2, 4));
		m_creatureData.put(Guard.class, new CreatureData(2, 5));
		m_creatureData.put(Necromancer.class, new CreatureData(2, 6));

		m_creatureData.put(Bat.class, new CreatureData(3, 1));
		m_creatureData.put(Brute.class, new CreatureData(3, 2));
		m_creatureData.put(Shaman.class, new CreatureData(3, 3));
		m_creatureData.put(Spinner.class, new CreatureData(3, 4));
		m_creatureData.put(DM200.class, new CreatureData(3, 5));
		// m_creatureData.put(Ballista.class, new CreatureData(3, 6));

		m_creatureData.put(Ghoul.class, new CreatureData(4, 1));
		m_creatureData.put(Elemental.class, new CreatureData(4, 2));
		m_creatureData.put(Warlock.class, new CreatureData(4, 3));
		m_creatureData.put(Monk.class, new CreatureData(4, 4));
		m_creatureData.put(Golem.class, new CreatureData(4, 5));

		m_creatureData.put(RipperDemon.class, new CreatureData(5, 1));
		m_creatureData.put(Succubus.class, new CreatureData(5, 2));
		m_creatureData.put(Eye.class, new CreatureData(5, 3));
		m_creatureData.put(Scorpio.class, new CreatureData(5, 4));
		// m_creatureData.put(DemonGoo.class, new CreatureData(5, 5));
	}

	private static class CreatureData {
		public int region;
		public int id;
		private CreatureData( int region, int id) {
			this.region = region;
			this.id = id;
		}
	}
}