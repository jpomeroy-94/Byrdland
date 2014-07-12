package com.jeffreypomeroy.byrdland;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

class ByrdlandUtil {

	// test of git system
	static FileInputStream fis;
	static	FileOutputStream fos;
	static	String syncTestFile = "synctest";
	static	ArrayList<String> theAryLst;
	static	Context utilContext;
	static	String syncDate, todayDate;
	static	String	doNothingTest;

	static protected boolean needToSync(){
		boolean doWe = false;
		todayDate = new SimpleDateFormat("MM/dd/yyyy",Locale.US).format(Calendar.getInstance().getTime());
		
		try {
			fis=utilContext.openFileInput( syncTestFile);
			byte[] dataArray = new byte[fis.available()];
			while (fis.read(dataArray) != -1){
				syncDate = new String(dataArray);
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			syncDate="00/00/0000";
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			syncDate=todayDate;					
			e.printStackTrace();
		} finally {
			try {
				//now close it
				
					fis.close();
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				
			}
		}
		
		if (!todayDate.equalsIgnoreCase(syncDate)){
			
			doWe=true;
		}
		
		return doWe;
	}	
	static void setSyncDate(){
		try {
			todayDate = new SimpleDateFormat("MM/dd/yyyy",Locale.US).format(Calendar.getInstance().getTime());
			byte[] buffer = todayDate.getBytes();
			
			fos=utilContext.openFileOutput( syncTestFile, 0);
			
			fos.write(buffer);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			
			e.printStackTrace();
		} finally {
			try {
				//now close it
				
					fos.close();
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				
			}
		}
	}
	static void setContext(Context theContext){
		utilContext=theContext;
	}
	static String convertCodes(String theLine) {
		String newLine = "";
		String delimChar = "%";
		String checkValue;
		String replaceValue = "";
		String workStr = "";
		String[] theLineAry;
		int pos, theLineNo, loopCutoff;
		pos = theLine.indexOf(delimChar);
		if (pos > 0) {
			theLineAry = theLine.split(delimChar);
			theLineNo = theLineAry.length;
			loopCutoff = theLineNo - 1;
			for (int theLp = 0; theLp < theLineNo; theLp = theLp + 2) {
				// below doesnt concatenate to newLine
				workStr = theLineAry[theLp];
				if (newLine.isEmpty()) {
					newLine = workStr;
				} else {
					newLine+=workStr;
				}
				if (theLp < loopCutoff) {
					checkValue = theLineAry[theLp + 1];
					if (checkValue.equalsIgnoreCase("sglqt")) {
						replaceValue = "'";
					} else if (checkValue.equalsIgnoreCase("dblqt")) {
						replaceValue = "\"";
					} else {
						replaceValue = "error";
					}
					//below doesnt work
					//newLine.concat(replaceValue);
					newLine+=replaceValue;
				}
			}
		} else {
			newLine = theLine;
		}
		return newLine;
	}

	static void doSplit(String theLine, String theDelim) {
		theAryLst = new ArrayList<String>();
		String theFieldStr = "";
		int startPos, endPos, theDelimLen;
		startPos = 0;
		theDelimLen = theDelim.length();
		Boolean dontGetOut = true;

		while (dontGetOut) {
			endPos = theLine.indexOf(theDelim, startPos);
			if (endPos > -1) {
				theFieldStr = theLine.substring(startPos, endPos);
				try {
					theAryLst.add(theFieldStr);
				} catch (Exception e) {
					e.printStackTrace();
				}
				endPos += theDelimLen;
				startPos = endPos;
			} else {
				theFieldStr = theLine.substring(startPos, theLine.length());
				theAryLst.add(theFieldStr);
				dontGetOut = false;
			}

		}
	}

	static String getTheField(int entryNo) {
		String theReturn = theAryLst.get(entryNo).toString();
		return theReturn;
	}
	
	//--------------- error display
	
	static void errorDisplay(String errorMsg){
		
		AlertDialog.Builder messageBox = new AlertDialog.Builder(utilContext);
		messageBox.setMessage(errorMsg);
		messageBox.setPositiveButton("ok", new DialogInterface.OnClickListener(){

			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				// TODO Auto-generated method stub
			}
			
		});
		
		messageBox.show();
	}

	//=============== JsonLib Class

	static class JsonLib {

		// --- generic defs

		static JSONObject jsonObj, jsonRowObj;
		static JSONArray jsonAry, jsonRowAry;

		// --- table meta defs

		static JSONObject tableMetaInfoObj;
		static JSONObject allTablesMetaInfoObj = new JSONObject();

		static void getFromJson(String theJson) throws JSONException {

			try {
				// object xxxf
				jsonAry = new JSONArray(theJson); // seems to always be
													// byrdlandevents json
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		static int getLength(int posNo) {
			return jsonAry.length();// jsonAry is null here xxxf
		}

		static void getRow(int posNo, int rowNo) {
			try {
				// jsonRowAry = jsonAry.getJSONArray(rowNo);//this aborts
				jsonRowObj = jsonAry.getJSONObject(rowNo);
			} catch (JSONException e) {
				
				e.printStackTrace();
			}
		}

		static String getSqlColsStr(String tableName, int posNo)
				throws JSONException {
			String theReturn;
			Object theColNameObj, theColValueObj;
			String theColName, theColValue, theColType;
			String theColNames = "(";
			String theColValues = "(";
			String delim = "";
			String sqt = "'";
			String nsqt = "";
			String useSqt;
			tableMetaInfoObj = allTablesMetaInfoObj.getJSONObject(tableName);
			Iterator<?> theColNamesItr = jsonRowObj.keys();// blows up around here
			while (theColNamesItr.hasNext()) {
				// column names
				theColNameObj = theColNamesItr.next();
				theColName = theColNameObj.toString();
				// - need to check out column name to see its type
				theColType = tableMetaInfoObj.getString(theColName);
				if (!theColType.equalsIgnoreCase("serial")) {
					theColNames += (delim + theColName);
					// column values
					theColValueObj = jsonRowObj.get(theColName);
					theColValue = theColValueObj.toString();
					if (theColType.equalsIgnoreCase("varchar")
							|| theColType.equalsIgnoreCase("date")
							|| theColType.equalsIgnoreCase("time")) {
						useSqt = sqt;
					} else {
						useSqt = nsqt;
					}
					theColValues += (delim + useSqt + theColValue + useSqt);
					delim = ",";
				}
			}
			// theReturn+=theColName+",";
			theReturn = theColNames + ") values " + theColValues + ")";
			return theReturn;
		}

		static void saveMeta(String tableName, String tableMeta)
				throws JSONException {
			tableMetaInfoObj = new JSONObject(tableMeta);
			allTablesMetaInfoObj.put(tableName, tableMetaInfoObj);
		}

		static String getSqlValsStr(int posNo) {

			String theReturn = "values (";

			return theReturn;
		}
	}
}