package com.vishvajeet590.python.utils;

import androidx.annotation.NonNull;
import android.app.*;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.*;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.*;
import android.text.*;
import android.text.style.*;
import android.util.Log;
import android.view.*;
import android.view.inputmethod.*;
import android.widget.*;
import androidx.annotation.*;
import androidx.appcompat.app.*;
import androidx.core.content.*;
import androidx.lifecycle.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.StringTokenizer;

public abstract class ConsoleActivity extends AppCompatActivity
        implements ViewTreeObserver.OnGlobalLayoutListener, ViewTreeObserver.OnScrollChangedListener {

    // Because tvOutput has freezesText enabled, letting it get too large can cause a
    // TransactionTooLargeException. The limit isn't in the saved state itself, but in the
    // Binder transaction which transfers it to the system server. So it doesn't happen if
    // you're rotating the screen, but it does happen when you press Back.
    //
    // The exception message shows the size of the failed transaction, so I can determine from
    // experiment that the limit is about 500 KB, and each character consumes 4 bytes.
    private final int MAX_SCROLLBACK_LEN = 100000;

    private EditText etInput;
    boolean enterOTP = false;
    private ScrollView svOutput;
    private TextView tvOutput;
    private int outputWidth = -1, outputHeight = -1;
    public dataModel mod;
    private Button refreshBtn;
    public  boolean refreshNow = false;

    boolean to_Sava = false;

    int pointer = 0;
    String[] data_svae;

    boolean readable = false;
    String data_to_save ="";

    enum Scroll {
        TOP, BOTTOM
    }
    private Scroll scrollRequest;

    public static class ConsoleModel extends ViewModel {
        boolean pendingNewline = false;  // Prevent empty line at bottom of screen
        int scrollChar = 0;              // Character offset of the top visible line.
        int scrollAdjust = 0;            // Pixels by which that line is scrolled above the top
        //   (prevents movement when keyboard hidden/shown).
    }
    private ConsoleModel consoleModel;

    protected Task task;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        consoleModel = ViewModelProviders.of(this).get(ConsoleModel.class);
        task = ViewModelProviders.of(this).get(getTaskClass());
        registerReceiver(broadcastReceiver, new IntentFilter("SMSBraodcast"));
        registerReceiver(broadcastReceiver_data, new IntentFilter("StartData"));

        data_svae = new String[13];

        mod = readData();
        setContentView(resId("layout", "activity_console"));
        createInput();
        createOutput();


    }

    protected abstract Class<? extends Task> getTaskClass();

    private void createInput() {
        etInput = findViewById(resId("id", "etInput"));
        refreshBtn = findViewById(resId("id","refreshBtn"));
        refreshBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startRefresh(etInput);
            }
        });




        // Strip formatting from pasted text.
        etInput.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            public void afterTextChanged(Editable e) {
                for (CharacterStyle cs : e.getSpans(0, e.length(), CharacterStyle.class)) {
                    e.removeSpan(cs);
                }
            }
        });

        // At least on API level 28, if an ACTION_UP is lost during a rotation, then the app
        // (or any other app which takes focus) will receive an endless stream of ACTION_DOWNs
        // until the key is pressed again. So we react to ACTION_UP instead.
        etInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE ||
                        (event != null && event.getAction() == KeyEvent.ACTION_UP)) {

                    if (pointer == 999){
                        if(etInput.getText().toString().equalsIgnoreCase("y")){
                            data_svae[0] = "Config";
                            for (String x : data_svae){
                                data_to_save = data_to_save+"%"+x;
                            }
                            saveToAllConfigs(data_to_save);
                            data_to_save = "";

                        }
                    }
                    else if (pointer == 99){

                    }
                    if (readable == false && to_Sava == true && pointer != 99){
                        //Toast.makeText(ConsoleActivity.this, "Cap = "+etInput.getText().toString(), Toast.LENGTH_SHORT).show();
                        data_svae[pointer] = etInput.getText().toString();
                        to_Sava = false;

                    }


                    String text = etInput.getText().toString() + "\n";
                    etInput.setText("");
                    output(span(text, new StyleSpan(Typeface.BOLD)));
                    scrollTo(Scroll.BOTTOM);
                    task.onInput(text);

                }

                // If we return false on ACTION_DOWN, we won't be given the ACTION_UP.
                return true;
            }
        });

        task.inputEnabled.observe(this, new Observer<Boolean>() {
            @Override public void onChanged(@Nullable Boolean enabled) {
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                if (enabled) {
                    etInput.setVisibility(View.VISIBLE);
                    etInput.setEnabled(true);

                    // requestFocus alone doesn't always bring up the soft keyboard during startup
                    // on the Nexus 4 with API level 22: probably some race condition. (After
                    // rotation with input *already* enabled, the focus may be overridden by
                    // onRestoreInstanceState, which will run after this observer.)
                    etInput.requestFocus();
                    imm.showSoftInput(etInput, InputMethodManager.SHOW_IMPLICIT);
                } else {
                    // Disable rather than hide, otherwise tvOutput gets a gray background on API
                    // level 26, like tvCaption in the main menu when you press an arrow key.
                    etInput.setEnabled(false);
                    imm.hideSoftInputFromWindow(tvOutput.getWindowToken(), 0);
                }
            }
        });
    }

    private void createOutput() {
        svOutput = findViewById(resId("id", "svOutput"));
        svOutput.getViewTreeObserver().addOnGlobalLayoutListener(this);

        tvOutput = findViewById(resId("id", "tvOutput"));
        if (Build.VERSION.SDK_INT >= 23) {
            // noinspection WrongConstant
            tvOutput.setBreakStrategy(Layout.BREAK_STRATEGY_SIMPLE);
        }
        // Don't start observing task.output yet: we need to restore the scroll position first so
        // we maintain the scrolled-to-bottom state.
    }

    @Override protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        // Don't restore the UI state unless we have the non-UI state as well.
        if (task.getState() != Thread.State.NEW) {
            super.onRestoreInstanceState(savedInstanceState);
        }
    }

    @Override protected void onResume() {
        super.onResume();
        // Needs to be in onResume rather than onStart because onRestoreInstanceState runs
        // between them.
        if (task.getState() == Thread.State.NEW) {
            task.start();
        }
    }

    @Override protected void onPause() {
        super.onPause();
        saveScroll();  // Necessary to save bottom position in case we've never scrolled.
    }

    // This callback is run after onResume, after each layout pass. If a view's size, position
    // or visibility has changed, the new values will be visible here.
    @Override public void onGlobalLayout() {
        if (outputWidth != svOutput.getWidth() || outputHeight != svOutput.getHeight()) {
            // Can't register this listener in onCreate on API level 15
            // (https://stackoverflow.com/a/35054919).
            if (outputWidth == -1) {
                svOutput.getViewTreeObserver().addOnScrollChangedListener(this);
            }

            // Either we've just started up, or the keyboard has been hidden or shown.
            outputWidth = svOutput.getWidth();
            outputHeight = svOutput.getHeight();
            restoreScroll();
        } else if (scrollRequest != null) {
            int y = -1;
            switch (scrollRequest) {
                case TOP:
                    y = 0;
                    break;
                case BOTTOM:
                    y = tvOutput.getHeight();
                    break;
            }

            // Don't use smooth scroll, because if an output call happens while it's animating
            // towards the bottom, isScrolledToBottom will believe we've left the bottom and
            // auto-scrolling will stop. Don't use fullScroll either, because not only does it use
            // smooth scroll, it also grabs focus.
            svOutput.scrollTo(0, y);
            scrollRequest = null;
        }
    }

    @Override public void onScrollChanged() {
        saveScroll();
    }

    // After a rotation, a ScrollView will restore the previous pixel scroll position. However, due
    // to re-wrapping, this may result in a completely different piece of text being visible. We'll
    // try to maintain the text position of the top line, unless the view is scrolled to the bottom,
    // in which case we'll maintain that. Maintaining the bottom line will also cause a scroll
    // adjustment when the keyboard's hidden or shown.
    private void saveScroll() {
        if (isScrolledToBottom()) {
            consoleModel.scrollChar = tvOutput.getText().length();
            consoleModel.scrollAdjust = 0;
        } else {
            int scrollY = svOutput.getScrollY();
            Layout layout = tvOutput.getLayout();
            if (layout != null) {  // See note in restoreScroll
                int line = layout.getLineForVertical(scrollY);
                consoleModel.scrollChar = layout.getLineStart(line);
                consoleModel.scrollAdjust = scrollY - layout.getLineTop(line);
            }
        }
    }

    private void restoreScroll() {
        removeCursor();

        // getLayout sometimes returns null even when called from onGlobalLayout. The
        // documentation says this can happen if the "text or width has recently changed", but
        // does not define "recently". See Electron Cash issues #1330 and #1592.
        Layout layout = tvOutput.getLayout();
        if (layout != null) {
            int line = layout.getLineForOffset(consoleModel.scrollChar);
            svOutput.scrollTo(0, layout.getLineTop(line) + consoleModel.scrollAdjust);
        }

        // If we are now scrolled to the bottom, we should stick there. (scrollTo probably won't
        // trigger onScrollChanged unless the scroll actually changed.)
        saveScroll();

        task.output.removeObservers(this);
        task.output.observe(this, new Observer<CharSequence>() {
            @Override public void onChanged(@Nullable CharSequence text) {
                output(text);
            }
        });
    }

    private boolean isScrolledToBottom() {
        int visibleHeight = (svOutput.getHeight() - svOutput.getPaddingTop() -
                svOutput.getPaddingBottom());
        int maxScroll = Math.max(0, tvOutput.getHeight() - visibleHeight);
        return (svOutput.getScrollY() >= maxScroll);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater mi = getMenuInflater();
        mi.inflate(resId("menu", "top_bottom"), menu);
        return true;
    }

    @Override public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == resId("id", "menu_top")) {
            scrollTo(Scroll.TOP);
        } else if (id == resId("id", "menu_bottom")) {
            scrollTo(Scroll.BOTTOM);
        } else {
            return false;
        }
        return true;
    }

    public static Spannable span(CharSequence text, Object... spans) {
        Spannable spanText = new SpannableStringBuilder(text);
        for (Object span : spans) {
            spanText.setSpan(span, 0, text.length(), 0);
        }
        return spanText;
    }

    private void output(CharSequence text) {

        removeCursor();
        if (consoleModel.pendingNewline) {
            tvOutput.append("\n");
            consoleModel.pendingNewline = false;
        }
        if (text.charAt(text.length() - 1) == '\n') {
            tvOutput.append(text.subSequence(0, text.length() - 1));
            consoleModel.pendingNewline = true;
        } else {
            tvOutput.append(text);
            Log.d("Test", "output: "+text);
            String Otptext = "";
            if (text.toString().equals("BEEP1S")){
                ToneGenerator toneGen1 = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);
                toneGen1.startTone(ToneGenerator.TONE_CDMA_ABBR_ALERT,150);
            }


            if (readable == true){

                if (text.toString().contains("Enter OTP (If this takes more than 2 minutes, press Enter to retry)")){

                    enterOTP = true;
                }
                if(text.toString().contains("Enter the registered mobile number")){
                    etInput.setText(mod.number);
                    pressEnter(etInput);
                    Log.d("Test", "input Num: "+mod.number);

                }
                if (text.toString().contains("If you're selecting multiple beneficiaries, make sure all are of the same age group (45+ or 18+)")){
                    etInput.setText(mod.benificiary);
                    pressEnter(etInput);
                    Log.d("Test", "input Ben: "+mod.benificiary);
                }
                if (text.toString().contains("Enter 0 for No Preference, 1 for COVISHIELD, 2 for COVAXIN, or 3 for SPUTNIK V. Default 0")){
                    etInput.setText(mod.vacType);
                    pressEnter(etInput);
                    Log.d("Test", "input vac: "+mod.vacType);

                }
                if (text.toString().contains("Enter 1 for Pincode or 2 for State/District. (Default 2) :")){
                    etInput.setText(mod.search);
                    pressEnter(etInput);
                    Log.d("Test", "input sear: "+mod.search);

                }
                if (text.toString().contains("Enter comma separated pincodes to monitor")){
                    etInput.setText(mod.pincodes);
                    pressEnter(etInput);
                    Log.d("Test", "input pin: "+mod.pincodes);

                }
                if (text.toString().contains("Enter State index:")){
                    etInput.setText(mod.state);
                    pressEnter(etInput);
                    Log.d("Test", "input state: "+mod.state);
                }
                if (text.toString().contains("Enter comma separated index numbers of districts to monitor")){
                    etInput.setText(mod.district);
                    pressEnter(etInput);
                    Log.d("Test", "input dis: "+mod.district);
                }
                if (text.toString().contains("Filter out centers with availability less than ?")){
                    etInput.setText(mod.availibility);
                    pressEnter(etInput);
                    Log.d("Test", "input avial: "+mod.availibility);
                }
                if (text.toString().contains("How often do you want to refresh the calendar (in seconds)? Default 15. Minimum 5")){
                    etInput.setText(mod.refresh);
                    pressEnter(etInput);
                    Log.d("Test", "input res: "+mod.refresh);
                }

                if (text.toString().contains("Use 1 for today, 2 for tomorrow, or provide a date in the format DD-MM-YYYY. Default 2:")){
                    etInput.setText(mod.startDay);
                    pressEnter(etInput);
                    Log.d("Test", "input startDay: "+mod.startDay);
                }
                if (text.toString().contains("Enter 0 for No Preference, 1 for Free Only, or 2 for Paid Only. Default 0")){
                    etInput.setText(mod.cost);
                    pressEnter(etInput);
                    Log.d("Test", "input cost: "+mod.cost);
                }
                if (text.toString().contains("Do you want to enable auto-booking? (yes-please or no) Default no")){
                    etInput.setText(mod.auto_book);
                    pressEnter(etInput);
                    Log.d("Test", "input auto: "+mod.auto_book);
                }

                /*if (text.toString().contains("Would you like to save this as a JSON file for easy use next time?: (y/n Default y):")){
                    etInput.setText('n');
                    pressEnter(etInput);
                    Log.d("Test", "input save = N");
                }*/


            }

            else {
                if (text.toString().contains("Enter OTP (If this takes more than 2 minutes, press Enter to retry)")){
                    pointer = 99;
                    enterOTP = true;
                    to_Sava = false;
                }

                if (text.toString().contains("Would you like to save this as a JSON file for easy use next time?: (y/n Default y):")){
                    pointer = 999;
                    to_Sava = false;
                }


                if(text.toString().contains("Enter the registered mobile number")){
                    to_Sava = true;
                    pointer = 3;

                }
                if (text.toString().contains("If you're selecting multiple beneficiaries, make sure all are of the same age group (45+ or 18+)")){
                   // etInput.setText(mod.benificiary);
                    //pressEnter(etInput);
                    //Log.d("Test", "input Ben: "+mod.benificiary);
                    pointer = 1;
                    to_Sava = true;
                }
                if (text.toString().contains("Enter 0 for No Preference, 1 for COVISHIELD, 2 for COVAXIN, or 3 for SPUTNIK V. Default 0")){
                    pointer = 4;
                    to_Sava = true;

                }
                if (text.toString().contains("Enter 1 for Pincode or 2 for State/District. (Default 2) :")){
                    pointer = 2;
                    to_Sava = true;

                }
                if (text.toString().contains("Enter comma separated pincodes to monitor")){
                    pointer = 5;
                    to_Sava = true;

                }
                if (text.toString().contains("Enter State index:")){
                   pointer = 6;
                    to_Sava = true;
                }
                if (text.toString().contains("Enter comma separated index numbers of districts to monitor")){
                    pointer = 7;
                    to_Sava = true;
                }
                if (text.toString().contains("Filter out centers with availability less than ?")){
                    pointer = 8;
                    to_Sava = true;
                }
                if (text.toString().contains("How often do you want to refresh the calendar (in seconds)? Default 15. Minimum 5")){
                    pointer =9;
                    to_Sava = true;
                }

                if (text.toString().contains("Use 1 for today, 2 for tomorrow, or provide a date in the format DD-MM-YYYY. Default 2:")){
                    pointer =10;
                    to_Sava = true;
                }
                if (text.toString().contains("Enter 0 for No Preference, 1 for Free Only, or 2 for Paid Only. Default 0")){
                    pointer =11;
                    to_Sava = true;
                }
                if (text.toString().contains("Do you want to enable auto-booking? (yes-please or no) Default no")){
                    pointer =12;
                    to_Sava = true;
                }


            }











            /*while (true){
                if (text.toString().contains("Enter OTP")){

                    FileInputStream fis = null;
                    FileOutputStream fos = null;
                    try {
                        fis = openFileInput("OTP");
                        InputStreamReader isr = new InputStreamReader(fis);
                        BufferedReader br = new BufferedReader(isr);
                        StringBuilder sb = new StringBuilder();
                        String otpText;
                        while ((otpText = br.readLine()) != null) {
                            sb.append(otpText).append("\n");
                        }
                        Otptext = otpText;
                        fos = openFileOutput("OTP", MODE_PRIVATE);
                        fos.close();



                        //  mEditText.setText(sb.toString());
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        if (fis != null) {
                            try {
                                fis.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    if (!Otptext.isEmpty() && Otptext.contains("Otp =")){
                        String val="";
                        // REGEX




                    }




                }
            }*/






        }

        Editable scrollback = (Editable) tvOutput.getText();
        if (scrollback.length() > MAX_SCROLLBACK_LEN) {
            scrollback.delete(0, MAX_SCROLLBACK_LEN / 10);
        }

        // Changes to the TextView height won't be reflected by getHeight until after the
        // next layout pass, so isScrolledToBottom is safe here.
        if (isScrolledToBottom()) {
            scrollTo(Scroll.BOTTOM);
        }
    }

    // Don't actually scroll until the next onGlobalLayout, when we'll know what the new TextView
    // height is.
    private void scrollTo(Scroll request) {
        // The "top" button should take priority over an auto-scroll.
        if (scrollRequest != Scroll.TOP) {
            scrollRequest = request;
            svOutput.requestLayout();
        }
    }

    // Because we've set textIsSelectable, the TextView will create an invisible cursor (i.e. a
    // zero-length selection) during startup, and re-create it if necessary whenever the user taps
    // on the view. When a TextView is focused and it has a cursor, it will adjust its containing
    // ScrollView whenever the text changes in an attempt to keep the cursor on-screen.
    // textIsSelectable implies focusable, so if there are no other focusable views in the layout,
    // then it will always be focused.
    //
    // To avoid interference from this, we'll remove any cursor before we adjust the scroll.
    // A non-zero-length selection is left untouched and may affect the scroll in the normal way,
    // which is fine because it'll only exist if the user deliberately created it.
    private void removeCursor() {
        Spannable text = (Spannable) tvOutput.getText();
        int selStart = Selection.getSelectionStart(text);
        int selEnd = Selection.getSelectionEnd(text);

        // When textIsSelectable is set, the buffer type after onRestoreInstanceState is always
        // Spannable, regardless of the value of bufferType. It would then become Editable (and
        // have a cursor added), during the first call to append(). Make that happen now so we can
        // remove the cursor before append() is called.
        if (!(text instanceof Editable)) {
            tvOutput.setText(text, TextView.BufferType.EDITABLE);
            text = (Editable) tvOutput.getText();

            // setText removes any existing selection, at least on API level 26.
            if (selStart >= 0) {
                Selection.setSelection(text, selStart, selEnd);
            }
        }

        if (selStart >= 0 && selStart == selEnd) {
            Selection.removeSelection(text);
        }

    }

    public int resId(String type, String name) {
        return Utils.resId(this, type, name);
    }

    // =============================================================================================

    public static abstract class Task extends AndroidViewModel {

        private Thread.State state = Thread.State.NEW;

        public void start() {
            new Thread(() -> {
                try {
                    Task.this.run();
                    output(spanColor("[Finished]", resId("color", "console_meta")));
                } finally {
                    inputEnabled.postValue(false);
                    state = Thread.State.TERMINATED;
                }
            }).start();
            state = Thread.State.RUNNABLE;
        }

        public Thread.State getState() { return state; }

        public MutableLiveData<Boolean> inputEnabled = new MutableLiveData<>();
        public BufferedLiveEvent<CharSequence> output = new BufferedLiveEvent<>();

        public Task(Application app) {
            super(app);
            inputEnabled.setValue(false);
        }

        /** Override this method to provide the task's implementation. It will be called on a
         *  background thread. */
        public abstract void run();

        /** Called on the UI thread each time the user enters some input, A trailing newline is
         * always included. The base class implementation does nothing. */
        public void onInput(String text) {}

        public void output(final CharSequence text) {
            if (text.length() == 0) return;
            output.postValue(text);
        }

        public void outputError(CharSequence text) {
            output(spanColor(text, resId("color", "console_error")));
        }

        public Spannable spanColor(CharSequence text, int colorId) {
            int color = ContextCompat.getColor(this.getApplication(), colorId);
            return span(text, new ForegroundColorSpan(color));
        }

        public int resId(String type, String name) {
            return Utils.resId(getApplication(), type, name);
        }
    }

    public static void pressEnter(EditText editText){
        BaseInputConnection inputConnection = new BaseInputConnection(editText, true);
        inputConnection.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER));
        inputConnection.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER));
    }


    BroadcastReceiver broadcastReceiver =  new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            Bundle b = intent.getExtras();

            String message = b.getString("message");

            Log.e("Otp", "= " + message);
            if (enterOTP){
                etInput.setText(message);
                BaseInputConnection inputConnection = new BaseInputConnection(etInput, true);
                inputConnection.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER));
                inputConnection.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER));
                enterOTP = false;
            }

            else {
                enterOTP = false;
            }

        }
    };





    BroadcastReceiver broadcastReceiver_data =  new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            Bundle b = intent.getExtras();
            Log.d("Otp","HEREEEEE");
            String[] message = b.getStringArray("DATA_MESS");

            Log.e("Otp", "= " + message[0]+"  "+message[2]);


        }
    };


    public dataModel readData(){
        File file  = new File(getFilesDir(),"RUNNING.txt");
        FileInputStream fis = null;
        dataModel dm = null;
        boolean read = false;
        ArrayList<dataModel> dataModelArrayList = new ArrayList<>();

        if (file.exists()){


            try {
                String[] data = new String[13];


                fis = openFileInput("RUNNING.txt");
                InputStreamReader isr = new InputStreamReader(fis);
                BufferedReader br = new BufferedReader(isr);
                StringBuilder sb = new StringBuilder();
                String text;
                int c = 0;
                while ((text = br.readLine()) != null) {


                    StringTokenizer st = new StringTokenizer(text,"%");
                    while (st.hasMoreTokens()){
                        String s = st.nextToken();
                        data[c] = s;
                        Log.d("Tomken", "Token: "+s);
                        c++;
                    }
                    c=0;
                    dm= new dataModel(data[0],data[1],data[2],data[3],data[4],data[5],data[6],data[7],data[8],data[9],data[10],data[11],data[12]);
                    read = true;
                    readable = true;



                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (fis != null) {
                    try {
                        fis.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }


            if (read == true){
                if (file.exists()){
                    deleteFile("RUNNING.txt");
                    Log.d("Test", "readData: Running Deleted");
                }
                else{

                    Log.d("Test", "readData: File not found");
                }
            }

        }


        return dm;
    }






    public void saveToAllConfigs(String x){

//String name, String benificiary, String search, String number, String vacType, String pincodes, String state, String district, String availibility, String refresh, String startDay, String cost, String auto_book
        String text = x+"<";
        FileOutputStream fos = null;
        try {
            fos = openFileOutput("ALLCONFIGS.txt", MODE_APPEND);
            fos.write(text.getBytes());
            Log.d("TAG", "saveToAllConfigs: ="+x);



        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.d("DATA", "runner: "+e.toString());

        } catch (IOException e) {
            e.printStackTrace();
            Log.d("DATA", "addToNewRun12333: "+e.toString());
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    Log.d("DATA", "addToNewRun: "+e.toString());
                    e.printStackTrace();
                }
            }
        }

    }



    public void startRefresh(EditText editText){
        BaseInputConnection inputConnection = new BaseInputConnection(editText, true);
        inputConnection.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_CTRL_RIGHT));
        inputConnection.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_C));
        inputConnection.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER));
        inputConnection.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER));
        inputConnection.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_CTRL_LEFT));
        inputConnection.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_C));
    }


}
