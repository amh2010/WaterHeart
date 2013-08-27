package nexters.waterheart;

import java.io.FileInputStream;

import android.annotation.SuppressLint;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.TouchDelegate;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.nineoldandroids.view.ViewHelper;

public class MainFragment extends SherlockFragment {

	View mainView;
	HeartManager heartManager;
	ViewFlipper tutorialFlipper;
	TutorialManager tutorial;
	CupManager cupManager;
	View main_heart;
	ImageView[] cups;
	ImageView undo;
	ClickManager clickManager;
	Animation toastAni;
	TextView toastText;
	static String yourName;

	TextView heartTextPercent;
	TextView heartTextML;

	private static final int TUTORIAL_NUMBER = 0;
	private static final int CUP_ONE = 0, CUP_TWO = 1, CUP_THREE = 2,
			CUP_FOUR = 3;
	private static final int ONCLICK_NUM = 0;
	private static final int FROM_CUPCUSTOM = 10;
	private static final int FROM_CUSTOM = 11;

	static int totalWater = 2000;
	float valueA = totalWater / 20; // 제일 작은 조각 한개의 용량
	float valueB = totalWater / 20; // 중간 크기 조각 한개의 용량
	float valueC = totalWater / 5; // 제일 큰 조각 한개의 용량
	int water;
	float[] originalValue = new float[14];
	float[] currentValue = new float[14];
	float[] eachWater = new float[14];
	ImageView[] heartImg = new ImageView[14];

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		/*
		 * setHasOptionsMenu(true)가 지정되어야만 fragment내에서 액션바 메뉴를 설정할 수 있다
		 */
		setHasOptionsMenu(true);
		mainView = inflater.inflate(R.layout.mainview, container, false);
		clickManager = new ClickManager(ONCLICK_NUM, getActivity(),
				fillWaterHandler);
		cupManager = new CupManager(getActivity());
		tutorial = new TutorialManager();
		toastText = (TextView) mainView.findViewById(R.id.toastText);
		toastAni = AnimationUtils.loadAnimation(getSherlockActivity(),
				R.anim.text_show);
		return mainView;
	}

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		init();

		super.onResume();
	}

	public void init() {
		if (main_heart == null) {
			main_heart = getActivity().findViewById(R.id.main_heart_layout);
			undo = (ImageView) getActivity().findViewById(R.id.main_undo);
			expandTouchArea(mainView, undo, 50);
			// 그 외 imageview들을 다 여기서 객체화
			heartTextPercent = (TextView) getActivity().findViewById(
					R.id.main_heart_percent);
			heartTextML = (TextView) getActivity().findViewById(
					R.id.main_heart_ml);
			cups = new ImageView[] {
					(ImageView) getActivity().findViewById(R.id.main_cup_drop),
					(ImageView) getActivity()
							.findViewById(R.id.main_cup_bottle),
					(ImageView) getActivity().findViewById(R.id.main_cup_cup),
					(ImageView) getActivity()
							.findViewById(R.id.main_cup_coffee) };
			cups[0].setImageResource(cupManager.cup_one_image);
			cups[1].setImageResource(cupManager.cup_two_image);
			cups[2].setImageResource(cupManager.cup_three_image);
			cups[3].setImageResource(cupManager.cup_four_image);
			main_heart.setOnClickListener(clickManager);
			undo.setOnClickListener(clickManager);
			for (int i = 0; i < 4; i++) {
				cups[i].setOnClickListener(clickManager);
				cups[i].setOnLongClickListener(longClick);
			}
			try {
				FileInputStream fis = getActivity().openFileInput("name.txt");
				byte[] data = new byte[fis.available()];
				while (fis.read(data) != -1) {
					;
				}
				yourName = new String(data);
				fis.close();
			} catch (Exception e) {
				yourName = "";
			}
			// 이 밑으로는 다 init()에서 희조가 추가한 부분
			// 하트 이미지뷰설정 및 하트 조각별 용량 설정, 그리고 초기 투명도 10퍼로 설정
			for (int i = 0; i < 14; i++) {
				heartImg[i] = (ImageView) getActivity().findViewById(
						R.id.main_heart01 + i);

				ViewHelper.setAlpha(heartImg[i], 0.05f);
			}
			// 여기는 일단 임시로
			// totalWater 가 2000으로 고정됬을거라고 가정했을 시:
			originalValue[0] = 80;
			originalValue[1] = 120;
			originalValue[2] = 60;
			originalValue[3] = 140;
			originalValue[4] = 75;
			originalValue[5] = 125;
			originalValue[6] = 90;
			originalValue[7] = 110;
			originalValue[8] = 85;
			originalValue[9] = 115;
			originalValue[10] = 50;
			originalValue[11] = 150;
			originalValue[12] = 450;
			originalValue[13] = 350;

			heartManager = new HeartManager(getActivity());
			heartManager.init();
		}

		water = heartManager.mainHeartShow();
		heartTextML.setText(String.valueOf(water));
		heartTextPercent.setText(String.valueOf((int) ((float) water
				/ totalWater * 100)));
		// 여기서 heartLogic()을 호출해야한다!!
		heartLogic();
		// heartLogic(heartWater);
	}

	public static void expandTouchArea(final View bigView,
			final View smallView, final int extraPadding) {
		bigView.post(new Runnable() {
			@Override
			public void run() {
				Rect rect = new Rect();
				smallView.getHitRect(rect);
				rect.top -= extraPadding;
				rect.left -= extraPadding;
				rect.right += extraPadding;
				rect.bottom += extraPadding;
				bigView.setTouchDelegate(new TouchDelegate(rect, smallView));
			}
		});
	}

	/*
	 * ClickManager에서 db를통해 물의 총량을 받아오면 그 물의 양을 이 핸들러로 넘겨주고 여기서 하트에 물을 채운다.
	 */
	Handler fillWaterHandler = new Handler() {
		String toastString = "";

		public void handleMessage(Message msg) {

			if (msg.what == FROM_CUPCUSTOM) {
				getActivity().findViewById(R.id.pager_title_strip)
						.setVisibility(View.VISIBLE);
				getActivity().findViewById(R.id.main_undo).setVisibility(
						View.VISIBLE);
				for (int i = 0; i < 4; i++) {
					cups[i].setOnClickListener(clickManager);
					cups[i].setOnLongClickListener(longClick);
				}
				if (msg.arg2 == 1) {
					toastText.setText("설정 완료! 컵의 용량이 " + msg.arg1
							+ " ml로 변경되었습니다!");
					toastText.startAnimation(toastAni);
				}
			} else if (msg.what == FROM_CUSTOM) {
				getActivity().findViewById(R.id.pager_title_strip)
						.setVisibility(View.VISIBLE);
				getActivity().findViewById(R.id.main_undo).setVisibility(
						View.VISIBLE);
				if (msg.arg1 == 1) {
					toastText.setText("정보 입력 완료");
					toastText.startAnimation(toastAni);
					try {
						FileInputStream fis = getActivity().openFileInput(
								"name.txt");
						byte[] data = new byte[fis.available()];
						while (fis.read(data) != -1) {
							;
						}
						yourName = new String(data);
						fis.close();
					} catch (Exception e) {
						yourName = "";
					}
				}

			} else {
				cupManager.getAllCupStates();
				// Toast.makeText(getSherlockActivity(), "" + msg.arg1, 1000)
				// .show();

				switch (msg.what) {
				case CUP_ONE:
					toastString = "" + cupManager.cup_one;
					toastText.setText(yourName + "님이 " + toastString
							+ "을 마셨습니다!");
					break;
				case CUP_TWO:
					toastString = "" + cupManager.cup_two;
					toastText.setText(yourName + "님이 " + toastString
							+ "을 마셨습니다!");
					break;
				case CUP_THREE:
					toastString = "" + cupManager.cup_three;
					toastText.setText(yourName + "님이 " + toastString
							+ "을 마셨습니다!");
					break;
				case CUP_FOUR:
					toastString = "" + cupManager.cup_four;
					toastText.setText(yourName + "님이 " + toastString
							+ "을 마셨습니다!");
					break;
				case 5: // undo버튼이 눌렸을때
					toastText.setText("취소버튼이 눌렸슴다");
					break;
				case 6:
					toastText.setText("개인정보를 내놓아주세영.");
					// toastText.startAnimation(toastAni);
					break;

				}
				toastText.startAnimation(toastAni);
				water = msg.arg1;
				heartLogic();

			}

		}

	};

	public void heartLogic() {
		for (int i = 0; i < 14; i++)
			eachWater[i] = water / 14; // 물 마실때마다 호출해야하는 반복문
		// 주의할점: water는 지금 한번 마셨을때 그 컵의 물의 용량이 아니라
		// 지금까지 마신 물의 총량이다!

		for (int i = 0; i < 14; i++) {
			currentValue[i] = eachWater[i]; // 0번쨰 하트의 물 양을 갱신한다.
			if (currentValue[i] > originalValue[i]) { // 한편 originalValue보다 더 많이
														// 채워졌을경우엔
				float tmp = currentValue[i] - originalValue[i];
				currentValue[i] = originalValue[i]; // currentValue는
													// originalValue값으로 맞춰주고
				if (i == 13)
					break;
				eachWater[i + 1] += tmp; // 여유분을 다음 물 양에 추가해준다.
			}
		}
		heartTextML.setText(String.valueOf(water));
		heartTextPercent.setText(String.valueOf((int) ((float) water
				/ totalWater * 100)));
		// 반복문을 통해 각 하트 조각들의 currentValue들에 물을 채웠으면
		// 이제 for문 밖에서 setHeartAlpha()값으로
		// 각각 하트 조각들마다의 currentValue값을 참조해서
		// 그에 알맞게 opacity를 조절한다.

		setHeartAlpha();
	} // heartLogic() 메소드 끝

	public void setHeartAlpha() {
		float alphaValue = 0.0f;
		for (int i = 0; i < 14; i++) {
			alphaValue = currentValue[i] / originalValue[i];
			if (alphaValue >= 0 && alphaValue < 0.1)
				ViewHelper.setAlpha(heartImg[i], 0.1f);
			else if (alphaValue >= 0.1 && alphaValue < 0.2)
				ViewHelper.setAlpha(heartImg[i], 0.2f);
			else if (alphaValue >= 0.2 && alphaValue < 0.3)
				ViewHelper.setAlpha(heartImg[i], 0.3f);
			else if (alphaValue >= 0.3 && alphaValue < 0.4)
				ViewHelper.setAlpha(heartImg[i], 0.4f);
			else if (alphaValue >= 0.4 && alphaValue < 0.5)
				ViewHelper.setAlpha(heartImg[i], 0.5f);
			else if (alphaValue >= 0.5 && alphaValue < 0.6)
				ViewHelper.setAlpha(heartImg[i], 0.55f);
			else if (alphaValue >= 0.6 && alphaValue < 0.7)
				ViewHelper.setAlpha(heartImg[i], 0.6f);
			else if (alphaValue >= 0.7 && alphaValue < 0.8)
				ViewHelper.setAlpha(heartImg[i], 0.65f);
			else if (alphaValue >= 0.8 && alphaValue < 0.9)
				ViewHelper.setAlpha(heartImg[i], 0.7f);
			else if (alphaValue >= 0.9 && alphaValue < 0.95)
				ViewHelper.setAlpha(heartImg[i], 0.75f);
			else if (alphaValue >= 0.95)
				ViewHelper.setAlpha(heartImg[i], 0.8f);
		}
	}

	OnLongClickListener longClick = new OnLongClickListener() {

		@SuppressLint("NewApi")
		@Override
		public boolean onLongClick(View v) {
			// TODO Auto-generated method stub
			getActivity().findViewById(R.id.pager_title_strip).setVisibility(
					View.INVISIBLE);
			getActivity().findViewById(R.id.main_undo).setVisibility(
					View.INVISIBLE);

			switch (v.getId()) {
			case R.id.main_cup_drop:
				getActivity()
						.getSupportFragmentManager()
						.beginTransaction()
						.add(android.R.id.content,
								new CupCustomizingFragment(fillWaterHandler,
										CUP_ONE)).addToBackStack(null).commit();
				return true;
			case R.id.main_cup_bottle:
				getActivity()
						.getSupportFragmentManager()
						.beginTransaction()
						.add(android.R.id.content,
								new CupCustomizingFragment(fillWaterHandler,
										CUP_TWO)).addToBackStack(null).commit();
				return true;
			case R.id.main_cup_cup:
				getActivity()
						.getSupportFragmentManager()
						.beginTransaction()
						.add(android.R.id.content,
								new CupCustomizingFragment(fillWaterHandler,
										CUP_THREE)).addToBackStack(null)
						.commit();
				return true;
			case R.id.main_cup_coffee:
				getActivity()
						.getSupportFragmentManager()
						.beginTransaction()
						.add(android.R.id.content,
								new CupCustomizingFragment(fillWaterHandler,
										CUP_FOUR)).addToBackStack(null)
						.commit();
				return true;
			}
			return false;
		}

	};

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		// TODO Auto-generated method stub
		inflater.inflate(R.menu.main, menu);
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		switch (item.getItemId()) {
		case R.id.action_pencil:
			getActivity().findViewById(R.id.pager_title_strip).setVisibility(
					View.INVISIBLE);
			getActivity().findViewById(R.id.main_undo).setVisibility(
					View.INVISIBLE);
			getActivity()
					.getSupportFragmentManager()
					.beginTransaction()
					.add(android.R.id.content,
							new CustomFragment01(fillWaterHandler))
					.addToBackStack(null).commit();
			return true;
		case R.id.action_question:
			tutorialFlipper = tutorial.getTutorial(TUTORIAL_NUMBER,
					getActivity());
			tutorialFlipper.setOnTouchListener(mOnTouchListener);
			tutorial.showTutorial();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/*
	 * 윈도우상의 튜토리얼이 받는 touchlistener
	 */
	public OnTouchListener mOnTouchListener = new OnTouchListener() {

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			if (event.getAction() == MotionEvent.ACTION_DOWN) {
				if (tutorialFlipper.getCurrentView() == tutorialFlipper
						.getChildAt(1)) { // 튜토리얼 페이지가 2개밖에 없기때문에
					tutorial.finishTutorial(); // 지금은 getChildAt(1) 로
					return true;
				}
				tutorial.showNext();
			}
			return true;
		}

	};

	@Override
	public void onSaveInstanceState(Bundle outState) {
		// TODO Auto-generated method stub
		/*
		 * saveFragmentBasicState nullpointerexception 에러 방지용....
		 */
		outState.putString("Don't crash", "Please");
		super.onSaveInstanceState(outState);
	}

	public void onDestroy() {
		tutorial.finishTutorial();
		super.onDestroy();
	}

}
