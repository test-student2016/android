package org.owntracks.android.data.repos;

import android.databinding.ObservableArrayList;
import android.databinding.ObservableArrayMap;
import android.databinding.ObservableList;
import android.databinding.ObservableMap;
import android.support.annotation.NonNull;

import org.owntracks.android.App;
import org.owntracks.android.injection.scopes.PerApplication;
import org.owntracks.android.messages.MessageCard;
import org.owntracks.android.messages.MessageLocation;
import org.owntracks.android.model.FusedContact;

import javax.inject.Inject;

import timber.log.Timber;

@PerApplication
public class MemoryContactsRepo implements ContactsRepo {
    ObservableMap<String, FusedContact> mMap;
    ObservableList<FusedContact> mList;

    private static final long MAJOR_STEP = 1000000;
    private long majorRevision = 0;
    private long revision = 0;



    @Inject
    public MemoryContactsRepo() {
        mMap = new ObservableArrayMap<>();
        mList = new ObservableArrayList<>();
    }

    @Override
    public ObservableMap<String, FusedContact> getAllAsMap() {
        return mMap;
    }

    @Override
    public ObservableList<FusedContact> getAllAsList() {
        return mList;
    }

    @Override
    public FusedContact getById(String id) {
        return mMap.get(id);
    }


    private @NonNull FusedContact getByIdLazy(String id) {
        FusedContact c = getById(id);

        if (c == null) {
            c = new FusedContact(id);
            put(id, c);
        }

        return c;
    }

    private void put(String id, FusedContact contact) {
        mMap.put(id, contact);
        mList.add(contact);
        Timber.v("new contact added:%s", id);
        revision++;
    }

    @Override
    public void clearAll() {
        mMap.clear();
        mList.clear();
        majorRevision-=MAJOR_STEP;
        revision = 0;
    }

    @Override
    public void remove(String id) {
        FusedContact c = mMap.remove(id);
        if(c != null)
            mList.remove(c);
        majorRevision-=MAJOR_STEP;
        revision = 0;
    }

    @Override
    public void update(String id, MessageCard m) {
        FusedContact c = getByIdLazy(id);
        c.setMessageCard(m);
        App.getEventBus().post(c);
        revision++;
    }

    @Override
    public void update(String id, MessageLocation m) {
        FusedContact c = getByIdLazy(id);
        if(c.getMessageLocation() != m)
            c.setMessageLocation(m);
        App.getEventBus().post(c);
        Timber.v("contact length %s", mList.size());
        revision++;
    }

    // Allows for quickly checking if items in the repo were updated/added (getRevison > savedRevision) or removed (getRevision < savedRevision)
    public long getRevision() {
        return majorRevision+revision;
    }



}
