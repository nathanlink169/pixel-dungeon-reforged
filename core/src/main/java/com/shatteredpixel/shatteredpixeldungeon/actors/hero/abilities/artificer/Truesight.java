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

package com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.artificer;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Awareness;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Blindness;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.ArmorAbility;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.effects.CheckedCell;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.ClassArmor;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfMagicMapping;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfBlastWave;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Gun;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.ShadowCaster;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.HeroIcon;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Point;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class Truesight extends ArmorAbility {

	{
		baseChargeUse = 20f;
	}

	@Override
	protected void activate(ClassArmor armor, Hero hero, Integer target) {
		Point c = Dungeon.level.cellToPoint(hero.pos);
		int distance = 8 + hero.pointsInTalent(Talent.ENHANCED_VISION) * 2;

		int[] rounding = ShadowCaster.rounding[distance];

		int left, right;
		int curr;
		boolean noticed = false;
		for (int y = Math.max(0, c.y - distance); y <= Math.min(Dungeon.level.height()-1, c.y + distance); y++) {
			if (rounding[Math.abs(c.y - y)] < Math.abs(c.y - y)) {
				left = c.x - rounding[Math.abs(c.y - y)];
			} else {
				left = distance;
				while (rounding[left] < rounding[Math.abs(c.y - y)]){
					left--;
				}
				left = c.x - left;
			}
			right = Math.min(Dungeon.level.width()-1, c.x + c.x - left);
			left = Math.max(0, left);
			for (curr = left + y * Dungeon.level.width(); curr <= right + y * Dungeon.level.width(); curr++){

				GameScene.effectOverFog( new CheckedCell( curr, hero.pos ) );
				Dungeon.level.mapped[curr] = true;

				if (Dungeon.level.secret[curr]) {
					Dungeon.level.discover(curr);

					if (Dungeon.level.heroFOV[curr]) {
						GameScene.discoverTile(curr, Dungeon.level.map[curr]);
						ScrollOfMagicMapping.discover(curr);
						noticed = true;
					}
				}

			}
		}

		if (noticed) {
			Sample.INSTANCE.play( Assets.Sounds.SECRET );
		}

		if (hero.hasTalent(Talent.SONAR)) {
			Awareness a = Buff.affect(hero, Awareness.class, Awareness.DURATION);
			switch (hero.pointsInTalent(Talent.SONAR)) {
				case 1:
					a.setType(Awareness.Type.HalfItem);
					break;
				case 2:
					a.setType(Awareness.Type.FullItem);
					break;
				case 3:
					a.setType(Awareness.Type.HalfMonster);
					break;
				case 4:
					a.setType(Awareness.Type.FullMonster);
					break;
			}
			Dungeon.observe();
		}

		if (hero.hasTalent(Talent.BRIGHT_LIGHT)) {
			int chance = hero.pointsInTalent(Talent.BRIGHT_LIGHT) * 2;
			for (Mob m : Dungeon.level.mobs) {
				if (m.state != m.SLEEPING && m.fieldOfView[Dungeon.hero.pos]) {
					if (Random.Int(10) < chance) {
						Buff.affect(m, Blindness.class, Blindness.DURATION);
					}
				}
			}
			GameScene.flash(0x80FFFFFF);
		}

		Sample.INSTANCE.play( Assets.Sounds.TELEPORT );
		GameScene.updateFog();

		armor.charge -= chargeUse(hero);
		armor.updateQuickslot();
	}

	@Override
	public int icon() {
		return HeroIcon.TRUESIGHT;
	}

	@Override
	public Talent[] talents() {
		return new Talent[]{Talent.ENHANCED_VISION, Talent.SONAR, Talent.BRIGHT_LIGHT, Talent.HEROIC_ENERGY};
	}
}
