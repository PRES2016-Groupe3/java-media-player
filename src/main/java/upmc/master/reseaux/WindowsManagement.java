package upmc.master.reseaux;

import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * Classe pour gérer les fenêtres.
 */
public class WindowsManagement {

	public static final String WINDOW_TITLE = "Java Media Player";

	/**
	 * Image correspondant à l'icône pour toutes les fenêtres
	 */
	public static final Image ICON = new Image(ClassLoader.getSystemResourceAsStream("images/iconUPMC.png"));

	/**
	 * Les classes utilitaires n'ont pas de contructeurs publiques
	 */
	private WindowsManagement() {

	}

	/**
	 * Ferme la fenêtre où se trouve un node et ouvre une nouvelle fenêtre avec
	 * une nouvelle scene.
	 * 
	 * @param node
	 *            le node pour lequel la fenêtre sera fermée
	 * @param title
	 *            le titre de la fenêtre
	 * @param scene
	 *            la nouvelle scene
	 */
	public static void closeAndOpenNewStage(Node node, String title, Scene scene) {
		node.getScene().getWindow().hide();
		Stage stage = new Stage();
		stage.setTitle(title);
		stage.getIcons().add(ICON);
		stage.initStyle(StageStyle.UNDECORATED);
		stage.setScene(scene);
		stage.show();
	}

}
