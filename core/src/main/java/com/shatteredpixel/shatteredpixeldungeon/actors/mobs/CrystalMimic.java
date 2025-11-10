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

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Constants;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Haste;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.combat.AttackContext;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.items.Heap;
import com.shatteredpixel.shatteredpixeldungeon.items.Honeypot;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.Artifact;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.Ring;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfTeleportation;
import com.shatteredpixel.shatteredpixeldungeon.items.trinkets.MimicTooth;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class CrystalMimic extends Mimic {

	{
		FLEEING = new Fleeing();
	}
	@Override
	public Constants.mobs.mobsBase GetConstants() { return Constants.mobs.crystalmimic; }

	@Override
	public String name(boolean forceNoMonsterUnknown) {
		if (alignment == Alignment.NEUTRAL){
			return Messages.get(Heap.class, "crystal_chest");
		} else {
			return super.name(forceNoMonsterUnknown);
		}
	}

	@Override
	public String description(boolean forceNoMonsterUnknown) {
		if (alignment == Alignment.NEUTRAL){
			String desc = null;
			for (Item i : m_Items.Get()){
				if (i instanceof Artifact){
					desc = Messages.get(Heap.class, "crystal_chest_desc", Messages.get(Heap.class, "artifact"));
					break;
				} else if (i instanceof Ring){
					desc = Messages.get(Heap.class, "crystal_chest_desc", Messages.get(Heap.class, "ring"));
					break;
				} else if (i instanceof Wand){
					desc = Messages.get(Heap.class, "crystal_chest_desc", Messages.get(Heap.class, "wand"));
					break;
				}
			}
			if (desc == null) {
				desc = Messages.get(Heap.class, "locked_chest_desc");
			}
			if (!MimicTooth.stealthyMimics()){
				desc += "\n\n" + Messages.get(this, "hidden_hint");
			}
			return desc;
		} else {
			return super.description(forceNoMonsterUnknown);
		}
	}

	//does not deal bonus damage, steals instead. See attackProc
	@Override
	public int damageRoll(AttackContext.AttackType type, boolean isMaxDamage) {
		if (alignment == Alignment.NEUTRAL) {
			alignment = Alignment.ENEMY;
			int dmg = super.damageRoll(type, isMaxDamage);
			alignment = Alignment.NEUTRAL;
			return dmg;
		} else {
			return super.damageRoll(type, isMaxDamage);
		}
	}

	public void stopHiding(){
		state = FLEEING;
		if (sprite != null) sprite.idle();
		//haste for 2 turns if attacking
		if (alignment == Alignment.NEUTRAL){
			Buff.affect(this, Haste.class, 2f);
		} else {
			Buff.affect(this, Haste.class, 1f);
		}
		if (Actor.chars().contains(this) && Dungeon.level.heroFOV[pos]) {
			enemy = Dungeon.hero;
			m_Target.Set(Dungeon.hero.pos);
			GLog.w(Messages.get(this, "reveal") );
			CellEmitter.get(pos).burst(Speck.factory(Speck.STAR), 10);
			Sample.INSTANCE.play(Assets.Sounds.MIMIC, 1, 1.25f);
		}
	}

	@Override
	public void onHit(AttackContext context, int finalDamage) {
		super.onHit(context, finalDamage);
		if (alignment == Alignment.NEUTRAL && context.defender == Dungeon.hero){
			steal( Dungeon.hero );

		} else {
			ArrayList<Integer> candidates = new ArrayList<>();
			for (int i : PathFinder.NEIGHBOURS8){
				if (Dungeon.level.passable[pos+i] && Actor.findChar(pos+i) == null){
					candidates.add(pos + i);
				}
			}

			if (!candidates.isEmpty()){
				ScrollOfTeleportation.appear(context.defender, Random.element(candidates));
			}

			if (alignment == Alignment.ENEMY) state = FLEEING;
		}
	}

	protected void steal( Hero hero ) {

		int tries = 10;
		Item item;
		do {
			item = hero.belongings.randomUnequipped();
		} while (tries-- > 0 && (item == null || item.unique || item.level() > 0));

		if (item != null && !item.unique && item.level() < 1 ) {

			GLog.w( Messages.get(this, "ate", item.name()) );
			if (!item.stackable) {
				Dungeon.quickslot.convertToPlaceholder(item);
			}
			item.updateQuickslot();

			if (item instanceof Honeypot){
				m_Items.Add(((Honeypot)item).shatter(this, this.pos));
				item.detach( hero.belongings.backpack );
			} else {
				m_Items.Add(item.detach( hero.belongings.backpack ));
				if ( item instanceof Honeypot.ShatteredPot)
					((Honeypot.ShatteredPot)item).pickupPot(this);
			}

		}
	}

	@Override
	protected void generatePrize( boolean useDecks ) {
		//Crystal mimic already contains a prize item. Just guarantee it isn't cursed.
		for (Item i : m_Items.Get()){
			i.cursed = false;
			i.cursedKnown = true;
		}
	}

	private static class Fleeing extends Mob.Fleeing {
		@Override
		protected void escaped(Mob mob) {
			if (!Dungeon.level.heroFOV[mob.pos] && Dungeon.level.distance(Dungeon.hero.pos, mob.pos) >= 6) {
				GLog.n(Messages.get(CrystalMimic.class, "escaped"));
				mob.destroy();
				mob.sprite.killAndErase();
			} else {
				mob.state = mob.WANDERING;
			}
		}

		@Override
		protected void nowhereToRun(Mob mob) {
			super.nowhereToRun(mob);
			if (mob.state == mob.HUNTING){
				mob.spend(-TICK); //crystal mimics are fast!
			}
		}
	}

}
