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

public class WanderersMap extends Trinket {

    {
        image = ItemSpriteSheet.WANDERERS_MAP;
    }

    @Override
    protected int upgradeEnergyCost() {
        //6 -> 8(14) -> 10(24) -> 12(36)
        return 6+2*level();
    }

    @Override
    public String statsDesc() {
        if (isIdentified()){
            return Messages.get(this, "stats_desc", String.format("%.1f", (mapSizeIncrease(buffedLvl())-1) * 100), itemIncreaseCount(buffedLvl()));
        } else {
            return Messages.get(this, "typical_stats_desc", String.format("%.1f",(mapSizeIncrease(0)-1) * 100), itemIncreaseCount(0));
        }
    }

    public static float mapSizeIncrease(){
        return mapSizeIncrease(trinketLevel(WanderersMap.class));
    }

    public static float mapSizeIncrease(int level ){
        if (level == -1){
            return 1f;
        } else {
            return 1.0f + 0.25f * (level + 1);
        }
    }

    public static int itemIncreaseCount() {
        return itemIncreaseCount(trinketLevel(WanderersMap.class));
    }

    public static int itemIncreaseCount(int level) {
        if (level == -1){
            return 0;
        } else {
            return level + 1;
        }
    }
}
