package com.example.imagescan;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.GridView;

import com.example.imagescan.MyImageView.OnMeasureListener;
import com.example.imagescan.NativeImageLoader.NativeImageCallBack;
import com.ramy.minervue.R;

public class ChildAdapter extends BaseAdapter {
	private Point mPoint = new Point(0, 0);//������װImageView�Ŀ�͸ߵĶ���
	/**
	 * �����洢ͼƬ��ѡ�����
	 */
	private HashMap<Integer, Boolean> mSelectMap = new HashMap<Integer, Boolean>();
	private GridView mGridView;
	private List<String> list;
	protected LayoutInflater mInflater;
	public boolean image_GONE =false;
	public ChildAdapter(Context context, List<String> list, GridView mGridView) {
		this.list = list;
		this.mGridView = mGridView;
		mInflater = LayoutInflater.from(context);
	}
	
	@Override
	public int getCount() {
		return list.size();
	}

	@Override
	public Object getItem(int position) {
		return list.get(position);
	}


	@Override
	public long getItemId(int position) {
		return position;
	}
	
	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		final ViewHolder viewHolder;
		String path = list.get(position);
		
		if(convertView == null){
			convertView = mInflater.inflate(R.layout.grid_child_item, null);
			viewHolder = new ViewHolder();
			viewHolder.mImageView = (MyImageView) convertView.findViewById(R.id.child_image);
			viewHolder.mCheckBox = (CheckBox) convertView.findViewById(R.id.child_checkbox);
			
			//��������ImageView�Ŀ�͸�
			viewHolder.mImageView.setOnMeasureListener(new OnMeasureListener() {
				
				@Override
				public void onMeasureSize(int width, int height) {
					mPoint.set(width, height);
				}
			});
			
			convertView.setTag(viewHolder);
		}else{
			viewHolder = (ViewHolder) convertView.getTag();
			viewHolder.mImageView.setImageResource(R.drawable.friends_sends_pictures_no);
		}
		viewHolder.mImageView.setTag(path);
		if(image_GONE){//����ѡ��ť
			viewHolder.mCheckBox.setVisibility(View.GONE);	
		}else{
			viewHolder.mCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
				
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					//�����δѡ�е�CheckBox,����Ӷ���
					if(!mSelectMap.containsKey(position) || !mSelectMap.get(position)){
						addAnimation(viewHolder.mCheckBox);
					}
					mSelectMap.put(position, isChecked);
				}
			});
			
			viewHolder.mCheckBox.setChecked(mSelectMap.containsKey(position) ? mSelectMap.get(position) : false);
		}
		
		//����NativeImageLoader����ر���ͼƬ
		Bitmap bitmap = NativeImageLoader.getInstance().loadNativeImage(path, mPoint, new NativeImageCallBack() {
			
			@Override
			public void onImageLoader(Bitmap bitmap, String path) {
				ImageView mImageView = (ImageView) mGridView.findViewWithTag(path);
				if(bitmap != null && mImageView != null){
					mImageView.setImageBitmap(bitmap);
				}
			}
		});
		
		if(bitmap != null){
			viewHolder.mImageView.setImageBitmap(bitmap);
		}else{
			viewHolder.mImageView.setImageResource(R.drawable.friends_sends_pictures_no);
		}
		
		return convertView;
	}
	
	/**
	 * ��CheckBox�ӵ�����������ÿ�Դ��nineoldandroids���ö��� 
	 * @param view
	 */
	private void addAnimation(View view){
		float [] vaules = new float[]{0.5f, 0.6f, 0.7f, 0.8f, 0.9f, 1.0f, 1.1f, 1.2f, 1.3f, 1.25f, 1.2f, 1.15f, 1.1f, 1.0f};
		AnimatorSet set = new AnimatorSet();
		set.playTogether(ObjectAnimator.ofFloat(view, "scaleX", vaules), 
				ObjectAnimator.ofFloat(view, "scaleY", vaules));
				set.setDuration(150);
		set.start();
	}
	
	
	/**
	 * ��ȡѡ�е�Item��position
	 * @return
	 */
	public List<Integer> getSelectItems(){
		List<Integer> list = new ArrayList<Integer>();
		for(Iterator<Map.Entry<Integer, Boolean>> it = mSelectMap.entrySet().iterator(); it.hasNext();){
			Map.Entry<Integer, Boolean> entry = it.next();
			if(entry.getValue()){
				list.add(entry.getKey());
			}
		}
		
		return list;
	}
	
	
	public  List<String> getSelectListItems(){
		return list;
	}
	public static class ViewHolder{
		public MyImageView mImageView;
		public CheckBox mCheckBox;
	}



}
