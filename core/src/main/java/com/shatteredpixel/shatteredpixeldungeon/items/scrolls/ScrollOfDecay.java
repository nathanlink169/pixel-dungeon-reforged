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

package com.shatteredpixel.shatteredpixeldungeon.items.scrolls;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Adrenaline;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Bless;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Doom;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Weakness;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.MirrorImage;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.Potion;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class ScrollOfDecay extends Scroll {

    {
        icon = ItemSpriteSheet.Icons.SCROLL_DECAY;
    }

    @Override
    public void doRead() {
        if (curUser.hasTalent(Talent.VOLATILE_SALVAGE)) {
            int rand = Random.NormalIntRange(1, 20);
            int chance;
            if (curUser.pointsInTalent(Talent.VOLATILE_SALVAGE) == 1) {
                chance = 2;
            } else {
                chance = 5;
            }
            if (rand > chance) { // Failed Roll
                detach(curUser.belongings.backpack);
            }
            else {
                GLog.p( Messages.get(Potion.class, "saved") );
            }
        }
        else {
            detach(curUser.belongings.backpack);
        }

        GameScene.flash(0xFF0000);

        int[] neighbours = getAffectedTiles(curUser.pos);
        for (int neighbour : neighbours) {
            if (Dungeon.level.passable[neighbour]) {
                Char ch = Actor.findChar(neighbour);

                if (ch != null) {
                    if (ch != curUser) {
                        if (ch.properties().contains(Char.Property.UNDEAD)) {
                            Buff.affect(ch, Bless.class, Bless.DURATION);
                            Buff.affect(ch, Adrenaline.class, Adrenaline.DURATION);
                        } else if (!ch.isImmune(getClass())) {
                            int damage = (int)Math.max(0.6f * ch.HP, 0.1f * ch.HT);
                            ch.damage(damage, this);
                            Buff.affect(ch, Weakness.class, Weakness.DURATION);

                            if (!ch.properties().contains(Char.Property.BOSS) && !ch.properties().contains(Char.Property.MINIBOSS)) {
                                Buff.affect(ch, Doom.class);
                            }
                        }
                    } else /*if (ch == curUser)*/ {
                        int damage = (int)Math.max(0.3f * ch.HP, 0.05f * ch.HT);
                        ch.damage(damage, this);
                        Buff.affect(ch, Weakness.class, Weakness.DURATION * 0.5f);
                    }
                }


                if (Dungeon.level.flamable[neighbour]) {
                    Level.set(neighbour, Terrain.EMBERS);
                }
            }
        }
        GameScene.updateMap();

        if (curUser.isAlive()) {
            Dungeon.observe();
        } else {
            Dungeon.fail(getClass());
            GLog.n(Messages.get(this, "ondeath"));
        }

        identify();

        Sample.INSTANCE.play( Assets.Sounds.READ );

        readAnimation();
    }

    public static int[] getAffectedTiles(int pos)
    {
        int w = Dungeon.level.width();
        int[] neighbours = new int[] { pos - (w * 2) - 2, pos - (w * 2) - 1, pos - (w * 2), pos - (w * 2) + 1, pos - (w * 2) + 2,
                                       pos - w - 2,       pos - w - 1,       pos - w,       pos - w + 1,       pos - w + 2,
                                       pos - 2,           pos - 1,           pos,           pos + 1,           pos + 2,
                                       pos + w - 2,       pos + w - 1,       pos + w,       pos + w + 1,       pos + w + 2,
                                       pos + (w * 2) - 2, pos + (w * 2) - 1, pos + (w * 2), pos + (w * 2) + 1, pos + (w * 2) + 2};
        return neighbours;
    }

    @Override
    public int value() {
        return isKnown() ? 30 * quantity : super.value();
    }
}
