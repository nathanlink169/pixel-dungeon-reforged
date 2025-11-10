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

package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroAction;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.rogue.DeathMark;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Brute;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.NPC;
import com.shatteredpixel.shatteredpixeldungeon.combat.AttackContext;
import com.shatteredpixel.shatteredpixeldungeon.combat.CombatModifier;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.CellSelector;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.ActionIndicator;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.ui.HeroIcon;
import com.watabou.utils.BArray;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.BitmapText;
import com.watabou.noosa.Image;
import com.watabou.noosa.Visual;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.PathFinder;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Preparation buff for Assassin subclass.
 * Grants bonus damage and assassination ability based on turns spent invisible.
 */
public class Preparation extends Buff implements ActionIndicator.Action,
		CombatModifier.PreArmorDamageModifier, CombatModifier.OnDamageEffect {

	{
		//always acts after other buffs, so invisibility effects can process first
		actPriority = BUFF_PRIO - 1;
	}

	/**
	 * Attack levels based on turns spent invisible
	 */
	public enum AttackLevel {
		LVL_1(1, 0.5f,  1, 1),
		LVL_2(3, 1.0f,  2, 3),
		LVL_3(5, 1.5f,  3, 6),
		LVL_4(7, 2.0f,  4, 12);

		final int turnsReq;
		final float baseDmgBonus;
		final int damageRolls;
		final int blinkDistance;

		AttackLevel(int turns, float damage, int rolls, int blink) {
			turnsReq = turns;
			baseDmgBonus = damage;
			damageRolls = rolls;
			blinkDistance = blink;
		}

		public boolean canKO(Char defender) {
			if (Char.hasProp(defender, Char.Property.BOSS)
					|| Char.hasProp(defender, Char.Property.MINIBOSS)) {
				return false;
			}

			float threshold = KOThreshold();
			return (defender.HP / (float) defender.GetMaxHP()) <= threshold;
		}

		public float KOThreshold() {
			// Base threshold from turns invisible
			float threshold = 0f;
			switch (this) {
				case LVL_1: threshold = 0f; break;
				case LVL_2: threshold = 0.50f; break;
				case LVL_3: threshold = 0.67f; break;
				case LVL_4: threshold = 0.80f; break;
			}

			// Apply Assassin's Reach talent
			if (Dungeon.hero != null && Dungeon.hero.hasTalent(Talent.ASSASSINS_REACH)) {
				threshold += 0.05f * Dungeon.hero.pointsInTalent(Talent.ASSASSINS_REACH);
			}

			return Math.min(1f, threshold);
		}

		public int damageRoll(Char attacker, boolean isMaxDamage) {
			int dmg;

			if (isMaxDamage) {
				// Max damage - take highest of all rolls
				dmg = 0;
				for (int i = 0; i < damageRolls; i++) {
					AttackContext context = new AttackContext.Builder(attacker, null).forceMaxDamage().build();
					dmg = Math.max(dmg, attacker.damageRoll(context));
				}
			} else {
				// Normal - roll multiple times and take best
				dmg = 0;
				for (int i = 0; i < damageRolls; i++) {
					AttackContext context = new AttackContext.Builder(attacker, null).build();
					dmg = Math.max(dmg, attacker.damageRoll(context));
				}
			}

			// Apply preparation damage bonus
			dmg = Math.round(dmg * (1f + baseDmgBonus));

			// Apply Enhanced Lethality talent
			if (attacker instanceof Hero) {
				Hero hero = (Hero) attacker;
				if (hero.hasTalent(Talent.ENHANCED_LETHALITY)) {
					dmg += 1 + hero.pointsInTalent(Talent.ENHANCED_LETHALITY);
				}
			}

			return dmg;
		}

		public int blinkDistance() {
			if (Dungeon.hero == null) return 0;

			// Enhanced Lethality talent increases blink range
			if (Dungeon.hero.hasTalent(Talent.ENHANCED_LETHALITY)) {
				return blinkDistance + Dungeon.hero.pointsInTalent(Talent.ENHANCED_LETHALITY);
			}

			return blinkDistance;
		}

		public static AttackLevel getLvl(int turnsInvis) {
			List<AttackLevel> values = Arrays.asList(values());
			Collections.reverse(values);
			for (AttackLevel lvl : values) {
				if (turnsInvis >= lvl.turnsReq) {
					return lvl;
				}
			}
			return LVL_1;
		}
	}

	private int turnsInvis = 0;
	private boolean consumedThisAttack = false;

	@Override
	public boolean act() {
		if (target.invisible > 0) {
			turnsInvis++;
			if (AttackLevel.getLvl(turnsInvis).blinkDistance() > 0 && target == Dungeon.hero) {
				ActionIndicator.setAction(this);
			}
			spend(TICK);
		} else {
			detach();
		}
		return true;
	}

	@Override
	public void detach() {
		super.detach();
		ActionIndicator.clearAction(this);
	}

	public int attackLevel() {
		return AttackLevel.getLvl(turnsInvis).ordinal() + 1;
	}

	public int damageRoll(Char attacker, boolean isMaxDamage) {
		return AttackLevel.getLvl(turnsInvis).damageRoll(attacker, isMaxDamage);
	}

	public boolean canKO(Char defender) {
		return !defender.isInvulnerable(target.getClass())
				&& AttackLevel.getLvl(turnsInvis).canKO(defender);
	}

	// ============================================================
	// COMBAT MODIFIER IMPLEMENTATION
	// ============================================================

	@Override
	public int priority() {
		// High priority - we want to completely replace damage calculation
		return Priority.HIGH;
	}

	@Override
	public boolean appliesTo(AttackContext context) {
		// Only applies if:
		// 1. We're the attacker
		// 2. It's a melee attack
		// 3. We haven't already consumed preparation this attack
		return context.attacker == target
				&& context.attackType == AttackContext.AttackType.MELEE
				&& !consumedThisAttack;
	}

	@Override
	public int modifyPreArmorDamage(AttackContext context, int currentDamage) {
		// Mark that we're using preparation this attack
		consumedThisAttack = true;

		// Calculate preparation damage
		int prepDamage = damageRoll(context.attacker, false);

		// Trigger bounty hunter talent if hero has it
		if (target == Dungeon.hero && Dungeon.hero.hasTalent(Talent.BOUNTY_HUNTER)) {
			Buff.affect(Dungeon.hero, Talent.BountyHunterTracker.class, 0.0f);
		}

		return prepDamage;
	}

	@Override
	public void onDamage(AttackContext context, int damageDealt) {
		if (!appliesTo(context)) {
			return; // Shouldn't happen, but safety
		}

		// Check for assassination (instant kill on low HP enemies)
		if (context.defender.isAlive() && canKO(context.defender)) {
			performAssassination(context);
		}

		// Reset consumption flag for next attack
		consumedThisAttack = false;

		// Detach preparation after use
		detach();
	}

	/**
	 * Performs the assassination (instant kill).
	 */
	private void performAssassination(AttackContext context) {
		Char enemy = context.defender;

		// Set HP to 0
		enemy.HP = 0;

		// Remove any buffs that would prevent death
		if (enemy.buff(Brute.BruteRage.class) != null) {
			enemy.buff(Brute.BruteRage.class).detach();
		}

		// Ensure death
		if (!enemy.isAlive()) {
			enemy.die(target);
		} else {
			// Edge case: some mechanics prevent death
			// Apply -1 damage to trigger death effects
			enemy.Damage(-1, target, context.damageType);

			// Check for Fear the Reaper (Death Mark)
			DeathMark.processFearTheReaper(enemy);
		}

		// Show assassination message
		if (enemy.sprite != null) {
			enemy.sprite.showStatus(
					CharSprite.NEGATIVE,
					Messages.get(this, "assassinated")
			);
		}
	}

	// ============================================================
	// ACTION INDICATOR IMPLEMENTATION
	// ============================================================

	@Override
	public String actionName() {
		return Messages.get(this, "action_name");
	}

	@Override
	public int actionIcon() {
		return HeroIcon.PREPARATION;
	}

	@Override
	public Visual primaryVisual() {
		Image actionIco = new HeroIcon(this);
		tintIcon(actionIco);
		return actionIco;
	}

	@Override
	public Visual secondaryVisual() {
		BitmapText txt = new BitmapText(PixelScene.pixelFont);
		txt.text(Integer.toString(Math.min(9, turnsInvis)));
		txt.hardlight(CharSprite.POSITIVE);
		txt.measure();
		return txt;
	}

	@Override
	public int indicatorColor() {
		return 0x444444;
	}

	@Override
	public void doAction() {
		GameScene.selectCell(attack);
	}

	private CellSelector.Listener attack = new CellSelector.Listener() {

		@Override
		public void onSelect(Integer cell) {
			if (cell == null) return;
			final Char enemy = Actor.findChar(cell);
			if (enemy == null || Dungeon.hero.isCharmedBy(enemy) || enemy instanceof NPC
					|| !Dungeon.level.heroFOV[cell] || enemy == Dungeon.hero) {
				GLog.w(Messages.get(Preparation.class, "no_target"));
				return;
			}

			// Just attack them if in range
			if (Dungeon.hero.canAttack(enemy)) {
				Dungeon.hero.curAction = new HeroAction.Attack(enemy);
				Dungeon.hero.next();
				return;
			}

			AttackLevel lvl = AttackLevel.getLvl(turnsInvis);

			// Check if we can blink to the enemy
			PathFinder.buildDistanceMap(Dungeon.hero.pos,
					BArray.or(Dungeon.level.passable, Dungeon.level.avoid, null),
					lvl.blinkDistance());

			int dest = -1;
			for (int i : PathFinder.NEIGHBOURS8) {
				// Cannot blink into a cell that's occupied or impassable
				if (Actor.findChar(cell + i) != null) continue;
				if (!Dungeon.level.passable[cell + i] && !Dungeon.level.avoid[cell + i]) continue;
				if (PathFinder.distance[cell + i] == Integer.MAX_VALUE) continue;

				// Pick the closest valid destination
				if (dest == -1 || PathFinder.distance[cell + i] < PathFinder.distance[dest]) {
					dest = cell + i;
				}
			}

			if (dest == -1 || PathFinder.distance[dest] == Integer.MAX_VALUE) {
				GLog.w(Messages.get(Preparation.class, "out_of_reach"));
				return;
			}

			// Perform blink attack
			Dungeon.hero.pos = dest;
			Dungeon.level.occupyCell(Dungeon.hero);

			// Visual effects
			if (Dungeon.level.heroFOV[dest] || Dungeon.level.heroFOV[cell]) {
				CellEmitter.get(dest).burst(Speck.factory(Speck.WOOL), 6);
				Sample.INSTANCE.play(Assets.Sounds.PUFF);
			}

			// Attack the enemy
			Dungeon.hero.curAction = new HeroAction.Attack(enemy);
			Dungeon.hero.next();
		}

		@Override
		public String prompt() {
			return Messages.get(Preparation.class, "prompt");
		}
	};

	// ============================================================
	// UI & DISPLAY
	// ============================================================

	@Override
	public int icon() {
		return BuffIndicator.PREPARATION;
	}

	@Override
	public void tintIcon(Image icon) {
		switch (AttackLevel.getLvl(turnsInvis)) {
			case LVL_1:
				icon.hardlight(0f, 1f, 0f);
				break;
			case LVL_2:
				icon.hardlight(1f, 1f, 0f);
				break;
			case LVL_3:
				icon.hardlight(1f, 0.6f, 0f);
				break;
			case LVL_4:
				icon.hardlight(1f, 0f, 0f);
				break;
		}
	}

	@Override
	public String desc() {
		String desc = Messages.get(this, "desc");

		AttackLevel lvl = AttackLevel.getLvl(turnsInvis);

		desc += "\n\n" + Messages.get(this, "desc_dmg",
				(int) (lvl.baseDmgBonus * 100),
				(int) (lvl.KOThreshold() * 100),
				(int) (lvl.KOThreshold() * 20));

		if (lvl.damageRolls > 1) {
			desc += " " + Messages.get(this, "desc_dmg_likely");
		}

		if (lvl.blinkDistance() > 0) {
			desc += "\n\n" + Messages.get(this, "desc_blink", lvl.blinkDistance());
		}

		desc += "\n\n" + Messages.get(this, "desc_invis_time", turnsInvis);

		if (lvl.ordinal() != AttackLevel.values().length - 1) {
			AttackLevel next = AttackLevel.values()[lvl.ordinal() + 1];
			desc += "\n" + Messages.get(this, "desc_invis_next", next.turnsReq);
		}

		return desc;
	}

	// ============================================================
	// SAVE/LOAD
	// ============================================================

	private static final String TURNS = "turnsInvis";

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		turnsInvis = bundle.getInt(TURNS);
		if (AttackLevel.getLvl(turnsInvis).blinkDistance() > 0) {
			ActionIndicator.setAction(this);
		}
	}

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put(TURNS, turnsInvis);
	}
}