package com.shatteredpixel.shatteredpixeldungeon.levels.traps;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Badges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Blob;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Fire;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Paralysis;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.GnollGeomancer;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.GnollGuard;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Kobold;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.Pushing;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.FlameParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfBlastWave;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.MiningLevel;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.BArray;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class KoboldFireTrap extends Trap {

    @Override
    public void activate() {
        GameScene.add(Blob.seed(pos, 15, Fire.class));
        CellEmitter.get(pos).burst(FlameParticle.FACTORY, 15);

        PathFinder.buildDistanceMap( pos, BArray.not( Dungeon.level.solid, null ), 1 );
        for (int i = 0; i < PathFinder.distance.length; i++) {
            if (i == pos) {
                continue;
            }
            if (PathFinder.distance[i] < Integer.MAX_VALUE) {
                Char ch = Actor.findChar(i);
                if (ch != null) { // need to check this as the hero could have triggered it from afar
                    ch.damage(Random.NormalIntRange(6, 12), this);

                    if (ch.isAlive()) {
                        Buff.prolong( ch, Paralysis.class, ch instanceof Hero ? 3 : 10 );
                    } else if (!ch.isAlive() && ch == Dungeon.hero) {
                        Dungeon.fail(this);
                        GLog.n(Messages.get(this, "ondeath"));
                    }

                    //trace a ballistica to our target (which will also extend past them
                    Ballistica trajectory = new Ballistica(pos, ch.pos, Ballistica.STOP_TARGET);
                    //trim it to just be the part that goes past them
                    trajectory = new Ballistica(trajectory.collisionPos, trajectory.path.get(trajectory.path.size()-1), Ballistica.PROJECTILE);
                    WandOfBlastWave.throwChar(ch, trajectory, 1, false, false, this);
                }

                Level.set(i, Terrain.WALL);
                CellEmitter.get( i - Dungeon.level.width() ).start(Speck.factory(Speck.ROCK), 0.07f, 10);
            }
        }
        Sample.INSTANCE.play(Assets.Sounds.ROCKS);
        Sample.INSTANCE.play(Assets.Sounds.BURNING);

        for (int i : PathFinder.NEIGHBOURS9) {
            Dungeon.level.discoverable[pos + i] = true;
        }
        for (int i : PathFinder.NEIGHBOURS9) {
            GameScene.updateMap( pos + i );
        }

        int twoTilesUp = pos - Dungeon.level.width() * 2;
        int twoTilesDown = pos + Dungeon.level.width() * 2;
        int twoTilesLeft = pos - 2;
        int twoTilesRight = pos + 2;
        SpawnKoboldIfPossible(twoTilesUp);
        SpawnKoboldIfPossible(twoTilesDown);
        SpawnKoboldIfPossible(twoTilesLeft);
        SpawnKoboldIfPossible(twoTilesRight);
    }

    private void SpawnKoboldIfPossible(int tile) {
        int tileType = Dungeon.level.map[tile];
        if ((Terrain.flags[tileType] & Terrain.PASSABLE) != 0) {
            if (Actor.findChar(tile) == null) {
                Kobold summon = new Kobold();
                summon.state = summon.HUNTING;
                summon.pos = tile;
                GameScene.add( summon );
                Dungeon.level.occupyCell(summon);
            }
        }
    }

}
