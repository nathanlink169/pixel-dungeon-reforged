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

package com.shatteredpixel.shatteredpixeldungeon.levels.traps;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;

import java.util.ArrayList;

public class PressurePlateTrap extends Trap {

	public static final int ACTIVE_ID = 105;
	public static final int INACTIVE_ID = 106;

	private ArrayList<Integer> m_Gates = new ArrayList<>();

	{
		color = GREEN;
		shape = CROSSHAIR;

		canBeHidden = false;
		avoidsHallways = true;
	}

	public void AddGateToLower(int gate) {
		if (active) {
			m_Gates.add(gate);
		} else {
			if (Dungeon.level.map[gate] == Terrain.LOCKED_GATE) {
				Level.set(gate, Terrain.UNLOCKED_GATE);
				GameScene.updateMap( gate );
			}
		}
	}

	@Override
	public void activate() {
		Sample.INSTANCE.play( Assets.Sounds.CLICK );

		for (int i = 0; i < m_Gates.size(); ++i) {
			if (Dungeon.level.map[m_Gates.get(i)] == Terrain.LOCKED_GATE) {
				Level.set(m_Gates.get(i), Terrain.UNLOCKED_GATE);
				GameScene.updateMap( m_Gates.get(i) );
			}
		}
	}

	private static final String GATES_KEY = "gates";

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);

		int[] arr = new int[m_Gates.size()];
		for (int i = 0; i < m_Gates.size(); i++) {
			arr[i] = m_Gates.get(i);
		}
		bundle.put(GATES_KEY, arr);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		int[] arr = bundle.getIntArray(GATES_KEY);
		m_Gates = new ArrayList<>();
		for (int value : arr) {
			m_Gates.add(value);
		}
	}
}
