package com.prime.mediasignencryption;

import static com.prime.mediasignencryption.MainActivity.queryReturnOk;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;

/**
 * @ClassName FileSelector
 * @Description TODO
 * @Date 2013-7-16
 * @Email
 * @Author
 * @Version V1.0
 */
public class FileSelector extends Activity {
    private static final String TAG = FileSelector.class.getSimpleName();
    public static final String FILE = "file";
    public static final String DIR = "dir";
    private static final int MSG_HIDE_SHOW_DIALOG = 1;
    private static final int MSG_SHOW_WAIT_DIALOG = 2;
    private static final int MSG_NOTIFY_DATACHANGE = 3;
    private static final int WAITDIALOG_DISPALY_TIME = 500;
    private Context mContext = null;
    private PrefUtils mPrefUtil;
    private File mCurrentDirectory;
    private LayoutInflater mInflater;
    private FileAdapter mAdapter = new FileAdapter();
    private ListView mListView;
    private ProgressDialog mPdWatingScan = null;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_SHOW_WAIT_DIALOG:
                    mPdWatingScan = ProgressDialog.show(FileSelector.this,
                            getResources().getString(R.string.scan_title),
                            getResources().getString(R.string.scan_tip));
                    break;
                case MSG_HIDE_SHOW_DIALOG:
                    removeMessages(MSG_SHOW_WAIT_DIALOG);
                    if (mPdWatingScan != null) {
                        mPdWatingScan.dismiss();
                        mPdWatingScan = null;
                    }
                    break;
                case MSG_NOTIFY_DATACHANGE:
                    mAdapter.notifyDataSetChanged();
                    break;
                default:
                    break;
            }
        }
    };

    private void startScanThread() {
        Message nmsg = mHandler.obtainMessage(MSG_SHOW_WAIT_DIALOG);
        mHandler.sendMessageDelayed(nmsg, WAITDIALOG_DISPALY_TIME);
        new Thread() {
            public void run() {
                ArrayList<File> files = mPrefUtil.getStorageList(false);
                mAdapter.getList(files);
                mHandler.sendEmptyMessage(MSG_HIDE_SHOW_DIALOG);
            }
        }.start();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mInflater = LayoutInflater.from(this);
        setContentView(R.layout.file_list);
        mContext = this;
        mListView = (ListView) findViewById(R.id.file_list);
        mListView.setAdapter(mAdapter);

        mListView.setOnItemClickListener(itemClickListener);
        mListView.setOnItemLongClickListener(itemLongClickListener);
        mListView.setOnKeyListener(keyListener);
        mPrefUtil = new PrefUtils(this);
        startScanThread();
    }

    private final View.OnKeyListener keyListener = new View.OnKeyListener() {
        @Override
        public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {
            if (keyCode == KeyEvent.KEYCODE_BACK && keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
                if (mAdapter.mFiles.length < 1) return false;
                File dir = mAdapter.mFiles[0];
                String dirParentPath =  dir.getParent();
                if (dirParentPath == null || dirParentPath.isEmpty()) return false;
                File parentDir = new File(dirParentPath);
                if (!parentDir.exists()) return false;
                Log.d(TAG, "onKey: parentDir path:" + parentDir.getPath());
                String grandparentDirPath = parentDir.getParent();
                if (grandparentDirPath == null || grandparentDirPath.isEmpty()) return false;
                File grandparentDir = new File(grandparentDirPath);
                if (!grandparentDir.exists()) return false;
                Log.d(TAG, "onKey: grandparentDir path:" + grandparentDir.getPath());

                File[] files = grandparentDir.listFiles(new DirectoryFilter());
                if (files == null || files.length == 0)
                    return false;
                boolean hasSubdirectories = false;
                for (File file : files) {
                    if (file.isDirectory()) {
                        Log.d(TAG, "onKey: file.isDirectory, file path:" + file.getPath());
                        hasSubdirectories = true;
                        break;
                    }
                }
                if (!hasSubdirectories)
                    return false;

                File[] path = {grandparentDir};
                mAdapter.files.clear();
                mAdapter.getList(path);
                return true;
            }
            return false;
        }
    };

    private final AdapterView.OnItemClickListener itemClickListener = new OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
            /*File selectFile = (File) adapterView.getItemAtPosition(position);
            if (selectFile.isFile()) {
                Intent intent = new Intent();
                intent.putExtra(FILE, selectFile.getPath());
                setResult(0, intent);
                finish();
            }*/
            File selectDir = (File) adapterView.getItemAtPosition(position);
            File[] files = selectDir.listFiles(new DirectoryFilter());
            if (files == null || files.length == 0) {
                Toast.makeText(mContext, getResources().getString(R.string.no_subfolders_tip), Toast.LENGTH_LONG).show();
                return;
            }

            boolean hasSubdirectories = false;
            for (File file : files) {
                if (file.isDirectory()) {
                    Log.d(TAG, "onItemClick: file.isDirectory, file path:" + file.getPath());
                    hasSubdirectories = true;
                    break;
                }
            }
            if (!hasSubdirectories) {
                Toast.makeText(mContext, getResources().getString(R.string.no_subfolders_tip), Toast.LENGTH_LONG).show();
                return;
            }

            File[] dir = {selectDir};
            mAdapter.files.clear();
            mAdapter.getList(dir);
        }
    };

    private final AdapterView.OnItemLongClickListener itemLongClickListener = new AdapterView.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long id) {
            Log.d(TAG, "onItemLongClick: get this media directory");
            File selectDir = (File) adapterView.getItemAtPosition(position);
            if (selectDir.isDirectory()) {
                Intent intent = new Intent();
                intent.putExtra(DIR, selectDir.getPath());
                setResult(queryReturnOk, intent);
                finish();
                return true;
            }
            return false;
        }
    };

    private class FileAdapter extends BaseAdapter {
        private File[] mFiles;
        private ArrayList<File> files = new ArrayList<>();

        public void setCurrentList(File directory) {
            File[] tempFiles = directory.listFiles(new DirectoryFilter());
            if (tempFiles == null) {
                Log.d(TAG, "setCurrentList: tempFiles is null");
                return;
            }
            Log.d(TAG, "setCurrentList: tempFiles size:" + tempFiles.length);

            for (int i = 0; (tempFiles != null) && (i < tempFiles.length);
                 i++) {
                if (tempFiles[i].isDirectory()) {
                    Log.d(TAG, "setCurrentList: tempFiles[i] getPath:" + tempFiles[i].getPath());
                    files.add(tempFiles[i]);
                    //setCurrentList ( tempFiles[i] );
                } else {
                    //files.add(tempFiles[i]);
                }
            }
            Log.d(TAG, "setCurrentList: files size:" + files.size());
        }

        public void getList(ArrayList<File> dir) {
            Log.d(TAG, "getList: (ArrayList<File> dir)");
            if (dir == null) {
                return;
            }
            for (int i = 0; i < dir.size(); i++) {
                File directory = dir.get(i);
                setCurrentList(directory);
            }
            mFiles = new File[files.size()];
            for (int i = 0; i < files.size(); i++) {
                mFiles[i] = (File) files.get(i);
            }
            mHandler.sendEmptyMessage(MSG_NOTIFY_DATACHANGE);
        }

        public void getList(File[] dir) {
            Log.d(TAG, "getList: (File[] dir)");
            for (int j = 0; j < dir.length; j++) {
                setCurrentList(dir[j]);
            }
            mFiles = new File[files.size()];
            for (int i = 0; i < files.size(); i++) {
                mFiles[i] = (File) files.get(i);
            }
            mHandler.sendEmptyMessage(MSG_NOTIFY_DATACHANGE);
        }

        @Override
        public int getCount() {
            return (mFiles == null) ? 0 : mFiles.length;
        }

        @Override
        public File getItem(int position) {
            File file = (mFiles == null) ? null : mFiles[position];
            return file;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.large_text, null);
            }
            TextView tv = (TextView) convertView;
            File dir = mFiles[position];
            String name = dir.getPath();
            String nameDest = null;
            if (mPrefUtil != null) {
                Log.d(TAG, "getView: mPrefUtil != null");
                nameDest = mPrefUtil.getTransPath(name);
            } else {
                Log.d(TAG, "getView: mPrefUtil == null");
                nameDest = name;
            }
            tv.setText(nameDest);
            return tv;
        }
    }

    class ZipFileFilter implements FilenameFilter {
        public boolean accept(File directory, String file) {
            String dir = directory.getPath();
            if (file.startsWith(".")) {
                //hide file skip
                return false;
            }
            if (new File(directory, file).isDirectory()) {
                return false;
            } else if (file.toLowerCase().endsWith(".zip")) {
                return true;
            } else {
                return false;
            }
        }
    }

    class DirectoryFilter implements FilenameFilter {
        public boolean accept(File directory, String file) {
            String dir = directory.getPath();
            Log.d(TAG, "DirectoryFilter accept: dir = " + dir);
            if (file.startsWith(".")) {
                //hide file skip, return false
                return false;
            }
            if (new File(directory, file).isDirectory())
                return true;
            /*else if (file.toLowerCase().endsWith(".zip")) {
                return false;*/
            else
                return false;
        }
    }

    public static class DirAndFileFilter implements FilenameFilter {
        public boolean accept(File directory, String file) {
            String dir = directory.getPath();
            Log.d(TAG, "DirAndFileFilter accept: " + file);
            if (file.startsWith(".")) {
                Log.d(TAG, "accept: file.startsWith(.):" + file);
                //hide file skip, return false
                return false;
            }
            if (file.toLowerCase().endsWith(".sign")) {
                //sign file skip, return false
                return false;
            } else {
                return true;
            }
        }
    }
}
