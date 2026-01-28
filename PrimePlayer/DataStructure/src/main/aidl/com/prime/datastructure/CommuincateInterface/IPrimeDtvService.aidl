package com.prime.datastructure.CommuincateInterface;
import com.prime.datastructure.CommuincateInterface.ParcelableClass;
import com.prime.datastructure.CommuincateInterface.IPrimeDtvServiceCallback;

interface IPrimeDtvService {
    Bundle invokeBundle(in Bundle data);
    ParcelableClass invokeParcel(int commandId, in ParcelableClass param);
    void registerCallback(IPrimeDtvServiceCallback callback, String caller);
    void unregisterCallback(IPrimeDtvServiceCallback callback);
}