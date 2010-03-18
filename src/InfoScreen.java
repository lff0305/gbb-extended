import java.util.Calendar;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.container.PopupScreen;
import net.rim.device.api.ui.container.VerticalFieldManager;

// 弹出窗口，显示当前时间和阅读进度
public final class InfoScreen extends PopupScreen{
	
	public  InfoScreen(int offset, int size) {
		super(new VerticalFieldManager(FIELD_VCENTER | FIELD_HCENTER ));

		//得到时间
		Calendar now = Calendar.getInstance();
		int hh = now.get(Calendar.HOUR_OF_DAY);
		int mm = now.get(Calendar.MINUTE);
		now = null;
		StringBuffer time = new StringBuffer("当前时间: ");
		if (hh < 10)
			time.append(0);
		time.append(hh).append(':');
		if (mm < 10)
			time.append(0);
		time.append(mm);

		add(new LabelField(time));
		add(new LabelField(calProgress(offset, size)));
		add(new LabelField("文件大小: " + String.valueOf(size /1024) + "KB"));
 	}

	/**
	 * 计算进度并返回格式化后的字符串
	 * @param offset : 偏移量
	 * @param size : 文件大小
	 * @return : 格式化后的进度百分比字符串 
	 */
	public static String calProgress(int offset, int size) {
		StringBuffer ps = new StringBuffer("当前进度: ");
		if (size > 0) {
			long p = ((long)offset *  10000) / size;
			if (p < 10) 
				ps.append("00.0").append(p);
			else if (p < 100)
				ps.append("00.").append(p);
			else if (p < 9999) 
				ps.append(p / 100).append('.').append(p % 100);
			else if (p == 10000) 
				ps.append(100);
			ps.append('%');
		}
		return ps.toString();
	}
	
    protected boolean navigationClick(int status, int time) {
    	close();
    	return true;
    }

    public boolean keyChar(char c, int status, int time) {
   		close();
    	return true;
    }

}
