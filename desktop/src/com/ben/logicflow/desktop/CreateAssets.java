package com.ben.logicflow.desktop;

import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.tools.texturepacker.TexturePacker;
import com.badlogic.gdx.tools.texturepacker.TexturePacker.Settings;

final class CreateAssets {
	private CreateAssets() {
	}
	public static void main(String[] args) {
		final Settings settings = new Settings();
		settings.pot = false;
		settings.filterMin = TextureFilter.MipMapLinearLinear;
		TexturePacker.process(settings, "C:\\Users\\Ben\\Desktop\\Developer\\Projects\\Logicflow\\core\\assets\\skin", "C:\\Users\\Ben\\Desktop\\Developer\\Projects\\Logicflow\\core\\assets\\skin", "skin");
	}
}