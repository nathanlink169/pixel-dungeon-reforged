package com.shatteredpixel.shatteredpixeldungeon.actors.mobs;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.Blacksmith;
import com.shatteredpixel.shatteredpixeldungeon.effects.Splash;
import com.shatteredpixel.shatteredpixeldungeon.items.Gold;
import com.shatteredpixel.shatteredpixeldungeon.items.quest.Pickaxe;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.GnollGeomancerSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.BossHealthBar;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;
import com.watabou.utils.Random;

import java.util.ArrayList;
import java.util.List;

public class Wyrm extends Mob
{
    {
        HP = HT = 150;
        spriteClass = GnollGeomancerSprite.class;

        EXP = 20;

        //acts after other mobs, just like sappers
        actPriority = MOB_PRIO-1;

        SLEEPING = new Wyrm.Sleeping();
        HUNTING = new Wyrm.Hunting();
        state = SLEEPING;

        //FOV is used to attack hero when they are in open space created by geomancer
        // but geomancer will lose sight and stop attacking if the hero flees behind walls.
        // Because of this geomancer can see through high grass and shrouding fod
        viewDistance = 12;

        properties.add(Property.BOSS);
        properties.add(Property.FIERY);
    }

    private int abilityCooldown = Random.NormalIntRange(2, 4);

    @Override
    protected boolean act() {
        return super.act();
    }

    @Override
    public boolean add(Buff buff) {
        //immune to buffs and debuff (except its own buffs) while sleeping
        if (state == SLEEPING){
            return false;
        } else {
            return super.add(buff);
        }
    }

    @Override
    public int damageRoll(boolean isMaxDamage) {
        if (isMaxDamage) return 12;
        return Random.NormalIntRange( 6, 12 );
    }

    @Override
    public int attackSkill( Char target ) {
        return 30;
    }

    @Override
    public int drRoll() {
        return super.drRoll() + Random.NormalIntRange(0, 6);
    }

    @Override
    public boolean reset() {
        return true;
    }

    @Override
    public float spawningWeight() {
        return 0;
    }

    @Override
    protected boolean getCloser(int target) {
        return false;
    }

    @Override
    protected boolean getFurther(int target) {
        return false;
    }

    @Override
    public boolean interact(Char c) {
        if (c != Dungeon.hero) {
            return super.interact(c);
        } else {
            final Pickaxe p = Dungeon.hero.belongings.getItem(Pickaxe.class);

            if (p == null){
                return true;
            }

            Dungeon.hero.sprite.attack(pos, new Callback() {
                @Override
                public void call() {
                    //does its own special damage calculation that's only influenced by pickaxe level and augment
                    //we pretend the wyrm is the owner here so that properties like hero str or or other equipment do not factor in
                    int dmg = p.damageRoll(Wyrm.this, false);

                    boolean wasSleeping = state == SLEEPING;

                    damage(dmg, p);
                    sprite.bloodBurstA(Dungeon.hero.sprite.center(), dmg);
                    sprite.flash();

                    if (wasSleeping){
                        GLog.n( Messages.get(Wyrm.this, "alert"));
                        spend(TICK);
                        sprite.idle();

                        state = HUNTING;
                        enemy = Dungeon.hero;
                        BossHealthBar.assignBoss(Wyrm.this);

                        for (Mob m : Dungeon.level.mobs){
                            if (m instanceof Kobold){
                                m.aggro(Dungeon.hero);
                            }
                        }
                    }

                    Sample.INSTANCE.play(Assets.Sounds.MINE, 1f, Random.Float(0.85f, 1.15f));
                    Invisibility.dispel(Dungeon.hero);
                    Dungeon.hero.spendAndNext(p.delayFactor(Wyrm.this));
                }
            });

            return false;
        }
    }

    @Override
    public void damage(int dmg, Object src) {
        int hpBracket = HT / 3;

        int curbracket = HP / hpBracket;
        if (curbracket == 3) curbracket--; //full HP isn't its own bracket

        inFinalBracket = curbracket == 0;

        super.damage(dmg, src);

        abilityCooldown -= dmg/10f;

        int newBracket =  HP / hpBracket;
        if (newBracket == 3) newBracket--; //full HP isn't its own bracket

        if (newBracket != curbracket) {
            //cannot be hit through multiple brackets at a time
            if (HP <= (curbracket-1)*hpBracket){
                HP = (curbracket-1)*hpBracket + 1;
            }

            BossHealthBar.bleed(newBracket <= 0);

        }
    }

    private boolean inFinalBracket = false;

    @Override
    public boolean isAlive() {
        //cannot die until final HP bracket, regardless of incoming dmg
        return super.isAlive() || !inFinalBracket;
    }

    @Override
    public String description() {
        if (state == SLEEPING){
            return Messages.get(this, "desc_sleeping");
        } else {
            return super.description();
        }
    }

    @Override
    public void die(Object cause) {
        super.die(cause);
        Blacksmith.Quest.beatBoss();
        Sample.INSTANCE.playDelayed(Assets.Sounds.ROCKS, 0.1f);
        PixelScene.shake( 3, 0.7f );

        List<Integer> li = new ArrayList<>();
        for (int i = 0; i < Dungeon.level.length(); i++){
            if ((Dungeon.level.map[i] == Terrain.EMPTY || Dungeon.level.map[i] == Terrain.EMPTY_DECO || Dungeon.level.map[i] == Terrain.WATER) && Dungeon.level.trueDistance(i, pos) <= 3){
                if (Random.Int(0,4) <= 2) { // 75%
                    li.add(i);
                }
            }
        }

        int goldCount = Random.IntRange(400, 600);
        int goldPerTile = goldCount / li.size();

        for (int i = 0; i < li.size(); ++i) {
            Dungeon.level.drop(new Gold((int)(goldPerTile * Random.Float(0.9f, 1.1f))), li.get(i)).sprite.drop();
        }
    }

    @Override
    public void beckon(int cell) {
        if (state == SLEEPING){
            //do nothing
        } else {
            super.beckon(cell);
        }
    }

    public Boolean hasSeen = false;
    private class Sleeping extends Mob.Sleeping {

        @Override
        protected void awaken(boolean enemyInFOV) {
            //do nothing, has special awakening rules
        }

        @Override
        public boolean act(boolean enemyInFOV, boolean justAlerted) {
            return super.act(enemyInFOV, justAlerted);
        }
    }

    public void Observe() {
        // only need to warn the user if this is the first time seeing the wyrm
        if (!hasSeen) {
            hasSeen = true;
            GLog.n( Messages.get(Wyrm.this, "warning"));
        }
    }

    private class Hunting extends Mob.Hunting {

        @Override
        public boolean act(boolean enemyInFOV, boolean justAlerted) {
            if (!enemyInFOV){
                spend(TICK);
                return true;
            } else {
                enemySeen = true;

                if (abilityCooldown-- <= 0){

                }

                spend(TICK);
                return true;
            }
        }

    }

    private static final String ABILITY_COOLDOWN = "ability_cooldown";
    private static final String HAS_SEEN = "has_seen";

    @Override
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);
        bundle.put(ABILITY_COOLDOWN, abilityCooldown);
        bundle.put(HAS_SEEN, hasSeen);
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        abilityCooldown = bundle.getInt(ABILITY_COOLDOWN);
        hasSeen = bundle.getBoolean(HAS_SEEN);
        if (state == HUNTING){
            BossHealthBar.assignBoss(this);
        }
    }
}
