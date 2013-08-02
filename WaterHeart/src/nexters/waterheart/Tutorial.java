package nexters.waterheart;

import android.app.Activity;
import android.app.Service;
import android.graphics.PixelFormat;
import android.view.WindowManager;
import android.widget.ViewFlipper;

public class Tutorial {
	ViewFlipper tutorial;
	WindowManager wm;
	Activity mActivity;
	public ViewFlipper getTutorial(int which, Activity activity){ //0:MainFragment 1:StampFragment 2:HistoryFragment
		mActivity=activity;
		wm=(WindowManager)mActivity.getSystemService(Service.WINDOW_SERVICE);
		switch(which){
		case 0:
			tutorial = (ViewFlipper)mActivity.getLayoutInflater().inflate(R.layout.tutorial, null);
			break;
		case 1:
			break;
		case 2:
			break;
		}
		return tutorial;
	}
	
	/*
	 * 액티비티의 화면이 아니라 윈도우 사이즈 전체를 width, height로 지정하여 튜토리얼이 모든 화면을
	 * 덮도록 함.
	 * FLAG_NOT_FOCUSABLE 플래그로 키이벤트는 받지 않도록 하며
	 * TYPE_PHONE으로 최상단 뷰에 배치해둠과 동시에 터치이벤트를 받을 수 있게 함
	 * TYPE_PHONE을 위해 USES-PERMISSION 등록함
	 */
	public void showTutorial(){
		WindowManager.LayoutParams param = new WindowManager.LayoutParams();
		param.x=0; param.y=0;
		param.width=WindowManager.LayoutParams.MATCH_PARENT;
		param.height=WindowManager.LayoutParams.MATCH_PARENT;
		param.flags=WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN|
				WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
		param.type=WindowManager.LayoutParams.TYPE_PHONE;
		param.format=PixelFormat.TRANSLUCENT;
		wm.addView(tutorial, param);
	}
	
	public void showNext(){
		tutorial.showNext();
	}
	
	public void finishTutorial(){
		//튜토리얼이 끝났을 때 viewflipper를 다시 처음상태를 보여주도록하고
		//windowmanager에서 뷰를 없앤다.
		tutorial.setDisplayedChild(0);
		if(wm!=null)
		wm.removeView(tutorial);
	}
}