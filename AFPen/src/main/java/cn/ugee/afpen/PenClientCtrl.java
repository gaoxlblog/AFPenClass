package cn.ugee.afpen;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.afpensdk.pen.DPenCtrl;
import com.afpensdk.pen.penmsg.IAFPenDotListener;
import com.afpensdk.pen.penmsg.IAFPenMsgListener;
import com.afpensdk.pen.penmsg.JsonTag;
import com.afpensdk.pen.penmsg.PenMsg;
import com.afpensdk.pen.penmsg.PenMsgType;
import com.afpensdk.util.LogUtil;

import org.json.JSONException;
import org.json.JSONObject;

public class PenClientCtrl implements IAFPenMsgListener//,IAFPenOfflineDataListener
{
    public static PenClientCtrl myInstance;

	private DPenCtrl iPenCtrl;

	private Context context;


    public String lastTryConnectAddr;
    public String lastTryConnectName;
    public int lastDotsCount;
	private PenClientCtrl(Context context )
	{
		this.context = context;

		iPenCtrl = DPenCtrl.getInstance();
		iPenCtrl.setListener( this );

        setContext(context);
	}

    public static synchronized PenClientCtrl getInstance(Context context )
	{
		if ( myInstance == null )
		{
			myInstance = new PenClientCtrl( context );
		}

		return myInstance;
	}

    public boolean connect( String address)
	{
		return iPenCtrl.connect( address );
	}
    public boolean connect( String name,String address)
    {
        lastTryConnectName = name;
        lastTryConnectAddr = address;
        return connect( address );
    }

    public void disconnect()
	{
		iPenCtrl.disconnect();
	}

	@Override
	public void onReceiveMessage(PenMsg penMsg )
	{
        Log.e("gaoxiaolin",", penMsg.penMsgType="+ penMsg.penMsgType);
		switch ( penMsg.penMsgType ) {

            case PenMsgType.FIND_DEVICE: {
                JSONObject obj = penMsg.getContentByJSONObject();
                try {
                    String penAddress = obj.getString(JsonTag.STRING_PEN_MAC_ADDRESS);
                    String penName;

                    if(obj.has(JsonTag.STRING_DEVICE_NAME))
                        penName = obj.getString(JsonTag.STRING_DEVICE_NAME);
                    else
                        penName = "NULL";
                    LogUtil.e("afble find : pen name="+penName + " addr="+penAddress);
                    boolean res = connect(penName, penAddress);
                    LogUtil.e("connetct r="+String.valueOf(res));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;

            }case PenMsgType.PEN_CONNECTION_TRY: {
                break;
            }case PenMsgType.PEN_CONNECTION_SUCCESS: {
                Intent intent = new Intent(Const.Broadcast.ACTION_PEN_MESSAGE);
                intent.putExtra(Const.Broadcast.MESSAGE_TYPE, penMsg.penMsgType);
                context.sendBroadcast(intent);
                break;
            }case PenMsgType.PEN_FW_VER: {
                JSONObject obj = penMsg.getContentByJSONObject();
                try {

                    Intent intent = new Intent(Const.Broadcast.ACTION_PEN_MESSAGE);
                    intent.putExtra(Const.Broadcast.MESSAGE_TYPE, penMsg.penMsgType);
                    intent.putExtra(JsonTag.STRING_PEN_FW_VERSION, obj.getString(JsonTag.STRING_PEN_FW_VERSION));
                    context.sendBroadcast(intent);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
            }case PenMsgType.PEN_CUR_BATT: {
                JSONObject obj = penMsg.getContentByJSONObject();
                try {

                    Intent intent = new Intent(Const.Broadcast.ACTION_PEN_MESSAGE);
                    intent.putExtra(Const.Broadcast.MESSAGE_TYPE, penMsg.penMsgType);
                    intent.putExtra(JsonTag.INT_BATT_VAL, obj.getInt(JsonTag.INT_BATT_VAL));
                    context.sendBroadcast(intent);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
            }case PenMsgType.PEN_CUR_MEMOFFSET: {
                JSONObject obj = penMsg.getContentByJSONObject();
                try {
                    Intent intent = new Intent(Const.Broadcast.ACTION_PEN_MESSAGE);
                    intent.putExtra(Const.Broadcast.MESSAGE_TYPE, penMsg.penMsgType);
                    intent.putExtra(JsonTag.INT_DOTS_MEMORY_OFFSET, obj.getInt(JsonTag.INT_DOTS_MEMORY_OFFSET));
                    context.sendBroadcast(intent);
                    lastDotsCount = obj.getInt(JsonTag.INT_DOTS_MEMORY_OFFSET);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
            }case PenMsgType.PEN_FLASH_USED_AMOUNT: {
                JSONObject obj = penMsg.getContentByJSONObject();
                try {
                    Intent intent = new Intent(Const.Broadcast.ACTION_PEN_MESSAGE);
                    intent.putExtra(Const.Broadcast.MESSAGE_TYPE, penMsg.penMsgType);
                    intent.putExtra(JsonTag.LONG_FLASH_USED, obj.getInt(JsonTag.LONG_FLASH_USED));
                    context.sendBroadcast(intent);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
            }
//            case PenMsgType.PEN_FLASH_DATA_DELETED: {
//                Intent intent = new Intent(Const.Broadcast.ACTION_PEN_MESSAGE);
//                intent.putExtra(Const.Broadcast.MESSAGE_TYPE, penMsg.penMsgType);
//                context.sendBroadcast(intent);
//                break;
//            }
            case PenMsgType.PEN_CONNECTION_TIMEOUT:{
                Intent intent = new Intent(Const.Broadcast.ACTION_PEN_MESSAGE);
                intent.putExtra(Const.Broadcast.MESSAGE_TYPE, penMsg.penMsgType);
                context.sendBroadcast(intent);
                break;
            }
		}

	}

	public void setDotListener(IAFPenDotListener mIAFPenDotListener){ this.iPenCtrl.setDotListener(mIAFPenDotListener);}
	public String getConnectDevice()
	{
		return this.iPenCtrl.getConnectedDevice();
	}

	public DPenCtrl getIPenCtrl()
	{
		return this.iPenCtrl;
	}

    public void setContext(Context context)
    {
        iPenCtrl.setContext( context );
    }
    public void requestOfflineDataInfo() {
        iPenCtrl.requestOfflineDataInfo();
    }
    public int btStartForPeripheralsList(){return iPenCtrl.btStartForPeripheralsList(context); }
    public void btStopSearchPeripheralsList(){iPenCtrl.btStopSearchPeripheralsList();}
    public void requestFWVer(){iPenCtrl.requestFWVer();}
    public void requestBatInfo(){iPenCtrl.requestBatInfo();}

//    @Override
//    public void offlineDataDidReceivePenData(List<AFDot> dots, JSONObject info) {
//        /* example 0~5348
//         E/afsdk: {"readCnt":3200,"readedCnt":3200,"totalCnt":5348}
//         E/afsdk: {"readCnt":2148,"readedCnt":5348,"totalCnt":5348}
//        * */
//        LogUtil.e(info.toString());
//        for (int i=0;i<dots.size();i++){
//            //handle dot
//            AFDot dot = dots.get(i);
////            LogUtil.e(String.format("X=%d Y=%d t=%d p=%d",dot.X,dot.Y,dot.type,dot.page));
//        }
//    }
}
