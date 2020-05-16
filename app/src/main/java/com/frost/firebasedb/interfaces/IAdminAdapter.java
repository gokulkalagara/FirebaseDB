package com.frost.firebasedb.interfaces;

import com.frost.firebasedb.models.User;

public interface IAdminAdapter {
    void deleteAdmin(User user, int position);

    void doCall(User user, int position);

    void openRideLogs(User user, int position);
}
