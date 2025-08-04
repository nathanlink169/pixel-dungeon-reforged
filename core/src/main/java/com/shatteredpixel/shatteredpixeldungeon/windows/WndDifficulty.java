package com.shatteredpixel.shatteredpixeldungeon.windows;

import com.shatteredpixel.shatteredpixeldungeon.SPDSettings;
import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.CheckBox;
import com.shatteredpixel.shatteredpixeldungeon.ui.IconButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.Icons;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;

import java.util.ArrayList;

public class WndDifficulty extends Window {
    private static final int WIDTH		= 120;
    private static final int TTL_HEIGHT = 16;
    private static final int BTN_HEIGHT = 16;
    private static final int GAP        = 1;

    private final boolean editable;
    private final ArrayList<CheckBox> boxes;

    public WndDifficulty( int currentDifficulty, boolean editable ) {

        super();

        this.editable = editable;

        RenderedTextBlock title = PixelScene.renderTextBlock( "Difficulty", 12 );
        title.hardlight( TITLE_COLOR );
        title.setPos(
                (WIDTH - title.width()) / 2,
                (TTL_HEIGHT - title.height()) / 2
        );
        PixelScene.align(title);
        add( title );

        boxes = new ArrayList<>();

        float pos = TTL_HEIGHT;

        CheckBox easyCb;
        CheckBox mediumCb;
        CheckBox hardCb;
        CheckBox impossibleCb;

        // Easy
        easyCb = new CheckBox( Messages.titleCase("Easy") ) {
            @Override
            protected void onClick() {
                if (checked()) {
                    // If we are currently checked, do not allow us to be unchecked
                    return;
                }
                super.onClick();
                handleTap(this);
            }
        };
        easyCb.checked( currentDifficulty == 1 );
        easyCb.active = editable;
        pos += GAP;
        easyCb.setRect( 0, pos, WIDTH-16, BTN_HEIGHT );
        add( easyCb );
        boxes.add( easyCb );

        IconButton easyInfo = new IconButton(Icons.get(Icons.INFO)){
            @Override
            protected void onClick() {
                super.onClick();
                ShatteredPixelDungeon.scene().add(
                        new WndMessage("An easier experience. Recommended for those just starting out.\n\n- Maximum health is increased\n- Increased chance to hit\n- More scrolls of upgrade\n- Surprise attacks deal significantly higher damage")
                );
            }
        };
        easyInfo.setRect(easyCb.right(), pos, 16, BTN_HEIGHT);
        add(easyInfo);
        pos = easyCb.bottom();
        // End Easy

        // Medium
        mediumCb = new CheckBox( Messages.titleCase("Medium") ) {
            @Override
            protected void onClick() {
                if (checked()) {
                    // If we are currently checked, do not allow us to be unchecked
                    return;
                }
                super.onClick();
                handleTap(this);
            }
        };
        mediumCb.checked( currentDifficulty == 2 );
        mediumCb.active = editable;
        pos += GAP;
        mediumCb.setRect( 0, pos, WIDTH-16, BTN_HEIGHT );
        add( mediumCb );
        boxes.add( mediumCb );

        IconButton mediumInfo = new IconButton(Icons.get(Icons.INFO)){
            @Override
            protected void onClick() {
                super.onClick();
                ShatteredPixelDungeon.scene().add(
                        new WndMessage("The intended experience.\n\nThere are no significant difficulty modifiers for medium difficulty.")
                );
            }
        };
        mediumInfo.setRect(mediumCb.right(), pos, 16, BTN_HEIGHT);
        add(mediumInfo);
        pos = mediumCb.bottom();
        // End Medium

        // Hard
        hardCb = new CheckBox( Messages.titleCase("Hard") ) {
            @Override
            protected void onClick() {
                if (checked()) {
                    // If we are currently checked, do not allow us to be unchecked
                    return;
                }
                super.onClick();
                handleTap(this);
            }
        };
        hardCb.checked( currentDifficulty == 3 );
        hardCb.active = editable;
        pos += GAP;
        hardCb.setRect( 0, pos, WIDTH-16, BTN_HEIGHT );
        add( hardCb );
        boxes.add( hardCb );

        IconButton hardInfo = new IconButton(Icons.get(Icons.INFO)){
            @Override
            protected void onClick() {
                super.onClick();
                ShatteredPixelDungeon.scene().add(
                        new WndMessage("An more challenging experience. You will need your wits about you.\n\n- Maximum health is decreased\n- Enemies have an increased chance to hit")
                );
            }
        };
        hardInfo.setRect(hardCb.right(), pos, 16, BTN_HEIGHT);
        add(hardInfo);
        pos = hardCb.bottom();
        // End Hard

        // Impossible
        impossibleCb = new CheckBox( Messages.titleCase("Impossible") ) {
            @Override
            protected void onClick() {
                if (checked()) {
                    // If we are currently checked, do not allow us to be unchecked
                    return;
                }
                super.onClick();
                handleTap(this);
            }
        };
        impossibleCb.checked( currentDifficulty == 4 );
        impossibleCb.active = editable;
        pos += GAP;
        impossibleCb.setRect( 0, pos, WIDTH-16, BTN_HEIGHT );
        add( impossibleCb );
        boxes.add( impossibleCb );

        IconButton impossibleInfo = new IconButton(Icons.get(Icons.INFO)){
            @Override
            protected void onClick() {
                super.onClick();
                ShatteredPixelDungeon.scene().add(
                        new WndMessage("You will die.\n\n- Maximum health is decreased further\n- Enemies have an increased to hit\n- Less scrolls of upgrade")
                );
            }
        };
        impossibleInfo.setRect(impossibleCb.right(), pos, 16, BTN_HEIGHT);
        add(impossibleInfo);
        pos = impossibleCb.bottom();
        // End Impossible

        resize( WIDTH, (int)pos );
    }

    private void handleTap(CheckBox currentCheckbox) {
        for (int i = 0; i < boxes.size(); ++i) {
            if (currentCheckbox != boxes.get(i)) {
                boxes.get(i).checked(false);
            }
        }
    }

    @Override
    public void onBackPressed() {

        if (editable) {
            for (int i=0; i < boxes.size(); i++) {
                if (boxes.get( i ).checked()) {
                    SPDSettings.difficulty(i + 1);
                }
            }
        }

        super.onBackPressed();
    }
}
