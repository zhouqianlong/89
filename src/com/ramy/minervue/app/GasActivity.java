package com.ramy.minervue.app;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import com.mediatek.engineermode.io.EmGpio;
import com.mediatek.engineermode.io.EmGpioOpration;
import com.ramy.minervue.bean.GasData;
import com.ramy.minervue.dao.AddressDao;
import com.ramy.minervue.service.GasService;
import com.ramy.minervue.sync.StatusManager;
import com.ramy.minervue.util.ATask;
import com.ramy.minervue.util.DataProcessUtils;
import com.ramy.minervue.view.SlipButton;
import com.ramy.minervue.view.SlipButton.OnChangedListener;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import com.ramy.minervue.*;

public class GasActivity extends Activity implements OnClickListener,
		OnChangedListener {
	public static final String TAG = "GasActivity";
	private static final String READ_DATA_CMD = "fe680124008d0d";
	private GasService gasService;
	private GasDatasReaderTask task;
	private Button takePic;
	private Button save;
	private SlipButton slipButton;
	private TextView tv_refresh;
	private TextView tv_ymd;
	private TextView tv_hms;
	private TextView tv_ch4_value;
	private TextView tv_co_value;
	private TextView tv_rh_value;
	private TextView tv_tem_value;
	private TextView tv_h2s_value;
	private AutoCompleteTextView et_address;
	private TextView tv_o2_value;
	private RelativeLayout loading_data;
	private RelativeLayout show_data;
	private String gasContent;
	private boolean isStop;
	private GasQueryTask queryTask;
	private AddressAdapter adapter;
	private AddressDao ad;
	private ScheduledExecutorService scheduledExecutorService;
	private int count;
	private AlertDialog.Builder builder;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		StatusManager.setGasPhoto(true);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.gas_activity);
		GpioInit();
		initView();
		setListener();
		ad = new AddressDao(this);
		scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
		startQuery();
		
//		Intent intent = new Intent(GasActivity.this,
//				GasPhotographerActivity.class);
//		intent.putExtra(getPackageName() + ".GasData", "123" + "\n"
//				+ getGasContent());
//		startActivity(intent);
	}

	private void startQuery() {
		if (task == null || task.isFinished()) {
			task = null;
			gasService = new GasService(this);
			task = new GasDatasReaderTask(this);
			task.start(READ_DATA_CMD);
		}
	}
	
	@Override
	protected void onResume() {
		if(EmGpio.setGpioDataHigh(217)&&EmGpio.setGpioDataHigh(216)){
//			Toast.makeText(this, "setGpioDataHigh成功", 0).show();
		}else{
//			Toast.makeText(this, "setGpioDataHigh失败", 0).show();
		}
		super.onResume();
	}

	private void GpioInit() {
		EmGpio.gpioInit();
		// EmGpioOpration.turnOff_Ext_out2();
		// EmGpioOpration.turnOn_Ext_out2();
		EmGpio.setGpioOutput(216);
		EmGpio.setGpioOutput(217);
		//EmGpio.setGpioDataLow(217);

		EmGpio.setGpioDataHigh(217);

	}

	private void setListener() {
		et_address.setOnItemClickListener(new MyOnItemClickListener());
		slipButton.SetOnChangedListener(this);
		takePic.setOnClickListener(this);
		save.setOnClickListener(this);
	}

	public void initView() {
		loading_data = (RelativeLayout) findViewById(R.id.loading_data);
		show_data = (RelativeLayout) findViewById(R.id.show_data);
		tv_refresh = (TextView) findViewById(R.id.auto_refresh_tv);
		tv_ymd = (TextView) findViewById(R.id.ymd);
		tv_hms = (TextView) findViewById(R.id.hms);
		tv_ch4_value = (TextView) findViewById(R.id.tv_ch4_value);
		tv_o2_value = (TextView) findViewById(R.id.tv_o2_value);
		tv_h2s_value = (TextView) findViewById(R.id.tv_h2s_value);
		tv_rh_value = (TextView) findViewById(R.id.tv_rh_value);
		tv_tem_value = (TextView) findViewById(R.id.tv_tem_value);
		tv_co_value = (TextView) findViewById(R.id.tv_co_value);
		et_address = (AutoCompleteTextView) findViewById(R.id.et_adress);
		slipButton = (SlipButton) findViewById(R.id.auto_refresh_bt);
		takePic = (Button) findViewById(R.id.takepic);
		save = (Button) findViewById(R.id.save);
		adapter = new AddressAdapter(this, null);
		et_address.setAdapter(adapter);
		loading_data.setVisibility(View.VISIBLE);
		show_data.setVisibility(View.INVISIBLE);
	}

	private class GasDatasReaderTask extends ATask<String, String, Integer> {
		public static final int OPEN_SERIAL_PORT_FAIL = 1;
		public static final int RECEIVE_DATA_ERROR = 2;
		public static final int RECEIVE_DATA_SUCCESS = 4;
		public static final int RECEIVE_DATA_TIME_OUT = 3;
		private GasActivity activity;

		public GasDatasReaderTask(GasActivity activity) {
			this.activity = activity;
		}

		@Override
		protected void onPreExecute() {
			if (builder == null) {
				builder = new AlertDialog.Builder(activity);
				builder.setTitle(R.string.error_tip);
				builder.setCancelable(false);
				builder.setPositiveButton(R.string.confirm,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								finish();
//								loading_data.setVisibility(View.INVISIBLE);
//								show_data.setVisibility(View.VISIBLE);
//										tv_ch4_value.setText("0%");
//										tv_o2_value.setText("0%");
//										tv_h2s_value.setText("0ppm");
//										tv_co_value.setText("0ppm");
//										tv_rh_value.setText("0%");
//										tv_tem_value.setText("28°C");
//								tv_ymd.setText("2016");
//								tv_hms.setText("08-01");
							}
						});
			}
		}

		protected synchronized Integer doInBackground(String... params) {
			gasService.close();
			if (!gasService.open()) {
				gasService.close();
				return OPEN_SERIAL_PORT_FAIL;
			}
			gasService.sendReadCmd(params[0]);
			return gasService.getReplysData();
		}

		protected void onPostExecute(Integer result) {
			switch (result) {
			case OPEN_SERIAL_PORT_FAIL:
				builder.setMessage(R.string.operation_failed);
				builder.create().show();
				StatusManager.setGasPhoto(false);
				break;
			case RECEIVE_DATA_ERROR:
			case RECEIVE_DATA_TIME_OUT:
				if (queryTask == null || count == 3) {
					builder.setMessage(R.string.invalid_state);
					builder.create().show();
					StatusManager.setGasPhoto(false);
				} else {
					count++;
				}
				break;
			case RECEIVE_DATA_SUCCESS:
				count = 0;
				loading_data.setVisibility(View.INVISIBLE);
				show_data.setVisibility(View.VISIBLE);
				setViews();
				break;
			default:
				break;
			}
			gasService = null;
		}

		private void setViews() {
			DataProcessUtils dp = gasService.getDataProcessUtils();
			StringBuffer buffer = new StringBuffer();
			List<GasData> gasdatas = dp.getGasData();
			for (GasData gd : gasdatas) {
				String gasName = gd.getGasName();
				String gasValue = gd.getGasValue();
				buffer.append(gasName + " :");
				if ("CH4".equals(gasName)) {
					tv_ch4_value.setText(gasValue);
					buffer.append(gasValue + "%");
				} else if ("O 2".equals(gasName)) {
					tv_o2_value.setText(gasValue);
					buffer.append(gasValue + "%");
				} else if ("H2S".equals(gasName)) {
					tv_h2s_value.setText(gasValue);
					buffer.append(gasValue + "ppm");
				} else if ("C O".equals(gasName)) {
					tv_co_value.setText(gasValue);
					buffer.append(gasValue + "ppm");
				} else if ("R H".equals(gasName)) {
					tv_rh_value.setText(gasValue);
					buffer.append(gasValue + "%");
				} else if ("Tem".equals(gasName)) {
					tv_tem_value.setText(gasValue);
					buffer.append(gasValue + "°C");
				}
				buffer.append("\n");
			}

			String ymd = dp.getYMD();
			String hms = dp.getHMS();
			buffer.append(ymd + "  " + hms + "\n");
			setGasContent(new String(buffer));
			tv_ymd.setText(ymd);
			tv_hms.setText(hms);
		}
	}

	@Override
	protected void onPause() {
		finish();
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		StatusManager.setGasPhoto(false);
		if (scheduledExecutorService != null) {
			isStop = true;
			scheduledExecutorService.shutdown();
		}
		if (task != null) {
			task.cancelAndClear(true);
			task = null;
		}
		if (gasService != null) {
			gasService.close();
		}
		if (queryTask != null) {
			queryTask = null;
		}
		/*if(EmGpio.setGpioDataLow(216) &&  EmGpio.setGpioDataLow(217) && EmGpio.gpioUnInit()){
			Log.i(TAG, "success!!");
			
		}*/
		EmGpio.setGpioDataLow(216);
		EmGpio.setGpioDataLow(217);
		EmGpio.gpioUnInit();
		super.onDestroy();
	}

	public void onClick(View v) {
		String address = et_address.getText().toString().trim();
		if (TextUtils.isEmpty(address)) {
			Toast.makeText(this, R.string.adress, 1).show();
			return;
		}
		ad.addData(address);
		switch (v.getId()) {
		case R.id.takepic:
			Intent intent = new Intent(GasActivity.this,
					GasPhotographerActivity.class);
			intent.putExtra(getPackageName() + ".GasData", address + "\n"
					+ getGasContent());
			startActivity(intent);
			finish();
			break;
		case R.id.save:
			MainService service = MainService.getInstance();
			String filename = service.getSyncManager().getLocalFileUtil()
					.generateSensorTxtFilename();
			try {
				FileOutputStream fous = new FileOutputStream(filename);
				fous.write((address + "\n").getBytes());
				fous.write(getGasContent().getBytes());
				Toast.makeText(this, R.string.file_save_success, 1).show();
				finish();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			break;
		}

	}

	private String getGasContent() {
		return gasContent;
	}

	private void setGasContent(String gasContent) {
		this.gasContent = gasContent;
	}

	@Override
	public void OnChanged(boolean CheckState) {
		isStop = !CheckState;
		if (CheckState) {
			if (queryTask == null) {
				queryTask = new GasQueryTask();
				scheduledExecutorService.scheduleWithFixedDelay(
						new GasQueryTask(), 3, 15, TimeUnit.SECONDS);
			}
			tv_refresh.setText("自动刷新已开启");
		} else {
			tv_refresh.setText("自动刷新已关闭");
		}
	}

	private class GasQueryTask implements Runnable {
		@Override
		public void run() {
			if (isStop) {
				return;
			}
			startQuery();
		}

	}

	private class MyOnItemClickListener implements OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			Cursor cursor = (Cursor) adapter.getItem(position);
			String address = cursor
					.getString(AddressAdapter.ADDRESS_CULUMN_INDEX);
			et_address.setText(address);

		}

	}

	private class AddressAdapter extends CursorAdapter {
		private final static int ADDRESS_CULUMN_INDEX = 1;
		private LayoutInflater inflater;
		private AddressDao addressDao;

		@SuppressWarnings("deprecation")
		public AddressAdapter(Context context, Cursor c) {
			super(context, c);
			inflater = LayoutInflater.from(context);
			addressDao = new AddressDao(context);
		}

		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			View view = inflater.inflate(R.layout.address_item, null);
			AddressViews addressViews = new AddressViews();
			addressViews.tv_address = (TextView) view
					.findViewById(R.id.tv_address);
			view.setTag(addressViews);
			return view;
		}

		public void bindView(View view, Context context, Cursor cursor) {
			AddressViews addressViews = (AddressViews) view.getTag();
			String address = cursor.getString(ADDRESS_CULUMN_INDEX);
			addressViews.tv_address.setText(address);
		}

		@Override
		public Cursor runQueryOnBackgroundThread(CharSequence constraint) {
			if (TextUtils.isEmpty(constraint)) {
				return null;
			}
			return addressDao.getAllDatas(constraint.toString());

		}

		private final class AddressViews {
			TextView tv_address;
		}
	}

}
