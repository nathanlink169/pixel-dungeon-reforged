package com.shatteredpixel.shatteredpixeldungeon.items.artifacts;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Cripple;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MagicImmune;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Regeneration;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Belongings;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.Bag;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.Ring;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfEnergy;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MeleeWeapon;
import com.shatteredpixel.shatteredpixeldungeon.journal.Catalog;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.ui.ItemButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.windows.IconTitle;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndBag;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndInfoItem;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class Ringbox extends Artifact {
    {
        image = ItemSpriteSheet.ARTIFACT_RINGBOX;

        levelCap = 10;
    }

    public static final String AC_OPEN = "OPEN";

    private Ring slot1Ring = null;
    private Ring slot2Ring = null;
    private Ring slot3Ring = null;

    @Override
    public ArrayList<String> actions(Hero hero ) {
        ArrayList<String> actions = super.actions( hero );
        if (isIdentified() && !cursed) {
            actions.add(AC_OPEN);
        }
        return actions;
    }

    @Override
    public void execute(Hero hero, String action ) {
        super.execute(hero, action);

        if (action.equals(AC_OPEN)) {
            GameScene.show( new Ringbox.WndRingboxEquip(this) );
        }
    }

    private static final String RING1 = "ring1";
    private static final String RING2 = "ring2";
    private static final String RING3 = "ring3";
    @Override
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);

        if (slot1Ring != null) bundle.put( RING1, slot1Ring );
        if (slot2Ring != null) bundle.put( RING2, slot2Ring );
        if (slot3Ring != null) bundle.put( RING3, slot3Ring );
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);

        if (bundle.contains(RING1)) slot1Ring = (Ring)bundle.get( RING1 );
        if (bundle.contains(RING2)) slot2Ring = (Ring)bundle.get( RING2 );
        if (bundle.contains(RING3)) slot3Ring = (Ring)bundle.get( RING3 );
    }

    @Override
    public String desc() {
        String desc = super.desc();

        if (isEquipped (Dungeon.hero)){
            desc += "\n\n";
            if (cursed)
                desc += Messages.get(this, "desc_cursed");
            else
                desc += Messages.get(this, "desc");
        }

        return desc;
    }

    @Override
    protected ArtifactBuff passiveBuff() {
        return new Ringbox.ringboxLeveling();
    }

    // A ring wrapper that scales another ring's bonus values
    private static class ScaledRing extends Ring {
        private Ring sourceRing;
        private float scaleFactor;

        public ScaledRing(Ring source, float scale) {
            this.sourceRing = source;
            this.scaleFactor = scale;
            // Copy important properties from source
            this.buffClass = source.buffClass;
            this.level(source.level());
            this.cursed = source.cursed;
            this.cursedKnown = source.cursedKnown;
            this.levelKnown = source.levelKnown;
        }

        @Override
        public int soloBonus() {
            return Math.round(sourceRing.soloBonus() * scaleFactor);
        }

        @Override
        public int soloBuffedBonus() {
            return Math.round(sourceRing.soloBuffedBonus() * scaleFactor);
        }

        @Override
        public RingBuff buff() {
            // Should not be called, but return null safely
            return null;
        }
    }


    private static class WndRingboxEquip extends Window {

        private static final int BTN_SIZE	= 32;
        private static final float GAP		= 2;
        private static final float BTN_GAP	= 12;
        private static final int WIDTH		= 116;

        private ItemButton slot1;
        private ItemButton slot2;
        private ItemButton slot3;

        WndRingboxEquip(final Ringbox ringbox){

            IconTitle titlebar = new IconTitle();
            titlebar.icon( new ItemSprite(ringbox) );
            titlebar.label( Messages.get(Ringbox.class, "name") );
            titlebar.setRect( 0, 0, WIDTH, 0 );
            add( titlebar );

            float firstRingPower = 30;
            float secondRingPower = 30;
            float thirdRingPower = 0;

            if (ringbox.level() >= 1) firstRingPower += 12.5f;
            if (ringbox.level() >= 2) secondRingPower += 12.5f;
            if (ringbox.level() >= 3) thirdRingPower += 30.0f;
            if (ringbox.level() >= 4) firstRingPower += 12.5f;
            if (ringbox.level() >= 5) secondRingPower += 12.5f;
            if (ringbox.level() >= 6) thirdRingPower += 12.5f;
            if (ringbox.level() >= 7) firstRingPower += 12.5f;
            if (ringbox.level() >= 8) secondRingPower += 12.5f;
            if (ringbox.level() >= 9) thirdRingPower += 12.5f;
            if (ringbox.level() >= 10) thirdRingPower += 12.5f;

            String desc;
            if (thirdRingPower > 0) {
                desc = Messages.get(Ringbox.class, "equipwindowthree", firstRingPower, secondRingPower, thirdRingPower);
            }
            else {
                desc = Messages.get(Ringbox.class, "equipwindowtwo", firstRingPower, secondRingPower);
            }

            RenderedTextBlock message =
                    PixelScene.renderTextBlock(desc, 6);
            message.maxWidth( WIDTH );
            message.setPos(0, titlebar.bottom() + GAP);
            add( message );

            slot1 = new ItemButton(){
                @Override
                protected void onClick() {
                    if (ringbox.slot1Ring != null){
                        item(new WndBag.Placeholder(ItemSpriteSheet.RING_HOLDER));
                        if (!ringbox.slot1Ring.doPickUp(Dungeon.hero)){
                            Dungeon.level.drop( ringbox.slot1Ring, Dungeon.hero.pos);
                        }
                        ringbox.slot1Ring = null;
                        ringboxLeveling buff = Dungeon.hero.buff(ringboxLeveling.class);
                        if (buff != null) buff.updateProxyBuffs();
                    } else {
                        GameScene.selectItem(new WndBag.ItemSelector() {

                            @Override
                            public String textPrompt() {
                                return Messages.get(Ringbox.class, "selectring");
                            }

                            @Override
                            public Class<?extends Bag> preferredBag(){
                                return Belongings.Backpack.class;
                            }

                            @Override
                            public boolean itemSelectable(Item item) {
                                return item instanceof Ring;
                            }

                            @Override
                            public void onSelect(Item item) {
                                if (!(item instanceof Ring)) {
                                    //do nothing, should only happen when window is cancelled
                                } else if (!item.isIdentified()) {
                                    GLog.w( Messages.get(Ringbox.class, "mustidentify") );
                                    hide();
                                } else if (item.cursed) {
                                    GLog.w( Messages.get(Ringbox.class, "ringcursed") );
                                    hide();
                                } else {
                                    if (item.isEquipped(Dungeon.hero)){
                                        ((Ring) item).doUnequip(Dungeon.hero, false, false);
                                    } else {
                                        item.detach(Dungeon.hero.belongings.backpack);
                                    }
                                    ringbox.slot1Ring = (Ring) item;
                                    item(ringbox.slot1Ring);
                                    ringboxLeveling buff = Dungeon.hero.buff(ringboxLeveling.class);
                                    if (buff != null) buff.updateProxyBuffs();
                                }

                            }
                        });
                    }
                }

                @Override
                protected boolean onLongClick() {
                    if (item() != null && item().name() != null){
                        GameScene.show(new WndInfoItem(item()));
                        return true;
                    }
                    return false;
                }
            };
            // TODO: Fix spacing pls
            if (thirdRingPower > 0) {
                slot1.setRect((WIDTH - BTN_GAP) / 3 - BTN_SIZE, message.top() + message.height() + GAP, BTN_SIZE, BTN_SIZE);
            }
            else {
                slot1.setRect((WIDTH - BTN_GAP) / 2 - BTN_SIZE, message.top() + message.height() + GAP, BTN_SIZE, BTN_SIZE);
            }
            if (ringbox.slot1Ring != null) {
                slot1.item(ringbox.slot1Ring);
            } else {
                slot1.item(new WndBag.Placeholder(ItemSpriteSheet.RING_HOLDER));
            }
            add( slot1 );

            slot2 = new ItemButton(){
                @Override
                protected void onClick() {
                    if (ringbox.slot2Ring != null){
                        item(new WndBag.Placeholder(ItemSpriteSheet.RING_HOLDER));
                        if (!ringbox.slot2Ring.doPickUp(Dungeon.hero)){
                            Dungeon.level.drop( ringbox.slot2Ring, Dungeon.hero.pos);
                        }
                        ringbox.slot2Ring = null;
                        ringboxLeveling buff = Dungeon.hero.buff(ringboxLeveling.class);
                        if (buff != null) buff.updateProxyBuffs();
                    } else {
                        GameScene.selectItem(new WndBag.ItemSelector() {

                            @Override
                            public String textPrompt() {
                                return Messages.get(Ringbox.class, "selectring");
                            }

                            @Override
                            public Class<?extends Bag> preferredBag(){
                                return Belongings.Backpack.class;
                            }

                            @Override
                            public boolean itemSelectable(Item item) {
                                return item instanceof Ring;
                            }

                            @Override
                            public void onSelect(Item item) {
                                if (!(item instanceof Ring)) {
                                    //do nothing, should only happen when window is cancelled
                                } else if (!item.isIdentified()) {
                                    GLog.w( Messages.get(Ringbox.class, "mustidentify") );
                                    hide();
                                } else if (item.cursed) {
                                    GLog.w( Messages.get(Ringbox.class, "ringcursed") );
                                    hide();
                                } else {
                                    if (item.isEquipped(Dungeon.hero)){
                                        ((Ring) item).doUnequip(Dungeon.hero, false, false);
                                    } else {
                                        item.detach(Dungeon.hero.belongings.backpack);
                                    }
                                    ringbox.slot2Ring = (Ring) item;
                                    item(ringbox.slot2Ring);
                                    ringboxLeveling buff = Dungeon.hero.buff(ringboxLeveling.class);
                                    if (buff != null) buff.updateProxyBuffs();
                                }

                            }
                        });
                    }
                }

                @Override
                protected boolean onLongClick() {
                    if (item() != null && item().name() != null){
                        GameScene.show(new WndInfoItem(item()));
                        return true;
                    }
                    return false;
                }
            };

            slot2.setRect( slot1.right() + BTN_GAP, slot1.top(), BTN_SIZE, BTN_SIZE );
            if (ringbox.slot2Ring != null) {
                slot2.item(ringbox.slot2Ring);
            } else {
                slot2.item(new WndBag.Placeholder(ItemSpriteSheet.RING_HOLDER));
            }
            add( slot2 );

            if (thirdRingPower == 0) {
                resize(WIDTH, (int) (slot2.bottom() + GAP));
            } else {
                slot3 = new ItemButton(){
                    @Override
                    protected void onClick() {
                        if (ringbox.slot3Ring != null){
                            item(new WndBag.Placeholder(ItemSpriteSheet.RING_HOLDER));
                            if (!ringbox.slot3Ring.doPickUp(Dungeon.hero)){
                                Dungeon.level.drop( ringbox.slot3Ring, Dungeon.hero.pos);
                            }
                            ringbox.slot3Ring = null;
                            ringboxLeveling buff = Dungeon.hero.buff(ringboxLeveling.class);
                            if (buff != null) buff.updateProxyBuffs();
                        } else {
                            GameScene.selectItem(new WndBag.ItemSelector() {

                                @Override
                                public String textPrompt() {
                                    return Messages.get(Ringbox.class, "selectring");
                                }

                                @Override
                                public Class<?extends Bag> preferredBag(){
                                    return Belongings.Backpack.class;
                                }

                                @Override
                                public boolean itemSelectable(Item item) {
                                    return item instanceof Ring;
                                }

                                @Override
                                public void onSelect(Item item) {
                                    if (!(item instanceof Ring)) {
                                        //do nothing, should only happen when window is cancelled
                                    } else if (!item.isIdentified()) {
                                        GLog.w( Messages.get(Ringbox.class, "mustidentify") );
                                        hide();
                                    } else if (item.cursed) {
                                        GLog.w( Messages.get(Ringbox.class, "ringcursed") );
                                        hide();
                                    } else {
                                        if (item.isEquipped(Dungeon.hero)){
                                            ((Ring) item).doUnequip(Dungeon.hero, false, false);
                                        } else {
                                            item.detach(Dungeon.hero.belongings.backpack);
                                        }
                                        ringbox.slot3Ring = (Ring) item;
                                        item(ringbox.slot3Ring);
                                        ringboxLeveling buff = Dungeon.hero.buff(ringboxLeveling.class);
                                        if (buff != null) buff.updateProxyBuffs();
                                    }

                                }
                            });
                        }
                    }

                    @Override
                    protected boolean onLongClick() {
                        if (item() != null && item().name() != null){
                            GameScene.show(new WndInfoItem(item()));
                            return true;
                        }
                        return false;
                    }
                };

                slot3.setRect( slot2.right() + BTN_GAP, slot2.top(), BTN_SIZE, BTN_SIZE );
                if (ringbox.slot3Ring != null) {
                    slot3.item(ringbox.slot3Ring);
                } else {
                    slot3.item(new WndBag.Placeholder(ItemSpriteSheet.RING_HOLDER));
                }
                add( slot3 );
                resize((int) (slot3.right() + GAP), (int) (slot3.bottom() + GAP));
            }
        }

    }

    public class ringboxLeveling extends ArtifactBuff{

        // Track the proxy buffs we've attached
        private ArrayList<Ring.RingBuff> proxyBuffs = new ArrayList<>();

        @Override
        public boolean attachTo(Char target) {
            if (super.attachTo(target)) {
                updateProxyBuffs();
                return true;
            }
            return false;
        }

        @Override
        public void detach() {
            // Remove all proxy buffs when ringbox is unequipped
            for (Ring.RingBuff buff : proxyBuffs) {
                buff.detach();
            }
            proxyBuffs.clear();
            super.detach();
        }

        public void updateProxyBuffs() {
            if (target == null) return;

            // Remove old proxy buffs
            for (Ring.RingBuff buff : proxyBuffs) {
                buff.detach();
            }
            proxyBuffs.clear();

            // Create new proxy buffs for each ring
            Ring[] rings = getRings();
            float[] powers = getRingPower();

            for (int i = 0; i < 3; i++) {
                if (rings[i] != null && powers[i] > 0) {
                    try {
                        // Create the actual buff instance through the ring's buff() method
                        // This properly handles inner class instantiation
                        Ring.RingBuff proxyBuff = rings[i].buff();

                        if (proxyBuff != null) {
                            // Create a scaled wrapper of the source ring
                            ScaledRing scaledRing = new ScaledRing(rings[i], powers[i]);

                            // Inject the scaled ring into the buff so it reports scaled levels
                            proxyBuff.setRingSource(scaledRing);

                            // Attach the buff to the target
                            proxyBuff.attachTo(target);

                            // Track it for later cleanup
                            proxyBuffs.add(proxyBuff);
                        }

                    } catch (Exception e) {
                        // If something fails, log it but continue
                        GLog.w("Failed to create proxy buff for ring: " + rings[i].getClass().getSimpleName());
                    }
                }
            }
        }

        @Override
        public boolean act() {
            spend( TICK );
            return true;
        }

        public void gainExp( float levelPortion ) {
            if (cursed || target.buff(MagicImmune.class) != null || levelPortion == 0) return;

            exp += Math.round(levelPortion*100);

            //past the soft charge cap, gaining  charge from leveling is slowed.
            if (charge > 5+(level()*2)){
                levelPortion *= (5+((float)level()*2))/charge;
            }
            partialCharge += levelPortion*6f;

            if (exp > 100+level()*100 && level() < levelCap){
                exp -= 100+level()*100;
                GLog.p( Messages.get(Ringbox.class, "levelup") );
                upgrade();
                if (level() == 3) {
                    GLog.p(Messages.get(Ringbox.class, "levelupthirdslot"));
                }
            }
            // Update proxy buffs when ringbox levels up (powers change)
            updateProxyBuffs();
        }

        public Ring[] getRings() {
            return new Ring[] { slot1Ring, slot2Ring, slot3Ring };
        }

        public float[] getRingPower() {
            float[] toReturn = new float[3];

            toReturn[0] = 0.3f;
            toReturn[1] = 0.3f;
            toReturn[2] = 0.0f;

            if (level() >= 1) toReturn[0] += 0.125f;
            if (level() >= 2) toReturn[1] += 0.125f;
            if (level() >= 3) toReturn[2] += 0.3f;
            if (level() >= 4) toReturn[0] += 0.125f;
            if (level() >= 5) toReturn[1] += 0.125f;
            if (level() >= 6) toReturn[2] += 0.125f;
            if (level() >= 7) toReturn[0] += 0.125f;
            if (level() >= 8) toReturn[1] += 0.125f;
            if (level() >= 9) toReturn[2] += 0.125f;
            if (level() >= 10) toReturn[2] += 0.125f;

            return toReturn;
        }
    }
}
