package com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.CorrosiveGas;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.AllyBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.AscensionChallenge;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Burning;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MagicImmune;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.DriedRose;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfRetribution;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.exotic.ScrollOfPsionicBlast;
import com.shatteredpixel.shatteredpixeldungeon.journal.Bestiary;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.CellSelector;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ConstructSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.GhostSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.ActionIndicator;
import com.shatteredpixel.shatteredpixeldungeon.ui.BossHealthBar;
import com.shatteredpixel.shatteredpixeldungeon.ui.HeroIcon;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndQuest;
import com.watabou.noosa.Game;
import com.watabou.noosa.Image;
import com.watabou.noosa.Visual;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Callback;
import com.watabou.utils.Random;

public class ConstructHero extends DirectableAlly implements ActionIndicator.Action {

    {
        spriteClass = ConstructSprite.class;

        flying = false;

        state = HUNTING;

        properties.add(Property.ELECTRIC);
        properties.add(Property.INORGANIC);
    }

    public ConstructHero(){
        super();
        HP = HT;
        ActionIndicator.setAction(this);
    }

    @Override
    protected boolean act() {
        float healthMultiplier = 0.15f;
        switch (Dungeon.hero.pointsInTalent(Talent.CONSTRUCT_HARDENING)) {
            case 1:
                healthMultiplier = 0.25f;
                break;
            case 2:
                healthMultiplier = 0.35f;
                break;
            case 3:
                healthMultiplier = 0.5f;
                break;
        }

        int newHT = (int)(Dungeon.hero.HT * healthMultiplier);
        if (newHT != HT) {
            int diff = newHT - HT;
            HT = newHT;
            HP += diff;
        }
        flying = Dungeon.hero.pointsInTalent(Talent.CONSTRUCT_MOBILITY) == 3;
        if (HP < HT) {
            ++HP;
        }
        return super.act();
    }

    @Override
    public void defendPos(int cell) {
        yell(Messages.get(this, "directed_position"));
        super.defendPos(cell);
    }

    @Override
    public void followHero() {
        yell(Messages.get(this, "directed_follow"));
        super.followHero();
    }

    @Override
    public void targetChar(Char ch) {
        yell(Messages.get(this, "directed_attack"));
        super.targetChar(ch);
    }

    @Override
    public int attackSkill(Char target) {
        //same accuracy as the hero.
        return Dungeon.hero.lvl + 9;
    }

    private int level() {
        return Dungeon.hero == null ? 0 : Dungeon.hero.lvl/5;
    }

    @Override
    public int damageRoll(boolean isMaxDamage) {
        int tier = 2 + Dungeon.hero.pointsInTalent(Talent.CONSTRUCT_LETHALITY);
        int min = tier + level();
        int max = 5*(tier+1) + level()*(tier+1);

        if (isMaxDamage) {
            return max;
        }
        return Random.NormalIntRange(min, max);
    }

    @Override
    public int defenseProc(Char enemy, int damage) {
        UpdateDefenseStat();
        return super.defenseProc(enemy, damage);
    }

    @Override
    public float speed() {
        float speed = super.speed();

        //moves 2 tiles at a time when returning to the hero
        if (state == WANDERING
                && defendingPos == -1
                && Dungeon.level.distance(pos, Dungeon.hero.pos) > 1){
            speed *= 2;
        }
        switch (Dungeon.hero.pointsInTalent(Talent.CONSTRUCT_MOBILITY)) {
            case 1:
                speed *= 1.5f;
                break;
            case 2:
            case 3:
                speed *= 2.0f;
                break;
        }

        return speed;
    }

    @Override
    public int defenseSkill(Char enemy) {
        UpdateDefenseStat();
        return super.defenseSkill(enemy);
    }

    private void UpdateDefenseStat() {
        float defenseMultiplier = 0.15f;
        switch (Dungeon.hero.pointsInTalent(Talent.CONSTRUCT_HARDENING)) {
            case 1:
                defenseMultiplier = 0.25f;
                break;
            case 2:
                defenseMultiplier = 0.35f;
                break;
            case 3:
                defenseMultiplier = 0.5f;
                break;
        }
        this.defenseSkill = (int)(Dungeon.hero.GetPureDefenseSkill() * defenseMultiplier);
    }

    @Override
    public int drRoll() {
        int drRoll = Dungeon.hero.drRoll();
        float defenseMultiplier = 0.15f;
        switch (Dungeon.hero.pointsInTalent(Talent.CONSTRUCT_HARDENING)) {
            case 1:
                defenseMultiplier = 0.25f;
                break;
            case 2:
                defenseMultiplier = 0.35f;
                break;
            case 3:
                defenseMultiplier = 0.5f;
                break;
        }
        return (int)(drRoll * defenseMultiplier);
    }

    @Override
    public void die(Object cause) {
        sayDefeated();
        Dungeon.hero.NotifyConstructDeath();
        super.die(cause);
    }

    public void sayAppeared(){
        if (Dungeon.hero.buff(AscensionChallenge.class) != null){
            yell( Messages.get( this, "dialogue_ascension"));

        } else {
            yell(Messages.get(this, "dialogue_appear"));
        }
        if (ShatteredPixelDungeon.scene() instanceof GameScene) {
            Sample.INSTANCE.play( Assets.Sounds.GHOST );
        }
    }

    public void sayDefeated(){
        yell( Messages.get( this, "defeated_by_enemy"));
        Sample.INSTANCE.play( Assets.Sounds.GHOST );
    }

    {
        immunities.add( CorrosiveGas.class );
        immunities.add( Burning.class );
        immunities.add( ScrollOfRetribution.class );
        immunities.add( ScrollOfPsionicBlast.class );
        immunities.add( AllyBuff.class );
    }

    @Override
    public String actionName() {
        return "";
    }

    @Override
    public Visual primaryVisual() {
        Image ico;
        ico = new HeroIcon(this);
        return ico;
    }

    @Override
    public int indicatorColor() {
        return 0xFF00BB;
    }

    @Override
    public void doAction() {
        GameScene.selectCell(constructDirector);

        ActionIndicator.setAction(this);
    }

    public CellSelector.Listener constructDirector = new CellSelector.Listener(){

        @Override
        public void onSelect(Integer cell) {
            if (cell == null) return;

            Sample.INSTANCE.play( Assets.Sounds.GHOST );

            directTocell(cell);

        }

        @Override
        public String prompt() {
            return  "\"" + Messages.get(ConstructHero.class, "direct_prompt") + "\"";
        }
    };
}