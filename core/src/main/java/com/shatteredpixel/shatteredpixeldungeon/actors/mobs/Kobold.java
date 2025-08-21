package com.shatteredpixel.shatteredpixeldungeon.actors.mobs;

import com.shatteredpixel.shatteredpixeldungeon.Constants;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Dread;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Spear;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CrystalSpireSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.KoboldSprite;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;
import com.watabou.utils.Reflection;

public class Kobold extends Mob
{
    @Override
    public Constants.mobs.mobsBase GetConstants() { return Constants.mobs.kobold; }

    @Override
    public Class<? extends CharSprite> GetSpriteName() {
        if (m_SpriteVariant == -1) {
            m_SpriteVariant = Random.Int(3);
        }

        switch (m_SpriteVariant){
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
            for (Mob m : Dungeon.level.mobs) {
                if (m instanceof Wyrm) {
                    if (m.state != m.SLEEPING) {
                        d = Buff.affect(this, Dread.class);
                        d.permanent = true;
                    }
                }
            }
        }

        return super.act();
    }
}
