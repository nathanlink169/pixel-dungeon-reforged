/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2025 Evan Debenham
 *
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

package com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.darts;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Bless;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.combat.AttackContext;
import com.shatteredpixel.shatteredpixeldungeon.combat.CombatModifier;
import com.shatteredpixel.shatteredpixeldungeon.combat.DamageType;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.ShadowParticle;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Random;

public class HolyDart extends TippedDart implements CombatModifier.PostArmorDamageModifier {

	{
		image = ItemSpriteSheet.HOLY_DART;
	}

	@Override
	protected void applyDartEffect(Char attacker, Char defender) {
		//do nothing to the hero when processing charged shot
		if (processingChargedShot && defender == attacker){
			return;
		}

		if (attacker.alignment == defender.alignment){
			Buff.affect(defender, Bless.class, Math.round(Bless.DURATION));
			return;
		}

		if (Char.hasProp(defender, Char.Property.UNDEAD) || Char.hasProp(defender, Char.Property.DEMONIC)){
			defender.sprite.emitter().start( ShadowParticle.UP, 0.05f, 10+buffedLvl() );
			Sample.INSTANCE.play(Assets.Sounds.BURNING);
			defender.Damage(Random.NormalIntRange(10 + Dungeon.scalingDepth()/3, 20 + Dungeon.scalingDepth()/3), this, DamageType.of(DamageType.POSITIVE_ENERGY));
			//also do not bless enemies if processing charged shot
		} else if (!processingChargedShot){
			Buff.affect(defender, Bless.class, Math.round(Bless.DURATION));
		}
	}

	@Override
	public int modifyPostArmorDamage(AttackContext context, int currentDamage) {
		return context.attacker.alignment == context.defender.alignment ? 0 : currentDamage;
	}
}
