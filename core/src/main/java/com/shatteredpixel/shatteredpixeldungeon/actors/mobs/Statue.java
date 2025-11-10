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
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.combat.AttackContext;
import com.shatteredpixel.shatteredpixeldungeon.combat.CombatModifier;
import com.shatteredpixel.shatteredpixeldungeon.combat.DamageType;
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.items.trinkets.RatSkull;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon.Enchantment;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.Grim;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MeleeWeapon;
import com.shatteredpixel.shatteredpixeldungeon.journal.Notes;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.utils.BundleableProperty;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

import java.util.EnumSet;

public class Statue extends Mob implements CombatModifier.OnDamageEffect {
	
	{
		state = PASSIVE;
	}
	@Override
	public Constants.mobs.mobsBase GetConstants() { return Constants.mobs.statue; }

	public boolean levelGenStatue = true;

	@Override
	public int GetMaxHP() {
		return 15 + Dungeon.depth * 5;
	}

	@Override
	public int defenseSkill() {
		return 4 + Dungeon.depth;
	}

	public void createWeapon( boolean useDecks ){
		if (useDecks) {
			m_Weapon.Set((MeleeWeapon) Generator.random(Generator.Category.WEAPON));
		} else {
			m_Weapon.Set((MeleeWeapon) Generator.randomUsingDefaults(Generator.Category.WEAPON));
		}
		levelGenStatue = useDecks;
		m_Weapon.Get().cursed = false;
		m_Weapon.Get().enchant( Enchantment.random() );
	}

	protected BundleableProperty.Object<Weapon> m_Weapon = new BundleableProperty.Object<>("weapon");
	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
		m_Weapon.Store(bundle);
	}
	
	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle( bundle );
		m_Weapon.Restore(bundle);
	}
	
	@Override
	public int damageRoll(AttackContext.AttackType type, boolean isMaxDamage) {
		if (isMaxDamage) return m_Weapon.Get().max();
		return m_Weapon.Get().damageRoll(isMaxDamage, false);
	}

	@Override
	protected EnumSet<DamageType> GetMeleeDamageType() {
		return m_Weapon.Get().damageType;
	}

	@Override
	public int attackSkill() {
		return (int)((9 + Dungeon.depth) * m_Weapon.Get().accuracyFactor( this ));
	}
	
	@Override
	public float attackDelay() {
		return super.attackDelay()*m_Weapon.Get().timeToUse();
	}

	@Override
	protected boolean canAttack(Char enemy) {
		return super.canAttack(enemy) || m_Weapon.Get().canReach(this, enemy.pos);
	}
	
	@Override
	public boolean add(Buff buff) {
		if (super.add(buff)) {
			if (state == PASSIVE && buff.type == Buff.buffType.NEGATIVE) {
				state = HUNTING;
			}
			return true;
		}
		return false;
	}

	@Override
	public int Damage(int dmg, Object src, EnumSet<DamageType> damageType ) {

		if (state == PASSIVE) {
			state = HUNTING;
		}
		
		return super.Damage( dmg, src, damageType );
	}
	
	@Override
	public void beckon( int cell ) {
		if (state != PASSIVE){
			super.beckon(cell);
		}
	}
	
	@Override
	public void die( Object cause ) {
		m_Weapon.Get().identify(false);
		Dungeon.level.drop( m_Weapon.Get(), pos ).sprite.drop();
		super.die( cause );
	}

	@Override
	public Notes.Landmark landmark() {
		return levelGenStatue ? Notes.Landmark.STATUE : null;
	}

	@Override
	public void destroy() {
		if (landmark() != null) {
			Notes.remove( landmark() );
		}
		super.destroy();
	}

	@Override
	public float spawningWeight() {
		return 0f;
	}

	@Override
	public boolean reset() {
		return true;
	}

	@Override
	public String description(boolean forceNoMonsterUnknown) {
		String desc = Messages.get(this, "desc");
		if (m_Weapon.Get() != null){
			desc += "\n\n" + Messages.get(this, "desc_weapon", m_Weapon.Get().name());
		}
		return desc;
	}
	
	{
		resistances.add(Grim.class);
	}

	public static Statue random(){
		return random( true );
	}

	public static Statue random( boolean useDecks ){
		Statue statue;
		float altChance = 1/10f * RatSkull.exoticChanceMultiplier();
		if (altChance > 0.1f) altChance = (altChance+0.1f)/2f; //rat skull is 1/2 as effective here
		if (Random.Float() < altChance){
			statue = new ArmoredStatue();
		} else {
			statue = new Statue();
		}
		statue.createWeapon(useDecks);
		return statue;
	}

	@Override
	public void onDamage(AttackContext context, int damageDealt) {
		if (!context.attacker.isAlive() && enemy == Dungeon.hero){
			Dungeon.fail(this);
			GLog.n( Messages.capitalize(Messages.get(Char.class, "kill", name(false))) );
		}
	}

	@Override
	public int priority() {
		return Priority.NORMAL;
	}

	@Override
	public boolean appliesTo(AttackContext context) {
		return context.attacker == this;
	}

	@Override
	public Weapon getWeapon() {
		return m_Weapon.Get();
	}
}
