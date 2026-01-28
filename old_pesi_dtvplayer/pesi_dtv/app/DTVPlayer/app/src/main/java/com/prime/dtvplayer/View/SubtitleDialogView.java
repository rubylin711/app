package com.prime.dtvplayer.View;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import com.prime.dtvplayer.R;
import com.prime.dtvplayer.Sysdata.SubtitleInfo;

import static android.view.KeyEvent.*;

/**
 Created by Edwin on 2017/12/6.
 */

public class SubtitleDialogView extends Dialog {
    private final String TAG = getClass().getSimpleName();
    private Context mContext;
    private SubtitleInfo mSubtitleInfo;
    private OnSubtitleClickedListener onSubtitleClickedListener;
    private RecyclerView subListView;
    private SubAdapter subAdapter;
    private int childPos = 0;
    public interface OnSubtitleClickedListener {
        void SubtitleClicked();
    }
    public SubtitleDialogView (
            Context context,
            final SubtitleInfo Subtitle,
            OnSubtitleClickedListener onSubtitleClickedListener)
    {

        super(context);
        Log.d(TAG, "SubtitleDialogView: ");
        this.onSubtitleClickedListener = onSubtitleClickedListener;
        mContext = context;
        mSubtitleInfo = Subtitle;
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.subtitle_dialog_view);

        WindowManager.LayoutParams wlp;
        if (this.getWindow() != null) {
            wlp = this.getWindow().getAttributes();
            wlp.gravity = Gravity.TOP | Gravity.START;
            wlp.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
            wlp.verticalMargin = 0.05f;
            wlp.horizontalMargin = 0.05f;
            this.getWindow().setBackgroundDrawableResource(android.R.color.transparent);    // Johnny 20180801 add for layout alpha
        }

        childPos = Subtitle.getCurPos();
        subListView = (RecyclerView)  findViewById(R.id.subtitleDialogListview);
        subAdapter = new SubAdapter(mSubtitleInfo);
        subListView.setAdapter(subAdapter);
        subAdapter.notifyDataSetChanged();


    }

    @Override
    public boolean onKeyDown(int keyCode, @NonNull KeyEvent event) {
        Log.d(TAG, "onKeyDown: ");
        View subtitleView = subListView.getFocusedChild();
        int curtv_position = subListView.getChildAdapterPosition(subtitleView);
        int listViewHeight = ((int) mContext.getResources().getDimension(R.dimen.LIST_VIEW_HEIGHT));
        int okCount = subAdapter.getItemCount();
        int okOffset = okCount * listViewHeight;
        int childCount = subListView.getChildCount();



        if (keyCode == KEYCODE_DPAD_CENTER) {
            // set curPos
        }
        if (keyCode == KEYCODE_DPAD_DOWN) {
            if ((curtv_position >= 0) && (curtv_position == okCount - 1)) {
                Log.d(TAG, "onKey: focus top item");
                childPos = 0;
                subListView.scrollToPosition(0);
                subListView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        View view = subListView.getLayoutManager().findViewByPosition(childPos);
                        if (view != null) {
                            view.requestFocus();
                        }
                    }
                }, 0);
                return true;
            }
            else {
                if (childPos == (childCount - 1)) {
                    Log.d(TAG, "onKey: scroll down");
                    subListView.scrollBy(0, listViewHeight);
                    subListView.getChildAt(childPos).requestFocus();
                    childPos = childCount - 1;
                    return true;
                }
                else if (childPos < (childCount - 1)) {
                    Log.d(TAG, "onKey: move down");
                    childPos = childPos + 1;
                }
            }
            Log.d(TAG, "onKeyDown: KEYCODE_DPAD_DOWN--childPos=" + childPos);
        }
        if (keyCode == KEYCODE_DPAD_UP) {
            if (curtv_position == 0) {
                Log.d(TAG, "onKey: focus bottom item");
                final int position = okCount-1;
                childPos = childCount - 1;
                subListView.scrollToPosition(position);
                subListView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        View view = subListView.getLayoutManager().findViewByPosition(position);
                        if (view != null) {
                            view.requestFocus();
                        }
                    }
                }, 0);
                return true;
            }
            else {
                if (childPos == 0) {
                    Log.d(TAG, "onKey: scroll up");
                    subListView.scrollBy(0, -listViewHeight);
                    childPos = 0;
                    subListView.getChildAt(childPos).requestFocus();
                    return true;
                }
                else if (childPos > 0) {
                    Log.d(TAG, "onKey: move up");
                    childPos--;
                }
            }
            Log.d(TAG, "onKeyDown: KEYCODE_DPAD_UP--childPos="+childPos);
        }
        return super.onKeyDown(keyCode, event);
    }

    class SubAdapter extends RecyclerView.Adapter<SubAdapter.ViewHolder> {
        class ViewHolder extends RecyclerView.ViewHolder {
            TextView subTxt;
            ImageView subImg;
            ViewHolder(View itemView) {
                super(itemView);
                subTxt = (TextView) itemView.findViewById(R.id.subtitleTxv);
                subImg = (ImageView) itemView.findViewById(R.id.subtitleIgv);
            }
        }
        SubtitleInfo subtitleInfo;
        SubAdapter(SubtitleInfo Subtitle) {            
            this.subtitleInfo = Subtitle;
        }
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater
                    .from(parent.getContext())
                    .inflate(R.layout.subtitle_dialog_item, parent, false);
            return new ViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            String subText = subtitleInfo.getComponent(position).getLangCode();
            holder.subTxt.setTextSize(
                    TypedValue.COMPLEX_UNIT_PX,
                    mContext.getResources().getDimension(R.dimen.TEXT_SIZE));
            holder.subTxt.setText(subText);
            holder.subTxt.setTextColor(Color.WHITE);
            holder.subImg.setImageResource(R.drawable.txtsubt);
            if (subtitleInfo.getComponent(position).getType() == 0)   // dvbsub
            {
                holder.subImg.setVisibility(View.INVISIBLE);
            }

            holder.itemView.setFocusableInTouchMode(true);  // Johnny 20181219 for mouse control
            holder.itemView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (hasFocus) {
                        holder.subTxt.setTextColor(Color.BLACK);
                        holder.subImg.setImageResource(R.drawable.txtsubtfocus);
                    }
                    else {
                        holder.subTxt.setTextColor(Color.WHITE);
                        holder.subImg.setImageResource(R.drawable.txtsubt);
                    }
                }
            });

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Johnny 20181219 for mouse control -s
                    int adapterPos = subListView.getChildAdapterPosition(v);    // find clicked view pos in recycleView adapter
                    subtitleInfo.setCurPos(adapterPos);
//                    subtitleInfo.setCurPos(childPos); // recycleView child pos != the pos in subtitleInfo
                    // Johnny 20181219 for mouse control -e

                    onSubtitleClickedListener.SubtitleClicked();
                    dismiss();
                }
            });

            if (position == childPos)
            {
                holder.itemView.requestFocus();
            }
        }

        @Override
        public int getItemCount() {
            return subtitleInfo.getComponentCount();
        }
    }
}
