package com.jeffreypomeroy.byrdland;

import java.util.ArrayList;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

class ByrdlandDb {

	// --- general defs
	// --- test of egit commit/push
	// --- test of cleanup branch commit/push
	private static final String BYRDLANDDBNAME = "byrdlandDb";
	private static final int DATABASE_VERSION = 1;

	// --- db defs

	private static final String KEY_ROWID = "_d";

	// --- event table defs

	private static final String[] COL_NAMES = { "eventdate", "eventtime",
			"eventdesc" };
	private static final String EVENTTABLENAME = "byrdlandevents";

	// --- members table

	static ArrayList<String> theMembersDesc = new ArrayList<String>();

	// --- helper and db

	private static DbOpen ourHelper;
	private static Context ourContext;
	private static SQLiteDatabase ourDatabase;

	// --- other

	// --- define the db open helper class

	private static class DbOpen extends SQLiteOpenHelper {

		private DbOpen(Context context) {
			super(context, BYRDLANDDBNAME, null, DATABASE_VERSION);
			// TODO Auto-generated constructor stub
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			// TODO Auto-generated method stub
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			// TODO Auto-generated method stub
			// db.execSQL(DROPEVENTTABLE);
			// onCreate(db);
		}

	}

	// --- constructor for main class

	protected static void setContext(Context sentContext) {
		ourContext = sentContext;
	}

	private static void initDb() {
		if (ourHelper == null) {
			
			// ourContext is not defined since becoming a static class
			ourHelper = new DbOpen(ourContext);
		}
		if (ourDatabase == null) {
			ourDatabase = ourHelper.getWritableDatabase();
		}
	}

	protected static void checkTable(String tableNameStr,
			String tableCreateSqlStr) throws SQLException {
		initDb();

		Cursor cursor = ourDatabase.rawQuery(
				"select DISTINCT tbl_name from sqlite_master where tbl_name = '"
						+ tableNameStr + "'", null);
		if (cursor != null) {
			if (cursor.getCount() > 0) {
				cursor.close();
			} else {
				
				ourDatabase.execSQL(tableCreateSqlStr);
				cursor.close();
			}
		}
	}

	protected static void clearTable(String tableNameStr) throws SQLException {
		initDb();
		
		ourDatabase.execSQL("DELETE FROM " + tableNameStr);
	}

	protected static void dropTable(String tableNameStr) throws SQLException {
		initDb();
		try {
			ourDatabase.execSQL("drop table " + tableNameStr);
		} catch (Exception e){
			//who cares
		}
	}

	protected static void runQuery(String theQuery) throws SQLException {
		initDb();
		ourDatabase.execSQL(theQuery);
	}

	// --- close method
	// xxxf: why not called?
	@SuppressWarnings("unused")
	private void close() {
		ourHelper.close();
	}

	// --- get an ascii file of the whole db

	protected static String getEventData() throws RuntimeException {
		initDb();
		String[] columns = { KEY_ROWID, COL_NAMES[0], COL_NAMES[1],
				COL_NAMES[2] };
		String result = "";
		Cursor c = ourDatabase.query(EVENTTABLENAME, columns, null, null, null,
				null, null);

		c.getColumnIndex(KEY_ROWID);
		int iDate = c.getColumnIndex(COL_NAMES[0]);
		int iTime = c.getColumnIndex(COL_NAMES[1]);
		int iDesc = c.getColumnIndex(COL_NAMES[2]);
		for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
			result = result + c.getString(iDate) + " " + c.getString(iTime)
					+ "\n     " + c.getString(iDesc) + "\n";
		}
		return result;
	}

	// xxxf: why do I have to do this
	@SuppressWarnings("rawtypes")
	protected static ArrayList<ArrayList> getYoutubeData()
			throws RuntimeException {
		String theValue;
		ArrayList<ArrayList> theReturn = new ArrayList<ArrayList>();
		ArrayList<String> theNames = new ArrayList<String>();
		ArrayList<String> theIds = new ArrayList<String>();
		initDb();
		String[] theColumns = { KEY_ROWID, "youtubedesc", "youtubeid" };
		Cursor c = ourDatabase.query("BYRDLANDYOUTUBEVIDEOS", theColumns, null,
				null, null, null, "YOUTUBEDESC");

		int iYouTubeDescNo = c.getColumnIndex("youtubedesc");
		int iYouTubeIdNo = c.getColumnIndex("youtubeid");
		for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
			theValue = c.getString(iYouTubeDescNo);
			theValue = ByrdlandUtil.convertCodes(theValue);
			theNames.add(theValue);
			theValue = c.getString(iYouTubeIdNo);
			theValue = ByrdlandUtil.convertCodes(theValue);
			theIds.add(theValue);
		}
		// check this out xxxf
		theReturn.add(theNames);
		theReturn.add(theIds);
		return theReturn;
	}

	@SuppressWarnings("unused")
	private void loadEventData(String... eventDataStr) {
		int theNo = eventDataStr.length;
		int strLp, pos1, endOfLine;
		String theLine, theLine2, theLine3, theDate, theTime, theDesc;
		ContentValues updateDbCv = new ContentValues();
		for (strLp = 0; strLp < theNo; strLp++) {
			theLine = eventDataStr[strLp];
			endOfLine = theLine.length();
			pos1 = theLine.indexOf("?:");
			theDate = theLine.substring(0, pos1);
			pos1++;
			endOfLine = theLine.length();
			theLine2 = theLine.substring(pos1, endOfLine);
			pos1 = theLine2.indexOf("?:");
			theTime = theLine2.substring(0, pos1);
			pos1++;
			theLine3 = theLine2.substring(0, endOfLine);
			theDesc = theLine3;
			updateDbCv.clear();
			updateDbCv.put(COL_NAMES[0], theDate);
			updateDbCv.put(COL_NAMES[1], theTime);
			updateDbCv.put(COL_NAMES[2], theDesc);
			ourDatabase.insert(EVENTTABLENAME, null, updateDbCv);
		}
	}

	protected static String getMemberDesc(int thePos) throws RuntimeException {
		String theFullMemberDesc = "";
		
		if (theMembersDesc.size() == 0) {
			initDb();
			String[] theColumns = { "membersname", "membersdesc" };
			
			Cursor c = ourDatabase.query("BYRDLANDMEMBERS", theColumns, null,
					null, null, null, "MEMBERSORDER");
			
			int iMemberName = c.getColumnIndex("membersname");
			int iMemberDesc = c.getColumnIndex("membersdesc");
			c.getCount();
			// blows up after this
			for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
				
				String theMemberName = c.getString(iMemberName);
				
				String theMemberDesc = c.getString(iMemberDesc);
				
				theFullMemberDesc = theMemberName + ": " + theMemberDesc;
				theMembersDesc.add(theFullMemberDesc);
			}
		}
		theFullMemberDesc = theMembersDesc.get(thePos);
		return theFullMemberDesc;
	}

}
