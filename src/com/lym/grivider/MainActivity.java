package com.lym.grivider;


import java.util.List;

import com.ramy.minervue.R;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

public class MainActivity  extends Activity{
	
	List<String> fileList =null;//�ļ��б�ҲΪ������������Դ
	FilePerate filePerate = null;//�ļ���������
	GridViewAdapter gridAdapter = null;//GirdAdapter������
	private String filePath = "";//�ļ�·�������ڽ����ļ�ѡ�������ص�·��
	
	//dialogListenerʵ�����Զ����MyDialogListener��
	//ͨ������ʵ�ֻ�ȡ�Ի���ķ���ֵ
//	MyDialogListener dialogListener = new MyDialogListener() {
//		@Override
//		public void getFilePath(String path) {
//			// TODO Auto-generated method stub
//			filePath = path;//�õ����ص��ļ�·������Ϊһ���ص�����
//		}
//	};
	
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.file_main_activity);
	}
	
	//��ť�Ķ���
	public void selectFile(View view){
		//���û��SD�����������ʾ
		if(FilePerate.getRootFolder() == null){
			Toast.makeText(this, "û��SD��", Toast.LENGTH_SHORT).show();
			return ;
		}
		//����һ���Զ���ĶԻ���
//		FileChooseDialog dialog = new FileChooseDialog(this,dialogListener);
//		dialog.setTitle("��ѡ���ļ�");
//		dialog.show();//��ʾ�Ի���
		System.out.println(filePath+"��Ŀ¼");//�������ֵ
	}
}

