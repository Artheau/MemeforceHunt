package memeforce;

import java.awt.Color;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import javax.imageio.ImageIO;
import javax.swing.ButtonGroup;
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
 * 
 * for %I in (*.bin) do (recomp.exe %~nI.bin %~nI.gfx 0 0 0)
 * 
 * Shoutouts to Zarby89
 */
public class Reskin {
	public static final String VERSION;
	private static final String RELEASE_URL = "https://github.com/fatmanspanda/MemeforceHunt/releases";

	private static final String VERSION_PATH = "/version";
	private static final String VERSION_URL = "https://raw.githubusercontent.com/fatmanspanda/MemeforceHunt/master/version";
	private static final boolean VERSION_GOOD;

	static {
		String line = "v0.0";
		try (
				BufferedReader br = new BufferedReader(new InputStreamReader(
						Reskin.class.getResourceAsStream(VERSION_PATH),
						StandardCharsets.UTF_8)
				);
			) {
				line = br.readLine();
			} catch (Exception e) {
				e.printStackTrace();
			}
		VERSION = line;
		System.out.println("Current version: " + VERSION);
		VERSION_GOOD = amIUpToDate();
		System.out.println("Up to date: " + VERSION_GOOD);
	}

	public static final int OFFSET = 0x18A800;
	public static final int PAL_LOC = 0x103B2D;
	public static final int PAL_OW = 0x100A03;

	static final int PER_ROW = 10;
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

		final Dimension d = new Dimension(320, 450);
		JFrame frame = new JFrame("Memeforce Hunt " + VERSION);

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

		// random patch button
		JButton rand = new JButton("Surprise me");
		l.putConstraint(NORTH, rand, 5, SOUTH, go);
		l.putConstraint(EAST, rand, 0, EAST, go);
		l.putConstraint(WEST, rand, 0, WEST, go);
		frame.add(rand);

		// preview
		JLabel prev = new JLabel();
		prev.setHorizontalAlignment(SwingConstants.CENTER);
		l.putConstraint(NORTH, prev, 5, SOUTH, skinsText);
		l.putConstraint(EAST, prev, 0, EAST, skinsText);
		l.putConstraint(WEST, prev, 0, WEST, skinsText);
		frame.add(prev);

		JPanel iconList = new JPanel(new GridBagLayout());
		iconList.setBackground(null);
		GridBagConstraints c = new GridBagConstraints();
		l.putConstraint(NORTH, iconList, 5, SOUTH, prev);
		l.putConstraint(EAST, iconList, 0, EAST, wrap);
		l.putConstraint(WEST, iconList, 0, WEST, wrap);
		frame.add(iconList);

		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridy = 0;
		c.gridx = 0;
		c.insets = new Insets(0, 0, 1, 1);
		ButtonGroup options = new ButtonGroup();

		for (Skin s : SKINS) {
			SkinButton sb = new SkinButton(s);
			sb.addActionListener(
				arg0 -> {
					skins.setSelectedItem(sb.getSkin());
				});
			options.add(sb);
			iconList.add(sb, c);
			c.gridx++;
			if (c.gridx == PER_ROW) {
				c.gridx = 0;
				c.gridy++;
			}
		}

		// update checks
		JButton update = new JButton("Check for updates");
		l.putConstraint(SOUTH, update, -5, SOUTH, wrap);
		l.putConstraint(EAST, update, -5, EAST, wrap);
		frame.add(update);

		update.addActionListener(
				arg0 -> {
					URL aa;
					try {
						aa = new URL(RELEASE_URL);
						Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
						if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
								desktop.browse(aa.toURI());
						}
					} catch (Exception e) {
						JOptionPane.showMessageDialog(frame,
								"uhhh",
								"Houston, we have a problem.",
								JOptionPane.WARNING_MESSAGE);
						e.printStackTrace();
					}
				});

		if (!VERSION_GOOD) {
			update.setOpaque(true);
			update.setBackground(Color.RED);
			update.setForeground(Color.WHITE);
			update.setText("Update available");
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
				int option = explorer.showSaveDialog(find);
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
							"Something went wrong.",
							"PROBLEM",
							JOptionPane.WARNING_MESSAGE,
							Skin.SCREAM.getImageIcon());
					return;
				}
				JOptionPane.showMessageDialog(frame,
						"SUCCESS",
						"Enjoy",
						JOptionPane.PLAIN_MESSAGE,
						Skin.BENANA.getImageIcon());
			});

		rand.addActionListener(
			arg0 -> {
				String n = fileName.getText();
				int r = (int) (Math.random() * SKINS.length);

				try {
					patchROM(n, SKINS[r]);
				} catch (Exception e) {
					JOptionPane.showMessageDialog(frame,
							"Something went wrong.",
							"PROBLEM",
							JOptionPane.WARNING_MESSAGE,
							Skin.SCREAM.getImageIcon());
					return;
				}
				JOptionPane.showMessageDialog(frame,
						"SUCCESS",
						"Enjoy",
						JOptionPane.PLAIN_MESSAGE,
						Skin.BENANA.getImageIcon());
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
				for (int i = 0; i < 1024; i++, pos++) {
					romStream[pos] = 0;
				}

				// write graphics
				pos = OFFSET;
				for (byte b : data) {
					romStream[pos++] = b;
				}

				romStream[PAL_LOC] = s.getPalette();
				romStream[PAL_OW] = s.getPaletteOW();

				fsOut.write(romStream, 0, romStream.length);
				fsOut.close();
			}
		}
	}

	private static boolean amIUpToDate() {
		boolean ret = true;
		URL vURL;
		try {
			vURL = new URL(VERSION_URL);
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return false;
		}

		try (
			InputStream s = vURL.openStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(s, StandardCharsets.UTF_8));
		) {
			String line = br.readLine();
			System.out.println("Discovered version: " + line);
			if (!line.equalsIgnoreCase(VERSION)) {
				ret = false;
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return ret;
	}
}