import net.rim.device.api.system.Bitmap;
import net.rim.device.api.ui.Color;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.component.ListField;
import net.rim.device.api.ui.component.ListFieldCallback;

public class HistoryList extends MyObjectListField implements ListFieldCallback 
{
	private Bitmap smallicon = Bitmap.getBitmapResource("smallicon.png");    
	public HistoryList() {
		super(ELLIPSIS);
		Font f = getFont();
		setRowHeight(f.getHeight() + 8);
	}

    public void drawListRow(ListField list, Graphics g, int index, int y, int w) {
        String row = get(list,index).toString();
        
     	int t = list.getRowHeight() ;
	   	int y1 = y + (t - 20) / 2;
	   	g.drawBitmap(2, y1, 20, 20, (smallicon), 0, 60); //画图标
     	
     	int y2 = (t - g.getFont().getHeight())/2 + y;
       	g.drawText(row, 25, y2, 0, w); // 显示书名
       	
       	int c = g.getColor();
     	int r = t + y - 1 ;
      	g.setColor(Color.LIGHTGREY);
       	g.drawLine(0, r, w, r);	//画底线 
       	g.setColor(c);
    }
}