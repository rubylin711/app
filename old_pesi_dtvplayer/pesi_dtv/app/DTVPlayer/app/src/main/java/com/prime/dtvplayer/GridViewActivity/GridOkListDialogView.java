package com.prime.dtvplayer.GridViewActivity;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.prime.dtvplayer.Activity.DTVActivity;
import com.prime.dtvplayer.R;
import com.prime.dtvplayer.Sysdata.FavGroupName;
import com.prime.dtvplayer.Sysdata.ProgramInfo;
import com.prime.dtvplayer.Sysdata.SimpleChannel;
import com.prime.dtvplayer.Sysdata.TpInfo;

import java.util.List;

/**
 * Created by scoty on 2017/12/11.
 */

public abstract class GridOkListDialogView extends Dialog{
    private static final String TAG="OkListDialogView";
    private Dialog mDialog = null;
    private Context mContext = null;
    private GridView oklistview;
    private GridOkListAdapter okAdapter=null;
    private int curFavGroup,curChannelIndex;
    private DTVActivity mDTVActivity = null;
    private List<TpInfo> mtpInfo =null;
    private List<DTVActivity.OkListManagerImpl> mOkListManager = null;
    private boolean oklistGoTop,oklistGoBottom;
    private int listCount = 7;
    public GridOkListDialogView(Context context, final List<DTVActivity.OkListManagerImpl> mOkListManagerList
            , final List<FavGroupName> mAllProgramGroup ,final int curServiceType,int curListPosition ,DTVActivity mdtv,int keycode) {
        super(context);
        mContext = context;
        //mtpInfo = tpListInfo;
        curFavGroup = curServiceType;//cur service type
        curChannelIndex = curListPosition;
        mOkListManager = mOkListManagerList;
        mDTVActivity = mdtv;
        ProgramInfo program = mdtv.ProgramInfoGetByChnum(1,0);
        Log.d(TAG, "OkListDialogView: ==>> " + program.getDisplayNum());
        mDialog = new Dialog(mContext, R.style.MyDialog){
            @Override
            public boolean onKeyDown(int keyCode, KeyEvent event){//key event
                TextView text = (TextView) mDialog.getWindow().findViewById(R.id.oklisttpinfoTXV);
                TextView textNoFavChannel = (TextView) mDialog.getWindow().findViewById(R.id.oklistnofavchannelTXV);
                View okView = oklistview.getFocusedChild();
                //int curtv_postiiton = oklistview.getChildAdapterPosition(okView);
                int curtv_postiiton = oklistview.getSelectedItemPosition();
                int listViewHeight = ((int) mContext.getResources().getDimension(R.dimen.LIST_VIEW_HEIGHT));
                //int okCount = okAdapter.getItemCount();
                int okCount = okAdapter.getCount();
                int okOffset = okCount * listViewHeight;
                switch (keyCode) {
                    case KeyEvent.KEYCODE_DPAD_RIGHT:{
                        Log.d(TAG,"oklist KEYCODE_DPAD_RIGHT");

                        curFavGroup++;
                        if(curFavGroup >= ProgramInfo.ALL_TV_RADIO_TYPE_MAX)
                            curFavGroup = ProgramInfo.ALL_TV_TYPE;
                        changeGroupTitle(mAllProgramGroup,curFavGroup);
                        okAdapter = new GridOkListAdapter(mContext,mOkListManagerList.get(curFavGroup).ProgramInfoList);
                        oklistview.setAdapter(okAdapter);

                        if(okAdapter.getCount() <= 0) {
                            textNoFavChannel.setVisibility(View.VISIBLE);
                            text.setVisibility(View.INVISIBLE);
                        }
                        else
                        {
                            textNoFavChannel.setVisibility(View.INVISIBLE);
                            text.setVisibility(View.VISIBLE);
                        }
                        okAdapter.notifyDataSetChanged();
                        if(curServiceType == curFavGroup)
                            oklistview.setSelection(curChannelIndex);
                            //setOklistFocus(oklistview,curChannelIndex);
                    }break;
                    case KeyEvent.KEYCODE_DPAD_LEFT:{
                        Log.d(TAG,"oklist KEYCODE_DPAD_LEFT");
                        curFavGroup--;
                        if(curFavGroup < ProgramInfo.ALL_TV_TYPE)
                            curFavGroup = ProgramInfo.RADIO_FAV2_TYPE;
                        changeGroupTitle(mAllProgramGroup,curFavGroup);
                        okAdapter = new GridOkListAdapter(mContext,mOkListManagerList.get(curFavGroup).ProgramInfoList);
                        oklistview.setAdapter(okAdapter);
                        if(okAdapter.getCount() <= 0) {
                            textNoFavChannel.setVisibility(View.VISIBLE);
                            text.setVisibility(View.INVISIBLE);
                        }
                        else
                        {
                            textNoFavChannel.setVisibility(View.INVISIBLE);
                            text.setVisibility(View.VISIBLE);
                        }
                        okAdapter.notifyDataSetChanged();
                        if(curServiceType == curFavGroup)
                            oklistview.setSelection(curChannelIndex);
                            //setOklistFocus(oklistview,curChannelIndex);
                    }break;
                   /* case KeyEvent.KEYCODE_DPAD_DOWN: {
                        Log.d(TAG,"oklist KEYCODE_DPAD_DOWN");
                        if ((curtv_postiiton >= 0) && (curtv_postiiton == okCount - 1)) {
                            oklistview.smoothScrollBy(0, (-okOffset));
                            oklistGoTop = true;
                        }
                    }break;
                    case KeyEvent.KEYCODE_DPAD_UP: {
                        Log.d(TAG, "oklist KEYCODE_DPAD_UP");
                        if (curtv_postiiton == 0) {
                            oklistview.smoothScrollBy(0, okOffset);
                            oklistGoBottom = true;
                        }
                    }*/
                }
                return super.onKeyDown(keyCode, event);
            }

        };
//        mDialog.setCancelable(false);// disable click back button  // Johnny 20181210 for keyboard control
        mDialog.setCanceledOnTouchOutside(false);// disable click home button and other area

        if(mDialog == null){
            return;
        }

        mDialog.show();//show dialog
        mDialog.setContentView(R.layout.oklist_dialog);
        Window window = mDialog.getWindow();//get dialog widow size
        WindowManager.LayoutParams lp=window.getAttributes();//set dialog window size to lp

        //lp.
        lp.dimAmount=0.0f;
        window.setAttributes(lp);//set dialog parameter
        Log.d(TAG, "OkListDialogView: ===>> width == " + lp.width);
        int width =lp.width;
        window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

        LayoutInflater minflater = LayoutInflater.from(mContext);;
        View layout = minflater.inflate(R.layout.oklist_dialog, null);
        Log.d(TAG, "oklist_dialog_init: width " + layout.getWidth());



        changeGroupTitle(mAllProgramGroup,curFavGroup);
        oklist_dialog_init(mOkListManagerList,keycode,width);

    }
    public void setListViewHeight(GridView listView) {

        // 获取ListView对应的Adapter
        GridOkListAdapter listAdapter = (GridOkListAdapter) listView.getAdapter();
        if (listAdapter == null) {
            return;
        }
        int totalHeight = 0;
        for (int i = 0, len = 9; i < len; i++) { // listAdapter.getCount()返回数据项的数目
            View listItem = listAdapter.getView(i, null, listView);
            listItem.measure(0, 0); // 计算子项View 的宽高
            totalHeight += listItem.getMeasuredHeight();//+ listView.getDividerHeight(); // 统计所有子项的总高度
            Log.d(TAG, "setListViewHeight: ==>>> i == " + i + " totalHeight == " + totalHeight);
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight;

        Log.d(TAG, "setListViewHeight: ==>>> " + params.height + " ");
        listView.setLayoutParams(params);
    }
    public void oklist_dialog_init(List<DTVActivity.OkListManagerImpl> mOkListManagerList, int keycode, int width)
    {
        TextView text = (TextView) mDialog.getWindow().findViewById(R.id.oklisttpinfoTXV);
        TextView textNoFavChannel = (TextView) mDialog.getWindow().findViewById(R.id.oklistnofavchannelTXV);

        oklistview = (GridView)mDialog.getWindow().findViewById(R.id.oklistLIV);
        okAdapter = new GridOkListAdapter(mContext,mOkListManagerList.get(curFavGroup).ProgramInfoList);
        oklistview.setNumColumns(1);

        /*int itemHeight = ((int) mContext.getResources().getDimension(R.dimen.LIST_VIEW_HEIGHT));
        ConstraintLayout.LayoutParams layoutParams = new ConstraintLayout.LayoutParams(
                width, itemHeight * listCount);*/


        //oklistview.setTranslationX(oklistview.getX());
        //oklistview.setTranslationY(oklistview.getY());
        //oklistview.setLayoutParams(layoutParams);

        oklistview.setAdapter(okAdapter);
        setListViewHeight(oklistview);
        oklistview.setOnItemClickListener(new oklistOnItemClick());
        /*oklistview.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                Log.d(TAG, "onScrolled: ");
            }
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                Log.d(TAG, "onScrollStateChanged: ");

                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    if (oklistGoTop) {
                        oklistGoTop = false;
                        recyclerView
                                .getChildAt(0)
                                .requestFocus();
                    }
                    else if (oklistGoBottom) {
                        oklistGoBottom = false;
                        recyclerView
                                .getChildAt(recyclerView.getChildCount()-1)
                                .requestFocus();
                    }
                }
            }
        });
        oklistGoTop = false;
        oklistGoBottom = false;*/

        if (keycode == KeyEvent.KEYCODE_DPAD_CENTER)//ok key
        {
            Log.d(TAG, "oklist_dialog_init: curChannelIndex == " + curChannelIndex);
            //setOklistFocus(oklistview,curChannelIndex);
            oklistview.setSelection(curChannelIndex);
        }
        else//fav key
        {
            if(okAdapter.getCount() <= 0) {
                textNoFavChannel.setVisibility(View.VISIBLE);
                text.setVisibility(View.INVISIBLE);
            }
            else {
                textNoFavChannel.setVisibility(View.INVISIBLE);
                text.setVisibility(View.VISIBLE);
                oklistview.setSelection(0);
            }
        }

        okAdapter.notifyDataSetChanged();

        TextView texthelp = (TextView) mDialog.getWindow().findViewById(R.id.oklisthelpTXV);
        SpannableStringBuilder style = new SpannableStringBuilder(mContext.getString(R.string.STR_OKLIST_HELP_OK_SELECT));
        style.setSpan(new ForegroundColorSpan(Color.YELLOW), 0, 2, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        style.setSpan(new ForegroundColorSpan(Color.YELLOW), 10, 12, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        texthelp.setText(style);

    }
/*
    public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder> {
        private Context cont;
        private List<ProgramInfo> listItems;

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView chnum;
            TextView chname;
            ImageView scramble;
            ImageView fav;
            ImageView chlock;

            ViewHolder(View itemView) {
                super(itemView);
                chnum = (TextView) itemView.findViewById(R.id.chnumTXV);//LCN
                chname = (TextView) itemView.findViewById(R.id.chnameTXV);//Channel name
                scramble = (ImageView) itemView.findViewById(R.id.scrambleIGV);//Scramble icon
                fav = (ImageView) itemView.findViewById(R.id.favIGV);//group icon
                chlock = (ImageView) itemView.findViewById(R.id.lockIGV);//Lock icon

            }
        }

        RecyclerAdapter(Context mContext,List<ProgramInfo> list) {
            cont = mContext;
            listItems = list;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View convertView = LayoutInflater
                    .from( parent.getContext() )
                    .inflate(R.layout.oklist_list_item, parent, false);

            convertView.setOnClickListener(new oklistOnClick());
            convertView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (hasFocus) {
//                            Log.d(TAG, "onFocusChange: channel = "+ ((TextView) v.findViewById(R.id.chnameTXV)).getText());
                        v.setBackgroundResource(R.drawable.focus_list);
                    }
                }
            });

            return new ViewHolder(convertView);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            TextView text = (TextView) mDialog.getWindow().findViewById(R.id.oklisttpinfoTXV);
            holder.chnum.setText(String.valueOf(listItems.get(position).getLCN()));
            holder.chname.setText(((ProgramInfo) listItems.get(position)).getChName());

            if(((ProgramInfo)listItems.get(position)).getCA() == 1)
                holder.scramble.setBackgroundResource(R.drawable.scramble);
            else
                holder.scramble.setBackgroundResource(android.R.color.transparent);

            if(((ProgramInfo)listItems.get(position)).getLock() == 1)
                holder.chlock.setBackgroundResource(R.drawable.lock);
            else
                holder.chlock.setBackgroundResource(android.R.color.transparent);

            for(int i = 0 ; i < mtpInfo.size() ; i++)
            {
                if(mtpInfo.get(i).getTpId() == listItems.get(position).getTpId())
                {
                    String[] qamString = mContext.getResources().getStringArray(R.array.STR_ARRAY_QAMLIST);
                    String string = mContext.getString(R.string.STR_FREQ)+" : "+mtpInfo.get(i).CableTp.getFreq()
                            +mContext.getString(R.string.STR_KHZ)+"    "
                            +mContext.getString(R.string.STR_SYM)+" : "+mtpInfo.get(i).CableTp.getSymbol()
                            +mContext.getString(R.string.STR_KSPS)+"\n"
                            +mContext.getString(R.string.STR_QAM)+" : "+qamString[mtpInfo.get(i).CableTp.getQam()];
                    text.setText(string);
                    break;
                }
            }

            holder.itemView.getLayoutParams().height = ((int) mContext.getResources().getDimension(R.dimen.LIST_VIEW_HEIGHT));
        }

        public long getItemId(int position) {
            return position;
        }

        @Override
        public int getItemCount() {
            if (listItems == null) {
                return 0;
            }
            else
            {
                return listItems.size();//list size
            }
        }
    }
*/

private class GridOkListAdapter extends BaseAdapter {
    private LayoutInflater mInflater;
    private Context cont;
    private List<SimpleChannel> listItems;
    private int selectItem;

    public GridOkListAdapter(Context context, List<SimpleChannel> list) {
        super();
        cont = context;
        listItems = list;
        mInflater = LayoutInflater.from(context);
    }
    class ViewHolder {
        TextView chnum;//channel num
        TextView chname;//channel name
        ImageView chlock;//channel lock
    }

    public int getCount() {
        if (listItems == null) {
            return 0;
        } else
        {
            return listItems.size();//list size
        }
    }
    public Object getItem(int position) {
        return position;
    }

    public long getItemId(int position) {
        return position;
    }

    public void setSelectItem(int position){
        this.selectItem = position;
    }

    public int getSelectItem(){
        return this.selectItem;
    }

    public View getView(int position, View convertView, ViewGroup parent){
        TextView text = (TextView) mDialog.getWindow().findViewById(R.id.oklisttpinfoTXV);
        ViewHolder holder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.oklist_list_item, null);

            holder = new ViewHolder();
            holder.chnum = (TextView) convertView.findViewById(R.id.chnumTXV);
            holder.chname = (TextView) convertView.findViewById(R.id.chnameTXV);
            holder.chlock = (ImageView) convertView.findViewById(R.id.lockIGV);
            convertView.setTag(holder);
        } else {
            // Get the ViewHolder back to get fast access to the TextView
            // and the ImageView.
            holder = (ViewHolder) convertView.getTag();
        }
        //Log.d(TAG,"getView AAAAAAAA position " + position + "size ==>> " + mtpInfo.size() + " lsit size ==>>" + listItems.size());
        holder.chnum.setText(Integer.toString(((SimpleChannel) listItems.get(position)).getChannelNum()));
        holder.chname.setText(((SimpleChannel) listItems.get(position)).getChannelName());
        if(((SimpleChannel)listItems.get(position)).getUserLock() == 1)
            holder.chlock.setBackgroundResource(R.drawable.lock);
        else
            holder.chlock.setBackgroundResource(android.R.color.transparent);

        ProgramInfo programInfo =
                mDTVActivity.ProgramInfoGetByChannelId(
                        mOkListManager.get(curFavGroup).ProgramInfoList.get(position).getChannelId());
        TpInfo mtpInfo = mDTVActivity.TpInfoGet(programInfo.getTpId());
        String[] qamString = cont.getResources().getStringArray(R.array.STR_ARRAY_QAMLIST);

        //holder.itemView.setBackground(cont.getDrawable(R.drawable.focus));
        //Log.d(TAG, "onFocusChange: tpinfo update position = " + position);

        String string = mContext.getString(R.string.STR_FREQ)+" : "+mtpInfo.CableTp.getFreq()
                +mContext.getString(R.string.STR_KHZ)+"    "
                +mContext.getString(R.string.STR_SYM)+" : "+mtpInfo.CableTp.getSymbol()
                +mContext.getString(R.string.STR_KSPS)+"\n"
                +mContext.getString(R.string.STR_QAM)+" : "+qamString[mtpInfo.CableTp.getQam()];
        text.setText(string);
        //holder.chnum.setTextColor(Color.BLACK);
        //holder.chname.setTextColor(Color.BLACK);
        text.setText(string);

        //mContext.mdtv.
//        for(int i = 0 ; i < mtpInfo.size() ; i++)
//        {
//            if(mtpInfo.get(i).getTpId() == listItems.get(position).getTpId())
//            {
//                String[] qamString = mContext.getResources().getStringArray(R.array.STR_ARRAY_QAMLIST);
//                String string = mContext.getString(R.string.STR_FREQ)+" : "+mtpInfo.get(i).CableTp.getFreq()
//                        +mContext.getString(R.string.STR_KHZ)+"    "
//                        +mContext.getString(R.string.STR_SYM)+" : "+mtpInfo.get(i).CableTp.getSymbol()
//                        +mContext.getString(R.string.STR_KSPS)+"\n"
//                        +mContext.getString(R.string.STR_QAM)+" : "+qamString[mtpInfo.get(i).CableTp.getQam()];
//                text.setText(string);
//                break;
//            }
//        }
        return convertView;
    }
}
    /*class oklistOnClick implements View.OnClickListener
    {
        @Override
        public void onClick(View v) {
            final int position = oklistview.getChildLayoutPosition(v);
            Log.d(TAG,"oklistOnClick curChannelIndex" + position);

            curChannelIndex = position;
            onSetPositiveButton(curFavGroup, position);
            dismissDialog();
        }
    }*/

    /*public void setOklistFocus(final GridView oklist, final int position)
    {
        if(position >= listCount)
            oklist.getLayoutManager().scrollToPosition(position);
        oklist.postDelayed(new Runnable() {
            @Override
            public void run() {
                View view = oklist.getLayoutManager().findViewByPosition(position);
                if (view != null) {
                    view.requestFocus();
                }
            }
        }, 0);
    }*/

    public void changeGroupTitle(List<FavGroupName> mAllProgramGroup,int mFavGroup)
    {
        TextView okTitle = (TextView) mDialog.getWindow().findViewById(R.id.oklisttitleTXV);
        okTitle.setText(mAllProgramGroup.get(mFavGroup).getGroupName());
    }

    class oklistOnItemClick implements AdapterView.OnItemClickListener
    {
        public void onItemClick(AdapterView<?> parent, View view, int position,long id)
        {
            Log.d(TAG,"oklistOnItemClick set channel " + position + "curFavGroup ==>> " + curFavGroup);
            curChannelIndex = position;
            onSetPositiveButton(curFavGroup, position);
            dismissDialog();
        }
    }


    public void dismissDialog(){
        Log.d(TAG, "dismissDialog");
        if(mDialog!=null&& mDialog.isShowing()){
            mDialog.dismiss();//close dialog
        }
    }

    public boolean isShowing(){
        Log.d(TAG, "isShowing");
        if(mDialog!=null&&mDialog.isShowing()){
            return mDialog.isShowing();//check dialog is exist
        }
        return false;
    }
    abstract public void onSetPositiveButton(int curFavGroup, int position);
}
