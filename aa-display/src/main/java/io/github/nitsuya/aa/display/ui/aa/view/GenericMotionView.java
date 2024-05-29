package io.github.nitsuya.aa.display.ui.aa.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.function.Consumer;

public class GenericMotionView extends View {

    public GenericMotionView(Context context) {
        super(context);
    }

    public GenericMotionView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public GenericMotionView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public GenericMotionView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public boolean onGenericMotionEvent(MotionEvent event) {
        onGenericMotionEvent.accept(event);
        return true;
    }

    public Consumer<MotionEvent> onGenericMotionEvent;

    public GenericMotionView setOnGenericMotionEvent(Consumer<MotionEvent> onGenericMotionEvent) {
        this.onGenericMotionEvent = onGenericMotionEvent;
        return this;
    }
}
