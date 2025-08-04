package com.shatteredpixel.shatteredpixeldungeon.actors.mobs;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Blob;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Fire;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Burning;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.Blacksmith;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.Pushing;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.effects.Splash;
import com.shatteredpixel.shatteredpixeldungeon.items.Gold;
import com.shatteredpixel.shatteredpixeldungeon.items.quest.DarkGold;
import com.shatteredpixel.shatteredpixeldungeon.items.quest.Pickaxe;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.WyrmSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.BossHealthBar;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Point;
import com.watabou.utils.Random;

import java.util.ArrayList;
import java.util.List;

public class Wyrm extends Mob
{
    {
        HP = HT = 150;
        EXP = 20;

        SLEEPING = new Wyrm.Sleeping();
        state = SLEEPING;
        viewDistance = 12;

        properties.add(Property.BOSS);
        properties.add(Property.FIERY);
        properties.add(Property.IMMOVABLE); //moves itself via ability, otherwise is static
    }

    @Override
    public Class<? extends CharSprite> GetSpriteClass() {
        return WyrmSprite.class;
    }

    private int[] dashPositions = null;

    @Override
    protected void onAdd(){
        super.onAdd();

        if (dashPositions == null) {
            dashPositions = new int[4];
            dashPositions[0] = Dungeon.level.pointToCell(new Point(9, 9));
            dashPositions[1] = Dungeon.level.pointToCell(new Point(Dungeon.level.width() - 9, 9));
            dashPositions[2] = Dungeon.level.pointToCell(new Point(9, Dungeon.level.height() - 9));
            dashPositions[3] = Dungeon.level.pointToCell(new Point(Dungeon.level.width() - 9, Dungeon.level.height() - 9));
        }
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
    public float attackDelay() {
        return super.attackDelay() * 2.0f;
    }

    @Override
    public int damageRoll(boolean isMaxDamage) {
        if (isMaxDamage) return 8;
        return Random.NormalIntRange( 2, 8 );
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
    protected boolean canAttack( Char enemy ) {
        //cannot 'curve' hits like the hero, requires fairly open space to hit at a distance
        boolean canHit = Dungeon.level.distance(enemy.pos, pos) <= 2
                            && new Ballistica( pos, enemy.pos, Ballistica.PROJECTILE).collisionPos == enemy.pos
                            && new Ballistica( enemy.pos, pos, Ballistica.PROJECTILE).collisionPos == pos;

        if (!canHit) {
            canHit = !Dungeon.level.adjacent( pos, enemy.pos )
                        && (super.canAttack(enemy) || new Ballistica( pos, enemy.pos, Ballistica.PROJECTILE).collisionPos == enemy.pos);
        }

        return canHit;
    }

    @Override
    public boolean heroShouldInteract() {
        return true;
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
                        state = HUNTING;
                        ((WyrmSprite)sprite).setup();
                        sprite.idle();

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

    protected boolean doAttack( Char enemy ) {
        if (Dungeon.level.distance(enemy.pos, pos) <= 2
                && new Ballistica( pos, enemy.pos, Ballistica.PROJECTILE).collisionPos == enemy.pos
                && new Ballistica( enemy.pos, pos, Ballistica.PROJECTILE).collisionPos == pos) {

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

    protected void zap() {
        spend( attackDelay() );

        Invisibility.dispel(this);
        Char enemy = this.enemy;
        if (hit( this, enemy, true )) {
            if (!Dungeon.level.water[enemy.pos]) {
                Buff.affect( enemy, Burning.class ).reignite( enemy, 4f );
            }
            if (enemy.sprite.visible) Splash.at( enemy.sprite.center(), 0xFFFFBB33, 5);
        } else {
            enemy.sprite.showStatus( CharSprite.NEUTRAL,  enemy.defenseVerb() );
        }
    }

    public void onZapComplete() {
        zap();
        next();
    }

    @Override
    public void damage(int dmg, Object src, int damageType) {
        int hpBracket = HT / 3;

        int curbracket = HP / hpBracket;
        if (curbracket == 3) curbracket--; //full HP isn't its own bracket

        inFinalBracket = curbracket == 0;

        super.damage(dmg, src, damageType);

        int newBracket =  HP / hpBracket;
        if (newBracket == 3) newBracket--; //full HP isn't its own bracket

        if (newBracket != curbracket) {
            //cannot be hit through multiple brackets at a time
            if (HP <= (curbracket-1)*hpBracket){
                HP = (curbracket-1)*hpBracket + 1;
            }

            BossHealthBar.bleed(newBracket <= 0);
            carveRockAndDash();
        }
    }

    private void carveRockAndDash() {
        int dashIndex;
        do {
            dashIndex = Random.Int(4);
        } while (dashPositions[dashIndex] == -1);

        int dashPos = dashPositions[dashIndex];
        dashPositions[dashIndex] = -1;

        // if position is more than 12 tiles away, cap it
        Ballistica path = new Ballistica(pos, dashPos, Ballistica.STOP_TARGET);

        if (path.dist > 15){
            dashPos = path.path.get(15);
        }

        // Find a spot without a character or trap
        if (Actor.findChar(dashPos) != null || Dungeon.level.traps.get(dashPos) != null){
            ArrayList<Integer> candidates = new ArrayList<>();
            for (int i : PathFinder.NEIGHBOURS8){
                if (Actor.findChar(dashPos+i) == null && Dungeon.level.traps.get(dashPos+i) == null){
                    candidates.add(dashPos+i);
                }
            }
            if (!candidates.isEmpty()) {
                dashPos = Random.element(candidates);
            }
        }

        path = new Ballistica(pos, dashPos, Ballistica.STOP_TARGET);

        ArrayList<Integer> cells = new ArrayList<>(path.subPath(0, path.dist));
        cells.addAll(spreadDiamondAOE(cells));
        cells.addAll(spreadDiamondAOE(cells));
        cells.addAll(spreadDiamondAOE(cells));

        ArrayList<Integer> exteriorCells = spreadDiamondAOE(cells);

        for (int i : cells){
            if (Dungeon.level.map[i] == Terrain.WALL_DECO){
                Dungeon.level.drop(new DarkGold(), i).sprite.drop();
                Dungeon.level.map[i] = Terrain.EMPTY_DECO;
            } else if (Dungeon.level.solid[i]){
                Dungeon.level.map[i] = Terrain.EMPTY_DECO;
            } else if (Dungeon.level.map[i] == Terrain.HIGH_GRASS || Dungeon.level.map[i] == Terrain.FURROWED_GRASS){
                Dungeon.level.map[i] = Terrain.GRASS;
            }
            CellEmitter.get( i - Dungeon.level.width() ).start(Speck.factory(Speck.ROCK), 0.07f, 10);
            GameScene.add(Blob.seed(i, 5, Fire.class));
        }
        for (int i : exteriorCells){
            if (!Dungeon.level.solid[i]
                    && Dungeon.level.map[i] != Terrain.EMPTY_SP
                    && !Dungeon.level.adjacent(i, Dungeon.level.entrance())
                    && Dungeon.level.traps.get(i) == null
                    && Dungeon.level.plants.get(i) == null
                    && Actor.findChar(i) == null){
                Dungeon.level.map[i] = Terrain.EMPTY;
            }
        }
        if (Dungeon.level.solid[dashPos]){
            Dungeon.level.map[dashPos] = Terrain.EMPTY_DECO;
        }
        //we potentially update a lot of cells, so might as well just reset properties instead of incrementally updating
        Dungeon.level.buildFlagMaps();
        Dungeon.level.cleanWalls();
        GameScene.updateMap();
        GameScene.updateFog();
        Dungeon.observe();

        PixelScene.shake(3, 0.7f);
        Sample.INSTANCE.play(Assets.Sounds.ROCKS);

        int oldpos = pos;
        pos = dashPos;
        spend(TICK);
        Actor.add(new Pushing(this, oldpos, pos));
    }

    private ArrayList<Integer> spreadDiamondAOE(ArrayList<Integer> currentCells){
        ArrayList<Integer> spreadCells = new ArrayList<>();
        for (int i : currentCells){
            for (int j : PathFinder.NEIGHBOURS4){
                if (Dungeon.level.insideMap(i+j) && !spreadCells.contains(i+j) && !currentCells.contains(i+j)){
                    spreadCells.add(i+j);
                }
            }
        }
        return spreadCells;
    }

    private boolean inFinalBracket = false;

    @Override
    public boolean isAlive() {
        //cannot die until final HP bracket, regardless of incoming dmg
        return super.isAlive() || !inFinalBracket;
    }

    @Override
    public String description(boolean forceNoMonsterUnknown) {
        if (state == SLEEPING){
            return Messages.get(this, "desc_sleeping");
        } else {
            return super.description(forceNoMonsterUnknown);
        }
    }

    @Override
    public boolean act() {
        if (state == HUNTING) {
            for (int i : PathFinder.NEIGHBOURS9) {
                if (Dungeon.level.map[pos + i] == Terrain.WATER){
                    Level.set( pos + i, Terrain.EMPTY);
                    GameScene.updateMap( pos + i );
                    CellEmitter.get( pos + i ).burst( Speck.factory( Speck.STEAM ), 10 );
                }
                else {
                    int vol = Fire.volumeAt(pos + i, Fire.class);
                    if (vol < 4 && !Dungeon.level.water[pos + i] && !Dungeon.level.solid[pos + i]) {
                        GameScene.add(Blob.seed(pos + i, 4 - vol, Fire.class));
                    }
                }
            }
        }
        return super.act();
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
    }

    public void Observe() {
        // only need to warn the user if this is the first time seeing the wyrm
        if (!hasSeen) {
            hasSeen = true;
            GLog.n( Messages.get(Wyrm.this, "warning"));
        }
    }

    private static final String DASH_POSITIONS = "dash_positions";
    private static final String HAS_SEEN = "has_seen";

    @Override
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);
        bundle.put(HAS_SEEN, hasSeen);
        bundle.put(DASH_POSITIONS, dashPositions);
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        hasSeen = bundle.getBoolean(HAS_SEEN);
        dashPositions = bundle.getIntArray(DASH_POSITIONS);
        if (state == HUNTING){
            BossHealthBar.assignBoss(this);
        }
    }
}
