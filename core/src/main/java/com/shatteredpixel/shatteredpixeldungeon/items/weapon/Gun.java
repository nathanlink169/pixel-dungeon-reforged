package com.shatteredpixel.shatteredpixeldungeon.items.weapon;

import static com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent.ARCSHIELDING;
import static com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent.EFFECTIVE_SHOT;
import static com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent.VOLATILE_CHAIN;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Barrier;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.EffectiveShotCooldown;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.combat.DamageType;
import com.shatteredpixel.shatteredpixeldungeon.effects.Beam;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.SmokeParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfSharpshooting;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.MissileWeapon;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.CellSelector;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonTilemap;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.PathFinder;

import java.util.ArrayList;
import java.util.EnumSet;

public class Gun extends Weapon {

    public static final String AC_SHOOT		    = "SHOOT";
    public static final String AC_RELOAD        = "RELOAD";
    public static final float TIME_TO_RELOAD	= 6f;

    private boolean isLoaded = true;

    {
        image = ItemSpriteSheet.GUN;

        defaultAction = AC_SHOOT;

        unique = true;
        bones = false;

        damageType = DamageType.of(DamageType.PIERCING);
    }

    @Override
    public boolean GetUsesTargetting() { return isLoaded; }

    @Override
    public ArrayList<String> actions(Hero hero) {
        ArrayList<String> actions = super.actions(hero);
        actions.remove(AC_EQUIP);
        if (isLoaded) {
            actions.add(AC_SHOOT);
        }
        else {
            actions.add(AC_RELOAD);
        }
        return actions;
    }

    @Override
    public void execute(Hero hero, String action) {

        super.execute(hero, action);

        if (action.equals(AC_SHOOT) && !isLoaded) {
            action = AC_RELOAD;
        } else
        if (action.equals(AC_RELOAD) && isLoaded) {
            action = AC_SHOOT;
        }

        if (action.equals(AC_SHOOT)) {
            curUser = hero;
            curItem = this;
            GameScene.selectCell( shooter );
        }
        if (action.equals(AC_RELOAD)) {
            SetIsLoaded(true);
            hero.sprite.operate( hero.pos );
            hero.spendAndNext( TIME_TO_RELOAD );
        }
    }

    public void SetIsLoaded(boolean value) {
        isLoaded = value;
        updateQuickslot();
    }

    @Override
    public String info() {
        String info = super.info();

        info += "\n\n" + Messages.get( Gun.class, "stats",
                Math.round(augment.damageFactor(min())),
                Math.round(augment.damageFactor(max())),
                STRReq());

        if (STRReq() > Dungeon.hero.STR()) {
            info += " " + Messages.get(Weapon.class, "too_heavy");
        }

        switch (augment) {
            case SPEED:
                info += "\n\n" + Messages.get(Weapon.class, "faster");
                break;
            case DAMAGE:
                info += "\n\n" + Messages.get(Weapon.class, "stronger");
                break;
            case NONE:
        }

        if (enchantment != null && (cursedKnown || !enchantment.curse())){
            info += "\n\n" + Messages.capitalize(Messages.get(Weapon.class, "enchanted", enchantment.name()));
            if (enchantHardened) info += " " + Messages.get(Weapon.class, "enchant_hardened");
            info += " " + enchantment.desc();
        } else if (enchantHardened){
            info += "\n\n" + Messages.get(Weapon.class, "hardened_no_enchant");
        }

        if (cursed && isEquipped( Dungeon.hero )) {
            info += "\n\n" + Messages.get(Weapon.class, "cursed_worn");
        } else if (cursedKnown && cursed) {
            info += "\n\n" + Messages.get(Weapon.class, "cursed");
        } else if (!isIdentified() && cursedKnown){
            info += "\n\n" + Messages.get(Weapon.class, "not_cursed");
        }

        info += "\n\n" + Messages.get(MissileWeapon.class, "distance");

        return info;
    }

    @Override
    public int STRReq(int lvl) {
        return 10; // gun always needs 10 strength
    }

    @Override
    public int min(int lvl) {
        int dmg = 4 + (4*Dungeon.hero.lvl/5)
                + (RingOfSharpshooting.levelDamageBonus(Dungeon.hero) / 2)
                + (curseInfusionBonus ? 1 + Dungeon.hero.lvl/30 : 0);
        return Math.max(0, dmg);
    }

    @Override
    public int max(int lvl) {
        int dmg = 8 + (8*Dungeon.hero.lvl/5)
                + RingOfSharpshooting.levelDamageBonus(Dungeon.hero)
                + (curseInfusionBonus ? 2 + Dungeon.hero.lvl/15 : 0);
        return Math.max(0, dmg);
    }

    @Override
    public int damageRoll(boolean isMaxDamage, boolean usedByHero) {
        if (Dungeon.hero.hasTalent(EFFECTIVE_SHOT)) {
            if (Dungeon.hero.buff(EffectiveShotCooldown.class) == null) {
                isMaxDamage = true;
            }
        }
        int damage = super.damageRoll(isMaxDamage, usedByHero);
        if (Dungeon.hero.buff(Talent.ArtificerFoodDamageBonus.class) != null) {
            damage += 3;
            Dungeon.hero.buff(Talent.ArtificerFoodDamageBonus.class).detach();
        }
        return damage;
    }

    @Override
    public int level() {
        int level = Dungeon.hero == null ? 0 : Dungeon.hero.lvl/5;
        if (curseInfusionBonus) level += 1 + level/6;
        return level;
    }

    @Override
    public int buffedLvl() {
        //level isn't affected by buffs/debuffs
        return level();
    }

    @Override
    public boolean isUpgradable() {
        return false;
    }

    // replace adaptive minefield
    // return all enemies directly hit by beam for powerful shot quickdraw
    public ArrayList<Mob> fire(final Hero user, final int cell, final boolean playSFX, final boolean spendTime) {
        ArrayList<Mob> mobs = new ArrayList<>();
        ArrayList<Char> alreadyDamagedMobs = new ArrayList<>();
        ArrayList<Integer> positionsToDamage = new ArrayList<Integer>();
        final Ballistica beam = new Ballistica( curUser.pos, cell, Ballistica.STOP_SOLID);
        int lastHitEnemyID = -1;

        if (user.hasTalent(ARCSHIELDING)) {
            float threshold = 0.25f;
            if (user.pointsInTalent(ARCSHIELDING) == 2) threshold = 0.4f;
            if (user.HP/(float)user.GetMaxHP() <= threshold) {
                int shielding = 3;
                if (user.pointsInTalent(ARCSHIELDING) == 2) shielding = 5;
                Buff.affect(Dungeon.hero, Barrier.class).setShield(shielding);
            }
        }

        int maxDistance = Math.min(level() * 2 + 12, beam.dist);

        ArrayList<Char> chars = new ArrayList<>();
        for (int c : beam.subPath(1, maxDistance)) {

            Char ch;
            if ((ch = Actor.findChar( c )) != null) {

                if (ch instanceof Mob && ((Mob) ch).state == ((Mob) ch).PASSIVE
                        && !(Dungeon.level.mapped[c] || Dungeon.level.visited[c])){
                    //avoid harming undiscovered passive chars
                } else {
                    if (!(ch instanceof Mob && ch.alignment == Char.Alignment.ALLY)) {
                        chars.add(ch);
                        lastHitEnemyID = ch.id();
                    }
                }
            }

            if (c == beam.collisionPos) {
                break;
            }
        }

        for (Char ch : chars) {
            ch.Damage( damageRoll(false, true), this, DamageType.of(DamageType.EXPLOSIVE) );

            for (int o : PathFinder.NEIGHBOURS8) {
                int position = ch.pos + o;
                Mob adjacent = Dungeon.level.findMob(position);
                if (adjacent != null && !chars.contains(adjacent) && !alreadyDamagedMobs.contains(adjacent)) {
                    alreadyDamagedMobs.add(adjacent);
                    if (adjacent.state == adjacent.PASSIVE && !(Dungeon.level.mapped[position] || Dungeon.level.visited[position])){
                        //avoid harming undiscovered passive chars
                    } else {
                        if (!(adjacent.alignment == Char.Alignment.ALLY)) {
                            adjacent.Damage( damageRoll(false, true) / 2, this, EnumSet.of(DamageType.EXPLOSIVE));
                        }
                    }
                }

                positionsToDamage.add(ch.pos + o);
            }

            positionsToDamage.add(ch.pos);
            ch.sprite.flash();
        }

        if (playSFX) {
            Sample.INSTANCE.play(Assets.Sounds.BLAST);
        }

        positionsToDamage.add(beam.collisionPos);
        for (int o : PathFinder.NEIGHBOURS8) {
            int position = beam.collisionPos + o;
            Mob adjacent = Dungeon.level.findMob(position);
            if (adjacent != null && !chars.contains(adjacent) && !alreadyDamagedMobs.contains(adjacent)) {
                alreadyDamagedMobs.add(adjacent);
                if (adjacent.state == adjacent.PASSIVE && !(Dungeon.level.mapped[position] || Dungeon.level.visited[position])){
                    //avoid harming undiscovered passive chars
                } else {
                    if (!(adjacent.alignment == Char.Alignment.ALLY)) {
                        adjacent.Damage( damageRoll(false, true) / 2, this, EnumSet.of(DamageType.EXPLOSIVE));
                    }
                }
            }
            positionsToDamage.add(position);
        }

        if (spendTime) {
            user.sprite.operate(user.pos);
            user.spendAndNext(1.0f);
        }

        for (Integer p : positionsToDamage) {

            if (p < 0 || p >= Dungeon.level.map.length) {
                continue;
            }

            if (Dungeon.level.map[p] == Terrain.EMPTY ||
                    Dungeon.level.map[p] == Terrain.EMPTY_DECO ||
                    Dungeon.level.map[p] == Terrain.OPEN_DOOR ||
                    Dungeon.level.map[p] == Terrain.GRASS ||
                    Dungeon.level.map[p] == Terrain.FURROWED_GRASS ||
                    Dungeon.level.flamable[p]) {
                Dungeon.level.destroy(p);
                if (Dungeon.level.map[p] == Terrain.EMPTY ||
                        Dungeon.level.map[p] == Terrain.EMPTY_DECO ||
                        Dungeon.level.map[p] == Terrain.OPEN_DOOR ||
                        Dungeon.level.map[p] == Terrain.GRASS ||
                        Dungeon.level.map[p] == Terrain.FURROWED_GRASS ||
                        Dungeon.level.flamable[p]) {
                    Level.set(p, Terrain.EMBERS);
                }
                GameScene.updateMap( p );
            }

            if (Dungeon.level.heroFOV[p]) {
                CellEmitter.get(p).burst(SmokeParticle.FACTORY, 8);
            }
        }

        if (user.hasTalent(EFFECTIVE_SHOT)) {
            if (user.buff(EffectiveShotCooldown.class) == null) {
                Buff.affect(user, EffectiveShotCooldown.class).set(7 - (user).pointsInTalent(EFFECTIVE_SHOT));
            }
            else {
                EffectiveShotCooldown cd = user.buff(EffectiveShotCooldown.class);
                if (cd.left == 1) {
                    cd.detach();
                }
                else {
                    cd.left--;
                }
            }
        }

        Invisibility.dispel();
        curUser.sprite.parent.add(new Beam.GunRay(curUser.sprite.center(), DungeonTilemap.raisedTileCenterToWorld( beam.collisionPos )));

        if (Dungeon.hero.hasTalent(VOLATILE_CHAIN) && lastHitEnemyID != -1) {
            Buff.prolong(Dungeon.hero, Talent.VolatileChainTracker.class, 5f).object = lastHitEnemyID;
        }

        Dungeon.observe();
        return mobs;
    }

    public class Bullet extends MissileWeapon {
        {
            damageType = DamageType.of(DamageType.PIERCING);
        }
        @Override
        public int damageRoll(boolean isMaxDamage, boolean usedByHero) {
            return Gun.this.damageRoll(isMaxDamage, usedByHero);
        }
    }

    private CellSelector.Listener shooter = new CellSelector.Listener() {
        @Override
        public void onSelect( Integer target ) {
            if (target != null) {
                fire(curUser, target, true, true);
                isLoaded = false;
                updateQuickslot();
            }
        }
        @Override
        public String prompt() {
            return Messages.get(SpiritBow.class, "prompt");
        }
    };

    @Override
    public String status() {
        if (isLoaded) return "1/1";
        return "0/1";
    }
}
