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

package com.shatteredpixel.shatteredpixeldungeon.actors.mobs;

import com.shatteredpixel.shatteredpixeldungeon.Constants;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.combat.DamageType;
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.StatueSprite;
import com.shatteredpixel.shatteredpixeldungeon.utils.BundleableProperty;
import com.watabou.utils.Bundle;

import java.util.EnumSet;

public class ArmoredStatue extends Statue {

	@Override
	public Constants.mobs.mobsBase GetConstants() { return Constants.mobs.armoredstatue; }

	@Override
	public int GetMaxHP() {
		return 30 + Dungeon.depth * 10;
	}

	@Override
	public void createWeapon(boolean useDecks) {
		super.createWeapon(useDecks);

		m_Armour.Set(Generator.randomArmor());
		m_Armour.Get().cursed = false;
		m_Armour.Get().inscribe(Armor.Glyph.random());
	}

	protected BundleableProperty.Object<Armor> m_Armour = new BundleableProperty.Object("armor", null);

	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
		m_Armour.Store(bundle);
	}

	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle( bundle );
		m_Armour.Restore(bundle);
	}

	@Override
	public int drRoll(EnumSet<DamageType> damageType) {
		return super.drRoll(damageType) + m_Armour.Get().drRoll(damageType);
	}

	//used in some glyph calculations
	public Armor armor(){
		return m_Armour.Get();
	}

	@Override
	public int glyphLevel(Class<? extends Armor.Glyph> cls) {
		if (m_Armour.Get() != null && m_Armour.Get().hasGlyph(cls, this)){
			return Math.max(super.glyphLevel(cls), m_Armour.Get().buffedLvl());
		} else {
			return super.glyphLevel(cls);
		}
	}

	@Override
	public CharSprite sprite() {
		CharSprite sprite = super.sprite();
		if (sprite instanceof StatueSprite) {
			if (m_Armour.Get() != null) {
				((StatueSprite) sprite).setArmor(m_Armour.Get().tier);
			} else {
				((StatueSprite) sprite).setArmor(3);
			}
		}
		return sprite;
	}

	@Override
	public int defenseSkill() {
		return Math.round(super.defenseSkill() + m_Armour.Get().augment.evasionFactor(m_Armour.Get().buffedLvl()));
	}

	@Override
	public void die( Object cause ) {
		m_Armour.Get().identify(false);
		Dungeon.level.drop( m_Armour.Get(), pos ).sprite.drop();
		super.die( cause );
	}

	@Override
	public String description(boolean forceNoMonsterUnknown) {
		String desc = Messages.get(this, "desc");
		if (m_Weapon.Get() != null && m_Armour.Get() != null){
			desc += "\n\n" + Messages.get(this, "desc_arm_wep", m_Weapon.Get().name(), m_Armour.Get().name());
		}
		return desc;
	}

	@Override
	public Armor getArmor() {
		return m_Armour.Get();
	}
}
