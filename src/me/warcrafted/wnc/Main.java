package me.warcrafted.wnc;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Main {

	private JFrame frame;
	private JTextField textFieldName;
	private JTextField textFieldRealm;
	private JLabel labelScanner;

	private String s = "Scanner: ";

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Main window = new Main();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public Main() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 277, 110);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setTitle("Warcraft Name Checker");
		frame.getContentPane().setLayout(null);
		frame.setLocationRelativeTo(null);
		frame.setResizable(false);
		frame.setEnabled(true);

		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}

		textFieldName = new JTextField();
		textFieldName.setBounds(55, 56, 100, 20);
		frame.getContentPane().add(textFieldName);
		textFieldName.setColumns(10);

		final JComboBox<Object> comboBox = new JComboBox<Object>(new String[] { "EU", "US" });
		comboBox.setBounds(55, 10, 100, 20);
		frame.getContentPane().add(comboBox);

		textFieldRealm = new JTextField();
		textFieldRealm.setBounds(55, 33, 100, 20);
		frame.getContentPane().add(textFieldRealm);
		textFieldRealm.setColumns(10);

		JLabel labelRegion = new JLabel("Region:");
		labelRegion.setBounds(10, 10, 46, 14);
		frame.getContentPane().add(labelRegion);

		JLabel labelRealm = new JLabel("Realm:");
		labelRealm.setBounds(10, 33, 36, 14);
		frame.getContentPane().add(labelRealm);

		JLabel lblName = new JLabel("Name:");
		lblName.setBounds(10, 56, 36, 14);
		frame.getContentPane().add(lblName);

		JButton buttonCheck = new JButton("Check");
		buttonCheck.setBounds(165, 32, 89, 23);
		frame.getContentPane().add(buttonCheck);

		labelScanner = new JLabel("Scanner: 0%");
		labelScanner.setBounds(175, 59, 89, 14);
		labelScanner.setVisible(false);
		frame.getContentPane().add(labelScanner);

		buttonCheck.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String server = comboBox.getSelectedItem().toString();
				String realm = textFieldRealm.getText();
				String name = textFieldName.getText();

				int j = allCharacters(server, realm, name);
				JOptionPane.showMessageDialog(frame, "Scanning through " + j + " characters...", "Scanner", JOptionPane.INFORMATION_MESSAGE);

				boolean available = isAvailable(server, realm, name, 1, j);
				labelScanner.setText(s + ("100%"));
				labelScanner.setVisible(false);

				JOptionPane.showMessageDialog(frame, "The name \"" + name + "\" is " + (available ? "" : "not") + " available!", "Scanner", JOptionPane.INFORMATION_MESSAGE);
			}
		});
	}

	private int allCharacters(String server, String realm, String name) {
		if (server.isEmpty() || realm.isEmpty() || name.isEmpty())
			return 0;

		String u = "http://" + server + ".battle.net/wow/en/search?f=wowcharacter&q=" + name;
		URL url = null;

		try {
			url = new URL(u);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}

		Document doc = null;

		try {
			doc = Jsoup.parse(url.openStream(), "UTF-8", u);
		} catch (IOException e) {
			e.printStackTrace();
		}

		Elements elements = new Elements();
		elements.addAll(doc.select("#menu-search > li.item-active > a > span"));

		int j = Integer.parseInt(elements.first().text().split("\\(")[1].split("\\)")[0]);
		return j;
	}

	private boolean isAvailable(String server, String realm, String name, final int page, final int all) {
		if (server.isEmpty() || realm.isEmpty() || name.isEmpty())
			return false;

		String u = "http://" + server + ".battle.net/wow/en/search?f=wowcharacter&page=" + page + "&q=" + name;
		URL url = null;

		try {
			url = new URL(u);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}

		Document doc = null;

		try {
			doc = Jsoup.parse(url.openStream(), "UTF-8", u);
		} catch (IOException e) {
			e.printStackTrace();
		}

		int percent = (int) (((page * 25) * 100) / all);

		if (!labelScanner.isVisible())
			labelScanner.setVisible(true);
		labelScanner.setText(s + (percent + "%"));
		labelScanner.paintImmediately(labelScanner.getVisibleRect());

		for (Element el : doc.select("#content > div > div.content-bot.clear > div > div.search-right > div.view-table > div > table > tbody")) {
			String[] c = el.text().split(name);

			for (String s : c) {
				if (s.contains(realm)) {
					return false;
				}
			}
		}

		int curMax = 0;

		for (Element el : doc.select("#content > div > div.content-bot.clear > div > div.search-right > div:nth-child(3) > div > ul")) {
			String[] s = el.text().split(" ");

			for (String ss : s) {
				if (isInteger(ss) && toInteger(ss) > curMax) {
					curMax = toInteger(ss);
				}
			}
		}

		return (curMax == 0 || page == curMax) ? true : isAvailable(server, realm, name, page + 1, all);
	}

	private int toInteger(String s) {
		return Integer.valueOf(s);
	}

	private boolean isInteger(String s) {
		try {
			Integer.parseInt(s);
			return true;
		} catch (NumberFormatException ex) {
			return false;
		}
	}
}
