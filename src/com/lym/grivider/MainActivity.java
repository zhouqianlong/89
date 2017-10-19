package com.lym.grivider;


import java.util.List;

import com.ramy.minervue.R;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

public class MainActivity  extends Activity{
	
	List<String> fileList =null;//文件列表，也为适配器的数据源
	FilePerate filePerate = null;//文件操作对象
	GridViewAdapter gridAdapter = null;//GirdAdapter适配器
	private String filePath = "";//文件路径，用于接收文件选择器返回的路径
	
	//dialogListener实现了自定义的MyDialogListener类
	//通过该类实现获取对话框的返回值
//	MyDialogListener dialogListener = new MyDialogListener() {
//		@Override
//		public void getFilePath(String path) {
//			// TODO Auto-generated method stub
//			filePath = path;//得到返回的文件路径，其为一个回调方法
//		}
//	};
	
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.file_main_activity);
	}
	
	//按钮的动作
	public void selectFile(View view){
		//如果没有SD卡，则输出提示
		if(FilePerate.getRootFolder() == null){
			Toast.makeText(this, "没有SD卡", Toast.LENGTH_SHORT).show();
			return ;
		}
		//创建一个自定义的对话框
//		FileChooseDialog dialog = new FileChooseDialog(this,dialogListener);
//		dialog.setTitle("请选择文件");
//		dialog.show();//显示对话框
		System.out.println(filePath+"根目录");//输出返回值
	}
}

