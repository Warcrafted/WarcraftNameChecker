package me.warcrafted.wnc;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Main {

	private JFrame frame;

	private JProgressBar progressBar;

	private JTextField textFieldName;
	private JTextField textFieldRealm;

	private JButton buttonName;
	private JButton buttonRealms;

	private List<String> euRealms;
	private List<String> usRealms;

	private List<String> used;
	private List<String> available;

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
		frame.setBounds(100, 100, 280, 130);
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

		getRealms("eu");
		getRealms("us");

		used = new ArrayList<String>();
		available = new ArrayList<String>();

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

		buttonName = new JButton("Check Name");
		buttonName.setToolTipText("See if the name is available on given realm");
		buttonName.setBounds(165, 20, 100, 23);
		frame.getContentPane().add(buttonName);

		buttonName.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				enableButtons(false);

				String server = comboBox.getSelectedItem().toString();
				String realm = textFieldRealm.getText();
				String name = textFieldName.getText();

				int j = allCharacters(server, realm, name);
				JOptionPane.showMessageDialog(frame, "Scanning through " + j + " characters...", "Scanner", JOptionPane.INFORMATION_MESSAGE);

				boolean available = isAvailable(server, realm, name, 1, j);

				progressBar.setValue(0);

				JOptionPane.showMessageDialog(frame, "The name \"" + name + "\" is " + (available ? "" : "not") + " available!", "Scanner", JOptionPane.INFORMATION_MESSAGE);

				enableButtons(true);
			}
		});

		buttonRealms = new JButton("Check Realms");
		buttonRealms.setToolTipText("See all the available realms for this name");
		buttonRealms.setBounds(165, 45, 100, 23);
		frame.getContentPane().add(buttonRealms);

		progressBar = new JProgressBar();
		progressBar.setBounds(10, 85, 255, 15);
		progressBar.setBorderPainted(false);
		frame.getContentPane().add(progressBar);

		buttonRealms.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				enableButtons(false);

				String region = comboBox.getSelectedItem().toString();
				String name = textFieldName.getText();

				JOptionPane.showMessageDialog(frame, "Scanning for available realms...", "Scanner", JOptionPane.INFORMATION_MESSAGE);

				List<String> realms = getRealmsAvailable(region, name, 1);

				progressBar.setValue(0);

				if (!realms.isEmpty()) {
					Window window = new Window(name + "-" + region.toLowerCase());
					window.setContents(realms);

				} else {
					JOptionPane.showMessageDialog(frame, "There are no available realms for this name.", "Scanner", JOptionPane.INFORMATION_MESSAGE);
				}

				enableButtons(true);
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
		progressBar.setValue(percent);
		progressBar.repaint();

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

	private List<String> getRealms(String server) {
		if (euRealms == null) {
			euRealms = new ArrayList<String>();
		} else {
			if (server.equalsIgnoreCase("eu") && !euRealms.isEmpty() && !usRealms.isEmpty()) {
				return euRealms;
			}
		}

		if (usRealms == null) {
			usRealms = new ArrayList<String>();
		} else {
			if (server.equalsIgnoreCase("us") && !usRealms.isEmpty() && !euRealms.isEmpty()) {
				return usRealms;
			}
		}

		if (server.isEmpty())
			return null;

		String u = "http://" + server + ".battle.net/wow/en/status";
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

		int j = server.equals("eu") ? 266 : 246; // 266 realms on eu, 246 realms on us

		for (int i = 1; i <= j; i++) {
			Element name = doc.select("#all-realms > div > table > tbody > tr:nth-child( " + i + ") > td.name").first();
			Element type = doc.select("#all-realms > div > table > tbody > tr:nth-child( " + i + ") > td.type").first();
			Element population = doc.select("#all-realms > div > table > tbody > tr:nth-child( " + i + ") > td.population").first();
			Element locale = doc.select("#all-realms > div > table > tbody > tr:nth-child( " + i + ") > td.locale").first();

			if (server.equals("eu")) {
				euRealms.add(name.text() + ":" + type.text() + ":" + population.text() + ":" + locale.text());
			} else if (server.equals("us")) {
				usRealms.add(name.text() + ":" + type.text() + ":" + population.text() + ":" + locale.text());
			}
		}

		return server.equals("eu") ? euRealms : usRealms;
	}

	private List<String> getRealmsAvailable(String server, String name, final int page) {
		if (server.isEmpty() || name.isEmpty())
			return null;

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

		for (String realm : getRealms(server)) {
			String realmName = realm.split(":")[0].trim();

			if (isUsed(name, realmName, doc)) {
				used.add(realm);
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

		int percent = (int) ((page * 100) / curMax);
		progressBar.setValue(percent);
		progressBar.update(progressBar.getGraphics());

		if (curMax == 0 || page == curMax) {
			for (String realm : getRealms(server)) {
				if (!contains(realm, used)) {
					available.add(realm);
				}
			}

			return available;
		} else {
			return getRealmsAvailable(server, name, page + 1);
		}
	}

	private boolean contains(String a, List<String> b) {
		for (String string : b) {
			if (string.equals(a)) {
				return true;
			}
		}

		return false;
	}

	private boolean isUsed(String name, String realm, Document doc) {
		Element el = doc.select("#content > div > div.content-bot.clear > div > div.search-right > div.view-table > div > table > tbody").first();
		String[] c = el.text().split(name);

		List<String> a = new ArrayList<String>();

		for (String s : c) {
			if (s.contains(realm) && !a.contains(realm)) {
				a.add(realm);
			}
		}

		return a.contains(realm);
	}

	private void enableButtons(boolean b) {
		buttonName.setEnabled(b);
		buttonRealms.setEnabled(b);
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
