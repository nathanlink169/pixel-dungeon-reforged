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
import com.shatteredpixel.shatteredpixeldungeon.Constants;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.Statistics;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Paralysis;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.ShieldBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.Blacksmith;
import com.shatteredpixel.shatteredpixeldungeon.combat.AttackContext;
import com.shatteredpixel.shatteredpixeldungeon.combat.DamageType;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.Pushing;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.effects.Splash;
import com.shatteredpixel.shatteredpixeldungeon.effects.TargetedCell;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.quest.DarkGold;
import com.shatteredpixel.shatteredpixeldungeon.items.quest.Pickaxe;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfTeleportation;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfBlastWave;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.GnollGeomancerSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.sprites.MissileSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.BossHealthBar;
import com.shatteredpixel.shatteredpixeldungeon.utils.BundleableProperty;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;
import com.watabou.utils.GameMath;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import java.util.ArrayList;
import java.util.EnumSet;

public class GnollGeomancer extends Mob {

	{
		//acts after other mobs, just like sappers
		actPriority = MOB_PRIO-1;

		SLEEPING = new Sleeping();
		HUNTING = new Hunting();
		state = SLEEPING;
	}
	@Override
	public Constants.mobs.mobsBase GetConstants() { return Constants.mobs.gnollgeomancer; }

	private int sapperID = -1;
	private int[] sapperSpawns = null;

	@Override
	protected boolean act() {
		if (sapperSpawns == null){
			sapperSpawns = new int[3];
			int i = 0;
			for (Mob m : Dungeon.level.mobs){
				if (m instanceof GnollSapper){
					sapperSpawns[i] = ((GnollSapper) m).SpawnPosition();
					i++;
				}
				if (i == 3) break;
			}
		}

		if (m_ThrowingRocksFromPosition.Get() != null){
			boolean attacked = false;
			for (int rock : m_ThrowingRocksFromPosition.Get()) {
				if (rock != -1 && Dungeon.level.map[rock] == Terrain.MINE_BOULDER) {
					attacked = true;
					GnollGeomancer.doRockThrowAttack(this, rock, m_ThrowingRocksToPosition.Get());
				}
			}

			m_ThrowingRocksFromPosition.Set(null);
			m_ThrowingRocksToPosition.Set(-1);

			spend(TICK);
			return !attacked;
		} else {
			return super.act();
		}

	}

	@Override
	public boolean isInvulnerable(Class effect) {
		return super.isInvulnerable(effect)
				|| (buff(RockArmor.class) != null && effect != Pickaxe.class)
				|| hasSapper();
	}

	@Override
	public boolean add(Buff buff) {
		//immune to buffs and debuff (except its own buffs) while sleeping
		if (state == SLEEPING && !(buff instanceof RockArmor || buff instanceof DelayedRockFall)){
			return false;
		} else {
			return super.add(buff);
		}
	}

	@Override
	public boolean reset() {
		return true;
	}

	@Override
	public float spawningWeight() {
		return 0;
	}

	@Override
	public boolean heroShouldInteract() {
		return super.heroShouldInteract() || buff(RockArmor.class) != null;
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
	public boolean interact(Char c) {
		if (c != Dungeon.hero || buff(RockArmor.class) == null) {
			return super.interact(c);
		} else {
			final Pickaxe p = Dungeon.hero.belongings.getItem(Pickaxe.class);

			if (p == null){
				return true;
			}

			Dungeon.hero.sprite.attack(pos, new Callback() {
				@Override
				public void call() {
					//does its own special damage calculation that's only influenced by pickaxe level and augment
					//we pretend the geomancer is the owner here so that properties like hero str or or other equipment do not factor in
					int dmg = p.damageRoll(false, true);

					boolean wasSleeping = state == SLEEPING;

					//ensure we don't do enough damage to break the armor at the start
					if (wasSleeping) dmg = Math.min(dmg, 15);

					dmg = Math.min(dmg, buff(RockArmor.class).shielding());

					Damage(dmg, p, DamageType.of(DamageType.PIERCING));
					sprite.bloodBurstA(Dungeon.hero.sprite.center(), dmg);
					sprite.flash();

					m_Hits.Increment();
					if (m_Hits.Get() == 1){
						GLog.w( Messages.get(GnollGeomancer.this, "warning"));
					} if (m_Hits.Get() == 3){
						GLog.n( Messages.get(GnollGeomancer.this, "alert"));
						wasSleeping = false;
						spend(TICK);
						sprite.idle();

						carveRockAndDash();

						state = HUNTING;
						enemy = Dungeon.hero;
						BossHealthBar.assignBoss(GnollGeomancer.this);

						for (Mob m : Dungeon.level.mobs){
							if (m instanceof GnollGuard){
								m.aggro(Dungeon.hero);
								if (!((GnollGuard) m).hasSapper()){
									m.beckon(pos);
								}
							} else if (m instanceof GnollSapper){
								m.aggro(Dungeon.hero);
							}
						}
					}

					if (wasSleeping) {
						state = SLEEPING;
						alerted = false;
					}

					if (buff(RockArmor.class) == null){
						Splash.around(sprite, 0x555555, 30);
						sprite.idle();
					}

					Sample.INSTANCE.play(Assets.Sounds.MINE, 1f, Random.Float(0.85f, 1.15f));
					Invisibility.dispel(Dungeon.hero);
					Dungeon.hero.spendAndNext(p.timeToUse());
				}
			});

			return false;
		}
	}

	@Override
	public int Damage(int dmg, Object src, EnumSet<DamageType> damageType) {
		int preHP = HP;
		int hpBracket = GetMaxHP() / 3;

		int curbracket = HP / hpBracket;
		if (curbracket == 3) curbracket--; //full HP isn't its own bracket

		inFinalBracket = curbracket == 0;

		super.Damage(dmg, src, damageType);

		m_AbilityCooldown.Subtract((int) (dmg/10f));

		int newBracket =  HP / hpBracket;
		if (newBracket == 3) newBracket--; //full HP isn't its own bracket

		if (newBracket != curbracket) {
			//cannot be hit through multiple brackets at a time
			if (HP <= (curbracket-1)*hpBracket){
				HP = (curbracket-1)*hpBracket + 1;
			}

			BossHealthBar.bleed(newBracket <= 0);

			carveRockAndDash();
			Buff.affect(this, RockArmor.class).setShield(25);
		}

		return HP - preHP;
	}

	private boolean inFinalBracket = false;

	@Override
	public boolean isAlive() {
		//cannot die until final HP bracket, regardless of incoming dmg
		return super.isAlive() || !inFinalBracket;
	}

	public void linkSapper( GnollSapper sapper ){
		this.sapperID = sapper.id();
		if (sprite instanceof GnollGeomancerSprite){
			((GnollGeomancerSprite) sprite).setupArmor();
		}
	}

	public boolean hasSapper(){
		return sapperID != -1
				&& Actor.findById(sapperID) instanceof GnollSapper
				&& ((GnollSapper)Actor.findById(sapperID)).isAlive();
	}

	public void loseSapper(){
		if (sapperID != -1){
			sapperID = -1;
			if (sprite instanceof GnollGeomancerSprite){
				((GnollGeomancerSprite) sprite).loseArmor();
			}
		}
	}

	private void carveRockAndDash(){

		//aim for closest sapper, preferring living ones within 16 tiles
		int closestSapperPos = -1;
		boolean closestisAlive = false;
		for (int i = 0; i < sapperSpawns.length; i++){
			if (sapperSpawns[i] == -1){
				continue;
			}

			if (closestSapperPos == -1) {
				closestSapperPos = sapperSpawns[i];
				for (Mob m : Dungeon.level.mobs){
					if (m instanceof GnollSapper && ((GnollSapper) m).SpawnPosition() == closestSapperPos){
						closestisAlive = true;
						break;
					}
				}
				continue;
			}

			boolean sapperAlive = false;
			for (Mob m : Dungeon.level.mobs){
				if (m instanceof GnollSapper && ((GnollSapper) m).SpawnPosition() == sapperSpawns[i]){
					sapperAlive = true;
					break;
				}
			}

			if ((sapperAlive && !closestisAlive && Dungeon.level.distance(pos, sapperSpawns[i]) <= 16)
					|| Dungeon.level.trueDistance(pos, sapperSpawns[i]) < Dungeon.level.trueDistance(pos, closestSapperPos)) {
				closestSapperPos = sapperSpawns[i];
				closestisAlive = sapperAlive;
			}
		}

		int dashPos = closestSapperPos;

		//can only dash 3 times
		if (dashPos == -1){
			return;
		}

		//if spawn pos is more than 12 tiles away, get as close as possible
		Ballistica path = new Ballistica(pos, dashPos, Ballistica.STOP_TARGET);

		if (path.dist > 12){
			dashPos = path.path.get(12);
		}

		if (Actor.findChar(dashPos) != null || Dungeon.level.traps.get(dashPos) != null){
			ArrayList<Integer> candidates = new ArrayList<>();
			for (int i : PathFinder.NEIGHBOURS8){
				if (Actor.findChar(dashPos+i) == null && Dungeon.level.traps.get(dashPos+i) == null){
					candidates.add(dashPos+i);
				}
			}
			if (!candidates.isEmpty()) {
				dashPos = Random.element(candidates);
			}
		}

		for (int i = 0; i < sapperSpawns.length; i++){
			if (sapperSpawns[i] == closestSapperPos){
				sapperSpawns[i] = -1;
			}
		}

		path = new Ballistica(pos, dashPos, Ballistica.STOP_TARGET);

		ArrayList<Integer> cells = new ArrayList<>(path.subPath(0, path.dist));
		cells.addAll(spreadDiamondAOE(cells));
		cells.addAll(spreadDiamondAOE(cells));
		cells.addAll(spreadDiamondAOE(cells));

		ArrayList<Integer> exteriorCells = spreadDiamondAOE(cells);

		for (int i : cells){
			if (Dungeon.level.map[i] == Terrain.WALL_DECO){
				Dungeon.level.drop(new DarkGold(), i).sprite.drop();
				Dungeon.level.map[i] = Terrain.EMPTY_DECO;
			} else if (Dungeon.level.solid[i]){
				if (Random.Int(3) == 0){
					Dungeon.level.map[i] = Terrain.MINE_BOULDER;
				} else {
					Dungeon.level.map[i] = Terrain.EMPTY_DECO;
				}
			} else if (Dungeon.level.map[i] == Terrain.HIGH_GRASS || Dungeon.level.map[i] == Terrain.FURROWED_GRASS){
				Dungeon.level.map[i] = Terrain.GRASS;
			}
			CellEmitter.get( i - Dungeon.level.width() ).start(Speck.factory(Speck.ROCK), 0.07f, 10);
		}
		for (int i : exteriorCells){
			if (!Dungeon.level.solid[i]
					&& Dungeon.level.map[i] != Terrain.EMPTY_SP
					&& !Dungeon.level.adjacent(i, Dungeon.level.entrance())
					&& Dungeon.level.traps.get(i) == null
					&& Dungeon.level.plants.get(i) == null
					&& Actor.findChar(i) == null){
				Dungeon.level.map[i] = Terrain.MINE_BOULDER;
			}
		}
		if (Dungeon.level.solid[dashPos]){
			Dungeon.level.map[dashPos] = Terrain.EMPTY_DECO;
		}
		//we potentially update a lot of cells, so might as well just reset properties instead of incrementally updating
		Dungeon.level.buildFlagMaps();
		Dungeon.level.cleanWalls();
		GameScene.updateMap();
		GameScene.updateFog();
		Dungeon.observe();

		PixelScene.shake(3, 0.7f);
		Sample.INSTANCE.play(Assets.Sounds.ROCKS);

		int oldpos = pos;
		pos = dashPos;
		spend(TICK);
		m_AbilityCooldown.Set(1);
		Actor.add(new Pushing(this, oldpos, pos));

		if (closestisAlive){
			GnollSapper closest = null;
			for (Mob m : Dungeon.level.mobs){
				if (m instanceof GnollSapper && ((GnollSapper) m).SpawnPosition() == closestSapperPos){
					closest = (GnollSapper) m;
					break;
				}
			}
			if (closest != null){
				Actor guard = closest.getPartner();
				closest.linkPartner(this);
				//moves sapper and its guard toward geomancer if it is too far away
				if (Dungeon.level.distance(closest.pos, dashPos) > 3){
					ArrayList<Integer> candidates = new ArrayList<>();
					for (int i : PathFinder.NEIGHBOURS8){
						if (!Dungeon.level.solid[dashPos+i]
								&& Dungeon.level.traps.get(dashPos+i) == null
								&& Dungeon.level.plants.get(dashPos+i) == null
								&& Actor.findChar(dashPos+i) == null){
							candidates.add(dashPos+i);
						}
					}

					if (!candidates.isEmpty()){
						int newSapperPos = Random.element(candidates);
						ScrollOfTeleportation.appear(closest, newSapperPos);
						closest.SetSpawnPosition(newSapperPos);
						candidates.remove((Integer)newSapperPos);

						if (guard instanceof GnollGuard && !candidates.isEmpty()){
							ScrollOfTeleportation.appear((GnollGuard)guard, Random.element(candidates));
						}

					}
				}
			}
		}
	}

	private ArrayList<Integer> spreadDiamondAOE(ArrayList<Integer> currentCells){
		ArrayList<Integer> spreadCells = new ArrayList<>();
		for (int i : currentCells){
			for (int j : PathFinder.NEIGHBOURS4){
				if (Dungeon.level.insideMap(i+j) && !spreadCells.contains(i+j) && !currentCells.contains(i+j)){
					spreadCells.add(i+j);
				}
			}
		}
		return spreadCells;
	}

	@Override
	public String description(boolean forceNoMonsterUnknown) {
		if (state == SLEEPING){
			return Messages.get(this, "desc_sleeping");
		} else {
			String desc = super.description(forceNoMonsterUnknown);
			if (buff(RockArmor.class) != null){
				if (hasSapper()){
					desc += "\n\n" + Messages.get(this, "desc_armor_sapper");
				} else {
					desc += "\n\n" + Messages.get(this, "desc_armor");
				}
			}
			return desc;
		}
	}

	@Override
	public void die(Object cause) {
		super.die(cause);
		Blacksmith.Quest.beatBoss();
		Sample.INSTANCE.playDelayed(Assets.Sounds.ROCKS, 0.1f);
		PixelScene.shake( 3, 0.7f );

		for (int i = 0; i < Dungeon.level.length(); i++){
			if (Dungeon.level.map[i] == Terrain.MINE_BOULDER && Dungeon.level.trueDistance(i, pos) <= 6){
				Level.set(i, Terrain.EMPTY_DECO);
				GameScene.updateMap(i);
				Splash.at(i, 0x555555, 15);
			}
		}
	}

	@Override
	public void beckon(int cell) {
		if (state == SLEEPING){
			//do nothing
		} else {
			super.beckon(cell);
		}
	}

	private static class Sleeping extends Mob.Sleeping {

		@Override
		protected void awaken(Mob mob, boolean enemyInFOV) {
			//do nothing, has special awakening rules
		}
	}

	private static class Hunting extends Mob.Hunting {

		@Override
		public boolean act(Mob mob, boolean enemyInFOV, boolean justAlerted) {
			if (!enemyInFOV){
				mob.spend(TICK);
				return true;
			} else {
				mob.m_EnemySeen.Set(true);

				//use abilities more frequently on the hero's initial approach or if sapper is alive
				// but only if hero isn't stunned, to prevent stunlocking
				if ((Dungeon.level.distance(mob.pos, mob.enemy.pos) > 2 || ((GnollGeomancer)mob).hasSapper())
						&& mob.buff(RockArmor.class) != null
						&& mob.enemy.buff(Paralysis.class) == null){
					((GnollGeomancer)mob).m_AbilityCooldown.Decrement();
				}

				if (((GnollGeomancer)mob).hasSapper()){
					((GnollSapper)Actor.findById(((GnollGeomancer)mob).sapperID)).aggro(mob.enemy);
				}

				((GnollGeomancer)mob).m_AbilityCooldown.Decrement();
				if (((GnollGeomancer)mob).m_AbilityCooldown.Get() <= 0){

					boolean targetNextToBarricade = false;
					for (int i : PathFinder.NEIGHBOURS8){
						if (Dungeon.level.map[mob.enemy.pos+i] == Terrain.BARRICADE
								|| Dungeon.level.map[mob.enemy.pos+i] == Terrain.ENTRANCE){
							targetNextToBarricade = true;
							break;
						}
					}

					// 50/50 to either throw a rock or do rockfall, but never do rockfall twice
					// unless target is next to a barricade, then always try to throw
					// unless nothing to throw, then always rockfall
					int hpBracket = mob.GetMaxHP() / 3;

					int curbracket = mob.HP / hpBracket;
					if (curbracket == 3) curbracket--; //full HP isn't its own bracket

					Ballistica aim = GnollGeomancer.prepRockThrowAttack(mob.enemy, mob);
					if (aim != null && (targetNextToBarricade || ((GnollGeomancer)mob).m_LastAbilityWasRockfall.Get() || Random.Int(2) == 0)) {

						((GnollGeomancer)mob).m_LastAbilityWasRockfall.Set(false);
						((GnollGeomancer)mob).m_ThrowingRocksFromPosition.Set(new int[]{-1, -1, -1});
						((GnollGeomancer)mob).m_ThrowingRocksToPosition.Set(aim.collisionPos);

						//do up to 3 thrown rock attacks at once, depending on HP
						for (int i = 0; i < 3 - curbracket; i++){
							if (aim == null) break;

							((GnollGeomancer)mob).m_ThrowingRocksFromPosition.Get()[i] = aim.sourcePos;

							Ballistica warnPath = new Ballistica(aim.sourcePos, aim.collisionPos, Ballistica.STOP_SOLID);
							for (int j : warnPath.subPath(0, warnPath.dist)){
								mob.sprite.parent.add(new TargetedCell(j, 0xFF0000));
							}

							aim = GnollGeomancer.prepRockThrowAttack(mob.enemy, mob);
						}

						Dungeon.hero.interrupt();
						((GnollGeomancer)mob).m_AbilityCooldown.Set(Random.NormalIntRange(3, 5));
						mob.spend(GameMath.gate(TICK, (int)Math.ceil(mob.enemy.cooldown()), 3*TICK));
						return true;
					} else if (GnollGeomancer.prepRockFallAttack(mob.enemy, mob, 6-2*curbracket, true)) {
						((GnollGeomancer)mob).m_LastAbilityWasRockfall.Set(true);
						Dungeon.hero.interrupt();
						mob.spend(GameMath.gate(TICK, (int)Math.ceil(mob.enemy.cooldown()), 3*TICK));
						((GnollGeomancer)mob).m_AbilityCooldown.Set(Random.NormalIntRange(3, 5));
						return true;
					}
				}

				//does not perform regular attacks
				mob.spend(TICK);
				return true;
			}
		}

	}

	//*** These methods are public static as their logic is also accessed by gnoll sappers ***

	public static Ballistica prepRockThrowAttack( Char target, Char source ){
		ArrayList<Integer> candidateRocks = new ArrayList<>();

		for (int i = 0; i < Dungeon.level.length(); i++){
			if (source.fieldOfView[i] && Dungeon.level.map[i] == Terrain.MINE_BOULDER){
				if (new Ballistica(i, target.pos, Ballistica.PROJECTILE).collisionPos == target.pos){
					candidateRocks.add(i);
				}
			}
		}

		//ignore rocks already being thrown
		for (Char ch : Actor.chars()){
			if (ch instanceof GnollGeomancer && ((GnollGeomancer) ch).m_ThrowingRocksFromPosition.Get() != null){
				for (int i : ((GnollGeomancer) ch).m_ThrowingRocksFromPosition.Get()){
					candidateRocks.remove((Integer)i);
				}
			} else if (ch instanceof GnollSapper){
				candidateRocks.remove((Integer)((GnollSapper) ch).ThrowingRocksFromPosition());
			}
		}

		if (candidateRocks.isEmpty()){
			return null;
		} else {

			//throw closest rock to enemy
			int throwingFromPos = candidateRocks.get(0);
			for (int i : candidateRocks){
				if (Dungeon.level.trueDistance(i, target.pos) < Dungeon.level.trueDistance(throwingFromPos, target.pos)){
					throwingFromPos = i;
				}
			}
			int throwingToPos = target.pos;

			return new Ballistica(throwingFromPos, throwingToPos, Ballistica.PROJECTILE);

		}
	}

	private static int rocksInFlight = 0;
	private static ArrayList<Char> knockedChars = new ArrayList<>();

	public static void doRockThrowAttack( Char source, int from, int to ){

		Level.set(from, Terrain.EMPTY);
		GameScene.updateMap(from);
		source.sprite.attack(from, new Callback() {
			@Override
			public void call() {
				source.sprite.idle();
				//do nothing
			}
		});

		Ballistica rockPath = new Ballistica(from, to, Ballistica.MAGIC_BOLT);

		Sample.INSTANCE.play(Assets.Sounds.MISS);
		((MissileSprite)source.sprite.parent.recycle( MissileSprite.class )).
				reset( from, rockPath.collisionPos, new GnollGeomancer.Boulder(), new Callback() {
					@Override
					public void call() {
						Splash.at(rockPath.collisionPos, 0x555555, 15);
						Sample.INSTANCE.play(Assets.Sounds.ROCKS);

						Char ch = Actor.findChar(rockPath.collisionPos);
						if (ch == Dungeon.hero){
							PixelScene.shake( 3, 0.7f );
						} else {
							PixelScene.shake(0.5f, 0.5f);
						}

						if (ch != null && !(ch instanceof GnollGeomancer)){
							ch.Damage(Random.NormalIntRange(6, 12), new GnollGeomancer.Boulder(), DamageType.of(DamageType.BLUDGEONING));

							if (ch == Dungeon.hero){
								Statistics.questScores[2] -= 100;
							}

							if (ch.isAlive()){
								Buff.prolong( ch, Paralysis.class, ch instanceof GnollGuard ? 10 : 3 );
							} else if (!ch.isAlive() && ch == Dungeon.hero) {
								Badges.validateDeathFromEnemyMagic();
								Dungeon.fail( source.getClass() );
								GLog.n( Messages.get( GnollGeomancer.class, "rock_kill") );
							}

							if (!knockedChars.contains(ch) && rockPath.path.size() > rockPath.dist+1) {
								Ballistica trajectory = new Ballistica(ch.pos, rockPath.path.get(rockPath.dist + 1), Ballistica.MAGIC_BOLT);
								WandOfBlastWave.throwChar(ch, trajectory, 1, false, false, source);
								knockedChars.add(ch);
							}
						} else if (ch == null) {
							Dungeon.level.pressCell(rockPath.collisionPos);
						}

						rocksInFlight--;
						if (rocksInFlight <= 0) {
							rocksInFlight = 0;
							source.next();
							knockedChars.clear();
						}
					}
				} );
		rocksInFlight++;
	}

	public static class Boulder extends Item {
		{
			image = ItemSpriteSheet.GEO_BOULDER;
		}
	}

	//similar overall logic as DM-300's rock fall attack, but with more parameters
	public static boolean prepRockFallAttack( Char target, Char source, int range, boolean avoidBarricades ){
		final int rockCenter = target.pos;

		int safeCell;
		do {
			safeCell = rockCenter + PathFinder.NEIGHBOURS8[Random.Int(8)];
		} while (safeCell == source.pos
				|| (Dungeon.level.solid[safeCell] && Random.Int(5) != 0)
				|| (Dungeon.level.traps.containsKey(safeCell) && Random.Int(5) != 0));

		ArrayList<Integer> rockCells = new ArrayList<>();

		int start = rockCenter - Dungeon.level.width() * range - range;
		int pos;
		for (int y = 0; y < 1+2*range; y++) {
			pos = start + Dungeon.level.width() * y;
			for (int x = 0; x < 1+2*range; x++) {
				if (!Dungeon.level.insideMap(pos)) {
					pos++;
					continue;
				}
				if (avoidBarricades){
					boolean barricade = false;
					for (int j : PathFinder.NEIGHBOURS9){
						if (Dungeon.level.map[pos+j] == Terrain.BARRICADE
								|| Dungeon.level.map[pos+j] == Terrain.ENTRANCE){
							barricade = true;
						}
					}
					if (barricade){
						pos++;
						continue;
					}
				}
				//add rock cell to pos, if it is not solid, isn't the safecell, and isn't where geomancer is standing
				if (!Dungeon.level.solid[pos]
						&& pos != safeCell
						&& !(Actor.findChar(pos) instanceof GnollGeomancer)
						&& !(source instanceof GnollGeomancer && Actor.findChar(pos) instanceof GnollSapper)
						&& Random.Int(1+Dungeon.level.distance(rockCenter, pos)/2) == 0) {
					rockCells.add(pos);
				}
				pos++;
			}
		}
		for (int i : rockCells){
			source.sprite.parent.add(new TargetedCell(i, 0xFF0000));
		}
		//don't want to overly punish players with slow move or attack speed
		Buff.append(source, GnollRockFall.class, GameMath.gate(TICK, (int)Math.ceil(target.cooldown()), 3*TICK)).setRockPositions(rockCells);

		source.sprite.attack(target.pos, new Callback() {
			@Override
			public void call() {
				//do nothing
				source.sprite.idle();
			}
		});

		return true;
	}

	public static class GnollRockFall extends DelayedRockFall{

		@Override
		public void affectChar(Char ch) {
			ch.Damage(Random.NormalIntRange(6, 12), this, DamageType.of(DamageType.BLUDGEONING));
			if (ch == Dungeon.hero){
				Statistics.questScores[2] -= 100;
			}
			if (ch.isAlive()) {
				Buff.prolong(ch, Paralysis.class, ch instanceof GnollGuard ? 10 : 3);
			} else if (ch == Dungeon.hero){
				Dungeon.fail( target );
				GLog.n( Messages.get( GnollGeomancer.class, "rockfall_kill") );
			}
		}

		@Override
		public void affectCell(int cell) {
			if (Dungeon.level.map[cell] != Terrain.EMPTY_SP
					&& !Dungeon.level.adjacent(cell, Dungeon.level.entrance())
					&& Random.Int(3) == 0) {
				Level.set(cell, Terrain.MINE_BOULDER);
				GameScene.updateMap(cell);
			}
		}

	}

	public static class RockArmor extends ShieldBuff { }

	private BundleableProperty.Int m_Hits = new BundleableProperty.Int("hits", 0);
	private BundleableProperty.Int m_AbilityCooldown = new BundleableProperty.Int("ability_cooldown", Random.NormalIntRange(3, 5));
	private BundleableProperty.Bool m_LastAbilityWasRockfall = new BundleableProperty.Bool("last_ability_was_rockfall", false);
	private BundleableProperty.IntArray m_ThrowingRocksFromPosition = new BundleableProperty.IntArray("rock_from_pos", null);
	private BundleableProperty.Int m_ThrowingRocksToPosition = new BundleableProperty.Int("rock_to_pos", -1);
	private BundleableProperty.Int m_SapperID = new BundleableProperty.Int("sapper_id", -1);
	private BundleableProperty.IntArray m_SapperSpawns = new BundleableProperty.IntArray("sapper_spawns", null);
	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		m_Hits.Store(bundle);
		m_AbilityCooldown.Store(bundle);
		m_LastAbilityWasRockfall.Store(bundle);
		m_ThrowingRocksFromPosition.Store(bundle);
		m_ThrowingRocksToPosition.Store(bundle);
		m_SapperID.Store(bundle);
		m_SapperSpawns.Store(bundle);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		m_Hits.Restore(bundle);
		m_AbilityCooldown.Restore(bundle);
		m_LastAbilityWasRockfall.Restore(bundle);
		m_ThrowingRocksFromPosition.Restore(bundle);
		m_ThrowingRocksToPosition.Restore(bundle);
		m_SapperID.Restore(bundle);
		m_SapperSpawns.Restore(bundle);

		if (m_Hits.Get() >= 3){
			BossHealthBar.assignBoss(this);
		}
	}
}
