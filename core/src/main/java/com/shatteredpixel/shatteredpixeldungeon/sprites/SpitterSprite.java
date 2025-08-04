package com.shatteredpixel.shatteredpixeldungeon.sprites;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.watabou.noosa.TextureFilm;
import com.watabou.utils.Callback;

public class SpitterSprite extends MobSprite{

    private int cellToAttack;

    @Override
    public void setup() {
        super.setup();
        zap = attack.clone();
    }

    @Override
    protected void setupFrames() {
        perspectiveRaise = 0f;

        texture( Assets.Sprites.SPITTER );

        TextureFilm frames = new TextureFilm( texture, 16, 16 );

        idle = new Animation( 10, true );
        idle.frames( frames, 0, 0, 0, 0, 0, 1, 0, 1 );

        run = new Animation( 15, true );
        run.frames( frames, 0, 2, 0, 3 );

        attack = new Animation( 12, false );
        attack.frames( frames, 0, 4, 5, 0 );

        die = new Animation( 12, false );
        die.frames( frames, 6, 7, 8, 9 );
    }

    @Override
    public void link(Char ch) {
        super.link(ch);
        if (parent != null) {
            parent.sendToBack(this);
            if (aura != null){
                parent.sendToBack(aura);
            }
        }
        renderShadow = false;
    }

    @Override
    public int blood() {
        return 0xFFFF4422;
    }

    @Override
    public void attack( int cell ) {
        if (!Dungeon.level.adjacent( cell, ch.pos )) {

            cellToAttack = cell;
            zap(cell);

        } else {

            super.attack( cell );

        }
    }

    @Override
    public void onComplete( Animation anim ) {
        if (anim == zap) {
            idle();

            ((MissileSprite)parent.recycle( MissileSprite.class )).
                    reset( this, cellToAttack, new SpitterSprite.SpitterShot(), new Callback() {
                        @Override
                        public void call() {
                            ch.onAttackComplete();
                        }
                    } );
        } else {
            super.onComplete( anim );
        }
    }

    public class SpitterShot extends Item {
        {
            image = ItemSpriteSheet.FISHING_SPEAR;
        }
    }
}
