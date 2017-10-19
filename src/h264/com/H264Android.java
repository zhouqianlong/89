package h264.com;

import android.util.Log;

 

public class H264Android { 
	public static H264Android instances = new H264Android();
	public static H264Android getInstances(){
		if(instances==null){
			return instances = new H264Android();
		}else{
			return instances;
		}
	}
	VView VView = new VView();
	public int JInitDecoder(int width, int height){
		return VView.InitDecoder(width, height);
	}
	public int JUninitDecoder(){
		return VView.UninitDecoder();
	}
	public int JDecoderNal(byte[] in, int insize, byte[] out){
		return VView.DecoderNal(in, insize, out);
	}


}

class VView{ 
	VView(){
		Log.i("H264", "Ö´ÐÐh264£º"+System.currentTimeMillis());
	}
	public native int InitDecoder(int width, int height);

	public native int UninitDecoder();

	public native int DecoderNal(byte[] in, int insize, byte[] out);

	static {
		System.loadLibrary("H264Android");
	}
}