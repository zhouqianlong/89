/**
 * �ļ�ѡ�����Ի���
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
	private GridView gridView = null;//gridView����
	private TextView emptyText = null;//���ļ���Ϊ��ʱ��ʾ
	List<String> fileList =null;//��ǰ·���µ��������ļ��б�
	FilePerate filePerate = null;//�ļ���������
	GridViewAdapter gridAdapter = null;//GridView��������
	
	MyDialogListener listener = null;//�Զ���Ľӿڣ����ڴ��ط���ֵ
	Context context = null;//�����Ķ���
	
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
	
		
		filePerate = new FilePerate();//�õ�һ���ļ���������
//		fileList = filePerate.getAllFile("/storage/sdcard0/Android/data/com.ramy.minervue/files");//�õ���Ŀ¼�µ�������Ŀ¼
		fileList = filePerate.getAllFile("/storage/emulated/0/Android/data/com.ramy.minervue/files");//�õ���Ŀ¼�µ�������Ŀ¼
		
		emptyText = (TextView)findViewById(R.id.empty_text);//TextView���󣬵��ļ���Ϊ��ʱ��ʾ���ļ���Ϊ��"
		gridView = (GridView )findViewById(R.id.grid_view);
		
		gridAdapter = new GridViewAdapter(filePerate,fileList,context);//�õ�������
		gridView.setAdapter(gridAdapter);//ΪGridView���������
		
		//ΪgridView���Ӷ������ü���������������ļ�ʱ�Ķ���
		gridView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				String path = "";
				//�õ�ѡ���ļ���Ŀ¼
				path = filePerate.getCurrentPath()+"//"+filePerate.getFileList().get(position);
				fileList = filePerate.getAllFile(path);//�õ�ѡ����ļ�·���µ��ļ�List
				if(fileList != null){//���������Ĳ����ļ������ļ��У�����������������Դ
					setEmptyTextState(fileList.size());//����TextView��״̬
					gridAdapter.notifyDataSetChanged();//֪ͨ������������Դ�Ըı�
				}else{
					listener.getFilePath(path);
					Toast.makeText(getContext(), path, 0).show();
					dismiss();
				}
			}
		});
	}

	
	//���̼����¼�
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK://������·��ؼ�
			//�����ǰ�Ѿ�����Ŀ¼
			if((filePerate.getCurrentPath()).equals(FilePerate.getRootFolder())){
				this.dismiss();//����Ѿ����ص���Ŀ¼����رնһ���
				listener.getFilePath(FilePerate.getRootFolder());//�õ���һ��Ŀ¼
				return false;
			}
//			����õ��ϼ�Ŀ¼
			String path = filePerate.getParentFolder(filePerate.getCurrentPath());
			fileList = filePerate.getAllFile(path);//��������Դ
			setEmptyTextState(fileList.size());//����TextView��״̬
			gridAdapter.notifyDataSetChanged();//����GridAdapter
			break;
		default:
			break;
	}
	return false;
	}
	
	//�����ı���״̬
	public void setEmptyTextState(int num){
		//������ļ����´����ļ�����num>0)����ʾtextView,������ʾ
		emptyText.setVisibility(View.VISIBLE);
		if(num>0){
			emptyText.setVisibility(View.GONE);
		}
	}
}
