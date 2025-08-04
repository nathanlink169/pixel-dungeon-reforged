/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2025 Evan Debenham
 *
 * Pixel Dungeon Reforged
 * Copyright (C) 2024-2025 Nathan Pringle
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs;

import com.shatteredpixel.shatteredpixeldungeon.Challenges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.Statistics;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.AscensionChallenge;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.RipperDemon;
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.PlateArmor;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.Artifact;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.DriedRose;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.EtherealChains;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.SandalsOfNature;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.TimekeepersHourglass;
import com.shatteredpixel.shatteredpixeldungeon.items.quest.DarkGold;
import com.shatteredpixel.shatteredpixeldungeon.items.quest.Pickaxe;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfTransmutation;
import com.shatteredpixel.shatteredpixeldungeon.items.trinkets.ParchmentScrap;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MeleeWeapon;
import com.shatteredpixel.shatteredpixeldungeon.journal.Notes;
import com.shatteredpixel.shatteredpixeldungeon.levels.HallsLevel;
import com.shatteredpixel.shatteredpixeldungeon.levels.RegularLevel;
import com.shatteredpixel.shatteredpixeldungeon.levels.SewerLevel;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.Chasm;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.Room;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.quest.BlacksmithRoom;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.BlacksmithSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.HalfRipperSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.MobSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.RatSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.SpawnerSprite;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndBlacksmith;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndHalfRipperRewards;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndQuest;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndSadGhost;
import com.watabou.noosa.Game;
import com.watabou.utils.BArray;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

public class HalfRipper extends NPC {

    {
        properties.add(Property.DEMONIC);

        HP = HT = 60;
        defenseSkill = 16;

        WANDERING = new HalfRipper.Wandering();
        state = WANDERING;
    }

    protected class Wandering extends Mob.Wandering{
        @Override
        protected int randomDestination() {
            int pos = super.randomDestination();
            //cannot wander onto heaps or the level exit
            if (Dungeon.level.heaps.get(pos) != null ||
                pos == Dungeon.level.exit() ||
                !((RegularLevel)Dungeon.level).GetExitRoom().inside(Dungeon.level.cellToPoint(pos)))
            {
                return -1;
            }
            return pos;
        }

        @Override
        public boolean act( boolean enemyInFOV, boolean justAlerted ) {
            if (Quest.started() && !Quest.failed && !Quest.completed()) {
                HalfRipper.this.state = new HalfRipper.Escaping();
                HalfRipper.this.alignment = Alignment.ALLY;
                return true;
            }
            return super.act(enemyInFOV, justAlerted);
        }
    }

    protected class Escaping implements AiState {

        public static final String TAG	= "ESCAPING";

        private boolean goingForStairs = false;

        @Override
        public boolean act( boolean enemyInFOV, boolean justAlerted ) {
            int dungeonPosition = Dungeon.level.entrance();
            if (pos == dungeonPosition) {
                if (Quest.corrupted && Dungeon.depth != 24) {
                    yell(Messages.get(HalfRipper.class, "ascend_corrupted"));
                }
                else {
                    yell(Messages.get(HalfRipper.class, "ascend"));
                }
                HalfRipper.this.ascend();
                return true;
            }

            int oldPos = pos;
            target = Dungeon.hero.pos;
            PathFinder.buildDistanceMap(dungeonPosition, BArray.or(Dungeon.level.passable, Dungeon.level.avoid, null));

            if (PathFinder.distance[pos] <= 5){
                if (getCloser(dungeonPosition)) {
                    if (!goingForStairs) {
                        if (Quest.corrupted && Dungeon.depth != 24) {
                            yell(Messages.get(HalfRipper.class, "stairs_corrupted"));
                        }
                        else {
                            yell(Messages.get(HalfRipper.class, "stairs"));
                        }
                        goingForStairs = true;
                    }
                    spend( 1 / speed() );
                    return moveSprite( oldPos, pos );
                }
            }

            goingForStairs = false;
            // if we get here, we couldn't find or move towards the stairs. Follow the hero instead
            if (getCloser( target )) {
                spend( 1 / speed() );
                return moveSprite( oldPos, pos );
            } else {
                //if it can't move closer to hero, then try to attack something nearby
                HashSet<Char> enemies = new HashSet<>();
                for (Mob mob : Dungeon.level.mobs) {
                    if (mob.alignment == Alignment.ENEMY && Dungeon.level.adjacent( pos, mob.pos )
                            && mob.invisible <= 0 && !mob.isInvulnerable(getClass()) && !isCharmedBy(mob))
                        //do not target passive mobs
                        //intelligent allies also don't target mobs which are wandering or asleep
                        if (mob.state != mob.PASSIVE &&
                                (!intelligentAlly || (mob.state != mob.SLEEPING && mob.state != mob.WANDERING))) {
                            enemies.add(mob);
                        }
                }
                if (!enemies.isEmpty()) {
                    int item = Random.Int(enemies.size());
                    int i = 0;
                    Char halfRipperTarget = null;
                    for (Char potentialTarget : enemies) {
                        if (i == item) {
                            halfRipperTarget = potentialTarget;
                            break;
                        }
                    }

                    if (halfRipperTarget != null)
                        doAttack(halfRipperTarget);
                }
                spend( TICK );
            }
            return true;
        }
    }

    @Override
    public Class<? extends CharSprite> GetSpriteClass() {
        return HalfRipperSprite.class;
    }

    @Override
    public Notes.Landmark landmark() {
        return (!Quest.completed() && !Quest.failed()) ? Notes.Landmark.HALF_RIPPER : null;
    }

    public void ascend() {
        if (Dungeon.depth == 21) {
            // Finished!
            Statistics.questScores[4] += 2000;
            Quest.complete();
            Game.runOnRenderThread(new Callback() {
                @Override
                public void call() {
                    GameScene.show(new WndHalfRipperRewards(generateRewards()));
                }
            });
        } else {
            Statistics.questScores[4] += 1000;
            Quest.depth--;
        }
        destroy();
        ((HalfRipperSprite)sprite).ascend();
    }

    private Item[] generateRewards() {
        Item[] toReturn = new Item[6];

        // Melee weapons
        MeleeWeapon w1 = Generator.halfRipperReweard();
        MeleeWeapon w2;
        do {
            w2 = Generator.halfRipperReweard();
        } while(w2.isSimilar(w1));

        toReturn[0] = upgradeRewardWeapon(w1);
        toReturn[1] = upgradeRewardWeapon(w2);

        // Armour
        PlateArmor armour = new PlateArmor();
        armour.level(0);
        armour.cursed = false;

        // 20%: +4, 50%: +5, 30%: +6
        float itemLevelRoll = Random.Float();
        if (itemLevelRoll < 0.2f){
            armour.upgrade(4);
        } else if (itemLevelRoll < 0.7f){
            armour.upgrade(5);
        } else {
            armour.upgrade(6);
        }

        float itemEnchantRoll = Random.Float();
        if (itemEnchantRoll < 0.5f) {
            armour.inscribe(Armor.Glyph.randomUncommon());
        } else {
            armour.inscribe(Armor.Glyph.randomRare());
        }
        toReturn[2] = armour;

        // Artifact
        toReturn[3] = Generator.randomArtifact();
        if (toReturn[3] == null) {
            toReturn[3] = Generator.random(Generator.Category.RING);
        }
        toReturn[3].level(0);

        itemLevelRoll = Random.Float();
        if (toReturn[3] instanceof EtherealChains || toReturn[3] instanceof TimekeepersHourglass) {
            if (itemLevelRoll < 0.5f){
                toReturn[3].upgrade(2);
            } else {
                toReturn[3].upgrade(3);
            }
        } else if (toReturn[3] instanceof SandalsOfNature) {
            toReturn[3].upgrade(2);
        } else {
            if (itemLevelRoll < 0.2f) {
                toReturn[3].upgrade(4);
            } else if (itemLevelRoll < 0.7f) {
                toReturn[3].upgrade(5);
            } else {
                toReturn[3].upgrade(6);
            }
        }

        // Ring
        do {
            toReturn[4] = Generator.random(Generator.Category.RING);
        } while(toReturn[4].isSimilar(toReturn[3]));
        toReturn[4].level(0);
        itemLevelRoll = Random.Float();
        if (itemLevelRoll < 0.2f){
            toReturn[4].upgrade(4);
        } else if (itemLevelRoll < 0.7f){
            toReturn[4].upgrade(5);
        } else {
            toReturn[4].upgrade(6);
        }

        // Scrolls of Transmutation
        toReturn[5] = new ScrollOfTransmutation().quantity(5);

        for (Item item : toReturn) {
            item.identify(false);
        }

        return toReturn;
    }

    private MeleeWeapon upgradeRewardWeapon(MeleeWeapon input) {
        input.level(0);
        input.enchant(null);
        input.cursed = false;

        // 20%: +4, 50%: +5, 30%: +6
        float itemLevelRoll = Random.Float();
        int itemLevel;
        if (itemLevelRoll < 0.2f){
            itemLevel = 4;
        } else if (itemLevelRoll < 0.7f){
            itemLevel = 5;
        } else {
            itemLevel = 6;
        }
        input.upgrade(itemLevel);

        float itemEnchantRoll = Random.Float();
        if (itemEnchantRoll < 0.5f) {
            input.enchant(Weapon.Enchantment.randomUncommon());
        } else {
            input.enchant(Weapon.Enchantment.randomRare());
        }

        return input;
    }

    public void forceKill() {
        destroy();
        if (sprite != null) {
            ((HalfRipperSprite) sprite).killInstant();
        }
    }

    @Override
    protected boolean act() {
        if (Dungeon.hero.buff(AscensionChallenge.class) != null || Quest.failed){
            die(null);
            Notes.remove( landmark() );
            return true;
        }
        Quest.pos = pos;
        Quest.hp = HP;
        return super.act();
    }

    @Override
    public boolean interact(Char c) {

        sprite.turnTo( pos, c.pos );

        if (c != Dungeon.hero){
            return true;
        }

        if (!Quest.given) {
            String msg = "";
            switch (Dungeon.hero.heroClass){
                case WARRIOR:   msg += Messages.get(HalfRipper.this, "intro_quest_warrior"); break;
                case MAGE:      msg += Messages.get(HalfRipper.this, "intro_quest_mage"); break;
                case ROGUE:     msg += Messages.get(HalfRipper.this, "intro_quest_rogue"); break;
                case HUNTRESS:  msg += Messages.get(HalfRipper.this, "intro_quest_huntress"); break;
                case DUELIST:   msg += Messages.get(HalfRipper.this, "intro_quest_duelist"); break;
                case CLERIC:    msg += Messages.get(HalfRipper.this, "intro_quest_cleric"); break;
                case ARTIFICER: msg += Messages.get(HalfRipper.this, "intro_quest_artificer"); break;
            }
            msg += "\n\n" + Messages.get(HalfRipper.this, "intro_quest");

            final String msgFinal = msg;
            Game.runOnRenderThread(new Callback() {
                @Override
                public void call() {
                    GameScene.show(new WndQuest(HalfRipper.this, msgFinal) {
                        @Override
                        public void hide() {
                            super.hide();

                            HalfRipper.Quest.given = true;
                            HalfRipper.Quest.completed = false;
                            HalfRipper.Quest.started = false;
                            HalfRipper.Quest.failed = false;
                        }
                    } );
                }
            });
        } else if (!Quest.started) {
            Game.runOnRenderThread(new Callback() {
                @Override
                public void call() {
                    GameScene.show(new WndQuest(HalfRipper.this, Messages.get(HalfRipper.this, "quest_started")) {
                        @Override
                        public void hide() {
                            super.hide();
                            HalfRipper.Quest.started = true;
                            HalfRipper.this.state = new HalfRipper.Escaping();
                            HalfRipper.this.alignment = Alignment.ALLY;
                            Quest.depth = 24;
                        }
                    } );
                }
            });
        } else {
            return super.interact(c);
        }

        return true;
    }

    @Override
    public int damageRoll(boolean isMaxDamage) {
        if (isMaxDamage) return 15;
        return Random.NormalIntRange( 10, 15 );
    }

    @Override
    public int attackSkill( Char target ) {
        return 20;
    }

    @Override
    public float attackDelay() {
        return super.attackDelay()*0.5f;
    }

    @Override
    public int drRoll() {
        return super.drRoll() + Random.NormalIntRange(0, 4);
    }

    @Override
    public void damage( int dmg, Object src, int damageType ) {
        super.damage(dmg, src, damageType);
        if (Quest.corrupted()) {
            yell(Messages.get(this, "take_damage_corrupted"));
        } else {
            yell(Messages.get(this, "take_damage"));
        }
    }

    @Override
    public boolean add( Buff buff ) {
        return false;
    }

    @Override
    public boolean reset() {
        return true;
    }

    public static class Quest {

        //quest state information
        private static boolean spawned;
        private static boolean given;
        private static boolean started;
        private static boolean failed;
        private static boolean completed;
        private static int depth;
        private static int hp;
        private static boolean corrupted;
        private static int pos;
        private static boolean spawnedAbandonedRipper;
        private static boolean abandoned;

        private static final String NODE	= "halfripper";

        private static final String SPAWNED		= "spawned";
        private static final String GIVEN		= "given";
        private static final String STARTED		= "started";
        private static final String FAILED  	= "failed";
        private static final String ABANDONED   = "abandoned";
        private static final String COMPLETED	= "completed";
        private static final String DEPTH       = "depth";
        private static final String HP          = "hp";
        private static final String CORRUPTED   = "corrupted";
        private static final String POS         = "pos";
        private static final String SPAWNED_ABANDONED_RIPPER = "spawned abandoned ripper";

        public static void storeInBundle( Bundle bundle ) {

            Bundle node = new Bundle();

            node.put( SPAWNED, spawned );

            if (spawned) {
                node.put( GIVEN, given );
                node.put( STARTED, started );
                node.put( FAILED, failed );
                node.put( COMPLETED, completed );
                node.put( DEPTH, depth );
                node.put( HP, hp );
                node.put( CORRUPTED, corrupted );
                node.put( POS, pos );
                node.put( SPAWNED_ABANDONED_RIPPER, spawnedAbandonedRipper);
                node.put( ABANDONED, abandoned);
            }

            bundle.put( NODE, node );
        }

        public static void restoreFromBundle( Bundle bundle ) {

            Bundle node = bundle.getBundle( NODE );

            if (!node.isNull() && (spawned = node.getBoolean( SPAWNED ))) {
                given = node.getBoolean( GIVEN );
                started = node.getBoolean( STARTED );
                failed = node.getBoolean( FAILED );
                completed = node.getBoolean( COMPLETED );
                depth = node.getInt( DEPTH );
                hp = node.getInt( HP );
                corrupted = node.getBoolean( CORRUPTED );
                pos = node.getInt(POS);
                spawnedAbandonedRipper = node.getBoolean(SPAWNED_ABANDONED_RIPPER);
                abandoned = node.getBoolean(ABANDONED);
            }
        }

        public static void reset() {
            given = false;
            spawned = false;
            failed = false;
            completed = false;
            corrupted = false;
            depth = -1;
            hp = -1;
            pos = -1;
            spawnedAbandonedRipper = false;
            abandoned = false;
        }

        public static void spawn(HallsLevel level, Room room ) {
            if (!Quest.spawned && Dungeon.depth == 24) {

                HalfRipper hr = new HalfRipper();
                do {
                    hr.pos = level.pointToCell(room.random());
                } while (hr.pos == -1 || level.solid[hr.pos] || !level.passable[hr.pos] || !level.openSpace[hr.pos] || hr.pos == level.exit());
                level.mobs.add( hr );

                spawned = true;
                given = false;
                started = false;
                completed = false;
                failed = false;
                corrupted = (Random.Int(6) == 0);

            } else if (Quest.spawned && Dungeon.depth == Quest.depth && !failed && !completed) {
                boolean alreadySpawned = false;
                if (level.mobs != null) {
                    for (Mob m : level.mobs) {
                        if (m instanceof HalfRipper) {
                            alreadySpawned = true;
                            break;
                        }
                    }
                }
                if (!alreadySpawned) {
                    HalfRipper hr = new HalfRipper();
                    hr.pos = level.exit(); // when the player goes up, they will bump us out of the way
                    hr.HP = Quest.hp;
                    level.mobs.add( hr );
                }
            } else if (Quest.abandoned && !Quest.spawnedAbandonedRipper && Dungeon.depth == Quest.depth) {
                Quest.spawnedAbandonedRipper = true;
                RipperDemon theOneYouAbandonedYouMonster = new RipperDemon();
                theOneYouAbandonedYouMonster.HP = hp;
                theOneYouAbandonedYouMonster.pos = Quest.pos;
                theOneYouAbandonedYouMonster.state = theOneYouAbandonedYouMonster.HUNTING;
                level.mobs.add(theOneYouAbandonedYouMonster);
            }
        }

        public static String GetAbandonTitleKey() {
            if (corrupted && depth != 24) {
                return "abandon_title_corrupted";
            }
            return "abandon_title";
        }

        public static String GetAbandonDescriptionKey(boolean isGoingUp) {
            if (isGoingUp) {
                if (corrupted && depth != 24) {
                    return "abandon_up_corrupted_desc";
                }
                return "abandon_up_desc";
            }
            if (corrupted && depth != 24) {
                return "abandon_down_corrupted_desc";
            }
            return "abandon_down_desc";
        }

        public static void checkNeedToKill() {
            if (Quest.started() && (Quest.failed() || Quest.completed())) {
                if (Dungeon.level.mobs != null) {
                    for (Mob m : Dungeon.level.mobs) {
                        if (m instanceof HalfRipper) {
                            HalfRipper hr = (HalfRipper) m;
                            hr.forceKill();
                            break;
                        }
                    }
                }
            }
        }

        public static void abandon() {
            GLog.newLine();
            if (Quest.corrupted) {
                GLog.n("%s", Messages.get(HalfRipper.class, "abandon_corrupted_reaction"));
            }
            else {
                GLog.n("%s: \"%s\" ", Messages.titleCase(Messages.get(HalfRipper.class, "name")), Messages.get(HalfRipper.class, "abandon_reaction"));
            }
            Quest.failed = true;
            Notes.remove( Notes.Landmark.HALF_RIPPER );
            Quest.abandoned = true;
        }

        public static boolean given() { return given; }
        public static boolean started() { return started; }
        public static void start() { started = true; }
        public static boolean completed() { return given && completed; }
        public static boolean failed() { return failed; }
        public static void complete() {
            completed = true;
            depth = -1;
            pos = -1;
        }
        public static int depth() { return depth; }

        public static boolean corrupted() { return corrupted; }
    }
}
