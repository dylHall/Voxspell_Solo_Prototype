package spellingGUI;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JTextArea;
//Author Dylan Hall, dhal525
//This class deals with the logic for beginning spelling tests and recieving inputs
//from the JTextField
public class SpellingTest implements ActionListener{

	enum TestType{NORMAL,REVIEW};//enum defined in here for use for switch statements, public.
	private WordData _dataSet;//word data set to use.
	private JTextArea _txtOutput;//text field to write to.
	//Fields all for allowing logic to be stored for later
	private ArrayList<String> _testingWords;
	private TestType _type; 
	private int _wordNumber;
	private String _currentWord;
	private Boolean _wrongFirstAttempt;//checking for faulted/failed words
	private boolean _stopped;//boolean for when need to stop responding to events.
	private int _correct=0;

	public SpellingTest(WordData data, TestType type, JTextArea txtOutput){
		_stopped = false;
		_txtOutput = txtOutput;
		_dataSet = data;
		_type = type;
		_wordNumber =0;
		_wrongFirstAttempt = false;
		_testingWords = new ArrayList<String>();
		switch(type){//switches to get different words, but still uses same list.
		case NORMAL:
			ArrayList<Word> randomWords = _dataSet.getRandomWordsNormal();
			for(Word word : randomWords){
				_testingWords.add(word.getWordKey());
			}//now have all the tester words.
			break;
		case REVIEW:
			ArrayList<Word> randomWordsReview = _dataSet.getRandomWordsReview();
			for(Word word : randomWordsReview){
				_testingWords.add(word.getWordKey());
			}
			break;
		}
		if(_testingWords.size()==0){//error check just in case somehow reaches this point.
			_stopped = true;
			txtOutput.append("No words avaliable!");
		}else{
			communicateWord();
		}
	}

	/**
	 * Method for speaking word, makes two different strings then gets festival to speak
	 */
	public void communicateWord(){
		_currentWord = _testingWords.get(_wordNumber);
		String speak = null;
		if(_wrongFirstAttempt==true){
			if(_type.equals(TestType.REVIEW)){
				speak = "echo \"Spell ";
				char[] wordArray = _currentWord.toCharArray();
				String segmentWord = "";
				for(char c : wordArray){
					segmentWord = segmentWord+c+".. ";
				}
				speak = speak+segmentWord+" \" | festival --tts";
			} else{
				speak = "echo \"Spell "+_currentWord+" .. "+_currentWord+" \" | festival --tts";
			}
			_txtOutput.append("Try spell it again...");
		}else{
			speak = "echo \"Spell "+_currentWord+"\" | festival --tts";
			_txtOutput.append("Spell word "+(_wordNumber+1)+" of "+_testingWords.size()+"\n");
		}
		speakConcurrent(speak);//passes to method in this class.
	}

	/**
	 * Function to increment and check if the logic needs to stop.
	 */
	public void nextWord(){
		_wordNumber++;
		if(_wordNumber==_testingWords.size()){//terminate flow.
			_stopped = true;
			_txtOutput.append("\nQuiz finished, you spelt "+_correct+" word(s) correct on the first attempt!\n\nPlease choose another option");
		}else{
			communicateWord();//calls the next word to be spelled.
		}
	}

	/**
	 * First method called to check if the user input from textfield was correct.
	 * Logic flows to next method if was incorrect (different talking/logic_
	 * @param userInput String corresponding to what the user input.
	 */
	public void checkInput(String userInput){
		if(_wrongFirstAttempt==true){
			checkFault(userInput);//next flow.
		}else{
			if(userInput.equals(_currentWord)){
				_correct++;
				_dataSet.updateWord(_currentWord, Category.MASTERED);
				if(_type.equals(TestType.REVIEW)){//additional logic if review.
					_dataSet.removeFromFailedList(_currentWord);
				}
				_txtOutput.append("Correct!!\n");
				String speak = "echo \"Correct\" | festival --tts";
				speakBlock(speak);
				nextWord();//increments

			}else{//if incorrect, set a flag to be true, will flow to other method.
				//does not increment or update word.
				_txtOutput.append("Incorrect\n");
				String speak = "echo \"Incorrect\" | festival --tts";
				speakBlock(speak);
				_wrongFirstAttempt=true;
				communicateWord();
			}
		}
	}

	/**
	 * Method for secondary checking, then puts on faulted/failed list but MUST
	 * be one or the other as have failed word once.
	 * @param userInput String corresponding to what the user input.
	 */
	public void checkFault(String userInput){
		if(userInput.equals(_currentWord)){
			_dataSet.updateWord(_currentWord, Category.FAULTED);
			if(_type.equals(TestType.REVIEW)){//still removes if faulted.
				_dataSet.removeFromFailedList(_currentWord);
			}
			_txtOutput.append("Correct!!\n");
			String speak = "echo \"Correct\" | festival --tts";
			speakBlock(speak);
		}else{//failed, will be added to fail list while updating word.
			_dataSet.updateWord(_currentWord, Category.FAILED);
			_txtOutput.append("Incorrect\n");
			String speak = "echo \"Incorrect\" | festival --tts";
			speakBlock(speak);
		}
		_wrongFirstAttempt=false;//always set back to false, anticipating new word.
		nextWord();//increment now.
	}

	/**
	 * Method for speaking words using festival process
	 * @param speak - string corresponding to words to be spoken.
	 */
	private void speakBlock(String speak){
		ProcessBuilder builder = new ProcessBuilder("/bin/bash", "-c",speak);
		try{
			Process process = builder.start();
			process.waitFor();
			//process.waitFor();
		}catch (Exception ex){

		}
	}

	/**
	 * Method for speaking words using festival process on another thread.
	 * @param speak - string corresponding to words to be spoken.
	 */
	private void speakConcurrent(String speak){
		ProcessBuilder builder = new ProcessBuilder("/bin/bash", "-c",speak);
		try{
			@SuppressWarnings("unused")//cause not used in wait for
			Process process = builder.start();
		}catch (Exception ex){

		}
	}

	/**
	 * Listens to event fired from JTextField, unpacks the string from it, then
	 * uses that as "userInput" for other functions. Other functional flow will
	 * check if input correct or not
	 * will not do this process if quiz has "stopped" (shown by boolean)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		if(_stopped==false){
			String userInput = e.getActionCommand();
			userInput = userInput.toLowerCase();
			checkInput(userInput);
		}
	}
}
