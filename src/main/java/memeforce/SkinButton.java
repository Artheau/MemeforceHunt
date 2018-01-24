package memeforce;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.BorderFactory;
import javax.swing.JRadioButton;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;

public class SkinButton extends JRadioButton {
	private static final long serialVersionUID = 1130434853839396656L;

	private final Skin skin;

	private static final int D_SIZE = 28;
	private static final Dimension D = new Dimension(D_SIZE, D_SIZE);

	private static final Border BORDER_ON = BorderFactory.createBevelBorder(BevelBorder.LOWERED);
	private static final Border BORDER_OFF = BorderFactory.createBevelBorder(BevelBorder.RAISED);

	private static final Color BG_ON = new Color(192, 224, 192);
	private static final Color BG_OFF = new Color(224, 224, 224);
	private static final Color BG_HOVER = new Color(120, 192, 248);

	public SkinButton(Skin skin) {
		super();
		this.skin = skin;

		this.setIcon(skin.getImageIconSmall());
		this.setBackground(null);
		this.setPreferredSize(D);
		this.setMinimumSize(D);
		this.setMaximumSize(D);

		this.setVerticalAlignment(SwingConstants.CENTER);
		this.setHorizontalAlignment(SwingConstants.CENTER);
		this.setIcon(skin.getImageIconSmall());

		this.setBorderPainted(true);
		this.setBorder(BORDER_OFF);
		this.setBackground(BG_OFF);

		this.setFocusable(false);
		this.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

		this.addChangeListener(arg0 -> doState());

		this.addMouseListener(new MouseListener() {
			public void mouseClicked(MouseEvent e) {}
			public void mousePressed(MouseEvent e) {
				setBackground(BG_HOVER);
				setBorder(BORDER_ON);
			}

			public void mouseReleased(MouseEvent e) {
				doState();
			}

			public void mouseEntered(MouseEvent e) {
				setBackground(BG_HOVER);
			}

			public void mouseExited(MouseEvent e) {
				doState();
			}
		});
	}

	private void doState() {
		if (this.isSelected()) {
			this.setBackground(BG_ON);
			this.setBorder(BORDER_ON);
		} else {
			this.setBackground(BG_OFF);
			this.setBorder(BORDER_OFF);
		}
	}

	public Skin getSkin() {
		return skin;
	}
}