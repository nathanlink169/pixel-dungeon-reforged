package com.shatteredpixel.shatteredpixeldungeon.items.weapon;

import static com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent.ARCSHIELDING;
import static com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent.EFFECTIVE_SHOT;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Challenges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Barrier;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.EffectiveShotCooldown;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.RevealedArea;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.huntress.NaturesPower;
import com.shatteredpixel.shatteredpixeldungeon.effects.SpellSprite;
import com.shatteredpixel.shatteredpixeldungeon.effects.Splash;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.LeafParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfSharpshooting;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfRecharging;
import com.shatteredpixel.shatteredpixeldungeon.items.spells.ReclaimTrap;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfRegrowth;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.MissileWeapon;
import com.shatteredpixel.shatteredpixeldungeon.journal.Bestiary;
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
import com.shatteredpixel.shatteredpixeldungeon.ui.QuickSlotButton;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.noosa.particles.Emitter;
import com.watabou.utils.Callback;
import com.watabou.utils.Random;
import com.watabou.utils.Reflection;

import java.util.ArrayList;

public class Gun extends Weapon {

    public static final String AC_SHOOT		    = "SHOOT";
    public static final String AC_RELOAD        = "RELOAD";
    public static final float TIME_TO_RELOAD	= 2f;

    private boolean isLoaded = true;

    {
        image = ItemSpriteSheet.ARTIFACT_GUN;

        defaultAction = AC_SHOOT;

        unique = true;
        bones = false;
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
            hero.sprite.operate( hero.pos );
            hero.spendAndNext( TIME_TO_RELOAD );
        }
    }

    public void SetIsLoaded(boolean value) {
        isLoaded = value;
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
        int dmg = 1 + (int)(Dungeon.hero.lvl*0.6f) // divided by 5, multiplied by 3
                + (curseInfusionBonus ? 1 + Dungeon.hero.lvl/30 : 0);
        return Math.max(0, dmg);
    }

    @Override
    public int max(int lvl) {
        int dmg = 6 + (int)(Dungeon.hero.lvl*1.2f) // divided by 2.5, multiplied by 3
                + (curseInfusionBonus ? 2 + Dungeon.hero.lvl/15 : 0);
        return Math.max(0, dmg);
    }

    @Override
    public int targetingPos(Hero user, int dst) {
        return knockArrow().targetingPos(user, dst);
    }

    private int targetPos;

    @Override
    public int damageRoll(Char owner, boolean isMaxDamage) {
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

    public Bullet knockArrow(){
        return new Bullet();
    }

    public class Bullet extends MissileWeapon {

        {
            image = ItemSpriteSheet.SPIRIT_ARROW;

            hitSound = Assets.Sounds.HIT_ARROW;
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
            return Gun.this.damageRoll(owner, isMaxDamage);
        }

        @Override
        public boolean hasEnchant(Class<? extends Enchantment> type, Char owner) {
            return Gun.this.hasEnchant(type, owner);
        }

        @Override
        public int proc(Char attacker, Char defender, int damage) {
            return Gun.this.proc(attacker, defender, damage);
        }

        @Override
        public float delayFactor(Char user) {
            return Gun.this.delayFactor(user);
        }

        @Override
        public int STRReq(int lvl) {
            return Gun.this.STRReq();
        }

        @Override
        protected void onThrow( int cell ) {
            Char enemy = Actor.findChar( cell );
            if (enemy == null || enemy == curUser) {
                Plant p = Dungeon.level.plants.get(cell);
                if (curUser.pointsInTalent(Talent.ADAPTIVE_MINEFIELD) == 2 && p != null) {
                    p.wither();
                }

                Trap t = Dungeon.level.traps.get(cell);
                if (curUser.hasTalent(Talent.ADAPTIVE_MINEFIELD) && t != null && t.active) {
                    if (!t.visible) {
                        t.reveal();
                    }
                    t.disarm(); //even disarms traps that normally wouldn't be

                    if (curUser.pointsInTalent(Talent.ADAPTIVE_MINEFIELD) == 2 && !Dungeon.isChallenged(Challenges.NO_HERBALISM)) {
                        // Plant Dewcatcher
                        if (t instanceof GeyserTrap) {
                            Dungeon.level.traps.remove(cell);
                            GameScene.updateMap(cell);
                            Dungeon.level.plant( new WandOfRegrowth.Dewcatcher.Seed(), cell );
                        }
                        // Plant Blindweed
                        if (t instanceof FlashingTrap) {
                            Dungeon.level.traps.remove(cell);
                            GameScene.updateMap(cell);
                            Dungeon.level.plant( new Blindweed.Seed(), cell);
                        }
                        // Plant Fadelead
                        if (t instanceof DisarmingTrap ||
                            t instanceof DistortionTrap ||
                            t instanceof GatewayTrap ||
                            t instanceof SummoningTrap ||
                            t instanceof TeleportationTrap) { // and warping trap, but teleportation trap covers that
                            Dungeon.level.traps.remove(cell);
                            GameScene.updateMap(cell);
                            Dungeon.level.plant( new Fadeleaf.Seed(), cell);
                        }
                        // Plant Firebloom
                        if (t instanceof BlazingTrap ||
                            t instanceof BurningTrap) {
                            Dungeon.level.traps.remove(cell);
                            GameScene.updateMap(cell);
                            Dungeon.level.plant( new Firebloom.Seed(), cell);
                        }
                        // Plant Icecap
                        if (t instanceof ChillingTrap ||
                            t instanceof FrostTrap) {
                            Dungeon.level.traps.remove(cell);
                            GameScene.updateMap(cell);
                            Dungeon.level.plant( new Icecap.Seed(), cell);
                        }
                        // Plant Sorrowmoss
                        if (t instanceof CorrosionTrap ||
                            t instanceof OozeTrap ||
                            t instanceof PoisonDartTrap || // and TenguDartTrap
                            t instanceof ToxicGasRoom.ToxicVent ||
                            t instanceof ToxicTrap) {
                            Dungeon.level.traps.remove(cell);
                            GameScene.updateMap(cell);
                            Dungeon.level.plant (new Sorrowmoss.Seed(), cell);
                        }
                        // Plant Stormvine
                        if (t instanceof ConfusionTrap ||
                            t instanceof ShockingTrap ||
                            t instanceof StormTrap) {
                            Dungeon.level.traps.remove(cell);
                            GameScene.updateMap(cell);
                            Dungeon.level.plant( new Stormvine.Seed(), cell);
                        }
                    }

                    Sample.INSTANCE.play(Assets.Sounds.LIGHTNING);
                    Bestiary.setSeen(t.getClass());

                } else {
                    parent = null;
                    Splash.at( cell, 0xCC99FFFF, 1 );
                }
            } else {
                if (!curUser.shoot( enemy, this )) {
                    Splash.at(cell, 0xCC99FFFF, 1);
                }
            }
        }

        @Override
        public void throwSound() {
            Sample.INSTANCE.play( Assets.Sounds.ATK_SPIRITBOW, 1, Random.Float(0.87f, 1.15f) );
        }

        @Override
        public void cast(final Hero user, final int dst) {
            Gun.this.targetPos = throwPos( user, dst );
            super.cast(user, dst);

            if (user.hasTalent(ARCSHIELDING)) {
                float threshold = 0.25f;
                if (user.pointsInTalent(ARCSHIELDING) == 2) threshold = 0.4f;
                if (user.HP/(float)user.HT <= threshold) {
                    int shielding = 3;
                    if (user.pointsInTalent(ARCSHIELDING) == 2) shielding = 5;
                    Buff.affect(Dungeon.hero, Barrier.class).setShield(shielding);
                }
            }
        }
    }

    private CellSelector.Listener shooter = new CellSelector.Listener() {
        @Override
        public void onSelect( Integer target ) {
            if (target != null) {
                knockArrow().cast(curUser, target);
                isLoaded = false;
            }
        }
        @Override
        public String prompt() {
            return Messages.get(SpiritBow.class, "prompt");
        }
    };
}
