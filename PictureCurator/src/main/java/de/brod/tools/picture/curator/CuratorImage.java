package de.brod.tools.picture.curator;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageInputStream;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;

public class CuratorImage {

	private BufferedImage bufferedImage;
	private int orientation;

	public CuratorImage(File file) throws IOException {
		// de.brod.tools.picture.curator.Metadata.readAndDisplayMetadata(file);
		bufferedImage = ImageIO.read(file);
		orientation = getOrientation(file);
	}

	public Image resizeBufferedImage(int scaledWidth, int scaledHeight) {
		Image image = SwingFXUtils.toFXImage(createResizedCopy(bufferedImage, scaledWidth, scaledHeight, true), null);
		return image;
	}

	BufferedImage createResizedCopy(BufferedImage poriginalImage, int scaledWidth, int scaledHeight,
			boolean preserveAlpha) {
		System.out.println("resizing...");
		double rotation = 0;
		if (orientation == 6) {
			rotation = 90;
		} else if (orientation == 8) {
			rotation = -90;
		}
		int imageType = preserveAlpha ? BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB;
		BufferedImage scaledBI = new BufferedImage(scaledWidth, scaledHeight, imageType);
		Graphics2D g = scaledBI.createGraphics();
		g.setColor(Color.LIGHT_GRAY);
		g.fillRect(0, 0, scaledWidth, scaledHeight);
		if (preserveAlpha) {
			g.setComposite(AlphaComposite.Src);
		}
		float newWidth;
		float newHeight;
		if (rotation != 0) {
			newWidth = scaledHeight;
			newHeight = newWidth / poriginalImage.getWidth() * poriginalImage.getHeight();
			if (newHeight > scaledWidth) {
				newHeight = scaledWidth;
				newWidth = newHeight / poriginalImage.getHeight() * poriginalImage.getWidth();
			}
			g.rotate(Math.toRadians(rotation), scaledWidth / 2, scaledHeight / 2);
		} else {
			newWidth = scaledWidth;
			newHeight = newWidth / poriginalImage.getWidth() * poriginalImage.getHeight();
			if (newHeight > scaledHeight) {
				newHeight = scaledHeight;
				newWidth = newHeight / poriginalImage.getHeight() * poriginalImage.getWidth();
			}
		}
		g.drawImage(poriginalImage, (int) ((scaledWidth - newWidth) / 2), (int) ((scaledHeight - newHeight) / 2),
				(int) newWidth, (int) newHeight, null);
		g.dispose();
		return scaledBI;
	}

	int getOrientation(File file) {
		ImageInputStream iis = null;
		try {
			iis = ImageIO.createImageInputStream(file);
			Iterator<ImageReader> readers = ImageIO.getImageReaders(iis);

			if (readers.hasNext()) {

				// pick the first available ImageReader
				ImageReader reader = readers.next();

				// attach source to the reader
				reader.setInput(iis, true);

				// read metadata of first image
				IIOMetadata metadata = reader.getImageMetadata(0);

				Node root = metadata.getAsTree(metadata.getNativeMetadataFormatName());
				// displayMetadata(root);

				byte[] exifData = null;
				for (Node unkown : findNodes(root, new ArrayList<Node>(2), "unknown")) {
					Node marker = unkown.getAttributes().item(0);
					if (marker.getNodeValue().equals("225")) {
						exifData = (byte[]) ((IIOMetadataNode) unkown).getUserObject();
						break;
					}
				}
				int findOrientation = findOrientation(exifData);
				System.out.println(file.getName() + ": " + findOrientation);
				return findOrientation;
			}
		} catch (Exception e) {

			e.printStackTrace();
		} finally {
			if (iis != null) {
				try {
					iis.close();
				} catch (IOException e) {
					// could not close
				}
			}
		}
		return 0;
	}

	private int findOrientation(byte[] exifData) {
		ByteBuffer buf = ByteBuffer.wrap(exifData);

		// exif header
		if (buf.getInt() != 0x45786966 || buf.getShort() != 0x0000) {
			throw new IllegalArgumentException("Invalid EXIF Header!");
		}

		buf = buf.slice();

		// tiff header
		// byte order
		switch (buf.getShort()) {
		case 0x4949:
			buf.order(ByteOrder.LITTLE_ENDIAN);
			break;
		case 0x4D4D:
			buf.order(ByteOrder.BIG_ENDIAN);
			break;
		default:
			throw new IllegalArgumentException("Invalid byte order!");
		}

		// 42
		if (buf.getShort() != 0x002A) {
			throw new IllegalArgumentException("Invalid TIFF Header!");
		}

		// offset to first directory (skip to position)
		buf.position(buf.getInt());

		// first directory
		int numOfTags = buf.getShort() & 0xffff;
		for (int i = 0; i < numOfTags; i++) {
			// orientation tag # is 0x112 according to EXIF spec
			if (buf.getShort() == 0x112) {
				buf.position(buf.position() + 6);
				return buf.getShort() & 0xff;
			} else {
				buf.position(buf.position() + 10);
			}
		}

		return -1;
	}

	private List<Node> findNodes(Node parent, List<Node> foundNodes, String wantedNode) {
		if (parent == null) {
			return foundNodes;
		}

		if (wantedNode.equals(parent.getNodeName())) {
			foundNodes.add(parent);
		}
		// search attributes
		if (parent.hasAttributes()) {
			NamedNodeMap attr = parent.getAttributes();

			for (int i = 0; i < attr.getLength(); i++) {
				foundNodes = findNodes(attr.item(i), foundNodes, wantedNode);
			}
		}
		// search children
		Node child = parent.getFirstChild();
		while (child != null) {
			foundNodes = findNodes(child, foundNodes, wantedNode);
			child = child.getNextSibling();
		}
		return foundNodes;
	}
}
