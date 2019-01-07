package th.ac.kmitl.it.crowdalert.component;

import android.app.AlertDialog;
import android.content.Context;

public abstract class Dialog {
    protected Context ctx;
    protected AlertDialog.Builder mBuilder;
    protected AlertDialog dialog;
    abstract void setupDialog();
}
