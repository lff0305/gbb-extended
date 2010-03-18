import net.rim.device.api.system.Characters;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.container.PopupScreen;
import net.rim.device.api.ui.container.VerticalFieldManager;
import net.rim.device.api.ui.text.NumericTextFilter;
import net.rim.device.api.util.CharacterUtilities;

public final class JumpScreen extends PopupScreen{
	private int value;
	private StringBuffer data;
	private LabelField percentField ;
	private NumericTextFilter ntf;

	public  JumpScreen(int offset, int size) {
		super(new VerticalFieldManager(FIELD_VCENTER | FIELD_HCENTER ));
		VerticalFieldManager vfm = (VerticalFieldManager)this.getDelegate();
		
		ntf = new NumericTextFilter(NumericTextFilter.ALLOW_DECIMAL);
		data = new StringBuffer();
		percentField = new LabelField("百分比(%):" + data.toString(), NON_FOCUSABLE);
		value = -1; 

		vfm.add(new LabelField(InfoScreen.calProgress(offset, size), NON_FOCUSABLE));
		vfm.add(percentField);
	}

	/**
	 * 返回跳转比例
	 * @return - 跳转比例
	 */
	public int getValue() {
		return value;
	}
	
	/**
	 * 设置跳转比例
	 */
	public void setValue() {
		String s = data.toString();
		if (s.length() == 0)
			value = -1;
		else if (s.length() == 3)
			value = Integer.parseInt(s.substring(0, 2)) * 100;
		else 
			value = (int) (Float.parseFloat(s) * 100);
		close();
	}
	
	/**
	 * 设置跳转比例为-1,表明无需跳转
	 */
	private void cancel() {
		value = -1;
		data = null;
		close();
	}
	
	private void update() {
		percentField.setText("百分比(%):" + data.toString());
	}
	
	protected boolean keyChar(char key, int status, int time) {
		int len = data.length();
		
		if (key == Characters.ENTER) {
			setValue();
			close();
		}
		else if (key == Characters.ESCAPE) 
			cancel();
		
		else if (key != Characters.BACKSPACE) {
			if (len == 5)
				return true;
			else if (len == 2) {
				data.append('.');
				update();
				return true;
			}
		}

		switch(key) {
			case Characters.BACKSPACE:
				if (len > 0) 
					data.deleteCharAt(len - 1);
				break;
			default:
				char newkey = ntf.convert(key, status);
				if (CharacterUtilities.isDigit(newkey))
					data.append(newkey);
				break;
		}
		update();
		return true;
	}
}
