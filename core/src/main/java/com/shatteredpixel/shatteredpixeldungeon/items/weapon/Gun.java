package com.shatteredpixel.shatteredpixeldungeon.items.weapon;

import static com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent.ARCSHIELDING;
import static com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent.EFFECTIVE_SHOT;
import static com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent.VOLATILE_CHAIN;
import static com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent.WEAPON_MOD_AUTOLOAD;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Barrier;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Cripple;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.EffectiveShotCooldown;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FlavourBuff;
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
import com.shatteredpixel.shatteredpixeldungeon.utils.BundleableProperty;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.PathFinder;

import java.util.ArrayList;
import java.util.EnumSet;

public class Gun extends Weapon {

    public static final String AC_SHOOT		    = "SHOOT";
    public static final String AC_RELOAD        = "RELOAD";
    public static final float TIME_TO_RELOAD	= 6f;

    private BundleableProperty.Bool m_IsLoaded = new BundleableProperty.Bool("is_loaded", true);

    {
        image = ItemSpriteSheet.GUN;

        defaultAction = AC_SHOOT;

        unique = true;
        bones = false;

        damageType = DamageType.of(DamageType.PIERCING);
    }

    @Override
    public boolean GetUsesTargetting() { return m_IsLoaded.Get(); }

    @Override
    public ArrayList<String> actions(Hero hero) {
        ArrayList<String> actions = super.actions(hero);
        actions.remove(AC_EQUIP);
        if (m_IsLoaded.Get()) {
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

        if (action.equals(AC_SHOOT) && !m_IsLoaded.Get()) {
            action = AC_RELOAD;
        } else
        if (action.equals(AC_RELOAD) && m_IsLoaded.Get()) {
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
        m_IsLoaded.Set(value);
        updateQuickslot();

        if (Dungeon.hero.buff(AutoReloadTracker.class) != null) {
            Dungeon.hero.buff(AutoReloadTracker.class).detach();
        }
        if (!value) {
            Buff.affect(Dungeon.hero, AutoReloadTracker.class, AutoReloadTracker.GetDuration());
        }
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
        int dmg = 6 + (6*Dungeon.hero.lvl/5)
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

    @Override
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);
        m_IsLoaded.Store(bundle);
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        m_IsLoaded.Restore(bundle);
    }

    // Main fire implementation with explosion radius support
// explosionRadius: 1 = 3x3, 2 = 5x5, 3 = 7x7, etc.
    public ArrayList<Mob> fire(final Hero user, final int cell, final boolean playSFX, final boolean spendTime, int explosionRadius) {
        ArrayList<Mob> mobs = new ArrayList<>();
        ArrayList<Char> alreadyDamagedMobs = new ArrayList<>();
        ArrayList<Integer> positionsToDamage = new ArrayList<>();
        final Ballistica beam = new Ballistica(curUser.pos, cell, Ballistica.STOP_SOLID);
        int lastHitEnemyID = -1;

        // ARCSHIELDING talent handling
        if (user.hasTalent(ARCSHIELDING)) {
            float threshold = 0.25f;
            if (user.pointsInTalent(ARCSHIELDING) == 2) threshold = 0.4f;
            if (user.HP / (float) user.GetMaxHP() <= threshold) {
                int shielding = 3;
                if (user.pointsInTalent(ARCSHIELDING) == 2) shielding = 5;
                Buff.affect(Dungeon.hero, Barrier.class).setShield(shielding);
            }
        }

        int maxDistance = Math.min(level() * 2 + 12, beam.dist);

        // Find all characters in the beam path
        ArrayList<Char> chars = new ArrayList<>();
        for (int c : beam.subPath(1, maxDistance)) {
            Char ch;
            if ((ch = Actor.findChar(c)) != null) {
                if (ch instanceof Mob && ((Mob) ch).state == ((Mob) ch).PASSIVE
                        && !(Dungeon.level.mapped[c] || Dungeon.level.visited[c])) {
                    // avoid harming undiscovered passive chars
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

        // Damage directly hit characters and their adjacent enemies
        for (Char ch : chars) {
            if (ch instanceof Mob) {
                mobs.add((Mob) ch);
            }
            ch.Damage(damageRoll(false, true), this, DamageType.of(DamageType.EXPLOSIVE));
            ch.sprite.flash();

            positionsToDamage.add(ch.pos);
            damageAdjacentEnemies(ch.pos, chars, alreadyDamagedMobs, positionsToDamage, explosionRadius);
        }

        // Damage enemies adjacent to collision point
        positionsToDamage.add(beam.collisionPos);
        damageAdjacentEnemies(beam.collisionPos, chars, alreadyDamagedMobs, positionsToDamage, explosionRadius);

        // Handle timing
        if (spendTime) {
            user.sprite.operate(user.pos);
            user.spendAndNext(1.0f);
        }

        // Play sound effects
        if (playSFX) {
            Sample.INSTANCE.play(Assets.Sounds.BLAST);
        }

        // Burn terrain
        burnTerrain(positionsToDamage);

        // EFFECTIVE_SHOT talent handling
        if (user.hasTalent(EFFECTIVE_SHOT)) {
            if (user.buff(EffectiveShotCooldown.class) == null) {
                Buff.affect(user, EffectiveShotCooldown.class).set(7 - (user).pointsInTalent(EFFECTIVE_SHOT));
            } else {
                EffectiveShotCooldown cd = user.buff(EffectiveShotCooldown.class);
                if (cd.left == 1) {
                    cd.detach();
                } else {
                    cd.left--;
                }
            }
        }

        // Visual beam effect
        Invisibility.dispel();
        curUser.sprite.parent.add(new Beam.GunRay(curUser.sprite.center(), DungeonTilemap.raisedTileCenterToWorld(beam.collisionPos)));

        // VOLATILE_CHAIN talent handling
        if (Dungeon.hero.hasTalent(VOLATILE_CHAIN) && lastHitEnemyID != -1) {
            Buff.prolong(Dungeon.hero, Talent.VolatileChainTracker.class, 5f).object = lastHitEnemyID;
        }

        Buff.affect(Dungeon.hero, AutoReloadTracker.class, AutoReloadTracker.GetDuration());

        Dungeon.observe();
        return mobs;
    }

    // Helper: Get all cells for explosion based on radius
    private ArrayList<Integer> getExplosionCells(int centerPos, int radius) {
        ArrayList<Integer> cells = new ArrayList<>();
        int width = Dungeon.level.width();

        for (int y = -radius; y <= radius; y++) {
            for (int x = -radius; x <= radius; x++) {
                cells.add(centerPos + x + y * width);
            }
        }

        return cells;
    }

    // Helper: Damage enemies adjacent to a position
    private void damageAdjacentEnemies(int centerPos, ArrayList<Char> directlyHitChars,
                                       ArrayList<Char> alreadyDamagedMobs,
                                       ArrayList<Integer> positionsToDamage,
                                       int explosionRadius) {
        ArrayList<Integer> explosionCells = getExplosionCells(centerPos, explosionRadius);

        for (int position : explosionCells) {
            Mob adjacent = Dungeon.level.findMob(position);

            if (adjacent != null && !directlyHitChars.contains(adjacent) && !alreadyDamagedMobs.contains(adjacent)) {
                alreadyDamagedMobs.add(adjacent);

                // Skip undiscovered passive chars
                if (adjacent.state == adjacent.PASSIVE &&
                        !(Dungeon.level.mapped[position] || Dungeon.level.visited[position])) {
                    continue;
                }

                // Damage non-ally enemies
                if (adjacent.alignment != Char.Alignment.ALLY) {
                    adjacent.Damage(damageRoll(false, true) / 2, this, EnumSet.of(DamageType.EXPLOSIVE));
                }
            }

            positionsToDamage.add(position);
        }
    }

    // Helper: Burn terrain at damaged positions
    private void burnTerrain(ArrayList<Integer> positionsToDamage) {
        for (Integer p : positionsToDamage) {
            if (p < 0 || p >= Dungeon.level.map.length) {
                continue;
            }

            int terrain = Dungeon.level.map[p];

            // Check if terrain can be burned/destroyed
            if (terrain == Terrain.EMPTY ||
                    terrain == Terrain.EMPTY_DECO ||
                    terrain == Terrain.OPEN_DOOR ||
                    terrain == Terrain.GRASS ||
                    terrain == Terrain.FURROWED_GRASS ||
                    Dungeon.level.flamable[p]) {

                Level.set(p, Terrain.EMBERS);
                GameScene.updateMap(p);
            }

            // Show smoke particle effects
            if (Dungeon.level.heroFOV[p]) {
                CellEmitter.get(p).burst(SmokeParticle.FACTORY, 8);
            }
        }
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
                fire(curUser, target, true, true, 1);
                m_IsLoaded.Set(false);
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
        if (m_IsLoaded.Get()) return "1/1";
        return "0/1";
    }

    public static class AutoReloadTracker extends FlavourBuff {

        public static int GetDuration() {
            return 100 - Dungeon.hero.pointsInTalent(WEAPON_MOD_AUTOLOAD) * 20;
        }

        @Override
        public boolean act() {
            Gun gun = Dungeon.hero.belongings.getItem(Gun.class);
            gun.SetIsLoaded(true);
            GLog.p( Messages.get(this, "auto_reload") );
            return super.act();
        }

        public void HandleTalentPurchase() {
            Gun gun = Dungeon.hero.belongings.getItem(Gun.class);
            if (gun != null) {
                spendConstant(-20);
            }
        }
    }
}
