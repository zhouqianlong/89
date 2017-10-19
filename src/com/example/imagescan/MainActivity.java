package com.example.imagescan;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.ramy.minervue.R;
import com.ramy.minervue.app.VideoActivity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
/**
 * @blog http://blog.csdn.net/xiaanming
 * 
 * @author xiaanming
 * 
 *
 */
public class MainActivity extends Activity {
	private HashMap<String, List<String>> mGruopMap = new HashMap<String, List<String>>();
	private List<ImageBean> list = new ArrayList<ImageBean>();
	private final static int SCAN_OK = 1;
	private ProgressDialog mProgressDialog;
	private GroupAdapter adapter;
	private GridView mGroupGridView;
	private String imagePath ;
	private Handler mHandler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case SCAN_OK:
				//�رս�����
				mProgressDialog.dismiss();
				
//				adapter = new GroupAdapter(MainActivity.this, list = subGroupOfImage(mGruopMap), mGroupGridView);
//				mGroupGridView.setAdapter(adapter);
				list = subGroupOfImage(mGruopMap);
				if(list==null){ 
					Toast.makeText(getApplicationContext(), "û���ҵ�ͼƬ�����ϴ�,�����պ��ϴ�", 0).show();
					finish();
					startActivity(new Intent(MainActivity.this, VideoActivity.class));
					return;
				}
				List<String> childList = mGruopMap.get(list.get(0).getFolderName());
				Intent mIntent = new Intent(MainActivity.this, ShowImageActivity.class);//ֱ�ӽ���Ŀ¼
				mIntent.putStringArrayListExtra("data", (ArrayList<String>)childList);
				startActivity(mIntent);
				finish();
				break;
			}
		}
		
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		imagePath = getExternalFilesDir(null).getPath()+"/image";
		mGroupGridView = (GridView) findViewById(R.id.main_grid);
		getImages();
		mGroupGridView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				List<String> childList = mGruopMap.get(list.get(position).getFolderName());
				Intent mIntent = new Intent(MainActivity.this, ShowImageActivity.class);
				mIntent.putStringArrayListExtra("data", (ArrayList<String>)childList);
				startActivity(mIntent);
				
			}
		});
	}

	/**
	 * ����ContentProviderɨ���ֻ��е�ͼƬ���˷��������������߳���
	 */
	private void getImages() {
		if(!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
			Toast.makeText(this, "�����ⲿ�洢", Toast.LENGTH_SHORT).show();
			return;
		}
		
		//��ʾ������
		mProgressDialog = ProgressDialog.show(this, null, "���ڼ���...");
		
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				File file = new File(imagePath);
				File [] list = file.listFiles();//��ȡĿ¼�������ļ�
				for(int i = 0 ; i<list.length;i++){
					String path = list[i].getPath();		//��ȡͼƬ��·��
					String parentName = new File(path).getParentFile().getName();//��ȡ��ͼƬ�ĸ�·����
					

					//���ݸ�·������ͼƬ���뵽mGruopMap��
					if (!mGruopMap.containsKey(parentName)) {
						List<String> chileList = new ArrayList<String>();
						chileList.add(path);
						mGruopMap.put(parentName, chileList);
					} else {
						mGruopMap.get(parentName).add(path);
					}
				}
				//֪ͨHandlerɨ��ͼƬ���
				mHandler.sendEmptyMessage(SCAN_OK);
				
			}
		}).start();
	}
	
	
	/**
	 * ��װ�������GridView������Դ����Ϊ����ɨ���ֻ���ʱ��ͼƬ��Ϣ����HashMap��
	 * ������Ҫ����HashMap��������װ��List
	 * 
	 * @param mGruopMap
	 * @return
	 */
	private List<ImageBean> subGroupOfImage(HashMap<String, List<String>> mGruopMap){
		if(mGruopMap.size() == 0){
			return null;
		}
		List<ImageBean> list = new ArrayList<ImageBean>();
		
		Iterator<Map.Entry<String, List<String>>> it = mGruopMap.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<String, List<String>> entry = it.next();
			ImageBean mImageBean = new ImageBean();
			String key = entry.getKey();
			List<String> value = entry.getValue();
			
			mImageBean.setFolderName(key);
			mImageBean.setImageCounts(value.size());
			mImageBean.setTopImagePath(value.get(0));//��ȡ����ĵ�һ��ͼƬ
			
			list.add(mImageBean);
		}
		
		return list;
		
	}


}
