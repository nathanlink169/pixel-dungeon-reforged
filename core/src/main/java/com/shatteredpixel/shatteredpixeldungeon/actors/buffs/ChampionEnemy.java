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

import com.shatteredpixel.shatteredpixeldungeon.Challenges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Blob;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Fire;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.duelist.ElementalStrike;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.mage.ElementalBlast;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.mage.WarpBeacon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.spells.GuidingLight;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.spells.HolyLance;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.spells.HolyWeapon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.spells.Judgement;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.spells.Smite;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.spells.Sunray;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.CrystalWisp;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.DM100;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Eye;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Fiend;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Shaman;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.UnholyPriest;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Warlock;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.YogFist;
import com.shatteredpixel.shatteredpixeldungeon.combat.AttackContext;
import com.shatteredpixel.shatteredpixeldungeon.combat.CombatModifier;
import com.shatteredpixel.shatteredpixeldungeon.items.bombs.ArcaneBomb;
import com.shatteredpixel.shatteredpixeldungeon.items.bombs.HolyBomb;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfDecay;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfRetribution;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfTeleportation;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.exotic.ScrollOfPsionicBlast;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.CursedWand;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfBlastWave;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfDisintegration;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfDisplacement;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfFireblast;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfFrost;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfLightning;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfLivingEarth;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfMagicMissile;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfPrismaticLight;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfTransfusion;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfWarding;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.Blazing;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.Grim;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.Shocking;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.darts.HolyDart;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.DisintegrationTrap;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.GrimTrap;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.watabou.noosa.Image;
import com.watabou.utils.BArray;
import com.watabou.utils.Bundle;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import java.util.HashSet;

public abstract class ChampionEnemy extends Buff {

	{
		type = buffType.POSITIVE;
		revivePersists = true;
	}

	protected int color;
	protected int rays;

	@Override
	public int icon() {
		return BuffIndicator.CORRUPT;
	}

	@Override
	public void tintIcon(Image icon) {
		icon.hardlight(color);
	}

	@Override
	public void fx(boolean on) {
		if (on) target.sprite.aura( color, rays );
		else target.sprite.clearAura();
	}

	public boolean canAttackWithExtraReach( Char enemy ){
		return false;
	}

	{
		immunities.add(AllyBuff.class);
	}

	public static void rollForChampion(Mob m){
		if (Dungeon.mobsToChampion <= 0) Dungeon.mobsToChampion = 8;

		Dungeon.mobsToChampion--;

		//we roll for a champion enemy even if we aren't spawning one to ensure that
		//mobsToChampion does not affect levelgen RNG (number of calls to Random.Int() is constant)
		Class<?extends ChampionEnemy> buffCls;
		switch (Random.Int(6)){
			case 0: default:    buffCls = Blazing.class;      break;
			case 1:             buffCls = Projecting.class;   break;
			case 2:             buffCls = AntiMagic.class;    break;
			case 3:             buffCls = Giant.class;        break;
			case 4:             buffCls = Blessed.class;      break;
			case 5:             buffCls = Growing.class;      break;
		}

		if (Dungeon.mobsToChampion <= 0 && Dungeon.isChallenged(Challenges.CHAMPION_ENEMIES)) {
			Buff.affect(m, buffCls);
			if (m.state != m.PASSIVE) {
				m.state = m.WANDERING;
			}
		}
	}

	public static class Blazing extends ChampionEnemy implements CombatModifier.PreArmorDamageModifier, CombatModifier.OnHitEffect {

		{
			color = 0xFF8800;
			rays = 4;
		}

		@Override
		public void detach() {
			//don't trigger when killed by being knocked into a pit
			if (target.flying || !Dungeon.level.pit[target.pos]) {
				for (int i : PathFinder.NEIGHBOURS9) {
					if (!Dungeon.level.solid[target.pos + i] && !Dungeon.level.water[target.pos + i]) {
						GameScene.add(Blob.seed(target.pos + i, 2, Fire.class));
					}
				}
			}
			super.detach();
		}

		{
			immunities.add(Burning.class);
		}

		@Override
		public int modifyPreArmorDamage(AttackContext context, int currentDamage) {
			return (int) (currentDamage * (context.attacker == target ? 1.25f : 1.0f));
		}

		@Override
		public int priority() {
			return Priority.NORMAL;
		}

		@Override
		public boolean appliesTo(AttackContext context) {
			return context.attacker == target;
		}

		@Override
		public void onHit(AttackContext context, int finalDamage) {
			if (!Dungeon.level.water[context.defenderPosition]) {
				Buff.affect(context.defender, Burning.class).reignite(context.defender);
			}
		}
	}

	public static class Projecting extends ChampionEnemy implements CombatModifier.PreArmorDamageModifier {

		{
			color = 0x8800FF;
			rays = 4;
		}

		@Override
		public boolean canAttackWithExtraReach(Char enemy) {
			if (Dungeon.level.distance( target.pos, enemy.pos ) > 4){
				return false;
			} else {
				boolean[] passable = BArray.not(Dungeon.level.solid, null);
				for (Char ch : Actor.chars()) {
					//our own tile is always passable
					passable[ch.pos] = ch == target;
				}

				PathFinder.buildDistanceMap(enemy.pos, passable, 4);

				return PathFinder.distance[target.pos] <= 4;
			}
		}

		@Override
		public int modifyPreArmorDamage(AttackContext context, int currentDamage) {
			return (int) (currentDamage * (context.attacker == target ? 1.25f : 1.0f));
		}

		@Override
		public int priority() {
			return Priority.NORMAL;
		}

		@Override
		public boolean appliesTo(AttackContext context) {
			return context.attacker == target;
		}
	}

	public static class AntiMagic extends ChampionEnemy implements CombatModifier.PostArmorDamageModifier {

		{
			color = 0x00FF00;
			rays = 5;
		}


		public static final HashSet<Class> RESISTS = new HashSet<>();
		static {
			RESISTS.add( MagicalSleep.class );
			RESISTS.add( Charm.class );
			RESISTS.add( Weakness.class );
			RESISTS.add( Vulnerable.class );
			RESISTS.add( Hex.class );
			RESISTS.add( Degrade.class );

			RESISTS.add( DisintegrationTrap.class );
			RESISTS.add( GrimTrap.class );

			RESISTS.add( ArcaneBomb.class );
			RESISTS.add( HolyBomb.HolyDamage.class );
			RESISTS.add( ScrollOfRetribution.class );
			RESISTS.add( ScrollOfPsionicBlast.class );
			RESISTS.add( ScrollOfTeleportation.class );
			RESISTS.add( ScrollOfDecay.class );
			RESISTS.add( HolyDart.class );

			RESISTS.add( GuidingLight.class );
			RESISTS.add( HolyWeapon.class );
			RESISTS.add( Sunray.class );
			RESISTS.add( HolyLance.class );
			RESISTS.add( Smite.class );
			RESISTS.add( Judgement.class );

			RESISTS.add( ElementalBlast.class );
			RESISTS.add( CursedWand.class );
			RESISTS.add( WandOfBlastWave.class );
			RESISTS.add( WandOfDisintegration.class );
			RESISTS.add( WandOfFireblast.class );
			RESISTS.add( WandOfFrost.class );
			RESISTS.add( WandOfLightning.class );
			RESISTS.add( WandOfLivingEarth.class );
			RESISTS.add( WandOfMagicMissile.class );
			RESISTS.add( WandOfPrismaticLight.class );
			RESISTS.add( WandOfTransfusion.class );
			RESISTS.add( WandOfWarding.Ward.class );
			RESISTS.add( WandOfDisplacement.class );

			RESISTS.add( ElementalStrike.class );
			RESISTS.add( com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.Blazing.class );
			RESISTS.add( WandOfFireblast.FireBlastOnHit.class );
			RESISTS.add( Shocking.class );
			RESISTS.add( WandOfLightning.LightningOnHit.class );
			RESISTS.add( Grim.class );

			RESISTS.add( WarpBeacon.class );

			RESISTS.add( DM100.LightningBolt.class );
			RESISTS.add( Shaman.EarthenBolt.class );
			RESISTS.add( UnholyPriest.class );
			RESISTS.add( CrystalWisp.LightBeam.class );
			RESISTS.add( Warlock.DarkBolt.class );
			RESISTS.add( UnholyPriest.CursedBolt.class );
			RESISTS.add( Eye.DeathGaze.class );
			RESISTS.add( YogFist.BrightFist.LightBeam.class );
			RESISTS.add( YogFist.DarkFist.DarkBolt.class );
			RESISTS.add( Fiend.class );
			RESISTS.add( Fiend.FiendExplosion.class );
		}

		{
			immunities.addAll(RESISTS);
		}

		@Override
		public int modifyPostArmorDamage(AttackContext context, int currentDamage) {
			return (int) (currentDamage * 0.5f);
		}

		@Override
		public int priority() {
			return Priority.NORMAL;
		}

		@Override
		public boolean appliesTo(AttackContext context) {
			return context.defender == target;
		}
	}

	//Also makes target large, see Char.properties()
	public static class Giant extends ChampionEnemy implements CombatModifier.PostArmorDamageModifier {

		{
			color = 0x0088FF;
			rays = 5;
		}

		@Override
		public boolean canAttackWithExtraReach(Char enemy) {
			if (Dungeon.level.distance( target.pos, enemy.pos ) > 2){
				return false;
			} else {
				boolean[] passable = BArray.not(Dungeon.level.solid, null);
				for (Char ch : Actor.chars()) {
					//our own tile is always passable
					passable[ch.pos] = ch == target;
				}

				PathFinder.buildDistanceMap(enemy.pos, passable, 2);

				return PathFinder.distance[target.pos] <= 2;
			}
		}

		@Override
		public int modifyPostArmorDamage(AttackContext context, int currentDamage) {
			return (int) (currentDamage * 0.2f);
		}

		@Override
		public int priority() {
			return Priority.NORMAL;
		}

		@Override
		public boolean appliesTo(AttackContext context) {
			return context.defender == target;
		}
	}

	public static class Blessed extends ChampionEnemy implements CombatModifier.AccuracyModifier, CombatModifier.EvasionModifier {

		{
			color = 0xFFFF00;
			rays = 6;
		}

		@Override
		public float modifyAccuracy(AttackContext context, float currentAccuracy) {
			return currentAccuracy * (context.attacker == target ? 4.0f : 1.0f);
		}

		@Override
		public float modifyEvasion(AttackContext context, float currentEvasion) {
			return currentEvasion * (context.defender == target ? 4.0f : 1.0f);
		}

		@Override
		public int priority() {
			return Priority.NORMAL;
		}

		@Override
		public boolean appliesTo(AttackContext context) {
			return context.attacker == target || context.defender == target;
		}
	}

	public static class Growing extends ChampionEnemy implements CombatModifier.AccuracyModifier, CombatModifier.EvasionModifier, CombatModifier.PreArmorDamageModifier {

		{
			color = 0xFF2222; //a little white helps it stick out from background
			rays = 6;
		}

		private float multiplier = 1.19f;

		@Override
		public boolean act() {
			multiplier += 0.01f;
			spend(4*TICK);
			return true;
		}

		@Override
		public float modifyAccuracy(AttackContext context, float currentAccuracy) {
			return currentAccuracy * (context.attacker == target ? multiplier : 1.0f);
		}

		@Override
		public float modifyEvasion(AttackContext context, float currentEvasion) {
			return currentEvasion * (context.defender == target ? multiplier : 1.0f);
		}

		@Override
		public int priority() {
			return Priority.NORMAL;
		}

		@Override
		public boolean appliesTo(AttackContext context) {
			return context.attacker == target || context.defender == target;
		}

		@Override
		public String desc() {
			return Messages.get(this, "desc", (int)(100*(multiplier-1)), (int)(100*(1 - 1f/multiplier)));
		}

		private static final String MULTIPLIER = "multiplier";

		@Override
		public void storeInBundle(Bundle bundle) {
			super.storeInBundle(bundle);
			bundle.put(MULTIPLIER, multiplier);
		}

		@Override
		public void restoreFromBundle(Bundle bundle) {
			super.restoreFromBundle(bundle);
			multiplier = bundle.getFloat(MULTIPLIER);
		}

		@Override
		public int modifyPreArmorDamage(AttackContext context, int currentDamage) {
			return (int) (currentDamage * (context.attacker == target ? multiplier : 1.0f));
		}
	}

}
