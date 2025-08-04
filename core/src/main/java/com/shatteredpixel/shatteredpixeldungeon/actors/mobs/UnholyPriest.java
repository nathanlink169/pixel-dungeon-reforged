package com.shatteredpixel.shatteredpixeldungeon.actors.mobs;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Badges;
import com.shatteredpixel.shatteredpixeldungeon.Challenges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.AscensionChallenge;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Bless;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.ChampionEnemy;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Cursed;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invulnerability;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Vulnerable;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.SparkParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.AcidicSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.DM100Sprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.RatSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ShamanSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.UnholyPriestSprite;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Callback;
import com.watabou.utils.Random;

public class UnholyPriest extends Mob implements Callback {

    {
        HP = HT = 20;
        defenseSkill = 4;

        EXP = 6;
        maxLvl = 13;

        loot = Generator.Category.SCROLL;
        lootChance = 0.25f;

        properties.add(Property.DEMONIC);
    }
    @Override
    public Class<? extends CharSprite> GetSpriteClass() {
        return UnholyPriestSprite.class;
    }

    @Override
    public int damageRoll(boolean isMaxDamage) {
        if (isMaxDamage) return 4;
        return Random.NormalIntRange( 1, 4 );
    }

    @Override
    public int attackSkill( Char target ) {
        return 11;
    }

    @Override
    public int drRoll() {
        return super.drRoll() + Random.NormalIntRange(0, 4);
    }

    @Override
    protected boolean canAttack( Char enemy ) {
        return super.canAttack(enemy)
                || new Ballistica( pos, enemy.pos, Ballistica.MAGIC_BOLT).collisionPos == enemy.pos;
    }

    @Override
    protected boolean doAttack( Char enemy ) {

        if (Dungeon.level.adjacent( pos, enemy.pos )
                || new Ballistica( pos, enemy.pos, Ballistica.MAGIC_BOLT).collisionPos != enemy.pos) {

            return super.doAttack( enemy );

        } else {

            if (sprite != null && (sprite.visible || enemy.sprite.visible)) {
                sprite.zap( enemy.pos );
                return false;
            } else {
                zap();
                return true;
            }
        }
    }

    public void onZapComplete() {
        zap();
        next();
    }

    //used so resistances can differentiate between melee and magical attacks
    public static class CursedBolt{}

    private void zap() {
        spend( 1f );

        Invisibility.dispel(this);
        Char enemy = this.enemy;
        if (hit( this, enemy, true )) {

            if (Random.Int( 2 ) == 0) {
                boolean isBlessed = false;

                for (Buff b : enemy.buffs()){
                    if (b instanceof Bless || b instanceof Invulnerability){
                        isBlessed = true;
                        break;
                    }
                }

                if (!isBlessed) {
                    Buff.prolong(enemy, Cursed.class, Cursed.DURATION);
                    if (enemy == Dungeon.hero) Sample.INSTANCE.play(Assets.Sounds.DEBUFF);
                }
            }

            int dmg = Random.NormalIntRange( 1, 4 );
            dmg = Math.round(dmg * AscensionChallenge.statModifier(this));
            enemy.damage( dmg, new CursedBolt() );

            if (!enemy.isAlive() && enemy == Dungeon.hero) {
                Badges.validateDeathFromEnemyMagic();
                Dungeon.fail( this );
                GLog.n( Messages.get(this, "bolt_kill") );
            }
        } else {
            enemy.sprite.showStatus( CharSprite.NEUTRAL,  enemy.defenseVerb() );
        }
    }

    @Override
    public void call() {
        next();
    }

}
