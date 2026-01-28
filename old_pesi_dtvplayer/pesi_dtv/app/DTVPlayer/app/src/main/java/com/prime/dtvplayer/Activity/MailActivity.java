package com.prime.dtvplayer.Activity;

import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.Guideline;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.prime.dtvplayer.Database.DatabaseHandler;
import com.prime.dtvplayer.R;
import com.prime.dtvplayer.Sysdata.ExtKeyboardDefine;
import com.prime.dtvplayer.Sysdata.MailInfo;
import com.prime.dtvplayer.View.ActivityHelpView;
import com.prime.dtvplayer.View.ActivityTitleView;
import com.prime.dtvplayer.View.MailInfoDialog;
import com.prime.dtvplayer.View.MessageDialogView;

import java.util.ArrayList;
import java.util.List;

import static android.view.KeyEvent.KEYCODE_DPAD_CENTER;
import static android.view.KeyEvent.KEYCODE_PROG_GREEN;
import static android.view.KeyEvent.KEYCODE_PROG_RED;

public class MailActivity extends DTVActivity {
    private static final String TAG = "MailActivity";

    private RecyclerView mailRecycler;
    private MailAdapter mailAdapter;
    private TextView noMailMsg;
    private LinearLayoutManager mailLayoutManager = null;
    private static int visibleCount;
    private String TableName = "" ;

    private static int ID_COL_INDEX = 1;
    private static int MAIL_COL_INDEX = 2;
    private static int READ_COL_INDEX = 3;

    DatabaseHandler MailHandler ;
    public List<MailInfo> DataBaseList = new ArrayList<>();
    public List<MailInfo> mailList = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mail);

        ActivityTitleView setActivityTitle;
        setActivityTitle = (ActivityTitleView) findViewById(R.id.TitleLayout);
        setActivityTitle.setTitleView(getString(R.string.STR_MAIL_TITLE));

        ActivityHelpView MailHelpView;
        MailHelpView = (ActivityHelpView) findViewById(R.id.HelpViewLayout);
        MailHelpView.resetHelp(1,R.drawable.help_red,getString(R.string.STR_DELETE));
        MailHelpView.resetHelp(2,R.drawable.help_green,getString(R.string.STR_DELETE_ALL));
        MailHelpView.resetHelp(3,0,null);
        MailHelpView.resetHelp(4,0,null);

        // Johnny 20181228 for mouse control -s
        MailHelpView.setHelpIconClickListener(1, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onProgRedClicked();
            }
        });
        MailHelpView.setHelpIconClickListener(2, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onProgGreenClicked();
            }
        });
        // Johnny 20181228 for mouse control -e

        MailHelpView.setHelpInfoTextBySplit(null);

        noMailMsg = (TextView) findViewById(R.id.noMailmsgTXV);

        ConstraintLayout MailTitle = (ConstraintLayout) findViewById(R.id.mailListTitle);
        MailTitle.setFocusable(false);

        MailListInit();
        InitRecyclerView();

        if(mailList.size() == 0) {
            noMailMsg.setVisibility(View.VISIBLE);
            noMailMsg.requestFocus();
        }
    }

    private void MailListInit()
    {
        MailHandler = GetMailHandler();
        TableName = getMailTableName();
        int ret = MailHandler.checkDatabase();
        Log.d(TAG, "MailListInit:  ret = " + ret);
        if(ret == -1)
        {
            new MessageDialogView(this, getString(R.string.STR_CONNECT_DATABASE_FAIL), 3000) {
                public void dialogEnd() {
                    finish();
                }
            }.show();
        }
        else
        {
            DataBaseList = GetMailList();
            if(DataBaseList.size() > 0)
            {
                for( int i = DataBaseList.size()-1; i >=0; i-- )
                    mailList.add(DataBaseList.get(i));
            }

            Log.d(TAG, "MailInit: mailList.size =" + mailList.size());
            //for(int i = 0; i < mailList.size(); i++)
            //    Log.d(TAG, "MailInit: i =" + i + "    ID = " + mailList.get(i).getMailID() + "   msg = " + mailList.get(i).getMailMsg() + "     read = " + mailList.get(i).getMailRead());
        }
    }

    private void InitRecyclerView()
    {
        mailRecycler = (RecyclerView) this.findViewById(R.id.mailLIV);
        mailLayoutManager = new LinearLayoutManager(this);
        mailRecycler.setLayoutManager(mailLayoutManager);

        mailRecycler.setItemAnimator(null);

        //for largest display size,--start
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int height = size.y;

        int itemHeight =  ((int) getResources().getDimension(R.dimen.LIST_VIEW_HEIGHT));
        Guideline top = (Guideline) findViewById(R.id.mailTop_guideline);
        Guideline bottom = (Guideline) findViewById(R.id.mailBot_guideline);
        float topPercent = ((ConstraintLayout.LayoutParams)top.getLayoutParams()).guidePercent;
        float bottomPercent = ((ConstraintLayout.LayoutParams)bottom.getLayoutParams()).guidePercent;
        float guideLineRange = bottomPercent - topPercent;
        visibleCount = (int)(height*guideLineRange)/itemHeight;
        if(visibleCount > 10)
            visibleCount = 10;

        // set height
        int visibleHeight = (int) getResources().getDimension(R.dimen.LIST_VIEW_HEIGHT) * visibleCount;
        ViewGroup.LayoutParams layoutParams = mailRecycler.getLayoutParams();
        layoutParams.height = visibleHeight;
        mailRecycler.setLayoutParams(layoutParams);

        mailAdapter = new MailActivity.MailAdapter(mailList);
        mailRecycler.setAdapter(mailAdapter);
    }

    private class MailAdapter extends RecyclerView.Adapter<MailAdapter.ViewHolder> {

        List<MailInfo> listItem;
        MailAdapter(List<MailInfo> mailInfoList) {
            listItem = mailInfoList;
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView no;
            TextView mailTitle;
            TextView mailRead;
            ViewHolder(View itemView){
                super(itemView);
                no = (TextView) itemView.findViewById(R.id.mailNoTXV);
                mailTitle = (TextView) itemView.findViewById(R.id.mailTXV);
                mailRead = (TextView) itemView.findViewById(R.id.mailReadTXV);
            }
        }

        @Override
        public MailAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View convertView = LayoutInflater
                    .from(parent.getContext())
                    .inflate(R.layout.mail_list_item, parent, false);
            return new ViewHolder(convertView);
        }

        @Override
        public void onBindViewHolder(final MailAdapter.ViewHolder holder, int position) {
            if (listItem == null)
                return;
            holder.no.setText(String.valueOf(position+1));
            holder.mailTitle.setText(listItem.get(position).getMailMsg());
            if(listItem.get(position).getMailRead() == MailInfo.MAILUNREAD)
                holder.mailRead.setText(getString(R.string.STR_UNREAD));
            else
                holder.mailRead.setText(getString(R.string.STR_READ));


            holder.itemView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View itemView, boolean hasFocus) {
                    if (hasFocus) {
                        holder.no.setSelected(true);
                        holder.mailTitle.setSelected(true);
                        holder.mailRead.setSelected(true);
                    }
                    else {
                        holder.no.setSelected(false);
                        holder.mailTitle.setSelected(false);
                        holder.mailRead.setSelected(false);
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return listItem.size();
        }
    }

    public boolean onKeyDown(int keyCode, final KeyEvent event)
    {
        switch (keyCode) {
            case KEYCODE_DPAD_CENTER: {
                int curPos = mailRecycler.getChildAdapterPosition(mailRecycler.getFocusedChild());
                MailInfo info = null;
                Log.d(TAG, "onKeyDown:  curPos = " + curPos + "    mailList.size =" + mailList.size());

                if(curPos >= mailList.size())
                    break;
                
                info = mailList.get(curPos);
                if(info == null)
                    Log.d(TAG, "onKeyDown:  Mail is NULL !!!!!!!!!!!");
                else {
                    // Edwin 20190509 fix dialog not focus -s
                    final MailInfoDialog dialog = new MailInfoDialog(this, info);
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run () {
                            dialog.mDialog.show();
                        }
                    }, 150);
                    // Edwin 20190509 fix dialog not focus -e
                    int originPos = 0;
                    if (info.getMailRead() == MailInfo.MAILUNREAD) {
                        originPos = getDataBasePosition(curPos);
                        MailHandler.writeTo(TableName, originPos+1, READ_COL_INDEX, MailInfo.MAILREAD);
                        mailList.get(curPos).setMailRead(MailInfo.MAILREAD);
                        mailAdapter.notifyItemChanged(curPos);
                    }
                }
            }break;

            case KEYCODE_PROG_RED:
            case ExtKeyboardDefine.KEYCODE_PROG_RED: // Johnny 20181210 for keyboard control
            {
                onProgRedClicked();     // Johnny 20181228 for mouse control
            }break;
            case KEYCODE_PROG_GREEN:
            case ExtKeyboardDefine.KEYCODE_PROG_GREEN: // Johnny 20181210 for keyboard control
            {
                onProgGreenClicked();   // Johnny 20181228 for mouse control
            }break;

        }
        return super.onKeyDown(keyCode, event);
    }

    private int getDataBasePosition( int curPosition)
    {
        return (curPosition - mailList.size() +1) * -1;
    }

    // Johnny 20181228 for mouse control -s
    private void onProgRedClicked() {
        if(mailList.size() <= 0)
            return;

        int curPos = mailRecycler.getChildAdapterPosition(mailRecycler.getFocusedChild());
        int originPos = getDataBasePosition(curPos);
        Log.d(TAG, "onKeyDown:  curPos =" + curPos + "         originPos = " + originPos);
        MailHandler.deleteFrom(TableName, originPos+1);

        mailList.remove(curPos);
        mailAdapter.notifyItemRemoved(curPos);
        if(curPos == mailList.size()-1)
            curPos--;
        mailAdapter.notifyItemRangeChanged(curPos, mailAdapter.getItemCount()-curPos);

        if(mailList.size() == 0) {
            noMailMsg.setVisibility(View.VISIBLE);
            noMailMsg.requestFocus();
        }
    }

    private void onProgGreenClicked() {
        if(mailList.size() <= 0)
            return;

        for( int i = mailList.size() ; i > 0 ; i--)
            MailHandler.deleteFrom(TableName, i);

        mailList.clear();
        mailAdapter.notifyDataSetChanged();

        if(mailList.size() == 0) {
            noMailMsg.setVisibility(View.VISIBLE);
            noMailMsg.requestFocus();
        }
    }
    // Johnny 20181228 for mouse control -e
}
