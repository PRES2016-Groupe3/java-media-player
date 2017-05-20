package upmc.master.reseaux.panes;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import upmc.master.reseaux.WindowsManagement;
import upmc.master.reseaux.Camera;

public class SelectionPane extends BorderPane {
	public SelectionPane() {
		setStyle("-fx-background-color: linear-gradient(from 25% 25% to 100% 100%, #ffffff, #ffff00);");
		
		Label label = new Label("Please select your camera");
		label.setStyle("-fx-font-weight: bold;");
		label.setPadding(new Insets(0, 0, 10, 0));
		
		Button arDrone = new Button("",
				new ImageView(new Image(ClassLoader.getSystemResourceAsStream("images/ARDrone.png"))));
		arDrone.setOnAction((event) -> {
			WindowsManagement.closeAndOpenNewStage(this, WindowsManagement.WINDOW_TITLE,
					new Scene(new ConnectionPane(Camera.AR_DRONE), 380, 200));
		});
		arDrone.setStyle("-fx-background-color: transparent;");
		
		Button goPro = new Button("",
				new ImageView(new Image(ClassLoader.getSystemResourceAsStream("images/GoPro.png"))));
		goPro.setOnAction((event) -> {
			WindowsManagement.closeAndOpenNewStage(this, WindowsManagement.WINDOW_TITLE,
					new Scene(new ConnectionPane(Camera.GO_PRO), 380, 200));
		});
		goPro.setStyle("-fx-background-color: transparent;");
		BorderPane bp = new BorderPane();
		bp.setStyle("-fx-border-color: grey transparent transparent transparent;");
		bp.setLeft(arDrone);
		bp.setRight(goPro);
		
		Button exit = new Button("Exit",
				new ImageView(new Image(ClassLoader.getSystemResourceAsStream("images/exit.png"))));
		exit.setOnAction((event) -> {
			System.exit(0);
		});
		exit.setStyle("-fx-background-color: transparent;");
		setTop(label);
		setCenter(bp);
		setBottom(exit);
		setAlignment(exit, Pos.CENTER_RIGHT);
		bp.setPadding(new Insets(20));
		setPadding(new Insets(10));
	}
}
