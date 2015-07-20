package com.ben.logicflow;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

public final class Assets {
	//The skin of the application's UI.
	private static final Skin SKIN = new Skin(new TextureAtlas(Gdx.files.internal("skin" + Application.getFileSeparator() + "skin.atlas")));
	private Assets() {
	}
	static void initialise() {
		/*
		 * Rather than save multiple images of the same font with different sizes, fonts are generated during runtime from a TrueType font
		 * file.
		 */
		final FreeTypeFontGenerator fontGenerator = new FreeTypeFontGenerator(Gdx.files.internal("skin" + Application.getFileSeparator() + "calibri-light.ttf"));
		final FreeTypeFontParameter fontParameter = new FreeTypeFontParameter();
		fontParameter.size = 16;
		//Enable the use of mipmap filters.
		fontParameter.genMipMaps = true;
		//Blur assets when zooming out so they don't look pixelated.
		fontParameter.minFilter = TextureFilter.MipMapLinearLinear;
		//As these fonts are referenced in skin.json, they need to be added to the skin before the JSON file is loaded.
		SKIN.add("default-font", fontGenerator.generateFont(fontParameter), BitmapFont.class);
		fontParameter.size = 60;
		fontParameter.borderWidth = 0.75f;
		SKIN.add("title-font", fontGenerator.generateFont(fontParameter), BitmapFont.class);
		fontParameter.size = 30;
		fontParameter.borderWidth = 0.5f;
		SKIN.add("sub-title-font", fontGenerator.generateFont(fontParameter), BitmapFont.class);
		fontParameter.size = 20;
		fontParameter.borderWidth = 0;
		SKIN.add("small-sub-title-font", fontGenerator.generateFont(fontParameter), BitmapFont.class);
		fontGenerator.dispose();
		SKIN.load(Gdx.files.internal("skin" + Application.getFileSeparator() + "skin.json"));
	}
	static void dispose() {
		SKIN.dispose();
	}
	public static Skin getSkin() {
		return SKIN;
	}
}