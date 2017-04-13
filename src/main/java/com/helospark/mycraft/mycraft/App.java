package com.helospark.mycraft.mycraft;

import javax.swing.JOptionPane;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.helospark.mycraft.mycraft.game.Game;
import com.helospark.mycraft.mycraft.singleton.Singleton;

/**
 * Hello world!
 *
 */
public class App {

	Game game = new Game();

	public App(boolean isServer) {
		game.setServer(isServer);
		game.initialize();
	}

	public void start() {
		game.run();
	}

	public static void main(String[] args) {
		ApplicationContext context = new ClassPathXmlApplicationContext(
				new String[] { "springConfig.xml" });

		int dialogResult = JOptionPane.showConfirmDialog(null,
				"Start as server?", "Warning", JOptionPane.YES_NO_OPTION);
		boolean isServer = false;
		if (dialogResult == JOptionPane.YES_OPTION) {
			isServer = true;
		}
		Singleton.getInstance().setApplicationContext(context);
		App app = new App(isServer);
		app.start();
	}
}