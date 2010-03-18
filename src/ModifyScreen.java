import net.rim.device.api.system.Characters;
import net.rim.device.api.ui.component.EditField;
import net.rim.device.api.ui.container.PopupScreen;
import net.rim.device.api.ui.container.VerticalFieldManager;

public final class ModifyScreen extends PopupScreen{
	private EditField bm;
	public String value;
	public  ModifyScreen(String title) {
		super(new VerticalFieldManager(FIELD_VCENTER | FIELD_HCENTER ), DEFAULT_MENU);
		bm = new EditField(title, "");
		add(bm);
 	}
	
	protected boolean keyChar(char key, int status, int time) {
		if (key == Characters.ENTER || key == Characters.ESCAPE) {
			value = (key == Characters.ENTER) ? bm.getText().trim() : null;
			close();
		}
		return super.keyChar(key, status, time);
	}

}
