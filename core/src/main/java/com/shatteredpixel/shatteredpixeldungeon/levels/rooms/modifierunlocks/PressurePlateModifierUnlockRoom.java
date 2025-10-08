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
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Blob;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.CorrosiveGas;
import com.shatteredpixel.shatteredpixeldungeon.items.ChallengeCup;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.levels.painters.Painter;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.special.ToxicGasRoom;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.PressurePlateTrap;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.Trap;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Point;

import java.util.ArrayList;
import java.util.Collections;

public class PressurePlateModifierUnlockRoom extends ModifierUnlockRoom {

	@Override
	public int minWidth() { return 11; }
	@Override
	public int minHeight() { return 11; }
	@Override
	public int maxWidth() { return 11; }
	@Override
	public int maxHeight() { return 11; }

	@Override
	public void paint(Level level) {
		final int centerPos = (minWidth() - 1) / 2;

		final int topLeftCell = level.pointToCell(new Point(left + 1, top + 1));
		final int topRightCell = level.pointToCell(new Point(right - 1, top + 1));
		final int bottomLeftCell = level.pointToCell(new Point(left + 1, bottom - 1));
		final int bottomRightCell = level.pointToCell(new Point(right - 1, bottom - 1));
		final int center = level.pointToCell(new Point(left + centerPos, top + centerPos));

		Painter.fill( level, this, Terrain.WALL );
		Painter.fill( level, this, 1, Terrain.EMPTY );
		Painter.set(level, level.cellToPoint(center), Terrain.PEDESTAL);
		level.drop(new ChallengeCup(), center);

		for (int n : PathFinder.NEIGHBOURS8) {
			Painter.set(level, center + n, Terrain.LOCKED_GATE);

			if (level.map[topLeftCell + n] == Terrain.EMPTY) {
				Painter.set(level, topLeftCell + n, Terrain.LOCKED_GATE);
			}
			if (level.map[topRightCell + n] == Terrain.EMPTY) {
				Painter.set(level, topRightCell + n, Terrain.LOCKED_GATE);
			}
			if (level.map[bottomLeftCell + n] == Terrain.EMPTY) {
				Painter.set(level, bottomLeftCell + n, Terrain.LOCKED_GATE);
			}
			if (level.map[bottomRightCell + n] == Terrain.EMPTY) {
				Painter.set(level, bottomRightCell + n, Terrain.LOCKED_GATE);
			}
		}

		SetupCorrossiveTrap(level, level.pointToCell(new Point(left + centerPos - 2, top + centerPos)));
		SetupCorrossiveTrap(level, level.pointToCell(new Point(left + centerPos + 2, top + centerPos)));
		SetupCorrossiveTrap(level, level.pointToCell(new Point(left + centerPos, top + centerPos - 2)));
		SetupCorrossiveTrap(level, level.pointToCell(new Point(left + centerPos, top + centerPos + 2)));

		final PressurePlateTrap startingTrap = (PressurePlateTrap) new PressurePlateTrap().reveal();
		final PressurePlateTrap topLeftCornerTrap = (PressurePlateTrap) new PressurePlateTrap().reveal();
		final PressurePlateTrap topRightCornerTrap = (PressurePlateTrap) new PressurePlateTrap().reveal();
		final PressurePlateTrap bottomLeftCornerTrap = (PressurePlateTrap) new PressurePlateTrap().reveal();
		final PressurePlateTrap bottomRightCornerTrap = (PressurePlateTrap) new PressurePlateTrap().reveal();

		level.setTrap(topLeftCornerTrap, topLeftCell);
		Painter.set(level, topLeftCell, Terrain.TRAP);
		level.setTrap(topRightCornerTrap, topRightCell);
		Painter.set(level, topRightCell, Terrain.TRAP);
		level.setTrap(bottomLeftCornerTrap, bottomLeftCell);
		Painter.set(level, bottomLeftCell, Terrain.TRAP);
		level.setTrap(bottomRightCornerTrap, bottomRightCell);
		Painter.set(level, bottomRightCell, Terrain.TRAP);

		// 0 top, 1 left, 2 right, 3 bottom
		int doorLocation = -1;
		for (Door d : connected.values()) {
			d.set(Door.Type.UNLOCKED);
			if (d.x == left) doorLocation = 1;
			if (d.x == right) doorLocation = 2;
			if (d.y == top) doorLocation = 0;
			if (d.y == bottom) doorLocation = 3;
		}

		final ArrayList<PressurePlateTrap> corners = new ArrayList<>();
		corners.add(topLeftCornerTrap);
		corners.add(topRightCornerTrap);
		corners.add(bottomLeftCornerTrap);
		corners.add(bottomRightCornerTrap);

		Collections.shuffle(corners);

		int startingTrapCell = 0;
		if (doorLocation == 0)
			startingTrapCell = level.pointToCell(new Point(left + centerPos, bottom - 1));
		if (doorLocation == 1)
			startingTrapCell = level.pointToCell(new Point(right - 1, top + centerPos));
		if (doorLocation == 2)
			startingTrapCell = level.pointToCell(new Point(left + 1, top + centerPos));
		if (doorLocation == 3)
			startingTrapCell = level.pointToCell(new Point(left + centerPos, top + 1));

		level.setTrap(startingTrap, startingTrapCell);
		Painter.set(level, startingTrapCell, Terrain.TRAP);

		for (int n : PathFinder.NEIGHBOURS8) {
			if (level.map[corners.get(0).pos + n] == Terrain.LOCKED_GATE) {
				startingTrap.AddGateToLower(corners.get(0).pos + n);
			}
			if (level.map[corners.get(1).pos + n] == Terrain.LOCKED_GATE) {
				corners.get(0).AddGateToLower(corners.get(1).pos + n);
			}
			if (level.map[corners.get(2).pos + n] == Terrain.LOCKED_GATE) {
				corners.get(1).AddGateToLower(corners.get(2).pos + n);
			}
			if (level.map[corners.get(3).pos + n] == Terrain.LOCKED_GATE) {
				corners.get(2).AddGateToLower(corners.get(3).pos + n);
			}
			corners.get(3).AddGateToLower(center + n);
		}
	}

	private void SetupCorrossiveTrap(Level level, int cell) {
		level.setTrap(new CorrosiveVent().reveal(), cell);
		Blob.seed(cell, 50, CorrosiveGasSeed.class, level);
		Painter.set(level, cell, Terrain.INACTIVE_TRAP);
	}

	@Override
	public boolean canConnect(Point p) {
		if (!super.canConnect(p)){
			return false;
		}

		if ((p.x == left || p.x == right) && p.y > top + 2 && p.y < bottom - 2) {
			return true;
		}
		if ((p.y == top || p.y == bottom) && p.x > left + 2 && p.x < right - 2) {
			return true;
		}
		return false;
	}

	public static class CorrosiveGasSeed extends Blob {

		@Override
		protected void evolve() {
			int cell;
			CorrosiveGas gas = (CorrosiveGas) Dungeon.level.blobs.get(CorrosiveGas.class);
			for (int i=area.top-1; i <= area.bottom; i++) {
				for (int j = area.left-1; j <= area.right; j++) {
					cell = j + i* Dungeon.level.width();
					if (Dungeon.level.insideMap(cell)) {
						if (Dungeon.level.map[cell] != Terrain.INACTIVE_TRAP){
							off[cell] = 0;
							continue;
						}

						off[cell] = cur[cell];
						volume += off[cell];

						if (gas == null || gas.volume == 0){
							GameScene.add(Blob.seed(cell, off[cell], CorrosiveGas.class));
						} else if (gas.cur[cell] <= 9*off[cell]){
							GameScene.add(Blob.seed(cell, off[cell], CorrosiveGas.class));
						}
					}
				}
			}
		}

	}

	public static class CorrosiveVent extends Trap {

		{
			color = BLACK;
			shape = GRILL;

			canBeHidden = false;
			active = false;
		}

		@Override
		public void activate() {
			//does nothing, this trap is just decoration and is always deactivated
		}

	}
}
