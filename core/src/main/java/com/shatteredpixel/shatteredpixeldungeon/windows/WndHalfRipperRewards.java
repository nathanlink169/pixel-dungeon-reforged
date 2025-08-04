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

package com.shatteredpixel.shatteredpixeldungeon.windows;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.Ghost;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.HalfRipper;
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.Artifact;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.FetidRatSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.GnollTricksterSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.GreatCrabSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.HalfRipperSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.ItemButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;

public class WndHalfRipperRewards extends Window {

    private static final int WIDTH		= 120;
    private static final int BTN_SIZE	= 32;
    private static final int BTN_GAP	= 5;
    private static final int GAP		= 2;

    private Item[] rewards;

    public WndHalfRipperRewards( Item[] rewards ) {

        super();
        this.rewards = rewards;

        final String titleKey = (HalfRipper.Quest.corrupted() ? "escape_title_corrupted" : "escape_title");
        final String descKey = (HalfRipper.Quest.corrupted() ? "escape_desc_corrupted" : "escape_desc");

        IconTitle titlebar = new IconTitle();
        titlebar.icon(new HalfRipperSprite());
        titlebar.label(Messages.get(HalfRipper.class, titleKey));
        RenderedTextBlock message = PixelScene.renderTextBlock(Messages.get(HalfRipper.class, descKey), 6);

        titlebar.setRect( 0, 0, WIDTH, 0 );
        add( titlebar );

        message.maxWidth(WIDTH);
        message.setPos(0, titlebar.bottom() + GAP);
        add( message );

        ItemButton weapon1Btn = new ItemButton() {
            @Override
            protected void onClick() {
                GameScene.show(new RewardWindow(item()));
            }
        };
        weapon1Btn.item( rewards[0] );
        weapon1Btn.setRect( (WIDTH - BTN_GAP) / 2 - BTN_SIZE, message.top() + message.height() + BTN_GAP, BTN_SIZE, BTN_SIZE );
        add( weapon1Btn );

        ItemButton weapon2Btn = new ItemButton(){
            @Override
            protected void onClick() {
                GameScene.show(new RewardWindow(item()));
            }
        };
        weapon2Btn.item( rewards[1] );
        weapon2Btn.setRect( weapon1Btn.right() + BTN_GAP, weapon1Btn.top(), BTN_SIZE, BTN_SIZE );
        add(weapon2Btn);

        ItemButton armourBtn = new ItemButton(){
            @Override
            protected void onClick() {
                GameScene.show(new RewardWindow(item()));
            }
        };
        armourBtn.item( rewards[2] );
        armourBtn.setRect( weapon1Btn.left(), weapon1Btn.bottom() + BTN_GAP, BTN_SIZE, BTN_SIZE );
        add(armourBtn);

        ItemButton artifactBtn = new ItemButton() {
            @Override
            protected void onClick() {
                GameScene.show(new RewardWindow(item()));
            }
        };
        artifactBtn.item( rewards[3] );
        artifactBtn.setRect( armourBtn.right() + BTN_GAP, armourBtn.top(), BTN_SIZE, BTN_SIZE );
        add(artifactBtn);

        ItemButton ringBtn = new ItemButton(){
            @Override
            protected void onClick() {
                GameScene.show(new RewardWindow(item()));
            }
        };
        ringBtn.item( rewards[4] );
        ringBtn.setRect( armourBtn.left(), armourBtn.bottom() + BTN_GAP, BTN_SIZE, BTN_SIZE );
        add(ringBtn);

        ItemButton scrollBtn = new ItemButton() {
            @Override
            protected void onClick() {
                GameScene.show(new RewardWindow(item()));
            }
        };
        scrollBtn.item( rewards[5] );
        scrollBtn.setRect( ringBtn.right() + BTN_GAP, ringBtn.top(), BTN_SIZE, BTN_SIZE );
        add(scrollBtn);

        resize(WIDTH, (int) scrollBtn.bottom() + BTN_GAP);
    }

    private void selectReward( Item reward ) {

        hide();

        if (reward == null) return;

        for (int i = 0; i < rewards.length; ++i) {
            if (reward != rewards[i] && rewards[i] instanceof Artifact) {
                Generator.readdArtifact(((Artifact)(rewards[i])).getClass());
            }
        }

        reward.identify(false);
        if (reward.doPickUp( Dungeon.hero )) {
            GLog.i( Messages.capitalize(Messages.get(Dungeon.hero, "you_now_have", reward.name())) );
        } else {
            Dungeon.level.drop( reward, Dungeon.hero.pos ).sprite.drop();
        }

        GLog.newLine();
        if (HalfRipper.Quest.corrupted()) {
            GLog.n("%s", Messages.get(HalfRipper.class, "farewell_corrupted"));
        } else {
            GLog.n("%s", Messages.get(HalfRipper.class, "farewell"));
        }
    }

    private class RewardWindow extends WndInfoItem {

        public RewardWindow( Item item ) {
            super(item);

            RedButton btnConfirm = new RedButton(Messages.get(WndSadGhost.class, "confirm")){
                @Override
                protected void onClick() {
                    RewardWindow.this.hide();

                    WndHalfRipperRewards.this.selectReward( item );
                }
            };
            btnConfirm.setRect(0, height+2, width/2-1, 16);
            add(btnConfirm);

            RedButton btnCancel = new RedButton(Messages.get(WndSadGhost.class, "cancel")){
                @Override
                protected void onClick() {
                    RewardWindow.this.hide();
                }
            };
            btnCancel.setRect(btnConfirm.right()+2, height+2, btnConfirm.width(), 16);
            add(btnCancel);

            resize(width, (int)btnCancel.bottom());
        }
    }
}