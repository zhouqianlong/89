package com.tk.ch4gas;

import java.util.Locale;

import com.mediatek.engineermode.io.EmGpio;
import com.ramy.minervue.R;
import com.tk.ch4gas.db.DBHelper;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android_serialport_api.GasDetector;
import android_serialport_api.SerialCmdUtils;

public class GasMainActivity extends FragmentActivity implements ActionBar.TabListener {
	private GasDetector detector;
	private DBHelper db;
	public float [] list = null;
	public float zero = 0;//零点输入
	public float zero_ = 0;//零点检测
	public float biaoqi = 0;//标气输入
	public float biaoqi_ = 0;//标气检测浓度
	public GasDetector getGasDetector(){
		return detector;
	}
	public static GasMainActivity instances;
	public static GasMainActivity getIntances(){
		return instances;
	}

	/**
	 * The {@link android.support.v4.view.PagerAdapter} that will provide
	 * fragments for each of the sections. We use a
	 * {@link android.support.v4.app.FragmentPagerAdapter} derivative, which
	 * will keep every loaded fragment in memory. If this becomes too memory
	 * intensive, it may be best to switch to a
	 * {@link android.support.v4.app.FragmentStatePagerAdapter}.
	 */
	SectionsPagerAdapter mSectionsPagerAdapter;

	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	ViewPager mViewPager;

	private void GpioInit() {
		boolean isTrue = false;
		isTrue = EmGpio.gpioInit();
		Log.i("GPIO", "EmGpio.gpioInit():"+isTrue);
		isTrue =EmGpio.setGpioOutput(216);
		Log.i("GPIO", "EmGpio.setGpioOutput(216):"+isTrue);
		isTrue = EmGpio.setGpioOutput(217);
		Log.i("GPIO", "EmGpio.setGpioOutput(217):"+isTrue);
		isTrue =EmGpio.setGpioDataHigh(216);
		Log.i("GPIO", "EmGpio.setGpioDataHigh(216):"+isTrue);
		isTrue =EmGpio.setGpioDataLow(217);
		Log.i("GPIO", "EmGpio.setGpioDataLow(217):"+isTrue);

	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		GpioInit();
		db = new DBHelper(this);
		instances = this;
		setContentView(R.layout.gasactivity_main);
		detector = new GasDetector(this);
		detector.close();
		// Set up the action bar.
		final ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		// Create the adapter that will return a fragment for each of the three
		// primary sections of the app.
		mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);

		// When swiping between different sections, select the corresponding
		// tab. We can also use ActionBar.Tab#select() to do this if we have
		// a reference to the Tab.
		mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
			@Override
			public void onPageSelected(int position) {
				actionBar.setSelectedNavigationItem(position);
			}
		});

		// For each of the sections in the app, add a tab to the action bar.
		for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
			// Create a tab with text corresponding to the page title defined by
			// the adapter. Also specify this Activity object, which implements
			// the TabListener interface, as the callback (listener) for when
			// this tab is selected.
			actionBar.addTab(
					actionBar.newTab()
					.setText(mSectionsPagerAdapter.getPageTitle(i))
					.setTabListener(this));
		}
		
		
	}

	@Override
	protected void onResume() {
		super.onResume();
		initXSGASValue();
	}
	public void initXSGASValue(){
		list = 	db.findGAS_CALIBRATION();
	}

	//0零点标定输入
	//1 标气标定输入
	//3 零点检测的实际值
	//4 标气检测的实际值
//	public float getBJ(){
//		if(list==null){
//			return 1;
//		}else{
//			zero    = SerialCmdUtils.getVolByPercentage(list[0]);
//			biaoqi  = SerialCmdUtils.getVolByPercentage(list[1]);
//			zero_   = list[3];
//			biaoqi_ = list[4];
//		}
//		
//		if(biaoqi_==0){
//			return 1;
//		}
//		float result = (biaoqi-(zero-zero_))/biaoqi_;
//		Log.i("GAS_CH4", "零点输入:"+zero+" 标气输入:"+biaoqi+"零点检测实际值:"+zero_+"标气检测实际值:"+biaoqi_);
//		return result;
//	}
	public float getBJ(){
		if(list==null){
			return 1;
		}else{
//			zero    = SerialCmdUtils.getVolByPercentage(list[0]);
			zero    = list[0]*10000;
			zero_   = list[3];
			
//			biaoqi  = SerialCmdUtils.getVolByPercentage(list[1]);
			biaoqi  = list[1]*10000;
			biaoqi_ = list[4];
		}
		float x = biaoqi - zero;
		float y = biaoqi_- zero_;
		if(y==0){
			return 1 ;
		}
		Log.i("GAS_CH4", "零点输入:"+zero+" 标气输入:"+biaoqi+"零点检测实际值:"+zero_+"标气检测实际值:"+biaoqi_);
		return  x/y;
	}

	@Override
	public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
		// When the given tab is selected, switch to the corresponding page in
		// the ViewPager.
		mViewPager.setCurrentItem(tab.getPosition());
	}

	@Override
	public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
	}

	@Override
	public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
	}

	/**
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
	 * one of the sections/tabs/pages.
	 */
	public class SectionsPagerAdapter extends FragmentPagerAdapter {
		Fragment gasmainfragement  = new GasMainFragement();
		Fragment gasbdfragement  = new GasBDFragement();
		Fragment gasaboutfragement  = new GasAboutFragement();
		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}
		@Override
		public Fragment getItem(int position) {
			switch (position) {
			case 0:
				return gasmainfragement;
			case 1:
				return gasbdfragement;
			case 2:
				return gasaboutfragement;
			}
			return gasmainfragement;
					
		}

		@Override
		public int getCount() {
			// Show 3 total pages.
			return 3;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			Locale l = Locale.getDefault();
			switch (position) {
			case 0:
				return getString(R.string.title_section1).toUpperCase(l);
			case 1:
				return getString(R.string.title_section2).toUpperCase(l);
			case 2:
				return getString(R.string.title_section3).toUpperCase(l);
			}
			return null;
		}
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		instances  = null;
		boolean isTrue = EmGpio.setGpioDataLow(216);
		Log.i("GPIO", "EmGpio.setGpioDataLow(216):"+isTrue);
//		isTrue =EmGpio.setGpioDataLow(217);
//		Log.i("GPIO", "EmGpio.setGpioDataLow(217):"+isTrue);
		isTrue = EmGpio.gpioUnInit();
		Log.i("GPIO", "EmGpio.gpioUnInit():"+isTrue);		
		
	}

}
