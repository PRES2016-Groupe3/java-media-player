package upmc.master.reseaux.panes;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.IntBuffer;

import org.bytedeco.javacpp.avutil;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;

import com.codeminders.ardrone.data.decoder.ardrone10.video.BufferedVideoImage;

import javafx.event.EventHandler;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import sun.awt.image.IntegerComponentRaster;
import upmc.master.reseaux.Camera;

public class MediaPlayerPane extends Pane {

	// AR.Drone 1.0

	private static final int PORT = 5555;
	private static final int BUFFER_SIZE = 100 * 1024;
	private static final byte[] DRONE_IP = { (byte) 192, (byte) 168, (byte) 1, (byte) 1 };
	private static final byte[] TRIGGER_BYTES = { 0x01, 0x00, 0x00, 0x00 };
	private static final int DRONE_TIMEOUT = 100;

	// GoPro

	private static final String GO_PRO_IP = "10.5.5.9";
	private static final int GO_PRO_PORT = 8080;

	private Dimension videoSize;
	private boolean doubleClicked;

	public MediaPlayerPane(Camera camera) {
		setVideoSize(new Dimension());
		setDoubleClicked(false);

		Thread thread = new Thread(() -> {
			switch (camera) {
			case AR_DRONE:
				receiveAndPlayARDrone();
				break;
			case GO_PRO:
				receiveAndPlayGoPro();
				break;
			}

		});
		thread.start();

		setOnMouseClicked(new EventHandler<MouseEvent>() {
			public void handle(MouseEvent mouseEvent) {
				if (mouseEvent.getButton().equals(MouseButton.PRIMARY)) {
					if (mouseEvent.getClickCount() == 2) {
						System.exit(0);
					}
				}
			}
		});

	}

	private void receiveAndPlayARDrone() {

		byte[] buf = new byte[BUFFER_SIZE];
		BufferedVideoImage vi = new BufferedVideoImage();
		InetAddress droneAddr = null;
		ImageView videoImage = null;
		PixelFormat<IntBuffer> argb = PixelFormat.getIntArgbInstance();

		try {
			droneAddr = InetAddress.getByAddress(DRONE_IP);
		} catch (UnknownHostException e1) {
			e1.printStackTrace();
		}
		DatagramPacket in = new DatagramPacket(buf, buf.length, droneAddr, PORT);
		DatagramPacket out = new DatagramPacket(TRIGGER_BYTES, TRIGGER_BYTES.length, droneAddr, PORT);
		try (DatagramSocket socket = new DatagramSocket(PORT);) {
			sendAndWait(socket, in, out);
		} catch (IOException e) {
			e.printStackTrace();
		}

		vi.addImageStream(buf, in.getLength());

		int w = vi.getWidth();
		int h = vi.getHeight();
		WritableImage wi = new WritableImage(w, h);
		PixelWriter pw = wi.getPixelWriter();
		
		pw.setPixels(0, 0, w, h, argb, vi.getJavaPixelData(), 0, w);

		videoImage = new ImageView(wi);

		getChildren().add(videoImage);

		synchronized (getVideoSize()) {
			getVideoSize().setSize(wi.getWidth(), wi.getHeight());
			getVideoSize().notify();
		}

		try (DatagramSocket socket = new DatagramSocket(PORT);) {
			socket.setSoTimeout(DRONE_TIMEOUT);
			while (!Thread.interrupted()) {

				sendAndWait(socket, in, out);
				vi.addImageStream(buf, in.getLength());
				pw.setPixels(0, 0, w, h, argb, vi.getJavaPixelData(), 0, w);
				videoImage.setImage(wi);

			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private void sendAndWait(DatagramSocket socket, DatagramPacket in, DatagramPacket out) {

		try {
			socket.send(out);
			socket.receive(in);
		} catch (SocketTimeoutException e) {
			return;
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private void receiveAndPlayGoPro() {

		avutil.av_log_set_level(avutil.AV_LOG_QUIET);

		ImageView videoImage = null;

		int lastSeq = 0;
		Java2DFrameConverter converter = new Java2DFrameConverter();
		HttpURLConnection hurl = null;
		String line = null;
		FFmpegFrameGrabber grabber;

		try (BufferedReader reader = new BufferedReader(new InputStreamReader(loadPlaylist(hurl)));) {
			while ((line = reader.readLine()).startsWith("#"))
				;
			grabber = new FFmpegFrameGrabber(loadMediaSegment(hurl, line));
			grabber.start();
			synchronized (getVideoSize()) {
				getVideoSize().setSize(grabber.getImageWidth(), grabber.getImageHeight());
				getVideoSize().notify();
			}
			videoImage = new ImageView(toFXImage(converter.getBufferedImage(grabber.grabImage()), null));
			getChildren().add(videoImage);
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		while (!Thread.interrupted()) {
			try {

				try (BufferedReader reader = new BufferedReader(new InputStreamReader(loadPlaylist(hurl)));) {

					int seq = -1;
					int firstSeqment;

					Frame frame;
					while (seq == -1) {
						if ((line = reader.readLine()).startsWith("#EXT-X-MEDIA-SEQUENCE:")) {
							seq = Integer.parseInt(line.replaceAll("[^0-9]", ""));

						}

					}
					firstSeqment = lastSeq + 1 - seq;
					lastSeq = seq + 7;
					while (firstSeqment > 0) {
						if (((line = reader.readLine()) != null) && !(line.startsWith("#"))) {
							firstSeqment--;

						}
					}
					while ((line = reader.readLine()) != null) {
						if (!line.startsWith("#")) {

							grabber = new FFmpegFrameGrabber(loadMediaSegment(hurl, line));
							grabber.start();

							while ((frame = grabber.grabImage()) != null) {

								videoImage.setImage(toFXImage(converter.getBufferedImage(frame), null));

							}
							grabber.close();
						}
					}

				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	private InputStream loadPlaylist(HttpURLConnection hurl) throws IOException {
		hurl = (HttpURLConnection) new URL("http://" + GO_PRO_IP + ":" + GO_PRO_PORT + "/live/amba.m3u8")
				.openConnection();
		return hurl.getInputStream();
	}

	private InputStream loadMediaSegment(HttpURLConnection hurl, String line) throws IOException {
		hurl = (HttpURLConnection) new URL("http://" + GO_PRO_IP + ":" + GO_PRO_PORT + "/live/" + line)
				.openConnection();
		return hurl.getInputStream();
	}

	public Dimension getVideoSize() {
		return videoSize;
	}

	public void setVideoSize(Dimension videoSize) {
		this.videoSize = videoSize;
	}

	public boolean isDoubleClicked() {
		return doubleClicked;
	}

	public void setDoubleClicked(boolean doubleClicked) {
		this.doubleClicked = doubleClicked;
	}

	public static WritableImage toFXImage(BufferedImage bimg, WritableImage wimg) {
		int bw = bimg.getWidth();
		int bh = bimg.getHeight();

		BufferedImage converted = new BufferedImage(bw, bh, BufferedImage.TYPE_INT_ARGB_PRE);
		Graphics2D g2d = converted.createGraphics();
		g2d.drawImage(bimg, 0, 0, null);
		g2d.dispose();
		bimg = converted;

		// assert(bimg.getType == TYPE_INT_ARGB[_PRE]);

		if (wimg == null) {
			wimg = new WritableImage(bw, bh);
		}
		PixelWriter pw = wimg.getPixelWriter();
		IntegerComponentRaster icr = (IntegerComponentRaster) bimg.getRaster();
		int data[] = icr.getDataStorage();
		int offset = icr.getDataOffset(0);
		int scan = icr.getScanlineStride();
		PixelFormat<IntBuffer> pf = (bimg.isAlphaPremultiplied() ? PixelFormat.getIntArgbPreInstance()
				: PixelFormat.getIntArgbInstance());
		pw.setPixels(0, 0, bw, bh, pf, data, offset, scan);
		return wimg;
	}

}
