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

package com.shatteredpixel.shatteredpixeldungeon.levels.rooms.modifierunlocks;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.SPDSettings;
import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.Room;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.secret.SecretArtilleryRoom;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.secret.SecretChestChasmRoom;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.secret.SecretGardenRoom;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.secret.SecretHoardRoom;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.secret.SecretHoneypotRoom;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.secret.SecretLaboratoryRoom;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.secret.SecretLarderRoom;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.secret.SecretLibraryRoom;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.secret.SecretMazeRoom;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.secret.SecretRunestoneRoom;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.secret.SecretSummoningRoom;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.secret.SecretWellRoom;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.special.SpecialRoom;
import com.watabou.utils.Bundle;
import com.watabou.utils.Point;
import com.watabou.utils.Random;
import com.watabou.utils.Reflection;

import java.util.ArrayList;
import java.util.Arrays;


public abstract class ModifierUnlockRoom extends Room {
	
	
	private static final ArrayList<Class<? extends ModifierUnlockRoom>> ALL_SECRETS = new ArrayList<>( Arrays.asList(
			LevitationModifierUnlockRoom.class, BarrierModifierUnlockRoom.class, SummoningModifierUnlockRoom.class, PressurePlateModifierUnlockRoom.class));
	
	private static int m_DepthToSpawn = -1;
	public static int GetDepth() { return m_DepthToSpawn; }

	public static void initForRun() {
		if (!Dungeon.creative) {
			do {
				m_DepthToSpawn = Random.Int(25);
			} while (m_DepthToSpawn % 5 == 0);
		} else {
			m_DepthToSpawn = -1;
		}
	}

	@Override
	public int maxConnections(int direction) {
		return 1;
	}
	
	public static ModifierUnlockRoom createRoom(){
		return Reflection.newInstance(ALL_SECRETS.get(Random.Int(ALL_SECRETS.size())));
	}
	
	private static final String DEPTH_TAG = "modifier_unlock_room_depth";
	
	public static void restoreRoomsFromBundle( Bundle bundle ) {
		if (bundle.contains( DEPTH_TAG )) {
			m_DepthToSpawn = bundle.getInt(DEPTH_TAG);
		} else {
			initForRun();
			ShatteredPixelDungeon.reportException(new Exception("modifier unlock room data didn't exist!"));
		}
	}
	
	public static void storeRoomsInBundle( Bundle bundle ) {
		bundle.put(DEPTH_TAG, m_DepthToSpawn);
	}

	@Override
	public boolean canPlaceWater(Point p){
		return false;
	}

	@Override
	public boolean canPlaceGrass(Point p){
		return false;
	}

	@Override
	public boolean canPlaceTrap(Point p){
		return false;
	}

	@Override
	public boolean canPlaceItem(Point p, Level l){
		return false;
	}
	@Override
	public boolean canPlaceCharacter(Point p, Level l){
		return false;
	}

}
