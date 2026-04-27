package com.badlogic.drop;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;


/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Main implements ApplicationListener {
    Texture backgroundTexture;
    Texture tanziTexture;
    Texture glassTexture;
    Sound glassSound;
    Music backgroundMusic;

    SpriteBatch spriteBatch;
    FitViewport fitViewport;

    Sprite tanziSprite;

    Vector2 touchPos;

    Array<Sprite> glassSprites;

    float dropTimer;

    Rectangle tanziRect;
    Rectangle glassRect;

    BitmapFont font;

    public static int widthFactor = 100;

    public static int score = 0;

    @Override
    public void create() {
        backgroundTexture = new Texture("Assets/Mcs.jpg");
        tanziTexture = new Texture("Assets/Tanzi.png");
        glassTexture = new Texture("Assets/Regenbogenglas.jpg");
        glassSound = Gdx.audio.newSound(Gdx.files.internal("Assets/Glass.mp3"));
        backgroundMusic =  Gdx.audio.newMusic(Gdx.files.internal("Assets/Leben.mp3"));

        spriteBatch = new SpriteBatch();
        fitViewport = new FitViewport(8*widthFactor, 5*widthFactor);

        tanziSprite = new Sprite(tanziTexture);
        tanziSprite.setSize(widthFactor,widthFactor);

        touchPos = new Vector2();

        glassSprites = new Array<>();

        tanziRect = new Rectangle();
        glassRect = new Rectangle();

        backgroundMusic.setLooping(true);
        backgroundMusic.setVolume(1f);

        backgroundMusic.play();
        backgroundMusic.setPosition(31);

        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("Assets/arial.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = widthFactor/2; // Große, scharfe Schrift

        font = generator.generateFont(parameter);
        font.setColor(Color.PURPLE);
        generator.dispose();
    }

    @Override
    public void resize(int width, int height) {
        fitViewport.update(width, height, true);
    }

    @Override
    public void render() {
        input();
        logic();
        draw();
    }

    private void draw() {
        ScreenUtils.clear(Color.BLACK);
        fitViewport.apply();
        spriteBatch.setProjectionMatrix(fitViewport.getCamera().combined);

        float worldWidth = fitViewport.getWorldWidth();
        float worldHeight = fitViewport.getWorldHeight();

        spriteBatch.begin();

        spriteBatch.draw(backgroundTexture,0,0,worldWidth,worldHeight);
        tanziSprite.draw(spriteBatch);
        font.draw(spriteBatch, "Punkte: " + score, widthFactor/5f, (9 * widthFactor) /2f);

        for (Sprite glassSprite : glassSprites) {
            glassSprite.draw(spriteBatch);
        }

        spriteBatch.end();
    }

    private void logic() {
        float worldWidth = fitViewport.getWorldWidth();
        float worldHeight = fitViewport.getWorldHeight();

        float tanziWidth = tanziSprite.getWidth();
        float tanziHeight = tanziSprite.getHeight();

        tanziSprite.setX(MathUtils.clamp(tanziSprite.getX(),0,worldWidth - tanziWidth));

        float delta = Gdx.graphics.getDeltaTime();

        tanziRect.set(tanziSprite.getX(), tanziSprite.getY(), tanziWidth, tanziHeight);

        for (int i = glassSprites.size - 1; i >= 0 ; i--) {
            Sprite glassSprite = glassSprites.get(i);
            float glassWidth = glassSprite.getWidth();
            float glassHeight = glassSprite.getHeight();

            glassSprite.translateY(widthFactor*-4f*delta);
            glassRect.set(glassSprite.getX(),glassSprite.getY(),glassWidth,glassHeight);

            if (glassSprite.getY() < -glassHeight) {
                glassSprites.removeIndex(i);
            } else if (tanziRect.overlaps(glassRect)) {
                glassSprites.removeIndex(i);
                glassSound.play();
                score++;
            }
        }

        dropTimer += delta;
        if (dropTimer > 1f) {
            dropTimer = 0;
            createGlass();
        }

    }

    private void input() {
        float speed = widthFactor*8f;
        float delta = Gdx.graphics.getDeltaTime();

        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            tanziSprite.translateX(speed * delta);
        } else if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            tanziSprite.translateX(-speed * delta);
        }

        if (Gdx.input.isTouched()) {
            touchPos.set(Gdx.input.getX(), Gdx.input.getY());
            fitViewport.unproject(touchPos);
            tanziSprite.setCenterX(touchPos.x);
        }
    }

    private void createGlass() {
        float glassWidth = widthFactor;
        float glassHeight = widthFactor;
        float worldWidth = fitViewport.getWorldWidth();
        float worldHeight = fitViewport.getWorldHeight();

        Sprite glassSprite = new Sprite(glassTexture);
        glassSprite.setSize(glassWidth,glassHeight);

        glassSprite.setX(MathUtils.random(0f,worldWidth - glassWidth));
        glassSprite.setY(worldHeight);
        glassSprites.add(glassSprite);
    }

    @Override
    public void pause() {
        // Invoked when your application is paused.
    }

    @Override
    public void resume() {
        // Invoked when your application is resumed after pause.
    }

    @Override
    public void dispose() {
        font.dispose();
    }
}
