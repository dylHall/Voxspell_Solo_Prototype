package spellingGUI;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Set;
//Author Dylan Hall, dhal525
//This class represents all the data held by the program. Contains both the
//failed list and current stats. It is serializable so that it can be saved
//and reopened later.
public class WordData implements Serializable{

	private static final long serialVersionUID = 3333461596000156257L;
	private ArrayList<Word> _failedList;//Refer to word class for info on that object.
	private HashMap<String,Word> _statsData;
	private File _name;
	private boolean _emptyFlag; //flag for isEmpty() - robustness.

	public WordData(File name){
		_name = name;
		_emptyFlag=false;
	}

	/**
	 * This method is responsible for establishing the internal word list that the
	 * program uses to test. Note on specifying a new set of words/file, it will
	 * erase the previous statistics (by making new instances of the lists). Mainly
	 * for preventing errors of duplicates.
	 * @param textFile - a file passed to it to parse from and set new words
	 */
	public void updateDataFromFile(File textFile){
		if(!textFile.exists()){
			_emptyFlag = true;//sets flag then stops method, GUI will do checks on it and prompt user.
		}else{
			try{
				//make new data each time.
				_statsData = new HashMap<String,Word>();
				_failedList = new ArrayList<Word>();
				_emptyFlag=false;
				BufferedReader wordListRead = new BufferedReader(new FileReader(textFile));
				ArrayList<String> inputList = new ArrayList<String>();
				String currentLine;
				//Obtained list of words to the iterate through, collecting stats.
				while((currentLine=wordListRead.readLine()) != null){
					inputList.add(currentLine);
				}
				wordListRead.close();
				//this gets all the words into the stats list - all 0'd, but
				//shouldn't add if already has a word value for it.
				for(String inputWord : inputList){
					if(!_statsData.containsKey(inputWord)&&!inputWord.equals("")){
						Word newWord = new Word();//Word Type holds necessary data.
						newWord.setWordKey(inputWord);
						_statsData.put(inputWord, newWord);//uses string to look up in list.
					}
				}	
			}catch (IOException e) {
			}
		}
	}
	
	/**
	 * Simple method for when the object is queried.
	 * @return boolean, true corrseponding to no current internal word list file
	 */
	public boolean isEmpty(){
		return _emptyFlag;
	}

	/**
	 * Logic for updating a certain word based on how it was spelt in test.
	 * Uses enum for switch statement capabilities.
	 * @param wordKey - string corresponding to word spelt 
	 * @param category - enum representing on what attempt it was spelt correctly,
	 * will determine what the stats show.
	 */
	public void updateWord(String wordKey, Category category){
		Word currentWord = _statsData.get(wordKey);
		switch(category) {//uses methods in the Word class to increment that object.
		case MASTERED:
			currentWord.incrementMaster();
			break;
		case FAULTED:
			currentWord.incrementFaulted();
			break;
		case FAILED:
			currentWord.incrementFailed();
			_failedList.add(currentWord);//also adds word to the failed list here.
			break;
		}
		this.saveData();//saves after every change, this is in case user closes
		//application whenever.
	}

	/**
	 * Method to remove from failed list, will have to have been on the failed list to 
	 * remove it. This is due to the constant saving mechanic ensuring that after 
	 * test it is removed and saved simultaneously
	 */
	public void removeFromFailedList(String wordKey){
		Word currentWord = _statsData.get(wordKey);
		_failedList.remove(currentWord);
		this.saveData();//to ensure its working each time.
	}

	/**
	 * Random words from word list, could be less if internal word list is small.
	 * @return List containing 3 shuffled words, just have to increment through them
	 */
	public ArrayList<Word> getRandomWordsNormal(){
		ArrayList<Word> wordList = this.getWordsInList();//private method to get list.
		Collections.shuffle(wordList);//shuffles for randomneess
		ArrayList<Word> returnList = new ArrayList<Word>();
		int repeat;
		if(wordList.size()>3){//error checking just in case.
			repeat=3;
		}else{
			repeat=wordList.size();
		}
		for(int i=0;i<repeat;i++){
			returnList.add(wordList.get(i));
		}
		return returnList;
	}

	/**
	 * This method returns from the failed list in shuffled order.
	 * @return shuffled list of AT MOST 3, could be less depending on how many failed
	 * words currently in list.
	 */
	public ArrayList<Word> getRandomWordsReview(){
		Collections.shuffle(_failedList);
		if(_failedList.size()>3){//iterate through the shuffled list.
			ArrayList<Word> returnList = new ArrayList<Word>();
			for(int i=0;i<3;i++){
				returnList.add(_failedList.get(i));
			}
			return returnList;
		} else{//will be 3 or less.
			return _failedList;
		}
	}

	/**
	 * This method returns strings to print on the GUI. The ordering of the string
	 * is based on alphabetical ordering. This is due to Word implementing comparable<Word?
	 * based on string ordering.
	 * @return List of string to print on the GUI.
	 */
	public ArrayList<String> getStats(){
		ArrayList<Word> wordList = this.getWordsInList();
		Collections.sort(wordList);//works as Word implements comparable<Word> - so can compare the two to sort 
		ArrayList<String> outputStats = new ArrayList<String>();
		for(Word word : wordList){
			if(!word.notTested()){//checks that word has AT LEAST 1 field not 0, if not skip.
				int mastered = word.returnMaster();
				int faulted = word.returnFault();
				int failed = word.returnFail();
				String output = word.getWordKey()+":	mastered:"+mastered+"    faulted:"+faulted+"    failed:"+failed+"\n";
				outputStats.add(output);//gets string and adds to list
			}
		}
		if(outputStats.size()==0){//always returns list, so might as well return the zero message as a string
			String output = "No current statistics to display!";
			outputStats.add(output);
		}
		return outputStats;
	}
	
	/**
	 * Method associated with saving this object to disk, will have a name field to ensure
	 * its always saved the same way.
	 */
	public void saveData(){
		try{
			ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(_name));
			out.writeObject(this);
			out.close();
		}catch (IOException rip){
		}
	}
	
	/**
	 * This method is used to reset all the words within the stats, but doesn't change the 
	 * default words used. Simply zeroes all the word objects with a method defined in that class
	 */
	public void clearStats(){
		Set<String> wordSet = _statsData.keySet();
		for(String currentWordKey : wordSet){
			Word currentWord = _statsData.get(currentWordKey);
			currentWord.resetStats();
			_statsData.put(currentWordKey, currentWord);
		}
		saveData();
	}
	
	/**
	 * Simple private method to return the map as a list of word objects.
	 * @return list containing all word objects.
	 */
	private ArrayList<Word> getWordsInList(){
		Collection<Word> wordCollection =  _statsData.values();//has to get collection first.
		ArrayList<Word> wordList = new ArrayList<Word>();
		for(Word word : wordCollection){
			wordList.add(word);
		}
		return wordList;
	}
}
