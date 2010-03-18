import net.rim.device.api.system.Characters;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.util.Arrays;

public final class HistoryScreen extends TransitionScreen {
	private HistoryList historyList;
	private BookInfo[] bookList;// = null;
	private Record historyRecord ;
	private BookInfo compare;
	
   private MenuItem _openItem = new MenuItem("开始阅读",60,10){
    	public void run() {
    		openBook();
    	}
    };

	private MenuItem _infoItem = new MenuItem("查看详情",80,10){
    	public void run() {
    		showBookInfo();
    	}
    };
    private MenuItem _delOneItem = new MenuItem("删除记录",90,20){
    	public void run() {
    		delRecord();
    	}
    };
    private MenuItem _delAllItem = new MenuItem("清空记录",100,20){
    	public void run() {
    		clearRecord();
    	}
    };

	public HistoryScreen(){
		super(true, VERTICAL_SCROLL | VERTICAL_SCROLLBAR);   // no transition effect             
		setTitle("GBBReader - 最近阅读");
		addMenuItem(_openItem);
		addMenuItem(_infoItem);
		addMenuItem(_delOneItem);
		addMenuItem(_delAllItem);

        historyList = new HistoryList();
		compare = new BookInfo();
        updateList();
        add(historyList);
 	}
	
	private void updateList() {
		bookList = null;
		historyRecord = new Record(Record.HISTORY_NAME);
		bookList = historyRecord.loadHistoryList();
		if (bookList != null)
			Arrays.sort(bookList, compare);
		historyList.set(bookList);
		historyRecord = null;
	}
	
	//删除选中记录
	private void delRecord() {
		if (historyList.getSize() > 0) { 
			int i = historyList.getSelectedIndex();

			historyRecord = new Record(Record.HISTORY_NAME);
			historyRecord.deleteRecord(bookList[i].bookID);
			historyRecord = null;

			int t = bookList[i].bookName.lastIndexOf('.');
			String s = bookList[i].bookName.substring(0, t);
			historyRecord = new Record(Record.BOOKMARK_NAME);
			historyRecord.deleteBookMark(s);
			historyRecord = null;

			updateList();
			if (i == historyList.getSize())
				i--;
			historyList.setSelectedIndex(i);
		}
	}
	
	//清空所有历史记录
	private void clearRecord() {
		if (Dialog.ask(Dialog.D_DELETE, "清除全部记录?") == Dialog.DELETE) { 
			historyRecord = new Record(Record.HISTORY_NAME);
			for (int i = 0; i < bookList.length; i++) 
				historyRecord.deleteRecord(bookList[i].bookID);
			historyRecord = null;
			bookList = null;
			historyList.set(null);

			//删除所有书签记录
			historyRecord = new Record(Record.BOOKMARK_NAME);
			historyRecord.delAllBookMark();
			historyRecord = null;
		}
	}

	//显示书籍详细信息(文件大小及进度)
	private void showBookInfo() {
		int i = historyList.getSelectedIndex();
		String bookName = bookList[i].bookFullName;
		BookInfoDialog bid = new BookInfoDialog(bookName, null);
		UiApplication.getUiApplication().pushModalScreen(bid);
		bid = null;
		bookName = null;
	}
	
	//打开选中书
	private void openBook() {
		if (historyList.getSize() > 0) {
	    	int i = historyList.getSelectedIndex();
	    	String name = bookList[i].bookFullName;
			if (Setting.bookExists(name)) {
				TextScreen bookScreen = new TextScreen(name);
				UiApplication.getUiApplication().pushModalScreen(bookScreen);
				bookScreen = null;
				updateList();
			}
		    else 
				Dialog.alert("文件不存在!");
		}
	}
	
    protected boolean navigationClick(int status, int time) {
		if(!GBBMain.isTrackWheel) {
	    	openBook();
			return true;
    	}
		return super.navigationClick(status, time);
    }
    
	public boolean onClose() {
		bookList = null;
		historyRecord = null;
		return super.onClose();
	}
	
	protected boolean keyChar(char key, int status, int time)
    {	
    	switch(key) {
    		case Characters.BACKSPACE:
    			delRecord();
     			return true;
    		case Characters.ENTER :
    		case Characters.SPACE :
			case Characters.LATIN_SMALL_LETTER_J:
			case Characters.LATIN_SMALL_LETTER_D:
    			openBook();
     			return true;
     	}
    	return super.keyChar(key, status, time);
    }

// class GBBMainScreen ends
}

