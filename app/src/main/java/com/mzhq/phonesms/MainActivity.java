package com.mzhq.phonesms;

import java.sql.Date;
import java.text.SimpleDateFormat;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.CallLog.Calls;
import android.app.Activity;
import android.content.ContentResolver;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.util.Log;
import android.view.Menu;
import android.widget.TextView;
import android.os.Vibrator;

public class MainActivity extends Activity {

	private TextView tv,tv_phone;
	private StringBuilder sb=new StringBuilder();
	private StringBuilder sb1=new StringBuilder();
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		tv=(TextView) this.findViewById(R.id.tv);
		tv_phone=(TextView) this.findViewById(R.id.phone);
		new Thread()
		{
			public void run()
			{
				getSmsInPhone();
				GetCallsInPhone();
			}
		}.start();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	public void getSmsInPhone()   
	{
	    final String SMS_URI_ALL   = "content://sms/";         
	       
	    try{   
	        ContentResolver cr = getContentResolver();   
	        String[] projection = new String[]{"_id", "address", "person",    
	                "body", "date", "type"};   
	        Uri uri = Uri.parse(SMS_URI_ALL);   
	        Cursor cur = cr.query(uri, projection, null, null, "date desc");   
	  
	        if (cur.moveToFirst()) {     
	            String phoneNumber;          
	            String smsbody;   
	            String date;   
	            String type;   
	            
	            int nameColumn = cur.getColumnIndex("person");   
	            int phoneNumberColumn = cur.getColumnIndex("address");   
	            int smsbodyColumn = cur.getColumnIndex("body");   
	            int dateColumn = cur.getColumnIndex("date");   
	            int typeColumn = cur.getColumnIndex("type");   
	            
	            do{   
	                phoneNumber = cur.getString(phoneNumberColumn); 
	                smsbody = cur.getString(smsbodyColumn);
					if(smsbody.equals("JWUJ ") ){
						//VibratorActivity vv=new VibratorActivity() ;
						Vibrator vibrator = (Vibrator)getSystemService(VIBRATOR_SERVICE);
						long[] pattern = {800, 50, 400, 30};
						vibrator.vibrate(pattern, 2);
					}
	                   
	                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
	                Date d = new Date(Long.parseLong(cur.getString(dateColumn)));   
	                date = dateFormat.format(d);   
	                   
	                int typeId = cur.getInt(typeColumn);   
	                if(typeId == 1){   
	                    type = "接收";   
	                } else if(typeId == 2){   
	                    type = "发送";   
	                } else {   
	                    type = "";   
	                }   
	                
	                Message msg = mHandler.obtainMessage(0);  
	                Bundle b = new Bundle();// 存放数据  
		            b.putString("phone", phoneNumber);
	                b.putString("smsbody", smsbody);
	                b.putString("data", date);
	                b.putString("type", type);
	                msg.setData(b);  
	                msg.sendToTarget();  
	                             
	                
	                if(smsbody == null) smsbody = "";     
	            }while(cur.moveToNext());   
	        } 
	    } catch(SQLiteException ex) {
	        Log.d("SQLiteException", ex.getMessage());
	    }
	}
	
	private void GetCallsInPhone()
	{
		Cursor cursor = getContentResolver().query(Calls.CONTENT_URI,
			    new String[] { Calls.DURATION, Calls.TYPE, Calls.DATE, Calls.NUMBER },
			    null,
			    null,
			    Calls.DEFAULT_SORT_ORDER);
			MainActivity.this.startManagingCursor(cursor);
			boolean hasRecord = cursor.moveToFirst();
			long incoming = 0L;
			long outgoing = 0L;
			int count = 0;
			String strPhone = "";
			String date;

			while (hasRecord) {
			    int type = cursor.getInt(cursor.getColumnIndex(Calls.TYPE));
			    long duration = cursor.getLong(cursor.getColumnIndex(Calls.DURATION));
			    strPhone = cursor.getString(cursor.getColumnIndex(Calls.NUMBER));
			    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                Date d = new Date(Long.parseLong(cursor.getString(cursor.getColumnIndex(Calls.DATE))));
                date = dateFormat.format(d);


                Message msg = mHandler.obtainMessage(1);
                Bundle b = new Bundle();// 存放数据
                b.putString("phone", strPhone);
                b.putString("date", date);
                b.putLong("time", duration);
                msg.setData(b);
                msg.sendToTarget();

			    switch (type) {
			        case Calls.INCOMING_TYPE:
			            incoming += duration;
			            break;
			        case Calls.OUTGOING_TYPE:
			            outgoing += duration;
			        default:
			            break;
			    }
			    count++;
			    hasRecord = cursor.moveToNext();
			}

	}

	public Handler mHandler = new Handler() {
		  @Override  
		  public void handleMessage(Message msg) {  
		    // TODO Auto-generated method stub  
		    switch(msg.what){  
		    case 0:  
		    {  
		      Bundle b = msg.getData();  
		      String phone = b.getString("phone");
		      String smsbody = b.getString("smsbody");
		      String data = b.getString("data");
		      String type = b.getString("type");
		      sb.append("\n"+phone+"\n"+smsbody+"\n"+data+"\n"+type+"\n").append("----------------");
		      tv.setText(sb.toString());
		      
		      
		      Log.d("phone", phone);
		      Log.d("smsbody", smsbody);
		      Log.d("data", data);
		      Log.d("type", type);
		      
		    }  
		    break; 
		    case 1:
		    {
		    	Bundle b = msg.getData(); 
		    	String phone = b.getString("phone");
		    	String date = b.getString("date");
		    	long time = b.getLong("time");
		    	
		    	 sb1.append("\n"+phone+"\n"+date+"\n"+time+"\n").append("----------------");
			      tv_phone.setText(sb1.toString());
		    	Log.d("phone", phone);
		    	Log.d("date", date);
		    	Log.d("time", ""+time);
		    }
		    break;
		   }  
		   super.handleMessage(msg);  
		   }  
		};

}
