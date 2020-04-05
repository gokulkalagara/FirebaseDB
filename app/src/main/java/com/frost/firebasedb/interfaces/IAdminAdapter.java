package com.frost.firebasedb.interfaces;

import com.frost.firebasedb.models.User;

public interface IAdminAdapter {
    public void deleteAdmin(User user, int position);

    public void doCall(User user, int position);
}
