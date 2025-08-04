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

package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.items.Heap;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class Awareness extends FlavourBuff {
	
	{
		type = buffType.POSITIVE;
	}

	public enum Type {
		HalfItem, FullItem, HalfMonster, FullMonster
	}

	public static final float DURATION = 2f;

	@Override
	public boolean attachTo( Char target ) {
		boolean attached = super.attachTo(target);

		if (attached) {
			setType(Type.FullItem);
		}

		return attached;
	}

	@Override
	public void detach() {
		super.detach();
		Dungeon.observe();
		GameScene.updateFog();
	}

	private ArrayList<Integer> positions = null;
	public ArrayList<Integer> getPositions() { return positions; }

	public void setType(Type type) {
		if (positions == null) {
			positions = new ArrayList<>();
		}
		positions.clear();
		int size;
		switch (type) {
			case HalfItem:
				for (Heap heap : Dungeon.level.heaps.valueList()) {
					positions.add(heap.pos);
				}
				size = positions.size();
				for (int i = 0; i < size / 2; ++i) {
					positions.remove(Random.Int(positions.size()));
				}
				break;
			case FullItem:
				for (Heap heap : Dungeon.level.heaps.valueList()) {
					positions.add(heap.pos);
				}
				break;
			case HalfMonster:
				for (Mob m : Dungeon.level.mobs) {
					positions.add(m.pos);
				}
				size = positions.size();
				for (int i = 0; i < size / 2; ++i) {
					positions.remove(Random.Int(positions.size()));
				}
				for (Heap heap : Dungeon.level.heaps.valueList()) {
					positions.add(heap.pos);
				}
				break;
			case FullMonster:
				for (Mob m : Dungeon.level.mobs) {
					positions.add(m.pos);
				}
				for (Heap heap : Dungeon.level.heaps.valueList()) {
					positions.add(heap.pos);
				}
				break;
		}
	}
}
