package com.shatteredpixel.shatteredpixeldungeon.items.scrolls;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Doom;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Healing;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mimic;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.combat.DamageType;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;

import java.util.ArrayList;

public class ScrollKillAll extends Scroll {

    {
        icon = ItemSpriteSheet.Icons.SCROLL_PSIBLAST;
    }

    @Override
    public void doRead() {
        GameScene.flash( 0x80000000 );

        //scales from 0x to 1x power, maxing at ~10% HP
        float hpPercent = (curUser.GetMaxHP() - curUser.HP)/(float)(curUser.GetMaxHP());
        float power = Math.min( 4f, 4.45f*hpPercent);

        Sample.INSTANCE.play( Assets.Sounds.BLAST );
        GLog.i(Messages.get(this, "blast"));

        ArrayList<Mob> targets = new ArrayList<>();

        for (Mob mob : Dungeon.level.mobs.toArray( new Mob[0] )) {
            if (mob.alignment == Char.Alignment.ENEMY || mob instanceof Mimic) {
                targets.add(mob);
            }
        }

        for (Mob mob : targets){
            mob.Damage(100000, this, DamageType.of(DamageType.NONE));
            if (mob.isAlive()) {
                Buff.affect(mob, Doom.class);
            }
        }

        Dungeon.observe();

        identify();

        readAnimation();

        Healing healing = Buff.affect(Dungeon.hero, Healing.class);
        healing.setHeal((int) (0.8f * Dungeon.hero.GetMaxHP() + 14), 0.25f, 0);
        healing.applyVialEffect();

    }

    @Override
    public int value() {
        return isKnown() ? 40 * quantity : super.value();
    }
}
