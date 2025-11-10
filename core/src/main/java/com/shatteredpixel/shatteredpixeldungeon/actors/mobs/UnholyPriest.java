package com.shatteredpixel.shatteredpixeldungeon.actors.mobs;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Badges;
import com.shatteredpixel.shatteredpixeldungeon.Constants;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.AscensionChallenge;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Bleeding;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Bless;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Cursed;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invulnerability;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Poison;
import com.shatteredpixel.shatteredpixeldungeon.combat.AttackContext;
import com.shatteredpixel.shatteredpixeldungeon.combat.AttackResult;
import com.shatteredpixel.shatteredpixeldungeon.combat.CombatResolver;
import com.shatteredpixel.shatteredpixeldungeon.combat.DamageType;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Callback;
import com.watabou.utils.Random;

public class UnholyPriest extends Mob implements Callback {
    {
        immunities.add(Bleeding.class);
        immunities.add(Poison.class);
    }
    @Override
    public Constants.mobs.mobsBase GetConstants() { return Constants.mobs.unholypriest; }

    @Override
    protected boolean canAttack( Char enemy ) {
        return super.canAttack(enemy)
                || new Ballistica( pos, enemy.pos, Ballistica.MAGIC_BOLT).collisionPos == enemy.pos;
    }

    @Override
    public boolean doAttack(Char enemy) {
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
        // Build attack context
        AttackContext context = new AttackContext.Builder(this, enemy)
                .attackType(AttackContext.AttackType.RANGED)
                .damageType(GetRangedDamageType())
                .build();

        // Resolve attack - this handles EVERYTHING internally
        AttackResult result = CombatResolver.resolve(context);
        if (result.result == AttackResult.ResultType.HIT) {
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

            int dmg = damageRoll(AttackContext.AttackType.RANGED, false);
            dmg = Math.round(dmg * AscensionChallenge.statModifier(this));
            enemy.Damage( dmg, new CursedBolt(), DamageType.of(DamageType.NEGATIVE_ENERGY));

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
