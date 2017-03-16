package spellingGUI;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

//Author Dylan Hall, dhal525
//Class representing the GUI, contains the main method to be run on execute.
//Holds most of the logic associated with initiating and ensure correct running of 
//the program.
public class SpellingGUI extends JFrame implements ActionListener {
	//Note since this is intended to be run through a script, this files location
	//is set in the current directory.
	private static File WORD_DATA_FILE = new File ("dhal525WordData.ser");
	private static final long serialVersionUID = -7849162247904769768L;

	//Fields for populating the GUI
	private JTextField txt = new JTextField("");
	private JLabel title = new JLabel("Main Menu");
	private JLabel wordLabel = new JLabel("Enter Words Here: ");
	private JButton newQuiz = new JButton("New Spelling Quiz");
	private JButton review = new JButton("Review Mistakes");
	private JButton stats = new JButton("View Statistics");
	private JButton clear = new JButton("Clear Statistics");
	private JButton loadFile = new JButton("Select Different Input File");
	private JTextArea txtOutput = new JTextArea(10, 20);

	//fields for the two usable objects
	private SpellingTest _quiz; //Holds the "logic" during the tests Normal/Review
	private WordData _dataSet; //Holds data

	/**
	 * The constructor for this class initializes the GUI and populates it with components
	 * it also runs a few other methods within this class that help with logistics;
	 * such as checking for serializable object
	 * Layout was chosen to be GridBagLayout for prototype purposes for its general
	 * Tidiness/strength of modifying
	 */
	public SpellingGUI() {
		super("Spelling Quiz");
		setSize(400, 550);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		fileCheck();
		Font customFont = new Font("Tahoma",Font.ITALIC,12);

		//Button for New Spelling quiz, listener is this class.
		c.fill = GridBagConstraints.HORIZONTAL;
		c.ipady = 40;      
		c.weightx = 0.0;
		c.gridwidth = 3;
		c.gridx = 0;
		c.gridy = 1;
		add(newQuiz, c);
		newQuiz.addActionListener(this);

		//Button for Review Mistakes
		c.fill = GridBagConstraints.HORIZONTAL;
		c.ipady = 20;      
		c.weightx = 0.0;
		c.gridwidth = 3;
		c.gridx = 0;
		c.gridy = 2;
		add(review, c);
		review.addActionListener(this);

		//Button for View Statistics
		GridBagConstraints c2 = new GridBagConstraints();
		c2.fill = GridBagConstraints.HORIZONTAL;
		c2.weightx = 0.5;
		c2.gridwidth = 3;
		c2.gridx = 0;
		c2.gridy = 3;
		add(stats, c2);
		stats.addActionListener(this);

		//Button for Clear Statstics
		c2.fill = GridBagConstraints.HORIZONTAL;
		c2.weightx = 0.5;
		c2.gridwidth = 3;
		c2.gridx = 0;
		c2.gridy = 4;
		add(clear, c2);
		clear.addActionListener(this);

		//Button for Load from file, it specifies its own action listener as it
		//expects to sometimes be called at the start
		c2.fill = GridBagConstraints.HORIZONTAL;
		c2.weightx = 0.5;
		c2.gridwidth = 3;
		c2.gridx = 0;
		c2.gridy = 5;
		add(loadFile, c2);
		loadFile.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				txtOutput.setText("");
				getWordFile();
			}
		});
		//Title pane, just for prototyping where things could go
		c2.fill = GridBagConstraints.HORIZONTAL;
		c2.weightx = 0.5; 
		c2.gridwidth = 3;
		c2.gridx = 0;
		c2.gridy = 0;
		add(title,c2);

		//Text field settings
		c2.fill = GridBagConstraints.HORIZONTAL;
		c2.weightx = 0.5;
		c2.ipady = 250;//lots more padding for making text field large.
		c2.gridwidth = 3;
		c2.gridx = 0;
		c2.gridy = 6;
		JScrollPane scroll = new JScrollPane(txtOutput);
		add(scroll,c2);
		txtOutput.setFont(customFont);//setting a nicer custom font.
		txtOutput.append("Welcome to the Spelling Aid!\nPlease choose an option to begin.\n");

		//Label for the JTextField, lined up with the bottom of the page.
		c2.weightx = 0;
		c2.gridwidth = 1;
		c2.ipady = 5;
		c2.gridx = 0;
		c2.gridy = 7;
		c2.anchor = GridBagConstraints.PAGE_END; //bottom of space
		add(wordLabel,c2);

		//JTextField, with its own action listener - will fire to an active quiz object.
		c2.fill = GridBagConstraints.HORIZONTAL;
		c2.ipady = 0;       //reset to default
		c2.weighty = 1.0;   //request any extra vertical space
		c2.gridx = 1;       //aligned with button 2
		c2.gridwidth = 3;   //2 columns wide
		c2.gridy = 7;       //third row
		add(txt, c2);
		txt.addActionListener(new ActionListener() {//only when enter pressed?
			@Override
			public void actionPerformed(ActionEvent e) {
				if(_quiz==null){//safety check, shows nicer dialog.
					JOptionPane.showMessageDialog(txt, "Please start a test before entering words");
				}else{//has an active quiz, so fire event, can then unpack the String within it.
					if(!txt.getText().equals("")){
						_quiz.actionPerformed(e);
					}
				}
				txt.setText("");//clears text field to show user its been entered in
			}
		});

	}

	/**
	 * Method for most of the buttons on the GUI, will unpack and check where it 
	 * came from. Will perform necessary operations based on context.
	 * Always does a check to see if the data is "empty" - method in WordData, means
	 * there hasn't been a word file, so must specify to the user to choose a file.
	 */
	public void actionPerformed(ActionEvent e) {
		txtOutput.setText("");
		if(_dataSet.isEmpty()){
			int reply = JOptionPane.showConfirmDialog(null,"Please select a file before proceeding","No Wordlist File",JOptionPane.OK_CANCEL_OPTION);
			if(reply==JOptionPane.OK_OPTION){
				getWordFile();//private method in class for dealing with choices.
			}
		}else{
			//note both Normal/Review quizes are treated fairly uniformly to reuse logic.
			if(e.getSource()==newQuiz){
				txtOutput.append("New Spelling Quiz\n");
				SpellingTest tester = new SpellingTest(_dataSet, SpellingTest.TestType.NORMAL, txtOutput);
				_quiz = tester;
			}else if(e.getSource()==review){
				SpellingTest tester = new SpellingTest(_dataSet, SpellingTest.TestType.REVIEW, txtOutput);
				_quiz = tester;
			}else if(e.getSource()==clear){//gives user choice when clicking clear.
				int reply = JOptionPane.showConfirmDialog(null,"Are you sure you want to clear stats?","Clear Stats",JOptionPane.YES_NO_OPTION);
				if(reply== JOptionPane.YES_OPTION){
					txtOutput.append("Stats Cleared\n");
					_dataSet.clearStats();
				} else{
					txtOutput.append("Stats not cleared.\n");
				}
			}else if(e.getSource()==stats){
				txtOutput.append("Current statistics (sorted alphabetically):\n");
				txtOutput.append("Mastered	 spelled correctly on the first attempt\n");
				txtOutput.append("Faulted 	 spelled correctly on the second attempt\n");
				txtOutput.append("Failed  	 spelled incorrectly after two attempts\n\n");
				ArrayList<String> output = _dataSet.getStats();//gets strings from WordData file.
				for(String s : output){
					txtOutput.append(s);
				}
			}
		}	
	}

	/**
	 * Function that checks for the serializable object on run time, uses a set name
	 * each time to check for it from a previous instance. Has various error checking
	 * mechanisms to ensure nullPointers are thrown and is robust.
	 */
	private void fileCheck(){
		WordData dataObject = null;
		if(!WORD_DATA_FILE.exists()){//if cant find instance, make one
			dataObject = new WordData(WORD_DATA_FILE);
			File standardFile = new File("wordlist");//A Defaulted option, tries it
			dataObject.updateDataFromFile(standardFile);
			dataObject.saveData();//note will be an "empty" WordData if file wasnt there
		}//will load each time just for uniformity
		WordData loadedData = null;
		try {//will open streams to read in then close them
			FileInputStream fileIn = new FileInputStream(WORD_DATA_FILE);
			ObjectInputStream wordDataIn = new ObjectInputStream(fileIn);
			loadedData = (WordData) wordDataIn.readObject();
			wordDataIn.close();
			fileIn.close();
		} catch (ClassNotFoundException | IOException e) {
			e.printStackTrace();
		}
		_dataSet = loadedData;
		if(_dataSet.isEmpty()){//specify a message on the text field to prompt user, not as intrusive, but will be later.
			txtOutput.append("Default file not found...\nPlease specify a text file from the menu.\n\n");
		}else{
			txtOutput.append("Default file or Previous Save found\n\n");
		}
	}

	/**
	 * Function for give a JFileChoose and accepting the option, then passing it to 
	 * the SpellingTest object to update its word list. Also deals with cancels/nulls.
	 */
	public void getWordFile(){
		File textFile = null;
		JFileChooser chooser = new JFileChooser();
		int returnValue = chooser.showOpenDialog(chooser);
		if(returnValue == JFileChooser.APPROVE_OPTION){
			textFile = chooser.getSelectedFile();
		}
		if(textFile == null){
			txtOutput.append("No new words loaded.");
		}else{
			_dataSet.updateDataFromFile(textFile);
			_dataSet.saveData();
			txtOutput.append("Words loaded in correctly!\nPrevious words removed.\nStatistics have been reset!\n");
		}
	}
	//main method to invoke the GUI
	public static void main(String[] args){
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				SpellingGUI frame = new SpellingGUI();
				frame.setVisible(true);
			}
		});
	}
}