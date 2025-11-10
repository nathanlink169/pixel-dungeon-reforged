/*
 * Pixel Dungeon Reforged
 * Copyright (C) 2024-2025 Nathan Pringle
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

package com.shatteredpixel.shatteredpixeldungeon.combat;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;

/**
 * Immutable results of an attack
 * It is a single source of truth for the results of the attack.
 */
public class AttackResult {
    public final AttackContext context;
    public final ResultType result;
    public final int damageDealt;
    public final boolean killed;

    private AttackResult(AttackContext context, ResultType result, int damageDealt) {
        this.context = context;
        this.result = result;
        this.damageDealt = damageDealt;
        this.killed = !context.defender.isAlive();
    }

    public static AttackResult hit(AttackContext context, int damage) {
        return new AttackResult(context, ResultType.HIT, damage);
    }

    public static AttackResult miss(AttackContext context) {
        return new AttackResult(context, ResultType.MISS, 0);
    }

    public static AttackResult invulnerable(AttackContext context) {
        return new AttackResult(context, ResultType.INVULNERABLE, 0);
    }

    public enum ResultType {
        HIT, MISS, INVULNERABLE
    }
}
