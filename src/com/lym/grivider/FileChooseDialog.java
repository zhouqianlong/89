/**
 * 文件选择器对话框
 */
package com.lym.grivider;

import java.util.List;

import com.ramy.minervue.R;
import com.ramy.minervue.sync.LocalFileUtil;
import com.wifitalk.Utils.MyDialogListener;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;

public class FileChooseDialog extends Dialog{
	private GridView gridView = null;//gridView对象
	private TextView emptyText = null;//当文件夹为空时显示
	List<String> fileList =null;//当前路径下的所有子文件列表
	FilePerate filePerate = null;//文件操作对象
	GridViewAdapter gridAdapter = null;//GridView的适配器
	
	MyDialogListener listener = null;//自定义的接口，用于传回返回值
	Context context = null;//上下文对象
	
	public FileChooseDialog(Context context) {
		super(context);
		this.context = context;
	}
	public FileChooseDialog(Context context,MyDialogListener listener) {
		super(context);
		this.listener = listener;
		this.context = context;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.grid_view);
	
		
		filePerate = new FilePerate();//得到一个文件操作对象
//		fileList = filePerate.getAllFile("/storage/sdcard0/Android/data/com.ramy.minervue/files");//得到根目录下的所有子目录
		fileList = filePerate.getAllFile("/storage/emulated/0/Android/data/com.ramy.minervue/files");//得到根目录下的所有子目录
		
		emptyText = (TextView)findViewById(R.id.empty_text);//TextView对象，当文件夹为空时显示“文件夹为空"
		gridView = (GridView )findViewById(R.id.grid_view);
		
		gridAdapter = new GridViewAdapter(filePerate,fileList,context);//得到适配器
		gridView.setAdapter(gridAdapter);//为GridView添加适配器
		
		//为gridView的子对象设置监听器，即当点击文件时的动作
		gridView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				String path = "";
				//得到选中文件的目录
				path = filePerate.getCurrentPath()+"//"+filePerate.getFileList().get(position);
				fileList = filePerate.getAllFile(path);//得到选择的文件路径下的文件List
				if(fileList != null){//即如果点击的不是文件而是文件夹，更新适配器的数据源
					setEmptyTextState(fileList.size());//设置TextView的状态
					gridAdapter.notifyDataSetChanged();//通知适配器，数据源以改变
				}else{
					listener.getFilePath(path);
					Toast.makeText(getContext(), path, 0).show();
					dismiss();
				}
			}
		});
	}

	
	//键盘监听事件
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK://如果按下返回键
			//如果当前已经到跟目录
			if((filePerate.getCurrentPath()).equals(FilePerate.getRootFolder())){
				this.dismiss();//如果已经返回到根目录，则关闭兑换框
				listener.getFilePath(FilePerate.getRootFolder());//得到上一级目录
				return false;
			}
//			否则得到上级目录
			String path = filePerate.getParentFolder(filePerate.getCurrentPath());
			fileList = filePerate.getAllFile(path);//更新数据源
			setEmptyTextState(fileList.size());//设置TextView的状态
			gridAdapter.notifyDataSetChanged();//更新GridAdapter
			break;
		default:
			break;
	}
	return false;
	}
	
	//设置文本的状态
	public void setEmptyTextState(int num){
		//如果该文件夹下存在文件（即num>0)则不显示textView,否则显示
		emptyText.setVisibility(View.VISIBLE);
		if(num>0){
			emptyText.setVisibility(View.GONE);
		}
	}
}
