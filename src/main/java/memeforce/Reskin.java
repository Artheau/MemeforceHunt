package memeforce;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.filechooser.FileNameExtensionFilter;

import spritemanipulator.BetterJFileChooser;
import spritemanipulator.SpriteManipulator;

import static javax.swing.SpringLayout.*;

/*
 * Compression command:
 * recomp.exe u_item.bin item.bin 0 0 0 
 */
public class Reskin {
	public static final int OFFSET = 0x18A800;
	public static final int PAL_LOC = 0x100A01;

	static final Skin[] SKINS = Skin.values();

	public static void main(String[] args) throws IOException {
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				try {
					printGUI();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	public static void printGUI() {
		// try to set LaF
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			// do nothing
		} //end System

		final Dimension d = new Dimension(350, 350);
		JFrame frame = new JFrame("Reskin");

		SpringLayout l = new SpringLayout();
		JPanel wrap = (JPanel) frame.getContentPane();
		wrap.setLayout(l);

		JLabel skinsText = new JLabel("---");
		JComboBox<Skin> skins = new JComboBox<Skin>(Skin.values());
		skins.setEditable(false);

		// rom name
		JTextField fileName = new JTextField("");
		l.putConstraint(NORTH, fileName, 5, NORTH, wrap);
		l.putConstraint(EAST, fileName, -15, HORIZONTAL_CENTER, skinsText);
		l.putConstraint(WEST, fileName, 5, WEST, wrap);
		frame.add(fileName);

		// file chooser
		JButton find = new JButton("Open");
		l.putConstraint(NORTH, find, 0, NORTH, fileName);
		l.putConstraint(EAST, find, -5, EAST, wrap);
		l.putConstraint(WEST, find, 5, EAST, fileName);
		frame.add(find);

		// skin chooser
		l.putConstraint(NORTH, skinsText, 5, SOUTH, fileName);
		l.putConstraint(EAST, skinsText, -5, EAST, wrap);
		l.putConstraint(WEST, skinsText, 5, HORIZONTAL_CENTER, wrap);
		frame.add(skinsText);

		// patch button
		JButton go = new JButton("Patch");
		l.putConstraint(NORTH, go, 5, SOUTH, fileName);
		l.putConstraint(EAST, go, -5, HORIZONTAL_CENTER, wrap);
		l.putConstraint(WEST, go, 5, WEST, wrap);
		frame.add(go);

		// preview
		JLabel prev = new JLabel();
		prev.setHorizontalAlignment(SwingConstants.CENTER);
		l.putConstraint(NORTH, prev, 5, SOUTH, skinsText);
		l.putConstraint(EAST, prev, 0, EAST, skinsText);
		l.putConstraint(WEST, prev, 0, WEST, skinsText);
		frame.add(prev);

		// random patch button
		JButton rand = new JButton("Surprise me");
		l.putConstraint(NORTH, rand, 5, SOUTH, go);
		l.putConstraint(EAST, rand, 0, EAST, go);
		l.putConstraint(WEST, rand, 0, WEST, go);
		frame.add(rand);

		JPanel iconList = new JPanel(new GridBagLayout());
		iconList.setBackground(null);
		GridBagConstraints c = new GridBagConstraints();
		l.putConstraint(NORTH, iconList, 5, SOUTH, prev);
		l.putConstraint(EAST, iconList, 0, EAST, wrap);
		l.putConstraint(WEST, iconList, 0, WEST, wrap);
		frame.add(iconList);

		final int maxCol = 6;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridy = 0;
		c.gridx = 0;
		for (Skin s : SKINS) {
			SkinButton sb = new SkinButton(s);
			sb.addActionListener(
				arg0 -> {
					skins.setSelectedItem(sb.getSkin());
				});
			iconList.add(sb, c);
			c.gridx++;
			if (c.gridx == maxCol) {
				c.gridx = 0;
				c.gridy++;
			}
		}

		// file explorer
		final BetterJFileChooser explorer = new BetterJFileChooser();
		FileNameExtensionFilter romFilter =
				new FileNameExtensionFilter("ALttP ROM files", new String[] { "sfc" });
		explorer.setAcceptAllFileFilterUsed(false);
		explorer.setFileFilter(romFilter);

		skins.addItemListener(
			arg0 -> {
				Skin sel = (Skin) skins.getSelectedItem();
				prev.setIcon(sel.getImageIcon());
				skinsText.setText(sel.toString());
			});
		prev.setIcon(Skin.BENANA.getImageIcon());
		skinsText.setText(Skin.BENANA.toString());

		// can't clear text due to wonky code
		// have to set a blank file instead
		final File EEE = new File("");

		// load sprite file
		find.addActionListener(
			arg0 -> {
				explorer.setSelectedFile(EEE);
				int option = explorer.showOpenDialog(find);
				if (option == JFileChooser.CANCEL_OPTION) { return; }

				// read the file
				String n = "";
				try {
					n = explorer.getSelectedFile().getPath();
				} catch (Exception e) {
					JOptionPane.showMessageDialog(frame,
							e.getMessage(),
							"PROBLEM",
							JOptionPane.WARNING_MESSAGE);
					return;
				}

				if (SpriteManipulator.testFileType(n, "sfc")) {
					fileName.setText(n);
				}
			});

		go.addActionListener(
			arg0 -> {
				String n = fileName.getText();
				try {
					patchROM(n, (Skin) skins.getSelectedItem());
				} catch (Exception e) {
					JOptionPane.showMessageDialog(frame,
							e.getMessage(),
							"PROBLEM",
							JOptionPane.WARNING_MESSAGE);
					return;
				}
				JOptionPane.showMessageDialog(frame,
						"SUCCESS",
						"Enjoy",
						JOptionPane.WARNING_MESSAGE);
			});

		rand.addActionListener(
			arg0 -> {
				String n = fileName.getText();
				int r = (int) (Math.random() * SKINS.length);

				try {
					patchROM(n, SKINS[r]);
				} catch (Exception e) {
					JOptionPane.showMessageDialog(frame,
							e.getMessage(),
							"PROBLEM",
							JOptionPane.WARNING_MESSAGE);
					return;
				}
				JOptionPane.showMessageDialog(frame,
						"SUCCESS",
						"Enjoy",
						JOptionPane.WARNING_MESSAGE);
			});

		// ico
		BufferedImage ico;
		try {
			ico = ImageIO.read(Skin.class.getResourceAsStream("/triforce piece.png"));
		} catch (IOException e) {
			ico = new BufferedImage(16, 16, BufferedImage.TYPE_4BYTE_ABGR);
		}

		frame.setIconImage(ico);

		// frame setting
		wrap.setBackground(new Color(225, 225, 225));
		frame.setSize(d);
		frame.setMinimumSize(d);
		frame.setResizable(false);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLocation(350, 350);

		frame.setVisible(true);
	}

	public static void patchROM(String romTarget, Skin s) throws IOException {
		byte[] romStream;
		try (FileInputStream fsInput = new FileInputStream(romTarget)) {
			romStream = new byte[(int) fsInput.getChannel().size()];
			fsInput.read(romStream);
			fsInput.getChannel().position(0);
			fsInput.close();

			try (FileOutputStream fsOut = new FileOutputStream(romTarget)) {
				byte[] data = s.getData();

				// clear up space (safety)
				int pos = OFFSET;
				for (int i = 0; i < 950; i++, pos++) {
					romStream[pos] = 0;
				}

				// write graphics
				pos = OFFSET;
				for (byte b : data) {
					romStream[pos++] = b;
				}
				romStream[PAL_LOC] = s.getPalette();

				fsOut.write(romStream, 0, romStream.length);
				fsOut.close();
			}
		}
	}
}