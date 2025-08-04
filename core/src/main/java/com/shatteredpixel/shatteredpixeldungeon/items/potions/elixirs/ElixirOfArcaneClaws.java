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

package com.shatteredpixel.shatteredpixeldungeon.items.potions.elixirs;

import com.shatteredpixel.shatteredpixeldungeon.Challenges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MindVision;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.effects.FloatingText;
import com.shatteredpixel.shatteredpixeldungeon.effects.SpellSprite;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfHealing;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfMindVision;
import com.shatteredpixel.shatteredpixeldungeon.items.quest.GooBlob;
import com.shatteredpixel.shatteredpixeldungeon.items.quest.RatClaw;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.DamageType;
import com.shatteredpixel.shatteredpixeldungeon.journal.Catalog;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.Image;
import com.watabou.utils.Bundle;
import com.watabou.utils.GameMath;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class ElixirOfArcaneClaws extends Elixir {

    {
        image = ItemSpriteSheet.ELIXIR_CLAWS;
    }

    @Override
    public void apply(Hero hero) {
        Buff.affect(hero, ArcaneClaws.class).set(Math.round(hero.HT * 4f));

        identify();
        Buff.prolong( hero, MindVision.class, MindVision.DURATION );
        SpellSprite.show(hero, SpellSprite.VISION, 1, 0.77f, 0.9f);
        Dungeon.observe();

        if (Dungeon.level.mobs.size() > 0) {
            GLog.i( Messages.get(this, "see_mobs") );
        } else {
            GLog.i( Messages.get(this, "see_none") );
        }
    }

    public static class ArcaneClaws extends Buff {

        {
            type = buffType.POSITIVE;
            announced = true;
        }

        private int left;

        public void set( int amount ){
            if (amount > left) left = amount;
        }

        public void extend( float duration ) {
            left += duration;
        }

        @Override
        public boolean act() {
            if (target.invisible <= 0) {
                for (Mob m : Dungeon.level.mobs) {
                    if (Dungeon.level.adjacent(target.pos, m.pos)) {
                        if (m.alignment == Char.Alignment.ENEMY) {
                            int damage = Math.min(left, Random.Int(4) + 1); // 1-4
                            m.damage(damage, Dungeon.hero, DamageType.MAGIC);
                            left -= damage;
                            if (left <= 0) {
                                break;
                            }
                        }
                    }
                }
            }

            if (left <= 0){
                detach();
                if (target instanceof Hero) {
                    ((Hero) target).resting = false;
                }
            } else {
                spend(TICK);
            }
            return true;
        }

        @Override
        public int icon() {
            return BuffIndicator.THORNS;
        }

        @Override
        public void tintIcon(Image icon) {
            icon.hardlight(0, 0.75f, 0.75f);
        }

        @Override
        public float iconFadePercent() {
            float max = Math.round(target.HT * 1.5f);
            return Math.max(0, (max - left) / max);
        }

        @Override
        public String iconTextDisplay() {
            return Integer.toString(left);
        }

        @Override
        public String desc() {
            return Messages.get(this, "desc", left);
        }

        private static final String LEFT = "left";

        @Override
        public void storeInBundle( Bundle bundle ) {
            super.storeInBundle( bundle );
            bundle.put( LEFT, left );
        }

        @Override
        public void restoreFromBundle( Bundle bundle ) {
            super.restoreFromBundle( bundle );
            left = bundle.getInt( LEFT );

        }
    }

    public static class Recipe extends com.shatteredpixel.shatteredpixeldungeon.items.Recipe.SimpleRecipe {

        {
            inputs =  new Class[]{PotionOfMindVision.class, RatClaw.class};
            inQuantity = new int[]{1, 1};

            cost = 6;

            output = ElixirOfArcaneClaws.class;
            outQuantity = 1;
        }

        @Override
        public Item brew(ArrayList<Item> ingredients) {
            Catalog.countUse(RatClaw.class);
            return super.brew(ingredients);
        }
    }

}
