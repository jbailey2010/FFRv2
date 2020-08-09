package com.devingotaswitch.rankings.extras;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.AttributeSet;
import android.widget.ArrayAdapter;
import android.widget.SpinnerAdapter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MultiSelectionSpinner extends androidx.appcompat.widget.AppCompatSpinner implements
        DialogInterface.OnMultiChoiceClickListener
{
    private String[] _items = null;
    private boolean[] mSelection = null;
    private String defaultDisplay;

    private final ArrayAdapter<String> simple_adapter;

    public MultiSelectionSpinner(Context context) {
        super(context);

        simple_adapter = new ArrayAdapter<>(context,
                android.R.layout.simple_spinner_item);
        super.setAdapter(simple_adapter);
    }

    public MultiSelectionSpinner(Context context, AttributeSet attrs) {
        super(context, attrs);

        simple_adapter = new ArrayAdapter<>(context,
                android.R.layout.simple_spinner_item);
        super.setAdapter(simple_adapter);
    }

    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
        if (mSelection != null && which < mSelection.length) {
            mSelection[which] = isChecked;

            simple_adapter.clear();
            if (buildSelectedItemString().length() > 0) {
                simple_adapter.add(buildSelectedItemString());
            } else {
                simple_adapter.add(defaultDisplay);
            }
        } else {
            throw new IllegalArgumentException(
                    "Argument 'which' is out of bounds.");
        }
    }

    @Override
    public boolean performClick() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMultiChoiceItems(_items, mSelection, this);

        builder.setPositiveButton("Ok", (arg0, arg1) -> {

        });

        builder.show();
        return true;
    }

    @Override
    public void setAdapter(SpinnerAdapter adapter) {
        throw new RuntimeException(
                "setAdapter is not supported by MultiSelectSpinner.");
    }

    public void setItems(List<String> items, String defaultMesssage) {
        _items = items.toArray(new String[0]);
        mSelection = new boolean[_items.length];
        simple_adapter.clear();
        simple_adapter.add(defaultMesssage);
        this.defaultDisplay = defaultMesssage;
        Arrays.fill(mSelection, false);
    }

    public void setSelection(List<String> selection) {
        int found = 0;
        Arrays.fill(mSelection, false);
        for (String sel : selection) {
            for (int j = 0; j < _items.length; ++j) {
                if (_items[j].equals(sel)) {
                    found++;
                    mSelection[j] = true;
                }
            }
        }
        simple_adapter.clear();
        if (found == 0) {
            simple_adapter.add(defaultDisplay);
        } else {
            simple_adapter.add(buildSelectedItemString());
        }
    }

    public void setSelection(int index) {
        Arrays.fill(mSelection, false);
        if (index >= 0 && index < mSelection.length) {
            mSelection[index] = true;
        } else {
            throw new IllegalArgumentException("Index " + index
                    + " is out of bounds.");
        }
        simple_adapter.clear();
        simple_adapter.add(buildSelectedItemString());
    }

    public Set<String> getSelectedStrings() {
        Set<String> selection = new HashSet<>();
        for (int i = 0; i < _items.length; ++i) {
            if (mSelection[i]) {
                selection.add(_items[i]);
            }
        }
        return selection;
    }

    private String buildSelectedItemString() {
        StringBuilder sb = new StringBuilder();
        boolean foundOne = false;

        for (int i = 0; i < _items.length; ++i) {
            if (mSelection[i]) {
                if (foundOne) {
                    sb.append(", ");
                }
                foundOne = true;

                sb.append(_items[i]);
            }
        }
        return sb.toString();
    }
}