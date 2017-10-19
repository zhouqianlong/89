package com.example.tst.adapter;

import it.sauronsoftware.ftp4j.FTPClient;
import it.sauronsoftware.ftp4j.FTPDataTransferListener;
import it.sauronsoftware.ftp4j.FTPFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.tst.MainActivity;
import com.example.tst.bean.ChatInfo;
import com.example.tst.gif.AnimatedGifDrawable;
import com.example.tst.gif.AnimatedImageSpan;
import com.ramy.minervue.R;
import com.ramy.minervue.db.UserBean;
import com.wifitalk.Utils.ProgressDialogUtils;

@SuppressLint("NewApi")
public class ChatLVAdapter extends BaseAdapter {
	private MainActivity mContext;
	private List<ChatInfo> list;
	private FTPClient client;//FTP服务器
	private FTPFile[] fls = null;//FTP文件目录
	/* 下载保存路径 */
	private String mSavePath;
	/* 记录进度条数量 */
	private int progress;
	/** 弹出的更多选择框 */
	private PopupWindow popupWindow;

	/** 复制，删除 */
	private TextView copy, delete;

	private LayoutInflater inflater;
	String ftpAddress = "";
	/**
	 * 执行动画的时间
	 */
	protected long mAnimationTime = 150;
	private boolean ftpConn = false;
	public ChatLVAdapter(MainActivity mContext, List<ChatInfo> list,final UserBean userBean) {
		super();
		this.mContext = mContext;
		this.list = list;
		inflater = LayoutInflater.from(mContext);
		initPopWindow();
		ftpAddress = userBean.getUserIp();
		new Thread(new Runnable() {

			@Override
			public void run() {
				ftpConn = connect(userBean.getUserIp());
			}
		}).start();
	}

	public void setList(List<ChatInfo> list) {
		this.list = list;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return list.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return list.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		final ViewHodler hodler;
		if (convertView == null) {
			hodler = new ViewHodler();
			convertView = LayoutInflater.from(mContext).inflate(R.layout.chat_lv_item, null);
			hodler.rl_main = (RelativeLayout) convertView.findViewById(R.id.rl_main);
			hodler.fromContainer = (ViewGroup) convertView.findViewById(R.id.chart_from_container);
			hodler.toContainer = (ViewGroup) convertView.findViewById(R.id.chart_to_container);
			hodler.fromContent = (TextView) convertView.findViewById(R.id.chatfrom_content);
			hodler.toContent = (TextView) convertView.findViewById(R.id.chatto_content);
			hodler.time = (TextView) convertView.findViewById(R.id.chat_time);
			convertView.setTag(hodler);
		} else {
			hodler = (ViewHodler) convertView.getTag();
		}

		
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				setContext(position, hodler);
			}
		}).start();

//		if (list.get(position).fromOrTo == 0) {
//			// 收到消息 from显示
//			hodler.toContainer.setVisibility(View.GONE);
//			hodler.fromContainer.setVisibility(View.VISIBLE);
//			// 对内容做处理
//			SpannableStringBuilder sb = handler(hodler.fromContent,list.get(position).content);
//			// 预设一个图片
//			hodler.fromContent.setBackgroundResource(R.drawable.chatfrom_bg);
//			if(list.get(position).type==2){
//				File file  = new File(list.get(position).content);
//				// 给 ImageView 设置一个 tag
//				hodler.fromContent.setTag("/storage/sdcard0/download/"+file.getName());
//				// 通过 tag 来防止图片错位
//				if (hodler.fromContent.getTag() != null && hodler.fromContent.getTag().equals("/storage/sdcard0/download/"+file.getName())) {
//					if(file.getName().indexOf(".mp4")>0){
//						Bitmap bitmap = getVideoThumbnail("/storage/sdcard0/download/"+file.getName(), 100, 100, MediaStore.Images.Thumbnails.MICRO_KIND);
//						if(bitmap!=null){
//							hodler.fromContent.setBackgroundDrawable(new BitmapDrawable(bitmap));
//							hodler.fromContent.setText("");
//						}else{
//							hodler.fromContent.setText(sb);
//						}
//					}else if(file.getName().indexOf(".jpg")>0){
//						Bitmap bitmap = getImageThumbnail("/storage/sdcard0/download/"+file.getName(), 200, 200);
//						if(bitmap!=null){
//							hodler.fromContent.setBackgroundDrawable(new BitmapDrawable(bitmap));
//							hodler.fromContent.setText("");
//						}else{
//							hodler.fromContent.setText(sb);
//						}
//						
//					}
//				}
//				if(list.get(position).downStatu==0){
//					listPath.add(list.get(position).content);
//					list.get(position).downStatu = 1;
//					hodler.fromContent.setText(sb);
//					downFile();
//				}
//			}else{
////				hodler.fromContent.setText(sb);
//			}
//			hodler.time.setText(list.get(position).time);
//		} else {
//			// 发送消息 to显示
//			hodler.toContainer.setVisibility(View.VISIBLE);
//			hodler.fromContainer.setVisibility(View.GONE);
//			// 对内容做处理
//			SpannableStringBuilder sb = handler(hodler.toContent,list.get(position).content);
//			hodler.toContent.setBackgroundResource(R.drawable.chatto_bg);
//			// 给 ImageView 设置一个 tag
//			hodler.toContent.setTag(list.get(position).content);
//			
//			// 通过 tag 来防止图片错位
//			if (hodler.toContent.getTag() != null && hodler.toContent.getTag().equals(list.get(position).content)) {
//				if(list.get(position).content.indexOf(".mp4")>0){
//					Bitmap bitmap = getVideoThumbnail(list.get(position).content, 100, 100, MediaStore.Images.Thumbnails.MICRO_KIND);
//					if(bitmap!=null){
//						hodler.toContent.setBackgroundDrawable(new BitmapDrawable(bitmap));
//						hodler.toContent.setText("");
//					}else{
//						hodler.toContent.setText(sb);
//					}
//				}else if(list.get(position).content.indexOf(".jpg")>0){
//					Bitmap bitmap = getImageThumbnail(list.get(position).content, 200, 200);
//					if(bitmap!=null){
//						hodler.toContent.setBackgroundDrawable(new BitmapDrawable(bitmap));
//						hodler.toContent.setText("");
//					}else{
//						hodler.toContent.setText(sb);
//					}
//				}else{
//					hodler.toContent.setText(sb);
//				}
//				
//			} 
//			//hodler.time.setText(list.get(position).time);
//		}
//	
		hodler.fromContent.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				//				Toast.makeText(mContext, "111", 0).show();
				if(list.get(position).type==2){
					openFile(new File(list.get(position).content));
				}
			}
		});
		hodler.toContent.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				String type = URLConnection
						.guessContentTypeFromName(list.get(position).content);
				if (type == null) {
					return;
				}
				try {
					File file = new File(list.get(position).content);
					Intent intent = new Intent(Intent.ACTION_VIEW);
					intent.setDataAndType(Uri.fromFile(file), type);
					mContext.startActivity(intent);
				} catch (Exception e) {
				}
			}
		});
		hodler.rl_main.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				mContext.setFaceLayoutVisizable_GONE();
			}
		});
		// 设置+按钮点击效果
		hodler.fromContent.setOnLongClickListener(new popAction(convertView,
				position, list.get(position).fromOrTo));
		hodler.toContent.setOnLongClickListener(new popAction(convertView,
				position, list.get(position).fromOrTo));
		return convertView;
	}

	private void setContext(final int position, final ViewHodler hodler) {
		mHandler.post(new Runnable() {
			@Override
			public void run() {
				if (list.get(position).fromOrTo == 0) {
					// 收到消息 from显示
					hodler.toContainer.setVisibility(View.GONE);
					hodler.fromContainer.setVisibility(View.VISIBLE);
					// 对内容做处理
					SpannableStringBuilder sb = handler(hodler.fromContent,list.get(position).content);
					// 预设一个图片
					hodler.fromContent.setBackgroundResource(R.drawable.chatfrom_bg);
					if(list.get(position).type==2){
						File file  = new File(list.get(position).content);
						hodler.fromContent.setTag("/storage/sdcard0/download/"+file.getName());
						// 通过 tag 来防止图片错位
//						if (hodler.fromContent.getTag() != null && hodler.fromContent.getTag().equals("/storage/sdcard0/download/"+file.getName())) {
//							if(file.getName().indexOf(".mp4")>0){
//								hodler.fromContent.setBackgroundDrawable(new BitmapDrawable(getVideoThumbnail("/storage/sdcard0/download/"+file.getName(), 100, 100, MediaStore.Images.Thumbnails.MICRO_KIND)));
//								hodler.fromContent.setText("");
//							}else if(file.getName().indexOf(".jpg")>0){
//								hodler.fromContent.setBackgroundDrawable(new BitmapDrawable(getImageThumbnail("/storage/sdcard0/download/"+file.getName(), 200, 200)));
//								hodler.fromContent.setText("");
//							}
//						}
						if(list.get(position).downStatu==0){
							listPath.add(list.get(position).content);
							list.get(position).downStatu = 1;
							hodler.fromContent.setText(sb);
							downFile();
						}
					}else{
						hodler.fromContent.setText(sb);
					}
					hodler.time.setText(list.get(position).time);
				} else {
					// 发送消息 to显示
					hodler.toContainer.setVisibility(View.VISIBLE);
					hodler.fromContainer.setVisibility(View.GONE);
					// 对内容做处理
					SpannableStringBuilder sb = handler(hodler.toContent,list.get(position).content);
					hodler.toContent.setBackgroundResource(R.drawable.chatto_bg);
					// 给 ImageView 设置一个 tag
					hodler.toContent.setTag(list.get(position).content);
					hodler.toContent.setText(sb);
					// 通过 tag 来防止图片错位
//					if (hodler.toContent.getTag() != null && hodler.toContent.getTag().equals(list.get(position).content)) {
//						if(list.get(position).content.indexOf(".mp4")>0){
//							Bitmap bitmap = getVideoThumbnail(list.get(position).content, 100, 100, MediaStore.Images.Thumbnails.MICRO_KIND);
//							if(bitmap!=null){
//								hodler.toContent.setBackgroundDrawable(new BitmapDrawable(bitmap));
//								hodler.toContent.setText("");
//							} 
//						}else if(list.get(position).content.indexOf(".jpg")>0){
//							Bitmap bitmap = getImageThumbnail(list.get(position).content, 200, 200);
//							if(bitmap!=null){
//								hodler.toContent.setBackgroundDrawable(new BitmapDrawable(bitmap));
//								hodler.toContent.setText("");
//							}
//						}
//						
//					} 
					//hodler.time.setText(list.get(position).time);
				}
			}
		});
		
	}

	private SpannableStringBuilder handler(final TextView gifTextView,
			String content) {
		SpannableStringBuilder sb = new SpannableStringBuilder(content);
		String regex = "(\\#\\[face/png/f_static_)\\d{3}(.png\\]\\#)";
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(content);
		while (m.find()) {
			String tempText = m.group();
			try {
				String num = tempText.substring(
						"#[face/png/f_static_".length(), tempText.length()
						- ".png]#".length());
				String gif = "face/gif/f" + num + ".gif";
				/**
				 * 如果open这里不抛异常说明存在gif，则显示对应的gif
				 * 否则说明gif找不到，则显示png
				 * */
				InputStream is = mContext.getAssets().open(gif);
				sb.setSpan(new AnimatedImageSpan(new AnimatedGifDrawable(is,
						new AnimatedGifDrawable.UpdateListener() {
					@Override
					public void update() {
						gifTextView.postInvalidate();
					}
				})), m.start(), m.end(),
				Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				is.close();
			} catch (Exception e) {
				String png = tempText.substring("#[".length(),
						tempText.length() - "]#".length());
				try {
					sb.setSpan(
							new ImageSpan(mContext, BitmapFactory
									.decodeStream(mContext.getAssets()
											.open(png))), m.start(), m.end(),
											Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				e.printStackTrace();
			}
		}
		return sb;
	}

	class ViewHodler {
		ImageView fromIcon, toIcon;
		TextView fromContent, toContent, time;
		ViewGroup fromContainer, toContainer;
		RelativeLayout rl_main;
	}

	/**
	 * 屏蔽listitem的所有事件
	 * */
	@Override
	public boolean areAllItemsEnabled() {
		return false;
	}

	@Override
	public boolean isEnabled(int position) {
		return false;
	}

	/**
	 * 初始化弹出的pop
	 * */
	private void initPopWindow() {
		View popView = inflater.inflate(R.layout.chat_item_copy_delete_menu,
				null);
		copy = (TextView) popView.findViewById(R.id.chat_copy_menu);
		delete = (TextView) popView.findViewById(R.id.chat_delete_menu);
		popupWindow = new PopupWindow(popView, LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT);
		popupWindow.setBackgroundDrawable(new ColorDrawable(0));
		// 设置popwindow出现和消失动画
		// popupWindow.setAnimationStyle(R.style.PopMenuAnimation);
	}

	/**
	 * 显示popWindow
	 * */
	public void showPop(View parent, int x, int y, final View view,
			final int position, final int fromOrTo) {
		// 设置popwindow显示位置
		popupWindow.showAtLocation(parent, 0, x, y);
		// 获取popwindow焦点
		popupWindow.setFocusable(true);
		// 设置popwindow如果点击外面区域，便关闭。
		popupWindow.setOutsideTouchable(true);
		// 为按钮绑定事件
		// 复制
		copy.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (popupWindow.isShowing()) {
					popupWindow.dismiss();
				}
				// 获取剪贴板管理服务
				ClipboardManager cm = (ClipboardManager) mContext
						.getSystemService(Context.CLIPBOARD_SERVICE);
				// 将文本数据复制到剪贴板
				cm.setText(list.get(position).content);
			}
		});
		// 删除
		delete.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (popupWindow.isShowing()) {
					popupWindow.dismiss();
				}
				if (fromOrTo == 0) {
					// from
					leftRemoveAnimation(view, position);
				} else if (fromOrTo == 1) {
					// to
					rightRemoveAnimation(view, position);
				}

				// list.remove(position);
				// notifyDataSetChanged();
			}
		});
		popupWindow.update();
		if (popupWindow.isShowing()) {

		}
	}

	/**
	 * 每个ITEM中more按钮对应的点击动作
	 * */
	public class popAction implements OnLongClickListener {
		int position;
		View view;
		int fromOrTo;

		public popAction(View view, int position, int fromOrTo) {
			this.position = position;
			this.view = view;
			this.fromOrTo = fromOrTo;
		}

		@Override
		public boolean onLongClick(View v) {
			// TODO Auto-generated method stub
			int[] arrayOfInt = new int[2];
			// 获取点击按钮的坐标
			v.getLocationOnScreen(arrayOfInt);
			int x = arrayOfInt[0];
			int y = arrayOfInt[1];
			// System.out.println("x: " + x + " y:" + y + " w: " +
			// v.getMeasuredWidth() + " h: " + v.getMeasuredHeight() );
			showPop(v, x, y, view, position, fromOrTo);
			return true;
		}
	}

	/**
	 * item删除动画
	 * */
	private void rightRemoveAnimation(final View view, final int position) {
		final Animation animation = (Animation) AnimationUtils.loadAnimation(
				mContext, R.anim.chatto_remove_anim);
		animation.setAnimationListener(new AnimationListener() {
			public void onAnimationStart(Animation animation) {
			}

			public void onAnimationRepeat(Animation animation) {
			}

			public void onAnimationEnd(Animation animation) {
				view.setAlpha(0);
				performDismiss(view, position);
				animation.cancel();
			}
		});

		view.startAnimation(animation);
	}

	/**
	 * item删除动画
	 * */
	private void leftRemoveAnimation(final View view, final int position) {
		final Animation animation = (Animation) AnimationUtils.loadAnimation(
				mContext, R.anim.chatfrom_remove_anim);
		animation.setAnimationListener(new AnimationListener() {
			public void onAnimationStart(Animation animation) {
			}

			public void onAnimationRepeat(Animation animation) {
			}

			public void onAnimationEnd(Animation animation) {
				view.setAlpha(0);
				performDismiss(view, position);
				animation.cancel();
			}
		});

		view.startAnimation(animation);
	}

	/**
	 * 在此方法中执行item删除之后，其他的item向上或者向下滚动的动画，并且将position回调到方法onDismiss()中
	 * 
	 * @param dismissView
	 * @param dismissPosition
	 */
	private void performDismiss(final View dismissView,
			final int dismissPosition) {
		final ViewGroup.LayoutParams lp = dismissView.getLayoutParams();// 获取item的布局参数
		final int originalHeight = dismissView.getHeight();// item的高度

		ValueAnimator animator = ValueAnimator.ofInt(originalHeight, 0)
				.setDuration(mAnimationTime);
		animator.start();

		animator.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator animation) {
				list.remove(dismissPosition);
				notifyDataSetChanged();
				// 这段代码很重要，因为我们并没有将item从ListView中移除，而是将item的高度设置为0
				// 所以我们在动画执行完毕之后将item设置回来
				ViewGroup.LayoutParams lp = dismissView.getLayoutParams();
				lp.height = originalHeight;
				dismissView.setLayoutParams(lp);
			}
		});

		animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(ValueAnimator valueAnimator) {
				// 这段代码的效果是ListView删除某item之后，其他的item向上滑动的效果
				lp.height = (Integer) valueAnimator.getAnimatedValue();
				dismissView.setLayoutParams(lp);
			}
		});

	}
	/*
	 * 打开文件
	 * @param file
	 */ 
	private void openFile(File file){ 
		String type = URLConnection
				.guessContentTypeFromName(file.getName());
		if (type == null) {
			Toast.makeText(mContext, R.string.unknown_file,
					Toast.LENGTH_SHORT).show();
			return;
		}
		try {
			file = new File("/storage/sdcard0/download/"+file.getName());
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setDataAndType(Uri.fromFile(file), type);
			mContext.startActivity(intent);
		} catch (ActivityNotFoundException e) {
			Toast.makeText(mContext, R.string.unknown_file,
					Toast.LENGTH_SHORT).show();
		}
	} 

	/**
	 * 下载文件
	 * @param position item id
	 * @param path 文件路径
	 */
	public boolean downIng = false;
	public void downFile(){
		if(downIng==false){
			downIng  = true;
			new Thread(new Runnable() {
				@Override
				public void run() {
					for(int i =0;i<30;i++){
						if(ftpConn==false){
							try {
								Thread.sleep(1000);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
						if(ftpConn){
							break;
						}
					}
					if(ftpConn==false){
						return;
					}
						for(int j=0;j<listPath.size();j++){
							Log.i("0090", "downFile:"+listPath.get(j));
							String[] names = listPath.get(j).split("/");
							final String fileName = names[names.length-1];
							try {
								String sdpath = Environment.getExternalStorageDirectory() + "/";
								mSavePath = sdpath + "download";///storage/sdcard0//.fota
								File file = new File(mSavePath);
								// 判断文件目录是否存在
								if (!file.exists())
									file.mkdir();
								String url = listPath.get(j).replace("storage/sdcard0/", "");
									   url = listPath.get(j).replace("storage/emulated/0/", "");
								client.changeDirectory(getWorkSpaces(url));
								fls = client.list();
								//判断本地是否已经下载过
								File devicefile = new File(mSavePath,fileName);
								// 获取文件大小
								int length = (int) getFtpFileSizeByName(url);//文件路径
								if(devicefile.exists()&&devicefile.getName().equals(fileName)){//本地存在
									if(devicefile.length()==length){
										Log.i("0090", "本地存在"+devicefile.getPath());
										return;//终止donload 	
									}
								}
								mHandler.post(new Runnable() {
									@Override
									public void run() { 
										
										ProgressDialogUtils.showProgressDialog(mContext, "下载中");
										
									}
								});
								client.download(
										fileName,//IMG_20170411_103142.jpg
										devicefile,
										new DownloadFTPDataTransferListener(length));
								mHandler.post(new Runnable() {

									@Override
									public void run() {
										Toast.makeText(mContext, fileName+"下载成功", 0).show();
										ProgressDialogUtils.dismissProgressDialog();
										notifyDataSetChanged();
										Log.i("0090",fileName+"下载成功");
									}
								});
								client.changeDirectory("//");
							} catch (final Exception e) {
								disconnect();
								connect(ftpAddress);
								e.printStackTrace();
								mHandler.post(new Runnable() {
									@Override
									public void run() {
										Toast.makeText(mContext, fileName+"下载失败"+e.toString(), 0).show();
										Log.e("0090",fileName+"下载失败"+e.getMessage());
										
									}
								});
							} 
						}
						downIng = false;
						listPath = new ArrayList<String>();
				}
			}).start();
		}



	}

	public List<String> listPath = new ArrayList<String>();

	public String getWorkSpaces(String url){
		String[] urls = url.split("/");
		StringBuffer sb = new StringBuffer();
		for(int i = 0 ; i< urls.length-1;i++){
			sb.append(urls[i]+"/");
		}
		return sb.toString();
	}
	/**
	 * 下载文件线程
	 */
	private class downloadFileThread extends Thread {
		private String url;
		private String fileName;
		private downloadFileThread(String url,String fileName,int what){
			this.fileName = fileName;
			this.url = url.replace("storage/sdcard0/", "");
			this.url = url.replace("storage/emulated/0", "");
		}
		@Override
		public void run()
		{


			try {

				String sdpath = Environment.getExternalStorageDirectory() + "/";
				mSavePath = sdpath + "download";///storage/sdcard0//.fota
				File file = new File(mSavePath);
				// 判断文件目录是否存在
				if (!file.exists())
				{
					file.mkdir();
				}
				client.changeDirectory(getWorkSpaces(url));
				fls = client.list();
				
				//判断本地是否已经下载过
				File devicefile = new File(mSavePath,fileName);
				// 获取文件大小
				int length = (int) getFtpFileSizeByName(url);//文件路径
				if(devicefile.exists()&&devicefile.getName().equals(fileName)){//本地存在
					if(devicefile.length()==length){
						Log.i("CameraZQL", "本地存在"+devicefile.getPath());
						return;//终止donload 	
					}
				}
				
				client.download(
						fileName,//IMG_20170411_103142.jpg
						devicefile,
						new DownloadFTPDataTransferListener(length));
				mHandler.post(new Runnable() {

					@Override
					public void run() {
						Toast.makeText(mContext, "下载成功", 0).show();
					}
				});

				client.changeDirectory("//");

			} catch (Exception ex) {
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						try {
							Log.e("CameraZQL", "下载失败重新登录下载"+url);
							final String mURL = url;
							disconnect();
							
							new Thread(new Runnable() {

								@Override
								public void run() {
									try {
										ftpConn = connect(ftpAddress);
										String sdpath = Environment.getExternalStorageDirectory() + "/";
										mSavePath = sdpath + "download";///storage/sdcard0//.fota
										File file = new File(mSavePath);
										// 判断文件目录是否存在
										if (!file.exists())
										{
											file.mkdir();
										}
										client.currentDirectory();
										client.changeDirectory(getWorkSpaces(mURL));
										
										fls = client.list();
										
										//判断本地是否已经下载过
										File devicefile = new File(mSavePath,fileName);
										// 获取文件大小
										int length = (int) getFtpFileSizeByName(mURL);//文件路径
										if(devicefile.exists()&&devicefile.getName().equals(fileName)){//本地存在
											if(devicefile.length()==length){
												Log.i("CameraZQL", "本地存在"+devicefile.getPath());
												return;//终止donload 	
											}
										}
										
										client.download(
												fileName,//IMG_20170411_103142.jpg
												devicefile,
												new DownloadFTPDataTransferListener(length));
										mHandler.post(new Runnable() {

											@Override
											public void run() {
												Toast.makeText(mContext, "下载成功", 0).show();
											}
										});
									} catch (Exception e) {
										e.printStackTrace();
									}  
								
								}
							}).start();
							
						} catch (Exception e) {
							e.printStackTrace();
						}  
					}
				});


			}










			/*
			try
			{
				// 判断SD卡是否存在，并且是否具有读写权限
				if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
				{
					// 获得存储卡的路径
					String sdpath = Environment.getExternalStorageDirectory() + "/";
					mSavePath = sdpath + "download";///storage/sdcard0//.fota
					fls  = client.listFiles(url);
					InputStream is = client.retrieveFileStream(url);//FTP文件路径//456641
					// 获取文件大小
					int length = (int) getFtpFileSizeByName(url);//文件路径
					// 创建输入流

					File file = new File(mSavePath);
					// 判断文件目录是否存在
					if (!file.exists())
					{
						file.mkdir();
					}
					File apkFile = new File(mSavePath,fileName);//文件名称
					if(apkFile.exists()==true){
						if(apkFile.length()==length){
							Log.e("0090", fileName+"文件已存在");
						}
					}else{
						Log.e("0090", fileName+"文件不存在 文件大小为:"+length);

					}
					Log.e("0090", fileName+"文件大小为:"+length);
					FileOutputStream fos = new FileOutputStream(apkFile);
					int count = 0;
					// 缓存
					byte buf[] = new byte[1024];
					// 写入到文件中
//					do {
//						int numread = is.read(buf);
//						count += numread;
//						// 计算进度条位置
//						progress = (int) (((float) count / length) * 100);
//						// 更新进度
////						mHandler.sendEmptyMessage(DOWNLOAD);
//						
//						if (count ==length)
//						{
//							// 下载完成
////							mHandler.sendEmptyMessage(what);
//							break;
//						}
//						Log.i("0090", "正在下载"+progress+"%"+"("+count+"/"+length+")");
//						// 写入文件
//						fos.write(buf, 0, numread);
//					} while (!cancelUpdate);// 点击取消就停止下载.



					client.setBufferSize(1024); 
			            //设置文件类型（二进制） 
					client.setFileType(FTPClient.BINARY_FILE_TYPE); 
					////Music//IMG_20170411_103142.jpg
					boolean statu = client.retrieveFile("/Music/IMG_20170411_103142.jpg", fos); 



					Log.e("0090", fileName+"文件下载完毕 statu 为："+statu);
					fos.close();
					is.close();
					client.completePendingCommand();	
				}
			} catch (MalformedURLException e)
			{
				e.printStackTrace();
			} catch (IOException e)
			{
				e.printStackTrace();
			}
			// 取消下载对话框显示
//			mDownloadDialog.dismiss();
			 */}
	};
	private void logv(String log) {
		Log.v("0090", log);
	}
	private class DownloadFTPDataTransferListener implements
	FTPDataTransferListener {

		private int totolTransferred = 0;
		private long fileSize = -1;

		public DownloadFTPDataTransferListener(long fileSize) {
			if (fileSize <= 0) {
				throw new RuntimeException(
						"the size of file muset be larger than zero.");
			}
			this.fileSize = fileSize;
		}

		@Override
		public void aborted() {
			// TODO Auto-generated method stub
			logv("FTPDataTransferListener : aborted");
		}

		@Override
		public void completed() {
			// TODO Auto-generated method stub
			logv("FTPDataTransferListener : completed");
			//			setLoadProgress(mPbLoad.getMax());

		}

		@Override
		public void failed() {
			// TODO Auto-generated method stub
			logv("FTPDataTransferListener : failed");
		}

		@Override
		public void started() {
			// TODO Auto-generated method stub
			logv("FTPDataTransferListener : started");
		}

		@Override
		public void transferred(int length) {
			totolTransferred += length;
			float percent = (float) totolTransferred / this.fileSize;
			logv("FTPDataTransferListener : transferred # percent @@" + percent);
			//			setLoadProgress((int) (percent * mPbLoad.getMax()));
		}
	}

	//    /** 
	//     * FTP下载单个文件测试 
	//     */ 
	//    public static void testDownload() { 
	//        FTPClient ftpClient = new FTPClient(); 
	//        FileOutputStream fos = null; 
	//
	//        try { 
	//            String remoteFileName = "/admin/pic/3.gif"; 
	//            fos = new FileOutputStream("c:/down.gif"); 
	//
	//            ftpClient.setBufferSize(1024); 
	//            //设置文件类型（二进制） 
	//            ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE); 
	//            ftpClient.retrieveFile(remoteFileName, fos); 
	//        } catch (IOException e) { 
	//            e.printStackTrace(); 
	//            throw new RuntimeException("FTP客户端出错！", e); 
	//        } finally { 
	//            try { 
	//                ftpClient.disconnect(); 
	//            } catch (IOException e) { 
	//                e.printStackTrace(); 
	//                throw new RuntimeException("关闭FTP连接发生异常！", e); 
	//            } 
	//        } 
	//    } 







	/* 是否取消更新 */
	private boolean cancelUpdate = false;
	/* 下载中 */
	private static final int DOWNLOAD = 1;
	Handler mHandler = new Handler(){


	};
	public boolean connect(String ftpAddress) {
		if(client==null){
			try {
			
				client = new FTPClient();
				if(client.isConnected()==true){
					return true;
				}
				client.connect(ftpAddress,2121);
				client.login("way", "way");
				mHandler.post(new Runnable() {
					@Override
					public void run() {
//						Toast.makeText(mContext, "login true", 0).show();
					}
				});
				return true;
			} catch (Exception e) {
				mHandler.post(new Runnable() {
					@Override
					public void run() {
//						Toast.makeText(mContext, "login error", 0).show();
					}
				});
				disconnect();
			}
			return false;
		}else{
			return true;
		}

	}

	private void disconnect() {
		if (client != null) {
			try {
				client.logout();
				client.disconnect(true);
			}catch (Exception ex) {
				ex.printStackTrace();
			}
			client = null;
		}
	}


	//根据文件url返回FTP目录中该文件的大小
	public long getFtpFileSizeByName(String name){
		long size = 0;
		String [] sp_str = name.split("/");
		String fileName = sp_str[sp_str.length-1];
		for(int i=0;i<fls.length;i++){
			if(fls[i].getName().equals(fileName)){
				size =  fls[i].getSize();
				break;
			}
		}
		return size;
	}
	
	   private Bitmap getImageThumbnail(String imagePath, int width, int height) {  
	        Bitmap bitmap = null;  
	        BitmapFactory.Options options = new BitmapFactory.Options();  
	        options.inJustDecodeBounds = true;  
	        // 获取这个图片的宽和高，注意此处的bitmap为null  
	        bitmap = BitmapFactory.decodeFile(imagePath, options);  
	        options.inJustDecodeBounds = false; // 设为 false  
	        // 计算缩放比  
	        int h = options.outHeight;  
	        int w = options.outWidth;  
	        int beWidth = w / width;  
	        int beHeight = h / height;  
	        int be = 1;  
	        if (beWidth < beHeight) {  
	            be = beWidth;  
	        } else {  
	            be = beHeight;  
	        }  
	        if (be <= 0) {  
	            be = 1;  
	        }  
	        options.inSampleSize = be;  
	        // 重新读入图片，读取缩放后的bitmap，注意这次要把options.inJustDecodeBounds 设为 false  
	        bitmap = BitmapFactory.decodeFile(imagePath, options);  
	        // 利用ThumbnailUtils来创建缩略图，这里要指定要缩放哪个Bitmap对象  
	        bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height,  
	                ThumbnailUtils.OPTIONS_RECYCLE_INPUT);  
	        return bitmap;  
	    }  
	  
	    /** 
	     * 获取视频的缩略图 
	     * 先通过ThumbnailUtils来创建一个视频的缩略图，然后再利用ThumbnailUtils来生成指定大小的缩略图。 
	     * 如果想要的缩略图的宽和高都小于MICRO_KIND，则类型要使用MICRO_KIND作为kind的值，这样会节省内存。 
	     * @param videoPath 视频的路径 
	     * @param width 指定输出视频缩略图的宽度 
	     * @param height 指定输出视频缩略图的高度度 
	     * @param kind 参照MediaStore.Images.Thumbnails类中的常量MINI_KIND和MICRO_KIND。 
	     *            其中，MINI_KIND: 512 x 384，MICRO_KIND: 96 x 96 
	     * @return 指定大小的视频缩略图 
	     */  
	    private Bitmap getVideoThumbnail(String videoPath, int width, int height,  
	            int kind) {  
	        Bitmap bitmap = null;  
	        // 获取视频的缩略图  
	        bitmap = ThumbnailUtils.createVideoThumbnail(videoPath, kind);  
	        bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height,  
	                ThumbnailUtils.OPTIONS_RECYCLE_INPUT);  
	        return bitmap;  
	    } 


}
