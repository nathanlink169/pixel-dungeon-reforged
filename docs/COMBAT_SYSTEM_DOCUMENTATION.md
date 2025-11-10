# Combat System Documentation

## Overview

The combat rework introduces a modular, pipeline-based combat system that replaces the previous combat logic. The new system uses immutable context objects, interface-based extensibility, and a clear phase-based resolution process.

### Key Benefits
- **Separation of Concerns**: Combat logic is centralized.
- **Extensibility**: New combat effects are added by implementing interfaces.
- **Predictability**: Strict phase ordering and priority system.
- **Testability**: Immutable contexts make unit testing straightforward.
- **Maintainability**: Expanding functionality should not mess with any other implementation.

---

## Architecture Philosophy

### Single Responsibility Principle
Each class has one job:
- `AttackContext` - Holds immutable attack state
- `AttackResult` - Holds immutable result data
- `CombatResolver` - Orchestrates combat pipeline
- `CombatModifier` - Interface for extending combat

### Immutability
`AttackContext` and `AttackResult` are immutable. Once created, they cannot be changed.

### Interface Segregation
Instead of one giant `CombatModifier` interface, we have focused interfaces for each phase:
- `AccuracyModifier`
- `EvasionModifier`
- `PreArmorDamageModifier`
- `ArmorModifier`
- `PostArmorDamageModifier`
- `OnHitEffect`
- `OnMissEffect`
- `OnDamageEffect`

Objects only implement the interfaces they need.

### Open/Closed Principle
The combat system is open for extension (add new modifiers) but closed for modification (don't change CombatResolver).

---

## Core Components

### AttackContext

An immutable snapshot of an attack at the moment it begins. Contains all information needed to resolve combat.

```java
public class AttackContext {
    // Participants
    public final Char attacker;
    public final Char defender;
    
    // Attack Properties
    public final AttackType attackType;          // MELEE or RANGED
    public final EnumSet<DamageType> damageType; // SLASHING, FIRE, etc.
    
    // Base Stats (captured at attack creation)
    public final int baseAccuracy;
    public final int baseEvasion;
    public final int baseDamage;
    public final int baseArmour;
    public final int startingAttackerHP;
    public final int startingDefenderHP;
    
    // Flags
    public final boolean isSurpriseAttack;
    public final boolean isMaxDamage;
    
    // Position Data
    public final int attackerPosition;
    public final int defenderPosition;
    public final int distance;
    
    // Visual Callbacks
    public final Runnable playHitSound;
    public final Consumer<Char> showSurpriseVisual;
}
```

#### Creating an AttackContext

Use the Builder pattern:

```java
AttackContext context = new AttackContext.Builder(attacker, defender)
    .attackType(AttackContext.AttackType.MELEE)
    .damageType(EnumSet.of(DamageType.SLASHING))
    .build();
```

The builder automatically captures:
- Base stats from attacker and defender
- Surprise attack status
- Positions and distance
- Visual callbacks

#### Why Immutable?

Once combat begins, the context shouldn't change. If a buff is applied mid-combat, it affects the *next* attack, not the current one.

---

### AttackResult

Immutable result of a combat action.

```java
public class AttackResult {
    public final AttackContext context;  // Reference to the attack that produced this result
    public final ResultType result;      // HIT, MISS, or INVULNERABLE
    public final int damageDealt;        // Actual damage after all calculations
    public final boolean killed;         // Was the defender killed?
}
```

#### Creating Results

Factory methods ensure consistency:

```java
AttackResult.hit(context, damageAmount);
AttackResult.miss(context);
AttackResult.invulnerable(context);
```

---

### DamageType

Enum representing different damage types with helper methods:

```java
public enum DamageType {
    NONE,  // Unblockable damage
    
    // Physical
    BLUDGEONING, PIERCING, SLASHING,
    
    // Energy
    ACID, COLD, ELECTRICITY, EXPLOSIVE, FIRE, POISON, 
    SONIC, WATER, POSITIVE_ENERGY, NEGATIVE_ENERGY, FORCE,
    
    // Non-Combat
    STARVATION, AMULET;
}
```

#### Helper Methods

```java
// Create an EnumSet of damage types
EnumSet<DamageType> types = DamageType.of(DamageType.FIRE, DamageType.SLASHING);

// Check type categories
boolean isPhysical = DamageType.IsDamagePhysical(damageType);
boolean isEnergy = DamageType.IsDamageEnergy(damageType);
```

---

### CombatModifier

Base interface for all combat modifications.

```java
public interface CombatModifier {
    /**
     * @return Priority order (higher = executed earlier)
     */
    int priority();
    
    /**
     * @return true if this modifier should apply to this attack
     */
    boolean appliesTo(AttackContext context);
}
```

All modifiers must implement these two methods. Each modifier type then extends this interface.

#### Priority Constants

```java
CombatModifier.Priority.HIGHEST  // 1000  - Things that absolutely need to run first. Generally used for automatic hits or misses
CombatModifier.Priority.HIGH     // 100   - Things that need to apply before most other things. For example, a flat damage bonus due to the hero having temporary strength would want to apply here, before any multipliers.
CombatModifier.Priority.NORMAL   // 0     - Most items and effects
CombatModifier.Priority.LOW      // -100  - Minor effects
CombatModifier.Priority.LOWEST   // -1000 - Must happen last
```

---

### CombatResolver

The orchestrator. Handles the entire combat pipeline. **You should rarely need to modify this class.**

#### Main Entry Point

```java
AttackResult result = CombatResolver.resolve(context);
```

#### Utility Method

```java
boolean willHit = CombatResolver.checkHit(context);
```

Checks if an attack would hit without resolving the full combat sequence. Useful for AI decision-making.

---

## Combat Pipeline

The combat system processes attacks in strict phases:

### Phase 1: Hit Determination

```
1. Check invulnerability
2. Apply AccuracyModifier(s) to base accuracy
3. Apply EvasionModifier(s) to base evasion
4. Handle infinite accuracy/evasion cases
5. Roll: Random.Float(accuracy) vs Random.Float(evasion)
```

**Output**: HIT, MISS, or INVULNERABLE

If the attack misses or hits an invulnerable target, the pipeline ends here.

### Phase 2: Base Damage

Base damage is captured in the `AttackContext` during creation (from `attacker.damageRoll(context)`).

### Phase 3: Pre-Armor Damage Modifiers

```java
damage = original_damage;
for (PreArmorDamageModifier modifier : modifiers) {
    if (modifier.appliesTo(context)) {
        damage = modifier.modifyPreArmorDamage(context, damage);
    }
}
```

**Purpose**: Modify raw damage before armor is applied. Use this for:
- Vulnerability effects that multiply damage
- Critical hit mechanics
- Damage type conversions

### Phase 4: Armor Application

```java
armor = base_armor;
for (ArmorModifier modifier : modifiers) {
    if (modifier.appliesTo(context)) {
        armor = modifier.modifyArmor(context, armor);
    }
}
damage = Math.max(0, damage - armor);
```

**Purpose**: Modify armor value or damage reduction. Use this for:
- Armor penetration
- Armor degradation
- Conditional armor bonuses

### Phase 5: Post-Armor Damage Modifiers

```java
for (PostArmorDamageModifier modifier : modifiers) {
    if (modifier.appliesTo(context)) {
        damage = modifier.modifyPostArmorDamage(context, damage);
    }
}
```

**Purpose**: Modify damage after armor is applied. Use this for:
- Flat damage reduction (shields)
- Minimum damage guarantees
- Final damage caps

### Phase 6: On-Hit Effects

```java
for (OnHitEffect effect : effects) {
    if (effect.appliesTo(context)) {
        effect.onHit(context, finalDamage);
    }
}
```

**Purpose**: Trigger effects when an attack hits, before damage is applied. Use this for:
- Buff application (poison, burn, etc.)
- Weapon enchantment procs
- Combo counters
- Knockback/displacement effects

**Important**: Damage has NOT been applied to the defender yet. The defender is still at `context.startingDefenderHP`.

### Phase 7: Apply Damage

```java
int damageDealt = context.defender.Damage(damage, context.attacker, context.damageType);
```

The damage is actually applied here. The defender's HP changes.

### Phase 8: Post-Damage Effects

```java
for (OnDamageEffect effect : effects) {
    if (effect.appliesTo(context)) {
        effect.onDamage(context, damageDealt);
    }
}
```

**Purpose**: React to damage that was dealt. Use this for:
- "When you deal damage" triggers
- Kill-related bonuses
- Damage reflection
- Life Steal

The defender is now at their new HP value, and you can check `context.defender.isAlive()`.

---

## Implementing Combat Modifiers

### Step 1: Choose Your Interfaces

Determine which combat phases your effect modifies:

- Affects hit chance? → `AccuracyModifier` or `EvasionModifier`
- Increases damage? → `PreArmorDamageModifier`
- Reduces armor? → `ArmorModifier`
- Shields damage? → `PostArmorDamageModifier`
- Applies a buff on hit? → `OnHitEffect`
- Triggers when missing? → `OnMissEffect`
- Reacts to damage dealt? → `OnDamageEffect`

### Step 2: Implement Required Methods

Every modifier needs:

```java
@Override
public int priority() {
    return CombatModifier.Priority.NORMAL;
}

@Override
public boolean appliesTo(AttackContext context) {
    // Return true if this modifier should affect this attack
    return true;
}
```

### Step 3: Implement Modifier Methods

Each interface has one method to implement.

---

## Examples

### Example 1: Simple Accuracy Buff

A buff that gives +20% accuracy when attacking.

```java
public class AimingBuff extends Buff implements CombatModifier.AccuracyModifier {
    
    @Override
    public int priority() {
        return CombatModifier.Priority.NORMAL;
    }
    
    @Override
    public boolean appliesTo(AttackContext context) {
        // Only apply if we're the attacker
        return context.attacker == target;
    }
    
    @Override
    public float modifyAccuracy(AttackContext context, float currentAccuracy) {
        return currentAccuracy * 1.2f;
    }
}
```

### Example 2: Conditional Damage Boost

A weapon enchantment that deals +50% damage to burning enemies.

```java
public class SmolderingEnchantment extends Weapon.Enchantment 
        implements CombatModifier.PreArmorDamageModifier {
    
    @Override
    public int priority() {
        return CombatModifier.Priority.NORMAL;
    }
    
    @Override
    public boolean appliesTo(AttackContext context) {
        // Only apply if defender is burning
        return context.defender.buff(Burning.class) != null;
    }
    
    @Override
    public int modifyPreArmorDamage(AttackContext context, int currentDamage) {
        return Math.round(currentDamage * 1.5f);
    }
}
```

### Example 3: Armor Penetration

An item that reduces enemy armor by 5.

```java
public class ArmorPiercingRing extends Ring 
        implements CombatModifier.ArmorModifier {
    
    @Override
    public int priority() {
        return CombatModifier.Priority.NORMAL;
    }
    
    @Override
    public boolean appliesTo(AttackContext context) {
        // Only apply when hero is attacking
        return context.attacker == Dungeon.hero;
    }
    
    @Override
    public int modifyArmor(AttackContext context, int currentArmor) {
        return Math.max(0, currentArmor - 5);
    }
}
```

### Example 4: Apply Buff on Hit

An enchantment that has a 30% chance to poison on hit.

```java
public class VenomousEnchantment extends Weapon.Enchantment 
        implements CombatModifier.OnHitEffect {
    
    @Override
    public int priority() {
        return CombatModifier.Priority.NORMAL;
    }
    
    @Override
    public boolean appliesTo(AttackContext context) {
        return true;  // Can apply to any attack
    }
    
    @Override
    public void onHit(AttackContext context, int finalDamage) {
        if (Random.Float() < 0.3f) {
            Buff.affect(context.defender, Poison.class)
                .set(finalDamage);
        }
    }
}
```

### Example 5: Multiple Interfaces

A complex buff that affects multiple phases.

```java
public class BerserkBuff extends Buff 
        implements CombatModifier.AccuracyModifier,
                   CombatModifier.PreArmorDamageModifier,
                   CombatModifier.EvasionModifier {
    
    @Override
    public int priority() {
        return CombatModifier.Priority.HIGH;  // Major buff
    }
    
    @Override
    public boolean appliesTo(AttackContext context) {
        return context.attacker == target;  // Only when we attack
    }
    
    @Override
    public float modifyAccuracy(AttackContext context, float currentAccuracy) {
        return currentAccuracy * 0.8f;  // -20% accuracy
    }
    
    @Override
    public int modifyPreArmorDamage(AttackContext context, int currentDamage) {
        return Math.round(currentDamage * 1.5f);  // +50% damage
    }
    
    @Override
    public float modifyEvasion(AttackContext context, float currentEvasion) {
        return currentEvasion * 0.5f;  // -50% evasion
    }
}
```

### Example 6: Priority-Dependent Effect

A talent that must apply before other modifiers.

```java
public class CriticalStrikeTalent implements CombatModifier.PreArmorDamageModifier {
    
    @Override
    public int priority() {
        return CombatModifier.Priority.HIGHEST;  // Apply first!
    }
    
    @Override
    public boolean appliesTo(AttackContext context) {
        // 10% chance on any attack by hero
        return context.attacker == Dungeon.hero && Random.Float() < 0.1f;
    }
    
    @Override
    public int modifyPreArmorDamage(AttackContext context, int currentDamage) {
        // Triple damage on crit
        return currentDamage * 3;
    }
}
```

---


## Testing

### Unit Testing Combat

The immutable context system makes unit testing straightforward.

```java
@Test
public void testCriticalHitTalent() {
    // Create mock characters
    Char attacker = new MockChar(100, 100);  // HP, attack skill
    Char defender = new MockChar(50, 50);    // HP, defense skill
    
    // Add the talent
    attacker.addTalent(new CriticalStrikeTalent());
    
    // Create context
    AttackContext context = new AttackContext.Builder(attacker, defender)
        .attackType(AttackContext.AttackType.MELEE)
        .damageType(EnumSet.of(DamageType.SLASHING))
        .build();
    
    // Verify base damage
    assertEquals(10, context.baseDamage);
    
    // Set up for crit (you'd mock Random for deterministic testing)
    AttackResult result = CombatResolver.resolve(context);
    
    // On crit, damage should be tripled
    assertTrue(result.damageDealt >= 30);  // 3x damage after armor
}
```

### Integration Testing

```java
@Test
public void testMultipleModifierInteraction() {
    Char attacker = Dungeon.hero;
    Char defender = new Rat();
    
    // Apply multiple buffs
    Buff.affect(attacker, StrengthBuff.class);
    Buff.affect(attacker, BerserkBuff.class);
    Buff.affect(defender, VulnerableBuff.class);
    
    AttackContext context = new AttackContext.Builder(attacker, defender)
        .attackType(AttackContext.AttackType.MELEE)
        .build();
    
    AttackResult result = CombatResolver.resolve(context);
    
    // All modifiers should have been applied in priority order
    assertTrue(result.damageDealt > context.baseDamage);
}
```

### Debugging

To debug which modifiers affected a combat action:

```java
// Add logging to CombatResolver (during development)
private static int applyPreArmorModifiers(AttackContext context, int baseDamage) {
    ArrayList<CombatModifier.PreArmorDamageModifier> modifiers = 
        gatherModifiers(context, CombatModifier.PreArmorDamageModifier.class);
    
    int damage = baseDamage;
    for (CombatModifier.PreArmorDamageModifier mod : modifiers) {
        if (mod.appliesTo(context)) {
            int oldDamage = damage;
            damage = mod.modifyPreArmorDamage(context, damage);
            System.out.println(mod.getClass().getSimpleName() + 
                ": " + oldDamage + " -> " + damage);
        }
    }
    
    return damage;
}
```

---

## Best Practices

### Do's

✅ **Use appropriate interfaces** - Only implement interfaces you need

✅ **Check `appliesTo` carefully** - Return false for irrelevant attacks

✅ **Use priority constants** - Don't use magic numbers for priority

✅ **Keep modifiers stateless** - All state should come from the context

✅ **Document your priorities** - If using non-standard priority, explain why

✅ **Test edge cases** - Zero damage, infinite accuracy, killed during hit, etc.

### Don'ts

❌ **Don't modify the context** - It's immutable for a reason

❌ **Don't call `CombatResolver.resolve()` from a modifier** - You're already inside the pipeline

❌ **Don't assume order** - Use priorities, don't rely on modifier collection order

❌ **Don't modify characters directly** - Use the appropriate phase (OnHitEffect, OnDamageEffect)

❌ **Don't implement all interfaces "just in case"** - Only implement what you need

❌ **Don't use side effects in `appliesTo`** - This method may be called multiple times

---

## Advanced Topics

### Modifier Discovery

When `CombatResolver.gatherModifiers()` is called, it searches:

1. Hero's talent manager (global effects)
2. Attacker's buffs
3. Attacker's equipment (weapon, armor, artifact, misc, ring)
4. Weapon enchantments (if attacking with weapon)
5. Armor glyphs
6. Defender's buffs
7. Defender's equipment
8. All of hero's artifacts (global effects)
9. All of hero's trinkets (global effects)

This is **automatic**. You don't need to register modifiers anywhere.

### Infinite Accuracy/Evasion

The constants `Char.INFINITE_ACCURACY` and `Char.INFINITE_EVASION` guarantee hit/miss:

```java
@Override
public float modifyAccuracy(AttackContext context, float currentAccuracy) {
    if (someCondition) {
        return Char.INFINITE_ACCURACY;  // Guaranteed hit
    }
    return currentAccuracy;
}
```

The hit resolution checks for these values and short-circuits the random roll.

### Damage Type Handling

Armor may respond differently to different damage types:

```java
@Override
public int modifyArmor(AttackContext context, int currentArmor) {
    // Reduce armor against fire damage
    if (context.damageType.contains(DamageType.FIRE)) {
        return currentArmor / 2;
    }
    return currentArmor;
}
```

Multiple damage types can be present:

```java
// Fire + slashing
EnumSet.of(DamageType.FIRE, DamageType.SLASHING)
```

### Visual Feedback

The `AttackContext` includes callbacks for visual/audio effects:

```java
context.playHitSound.run();  // Play hit sound
context.showSurpriseVisual.accept(defender);  // Show surprise indicator
```

These are set up automatically by the builder based on the attacker type.

---

## Future Enhancements

### Potential Additions

- **CombatEvent system** - Allow observers to listen to combat events without modifying
- **Damage breakdown** - Track exactly which modifiers contributed what
- **Context pooling** - Reuse context objects to reduce garbage collection
- **Async resolution** - For AI/simulation, resolve many combats in parallel

### Extension Points

The system is designed for easy extension:

- New damage types: Add to `DamageType` enum
- New attack types: Add to `AttackContext.AttackType`
- New modifier phases: Add interface to `CombatModifier`
- New priority levels: Add constants to `CombatModifier.Priority`

---

## Troubleshooting

### "My modifier isn't being called"

1. Check `appliesTo()` - Is it returning true?
2. Check modifier discovery - Is your object in the search path?
3. Check interfaces - Did you implement the right one?
4. Check priority - Is another modifier returning infinite/zero values?

### "Order is wrong"

1. Check priority values - Higher priority runs first
2. Check for implicit assumptions about order
3. Use explicit priorities, not defaults

### "Damage seems wrong"

1. Check which phase you're modifying
2. Remember: Pre-armor → Armor → Post-armor
3. Log modifier chain to see transformations
4. Check for integer overflow with large multipliers

### "Context doesn't have my data"

1. Context is created at attack start - state captured then
2. Don't rely on current HP, use `startingDefenderHP`
3. If you need data, extend `AttackContext` (advanced)

---

## Quick Reference

### Creating an Attack
```java
AttackContext context = new AttackContext.Builder(attacker, defender)
    .attackType(AttackContext.AttackType.MELEE)
    .damageType(DamageType.of(DamageType.SLASHING))
    .build();
AttackResult result = CombatResolver.resolve(context);
```

### Implementing a Modifier
```java
public class MyBuff extends Buff implements CombatModifier.AccuracyModifier {
    @Override
    public int priority() { return CombatModifier.Priority.NORMAL; }
    
    @Override
    public boolean appliesTo(AttackContext context) {
        return context.attacker == target;
    }
    
    @Override
    public float modifyAccuracy(AttackContext context, float currentAccuracy) {
        return currentAccuracy * 1.5f;
    }
}
```

### Common Patterns
```java
// Attacker-only effect
context.attacker == target

// Defender-only effect  
context.defender == target

// Weapon-based effect
context.attacker.getWeapon() == this

// Conditional effect
context.damageType.contains(DamageType.FIRE) && someCondition()

// Hero-only effect
context.attacker == Dungeon.hero
```
