package upmc.master.reseaux;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import upmc.master.reseaux.panes.SelectionPane;

/**
 * Classe qui permet de lancer l'application.
 */
public class AppliJMP extends Application {

	@Override
	public void start(Stage primaryStage) {
		primaryStage.setTitle(WindowsManagement.WINDOW_TITLE);
		primaryStage.getIcons().add(WindowsManagement.ICON);
		primaryStage.initStyle(StageStyle.UNDECORATED);

		
		Scene scene = new Scene(new SelectionPane(), 710, 360);
		primaryStage.setScene(scene);
		primaryStage.show();
		

	}

	public static void main(String[] args) {
		launch(args);
	}
}
