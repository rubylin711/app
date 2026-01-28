package com.prime.dtv.service.Dvr;

import android.os.ParcelFileDescriptor;
import android.util.Log;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DvrRecordInfo {
    private static final String TAG = "DVR DvrRecordInfo";
    static public final String FileInfoExtension =".dat";
    public static class FileParameters {
        private long ibase;
        private long iPts;
        private long iFrameSize;

        public FileParameters(long ibase, long iPts, long iFrameSize) {
            this.ibase = ibase;
            this.iPts = iPts;
            this.iFrameSize = iFrameSize;
        }

        public long getIbase() {
            return ibase;
        }

        public void setIbase(long ibase) {
            this.ibase = ibase;
        }

        public long getiPts() {
            return iPts;
        }

        public void setiPts(long iPts) {
            this.iPts = iPts;
        }

        public long getiFrameSize() {
            return iFrameSize;
        }

        public void setiFrameSize(long iFrameSize) {
            this.iFrameSize = iFrameSize;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            FileParameters myClass = (FileParameters) o;
            if(myClass.ibase==ibase&&myClass.iPts==iPts&&myClass.iFrameSize==iFrameSize){
                return true;
            }
            return false;
        }
        @Override
        public String toString() {
            return "FileParameters{" +
                    "ibase=" + ibase +
                    ", iPts=" + iPts +
                    ", iFrameSize=" + iFrameSize +
                    '}';
        }
    }

    public static class FileDataManager {
        private static final String TAG = "DVR FileDataManager";
        private String fileName;  // /storage/..../DMG/REC_x/REC_x.ts
        private String baseName;  // /storage/..../DMG/REC_x/REC_x.ts.dat
        public List<FileParameters> fileParametersList; //Analyze and save all the contents of REC_x.ts.dat. Each line in REC_x.ts.dat is a FileParameters object.
        private int parametersListindex =0;
        private  FileParameters parametersListFileParameters;

        public FileDataManager(String fileName,String baseName) {
            this.fileName = fileName;
            this.baseName = baseName;
            this.fileParametersList = new ArrayList<>();
        }

        public void clearFileData() {
            fileParametersList.clear();
        }

        public void addFileData(FileParameters parameters) {
            fileParametersList.add(parameters);
        }

        public FileParameters getFileData(int index) {
            if (!fileParametersList.isEmpty()) {
                return fileParametersList.get(index);
            }
            return null;
        }

        public String getFileName() {
            return fileName;
        }
        public String getbaseName(){
            return baseName;
        }

        public void printFileData() {
            int number = fileParametersList.size();
            Log.d(TAG, "File: " + fileName);
            for (int i = 0; i < number; i++) {
                FileParameters parameters = getFileData(i);
                if (parameters != null) {
                    Log.d(TAG, parameters.toString());
                } else {
                    Log.d(TAG, "No file data for " + fileName);
                }
            }

        }
        public FileParameters findNearestPtsInData(long targetPts,boolean flag){
            if(fileParametersList == null ||fileParametersList.isEmpty())
                return null;
            FileParameters nearestPtsInData = fileParametersList.get(0);
            long mindiff = Math.abs(nearestPtsInData.getiPts() - targetPts);
            for(int i=0;i<fileParametersList.size();i++){
                long value = fileParametersList.get(i).getiPts();
                long difference = Math.abs(value - targetPts);
                if(difference < mindiff){
                    mindiff = difference;
                    nearestPtsInData = fileParametersList.get(i);
                    if(flag){
                        parametersListindex = i;
                        parametersListFileParameters = nearestPtsInData;
                    }
                }
            }
            return nearestPtsInData;
        }

        public FileParameters findNextFileParameters(FileParameters p,boolean flag){
            if(fileParametersList == null ||fileParametersList.isEmpty())
                return null;
            int index = fileParametersList.indexOf(p);
            if(index == -1){
                Log.d(TAG,"findNextFileParameters error!");
            }
            parametersListindex = index;
            FileParameters nearestPtsInData = p;
            if(parametersListindex+1<fileParametersList.size()){
                nearestPtsInData = fileParametersList.get(parametersListindex+1);
                if(flag)
                    parametersListindex = parametersListindex+1;
            }else{
                nearestPtsInData = null;
                if(flag){
                    parametersListindex = 0 ;
                    parametersListFileParameters = nearestPtsInData;
                }
            }
            Log.d(TAG, "findNextFileParameters: equals true: "+parametersListindex);

            return nearestPtsInData;
        }

        public FileParameters findPreviousFileParameters(FileParameters p,boolean flag){
            if(fileParametersList == null ||fileParametersList.isEmpty()){
                Log.d(TAG, "findPreviousFileParameters: equals true: "+fileParametersList +" fileParametersList.isEmpty(): "+fileParametersList.isEmpty());
                return null;
            }
            int index = fileParametersList.indexOf(p);
            parametersListindex = index;
            FileParameters nearestPtsInData = p;
            if(parametersListindex-1>=0){
                nearestPtsInData = fileParametersList.get(parametersListindex-1);
                if(flag)
                    parametersListindex = parametersListindex-1;
            }else{
                nearestPtsInData = null;
                if(flag){
                    parametersListindex = 0 ;
                    parametersListFileParameters = nearestPtsInData;
                }
            }
            Log.d(TAG, "findPreviousFileParameters: equals true: "+parametersListindex);

            return nearestPtsInData;
        }

        public FileParameters getLastNearestPtsInData(boolean flag){
            if(fileParametersList == null ||fileParametersList.isEmpty())
                return null;
            int index = fileParametersList.size() -1;
            FileParameters nearestPtsInData = fileParametersList.get(index);
            if(flag)
                parametersListindex = index ;
            Log.d(TAG, "getLastNearestPtsInData: "+nearestPtsInData.toString());
            return nearestPtsInData;
        }
        public FileParameters getFirstNearestPtsInData(boolean flag){
            if(fileParametersList == null ||fileParametersList.isEmpty())
                return null;
            FileParameters nearestPtsInData = fileParametersList.get(0);
            if(flag){
                parametersListindex  = 0;
                parametersListFileParameters = nearestPtsInData;
            }
            Log.d(TAG, "getFirstNearestPtsInData: "+nearestPtsInData.toString());
            return nearestPtsInData;
        }

        public void parseFileContent(ParcelFileDescriptor parcelFileDescriptor) {
            if (parcelFileDescriptor == null) {
                Log.d(TAG, "parcelFileDescriptor is null");
                return;
            }
            FileInputStream fileInputStream = null;
            BufferedReader bufferedReader = null;
            try {
                fileInputStream = new FileInputStream(parcelFileDescriptor.getFileDescriptor());
                bufferedReader = new BufferedReader(new InputStreamReader(fileInputStream));
                String line;
                clearFileData();
                while ((line = bufferedReader.readLine()) != null) {
                    // Log.d(TAG,line);
                    parseLine(line);
                }
                //printFileData();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (bufferedReader != null) {
                        bufferedReader.close();
                    }
                    if (fileInputStream != null) {
                        fileInputStream.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private void parseLine(String line) {
            String[] parts = line.split("\t");
            if (parts.length == 3) {
                String ibase = extractValue(parts[0], "Ibase:");
                String iPts = extractValue(parts[1], "I-Pts:");
                String iFrameSize = extractValue(parts[2], "I-Frame size:");
                try {
                    FileParameters tmpFileParameter = new FileParameters(Long.parseLong(ibase), Long.parseLong(iPts), Long.parseLong(iFrameSize));
                    addFileData(tmpFileParameter);
                } catch (Exception e) {
                    Log.d(TAG, "ERROR!!: manager1:" + "ibase: " + ibase + " iPts: " + iPts + " iFrameSize: " + iFrameSize);
                }

                // Log.d(TAG,"Ibase: " + ibase);
                // Log.d(TAG,"I-Pts: " + iPts);
                // Log.d(TAG,"I-Frame size: " + iFrameSize);
                // Log.d(TAG,"-------------------");
            } else {
                System.err.println("Invalid line format: " + line);
            }
        }

        private String extractValue(String part, String prefix) {
            if (part.startsWith(prefix)) {
                return part.substring(prefix.length());
            } else {
                return null;
            }
        }
    }

    public static class FileDataListManager {
        private static final String TAG = "DVR FileDataListManager";
        public List<FileDataManager> FileDataManagerList;
        private int mFileDataManagerIndex;
        private FileDataManager mFileDataManager;

        public  FileDataListManager() {
            this.FileDataManagerList = new ArrayList<>();
        }

        public void add(FileDataManager m){
            FileDataManagerList.add(m);
        }

        public void remove(int index){
            FileDataManagerList.remove(index);
        }

        public void removeAll(List<FileDataManager> list){
            FileDataManagerList.removeAll(list);
        }

        public FileDataManager findNearestPtsInData(long targetPts,boolean flag){
            if(FileDataManagerList == null ||FileDataManagerList.isEmpty()||FileDataManagerList.get(0).fileParametersList.isEmpty())
                return null;
            FileDataManager nearestFileDataManager = FileDataManagerList.get(0);
            FileParameters  nearestPtsInData = FileDataManagerList.get(0).fileParametersList.get(0);
            long mindiff = Math.abs(nearestPtsInData.getiPts() - targetPts);
            int index = 0;
            for(int j=0;j<FileDataManagerList.size();j++){
                try {
                    FileParameters m = FileDataManagerList.get(j).findNearestPtsInData(targetPts,false);
                    long value = m.getiPts();
                    long difference = Math.abs(value - targetPts);
                    if(difference < mindiff){
                        mindiff = difference;
                        //nearestPtsInData = m;
                        nearestFileDataManager = FileDataManagerList.get(j);
                        index = j;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if(flag){
                mFileDataManagerIndex = index;
                mFileDataManager = nearestFileDataManager;
                FileDataManagerList.get(index).findNearestPtsInData(targetPts,true);
            }
            return nearestFileDataManager;
        }

        public Boolean checkIsFirstFileDataManager(FileDataManager m ){
            Boolean errBoolean = false;
            int index = -1;
            if(m != null)
                index = FileDataManagerList.indexOf(m);
            if(index == 0 || index == -1){
                errBoolean = true;
            }
            return  errBoolean;
        }

        public Boolean checkIsLastFileDataManager(FileDataManager m ){
            Boolean errBoolean = false;
            int index = -1;
            if(m != null)
                index = FileDataManagerList.indexOf(m);
            if(index == FileDataManagerList.size()-1||index == -1){
                errBoolean = true;
            }
            return  errBoolean;

        }

        public FileDataManager findNextNearestPtsInData(boolean flag){
            Log.d(TAG, "enter findPreviousNearestPtsInData: size: "+FileDataManagerList.size()+" mFileDataManagerIndex :"+mFileDataManagerIndex );
            mFileDataManagerIndex = FileDataManagerList.indexOf(mFileDataManager);
            if(mFileDataManagerIndex == -1){
                Log.d(TAG,"findNextNearestPtsInData mFileDataManagerIndex error!");
                mFileDataManagerIndex = FileDataManagerList.size()-1;
            }
            FileDataManager nearestFileDataManager = FileDataManagerList.get(0);
            int tmpFileDataManagerIndex  = mFileDataManagerIndex+1;
            if(tmpFileDataManagerIndex>=FileDataManagerList.size()){
                nearestFileDataManager = FileDataManagerList.get(FileDataManagerList.size()-1);
                tmpFileDataManagerIndex = FileDataManagerList.size()-1;
            }else{
                nearestFileDataManager = FileDataManagerList.get(tmpFileDataManagerIndex);
            }

            if(flag){
                mFileDataManagerIndex = tmpFileDataManagerIndex;
                mFileDataManager = nearestFileDataManager;
            }
            Log.d(TAG, "exit findPreviousNearestPtsInData: size: "+FileDataManagerList.size()+" mFileDataManagerIndex :"+mFileDataManagerIndex );
            return nearestFileDataManager;
        }

        public FileDataManager findFirstNearestPtsInData(boolean flag){

            if(FileDataManagerList.isEmpty())
                return null;
            FileDataManager nearestFileDataManager = FileDataManagerList.get(0);
            if(flag){
                mFileDataManagerIndex = 0;
                mFileDataManager = nearestFileDataManager;
            }
            return nearestFileDataManager;
        }

        public FileDataManager findLastNearestPtsInData(boolean flag){

            if(FileDataManagerList.isEmpty())
                return null;
            FileDataManager nearestFileDataManager = FileDataManagerList.get(FileDataManagerList.size()-1);
            if(flag){
                mFileDataManagerIndex = 0;
                mFileDataManager = nearestFileDataManager;
            }
            return nearestFileDataManager;
        }

        public FileDataManager findPreviousNearestPtsInData(boolean flag){

            Log.d(TAG, "findPreviousNearestPtsInData: size: "+FileDataManagerList.size()+" mFileDataManagerIndex :"+mFileDataManagerIndex );
            mFileDataManagerIndex = FileDataManagerList.indexOf(mFileDataManager);
            if(mFileDataManagerIndex == -1){
                Log.d(TAG,"mFileDataManagerIndex error!");
            }
            FileDataManager nearestFileDataManager = FileDataManagerList.get(FileDataManagerList.size()-1);
            int tmpFileDataManagerIndex  = mFileDataManagerIndex-1;
            if(tmpFileDataManagerIndex<0){
                nearestFileDataManager = FileDataManagerList.get(0);
                tmpFileDataManagerIndex = 0;
            }else{
                nearestFileDataManager = FileDataManagerList.get(tmpFileDataManagerIndex);
            }

            if(flag){
                mFileDataManagerIndex = tmpFileDataManagerIndex;
                mFileDataManager = nearestFileDataManager;
            }
            return nearestFileDataManager;
        }

        public void update(String path){

        }
    }
    }
