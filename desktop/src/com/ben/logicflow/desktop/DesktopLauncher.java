package com.ben.logicflow.desktop;

import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.ben.logicflow.Application;
import org.lwjgl.Sys;

import java.lang.Thread.UncaughtExceptionHandler;

final class DesktopLauncher {
	private DesktopLauncher() {
	}
	public static void main(String[] args) {
		final LwjglApplicationConfiguration configuration = new LwjglApplicationConfiguration();
		configuration.title = "Logicflow";
		configuration.width = 960;
		configuration.height = 600;
		//configuration.fullscreen = true;
		configuration.addIcon("icon" + Application.getFileSeparator() + "512x512.png", FileType.Internal);
		configuration.addIcon("icon" + Application.getFileSeparator() + "256x256.png", FileType.Internal);
		configuration.addIcon("icon" + Application.getFileSeparator() + "128x128.png", FileType.Internal);
		configuration.addIcon("icon" + Application.getFileSeparator() + "64x64.png", FileType.Internal);
		configuration.addIcon("icon" + Application.getFileSeparator() + "48x48.png", FileType.Internal);
		configuration.addIcon("icon" + Application.getFileSeparator() + "32x32.png", FileType.Internal);
		configuration.addIcon("icon" + Application.getFileSeparator() + "24x24.png", FileType.Internal);
		configuration.addIcon("icon" + Application.getFileSeparator() + "16x16.png", FileType.Internal);
		Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler() {
			@Override
			public void uncaughtException(Thread thread, Throwable exception) {
				Sys.alert("Runtime Error", exception.toString());
			}
		});
		new LwjglApplication(new Application(), configuration);
	}
}