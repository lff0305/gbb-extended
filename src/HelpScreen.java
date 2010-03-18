import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.NullField;
import net.rim.device.api.ui.component.SeparatorField;

public final class HelpScreen extends TransitionScreen {
	public HelpScreen(boolean t){
        super(t, VERTICAL_SCROLL | VERTICAL_SCROLLBAR);               
		setTitle("GBBReader - 帮助");
        
		add(new NullField(FOCUSABLE)); // 为了让内容能滚动
        String s =  "阅读时按键:\n上/下翻页: H、S/空格、回车、F、K \n上/下翻行: E、U/X、N \n" +
        			"显示时间及进度: C  字体放大/缩小: Y/I\n设置: O  查找: V  背光长亮: P\n跳转: Z  全屏: D/J  反色: 0\n" + 
        			"模式切换: T  添加书签: A  打开书签：L\n退出: Q  返回: R  帮助: M\n";
        add(new LabelField(s, NON_FOCUSABLE));
        add(new SeparatorField());
        s = "窗口中按键:\n上下移动: E、U/X、N\n确定: 空格/回车/D/J \n跳至首/尾: T/B\n返回: R\n";
        add(new LabelField(s, NON_FOCUSABLE));
        add(new SeparatorField());
        add(new LabelField("逐行滚动/图片背景更耗电！", NON_FOCUSABLE));
        add(new NullField(FOCUSABLE)); // 为了让内容能滚动
	}

// class GBBMainScreen ends
}
