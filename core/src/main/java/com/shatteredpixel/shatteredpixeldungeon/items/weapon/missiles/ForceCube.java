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

package com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Badges;
import com.shatteredpixel.shatteredpixeldungeon.Challenges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfBlastWave;
import com.shatteredpixel.shatteredpixeldungeon.combat.DamageType;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.journal.Notes;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.TenguDartTrap;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.PathFinder;

import java.util.ArrayList;

public class ForceCube extends MissileWeapon {
	
	{
		image = ItemSpriteSheet.FORCE_CUBE;
		
		tier = 5;
		baseUses = 5;
		
		sticky = false;

		damageType = DamageType.of(DamageType.BLUDGEONING);
	}

	@Override
	public int STRReq(int lvl) {
		if (Dungeon.isChallenged(Challenges.CUBE)) {
			return 10;
		}
		return super.STRReq(lvl);
	}

	@Override
	protected void decrementDurability() {
		if (Dungeon.isChallenged(Challenges.CUBE)) {
			return;
		}
		super.decrementDurability();
	}

	@Override
	public void hitSound(float pitch) {
		//no hitsound as it never hits enemies directly
	}

	@Override
	protected void onThrow(int cell) {
		if (Dungeon.level.pit[cell]){
			super.onThrow(cell);
			return;
		}

		rangedHit( null, cell );
		Dungeon.level.pressCell(cell);
		
		ArrayList<Char> targets = new ArrayList<>();
		if (Actor.findChar(cell) != null) targets.add(Actor.findChar(cell));
		
		for (int i : PathFinder.NEIGHBOURS8){
			if (!(Dungeon.level.traps.get(cell+i) instanceof TenguDartTrap)) Dungeon.level.pressCell(cell+i);
			if (Actor.findChar(cell + i) != null) targets.add(Actor.findChar(cell + i));
		}
		
		for (Char target : targets){
			curUser.shoot(target, this);
			if (target == Dungeon.hero && !target.isAlive()){
				Badges.validateDeathFromFriendlyMagic();
				Dungeon.fail(this);
				if (Dungeon.isChallenged(Challenges.CUBE)) {
					GLog.p(Messages.get(this, "ondeath_challenge"));
				} else {
					GLog.n(Messages.get(this, "ondeath"));
				}
			}
		}
		
		WandOfBlastWave.BlastWave.blast(cell);
		Sample.INSTANCE.play( Assets.Sounds.BLAST );
	}

	@Override
	public String desc() {
		if (Dungeon.isChallenged(Challenges.CUBE)) {
			return Messages.get(this, "desc_challenge");
		}
		return super.desc();
	}

	@Override
	public String info() {
		if (Dungeon.isChallenged(Challenges.CUBE)) {
			String info = "";
			if (Dungeon.hero != null) {
				Notes.CustomRecord note = Notes.findCustomRecord(customNoteID);
				if (note != null) {
					//we swap underscore(0x5F) with low macron(0x2CD) here to avoid highlighting in the item window
					info += Messages.get(this, "custom_note", note.title().replace('_', '_')) + "\n\n" + desc();
				} else {
					note = Notes.findCustomRecord(getClass());
					if (note != null) {
						//we swap underscore(0x5F) with low macron(0x2CD) here to avoid highlighting in the item window
						info += Messages.get(this, "custom_note_type", note.title().replace('_', '_')) + "\n\n" + desc();
					}
				}
			}

			info += desc();

			info += "\n\n" + Messages.get( this, "stats_challenge",
					Math.round(augment.damageFactor(min())),
					Math.round(augment.damageFactor(max())));

			if (enchantment != null && (cursedKnown || !enchantment.curse())){
				info += "\n\n" + Messages.get(Weapon.class, "enchanted", enchantment.name());
				info += " " + Messages.get(enchantment, "desc");
			}

			return info;
		}
		return super.info();
	}
}
