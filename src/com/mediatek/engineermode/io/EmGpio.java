package com.mediatek.engineermode.io;
public class EmGpio {
    public static native int getGpioMaxNumber();
    //开机
    public static native boolean gpioInit();
    //关机
    public static native boolean gpioUnInit();

    //输入
    public static native boolean setGpioInput(int gpioIndex);
   //输出
    public static native boolean setGpioOutput(int gpioIndex);
    //至高
	public static native boolean setGpioDataHigh(int gpioIndex);
    //
    public static native boolean setGpioDataLow(int gpioIndex);

    public static native int getCurrent(int hostNumber);

    public static native boolean setCurrent(int hostNumber, int currentDataIdx,
            int currentCmdIdx);

    static {
        System.loadLibrary("em_gpio_jni");
    }
}
