/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2024 Evan Debenham
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

package com.shatteredpixel.shatteredpixeldungeon.levels.rooms.standard;

import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Piranha;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.levels.painters.Painter;
import com.watabou.utils.Point;
import com.watabou.utils.Rect;

public class CrossRoom extends StandardRoom {

    @Override
    public float[] sizeCatProbs() {
        return new float[]{0, 2, 1};
    }

    @Override
    public Rect resize(int w, int h) {
        super.resize(w, h);
        if (width() % 2 == 0) right--;
        if (height() % 2 == 0) bottom--;
        return this;
    }

    @Override
    public boolean canConnect(Point p){
        int width = width();
        int height = height();

        if (width % 2 == 0) {
            --width;
        }
        if (height % 2 == 0) {
            --height;
        }

        // Integer division cuts off the decimal
        int widthMidpoint = width / 2;
        int heightMidpoint = height / 2;

        // points must be directly in the center.
        boolean validLeft = (p.x == left) && (p.y == top + heightMidpoint);
        boolean validRight = (p.x == right) && (p.y == top + heightMidpoint);
        boolean validTop = (p.x == left + widthMidpoint) && (p.y == top);
        boolean validBottom = (p.x == left + widthMidpoint) && (p.y == bottom);
        return validLeft || validRight || validTop || validBottom;
    }

    @Override
    public boolean canPlaceItem(Point p, Level l) {int width = width();
        int height = height();

        if (width % 2 == 0) {
            --width;
        }
        if (height % 2 == 0) {
            --height;
        }

        // Integer division cuts off the decimal
        int widthMidpoint = width / 2;
        int heightMidpoint = height / 2;

        // points must be directly in the center.
        boolean validLeft = (p.x == left + 2) && (p.y == top + heightMidpoint);
        boolean validRight = (p.x == right - 2) && (p.y == top + heightMidpoint);
        boolean validTop = (p.x == left + widthMidpoint) && (p.y == top + 2);
        boolean validBottom = (p.x == left + widthMidpoint) && (p.y == bottom - 2);

        return super.canPlaceItem(p, l) && (validLeft || validRight || validTop || validBottom);
    }

    @Override
    public void paint(Level level) {
        int width = width();
        int height = height();

        if (width % 2 == 0) {
            --width;
        }
        if (height % 2 == 0) {
            --height;
        }

        // Integer division cuts off the decimal
        int widthMidpoint = width / 2;
        int heightMidpoint = height / 2;

        // ( Level level, int x, int y, int w, int h, int value )
        // Left to Right Walls
        Painter.fill (level, this.left, this.top+heightMidpoint - 2, width, 5, Terrain.WALL);
        // Top to Bottom Walls
        Painter.fill (level, this.left + widthMidpoint - 2, this.top, 5, height, Terrain.WALL);
        // Left to Right Floors
        Painter.fill (level, this.left + 1, this.top + heightMidpoint - 1, width - 2, 3, Terrain.EMPTY);
        // Top to Bottom Floors
        Painter.fill (level, this.left + widthMidpoint - 1, this.top + 1, 3, height - 2, Terrain.EMPTY);

        for (Door door : connected.values()) {
            door.set( Door.Type.REGULAR );
        }
    }

}
