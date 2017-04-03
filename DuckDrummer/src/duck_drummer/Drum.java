/**
 * AutoDuck Drummer by Ronan Hanley
 * Feb. 2016
 */

package duck_drummer;

import java.awt.event.KeyEvent;

public class Drum {
	private String name;
	private int keyID;
	private int index;
	
	public Drum(String name, int keyID, int index) {
		this.name = name;
		this.keyID = keyID;
		this.index = index;
	}
	
	public String getName() {
		return name;
	}
	
	public String toString() {
		return name;
	}
	
	public int getKeyID() {
		return keyID;
	}
	
	public void setKeyId(int newID) {
		keyID = newID;
	}
	
	public String getAssignedKey() {
		return KeyEvent.getKeyText(keyID);
	}
	
	public int getIndex() {
		return index;
	}
	
}
