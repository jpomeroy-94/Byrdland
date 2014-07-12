package com.jeffreypomeroy.byrdland;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.TextView;

public class ByrdlandMain extends Activity implements OnClickListener {
	//test of git system
	ImageButton exitButton, syncButton, videosButton, eventsButton, allenImage,
			helenImage, marlinImage, kennethImage, sueImage;
	ByrdlandSync syncObj;
	WebView ourBrow;
	TextView tvDisplay;
	BandMembers bandMembers;
	static FileInputStream fis;
	static FileOutputStream fos;
	static String syncTestFile = "synctest";
	
	public enum BandMembers {
		ALLEN, SUE, KENNETH, HELEN, MARLIN
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.byrdlandmain);
		initializeVars();
		setListeners();
			checkSync();
		}

	public void initializeVars() {

		// buttons

		exitButton = (ImageButton) findViewById(R.id.ibExit);
		syncButton = (ImageButton) findViewById(R.id.ibSync);
		videosButton = (ImageButton) findViewById(R.id.ibVideos);
		eventsButton = (ImageButton) findViewById(R.id.ibEvents);

		// band

		allenImage = (ImageButton) findViewById(R.id.ibAllen);
		helenImage = (ImageButton) findViewById(R.id.ibHelen);
		marlinImage = (ImageButton) findViewById(R.id.ibMarlin);
		sueImage = (ImageButton) findViewById(R.id.ibSue);
		kennethImage = (ImageButton) findViewById(R.id.ibKenneth);

		// for gifs

		ourBrow = (WebView) findViewById(R.id.wvBrowser);

		ourBrow.setWebViewClient(new OurViewClient());
		try {
			ourBrow.loadUrl("file:///android_asset/lightning.htm");
		} catch (Exception e) {
			ByrdlandUtil.errorDisplay(e.getMessage());
			}
		// for descriptions

		tvDisplay = (TextView) findViewById(R.id.tvDisplay);
		
		// get sync object
		
		syncObj = new ByrdlandSync();

		// util setup error reporting needs context

		ByrdlandUtil.setContext(ByrdlandMain.this);
		ByrdlandDb.setContext(ByrdlandMain.this);
	}

	public void setListeners() {
		exitButton.setOnClickListener(this);
		syncButton.setOnClickListener(this);
		videosButton.setOnClickListener(this);
		eventsButton.setOnClickListener(this);
		allenImage.setOnClickListener(this);
		helenImage.setOnClickListener(this);
		marlinImage.setOnClickListener(this);
		sueImage.setOnClickListener(this);
		kennethImage.setOnClickListener(this);
	}
	private void checkSync(){
		try {
			if (ByrdlandUtil.needToSync()){
				syncObj.getFromServer();
				
				ByrdlandUtil.setSyncDate();
				
			}
		}
			catch (Exception e2){
				//do nothing
				;
			}
	}

	@Override
	public void onClick(View buttonView) {
		// TODO Auto-generated method stub
		switch (buttonView.getId()) {
		case R.id.ibExit:
			finish();
			break;
		case R.id.ibSync:
				ourBrow.setVisibility(View.VISIBLE);
			// syncObj.loadDmyStuff();
			syncObj.getFromServer();
			Handler handler = new Handler();
			handler.postDelayed(new Runnable() {
				@Override
				public void run() {
					ourBrow.setVisibility(View.GONE);
				}
			}, 2000);

			break;
		case R.id.ibVideos:
			runYouTube();
			break;
		case R.id.ibEvents:
			String theReport = getTheReport();
			if (!theReport.isEmpty()){
				printDialog(theReport);
			}
			break;
		case R.id.ibAllen:
			describeBandMember(BandMembers.ALLEN);
			break;
		case R.id.ibHelen:
			describeBandMember(BandMembers.HELEN);
			break;
		case R.id.ibKenneth:
			describeBandMember(BandMembers.KENNETH);
			break;
		case R.id.ibMarlin:
			describeBandMember(BandMembers.MARLIN);
			break;
		case R.id.ibSue:
			describeBandMember(BandMembers.SUE);
			break;
		}
	}

	public void describeBandMember(BandMembers theMember) {

		int thePos = theMember.ordinal();
		try {
			String memberDesc = ByrdlandDb.getMemberDesc(thePos);

			tvDisplay.setText(memberDesc);
			tvDisplay.setVisibility(View.VISIBLE);
			Handler handler = new Handler();
			handler.postDelayed(new Runnable() {
				@Override
				public void run() {
					tvDisplay.setVisibility(View.INVISIBLE);
				}
			}, 4000);
		} catch (Exception e) {
			String theMessage = "You need to sync first!";
			ByrdlandUtil.errorDisplay(theMessage);

		}
	}

	public void printDialog(String theText) {

		tvDisplay.setText(theText);
		tvDisplay.setVisibility(View.VISIBLE);
		Handler handler = new Handler();
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				tvDisplay.setVisibility(View.INVISIBLE);
			}
		}, 10000);
	}

	private void runYouTube() {

		Intent theIntent = new Intent();
		String thePackageName = getPackageName();
		String theClassName = thePackageName + ".ByrdlandVideoDisplay";
		theClassName = thePackageName + ".ByrdlandVideoDisplay";
		theIntent.setComponent(new ComponentName(thePackageName, theClassName));

		try {
			startActivity(theIntent);
		} catch (Exception e) {
			String theMessage = "You need to sync first!";
			ByrdlandUtil.errorDisplay(theMessage);
			
		}
		
	}

	public String getTheReport() {
		String theReport = "";
		try {
			theReport = ByrdlandDb.getEventData();
		} catch (Exception e) {
			String theMessage = "You need to sync first!";
			ByrdlandUtil.errorDisplay(theMessage);
		}
		return theReport;
	}

	public class ByrdlandSync {

		String theLocalUrlPath = "http://192.168.0.59/index.php?job=getbyrdlandstuff&operation=getbyrdlandstuff";
		String theUrlPath = "http://jeffreypomeroy.com/index.php?job=getbyrdlandstuff&operation=getbyrdlandstuff";
		public Object theRequest;

		public void getFromServer() throws RuntimeException {
			theRequest = new GetRequestAsync().execute(theUrlPath);
		}

		public void doUpdate(String theReturnStr) throws JSONException {
			// printDialog("- touch outside of box to erase -", theData,
			// ByrdlandMain.this);
			String theLine, theCode, theParam1, theParam2;
			int theReturnAryLen, returnLp;

			String[] theReturnAry = theReturnStr.split("\n");
			theReturnAryLen = theReturnAry.length;

			for (returnLp = 0; returnLp < theReturnAryLen; returnLp++) {

				theLine = theReturnAry[returnLp];
				// String[] theLineAry = theLine.split(":");//
				// PatternSyntaxException if ?:
				ByrdlandUtil.doSplit(theLine, "?:");

				theCode = ByrdlandUtil.getTheField(0);
				theParam1 = ByrdlandUtil.getTheField(1);
				theParam2 = ByrdlandUtil.getTheField(2);// code: instbl, table:
														// byrdlandyoutubevideos,
														// json: events!!!, lp:
														// 7

				
				if (theCode.equalsIgnoreCase("chktbl")) {
					ByrdlandDb.checkTable(theParam1, theParam2);

				} else if (theCode.equalsIgnoreCase("metatbl")) {
					
					ByrdlandUtil.JsonLib.saveMeta(theParam1, theParam2);
					

				} else if (theCode.equalsIgnoreCase("clrtbl")) {
					
					ByrdlandDb.clearTable(theParam1);
					

				} else if (theCode.equalsIgnoreCase("instbl")) {
					
					// - need to make this insert table
					ByrdlandUtil.JsonLib.getFromJson(theParam2);// theParam2 is
																// null for
																// byrdlandmembers
					int noItems = ByrdlandUtil.JsonLib.getLength(0);
					int retrieveLp;
					
					for (retrieveLp = 0; retrieveLp < noItems; retrieveLp++) {
						ByrdlandUtil.JsonLib.getRow(0, retrieveLp);// hangs on
																	// this line
						String queryCols = ByrdlandUtil.JsonLib.getSqlColsStr(
								theParam1, 0);
						String queryLine = "insert into " + theParam1 + " "
								+ queryCols;
						ByrdlandDb.runQuery(queryLine);
					}
					
				} else if (theCode.equalsIgnoreCase("drptbl")) {
					
					ByrdlandDb.dropTable(theParam1);
					
				}
			}
		}

		public class GetRequestAsync extends AsyncTask<String, Void, String> {

			public BufferedReader in;
			public String returnData;
			String dmy;

			@Override
			protected void onPostExecute(String result) {
				// TODO Auto-generated method stub
				super.onPostExecute(result);
				try {
					doUpdate(result);
				} catch (JSONException e) {
					ByrdlandUtil.errorDisplay(e.getMessage());
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			@Override
			protected String doInBackground(String... theUrlPath) {
				// TODO Auto-generated method stub
				URI theUrl;
				HttpResponse response;
				BufferedReader in;
				HttpClient theClient = new DefaultHttpClient();
				HttpGet request = new HttpGet();// example has mUrl in parens
				try {
					theUrl = new URI(theUrlPath[0]);
					request.setURI(theUrl);
					response = theClient.execute(request);
					in = new BufferedReader(new InputStreamReader(response
							.getEntity().getContent()));
					StringBuffer sb = new StringBuffer("");
					String theLine = "";
					String nl = System.getProperty("line.separator");
					while ((theLine = in.readLine()) != null) {
						sb.append(theLine + nl);
					}
					in.close();
					// xxxf
					returnData = sb.toString();

				} catch (URISyntaxException e) {
					// TODO Auto-generated catch block
					ByrdlandUtil.errorDisplay(e.getMessage());
					e.printStackTrace();
				} catch (ClientProtocolException e) {
					// TODO Auto-generated catch block
					ByrdlandUtil.errorDisplay(e.getMessage());
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					ByrdlandUtil.errorDisplay(e.getMessage());
					e.printStackTrace();
				}
				return returnData;
			}
		}

	}

	public class OurViewClient extends WebViewClient {

		@Override
		public boolean shouldOverrideUrlLoading(WebView theView, String url) {
			// TODO Auto-generated method stub
			theView.loadUrl(url);
			return true;
		}

	}

}