package com.mongodb.flightdemo;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

import javax.swing.ImageIcon;
import javax.swing.JComponent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class DrawingContext extends JComponent {

	protected static final Logger logger = LoggerFactory
			.getLogger(DrawingContext.class);

	private final static Image GREEN_PLANE = new ImageIcon(
			DrawingContext.class.getResource("/images/plane-green.png"))
			.getImage();
	private final static Image BLUE_PLANE = new ImageIcon(
			DrawingContext.class.getResource("/images/plane-blue.png"))
			.getImage();
	private final static Image RED_DOT = new ImageIcon(
			DrawingContext.class.getResource("/images/dot-red.png")).getImage();

	private Image image;
	private double angle = 0;

	private FlightInfo flightInfo;

	public DrawingContext(FlightInfo flightInfo) {
		this.flightInfo = flightInfo;
		if (flightInfo.getAirline() != null) {
			this.image = GREEN_PLANE;
		} else {
			this.image = BLUE_PLANE;
		}

		Dimension size = new Dimension(image.getWidth(null),
				image.getHeight(null));
		setPreferredSize(size);
		setMinimumSize(size);
		setMaximumSize(size);
		setSize(size);
		setLayout(null);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((flightInfo == null) ? 0 : flightInfo.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;

		DrawingContext other = (DrawingContext) obj;
		if (flightInfo == null) {
			if (other.flightInfo != null)
				return false;
		} else if (!flightInfo.equals(other.flightInfo))
			return false;

		return true;
	}

	protected void paintComponent(Graphics g) {
		Graphics2D g2d = (Graphics2D) g;

		AffineTransform tx = AffineTransform.getRotateInstance(angle,
				image.getWidth(null) / 2, image.getHeight(null) / 2);
		g2d.drawImage(image, tx, null);
	}

	public void setSpritePosition(AffineTransform xx) {
		Point2D newPositionInScreenCoordinates = xx.transform(
				new Point2D.Double(flightInfo.getLon(), flightInfo.getLat()),
				null);

		int newX = (int) newPositionInScreenCoordinates.getX();
		int newY = (int) newPositionInScreenCoordinates.getY();

		// center the image
		newX = newX - this.image.getWidth(null) / 2;
		newY = newY - this.image.getHeight(null) / 2;

		this.setLocation(newX, newY);
	}

	public Image getImage() {
		return image;
	}

	public void setImage(Image image) {
		this.image = image;
	}

	public FlightInfo getFlightInfo() {
		return flightInfo;
	}

	public void setFlightInfo(FlightInfo flightInfo) {
		this.changePosition(flightInfo);
	}

	@Override
	public String toString() {
		// return "DrawingContext [spriteEnv=" + spriteEnv + ", flightInfo=" +
		// flightInfo + "]";
		return flightInfo.getFlightNum();
	}

	public void changePosition(FlightInfo flightInfo) {
		if (this.flightInfo != null) {
			double currentLat = this.flightInfo.getLat();
			double currentLon = this.flightInfo.getLon();

			double deltaLat = flightInfo.getLat() - currentLat;
			double deltaLon = flightInfo.getLon() - currentLon;

			// rotate based on the last location
			// tan(a) = rise/run
			double angle = Math.atan(deltaLat / deltaLon);

			if (deltaLon >= 0) {
				if (deltaLat >= 0) {
					this.angle = Math.PI / 2 - angle;
				} else {
					this.angle = Math.PI / 2 + Math.abs(angle);
				}
			} else {
				this.angle = -1 * (Math.PI / 2 + angle);
			}

			// double degrees = this.angle * 180 / Math.PI;
			// logger.debug("current: (" + currentLat + "," + currentLon +
			// ") delta: (" + deltaLat + "," + deltaLon + ") angle: " +
			// degrees);
		}

		this.flightInfo = flightInfo;
	}

}