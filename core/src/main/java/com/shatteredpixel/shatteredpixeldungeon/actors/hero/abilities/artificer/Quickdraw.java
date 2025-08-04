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

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FlavourBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.ArmorAbility;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.ClassArmor;
import com.shatteredpixel.shatteredpixeldungeon.items.trinkets.WondrousResin;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.CursedWand;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfBlastWave;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Gun;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.ThrowingStone;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.MissileSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.HeroIcon;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.Game;
import com.watabou.noosa.tweeners.Delayer;
import com.watabou.utils.Callback;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class Quickdraw extends ArmorAbility {

	{
		baseChargeUse = 25f;
	}

	@Override
	protected void activate(ClassArmor armor, Hero hero, Integer target) {
		ArrayList<Mob> primaryTargets = new ArrayList<>();
		ArrayList<Mob> closeTargets = new ArrayList<>();
		ArrayList<Mob> sleepingTargets = new ArrayList<>();

		for (Mob m : hero.getVisibleEnemies()) {
			if (new Ballistica( hero.pos, m.pos, Ballistica.PROJECTILE ).collisionPos == m.pos) {
				// Valid target. Target rules:
				// Do not attack sleeping targets unless there is nothing else to attack.
				// Prioritize targets that are not in melee distance (harder to hit)
				if (m.state == m.SLEEPING) {
					sleepingTargets.add(m);
				} else if (Dungeon.level.adjacent(hero.pos, m.pos)) {
					closeTargets.add(m);
				} else {
					primaryTargets.add(m);
				}
			}
		}

		if (primaryTargets.isEmpty() && closeTargets.isEmpty() && sleepingTargets.isEmpty()) {
			GLog.w(Messages.get(this, "no_targets"));
			return;
		}
		Gun gun = hero.belongings.getItem(Gun.class);
		if (gun == null) {
			GLog.w(Messages.get(this, "no_gun"));
			return;
		}

		int chanceToRepeat = hero.pointsInTalent(Talent.DOUBLE_BARREL);
		int count = 5 + hero.pointsInTalent(Talent.MULTISHOT);
		for (int i = 0; i < count; ++i) {
			if (!fire(hero, primaryTargets, closeTargets, sleepingTargets, gun, i == 0)) {
				break;
			}

			if (chanceToRepeat > 0) {
				if (Random.Int(10) < chanceToRepeat) {
					if (!fire(hero, primaryTargets, closeTargets, sleepingTargets, gun, false)) {
						break;
					}
					if (!fire(hero, primaryTargets, closeTargets, sleepingTargets, gun, false)) {
						break;
					}
				}
			}
		}

		hero.sprite.operate( hero.pos );
		Invisibility.dispel();
		hero.busy();
		hero.spend( hero.attackDelay() );

		armor.charge -= chargeUse(hero);
		armor.updateQuickslot();
	}

	private boolean fire(Hero hero, ArrayList<Mob> primaryTargets, ArrayList<Mob> closeTargets, ArrayList<Mob> sleepingTargets, Gun gun, boolean playSFX) {
		Mob thisTarget = null;
		if (!primaryTargets.isEmpty()) {
			int randomIndex = Random.Int(primaryTargets.size());
			thisTarget = primaryTargets.get(randomIndex);
		} else if (!closeTargets.isEmpty()) {
			int randomIndex = Random.Int(closeTargets.size());
			thisTarget = closeTargets.get(randomIndex);
		} else if (!sleepingTargets.isEmpty()) {
			int randomIndex = Random.Int(sleepingTargets.size());
			thisTarget = sleepingTargets.get(randomIndex);
		} else {
			return false;
		}

		hero.sprite.turnTo( hero.pos, thisTarget.pos );
		if (gun.fire(hero, thisTarget.pos, playSFX)) {
			int chanceToKnockback = hero.pointsInTalent(Talent.POWERFUL_SHOT) * 2;
			if (chanceToKnockback > 0) {
				if (Random.Int(10) < chanceToKnockback) {
					//trace a ballistica to our target (which will also extend past them
					Ballistica trajectory = new Ballistica(hero.pos, thisTarget.pos, Ballistica.STOP_TARGET);
					//trim it to just be the part that goes past them
					trajectory = new Ballistica(trajectory.collisionPos, trajectory.path.get(trajectory.path.size() - 1), Ballistica.PROJECTILE);
					//knock them back along that ballistica
					WandOfBlastWave.throwChar(thisTarget,
							trajectory,
							3,
							false,
							true,
							hero);
				}
			}
		}

		if (!thisTarget.isAlive()) {
			primaryTargets.remove(thisTarget);
			closeTargets.remove(thisTarget);
			sleepingTargets.remove(thisTarget);
		} else if (sleepingTargets.contains(thisTarget)) {
			if (thisTarget.state != thisTarget.SLEEPING) {
				// If the gun didn't hit for some reason, the target would stay sleeping
				sleepingTargets.remove(thisTarget);
				if (Dungeon.level.adjacent(hero.pos, thisTarget.pos)) {
					closeTargets.add(thisTarget);
				} else {
					primaryTargets.add(thisTarget);
				}
			}
		} else if (closeTargets.contains(thisTarget)) {
			if (!Dungeon.level.adjacent(hero.pos, thisTarget.pos)) {
				closeTargets.remove(thisTarget);
				primaryTargets.add(thisTarget);
			}
		}
		return true;
	}


	@Override
	public int icon() {
		return HeroIcon.QUICKDRAW;
	}

	@Override
	public Talent[] talents() {
		return new Talent[]{Talent.POWERFUL_SHOT, Talent.DOUBLE_BARREL, Talent.MULTISHOT, Talent.HEROIC_ENERGY};
	}
}
