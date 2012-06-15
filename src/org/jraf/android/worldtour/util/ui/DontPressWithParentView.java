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
package org.jraf.android.worldtour.util.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

/**
 * See http://android.cyrilmottier.com/?p=525.
 */
public class DontPressWithParentView extends View {

    public DontPressWithParentView(Context context) {
        super(context);
    }

    public DontPressWithParentView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DontPressWithParentView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void setPressed(boolean pressed) {
        if (pressed && getParent() instanceof View && ((View) getParent()).isPressed()) {
            return;
        }
        super.setPressed(pressed);
    }
}
