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

package com.shatteredpixel.shatteredpixeldungeon.sprites;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.watabou.noosa.Image;
import com.watabou.noosa.TextureFilm;
import com.watabou.noosa.tweeners.AlphaTweener;
import com.watabou.utils.Callback;
import com.watabou.utils.Random;
import com.watabou.utils.RectF;

public class HalfRipperSprite extends MobSprite {

    private Animation stab;

    private boolean alt = Random.Int(2) == 0;

    @Override
    protected void setupFrames() {
        texture( Assets.Sprites.RIPPER );

        TextureFilm frames = new TextureFilm( texture, 15, 14 );

        final int offset = 17;

        idle = new Animation( 4, true );
        idle.frames( frames, 1 + offset, 0 + offset, 1 + offset, 2 + offset );

        run = new Animation( 15, true );
        run.frames( frames, 3 + offset, 4 + offset, 5 + offset, 6 + offset, 7 + offset, 8 + offset );

        attack = new Animation( 12, false );
        attack.frames( frames, 0 + offset, 9 + offset, 10 + offset, 9 + offset );

        stab = new Animation( 12, false );
        stab.frames( frames, 0 + offset, 9 + offset, 11 + offset,  + offset );

        die = new Animation( 15, false );
        die.frames( frames, 1 + offset, 13 + offset, 14 + offset, 15 + offset, 16 + offset );
    }

    @Override
    protected void setupFramesMonsterUnknown() {
        super.setupFramesMonsterUnknown();
        stab = attack.clone();
    }

    @Override
    public void attack( int cell ) {
        super.attack( cell );
        if (alt) {
            play( stab );
        }
        alt = !alt;
    }


    public void ascend() {
        sleeping = false;
        processStateRemoval( State.PARALYSED );

        hideEmo();

        if (health != null){
            health.killAndErase();
        }

        parent.add( new AlphaTweener( this, 0, 1.0f ) {
            @Override
            protected void onComplete() {
                HalfRipperSprite.this.killAndErase();
            }
        } );
    }

    public void killInstant() {
        sleeping = false;
        processStateRemoval( State.PARALYSED );

        hideEmo();

        if (health != null){
            health.killAndErase();
        }

        HalfRipperSprite.this.killAndErase();
    }

    @Override
    public void onComplete( Animation anim ) {
        super.onComplete( anim == stab ? attack : anim );
    }

}
