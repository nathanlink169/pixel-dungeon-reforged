package com.shatteredpixel.shatteredpixeldungeon.items.trinkets;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.ShadowParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.Artifact;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class LuckyDice extends Trinket {

    {
        image = ItemSpriteSheet.LUCKY_DICE;
    }

    @Override
    protected int upgradeEnergyCost() {
        //6 -> 8(14) -> 10(24) -> 12(36)
        return 6+2*level();
    }

    @Override
    public String statsDesc() {
        if (isIdentified()){
            return Messages.get(this, "stats_desc", String.format("%.1f", (expMultiplier(buffedLvl())-1) * 100), String.format("%.1f",((curseChance(buffedLvl())) * 100)));
        } else {
            return Messages.get(this, "typical_stats_desc", String.format("%.1f",(expMultiplier(0)-1) * 100), String.format("%.1f",((curseChance(0)) * 100)));
        }
    }

    public static void curseItem() {
        if (Random.Float() < curseChance()) {
            ArrayList<Item> items = Dungeon.hero.belongings.getAllItems(Item.class);
            ArrayList<Item> eligibleItems = new ArrayList<>();
            for (Item item : items) {
                if (!item.cursed) {
                    if (item instanceof Weapon || item instanceof Armor || item instanceof Artifact) {
                        eligibleItems.add(item);
                    }
                }
            }

            if (eligibleItems.size() > 0) {
                Item randomItem = eligibleItems.get(Random.Int(eligibleItems.size()));
                if (randomItem != null) {
                    randomItem.cursed = true;
                    if (!randomItem.cursedKnown) {
                        randomItem.cursedKnown = randomItem.isIdentified();
                    }
                    if (randomItem instanceof Weapon) {
                        ((Weapon) randomItem).enchant(Weapon.Enchantment.randomCurse());
                    } else if (randomItem instanceof Armor) {
                        ((Armor) randomItem).inscribe(Armor.Glyph.randomCurse());
                    } // artifacts just need the boolean set

                    Dungeon.hero.sprite.emitter().burst( ShadowParticle.CURSE, 6 );
                    Sample.INSTANCE.play( Assets.Sounds.CURSED );
                    GLog.n(Messages.get(LuckyDice.class, "item_cursed"));
                }
            }
        }
    }

    public static float curseChance(){
        return curseChance(trinketLevel(LuckyDice.class));
    }

    public static float curseChance(int level ){
        if (level == -1){
            return 0f;
        } else {
            return 0.005f * (level + 1);
        }
    }

    public static float expMultiplier() {
        return expMultiplier(trinketLevel(LuckyDice.class));
    }

    public static float expMultiplier(int level) {
        if (level == -1){
            return 1f;
        } else {
            return 1 + 0.1f * (level + 1);
        }
    }
}
