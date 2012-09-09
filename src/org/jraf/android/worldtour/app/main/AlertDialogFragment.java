/*
 * This source is part of the
 *      _____  ___   ____
 *  __ / / _ \/ _ | / __/___  _______ _
 * / // / , _/ __ |/ _/_/ _ \/ __/ _ `/
 * \___/_/|_/_/ |_/_/ (_)___/_/  \_, /
 *                              /___/
 * repository.
 *
 * Copyright 2012 Benoit 'BoD' Lubek (BoD@JRAF.org).  All Rights Reserved.
 */
package org.jraf.android.worldtour.app.main;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

public class AlertDialogFragment extends DialogFragment {
    public AlertDialogFragment() {}

    public static AlertDialogFragment newInstance(CharSequence message) {
        Bundle args = new Bundle(1);
        args.putCharSequence("message", message);
        AlertDialogFragment res = new AlertDialogFragment();
        res.setArguments(args);
        return res;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(getArguments().getCharSequence("message"));
        builder.setPositiveButton(android.R.string.ok, null);
        return builder.create();
    }
}
