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
import com.shatteredpixel.shatteredpixeldungeon.Statistics;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.ToxicGas;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Burning;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Cripple;
import com.shatteredpixel.shatteredpixeldungeon.combat.AttackContext;
import com.shatteredpixel.shatteredpixeldungeon.combat.CombatModifier;
import com.shatteredpixel.shatteredpixeldungeon.combat.DamageType;
import com.shatteredpixel.shatteredpixeldungeon.effects.FloatingText;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;

import java.util.EnumSet;

public class RotLasher extends Mob implements CombatModifier.OnDamageEffect {

	{
		state = WANDERING = new Waiting();
	}
	@Override
	public Constants.mobs.mobsBase GetConstants() { return Constants.mobs.rotlasher; }

	@Override
	protected boolean act() {
		if (HP < GetMaxHP() && (enemy == null || !Dungeon.level.adjacent(pos, enemy.pos))) {
			sprite.showStatusWithIcon(CharSprite.POSITIVE, Integer.toString(Math.min(5, GetMaxHP() - HP)), FloatingText.HEALING);
			HP = Math.min(GetMaxHP(), HP + 5);
		}
		return super.act();
	}

	@Override
	public int Damage(int dmg, Object src, EnumSet<DamageType> damageType) {
		if (src instanceof Burning || damageType.contains(DamageType.FIRE)) {
			int hp = HP;
			destroy();
			sprite.die();
			return hp;
		} else {
			return super.Damage(dmg, src, damageType);
		}
	}

	@Override
	public boolean Attack(Char enemy, AttackContext.AttackType attackType, EnumSet<DamageType> damageType) {
		if (enemy == Dungeon.hero){
			Statistics.questScores[1] -= 100;
		}
		return super.Attack(enemy, attackType, damageType);
	}

	@Override
	public boolean reset() {
		return true;
	}

	@Override
    public boolean getCloser(int target) {
		return false;
	}

	@Override
	protected boolean getFurther(int target) {
		return false;
	}

	{
		immunities.add( ToxicGas.class );
	}

	@Override
	public void onDamage(AttackContext context, int finalDamage) {
		Buff.affect(enemy, Cripple.class, 2f);
	}

	@Override
	public int priority() {
		return Priority.NORMAL;
	}

	@Override
	public boolean appliesTo(AttackContext context) {
		return context.attacker == this;
	}

	private static class Waiting extends Mob.Wandering {

		@Override
		protected boolean noticeEnemy(Mob mob) {
			mob.spend(TICK);
			return super.noticeEnemy(mob);
		}
	}
}
