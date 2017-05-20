package upmc.master.reseaux.panes;

import java.awt.Dimension;
import java.util.concurrent.TimeUnit;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import upmc.master.reseaux.Camera;
import upmc.master.reseaux.WindowsManagement;

public class ConnectionPane extends BorderPane {
	
	private Camera camera;

	public ConnectionPane(Camera camera) {
		setStyle("-fx-background-color: linear-gradient(from 25% 25% to 100% 100%, #ffffff, #ffff00);");
		this.camera = camera;

		Thread thread = new Thread(() -> {
			waitForConnection();
		});
		thread.start();

		ImageView upmcLogo = new ImageView(new Image(ClassLoader.getSystemResourceAsStream("images/logoUPMC.png")));
		Label label = new Label("Please connect your device to "+camera.getName());
		ProgressIndicator pi = new ProgressIndicator(-1);

		BorderPane bp = new BorderPane();
		bp.setLeft(label);
		BorderPane.setAlignment(label, Pos.CENTER_LEFT);
		bp.setRight(pi);
		
		Button retour = new Button("Return",
				new ImageView(new Image(ClassLoader.getSystemResourceAsStream("images/return.png"))));
		retour.setOnAction((event) -> {
			WindowsManagement.closeAndOpenNewStage(this, WindowsManagement.WINDOW_TITLE,
					new Scene(new SelectionPane(), 710, 360));
		});
		retour.setStyle("-fx-background-color: transparent;");

		Button cancel = new Button("Cancel",
				new ImageView(new Image(ClassLoader.getSystemResourceAsStream("images/cancel.png"))));
		cancel.setOnAction((event) -> {
			System.exit(0);
		});
		cancel.setStyle("-fx-background-color: transparent;");
		
		HBox hb = new HBox(10, retour, cancel);
		hb.setAlignment(Pos.CENTER);

		setTop(upmcLogo);
		BorderPane.setAlignment(upmcLogo, Pos.TOP_CENTER);
		setCenter(bp);
		setBottom(hb);
		
		setPadding(new Insets(10));
		
	}

	private void waitForConnection() {
		MediaPlayerPane mpp = new MediaPlayerPane(camera);
		Dimension videoSize = mpp.getVideoSize();
		synchronized (videoSize) {
			try {
				videoSize.wait();
				TimeUnit.SECONDS.sleep(2);
			} catch (InterruptedException e) {
				return;
			}
		}

		Platform.runLater(() -> {
			WindowsManagement.closeAndOpenNewStage(this, WindowsManagement.WINDOW_TITLE,
					new Scene(mpp, videoSize.width, videoSize.height));
		});

	}
}
