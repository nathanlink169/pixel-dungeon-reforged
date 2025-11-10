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
import com.shatteredpixel.shatteredpixeldungeon.Randomizer;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.combat.AttackContext;
import com.shatteredpixel.shatteredpixeldungeon.combat.CombatModifier;
import com.shatteredpixel.shatteredpixeldungeon.combat.DamageType;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.items.Gold;
import com.shatteredpixel.shatteredpixeldungeon.items.Honeypot;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.utils.BundleableProperty;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

public class Thief extends Mob implements CombatModifier.OnHitEffect, CombatModifier.OnDamageEffect {

	{
		WANDERING = new Wandering();
		FLEEING = new Fleeing();
	}

	@Override
	public Constants.mobs.mobsBase GetConstants() { return Constants.mobs.thief; }

	private BundleableProperty.Object<Item> m_Item = new BundleableProperty.Object<>("item");
	private BundleableProperty.Int m_MaxGold = new BundleableProperty.Int("max_gold");

	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
		m_Item.Store(bundle);
		m_MaxGold.Store(bundle);
	}

	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle( bundle );
		m_Item.Restore(bundle);
		m_MaxGold.Restore(bundle);
	}

	public Item GetItem() {
		return m_Item.Get();
	}

	public void SetItem(Item item) {
		m_Item.Set(item);
	}

	@Override
	public float speed() {
		if (m_Item.Get() != null) return (5.0f * super.speed()) / 6.0f;
		else return super.speed();
	}

	@Override
	public float attackDelay() {
		if (getRandomizerEnabled(RandomTraits.SINGLE_STRIKE)) {
			return super.attackDelay() * 2;
		}
		return super.attackDelay();
	}

	@Override
	public float GetLootChance(int slot) {
		//each drop makes future drops 1/3 as likely
		// so loot chance looks like: 1/33, 1/100, 1/300, 1/900, etc.
		return super.GetLootChance(slot) * (float)Math.pow(1/3f, Dungeon.LimitedDrops.THEIF_MISC.count);
	}

	@Override
	public void rollToDropLoot() {
		if (m_Item.Get() != null) {
			Dungeon.level.drop( m_Item.Get(), pos ).sprite.drop();
			//updates position
			if (m_Item.Get() instanceof Honeypot.ShatteredPot) ((Honeypot.ShatteredPot)m_Item.Get()).dropPot( this, pos );
			m_Item.Set(null);
		}
		super.rollToDropLoot();
	}

	@Override
	public Item createLoot(int itemSlot) {
		Dungeon.LimitedDrops.THEIF_MISC.count++;
		return super.createLoot(itemSlot);
	}

	protected boolean steal( Hero hero ) {

		Item toSteal = hero.belongings.getThiefItemToSteal();

		if (toSteal != null ) {

			GLog.w( Messages.get(Thief.class, "stole", toSteal.name()) );
			if (!toSteal.stackable) {
				Dungeon.quickslot.convertToPlaceholder(toSteal);
			}
			Item.updateQuickslot();

			m_Item.Set(toSteal.detach( hero.belongings.backpack ));
			if (m_Item.Get() instanceof Honeypot){
				m_Item.Set(((Honeypot)m_Item.Get()).shatter(this, this.pos));
			} else if (m_Item.Get() instanceof Honeypot.ShatteredPot) {
				((Honeypot.ShatteredPot)m_Item.Get()).pickupPot(this);
			}

			return true;
		} else {
			return false;
		}
	}

	@Override
	public String description(boolean forceNoMonsterUnknown) {
		String desc = super.description(forceNoMonsterUnknown);

		if (m_Item.Get() != null) {
			desc += Messages.get(this, "carries", m_Item.Get().name() );
		}

		return desc;
	}

	@Override
	public void onDamage(AttackContext context, int damageDealt) {
		if (context.defender == this) {
			if (getRandomizerEnabled(RandomTraits.GOLD_BAGS)) {
				if (m_MaxGold.Get() == 0) {
					m_MaxGold.Set(Random.Int(25, 50));
				}
				float percDamage = (float)damageDealt / (float)GetMaxHP();
				Dungeon.level.drop( new Gold((int) (m_MaxGold.Get() * percDamage)), pos ).sprite.drop();
			}
			else if (state == FLEEING) {
				Dungeon.level.drop( new Gold(), pos ).sprite.drop();
			}
		}
	}

	@Override
	public void onHit(AttackContext context, int finalDamage) {
		if (context.attacker == this) {
			if (getRandomizerEnabled(RandomTraits.CLUMSY_HANDS)) {
				if (Random.Int(3) > 0) {
					return;
				}
			}

			if (alignment == Alignment.ENEMY && m_Item.Get() == null
					&& enemy instanceof Hero && steal((Hero) enemy)) {
				state = FLEEING;
			}
		}
	}

	@Override
	public int priority() {
		return Priority.NORMAL;
	}

	@Override
	public boolean appliesTo(AttackContext context) {
		return context.attacker == this || context.defender == this;
	}

	private class Wandering extends Mob.Wandering {
		
		@Override
		public boolean act(Mob mob, boolean enemyInFOV, boolean justAlerted) {
			super.act(mob, enemyInFOV, justAlerted);
			
			//if an enemy is just noticed and the thief posses an item, run, don't fight.
			if (state == HUNTING && m_Item.Get() != null){
				state = FLEEING;
			}
			
			return true;
		}
	}

	private static class Fleeing extends Mob.Fleeing {
		@Override
		protected void escaped(Mob mob) {
			if (((Thief)mob).m_Item.Get() != null
					&& !Dungeon.level.heroFOV[mob.pos]
					&& Dungeon.level.distance(Dungeon.hero.pos, mob.pos) >= 6) {

				int count = 32;
				int newPos;
				do {
					newPos = Dungeon.level.randomRespawnCell( mob );
					if (count-- <= 0) {
						break;
					}
				} while (newPos == -1 || Dungeon.level.heroFOV[newPos] || Dungeon.level.distance(newPos, mob.pos) < (count/3));

				if (newPos != -1) {

					mob.pos = newPos;
					mob.sprite.place( mob.pos );
					mob.sprite.visible = Dungeon.level.heroFOV[mob.pos];
					if (Dungeon.level.heroFOV[mob.pos]) CellEmitter.get(mob.pos).burst(Speck.factory(Speck.WOOL), 6);

				}

				if (((Thief)mob).m_Item.Get() != null) GLog.n( Messages.get(Thief.class, "escapes", ((Thief)mob).m_Item.Get().name()));
				((Thief)mob).m_Item.Set( null );
				mob.state = mob.WANDERING;
			} else {
				mob.state = mob.WANDERING;
			}
		}
	}


	public enum RandomTraits {
		MASTER_PICKPOCKET, BANDIT_RECRUITMENT, BOLD_FINGERS, CLUMSY_HANDS, SINGLE_STRIKE, GOLD_BAGS
	}

	public static boolean getRandomizerEnabled(RandomTraits r) {
		switch (r) {
			case MASTER_PICKPOCKET: return Randomizer.getCreatureBuff(Thief.class) == 1;
			case BANDIT_RECRUITMENT: return Randomizer.getCreatureBuff(Thief.class) == 2;
			case BOLD_FINGERS: return Randomizer.getCreatureBuff(Thief.class) == 3;
			case CLUMSY_HANDS: return Randomizer.getCreatureNerf(Thief.class) == 1;
			case SINGLE_STRIKE: return Randomizer.getCreatureNerf(Thief.class) == 2;
			case GOLD_BAGS: return Randomizer.getCreatureNerf(Thief.class) == 3;
		}
		return false;
	}
}