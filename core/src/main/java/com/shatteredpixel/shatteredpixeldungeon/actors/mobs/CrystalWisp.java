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

import com.shatteredpixel.shatteredpixeldungeon.Badges;
import com.shatteredpixel.shatteredpixeldungeon.Constants;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.shatteredpixel.shatteredpixeldungeon.combat.AttackContext;
import com.shatteredpixel.shatteredpixeldungeon.combat.AttackResult;
import com.shatteredpixel.shatteredpixeldungeon.combat.CombatResolver;
import com.shatteredpixel.shatteredpixeldungeon.combat.DamageType;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CrystalWispSprite;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.utils.Random;

public class CrystalWisp extends Mob {
	@Override
	public Constants.mobs.mobsBase GetConstants() { return Constants.mobs.crystalwisp; }

	@Override
	public Class<? extends CharSprite> GetSpriteName() {
		if (m_SpriteVariant.Get() == -1) {
			m_SpriteVariant.Set(Random.Int(3));
		}

		switch (m_SpriteVariant.Get()){
			case 0: default:
				return CrystalWispSprite.Blue.class;
			case 1:
				return CrystalWispSprite.Green.class;
			case 2:
				return CrystalWispSprite.Red.class;
		}
	}

	@Override
	public boolean[] modifyPassable(boolean[] passable) {
		for (int i = 0; i < Dungeon.level.length(); i++){
			passable[i] = passable[i] || Dungeon.level.map[i] == Terrain.MINE_CRYSTAL;
		}
		return passable;
	}
	@Override
	protected boolean canAttack( Char enemy ) {
		return super.canAttack(enemy)
				|| new Ballistica( pos, enemy.pos, Ballistica.MAGIC_BOLT).collisionPos == enemy.pos;
	}

	public boolean doAttack(Char enemy) {

		if (Dungeon.level.adjacent( pos, enemy.pos )
				|| new Ballistica( pos, enemy.pos, Ballistica.MAGIC_BOLT).collisionPos != enemy.pos) {

			return super.doAttack( enemy );

		} else {

			if (sprite != null && (sprite.visible || enemy.sprite.visible)) {
				sprite.zap( enemy.pos );
				return false;
			} else {
				zap();
				return true;
			}
		}
	}

	@Override
	public void die(Object cause) {
		flying = false;
		super.die(cause);
	}

	//used so resistances can differentiate between melee and magical attacks
	public static class LightBeam {}

	private void zap() {
		spend( 1f );

		Invisibility.dispel(this);
		Char enemy = this.enemy;
		// Build attack context
		AttackContext context = new AttackContext.Builder(this, enemy)
				.attackType(AttackContext.AttackType.RANGED)
				.damageType(GetDamageType(AttackContext.AttackType.RANGED))
				.build();

		// Resolve attack - this handles EVERYTHING internally
		AttackResult result = CombatResolver.resolve(context);

		// Check result type for UI/feedback
		if (result.result == AttackResult.ResultType.MISS) {
			// Miss feedback is already handled by resolver, but you can add extra
			enemy.sprite.showStatus(CharSprite.NEUTRAL, enemy.defenseVerb());
		}

		// Check if killed hero specifically (for special death message)
		if (result.killed && enemy == Dungeon.hero) {
			Badges.validateDeathFromEnemyMagic();
			Dungeon.fail(this);
			GLog.n(Messages.get(this, "beam_kill"));
		}
	}

	public void onZapComplete() {
		zap();
		next();
	}
}
