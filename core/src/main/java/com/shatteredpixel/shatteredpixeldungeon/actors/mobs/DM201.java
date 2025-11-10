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
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Blob;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.CorrosiveGas;
import com.shatteredpixel.shatteredpixeldungeon.combat.AttackContext;
import com.shatteredpixel.shatteredpixeldungeon.combat.CombatModifier;
import com.shatteredpixel.shatteredpixeldungeon.items.quest.MetalShard;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.DM201Sprite;
import com.shatteredpixel.shatteredpixeldungeon.utils.BundleableProperty;
import com.watabou.utils.Bundle;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

public class DM201 extends DM200 implements CombatModifier.OnDamageEffect {
	{
		HUNTING = new Hunting();
	}

	@Override
	public Constants.mobs.mobsBase GetConstants() { return Constants.mobs.dm201; }

	private BundleableProperty.Bool m_Threatened = new BundleableProperty.Bool("threatened", false);


	public void onZapComplete(){
		zap();
		next();
	}

	private void zap( ){
		m_Threatened.Set(false);
		spend(TICK);

		GameScene.add(Blob.seed(enemy.pos, 15, CorrosiveGas.class).setStrength(8));
		for (int i : PathFinder.NEIGHBOURS8){
			if (!Dungeon.level.solid[enemy.pos+i]) {
				GameScene.add(Blob.seed(enemy.pos + i, 5, CorrosiveGas.class).setStrength(8));
			}
		}

	}

	@Override
	protected boolean canVent(int target) {
		return false;
	}

	@Override
    public boolean getCloser(int target) {
		return false;
	}

	@Override
	protected boolean getFurther(int target) {
		return false;
	}

	@Override
	public void rollToDropLoot() {
		if (Dungeon.hero.lvl > GetMaxHP() + 2) return;

		super.rollToDropLoot();

		int ofs;
		do {
			ofs = PathFinder.NEIGHBOURS8[Random.Int(8)];
		} while (Dungeon.level.solid[pos + ofs] && !Dungeon.level.passable[pos + ofs]);
		Dungeon.level.drop( new MetalShard(), pos + ofs ).sprite.drop( pos );
	}

	@Override
	public void onDamage(AttackContext context, int damageDealt) {
		if ((!Dungeon.level.adjacent(context.defenderPosition, context.attackerPosition))
				|| enemy == null || !Dungeon.level.adjacent(pos, enemy.pos)){
			m_Threatened.Set(true);
		}
	}

	@Override
	public int priority() {
		return Priority.NORMAL;
	}

	@Override
	public boolean appliesTo(AttackContext context) {
		return context.defender == this;
	}

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		m_Threatened.Store(bundle);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		m_Threatened.Restore(bundle);
	}

	private static class Hunting extends Mob.Hunting {
		@Override
		public boolean act( Mob mob, boolean enemyInFOV, boolean justAlerted ) {
			DM201 dm = (DM201) mob;
			if (dm.m_Threatened.Get() && enemyInFOV){
				if (dm.sprite != null && (dm.sprite.visible || dm.enemy.sprite.visible)) {
					dm.sprite.zap( dm.enemy.pos );
					return false;
				} else {
					dm.zap();
					return true;
				}
			} else {
				return super.act( dm, enemyInFOV, justAlerted );
			}
		}
	}
}
