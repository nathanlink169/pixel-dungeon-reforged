/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2024 Evan Debenham
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

package com.shatteredpixel.shatteredpixeldungeon.actors.mobs;

import com.shatteredpixel.shatteredpixeldungeon.Badges;
import com.shatteredpixel.shatteredpixeldungeon.Challenges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.Statistics;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Dread;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.LockedFloor;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.Ratmogrify;
import com.shatteredpixel.shatteredpixeldungeon.effects.Pushing;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.DriedRose;
import com.shatteredpixel.shatteredpixeldungeon.items.keys.CrystalKey;
import com.shatteredpixel.shatteredpixeldungeon.items.keys.IronKey;
import com.shatteredpixel.shatteredpixeldungeon.items.keys.SkeletonKey;
import com.shatteredpixel.shatteredpixeldungeon.items.quest.GooBlob;
import com.shatteredpixel.shatteredpixeldungeon.items.quest.RatClaw;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfTeleportation;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.AcidicSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.GooSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.RatSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.RatUsurperSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.BossHealthBar;
import com.watabou.utils.Bundle;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class RatUsurper extends Mob {

    {
        HP = HT = Dungeon.isChallenged(Challenges.STRONGER_BOSSES) ? 120 : 80;
        EXP = 10;
        defenseSkill = 8;

        properties.add(Property.BOSS);
    }
    @Override
    public Class<? extends CharSprite> GetSpriteClass() {
        return RatUsurperSprite.class;
    }

    @Override
    public int damageRoll(boolean isMaxDamage) {
        if (isMaxDamage) return 4;
        return Random.NormalIntRange( 4, 12 );
    }

    @Override
    public void notice() {
        super.notice();
        if (!BossHealthBar.isAssigned()) {
            BossHealthBar.assignBoss(this);
            Dungeon.level.seal();
            yell(Messages.get(this, "notice"));
            for (Char ch : Actor.chars()){
                if (ch instanceof DriedRose.GhostHero){
                    ((DriedRose.GhostHero) ch).sayBoss();
                }
            }
            for (Mob m : Dungeon.level.mobs) {
                if (m instanceof Rat) {
                    m.beckon(Dungeon.hero.pos);
                }
            }
        }
    }

    @Override
    public int attackSkill( Char target ) {
        return 16;
    }

    @Override
    public int drRoll() {
        return super.drRoll() + Random.NormalIntRange(0, 2);
    }

    @Override
    public void damage(int dmg, Object src, int damageType) {
        if (!BossHealthBar.isAssigned()){
            BossHealthBar.assignBoss( this );
            Dungeon.level.seal();
        }
        int lastHP = HP;
        boolean bleeding = (HP*2 <= HT);
        super.damage(dmg, src, damageType);
        if (lastHP != HP && HP > 0) {
            // only spawn rat if actually takes damage
            if (bleeding) {
                int hpBracket = HT / 8;
                int oldBracket = lastHP / hpBracket;
                int curbracket = HP / hpBracket;

                if (curbracket != oldBracket) {
                    teleportRat();
                }
                Rat.spawnAround(pos);
                if (Dungeon.isChallenged(Challenges.STRONGER_BOSSES)) {
                    Rat.spawnAround(pos);
                }
            }
            Rat.spawnAround(pos);
            if (Dungeon.isChallenged(Challenges.STRONGER_BOSSES)) {
                Rat.spawnAround(pos);
            }

            for (Mob m : Dungeon.level.mobs) {
                if (m instanceof Rat) {
                    m.beckon(pos);
                }
            }
        }
        else if (!isAlive()) {
            for (Mob m : Dungeon.level.mobs) {
                if (m instanceof Rat) {
                    Dread d = Buff.affect( m, Dread.class );
                    d.object = Dungeon.hero.id();
                    d.permanent = true;
                }
            }
        }

        if ((HP*2 <= HT) && !bleeding){
            BossHealthBar.bleed(true);
            yell(Messages.get(this, "help"));
        }
        LockedFloor lock = Dungeon.hero.buff(LockedFloor.class);
        if (lock != null && !isImmune(src.getClass()) && !isInvulnerable(src.getClass())){
            if (Dungeon.isChallenged(Challenges.STRONGER_BOSSES))   lock.addTime(dmg);
            else                                                    lock.addTime(dmg*1.5f);
        }
    }

    private void teleportRat() {
        Rat toTeleport = null;
        ArrayList<Rat> rats = new ArrayList<Rat>();
        for (Mob m : Dungeon.level.mobs) {
            if (m instanceof Rat) {
                rats.add((Rat) m);
            }
        }

        if (!rats.isEmpty()) {
            toTeleport = rats.get(Random.Int(rats.size()));
        }

        if (toTeleport != null) {
            yell (Messages.get(this, "protect_me"));

            int oldPos = this.pos;
            boolean anyValidPositions = false;
            for (int neighbour : PathFinder.NEIGHBOURS8) {
                if (Actor.findChar(pos+neighbour) == null &&
                        Dungeon.level.passable[pos+neighbour]) {
                    anyValidPositions = true;
                    break;
                }
            }
            if (!anyValidPositions) {
                ScrollOfTeleportation.teleportChar(this);
                ScrollOfTeleportation.appear(toTeleport, oldPos);
            }
            else {
                float bestDist;
                int bestPos = pos;

                Ballistica trajectory = new Ballistica(enemy.pos, pos, Ballistica.STOP_TARGET);
                int targetCell = trajectory.path.get(trajectory.dist + 1);
                //if the position opposite the direction of the hero is open, go there
                if (Actor.findChar(targetCell) == null && !Dungeon.level.solid[targetCell]) {
                    bestPos = targetCell;

                    //Otherwise go to the neighbour cell that's open and is furthest
                } else {
                    bestDist = Dungeon.level.trueDistance(pos, enemy.pos);

                    for (int i : PathFinder.NEIGHBOURS8) {
                        if (Actor.findChar(pos + i) == null
                                && !Dungeon.level.solid[pos + i]
                                && Dungeon.level.trueDistance(pos + i, enemy.pos) > bestDist) {
                            bestPos = pos + i;
                            bestDist = Dungeon.level.trueDistance(pos + i, enemy.pos);
                        }
                    }
                }

                Actor.add(new Pushing(this, pos, bestPos));
                pos = bestPos;

                // TODO: Half of this isn't necessary but it works for now
                //find closest cell that's adjacent to enemy, place subject there
                bestDist = Dungeon.level.trueDistance(enemy.pos, pos);
                bestPos = enemy.pos;
                for (int i : PathFinder.NEIGHBOURS8) {
                    if (Actor.findChar(enemy.pos + i) == null
                            && !Dungeon.level.solid[enemy.pos + i]
                            && Dungeon.level.trueDistance(enemy.pos + i, pos) < bestDist) {
                        bestPos = enemy.pos + i;
                        bestDist = Dungeon.level.trueDistance(enemy.pos + i, pos);
                    }
                }

                if (oldPos != enemy.pos) ScrollOfTeleportation.appear(toTeleport, oldPos);
            }
        }
    }
    @Override
    public boolean act() {
        boolean toReturn = super.act();

        if (state != SLEEPING && !Dungeon.hero.fieldOfView[this.pos]) {
            Statistics.qualifiedForBossChallengeBadge = false;
        }

        return toReturn;
    }

    @Override
    public void die( Object cause ) {

        super.die( cause );

        Dungeon.level.unseal();

        GameScene.bossSlain();
        Dungeon.level.drop( new SkeletonKey( Dungeon.depth ), pos ).sprite.drop();
        Dungeon.level.drop( new CrystalKey(Dungeon.depth), pos).sprite.drop();

        //60% chance of 2 blobs, 30% chance of 3, 10% chance for 4. Average of 2.5
        int blobs = Random.chances(new float[]{0, 0, 6, 3, 1});
        for (int i = 0; i < blobs; i++){
            int ofs;
            do {
                ofs = PathFinder.NEIGHBOURS8[Random.Int(8)];
            } while (!Dungeon.level.passable[pos + ofs]);
            Dungeon.level.drop( new RatClaw(), pos + ofs ).sprite.drop( pos );
        }

        Badges.validateBossSlain(this);
        if (Statistics.qualifiedForBossChallengeBadge){
            Badges.validateBossChallengeCompleted();
        }
        Statistics.bossScores[0] += 1000;

        yell( Messages.get(this, "defeated") );
    }

    @Override
    public void storeInBundle( Bundle bundle ) {
        super.storeInBundle( bundle );
    }

    @Override
    public void restoreFromBundle( Bundle bundle ) {

        super.restoreFromBundle( bundle );
        if (state != SLEEPING) BossHealthBar.assignBoss(this);
        if ((HP*2 <= HT)) BossHealthBar.bleed(true);
    }
}
