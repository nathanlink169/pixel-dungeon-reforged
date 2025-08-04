package com.shatteredpixel.shatteredpixeldungeon.actors.mobs;

import com.shatteredpixel.shatteredpixeldungeon.Challenges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Blob;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Fire;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Dread;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Spear;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.AcidicSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CrystalWispSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.GnollGuardSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.KoboldSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.RatSprite;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.utils.Bundle;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

public class Kobold extends Mob
{
    private Class<? extends CharSprite> spriteClass = null;

    {
        HP = HT = 35;
        defenseSkill = 15;

        EXP = 7;
        maxLvl = -2;

        loot = Spear.class;
        lootChance = 0.1f;

        properties.add(Property.FIERY);
    }
    @Override
    public Class<? extends CharSprite> GetSpriteClass() {
        if (spriteClass == null) {
            switch (Random.Int(3)){
                case 0: default:
                    spriteClass = KoboldSprite.Blue.class;
                    break;
                case 1:
                    spriteClass = KoboldSprite.Red.class;
                    break;
                case 2:
                    spriteClass = KoboldSprite.Purple.class;
                    break;
            }
        }
        return spriteClass;
    }

    @Override
    public int damageRoll(boolean isMaxDamage) {
        if (isMaxDamage) return 4;
        return Random.NormalIntRange( 1, 4 );
    }

    @Override
    public int attackSkill( Char target ) {
        return 20;
    }

    @Override
    public int drRoll() {
        return super.drRoll() + Random.NormalIntRange(0, 4);
    }

    public static final String SPRITE = "sprite";

    @Override
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);
        bundle.put(SPRITE, spriteClass);
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        spriteClass = bundle.getClass(SPRITE);
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
