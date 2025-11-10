package com.shatteredpixel.shatteredpixeldungeon.actors.mobs;

import com.shatteredpixel.shatteredpixeldungeon.Constants;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.combat.DamageType;
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.Potion;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfStrength;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.watabou.utils.Random;
import com.watabou.utils.Reflection;

public class Spitter extends Mob {
    @Override
    public Constants.mobs.mobsBase GetConstants() { return Constants.mobs.spitter; }

    @Override
    protected boolean canAttack( Char enemy ) {
        return !Dungeon.level.adjacent( pos, enemy.pos )
                && (super.canAttack(enemy) || new Ballistica( pos, enemy.pos, Ballistica.PROJECTILE).collisionPos == enemy.pos);
    }

    @Override
    public boolean getCloser(int target) {
        if (state == HUNTING) {
            return m_EnemySeen.Get() && getFurther( target );
        } else {
            return super.getCloser( target );
        }
    }

    @Override
    public void aggro(Char ch) {
        //cannot be aggroed to something it can't see
        //skip this check if FOV isn't initialized
        if (ch == null || fieldOfView == null
                || fieldOfView.length != Dungeon.level.length() || fieldOfView[ch.pos]) {
            super.aggro(ch);
        }
    }

    @Override
    public Item createLoot(int itemSlot) {
        Class<?extends Potion> loot;
        do{
            loot = (Class<? extends Potion>) Random.oneOf(Generator.Category.POTION.classes);
        } while (loot == PotionOfStrength.class);

        return Reflection.newInstance(loot);
    }
}
