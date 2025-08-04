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
import com.shatteredpixel.shatteredpixeldungeon.Badges;
import com.shatteredpixel.shatteredpixeldungeon.Challenges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Amok;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Bleeding;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Charm;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Cripple;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Degrade;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Hex;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MagicalSleep;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Sleep;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Terror;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Vulnerable;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Weakness;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.duelist.ElementalStrike;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.mage.ElementalBlast;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.mage.WarpBeacon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.spells.GuidingLight;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.spells.HolyLance;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.spells.HolyWeapon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.spells.Judgement;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.spells.Smite;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.spells.Sunray;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.spells.WallOfLight;
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
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
import com.shatteredpixel.shatteredpixeldungeon.journal.Document;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.DisintegrationTrap;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.GrimTrap;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.BallistaSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.MissileSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.RatSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.SnakeSprite;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

import java.util.HashSet;

public class Ballista extends Mob {

	private int ammo = ammoCapacity();

	public static final HashSet<Class> RESISTS = new HashSet<>();
	static {
		RESISTS.add( Amok.class );
		RESISTS.add( Terror.class );
		RESISTS.add( Sleep.class );
		RESISTS.add( Bleeding.class );
	}

	{
		HP = HT = 80;
		defenseSkill = 12;

		EXP = 9;
		maxLvl = 17;
		
		loot = Generator.Category.MISSILE;
		lootChance = 0.25f;

		immunities.addAll(RESISTS);
		properties.add(Property.IMMOVABLE);
		properties.add(Property.INORGANIC);
	}
	@Override
	public Class<? extends CharSprite> GetSpriteClass() {

		return BallistaSprite.class;
	}

	@Override
	public int damageRoll(boolean isMaxDamage) {
		if (isMaxDamage) return 40;
		return Random.NormalIntRange( 30, 40 );
	}

	@Override
	public int attackSkill( Char target ) {
		return 36;
	}

	@Override
	public int drRoll() {
		return super.drRoll() + Random.NormalIntRange(0, 16);
	}

	@Override
	protected boolean canAttack( Char enemy ) {
		return !Dungeon.level.adjacent( pos, enemy.pos ) && ammo > 0
				&& (super.canAttack(enemy) || new Ballistica( pos, enemy.pos, Ballistica.PROJECTILE).collisionPos == enemy.pos);
	}

	@Override
	public int attackProc( Char enemy, int damage ) {
		damage = super.attackProc( enemy, damage );
		--ammo;
		if (Random.Int( 4 ) == 0) {
			int oppositeAdjacent = enemy.pos + (enemy.pos - pos);
			Ballistica trajectory = new Ballistica(enemy.pos, oppositeAdjacent, Ballistica.MAGIC_BOLT);
			WandOfBlastWave.throwChar(enemy, trajectory, 2, false, false, this);
		}
		return damage;
	}

	@Override
	protected boolean getCloser( int target ) {
		if (ammo <= 0) {
			reload();
			return true;
		}
		return true;
	}

	protected int ammoCapacity() {
		return 1;
	}
	private void reload() {
		ammo = ammoCapacity();
		if (Dungeon.hero.getVisibleEnemies().contains(this)) {
			Sample.INSTANCE.play(Assets.Sounds.BALLISTA_RELOAD);
			yell(Messages.get(this, "loaded"));
		}
	}

	@Override
	public void aggro(Char ch) {
		//cannot be aggroed to something it can't see
		//skip this check if FOV isn't initialized
		if (ch == null || fieldOfView == null
				|| fieldOfView.length != Dungeon.level.length() || fieldOfView[ch.pos]) {
			super.aggro(ch);
		}
	}

	private static final String AMMO = "ammo";
	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put(AMMO, ammo);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		ammo = bundle.getInt(AMMO);
	}
}
