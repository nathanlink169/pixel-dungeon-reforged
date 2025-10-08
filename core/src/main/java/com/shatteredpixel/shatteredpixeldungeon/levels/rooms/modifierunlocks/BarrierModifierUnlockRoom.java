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

import com.shatteredpixel.shatteredpixeldungeon.items.ChallengeCup;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfLevitation;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfLiquidFlame;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.levels.painters.Painter;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.GatewayTrap;
import com.watabou.utils.Point;
import com.watabou.utils.Rect;

public class BarrierModifierUnlockRoom extends ModifierUnlockRoom {

	@Override
	public int minWidth() { return 5; }
	@Override
	public int minHeight() { return 7; }
	@Override
	public int maxWidth() { return 5; }
	@Override
	public int maxHeight() { return 7; }

	@Override
	public void paint(Level level) {

		Painter.fill( level, this, Terrain.WALL );
		Painter.fill( level, this, 1, Terrain.WATER );
		Painter.set(level, new Point(left + 2, top + 1), Terrain.PEDESTAL);

		for (int i = left + 1; i <= left + 3; ++i) {
			Painter.set(level, new Point(i, top + 2), Terrain.BARRICADE);
			Painter.set(level, new Point(i, top + 4), Terrain.BARRICADE);
		}

		level.drop(new ChallengeCup(), level.pointToCell(new Point(left + 2, top + 1)));

		level.addItemToSpawn(new PotionOfLiquidFlame()); // Requires 3

		for (Door d : connected.values()) {
			d.set(Door.Type.BARRICADE);
		}
	}

	@Override
	public boolean canConnect(Point p) {
		if (!super.canConnect(p)){
			return false;
		}

		if (p.x == left && p.y == bottom - 1) {
			return true;
		}
		if (p.x == right - 1 && p.y == bottom - 1) {
			return true;
		}
		if (p.x != left && p.y != right && p.y == bottom) {
			return true;
		}
		return false;
	}
}
