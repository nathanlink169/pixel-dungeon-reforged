package com.shatteredpixel.shatteredpixeldungeon.items.scrolls;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.AscensionChallenge;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

// Debug only class
public class ScrollTeleportToStaircase extends Scroll {

    {
        icon = ItemSpriteSheet.Icons.SCROLL_PASSAGE;
    }

    @Override
    public void doRead() {
        for (int i = 0; i < Dungeon.level.map.length; ++i) {
            if (Dungeon.hero.buff(AscensionChallenge.class) != null || Dungeon.level.map[Dungeon.hero.pos] == Terrain.EXIT) {
                if (Dungeon.level.map[i] == Terrain.ENTRANCE) {
                    ScrollOfTeleportation.appear(Dungeon.hero, i);
                    break;
                }
            } else {
                if (Dungeon.level.map[i] == Terrain.EXIT) {
                    ScrollOfTeleportation.appear(Dungeon.hero, i);
                    break;
                }
            }
        }

        Dungeon.observe();

        identify();

        readAnimation();
    }

    @Override
    public int value() {
        return isKnown() ? 30 * quantity : super.value();
    }
}
