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

package com.shatteredpixel.shatteredpixeldungeon.actors.hero;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.ArtifactRecharge;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.EffectiveShotCooldown;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.GreaterHaste;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MonkEnergy;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Recharging;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.SnipersMark;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.SoulMark;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.cleric.PowerOfMany;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.spells.AuraOfProtection;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.spells.GuidingLight;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.spells.LifeLinkSpell;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.spells.ShieldOfLight;
import com.shatteredpixel.shatteredpixeldungeon.combat.AttackContext;
import com.shatteredpixel.shatteredpixeldungeon.combat.CombatModifier;
import com.shatteredpixel.shatteredpixeldungeon.effects.FloatingText;
import com.shatteredpixel.shatteredpixeldungeon.items.BrokenSeal;
import com.shatteredpixel.shatteredpixeldungeon.items.KindOfWeapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Gun;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.SpiritBow;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Crossbow;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Flail;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.MissileWeapon;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.TargetHealthIndicator;
import com.watabou.utils.Random;

public class TalentManager implements
		CombatModifier.AccuracyModifier,
		CombatModifier.EvasionModifier,
		CombatModifier.PreArmorDamageModifier,
		CombatModifier.PostArmorDamageModifier,
		CombatModifier.OnHitEffect,
		CombatModifier.OnDamageEffect {

	@Override
	public int priority() {
		return CombatModifier.Priority.NORMAL;
	}

	@Override
	public boolean appliesTo(AttackContext context) {
		// Always check - individual talent methods will filter
		return true;
	}

	// ============================================
	// ACCURACY MODIFICATIONS
	// ============================================

	@Override
	public float modifyAccuracy(AttackContext context, float currentAccuracy) {
		float accuracy = currentAccuracy;

		// BLESS (Non-Cleric) - affects allies only
		accuracy = applyBlessAccuracy(context, accuracy);

		// PRECISE_ASSAULT - affects Dungeon.hero only
		if (context.attacker == Dungeon.hero) {
			accuracy = applyPreciseAssault(context, accuracy);
		}

		// LIQUID_AGILITY - affects Dungeon.hero only
		if (context.attacker == Dungeon.hero) {
			accuracy = applyLiquidAgilityAccuracy(context, accuracy);
		}

		// POINT_BLANK - affects Dungeon.hero with ranged weapons
		if (context.attacker == Dungeon.hero) {
			accuracy = applyPointBlank(context, accuracy);
		}

		// EFFECTIVE_SHOT - affects Dungeon.hero ranged attacks
		if (context.attacker == Dungeon.hero) {
			accuracy = applyEffectiveShot(context, accuracy);
		}

		return accuracy;
	}

	// ============================================
	// EVASION MODIFICATIONS
	// ============================================

	@Override
	public float modifyEvasion(AttackContext context, float currentEvasion) {
		float evasion = currentEvasion;

		// BLESS (Non-Cleric) - affects allies only
		evasion = applyBlessEvasion(context, evasion);

		// LIQUID_AGILITY - affects Dungeon.hero only
		if (context.defender == Dungeon.hero) {
			evasion = applyLiquidAgilityEvasion(context, evasion);
		}

		// QUICK_CALIBRATION - affects Dungeon.hero with ArtificerFoodEvasionBonus
		if (context.defender == Dungeon.hero) {
			evasion = applyQuickCalibration(context, evasion);
		}

		return evasion;
	}

	// ============================================
	// PRE-ARMOR DAMAGE MODIFICATIONS
	// ============================================

	@Override
	public int modifyPreArmorDamage(AttackContext context, int currentDamage) {
		int damage = currentDamage;

		// SEARING_LIGHT - bonus damage vs illuminated
		if (context.attacker == Dungeon.hero) {
			damage = applySearingLight(context, damage);
		}

		// WEAPON_RECHARGING - damage bonus when recharging
		if (context.attacker == Dungeon.hero) {
			damage = applyWeaponRecharging(context, damage);
		}

		// BOUNTY_HUNTER - tracking for prep bonus
		if (context.attacker == Dungeon.hero) {
			applyBountyHunter(context);
		}

		// SUCKER_PUNCH - bonus damage on surprise hits
		if (context.attacker == Dungeon.hero) {
			damage = applySuckerPunch(context, damage);
		}

		// FOLLOWUP_STRIKE - bonus damage after ranged weapon hit
		if (context.attacker == Dungeon.hero) {
			damage = applyFollowupStrike(context, damage);
		}

		// VOLATILE_CHAIN - bonus damage after gun hit
		if (context.attacker == Dungeon.hero) {
			damage = applyVolatileChain(context, damage);
		}

		// DEADLY_FOLLOWUP - bonus damage after ranged attack
		if (context.attacker == Dungeon.hero) {
			damage = applyDeadlyFollowup(context, damage);
		}

		return damage;
	}

	// ============================================
	// POST-ARMOR DAMAGE MODIFICATIONS
	// ============================================

	@Override
	public int modifyPostArmorDamage(AttackContext context, int currentDamage) {
		int damage = currentDamage;

		// AURA_OF_PROTECTION - damage reduction for allies near Dungeon.hero
		if (context.defender.alignment == Char.Alignment.ALLY) {
			damage = applyAuraOfProtection(context, damage);
		}

		// LIFE_LINK - damage reduction with life link
		if (context.defender.alignment == Char.Alignment.ALLY) {
			damage = applyLifeLink(context, damage);
		}

		// SHIELD_OF_LIGHT - damage reduction
		if (context.defender == Dungeon.hero || context.defender.alignment == Char.Alignment.ALLY) {
			damage = applyShieldOfLight(context, damage);
		}

		return damage;
	}

	// ============================================
	// ON-HIT EFFECTS
	// ============================================

	@Override
	public void onHit(AttackContext context, int finalDamage) {
		if (context.attacker == Dungeon.hero) {
			checkLethalDefense(context);
			checkLethalHaste(context);
			checkSnipersMark(context);
		}
	}

	// ============================================
	// ON-DAMAGE EFFECTS
	// ============================================

	@Override
	public void onDamage(AttackContext context, int damageDealt) {
		applySoulSiphon(context, damageDealt);
	}

	// ============================================
	// INDIVIDUAL TALENT IMPLEMENTATIONS
	// ============================================

	private float applyBlessAccuracy(AttackContext context, float accuracy) {
		// Only applies if:
		// 1. Dungeon.hero is NOT a Cleric
		// 2. Dungeon.hero has BLESS talent
		// 3. Attacker is an ally (not the Dungeon.hero attacking themselves)
		if (Dungeon.hero.heroClass == HeroClass.CLERIC) return accuracy;
		if (!Dungeon.hero.hasTalent(Talent.BLESS)) return accuracy;
		if (context.attacker == Dungeon.hero) return accuracy;
		if (context.attacker.alignment != Char.Alignment.ALLY) return accuracy;

		// 3%/5% boost depending on talent level
		float multiplier = 1.01f + 0.02f * Dungeon.hero.pointsInTalent(Talent.BLESS);
		return accuracy * multiplier;
	}

	private float applyBlessEvasion(AttackContext context, float evasion) {
		// Only applies if:
		// 1. Dungeon.hero is NOT a Cleric
		// 2. Dungeon.hero has BLESS talent
		// 3. Defender is an ally (not the Dungeon.hero defending themselves)
		if (Dungeon.hero.heroClass == HeroClass.CLERIC) return evasion;
		if (!Dungeon.hero.hasTalent(Talent.BLESS)) return evasion;
		if (context.defender == Dungeon.hero) return evasion;
		if (context.defender.alignment != Char.Alignment.ALLY) return evasion;

		// 3%/5% boost depending on talent level
		float multiplier = 1.01f + 0.02f * Dungeon.hero.pointsInTalent(Talent.BLESS);
		return evasion * multiplier;
	}

	private float applyPreciseAssault(AttackContext context, float accuracy) {
		if (!Dungeon.hero.hasTalent(Talent.PRECISE_ASSAULT)) return accuracy;

		// Check for ability weapon (don't apply to abilities)
		if (Dungeon.hero.belongings.abilityWeapon == Dungeon.hero.belongings.attackingWeapon()) {
			return accuracy;
		}

		// Check for monk ability tracker (don't apply)
		if (Dungeon.hero.buff(MonkEnergy.MonkAbility.UnarmedAbilityTracker.class) != null) {
			return accuracy;
		}

		// Check for special ability states (Flail, Crossbow)
		if (Dungeon.hero.buff(Flail.SpinAbilityTracker.class) != null) return accuracy;
		if (Dungeon.hero.buff(Crossbow.ChargedShot.class) != null) return accuracy;

		// Non-duelist: persistent 10%/20%/30% bonus
		if (Dungeon.hero.heroClass != HeroClass.DUELIST) {
			return accuracy * (1f + 0.1f * Dungeon.hero.pointsInTalent(Talent.PRECISE_ASSAULT));
		}

		// Duelist: check for tracker buff
		if (Dungeon.hero.buff(Talent.PreciseAssaultTracker.class) != null) {
			switch (Dungeon.hero.pointsInTalent(Talent.PRECISE_ASSAULT)) {
				case 1: return accuracy * 2f;
				case 2: return accuracy * 5f;
				case 3: return Char.INFINITE_ACCURACY;
			}
		}

		return accuracy;
	}

	private float applyLiquidAgilityAccuracy(AttackContext context, float accuracy) {
		if (!Dungeon.hero.hasTalent(Talent.LIQUID_AGILITY)) return accuracy;

		Talent.LiquidAgilACCTracker tracker = Dungeon.hero.buff(Talent.LiquidAgilACCTracker.class);
		if (tracker == null) return accuracy;

		// 3x or INFINITE depending on level
		float result = Dungeon.hero.pointsInTalent(Talent.LIQUID_AGILITY) == 2
				? Char.INFINITE_ACCURACY
				: accuracy * 3f;

		// Consume a use
		tracker.uses--;
		if (tracker.uses <= 0) {
			tracker.detach();
		}

		return result;
	}

	private float applyLiquidAgilityEvasion(AttackContext context, float evasion) {
		if (!Dungeon.hero.hasTalent(Talent.LIQUID_AGILITY)) return evasion;

		Talent.LiquidAgilEVATracker tracker = Dungeon.hero.buff(Talent.LiquidAgilEVATracker.class);
		if (tracker == null) return evasion;

		// 3x or INFINITE depending on level
		if (Dungeon.hero.pointsInTalent(Talent.LIQUID_AGILITY) == 1) {
			return evasion * 3f;
		} else if (Dungeon.hero.pointsInTalent(Talent.LIQUID_AGILITY) == 2) {
			return Char.INFINITE_EVASION;
		}

		return evasion;
	}

	private float applyPointBlank(AttackContext context, float accuracy) {
		if (!Dungeon.hero.hasTalent(Talent.POINT_BLANK)) return accuracy;

		KindOfWeapon wep = Dungeon.hero.belongings.attackingWeapon();
		if (!(wep instanceof MissileWeapon)) return accuracy;

		// Only applies at adjacent range
		if (context.distance != 1) return accuracy;

		// 0.5 base + 0.2 per talent point (0.7/0.9/1.1 multiplier)
		return accuracy * (0.5f + 0.2f * Dungeon.hero.pointsInTalent(Talent.POINT_BLANK));
	}

	private float applyEffectiveShot(AttackContext context, float accuracy) {
		if (!Dungeon.hero.hasTalent(Talent.EFFECTIVE_SHOT)) return accuracy;
		if (Dungeon.hero.buff(EffectiveShotCooldown.class) != null) return accuracy;

		KindOfWeapon wep = Dungeon.hero.belongings.attackingWeapon();

		// Guns always get infinite accuracy
		if (wep instanceof Gun.Bullet) {
			return Char.INFINITE_ACCURACY;
		}

		// Missile weapons for non-artificers
		if (wep instanceof MissileWeapon && Dungeon.hero.heroClass != HeroClass.ARTIFICER) {
			return Char.INFINITE_ACCURACY;
		}

		return accuracy;
	}

	private float applyQuickCalibration(AttackContext context, float evasion) {
		if (Dungeon.hero.buff(Talent.ArtificerFoodEvasionBonus.class) == null) return evasion;

		// 100% chance at level 2, 25% chance otherwise
		if (Dungeon.hero.pointsInTalent(Talent.QUICK_CALIBRATION) == 2 || Random.Int(4) == 0) {
			return Char.INFINITE_EVASION;
		}

		return evasion;
	}

	private int applySearingLight(AttackContext context, int damage) {
		if (!Dungeon.hero.hasTalent(Talent.SEARING_LIGHT)) return damage;

		// Only applies if defender has Illuminated buff
		if (context.defender.buff(GuidingLight.Illuminated.class) == null) {
			return damage;
		}

		// Add bonus damage
		return damage + 1 + 2 * Dungeon.hero.pointsInTalent(Talent.SEARING_LIGHT);
	}

	private int applyWeaponRecharging(AttackContext context, int damage) {
		if (Dungeon.hero.heroClass == HeroClass.DUELIST) return damage;
		if (!Dungeon.hero.hasTalent(Talent.WEAPON_RECHARGING)) return damage;

		// Only applies when recharging is active
		if (Dungeon.hero.buff(Recharging.class) == null && Dungeon.hero.buff(ArtifactRecharge.class) == null) {
			return damage;
		}

		// 2.5% base + 2.5% per point
		float multiplier = 1.025f + 0.025f * Dungeon.hero.pointsInTalent(Talent.WEAPON_RECHARGING);
		return Math.round(damage * multiplier);
	}

	private void applyBountyHunter(AttackContext context) {
		if (!Dungeon.hero.hasTalent(Talent.BOUNTY_HUNTER)) return;

		// Just tracking - actual prep bonus is calculated elsewhere
		Buff.affect(Dungeon.hero, Talent.BountyHunterTracker.class, 0f);
	}

	private int applySuckerPunch(AttackContext context, int damage) {
		if (!Dungeon.hero.hasTalent(Talent.SUCKER_PUNCH)) return damage;

		if (context.isSurpriseAttack && context.defender.buff(Talent.SuckerPunchTracker.class) == null) {
			damage += Random.IntRange(Dungeon.hero.pointsInTalent(Talent.SUCKER_PUNCH) , 2);
			Buff.affect(context.defender, Talent.SuckerPunchTracker.class);
		}

		return damage;
	}

	private int applyFollowupStrike(AttackContext context, int damage) {
		if (!Dungeon.hero.hasTalent(Talent.FOLLOWUP_STRIKE)) return damage;

		if (Dungeon.hero.belongings.attackingWeapon() instanceof MissileWeapon) {
			Buff.prolong(Dungeon.hero, Talent.FollowupStrikeTracker.class, 5f).object = context.defender.id();
		} else if (Dungeon.hero.buff(Talent.FollowupStrikeTracker.class) != null
				&& Dungeon.hero.buff(Talent.FollowupStrikeTracker.class).object == context.defender.id()){
			damage += 1 + Dungeon.hero.pointsInTalent(Talent.FOLLOWUP_STRIKE);
			Dungeon.hero.buff(Talent.FollowupStrikeTracker.class).detach();
		}

		return damage;
	}

	private int applyVolatileChain(AttackContext context, int damage) {
		if (!Dungeon.hero.hasTalent(Talent.VOLATILE_CHAIN)) return damage;

		if (Dungeon.hero.buff(Talent.VolatileChainTracker.class) != null
				&& Dungeon.hero.buff(Talent.VolatileChainTracker.class).object == context.defender.id()) {
			damage += 1 + Dungeon.hero.pointsInTalent(Talent.VOLATILE_CHAIN);
			Dungeon.hero.buff(Talent.VolatileChainTracker.class).detach();
		}

		return damage;
	}

	private int applyDeadlyFollowup(AttackContext context, int damage) {
		if (!Dungeon.hero.hasTalent(Talent.VOLATILE_CHAIN)) return damage;

		if (Dungeon.hero.buff(Talent.VolatileChainTracker.class) != null
				&& Dungeon.hero.buff(Talent.VolatileChainTracker.class).object == context.defender.id()) {
			damage += 1 + Dungeon.hero.pointsInTalent(Talent.VOLATILE_CHAIN);
			Dungeon.hero.buff(Talent.VolatileChainTracker.class).detach();
		}

		return damage;
	}

	private int applyAuraOfProtection(AttackContext context, int damage) {
		if (Dungeon.hero.buff(AuraOfProtection.AuraBuff.class) == null) return damage;

		// Check distance (2 tiles or has life link)
		boolean inRange = Dungeon.level.distance(context.defender.pos, Dungeon.hero.pos) <= 2;
		boolean hasLifeLink = context.defender.buff(LifeLinkSpell.LifeLinkSpellBuff.class) != null;

		if (context.defender != Dungeon.hero && !inRange && !hasLifeLink) return damage;

		// 10%/20% damage reduction
		float multiplier = 0.9f - 0.1f * Dungeon.hero.pointsInTalent(Talent.AURA_OF_PROTECTION);
		return Math.round(damage * multiplier);
	}

	private int applyLifeLink(AttackContext context, int damage) {
		if (context.defender.buff(PowerOfMany.PowerBuff.class) == null) return damage;
		if (context.defender.buff(LifeLinkSpell.LifeLinkSpellBuff.class) == null) return damage;

		// 30%/35%/40%/45%/50% damage reduction with life link
		float multiplier = 0.70f - 0.05f * Dungeon.hero.pointsInTalent(Talent.LIFE_LINK);
		return Math.round(damage * multiplier);
	}

	private int applyShieldOfLight(AttackContext context, int damage) {
		// Check for targeted shield
		ShieldOfLight.ShieldOfLightTracker tracker =
				context.defender.buff(ShieldOfLight.ShieldOfLightTracker.class);

		if (tracker != null && tracker.object == context.attacker.id()) {
			int min = 1 + Dungeon.hero.pointsInTalent(Talent.SHIELD_OF_LIGHT);
			int reduction = Random.NormalIntRange(min, 2 * min);
			return Math.max(0, damage - reduction);
		}

		// Check for passive shield (non-Cleric, targeted indicator)
		if (context.defender == Dungeon.hero) return damage;
		if (Dungeon.hero.heroClass == HeroClass.CLERIC) return damage;
		if (!Dungeon.hero.hasTalent(Talent.SHIELD_OF_LIGHT)) return damage;
		if (TargetHealthIndicator.instance.target() != context.attacker) return damage;

		// 33%/50% chance for -1 damage
		if (Random.Int(6) < 1 + Dungeon.hero.pointsInTalent(Talent.SHIELD_OF_LIGHT)) {
			return Math.max(0, damage - 1);
		}

		return damage;
	}

	private void checkLethalDefense(AttackContext context) {
		if (!Dungeon.hero.hasTalent(Talent.LETHAL_DEFENSE)) return;
		if (context.defender.isAlive()) return;

		// Reduce shield cooldown on kill
		BrokenSeal.WarriorShield shield = Dungeon.hero.buff(BrokenSeal.WarriorShield.class);
		if (shield != null) {
			shield.reduceCooldown(Dungeon.hero.pointsInTalent(Talent.LETHAL_DEFENSE) / 3f);
		}
	}

	private void checkLethalHaste(AttackContext context) {
		if (!Dungeon.hero.hasTalent(Talent.LETHAL_HASTE)) return;
		if (context.defender.isAlive()) return;

		if (context.defender.alignment == Char.Alignment.ENEMY) {
			Buff.affect(Dungeon.hero, GreaterHaste.class).set(2 + 2*Dungeon.hero.pointsInTalent(Talent.LETHAL_HASTE));
		}
	}

	private void checkSnipersMark(AttackContext context) {
		if (Dungeon.hero.subClass == HeroSubClass.SNIPER) {
			Weapon wep = Dungeon.hero.belongings.attackingWeapon();
			if (wep instanceof MissileWeapon && !(wep instanceof SpiritBow.SpiritArrow)){
					Actor.add(new Actor() {

						{
							actPriority = VFX_PRIO;
						}

						@Override
						protected boolean act() {
							if (context.defender.isAlive()) {
								if (Dungeon.hero.hasTalent(Talent.SHARED_UPGRADES)){
									int levelBonus = Math.min( 2*Dungeon.hero.pointsInTalent(Talent.SHARED_UPGRADES), wep.buffedLvl() );
									// bonus dmg is 16.67% x weapon level, max of 2/4/6
									float bonusDmg = levelBonus/6f;
									Buff.prolong(Dungeon.hero, SnipersMark.class, SnipersMark.DURATION + levelBonus).set(context.defender.id(), bonusDmg);
								} else {
									Buff.prolong(Dungeon.hero, SnipersMark.class, SnipersMark.DURATION).set(context.defender.id(), 0);
								}
							}
							Actor.remove(this);
							return true;
						}
					});
				}
		}
	}

	private void applySoulSiphon(AttackContext context, int finalDamage) {
		if (Dungeon.hero.subClass != HeroSubClass.WARLOCK) return;

		// Check if the defender has SoulMark
		if (context.defender.buff(SoulMark.class) == null) return;

		// Heal based on damage dealt: 50% or 100% based on talent level
		int healing = Math.round(finalDamage * 0.5f * Dungeon.hero.pointsInTalent(Talent.SOUL_SIPHON));

		if (healing > 0 && Dungeon.hero.HP < Dungeon.hero.GetMaxHP()) {
			if (context.attacker == Dungeon.hero) {
				Dungeon.hero.HP = Math.min(Dungeon.hero.HP + healing, Dungeon.hero.GetMaxHP());
				Dungeon.hero.sprite.showStatusWithIcon(CharSprite.POSITIVE, Integer.toString(healing), FloatingText.HEALING);
			} else {
				healing = Math.round(healing * 0.4f * Dungeon.hero.pointsInTalent(Talent.SOUL_SIPHON) / 3.0f);
				Dungeon.hero.HP = Math.min(Dungeon.hero.HP + healing, Dungeon.hero.GetMaxHP());
				Dungeon.hero.sprite.showStatusWithIcon(CharSprite.POSITIVE, Integer.toString(healing), FloatingText.HEALING);
			}
		}
	}
}
