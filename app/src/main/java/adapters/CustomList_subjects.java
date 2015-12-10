package adapters;

import android.app.Activity;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.DatePicker;

import java.util.ArrayList;
import java.util.Calendar;

import functions.DBhelp;
import functions.TableEntry;
import functions.ButtonAnimation;
import nsit.app.com.nsitapp.R;
import nsit.app.com.nsitapp.SubjectRemove;

public class CustomList_subjects extends ArrayAdapter<String>{
	private final Activity context;
	private final ArrayList<String> code,title;
	TextView cod,tit,att,msg;
	LinearLayout add,rem;

	public CustomList_subjects(Activity context, ArrayList<String> a, ArrayList<String> b) {
		super(context, R.layout.subject_list_item, a);
		this.context = context;
		code=a;
		title=b;
	}
	

	@Override
	public View getView(final int position, View view2, ViewGroup parent) {
		LayoutInflater inflater = context.getLayoutInflater();
		View view= inflater.inflate(R.layout.subject_list_item, null, true);
			 cod = (TextView) view.findViewById(R.id.code);
			 tit = (TextView) view.findViewById(R.id.title);
			 att = (TextView) view.findViewById(R.id.attendance);
			 msg = (TextView) view.findViewById(R.id.message);
			 add = (LinearLayout) view.findViewById(R.id.add);
			 rem = (LinearLayout) view.findViewById(R.id.rem);


		 cod.setText(code.get(position));
		 tit.setText(title.get(position));

		refresh(position);

		 rem.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {

				Intent i = new Intent(getContext(), SubjectRemove.class);
				i.putExtra("code", code.get(position));
				ButtonAnimation btnAnimation = new ButtonAnimation();
				btnAnimation.animateButton(view, getContext());
				getContext().startActivity(i);

			}
		});



		 add.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
				final DatePicker picker = new DatePicker(getContext());
				picker.setCalendarViewShown(false);
				builder.setView(picker);
				builder.setTitle(title.get(position));
				builder.setPositiveButton("Attended", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        DBhelp mDbHelper = new DBhelp(getContext());
                        SQLiteDatabase db = mDbHelper.getWritableDatabase();

                        // Create a new map of values, where column names are the keys
                        ContentValues values = new ContentValues();
                        Calendar cal = Calendar.getInstance();
                        cal.set(Calendar.DAY_OF_MONTH, picker.getDayOfMonth());
                        cal.set(Calendar.MONTH, picker.getMonth());
                        cal.set(Calendar.YEAR, picker.getYear());

                        Log.e("date selected", "Month" + picker.getMonth() + "\nDay " + picker.getDayOfMonth());
                        values.put(TableEntry.COLUMN_NAME_SUBJECT, code.get(position));
                        values.put(TableEntry.COLUMN_NAME_DATE, cal.getTimeInMillis());
                        values.put(TableEntry.COLUMN_NAME_STATUS, "Attended");

                        db.insert(
                                TableEntry.TABLE_NAME,
                                TableEntry.COLUMN_NAME_STATUS,
                                values);

                        refresh(position);
                        notifyDataSetChanged();
                    }
                });
				builder.setNegativeButton("Missed", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        DBhelp mDbHelper = new DBhelp(getContext());
                        SQLiteDatabase db = mDbHelper.getWritableDatabase();

                        // Create a new map of values, where column names are the keys
                        Log.e("date selected", picker.getMonth() + " ");
                        ContentValues values = new ContentValues();
                        Calendar cal = Calendar.getInstance();
                        cal.set(Calendar.DAY_OF_MONTH, picker.getDayOfMonth());
                        cal.set(Calendar.MONTH, picker.getMonth());
                        cal.set(Calendar.YEAR, picker.getYear());
                        values.put(TableEntry.COLUMN_NAME_SUBJECT, code.get(position));
                        values.put(TableEntry.COLUMN_NAME_DATE, cal.getTimeInMillis());
                        values.put(TableEntry.COLUMN_NAME_STATUS, "Missed");

                        // Insert the new row, returning the primary key value of the new row
                        db.insert(
                                TableEntry.TABLE_NAME,
                                TableEntry.COLUMN_NAME_STATUS,
                                values);

                        Log.e("Calling", " " + position);
                        refresh(position);
                        notifyDataSetChanged();

                    }
                });
                ButtonAnimation btnAnimation = new ButtonAnimation();
                btnAnimation.animateButton(view, getContext());
				builder.show();
			}
		});

		AnimationSet set = new AnimationSet(true);
		TranslateAnimation slide = new TranslateAnimation(-100, 0, -100, 0);
		slide.setInterpolator(new DecelerateInterpolator(5.0f));
		slide.setDuration(300);
		Animation fade = new AlphaAnimation(0, 1.0f);
		fade.setInterpolator(new DecelerateInterpolator(5.0f));
		fade.setDuration(300);
		set.addAnimation(slide);
		set.addAnimation(fade);
		view.startAnimation(set);

		return view;
	}


	void refresh(int position){
		DBhelp mDbHelper = new DBhelp(getContext());
		SQLiteDatabase db = mDbHelper.getReadableDatabase();

// Define a projection that specifies which columns from the database
// you will actually use after this query.
		String[] projection = {
				TableEntry._ID,
				TableEntry.COLUMN_NAME_SUBJECT,
				TableEntry.COLUMN_NAME_DATE ,
				TableEntry.COLUMN_NAME_STATUS,
				TableEntry.COLUMN_NAME_NUMBER
		};

// How you want the results sorted in the resulting Cursor
		String sortOrder =
				TableEntry.COLUMN_NAME_SUBJECT + " ASC";
		String[] whereArgs = new String[] {
				code.get(position)};
		Cursor c = db.query(
				TableEntry.TABLE_NAME,  // The table to query
				projection,                               // The columns to return
				TableEntry.COLUMN_NAME_SUBJECT + " = ?",                                // The columns for the WHERE clause
				whereArgs,                            // The values for the WHERE clause
				null,                                     // don't group the rows
				null,                                     // don't filter by row groups
				sortOrder                                 // The sort order
		);


		float total = c.getCount();
		float attended=0;

		if (c.moveToFirst()){
			do{
				Log.e("Date", c.getString(c.getColumnIndex(TableEntry.COLUMN_NAME_SUBJECT)));
				if(c.getString(c.getColumnIndex(TableEntry.COLUMN_NAME_STATUS)).equals("Attended"))
					attended++;
			}while(c.moveToNext());
		}



		if(total==0)
			att.setText("?");
		else {
			float x = attended/total*100;
			float attend = attended,missed = total-attended;
			if(x<75) {

				float m =(3*missed-attend);
				int  n = (int)Math.floor(m);
				msg.setTextColor(Color.parseColor("#ff3300"));
				msg.setText("Your attendance is short. You need to attend the next " + n + " classes to be safe");
			}
			else {
				float m = (attend-3*missed)/3;
				int n = (int)Math.floor(m);
				msg.setTextColor(Color.parseColor("#33cc00"));
				if(n!=0)
					msg.setText("You are safe. You can leave "+n+" classes and still be safe.");
				else
					msg.setText("You are safe, but you should not leave any class.");

			}
				att.setText(reducePlaces(x));
		}
	}

    // Reduces number of decimal places for a float number
    public String reducePlaces(Float f){
        String pString = Float.toString(f);
        String[] parts = pString.split("\\.");

        if(parts.length>1){
            if(parts[1].length() > 2)
            {
                parts[1] = parts[1].substring(0,2);
                parts[0] = parts[0]+"."+parts[1];
            }
        }

        return parts[0];
    }
}