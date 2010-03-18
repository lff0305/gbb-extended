import net.rim.device.api.system.Characters;
import net.rim.device.api.ui.component.ObjectListField;

public class MyObjectListField extends ObjectListField {

	public MyObjectListField(long style) {
		super(style);
	}
	
	public void onExposed() {
		super.onExposed();
		invalidate();
	}

	protected boolean keyChar(char key, int status, int time)
    {	
		int i = getSelectedIndex();
		int m = getSize() - 1;
    	switch(key) {
			case Characters.LATIN_SMALL_LETTER_X:
			case Characters.LATIN_SMALL_LETTER_N:
				int p;
				if (Setting.cycle > 0)
					p = (i == m) ? 0 : i + 1;
				else
					p = (i < m) ? i + 1 : m; 
				setSelectedIndex(p);
				invalidate(i);
				invalidate(p);
				return true;
			case Characters.LATIN_SMALL_LETTER_U:
			case Characters.LATIN_SMALL_LETTER_E:
				int t;
				if (Setting.cycle > 0)
					t = (i == 0) ? m : i - 1;
				else
					t = (i > 0) ? i - 1 : 0;
				setSelectedIndex(t);
				invalidate(i);
				invalidate(t);
				return true;
			case Characters.LATIN_SMALL_LETTER_T:
				setSelectedIndex(0);
				invalidate(i);
				invalidate(0);
				return true;
			case Characters.LATIN_SMALL_LETTER_B:
				setSelectedIndex(m);
				invalidate(i);
				invalidate(m); 
				return true;
      	}
    	return super.keyChar(key, status, time);
    }
	
    protected boolean navigationMovement(int dx,int dy,int status,int time) {
    	if (Setting.cycle > 0) {
	    	int i = getSelectedIndex();
	    	int m = getSize() - 1;
	    	if ((dy > 0) && (i == m)) { 
	    		setSelectedIndex(0);
				invalidate(0);
				invalidate(i);
				return true; 
	    	}
	    	else if ((dy < 0) && (i == 0)) {
	    		setSelectedIndex(m);
				invalidate(m);
				invalidate(i);
	    		return true;
	    	}
    	}
   		return super.navigationMovement(dx, dy, status, time);
    }
}
