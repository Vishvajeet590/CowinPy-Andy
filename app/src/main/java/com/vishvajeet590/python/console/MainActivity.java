package com.vishvajeet590.python.console;

import android.app.*;
import com.vishvajeet590.python.utils.*;
import com.vishvajeet590.python.utils.PythonConsoleActivity;

public class MainActivity extends PythonConsoleActivity {

    @Override protected Class<? extends Task> getTaskClass() {
        return Task.class;
    }

    public static class Task extends PythonConsoleActivity.Task {
        public Task(Application app) {
            super(app);
        }

        @Override public void run() {
            py.getModule("main").callAttr("main");
        }
    }
}
