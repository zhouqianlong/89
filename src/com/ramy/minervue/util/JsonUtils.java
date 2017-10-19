package com.ramy.minervue.util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.ramy.minervue.bean.WorkBean;

public class JsonUtils {
	
	//WorkBean –Ú¡–ªØ json
	public static String workBean_jsonBean_ToString(WorkBean workBean) throws JSONException{
		JSONObject object = new JSONObject();
		object.put("WorkName", workBean.getWorkName());
		object.put("describe", workBean.getDescribe());
		object.put("CreateTime", workBean.getCreateTime());
		object.put("EndTime", workBean.getEndTime());
		JSONArray sendNameArrau = new JSONArray();
		for(int s = 0 ; s < workBean.getSENDName().size();s++){
			sendNameArrau.put(workBean.getSENDName().get(s));
		}
		object.put("SENDName", sendNameArrau);
		JSONArray array =new JSONArray();
		for( int i = 0 ; i < workBean.getContent().size();i++){
			JSONObject object2 = new JSONObject();
			object2.put("image", workBean.getContent().get(i).getImagePath());
			object2.put("describe", workBean.getContent().get(i).getDescribe());
			array.put(object2);
		}
		object.put("WorkContent", array);
		JSONArray array2 = new JSONArray();
		JSONObject object2 = new JSONObject();
		for (int j = 0; j < workBean.getHandler().getContent().size(); j++) {
			JSONObject object3 = new JSONObject();
			object3.put("image", workBean.getHandler().getContent().get(j).getImagePath());
			object3.put("describe", workBean.getHandler().getContent().get(j).getDescribe());
			array2.put(object3);
		}
		object2.put("WorkContent", array2);
		object2.put("describe", workBean.getHandler().getDescribe());
		object.put("Handle", object2);
		return object.toString();
	}
	
	

}
