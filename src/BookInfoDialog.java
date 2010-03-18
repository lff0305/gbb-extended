import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.container.PopupScreen;
import net.rim.device.api.ui.container.VerticalFieldManager;


public final class BookInfoDialog extends PopupScreen{
	
	public  BookInfoDialog(String bookFullName, String format) {
		super(new VerticalFieldManager(FIELD_VCENTER | FIELD_HCENTER ));
		
		int i = bookFullName.lastIndexOf('/');
		String bookPath = "Â·¾¶: " + bookFullName.substring(7, i);
		String bookName =  "ÊéÃû: " + bookFullName.substring(i + 1);
		add(new LabelField(bookName));
		add(new LabelField(bookPath));
 	}
	
    protected boolean navigationClick(int status, int time) {
    	close();
    	return true;
    }

    public boolean keyChar(char c, int status, int time) {
   		close();
    	return super.keyChar(c, status, time);
    }

}
