package com.shatteredpixel.shatteredpixeldungeon.sprites;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Shaman;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.UnholyPriest;
import com.shatteredpixel.shatteredpixeldungeon.effects.MagicMissile;
import com.watabou.noosa.TextureFilm;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Callback;

public class UnholyPriestSprite extends MobSprite {
    @Override
    public void setup() {
        super.setup();
        zap = attack.clone();
    }

    @Override
    protected void setupFrames() {
        texture( Assets.Sprites.UNHOLY_PRIEST );

        TextureFilm frames = new TextureFilm( texture, 16, 16 );

        idle = new Animation( 1, true );
        idle.frames( frames, 0);

        run = new Animation( 12, true );
        run.frames( frames, 1, 2, 3, 4 );

        attack = new Animation( 12, false );
        attack.frames( frames, 6, 7, 8, 5 );

        die = new Animation( 12, false );
        die.frames( frames, 0, 9, 10, 11, 12 );
    }

    public void zap( int cell ) {

        super.zap( cell );

        MagicMissile.boltFromChar( parent,
                MagicMissile.CORROSION,
                this,
                cell,
                new Callback() {
                    @Override
                    public void call() {
                        ((UnholyPriest)ch).onZapComplete();
                    }
                } );
        Sample.INSTANCE.play( Assets.Sounds.ZAP );
    }

    @Override
    public void onComplete( Animation anim ) {
        if (anim == zap) {
            idle();
        }
        super.onComplete( anim );
    }
}
