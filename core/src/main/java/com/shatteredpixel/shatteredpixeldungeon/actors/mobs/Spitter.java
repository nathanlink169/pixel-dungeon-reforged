package com.shatteredpixel.shatteredpixeldungeon.actors.mobs;

import com.shatteredpixel.shatteredpixeldungeon.Challenges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Cripple;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Light;
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.Potion;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfHealing;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfStrength;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.sprites.AcidicSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.RatSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.SpitterSprite;
import com.watabou.utils.Random;
import com.watabou.utils.Reflection;

public class Spitter extends Mob {
    {
        HP = HT = 4;
        defenseSkill = 2;

        EXP = 2;
        maxLvl = 7;

        baseSpeed = 0.5f;

        loot = Generator.Category.POTION;
        lootChance = 0.1f;
    }
    @Override
    public Class<? extends CharSprite> GetSpriteClass() {
        return SpitterSprite.class;
    }

    @Override
    public float attackDelay() {
        return super.attackDelay() * 2.0f;
    }

    @Override
    public int damageRoll(boolean isMaxDamage) {
        if (isMaxDamage) return 4;
        return Random.NormalIntRange( 2, 4 );
    }

    @Override
    public int attackSkill( Char target ) {
        return 10;
    }

    @Override
    protected boolean canAttack( Char enemy ) {
        return !Dungeon.level.adjacent( pos, enemy.pos )
                && (super.canAttack(enemy) || new Ballistica( pos, enemy.pos, Ballistica.PROJECTILE).collisionPos == enemy.pos);
    }

    @Override
    protected boolean getCloser( int target ) {
        if (state == HUNTING) {
            return enemySeen && getFurther( target );
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
    public Item createLoot() {
        Class<?extends Potion> loot;
        do{
            loot = (Class<? extends Potion>) Random.oneOf(Generator.Category.POTION.classes);
        } while (loot == PotionOfStrength.class);

        return Reflection.newInstance(loot);
    }
}
