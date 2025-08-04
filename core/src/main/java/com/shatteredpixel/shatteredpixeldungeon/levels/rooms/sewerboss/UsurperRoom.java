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

package com.shatteredpixel.shatteredpixeldungeon.levels.rooms.sewerboss;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Goo;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Rat;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.RatUsurper;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.levels.painters.Painter;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.Room;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.standard.CrossRoom;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.standard.StandardRoom;
import com.shatteredpixel.shatteredpixeldungeon.tiles.CustomTilemap;
import com.watabou.noosa.Image;
import com.watabou.noosa.Tilemap;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Point;
import com.watabou.utils.PointF;
import com.watabou.utils.Random;

public class UsurperRoom extends CrossRoom {

    @Override
    public float[] sizeCatProbs() {
        return new float[]{0, 0, 1};
    }

    @Override
    public boolean canMerge(Level l, Room other, Point p, int mergeTerrain) {
        return false;
    }

    @Override
    public boolean canPlaceGrass(Point p){
        return false;
    }

    @Override
    public void paint(Level level) {
        super.paint(level);

        RatUsurper boss = new RatUsurper();
        boss.pos = level.pointToCell(center());
        level.mobs.add( boss );

        for (int neighbourOffset : PathFinder.NEIGHBOURS4) {
            Rat r = new Rat();
            r.pos = boss.pos + neighbourOffset;
            level.mobs.add(r);
        }

        setupBanners(level);
    }

    protected void setupBanners( Level level ) {
        Point center = center();

        Point leftBannerPos = new Point(center.x - 3, center.y - 2);
        Point rightBannerPos = new Point(center.x + 3, center.y - 2);

        BannerWall leftBanner = new BannerWall();
        BannerWall rightBanner = new BannerWall();
        leftBanner.setRect(leftBannerPos.x, leftBannerPos.y, 1, 1);
        rightBanner.setRect(rightBannerPos.x, rightBannerPos.y, 1, 1);

        level.customTiles.add(leftBanner);
        level.customTiles.add(rightBanner);
    }
    public static class BannerWall extends CustomTilemap {

        {
            texture = Assets.Environment.SEWER_BOSS;
        }

        @Override
        public Tilemap create() {
            Tilemap v = super.create();

            int[] data = new int[tileW * tileH];
            for (int i = 0; i < data.length; i++) {
                data[i] = 9;
            }

            v.map(data, tileW);
            return v;
        }

        @Override
        public Image image(int tileX, int tileY) {
            return null;
        }
    }
}
