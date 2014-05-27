package me.warcrafted.wnc;

import java.awt.TextArea;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

public class Window {

	private JFrame frame;

	private JTextField textField;
	private TextArea textArea;
	private JButton btnSave;

	private String name;

	/**
	 * Create the application.
	 */
	public Window(String name) {
		initialize();
		this.name = name;
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 450, 300);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setTitle("Warcraft Name Checker");
		frame.getContentPane().setLayout(null);
		frame.setLocationRelativeTo(null);
		frame.setResizable(false);
		frame.setEnabled(true);
		frame.setVisible(true);

		textField = new JTextField();
		textField.setBounds(5, 4, 90, 20);
		frame.getContentPane().add(textField);
		textField.setColumns(10);

		textArea = new TextArea();
		textArea.setBounds(0, 30, 444, 242);
		frame.getContentPane().add(textArea);

		JButton btnLoad = new JButton("Load");
		btnLoad.setToolTipText("Load realms from an existing text file");
		btnLoad.setBounds(98, 3, 89, 23);
		frame.getContentPane().add(btnLoad);

		btnLoad.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String name = textField.getText();

				if (!name.isEmpty()) {
					File file = new File("./wncData", name + ".txt");

					if (file.exists()) {
						String[] lines = null;
						
						try {
							lines = openFile();
						} catch (IOException ex) {
							ex.printStackTrace();
						}

						if(lines != null) {
							textArea.setText(null);
							
							for(String line : lines) {
								textArea.append(line + "\n");
							}
						}
						
					} else {
						JOptionPane.showMessageDialog(frame, "File doesn't exist!", "Error", JOptionPane.ERROR_MESSAGE);
					}
				}
			}
		});

		btnSave = new JButton("Save");
		btnSave.setToolTipText("Save these realms to a text file");
		btnSave.setBounds(350, 3, 89, 23);
		frame.getContentPane().add(btnSave);

		btnSave.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				File folder = new File(".", "wncData");

				if (!folder.exists()) {
					folder.mkdir();
				}

				File file = new File("./wncData", name + ".txt");

				if (!file.exists()) {
					try {
						file.createNewFile();
					} catch (IOException ex) {
						ex.printStackTrace();
						return;
					}

				} else {
					JOptionPane.showMessageDialog(frame, "File already exists!", "Error", JOptionPane.ERROR_MESSAGE);
					return;
				}

				PrintWriter writer = null;

				try {
					writer = new PrintWriter(file, "UTF-8");
				} catch (FileNotFoundException ex) {
					ex.printStackTrace();
					return;
				} catch (UnsupportedEncodingException ex) {
					ex.printStackTrace();
					return;
				}

				for (String line : textArea.getText().split("\n")) {
					writer.println(line);
				}

				writer.close();
				JOptionPane.showMessageDialog(frame, "Realms saved to " + name + ".txt", "Success", JOptionPane.INFORMATION_MESSAGE);
			}
		});
	}

	public void setContents(List<String> text) {
		for (String s : text) {
			textArea.append(s.replaceAll(":", " ") + "\n");
		}
	}

	private String[] openFile() throws IOException {
		String path = "./wncData/" + name + ".txt";

		FileReader fileReader = new FileReader(path);
		BufferedReader textReader = new BufferedReader(fileReader);

		int lines = readLines(path);
		String[] textData = new String[lines];

		for (int i = 0; i < lines; i++) {
			textData[i] = textReader.readLine();
		}

		textReader.close();
		return textData;
	}

	private int readLines(String path) throws IOException {
		FileReader fileReader = new FileReader(path);
		BufferedReader lineReader = new BufferedReader(fileReader);

		int lineNumber = 0;

		while ((lineReader.readLine()) != null) {
			lineNumber++;
		}

		lineReader.close();
		return lineNumber;
	}
}
