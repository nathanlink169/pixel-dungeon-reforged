package com.shatteredpixel.shatteredpixeldungeon.actors.mobs;

import com.shatteredpixel.shatteredpixeldungeon.Constants;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Dread;
import com.shatteredpixel.shatteredpixeldungeon.combat.DamageType;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.KoboldSprite;
import com.watabou.utils.Random;

public class Kobold extends Mob
{
    @Override
    public Constants.mobs.mobsBase GetConstants() { return Constants.mobs.kobold; }

    @Override
    public Class<? extends CharSprite> GetSpriteName() {
        if (m_SpriteVariant.Get() == -1) {
            m_SpriteVariant.Set(Random.Int(3));
        }

        switch (m_SpriteVariant.Get()) {
            case 0: default:
                return KoboldSprite.Blue.class;
            case 1:
                return KoboldSprite.Red.class;
            case 2:
                return KoboldSprite.Purple.class;
        }
    }


    @Override
    public boolean act() {
        Dread d = buff(Dread.class);
        if (d == null) {
            boolean foundWyrm = false;
            for (Mob m : Dungeon.level.mobs) {
                if (m instanceof Wyrm) {
                    if (m.state != m.SLEEPING) {
                        d = Buff.affect(this, Dread.class);
                        d.permanent = true;
                    }
                    foundWyrm = true;
                    break;
                }
            }
            if (!foundWyrm) {
                d = Buff.affect(this, Dread.class);
                d.permanent = true;
            }
        }

        return super.act();
    }
}
