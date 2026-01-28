package com.prime.launcher.Mail;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.prime.launcher.EPG.MiddleFocusRecyclerView;
import com.prime.launcher.Utils.Utils;
import com.prime.launcher.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** @noinspection CommentedOutCode*/
public class MailInboxView extends RelativeLayout {
    private static final String TAG = "MailInboxView";

    private MailManager g_mailManager;
    private int g_current_mail_id;
    private int g_current_mail_position;

    //UI
    private TextView g_textv_detail_no_data;
    private TextView g_textv_list_no_data;
    private View g_view_last_item;
    private List<Mail> g_mail_data_list;
    private MailDetailView g_view_mail_detail;
    private List<Integer> g_mail_id_list;
    private MiddleFocusRecyclerView g_rcv_mail_list;
    private MailListAdapter g_adpt_mail_list;

    public MailInboxView(Context context) {
        super(context);
        init();
    }

    public MailInboxView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MailInboxView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        g_mail_id_list = new ArrayList<>();
        g_mailManager = MailManager.GetInstance((AppCompatActivity) getContext());
        inflate(getContext(), R.layout.view_mail_inbox, this);
        g_rcv_mail_list = findViewById(R.id.lo_mail_inbox_rcv_list);
        g_view_mail_detail = findViewById(R.id.lo_mail_inbox_view_mail_detail);
        g_textv_list_no_data = findViewById(R.id.lo_mail_inbox_textv_list_hint);
        g_textv_detail_no_data = findViewById(R.id.lo_mail_inbox_textv_detail_hint);
        g_current_mail_id = -1;

        init_mail_data();
    }

    private void init_mail_data() {
        g_rcv_mail_list.setHasFixedSize(true);
        g_rcv_mail_list.setLayoutManager(new LinearLayoutManager(getContext()));
        g_rcv_mail_list.addItemDecoration(new MailListAdapter.MyItemDecoration(getContext()));

        g_adpt_mail_list = new MailListAdapter(getContext(), get_mail_data_list_from_db(), new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    g_view_last_item = v;
                    set_last_item_view(v);
                    g_current_mail_position = Utils.get_tag_position((String) v.getTag());
                    g_current_mail_id = Utils.get_tag_id((String) v.getTag());
                    if (Utils.get_sec_tag_id((String) v.getTag()) == 0 && !g_mail_id_list.contains(g_current_mail_id)) {
                        g_mail_id_list.add(g_current_mail_id);
                    }

                    if (g_current_mail_position > g_mail_data_list.size())
                        return;

                    if (!g_mail_data_list.isEmpty())
                        show_detail_mail(g_mail_data_list.get(g_current_mail_position));
                }
            }
        });
        g_rcv_mail_list.setAdapter(g_adpt_mail_list);
        update_focus();
    }

    private List<Mail> get_mail_data_list_from_db() {
        Log.d(TAG, "get_mail_data_list");
        g_mail_data_list = g_mailManager.get_mail_list_from_db();
        Collections.reverse(g_mail_data_list);

        /*g_mail_data_list = new ArrayList<>();
        MailInfo mailInfo = null;
        for (int i = 0; i < 10 ; i++) {
            mailInfo = new MailInfo();
            mailInfo.set_title("Number i = " + i);
            mailInfo.set_mail_id(i);
            mailInfo.set_read_status(0);
            g_mail_data_list.add(mailInfo);
        }*/
        return g_mail_data_list;
    }

    public void set_last_item_view(View view) {
        g_view_mail_detail.set_last_item_view(view);
    }

    public void show_detail_mail(Mail mail) {
        g_view_mail_detail.set_content(mail);
    }

    public void update_focus() {
        focus_first_child(g_rcv_mail_list);
        check_have_content();
    }

    private void focus_first_child(final MiddleFocusRecyclerView view) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (view.isComputingLayout() || view.getChildAt(0) == null) {
                    handler.postDelayed(this, 50L);
                } else {
                    view.getChildAt(0).requestFocus();
                }
            }
        }, 50L);
    }

    public void focus_last_item_view() {
        if (g_view_last_item != null && !g_mail_data_list.isEmpty()) {
            g_view_last_item.requestFocus();
        }
    }

    private void check_have_content() {
        if (g_mail_data_list == null) {
            return;
        }
        if (g_mail_data_list.isEmpty()) {
            g_textv_list_no_data.setVisibility(View.VISIBLE);
            g_textv_list_no_data.requestFocus();
            g_textv_detail_no_data.setVisibility(View.VISIBLE);
            g_view_mail_detail.update_title_layer_background(false);
            return;
        }
        g_textv_list_no_data.setVisibility(View.GONE);
        g_textv_detail_no_data.setVisibility(View.GONE);
        g_view_mail_detail.update_title_layer_background(true);
        if (g_current_mail_position <= g_mail_data_list.size() - 1) {
            g_rcv_mail_list.move_to_position(g_current_mail_position);
            return;
        }
        g_rcv_mail_list.move_to_position(g_mail_data_list.size() - 1);
    }

    public void delete_mail() {
        Log.d(TAG, "delete_mail");
        g_mail_data_list.remove(g_current_mail_position);
        g_adpt_mail_list.update_data(g_mail_data_list);
        check_have_content();
        g_mailManager.delete_mail_of_db(g_current_mail_id);
    }

    public void update_data() {
        update_mails_read_status(g_mail_data_list);
        get_mail_data_list_from_db();
        check_have_content();
        g_adpt_mail_list.update_data(g_mail_data_list);
    }

    public void update_mails_read_status(List<Mail> mailList) {
        if (!g_mail_id_list.isEmpty()) {
            for (int id: g_mail_id_list) {
                Mail mail = get_mail_by_id(id, mailList);

                if( mail != null) {
                    mail.setRead_status(Mail.READ);
                    g_mailManager.set_mail_read_status_from_mail_activity(getContext(), mail);
                }
            }
        }
    }

    public List<Mail> get_mail_data_list() {
        return g_mail_data_list;
    }

    private Mail get_mail_by_id(int id, List<Mail> mailList) {
        for (Mail mail: mailList) {
            if (mail.getId() == id)
                return mail;
        }
        return null;
    }
}
