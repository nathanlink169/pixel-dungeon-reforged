package com.shatteredpixel.shatteredpixeldungeon.items.weapon;

import static com.shatteredpixel.shatteredpixeldungeon.actors.Char.hit;
import static com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent.ARCSHIELDING;
import static com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent.EFFECTIVE_SHOT;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Challenges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Blob;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Web;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.AscensionChallenge;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Barrier;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.ChampionEnemy;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Cursed;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.EffectiveShotCooldown;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.RevealedArea;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Vulnerable;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Weakness;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.huntress.NaturesPower;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.effects.Beam;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.SpellSprite;
import com.shatteredpixel.shatteredpixeldungeon.effects.Splash;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.BlastParticle;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.LeafParticle;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.PurpleParticle;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.SmokeParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.Heap;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.glyphs.Viscosity;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfSharpshooting;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfRecharging;
import com.shatteredpixel.shatteredpixeldungeon.items.spells.ReclaimTrap;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfRegrowth;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.MissileWeapon;
import com.shatteredpixel.shatteredpixeldungeon.journal.Bestiary;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.special.ToxicGasRoom;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.BlazingTrap;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.BurningTrap;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.ChillingTrap;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.ConfusionTrap;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.CorrosionTrap;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.DisarmingTrap;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.DistortionTrap;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.FlashingTrap;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.FrostTrap;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.GatewayTrap;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.GeyserTrap;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.OozeTrap;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.PoisonDartTrap;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.ShockingTrap;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.StormTrap;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.SummoningTrap;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.TeleportationTrap;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.TenguDartTrap;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.ToxicTrap;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.Trap;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.WarpingTrap;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.plants.Blindweed;
import com.shatteredpixel.shatteredpixeldungeon.plants.Fadeleaf;
import com.shatteredpixel.shatteredpixeldungeon.plants.Firebloom;
import com.shatteredpixel.shatteredpixeldungeon.plants.Icecap;
import com.shatteredpixel.shatteredpixeldungeon.plants.Plant;
import com.shatteredpixel.shatteredpixeldungeon.plants.Sorrowmoss;
import com.shatteredpixel.shatteredpixeldungeon.plants.Stormvine;
import com.shatteredpixel.shatteredpixeldungeon.scenes.CellSelector;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.sprites.MissileSprite;
import com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonTilemap;
import com.shatteredpixel.shatteredpixeldungeon.ui.QuickSlotButton;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.noosa.particles.Emitter;
import com.watabou.utils.Callback;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;
import com.watabou.utils.Reflection;

import java.util.ArrayList;

public class Gun extends Weapon {

    public static final String AC_SHOOT		    = "SHOOT";
    public static final String AC_RELOAD        = "RELOAD";
    public static final float TIME_TO_RELOAD	= 4f;

    private boolean isLoaded = true;

    {
        image = ItemSpriteSheet.GUN;

        defaultAction = AC_SHOOT;

        unique = true;
        bones = false;

        damageType = DamageType.PIERCING;
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
        int dmg = 4 + (2*Dungeon.hero.lvl/5)
                + (RingOfSharpshooting.levelDamageBonus(Dungeon.hero) / 2)
                + (curseInfusionBonus ? 1 + Dungeon.hero.lvl/30 : 0);
        return Math.max(0, dmg);
    }

    @Override
    public int max(int lvl) {
        int dmg = 8 + (int)((Dungeon.hero.lvl/5.0f)*5)
                + RingOfSharpshooting.levelDamageBonus(Dungeon.hero)
                + (curseInfusionBonus ? 2 + Dungeon.hero.lvl/15 : 0);
        return Math.max(0, dmg);
    }

    @Override
    public int damageRoll(Char owner, boolean isMaxDamage) {
        if (((Hero)owner).hasTalent(EFFECTIVE_SHOT)) {
            if (owner.buff(EffectiveShotCooldown.class) == null) {
                isMaxDamage = true;
                Buff.affect(owner, EffectiveShotCooldown.class).set(7 - ((Hero) owner).pointsInTalent(EFFECTIVE_SHOT));
            }
            else {
                EffectiveShotCooldown cd = owner.buff(EffectiveShotCooldown.class);
                if (cd.left == 1) {
                    cd.detach();
                }
                else {
                    cd.left--;
                }
            }
        }
        int damage = augment.damageFactor(super.damageRoll(owner, isMaxDamage));
        if (owner.buff(Talent.ArtificerFoodDamageBonus.class) != null) {
            damage += 3;
            owner.buff(Talent.ArtificerFoodDamageBonus.class).detach();
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
        final Ballistica beam = new Ballistica( curUser.pos, cell, Ballistica.WONT_STOP);
        curUser.sprite.parent.add(new Beam.GunRay(curUser.sprite.center(), DungeonTilemap.raisedTileCenterToWorld( cell )));

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
                    chars.add(ch);
                }
            }
        }

        for (Char ch : chars) {
            ch.damage( damageRoll(user, false), this );

            for (int o : PathFinder.NEIGHBOURS8) {
                int position = ch.pos + o;
                Char adjacent = Dungeon.level.findMob(position);
                if (adjacent != null && !chars.contains(adjacent) && !alreadyDamagedMobs.contains(adjacent)) {
                    alreadyDamagedMobs.add(adjacent);
                    adjacent.damage( damageRoll(user, false) / 2, this);
                }

                positionsToDamage.add(ch.pos + o);
            }

            positionsToDamage.add(ch.pos);
            ch.sprite.flash();
        }

        if (playSFX) {
            Sample.INSTANCE.play(Assets.Sounds.BLAST);
        }

        positionsToDamage.add(cell);
        for (int o : PathFinder.NEIGHBOURS8) {
            int position = cell + o;
            Char adjacent = Dungeon.level.findMob(position);
            if (adjacent != null && !chars.contains(adjacent) && !alreadyDamagedMobs.contains(adjacent)) {
                alreadyDamagedMobs.add(adjacent);
                adjacent.damage( damageRoll(user, false) / 2, this);
            }
            positionsToDamage.add(position);
        }

        if (spendTime) {
            user.sprite.operate(user.pos);
            user.spendAndNext(1.0f);
        }

        for (Integer p : positionsToDamage) {
            if (Dungeon.level.map[p] == Terrain.EMPTY ||
                    Dungeon.level.map[p] == Terrain.EMPTY_DECO ||
                    Dungeon.level.map[p] == Terrain.OPEN_DOOR ||
                    Dungeon.level.map[p] == Terrain.GRASS ||
                    Dungeon.level.map[p] == Terrain.FURROWED_GRASS ||
                    Dungeon.level.flamable[p]) {
                Dungeon.level.destroy(p);
                Level.set(p, Terrain.EMBERS);
                GameScene.updateMap( p );

                Heap heap = Dungeon.level.heaps.get(p);
                if (heap != null) {
                    heap.explode();
                }

                if (Dungeon.level.heroFOV[p]) {
                    CellEmitter.get(p).burst(SmokeParticle.FACTORY, 8);
                }
            }
        }

        Dungeon.observe();
        return mobs;
    }

    public class Bullet extends MissileWeapon {
        {
            damageType = DamageType.PIERCING;
        }
        @Override
        public int damageRoll(Char owner, boolean isMaxDamage) {
            return Gun.this.damageRoll(owner, isMaxDamage);
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
