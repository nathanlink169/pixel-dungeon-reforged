package com.shatteredpixel.shatteredpixeldungeon.windows;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.GamesInProgress;
import com.shatteredpixel.shatteredpixeldungeon.SPDSettings;
import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.HeroSelectScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.CheckBox;
import com.shatteredpixel.shatteredpixeldungeon.ui.IconButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.Icons;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.watabou.input.KeyEvent;
import com.watabou.noosa.Camera;

import java.util.ArrayList;

public class WndHeroSelection extends Window {
    private static final int WIDTH		= 120;
    private static final int TTL_HEIGHT = 16;
    private static final int BTN_HEIGHT = 16;
    private static final int GAP        = 1;

    private final ArrayList<CheckBox> boxes;

    private HeroSelectScene m_heroSelectScene = null;

    public WndHeroSelection(HeroSelectScene scene) {

        super();

        m_heroSelectScene = scene;

        RenderedTextBlock title = PixelScene.renderTextBlock( Messages.get(this, "hero_selection"), 12 );
        title.hardlight( TITLE_COLOR );
        title.setPos(
                (WIDTH - title.width()) / 2,
                (TTL_HEIGHT - title.height()) / 2
        );
        PixelScene.align(title);
        add( title );

        boxes = new ArrayList<>();

        for (int i = 0;i <  HeroClass.TOTAL_COUNT; ++i) {
            CreateCheckbox(i);
        }

        resize( WIDTH, (int) (boxes.get(boxes.size() - 1).bottom() + GAP));
    }

    private void CreateCheckbox(int i) {
        HeroClass heroClass = HeroClass.Get(i);

        String className = Messages.get(HeroClass.class,heroClass.name()).toLowerCase();
        className = className.substring(0, 1).toUpperCase() + className.substring(1);
        CheckBox cb = new CheckBox( className ) {
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
        cb.checked( GamesInProgress.selectedClass == heroClass );
        if (boxes.isEmpty()) {
            cb.setRect(0, TTL_HEIGHT, WIDTH - 16, BTN_HEIGHT);
        } else {
            cb.setRect(0, boxes.get(boxes.size() - 1).bottom() + GAP, WIDTH - 16, BTN_HEIGHT);
        }
        add( cb );
        boxes.add( cb );

        IconButton infoButton = new IconButton(Icons.get(Icons.INFO)){
            @Override
            protected void onClick() {
                super.onClick();
                Window w = new WndHeroInfo(heroClass);
                if (GameScene.landscape()) {
                    w.offset(Camera.main.width / 6, 0);
                }
                ShatteredPixelDungeon.scene().addToFront(w);
            }
        };
        infoButton.setRect(cb.right(), cb.top(), 16, BTN_HEIGHT);
        add(infoButton);
    }

    private void handleTap(CheckBox currentCheckbox) {
        for (int i = 0; i < boxes.size(); ++i) {
            if (currentCheckbox != boxes.get(i)) {
                boxes.get(i).checked(false);
            }
        }
    }

    @Override
    public void destroy() {
        for (int i=0; i < boxes.size(); i++) {
            if (boxes.get( i ).checked()) {
                GamesInProgress.selectedClass = HeroClass.Get(i);
                SPDSettings.lastClass(i);
            }
        }

        if (m_heroSelectScene != null) {
            m_heroSelectScene.UpdateAfterWindowClose();
        }

        super.destroy();
    }
}
