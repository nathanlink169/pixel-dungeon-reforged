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

import com.shatteredpixel.shatteredpixeldungeon.items.Dewdrop;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.watabou.utils.Bundle;
import com.watabou.utils.FileUtils;
import com.watabou.utils.Random;

import java.io.IOException;
import java.util.HashSet;

public class Challenges {

	//Some of these internal IDs are outdated and don't represent what these challenges do
	public static final int NO_FOOD				= 1 << 0;
	public static final int NO_ARMOR			= 1 << 1;
	public static final int NO_HEALING			= 1 << 2;
	public static final int NO_HERBALISM		= 1 << 3;
	public static final int SWARM_INTELLIGENCE	= 1 << 4;
	public static final int DARKNESS			= 1 << 5;
	public static final int NO_SCROLLS		    = 1 << 6;
	public static final int CHAMPION_ENEMIES	= 1 << 7;
	public static final int STRONGER_BOSSES 	= 1 << 8;
	public static final int HORDE				= 1 << 9;
	public static final int MONSTER_UNKNOWN		= 1 << 10;
	public static final int TRINKET_MADNESS		= 1 << 11;
	public static final int RANDOMIZER			= 1 << 12;
	public static final int ADAPTIVE			= 1 << 13;

	public static final int MAX_VALUE           = 1 << 14;

	public static final String[] NAME_IDS = {
			"champion_enemies",
			"stronger_bosses",
			"no_food",
			"no_armor",
			"no_healing",
			"no_herbalism",
			"swarm_intelligence",
			"darkness",
			"no_scrolls",
			"horde",
			"monster_unknown",
			"trinket_madness",
			"randomizer",
			"adaptive"
	};

	public static final int[] MASKS = {
			CHAMPION_ENEMIES,
			STRONGER_BOSSES,
			NO_FOOD,
			NO_ARMOR,
			NO_HEALING,
			NO_HERBALISM,
			SWARM_INTELLIGENCE,
			DARKNESS,
			NO_SCROLLS,
			HORDE,
			MONSTER_UNKNOWN,
			TRINKET_MADNESS,
			RANDOMIZER,
			ADAPTIVE
	};

	public static int activeChallenges(){
		int chCount = 0;
		for (int ch : Challenges.MASKS){
			if ((Dungeon.challenges & ch) != 0) chCount++;
		}
		return chCount;
	}

	public static boolean isItemBlocked( Item item ){

		if (Dungeon.isChallenged(NO_HERBALISM) && item instanceof Dewdrop){
			return true;
		}

		return false;

	}

	public static float NoFoodMultiplier() { return 0.4f; }

	public static boolean HasAnyChallengesToUnlock() {
		load();
		int fullIndex = 0;
		for (int ch : Challenges.MASKS){
			fullIndex |= ch;
		}
		return fullIndex != m_UnlockedChallenges;
	}

	private static int m_UnlockedChallenges = 0;
	public static int UnlockRandomChallenge() {
		if (SPDSettings.creative())
			return -1;
		boolean valid = false;
		int toUnlock = -1;
		int index = -1;
		do {
			index = Random.Int(MASKS.length);
			toUnlock = MASKS[index];
			valid = !IsChallengeUnlocked(toUnlock);
		} while (!valid);
		m_UnlockedChallenges |= toUnlock;
		Save();
		return index;
	}

	public static boolean IsChallengeUnlocked(int mask) {
		//return (m_UnlockedChallenges & mask) != 0;
		return mask == STRONGER_BOSSES;
	}

	public static final String UNLOCKED_CHALLENGES_KEY = "unlocked_challenges_key";
	public static final String CHALLENGES_FILE	= "challenges.dat";

	public static void Save() {
		Bundle bundle = new Bundle();
		store( bundle );

		try {
			FileUtils.bundleToFile(CHALLENGES_FILE, bundle);
		} catch (IOException e) {
			ShatteredPixelDungeon.reportException(e);
		}
	}

	public static void store( Bundle bundle ) {
		bundle.put( UNLOCKED_CHALLENGES_KEY, m_UnlockedChallenges );
	}

	public static void load() {
		try {
			Bundle bundle = FileUtils.bundleFromFile( CHALLENGES_FILE );
			m_UnlockedChallenges = bundle.getInt(UNLOCKED_CHALLENGES_KEY);

		} catch (IOException e) {
			m_UnlockedChallenges = 0;
		}
	}
}