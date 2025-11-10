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

package com.shatteredpixel.shatteredpixeldungeon.items.wands;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Paralysis;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.combat.DamageType;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MagesStaff;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Random;

public class WandOfDisplacement extends DamageWand {

    {
        image = ItemSpriteSheet.WAND_DISPLACEMENT;
        collisionProperties = Ballistica.PROJECTILE;
    }

    public int min(int lvl){
        return 1+lvl;
    }
    public int max(int lvl) { return 4*lvl; }

    @Override
    public void onZap(Ballistica bolt) {
        Char ch = Actor.findChar( bolt.collisionPos );
        if (ch != null) {
            wandProc(ch, chargesPerCast());
            swap(Item.curUser, ch);
            ch.Damage(damageRoll(), this, DamageType.of(DamageType.BLUDGEONING));
            Sample.INSTANCE.play( Assets.Sounds.HIT_MAGIC, 1, Random.Float(0.87f, 1.15f) );

        } else {
            Dungeon.level.pressCell(bolt.collisionPos);
        }
    }

    private void moveChar(Char ch, int pos) {
        ch.sprite.move(ch.pos, pos);
        ch.move(pos);
    }

    private void swap(Char ch1, Char ch2) {
        int pos1 = ch1.pos;
        moveChar(ch1, ch2.pos);
        moveChar(ch2, pos1);
    }

    @Override
    public void onHit(MagesStaff staff, Char attacker, Char defender, int damage) {
        if (Random.Float() < 0.25f) {
            Buff.prolong(defender, Paralysis.class, Random.Float(1.0f, 2.0f));
            defender.sprite.emitter().burst(Speck.factory(Speck.LIGHT), 12);
        }
    }

    @Override
    public void staffFx(MagesStaff.StaffParticle particle) {
        particle.color( 0x9efaff ); particle.am = 0.6f;
        particle.setLifespan(0.6f);
        particle.acc.set(0.0f, 40.0f);
        particle.setSize( 2.0f, 4.0f);
        particle.shuffleXY(2.0f);
    }
}
