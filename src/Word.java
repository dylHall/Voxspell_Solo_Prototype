package spellingGUI;
import java.io.Serializable;

//Author Dylan Hall, dhal525
//This class represents a word type, that has a string representing the word, and 
//3 numbers representing its current stats for this current word.
//It implements Comparable<Word>, to allow for simple alphabetical sorting (stats)
//Also overrides equals method, another comparison type functionality
//It is also serializable, so that it can be saved along with a WordData object.
public class Word implements Serializable, Comparable<Word>{
	
	protected static final long serialVersionUID = 2699338661992190639L;
	protected String _wordKey;
	protected int _mastered=0;
	protected int _faulted=0;
	protected int _failed=0;

	public void setWordKey(String wordKey){
		_wordKey = wordKey;
	}
	
	/**
	 * Method to check if it has at least a single field as 1, if all 0, shouldn't
	 * come up on stats.
	 * @return true representing that it has all 0s, false meaning has at least 1 test on it.
	 */
	public boolean notTested(){
		if(_mastered==0&&_faulted==0&&_failed==0){
			return true;
		}else{
			return false;
		}
	}
	
	/**
	 * Overrides Object#equals(Object), allowing it to be used in .contains() while
	 * in a hashmap, useful functionality that reduces logic needed.
	 * @return true if the objects have same STRING represeting the word - doesn't care
	 * about the stats.
	 */
	@Override
	public boolean equals(Object obj){
		String otherWordValue = ((Word) obj)._wordKey;
		if(otherWordValue.equals(this._wordKey)){
			return true;
		} else{
			return false;
		}
	}
	
	//Getter//incrementer type methods for use on these objects.
	public String getWordKey(){
		return _wordKey;
	}
	
	public void incrementMaster(){
		_mastered++;
	}
	
	public void incrementFaulted(){
		_faulted++;
	}
	
	public void incrementFailed(){
		_failed++;
	}
	
	public int returnMaster(){
		return _mastered;
	}
	
	public int returnFault(){
		return _faulted;
	}
	
	public int returnFail(){
		return _failed;
	}
	
	/**
	 * Resets stats all to zero, so don't have to delete and remake word each time.
	 */
	public void resetStats(){
		_mastered=0;
		_faulted=0;
		_failed=0;
	}

	/**
	 * Ability to sort based on word ordering, simply uses the ordering given by
	 * String. Natural ordering. But must be utilized to allow for Collections.sort()
	 * used in WordData.s
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(Word other) {
		return _wordKey.compareTo(other._wordKey);
	}
}
