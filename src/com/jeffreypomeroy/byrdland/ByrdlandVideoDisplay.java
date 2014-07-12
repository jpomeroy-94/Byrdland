package com.jeffreypomeroy.byrdland;

import java.util.ArrayList;

import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerFragment;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;

public class ByrdlandVideoDisplay extends YouTubeFailureRecoveryActivity
		implements OnClickListener {

	//test of git system
	ArrayList<String> theSongNames;
	ArrayList<String> theYoutubeIds;
	YouTubePlayer thePlayer;
	String byrdlandStartId;
	Context videoContext;

	@SuppressWarnings("unchecked")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.byrdlandvideodisplay);
		int dmy;
		dmy=1;
		// - get song names and youtubeids
		try {
			// xxxf: why below is needed?
			@SuppressWarnings("rawtypes")
			ArrayList<ArrayList> theReturn = ByrdlandDb.getYoutubeData();
			
			theSongNames = theReturn.get(0);
			theYoutubeIds = theReturn.get(1);
			byrdlandStartId = theYoutubeIds.get(0);
			

			// listview setup

			ListView theListView = (ListView) findViewById(R.id.lvChooseVideo);

			final ArrayList<String> theList = new ArrayList<String>();
			
			for (int theListCtr = 0; theListCtr < theSongNames.size(); theListCtr++) {
				theList.add(theSongNames.get(theListCtr));
			}
			theListView.setAdapter(new ArrayAdapter<String>(this,
					R.layout.listviewrow, theList));

			// - set OnItemClickListener and change youtube
			theListView.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> parent, View theView,
						int position, long id) {
					// TODO Auto-generated method stub
					String byrdlandId = theYoutubeIds.get(position);
					thePlayer.cueVideo(byrdlandId);
				}

			});

			
			YouTubePlayerFragment youTubePlayerFragment = (YouTubePlayerFragment) getFragmentManager()
					.findFragmentById(R.id.youtube_fragment);
			youTubePlayerFragment.initialize(DeveloperKey.DEVELOPER_KEY, this);
			ImageButton btBvdExit = (ImageButton) findViewById(R.id.btBvdExit);
			btBvdExit.setOnClickListener(this);
			
		} catch (Exception e) {

			String theMessage="You need to sync first!";
			ByrdlandUtil.errorDisplay(theMessage);
			finish();

		}

	}

	@Override
	public void onInitializationSuccess(YouTubePlayer.Provider provider,
			YouTubePlayer player, boolean wasRestored) {
		if (!wasRestored) {
			
			thePlayer = player;
			thePlayer.cueVideo(byrdlandStartId);
		}
	}

	@Override
	protected YouTubePlayer.Provider getYouTubePlayerProvider() {
		
		return (YouTubePlayerFragment) getFragmentManager().findFragmentById(
				R.id.youtube_fragment);
	}

	@Override
	public void onClick(View clickNo) {
		// TODO Auto-generated method stub
		switch (clickNo.getId()) {
		case R.id.btBvdExit:
			finish();
			break;
		}
	}

}
