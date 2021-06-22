package cz.ackee.ui.textfield;

import android.content.Context;
import android.os.Build;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewParent;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatEditText;

import cz.ackee.ui.R;

import java.util.ArrayList;
import java.util.List;

/**
 * A special sub-class of {@link android.widget.EditText} designed for use as a child of {@link
 * TextInputLayout}.
 *
 * <p>Using this class allows us to display a hint in the IME when in 'extract' mode and provides
 * accessibility support for {@link TextInputLayout}.
 */
public class BindableTextInputEditText extends AppCompatEditText {

    private final List<TextWatcher> textWatchers = new ArrayList<>();

    public BindableTextInputEditText(Context context) {
        this(context, null);
        setBackground(null);
    }

    public BindableTextInputEditText(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.editTextStyle);
        setBackground(null);
    }

    public BindableTextInputEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setBackground(null);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        // Meizu devices expect TextView#mHintLayout to be non-null if TextView#getHint() is non-null.
        // In order to avoid crashing, we force the creation of the layout by setting an empty non-null
        // hint.
        TextInputLayout layout = getTextInputLayout();
        if (layout != null
                && layout.isProvidingHint()
                && super.getHint() == null
                && Build.MANUFACTURER.equalsIgnoreCase("Meizu")) {
            setHint("");
        }
    }

    @Nullable
    @Override
    public CharSequence getHint() {
        // Certain test frameworks expect the actionable element to expose its hint as a label. When
        // TextInputLayout is providing our hint, retrieve it from the parent layout.
        TextInputLayout layout = getTextInputLayout();
        if (layout != null && layout.isProvidingHint()) {
            return layout.getHint();
        }
        return super.getHint();
    }

    @Nullable
    @Override
    public InputConnection onCreateInputConnection(@NonNull EditorInfo outAttrs) {
        final InputConnection ic = super.onCreateInputConnection(outAttrs);
        if (ic != null && outAttrs.hintText == null) {
            // If we don't have a hint and our parent is a TextInputLayout, use its hint for the
            // EditorInfo. This allows us to display a hint in 'extract mode'.
            outAttrs.hintText = getHintFromLayout();
        }
        return ic;
    }

    @Nullable
    private TextInputLayout getTextInputLayout() {
        ViewParent parent = getParent();
        while (parent instanceof View) {
            if (parent instanceof TextInputLayout) {
                return (TextInputLayout) parent;
            }
            parent = parent.getParent();
        }
        return null;
    }

    @Nullable
    private CharSequence getHintFromLayout() {
        TextInputLayout layout = getTextInputLayout();
        return (layout != null) ? layout.getHint() : null;
    }

    @Override
    public void addTextChangedListener(TextWatcher watcher) {
        removeAllTextWatchers();
        if (watcher != null) {
            textWatchers.add(watcher);
        }
        super.addTextChangedListener(watcher);
    }

    @Override
    public void removeTextChangedListener(TextWatcher watcher) {
        if (watcher != null) {
            textWatchers.remove(watcher);
        }
        super.removeTextChangedListener(watcher);
    }

    private void removeAllTextWatchers() {
        for (int i = 0; i < textWatchers.size(); i++) {
            super.removeTextChangedListener(textWatchers.get(i));
        }
        textWatchers.clear();
    }

}