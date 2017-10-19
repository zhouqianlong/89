package com.example.tst;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.Editable;
import android.text.Selection;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.ImageSpan;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.tst.adapter.ChatLVAdapter;
import com.example.tst.adapter.FaceGVAdapter;
import com.example.tst.adapter.FaceVPAdapter;
import com.example.tst.bean.ChatInfo;
import com.example.tst.view.DropdownListView;
import com.example.tst.view.MyEditText;
import com.lym.grivider.FileChooseDialog;
import com.lym.grivider.FilePerate;
import com.ramy.minervue.R;
import com.ramy.minervue.app.MainService;
import com.ramy.minervue.app.VideoActivity;
import com.ramy.minervue.db.DBHelper;
import com.ramy.minervue.db.UserBean;
import com.wifitalk.Config.AppConfig;
import com.wifitalk.Utils.LongDataPacket;
import com.wifitalk.Utils.MyDialogListener;


public class MainActivity extends Activity implements OnClickListener{
	public boolean falg = false;
	public long otherCount = 0;//剩余的数量
	public int selectCount = 0 ;//查询次数
	public int ActivityStatu = 0;
	public static final int Page_Size = 10;
	private ViewPager mViewPager;
	private LinearLayout mDotsLayout;
	private ImageView image_face,image_photo;
	private MyEditText input;
	private Button send;
	public DropdownListView mListView;
	private View chat_face_container;
	public ChatLVAdapter mLvAdapter;
	// 7列3行
	private int columns = 7;
	private int rows = 3;
	private List<View> views = new ArrayList<View>();
	private List<String> staticFacesList;
	public List<ChatInfo> infos = new ArrayList<ChatInfo>();
	private UserBean userBean;
	DBHelper dbHelper;
	public static MainActivity instances;
	private String filePath = "";//文件路径，用于接收文件选择器返回的路径
	//dialogListener实现了自定义的MyDialogListener类
	//通过该类实现获取对话框的返回值
	MyDialogListener dialogListener = new MyDialogListener() {
		@Override
		public void getFilePath(String path) {
			// TODO Auto-generated method stub
			filePath = path;//得到返回的文件路径，其为一个回调方法
			if(path.equals("/storage/emulated/0")||path.equals("storage/sdcard0/")){
				return;
			}
		
			refersh("TK2",path);
		}
	};
	private long dbLengTh;//聊天记录总数
	public int startPostion;// 
	public int endPostion;// 
	private TextView tv_header;





	private void initViews() {
		dbHelper = new DBHelper(getApplicationContext());
		mListView = (DropdownListView) findViewById(R.id.message_chat_listview);
		tv_header = (TextView)findViewById(R.id.tv_header);
		userBean = (UserBean) getIntent().getSerializableExtra("UserBean");

		image_photo = (ImageView) findViewById(R.id.image_photo);
		mViewPager = (ViewPager) findViewById(R.id.face_viewpager);
		mViewPager.setOnPageChangeListener(new PageChange());
		mDotsLayout = (LinearLayout) findViewById(R.id.face_dots_container);
		input = (MyEditText) findViewById(R.id.input_sms);
		send = (Button) findViewById(R.id.send_sms);
		image_face = (ImageView) findViewById(R.id.image_face);
		chat_face_container = findViewById(R.id.chat_face_container);
		InitViewPager();
		//		mListView.setOnClickListener(this);
		//		Toast.makeText(getApplicationContext(), userBean.getUsername(), 0).show();
		if(userBean.getChatInfo()!=null){
			infos.add(userBean.getChatInfo());
		}
		dbLengTh = dbHelper.selectLiaoTian(userBean.getMacAddress());
		if(dbLengTh>Page_Size){//大于10个分页
			startPostion = (int)dbLengTh-Page_Size;
			endPostion = (int)dbLengTh;
		}else{
			startPostion = 0;
			endPostion = (int)dbLengTh;
		}
		infos = dbHelper.selectLiaoTian(userBean.getMacAddress(),startPostion,endPostion);
		otherCount = dbLengTh-10;
		tv_header.setText(userBean.getUsername());
		//		Toast.makeText(getApplicationContext(), "聊天记录有："+liaotianLen, 0).show();
		image_photo.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				
				final AlertDialog  aDialog =  new AlertDialog.Builder(MainActivity.this).setTitle("发送数据").setPositiveButton("发送文件", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface mdialog, int which) {
						//如果没有SD卡，则输出提示
						if(FilePerate.getRootFolder() == null){
							Toast.makeText(MainActivity.this, "没有SD卡", Toast.LENGTH_SHORT).show();
							return ;
						}
						//创建一个自定义的对话框
						FileChooseDialog dialog = new FileChooseDialog(MainActivity.this,dialogListener);
						dialog.setTitle("选择文件");
						dialog.show();//显示对话框
						System.out.println(filePath+"根目录");//输出返回值

					}
				}).setNegativeButton("录像拍摄",  new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Intent mIntent = new Intent(getApplicationContext(), VideoActivity.class);
						mIntent.putExtra("flag", true);
						startActivity(mIntent);
					}
				}).show();


				

			}
		});
		mLvAdapter = new ChatLVAdapter(this, infos,userBean);
		mListView.setAdapter(mLvAdapter);
		mListView.setSelection(infos.size());
		mListView.setStackFromBottom(true);
		input.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				chat_face_container.setVisibility(View.GONE);
				image_face.setTag(null);
			}
		});

		input.setOnEditorActionListener(new TextView.OnEditorActionListener() { 
			public boolean onEditorAction(TextView v, int actionId,                   KeyEvent event)  {                          
				if (actionId==EditorInfo.IME_ACTION_SEND ||(event!=null&&event.getKeyCode()== KeyEvent.KEYCODE_ENTER)) 
				{                
					Toast.makeText(getApplicationContext(), "点击了发送", 0).show();
					return true;             
				}               
				return false;           
			}       
		});

	}




	/*
	 * 初始表情 *
	 */
	private void InitViewPager() {
		// 获取页数
		for (int i = 0; i < getPagerCount(); i++) {
			views.add(viewPagerItem(i));
			LayoutParams params = new LayoutParams(16, 16);
			// LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT,
			// LayoutParams.WRAP_CONTENT);
			mDotsLayout.addView(dotsItem(i), params);
		}
		FaceVPAdapter mVpAdapter = new FaceVPAdapter(views);
		mViewPager.setAdapter(mVpAdapter);
		mDotsLayout.getChildAt(0).setSelected(true);
	}

	private View viewPagerItem(int position) {
		LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
		View layout = inflater.inflate(R.layout.face_gridview, null);
		GridView gridview = (GridView) layout.findViewById(R.id.chart_face_gv);
		/**
		 * 注：因为每一页末尾都有一个删除图标，所以每一页的实际表情columns *　rows　－　1; 空出最后一个位置给删除图标
		 * */
		List<String> subList = new ArrayList<String>();
		subList.addAll(staticFacesList
				.subList(position * (columns * rows - 1),
						(columns * rows - 1) * (position + 1) > staticFacesList
						.size() ? staticFacesList.size() : (columns
								* rows - 1)
								* (position + 1)));
		// 0-20 20-40 40-60 60-80
		/**
		 * 末尾添加删除图标
		 * */
		subList.add("emotion_del_normal.png");
		FaceGVAdapter mGvAdapter = new FaceGVAdapter(subList, this);
		gridview.setAdapter(mGvAdapter);
		gridview.setNumColumns(columns);
		// 单击表情执行的操作
		gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				try {
					String png = ((TextView) ((LinearLayout) view)
							.getChildAt(1)).getText().toString();
					if (!png.contains("emotion_del_normal")) {// 如果不是删除图标
						// input.setText(sb);
						insert(getFace(png));
					} else {
						delete();
					}

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		input.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) { }

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub
			}

			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub
			}
		});
		// 发送
		send.setOnClickListener(new Button.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (!TextUtils.isEmpty(input.getText().toString())) {
					infos.add(getChatInfoTo(input.getText().toString()));
					//					infos.add(getChatInfofrom("test"+input.getText().toString()));
					mLvAdapter.setList(infos);
					mLvAdapter.notifyDataSetChanged();
					mListView.setSelection(infos.size());
					refersh("TK1",input.getText().toString());
					input.setText("");
				}
			}
		});

		image_face.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if(image_face.getTag()==null){
					chat_face_container.setVisibility(View.VISIBLE);
					image_face.setTag(this);
					InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);  
					imm.showSoftInput(input,InputMethodManager.SHOW_FORCED);  
					imm.hideSoftInputFromWindow(input.getWindowToken(), 0); //强制隐藏键盘  
				}else{setFaceLayoutVisizable_GONE();}
			}
		});

	
		mListView.setOnScrollListener(new OnScrollListener() {   
			public void onScrollStateChanged(AbsListView view, int scrollState) {  
				switch (scrollState) {  
				// 当不滚动时  
				case OnScrollListener.SCROLL_STATE_IDLE:  
					// 判断滚动到底部  
					if (mListView.getLastVisiblePosition() == (mListView.getCount() - 1)) {  
					}  
					// 判断滚动到顶部  

					if(mListView.getFirstVisiblePosition() == 0){  

						final View firstVisibleItemView = mListView.getChildAt(0);
						if (firstVisibleItemView != null && firstVisibleItemView.getTop() == 0) {
							
							if(startPostion-Page_Size<0&&startPostion>0){//8  - 10 
								startPostion = 0;
								endPostion = startPostion;
								falg = true;
							}else{
//								dbLengTh;//总数
								if(otherCount==0){
									return;
								} 
								if(otherCount-10>10){
									startPostion-=10;
									endPostion-=10;
									otherCount-=10;
								}else{
									startPostion = 0;
									endPostion = (int) otherCount;
									otherCount = 0;
								}
								
								
							}
							
							final List<ChatInfo> tmpInfos = dbHelper.selectLiaoTian(userBean.getMacAddress(),startPostion,endPostion);
							selectCount++;
							for(int i = 0 ; i < infos.size();i++){
								tmpInfos.add(infos.get(i));
							}
							infos = tmpInfos;
							mHandler.post(new Runnable() {

								@Override
								public void run() {
//									tv_header.setText(selectCount+"查询到顶部"+infos.size()+"记录"+startPostion+":"+endPostion+"F："+firstVisibleItemView.getTop());
									mLvAdapter.setList(tmpInfos);
									mLvAdapter.notifyDataSetChanged();
									if(falg){
										mListView.setSelection(endPostion);
									}else{
										mListView.setSelection(10);
									}

								}
							});

						}

					
					}  

					break;  
				}   
			}   

			@Override    
			public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {    


			}    
		}); 

		/***
		 * listview下拉刷新
		 * */
//		mListView.setOnRefreshListenerHead(new DropdownListView.OnRefreshListenerHeader() {
//
//			@Override
//			public void onRefresh() {
//				// TODO Auto-generated method stub
//				new Thread() {
//					@Override
//					public void run() {
//						final View firstVisibleItemView = mListView.getChildAt(0);
//						if (firstVisibleItemView != null && firstVisibleItemView.getTop() == 0) {
//
//							if(startPostion-Page_Size<0){
//								startPostion = 0;
//								endPostion = Page_Size;
//								falg = true;
//								mHandler.post(new Runnable() {
//
//									@Override
//									public void run() {
//										Toast.makeText(getApplicationContext(), "已加载完毕全部信息", 0).show();
//
//									}
//								});
//							}else{
//								startPostion-=Page_Size;
//								endPostion-=Page_Size;
//							}
//							final List<ChatInfo> tmpInfos = dbHelper.selectLiaoTian(userBean.getMacAddress(),startPostion,endPostion);
//							selectCount++;
//							for(int i = 0 ; i < infos.size();i++){
//								tmpInfos.add(infos.get(i));
//							}
//							mHandler.post(new Runnable() {
//
//								@Override
//								public void run() {
//									tv_header.setText(selectCount+"查询到顶部"+infos.size()+"记录"+startPostion+":"+endPostion+"F："+firstVisibleItemView.getTop());
//									mListView.onRefreshCompleteHeader();
//									mLvAdapter.setList(tmpInfos);
//									mLvAdapter.notifyDataSetChanged();
//									if(falg){
//										mListView.setSelection(0);
//									}else{
//										mListView.setSelection(tmpInfos.size()-infos.size());
//									}
//
//								}
//							});
//
//						}
//
//					}
//
//				}.start();
//			}
//		});

		return gridview;
	}

	public void setFaceLayoutVisizable_GONE(){
		chat_face_container.setVisibility(View.GONE);
		image_face.setTag(null);
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);  
		imm.showSoftInput(input,InputMethodManager.SHOW_FORCED);  
		imm.hideSoftInputFromWindow(input.getWindowToken(), 0); //强制隐藏键盘  
	}
	private SpannableStringBuilder getFace(String png) {
		SpannableStringBuilder sb = new SpannableStringBuilder();
		try {
			/**
			 * 经过测试，虽然这里tempText被替换为png显示，但是但我单击发送按钮时，获取到輸入框的内容是tempText的值而不是png
			 * 所以这里对这个tempText值做特殊处理
			 * 格式：#[face/png/f_static_000.png]#，以方便判斷當前圖片是哪一個
			 * */
			String tempText = "#[" + png + "]#";
			sb.append(tempText);
			sb.setSpan(
					new ImageSpan(MainActivity.this, BitmapFactory
							.decodeStream(getAssets().open(png))), sb.length()
							- tempText.length(), sb.length(),
							Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

		} catch (Exception e) {
			e.printStackTrace();
		}

		return sb;
	}

	/**
	 * 向输入框里添加表情
	 * */
	private void insert(CharSequence text) {
		int iCursorStart = Selection.getSelectionStart((input.getText()));
		int iCursorEnd = Selection.getSelectionEnd((input.getText()));
		if (iCursorStart != iCursorEnd) {
			((Editable) input.getText()).replace(iCursorStart, iCursorEnd, "");
		}
		int iCursor = Selection.getSelectionEnd((input.getText()));
		((Editable) input.getText()).insert(iCursor, text);
	}

	/**
	 * 删除图标执行事件
	 * 注：如果删除的是表情，在删除时实际删除的是tempText即图片占位的字符串，所以必需一次性删除掉tempText，才能将图片删除
	 * */
	private void delete() {
		if (input.getText().length() != 0) {
			int iCursorEnd = Selection.getSelectionEnd(input.getText());
			int iCursorStart = Selection.getSelectionStart(input.getText());
			if (iCursorEnd > 0) {
				if (iCursorEnd == iCursorStart) {
					if (isDeletePng(iCursorEnd)) {
						String st = "#[face/png/f_static_000.png]#";
						((Editable) input.getText()).delete(
								iCursorEnd - st.length(), iCursorEnd);
					} else {
						((Editable) input.getText()).delete(iCursorEnd - 1,
								iCursorEnd);
					}
				} else {
					((Editable) input.getText()).delete(iCursorStart,
							iCursorEnd);
				}
			}
		}
	}

	/**
	 * 判断即将删除的字符串是否是图片占位字符串tempText 如果是：则讲删除整个tempText
	 * **/
	private boolean isDeletePng(int cursor) {
		String st = "#[face/png/f_static_000.png]#";
		String content = input.getText().toString().substring(0, cursor);
		if (content.length() >= st.length()) {
			String checkStr = content.substring(content.length() - st.length(),
					content.length());
			String regex = "(\\#\\[face/png/f_static_)\\d{3}(.png\\]\\#)";
			Pattern p = Pattern.compile(regex);
			Matcher m = p.matcher(checkStr);
			return m.matches();
		}
		return false;
	}

	private ImageView dotsItem(int position) {
		LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
		View layout = inflater.inflate(R.layout.dot_image, null);
		ImageView iv = (ImageView) layout.findViewById(R.id.face_dot);
		iv.setId(position);
		return iv;
	}

	private int getPagerCount() {
		int count = staticFacesList.size();
		return count % (columns * rows - 1) == 0 ? count / (columns * rows - 1)
				: count / (columns * rows - 1) + 1;
	}

	private void initStaticFaces() {
		try {
			staticFacesList = new ArrayList<String>();
			String[] faces = getAssets().list("face/png");
			for (int i = 0; i < faces.length; i++) {
				staticFacesList.add(faces[i]);
			}
			staticFacesList.remove("emotion_del_normal.png");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 表情页改变时，dots效果也要跟着改变
	 * */
	class PageChange implements OnPageChangeListener {

		@Override
		public void onPageScrollStateChanged(int arg0) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onPageSelected(int arg0) {
			// TODO Auto-generated method stub
			for (int i = 0; i < mDotsLayout.getChildCount(); i++) {
				mDotsLayout.getChildAt(i).setSelected(false);
			}
			mDotsLayout.getChildAt(arg0).setSelected(true);
		}

	}

	private ChatInfo getChatInfoTo(String message) {
		ChatInfo info = new ChatInfo();
		info.content = message;
		info.fromOrTo = 1;
		info.toMac = userBean.getMacAddress();
		info.mac = MainService.getInstance().getMacAddress();
		// if ((System.currentTimeMillis() - upTime) > 60000) {
		// upTime = System.currentTimeMillis();
		// info.time = DateFormatUtil.getCurrDate(Constant.DATE_PATTERN_1);
		// }else{
		// info.time = "";
		// }
		// info.time = DateFormatUtil.getCurrDate(Constant.DATE_PATTERN_1);
		dbHelper.AddLiaoTian(info);
		return info;
	}
	private ChatInfo getChatInfofrom(String message) {
		ChatInfo info = new ChatInfo();
		info.content = message;
		info.fromOrTo = 0;
		info.mac = MainService.getInstance().getMacAddress();
		info.toMac = userBean.getMacAddress();
		// if ((System.currentTimeMillis() - upTime) > 60000) {
		// upTime = System.currentTimeMillis();
		// info.time = DateFormatUtil.getCurrDate(Constant.DATE_PATTERN_1);
		// }else{
		// info.time = "";
		// }
		// info.time = DateFormatUtil.getCurrDate(Constant.DATE_PATTERN_1);
		dbHelper.AddLiaoTian(info);
		return info;
	}

	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0:
				mLvAdapter.setList(infos);
				mLvAdapter.notifyDataSetChanged();
				mListView.onRefreshCompleteHeader();
				break;
			}
		}
	};
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		instances = this;
		setContentView(R.layout.chat_main);
		initStaticFaces();
		initViews();
	}

	@Override
	public void onClick(View v) {
		chat_face_container.setVisibility(View.GONE);
		image_face.setTag(null);
	}

	/**
	 * 
	 * @param content 数据内容
	 * @param type  TK1 为聊天包    TK2 为传输文件包
	 */
	public void refersh(final String type,final String content) {
		if(type=="TK2"){
			infos.add(getChatInfoTo(content));
			mLvAdapter.setList(infos);
			mLvAdapter.notifyDataSetChanged();
		}
		mListView.setAdapter(mLvAdapter);
		mListView.setStackFromBottom(true);
		mListView.setSelection(infos.size()+1);
			
		setFaceLayoutVisizable_GONE();
		new Thread(new Runnable() {

			@Override
			public void run() {
				DatagramSocket clientSocket=null;
				try {
					clientSocket = new DatagramSocket();
					StringBuffer str = new StringBuffer(type+":"+MainService.getInstance().getMacAddress());
					LongDataPacket dataPacket = new LongDataPacket(str.toString().getBytes(), content.getBytes());
					clientSocket.send(new DatagramPacket(dataPacket.getAllData(),dataPacket.getAllData().length, InetAddress.getByName(userBean.getUserIp()), AppConfig.PortAudio));

				} catch (SocketException e) {
					e.printStackTrace();
				} catch (UnknownHostException e) {
					e.printStackTrace();
				} catch (Exception e) {
					e.printStackTrace();
				}finally{
					if(clientSocket!=null){
						clientSocket.close();
					}
				}
			}
		}).start();
	}
	@Override
	protected void onDestroy() {
		super.onDestroy();
		instances = null;
	}
	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		super.onBackPressed();
		if(userBean.getChatInfo()!=null){
			startActivity(new Intent(getApplicationContext(), com.ramy.minervue.app.MainActivity.class));
		}

	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
//		instances = null;
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		instances = this;
//		mLvAdapter.connect(userBean.getUserIp());
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		instances = this;
		setActiviyStatu();

	}


	public void setActiviyStatu(){
		ActivityStatu= 0;
		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				ActivityStatu=1;

			}
		}).start();

	}
}
