package com.shatteredpixel.shatteredpixeldungeon.items.weapon;

public class DamageType {
    public static final int NONE				= 1 << 0;
    public static final int BLUDGEONING			= 1 << 1;
    public static final int PIERCING			= 1 << 2;
    public static final int SLASHING    		= 1 << 3;
    public static final int MAGIC               = 1 << 4;

    public static boolean getIsDamageType(int flags, int damageType) {
        return (flags & damageType) != 0;
    }
}
