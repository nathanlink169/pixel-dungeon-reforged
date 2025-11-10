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

package com.shatteredpixel.shatteredpixeldungeon.items.armor.glyphs;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.AscensionChallenge;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Bless;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.ChampionEnemy;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Daze;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Hex;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.combat.AttackContext;
import com.shatteredpixel.shatteredpixeldungeon.combat.CombatModifier;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.items.trinkets.FerretTuft;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.watabou.utils.GameMath;

public class Stone extends Armor.Glyph implements CombatModifier.AccuracyModifier, CombatModifier.EvasionModifier, CombatModifier.PostArmorDamageModifier {

	private static ItemSprite.Glowing GREY = new ItemSprite.Glowing( 0x222222 );

	@Override
	public ItemSprite.Glowing glowing() {
		return GREY;
	}

	private float m_Accuracy;
	private float m_Evasion;
	@Override
	public float modifyAccuracy(AttackContext context, float currentAccuracy) {
		m_Accuracy = currentAccuracy;
		return currentAccuracy;
	}

	@Override
	public float modifyEvasion(AttackContext context, float currentEvasion) {
		m_Evasion = currentEvasion;
		return 0;
	}

	@Override
	public int modifyPostArmorDamage(AttackContext context, int currentDamage) {
		float hitChance;
		if (m_Evasion >= m_Accuracy) {
			hitChance = (m_Accuracy / m_Evasion) / 2f;
		} else {
			hitChance = 1f - (m_Evasion / m_Accuracy) / 2f;
		}

		hitChance = GameMath.gate(0.25f, (1f + 3f * hitChance) / 4f, 1f);
		return (int) Math.ceil(currentDamage * hitChance);
	}

	@Override
	public int priority() {
		return Priority.LOWEST; // We need to get the evasion and accuracy after all modifiers
	}

	@Override
	public boolean appliesTo(AttackContext context) {
		return context.defender.getArmor() != null && context.defender.getArmor().glyph == this;
	}
}
