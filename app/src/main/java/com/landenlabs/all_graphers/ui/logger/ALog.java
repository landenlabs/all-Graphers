/*
 * IBM Confidential
 * Copyright IBM Corp. 2016, 2021. Copyright WSI Corporation 1998, 2015
 */

package com.landenlabs.all_graphers.ui.logger;

import static android.content.Context.ACTIVITY_SERVICE;

import static com.landenlabs.all_graphers.ui.logger.ALogUtils.getString;

import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.TrafficStats;
import android.os.Build;
import android.os.Debug;
import android.os.Handler;
import android.os.Looper;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


import com.landenlabs.all_graphers.BuildConfig;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;


/**
 * Log wrapper for efficient logging.
 * <p>
 * Most commonly used <b>args</b> can be one or more objects
 * which will be convert to a string if log level is active.  If <b>object</b> is
 * provided for the tag it will be uses its simple class name in the log tag field.
 * <p>
 * Built-in joining of object strings or formatting delayed until logging is required.
 * Avoid pre-joining strings, such as:
 * <pre><font color="#006000">   ALog.d.tagMsg(this, " var1=" + var1 + " var2=" + var2);
 * </font></pre>
 * Instead, let ALog do the joining to avoid overhead when logging is disabled.
 * <pre><font color="#006000">   ALog.d.tagMsg(this, " var1=", var1, " var2=", var2);
 * </font></pre>
 * <p>
 * Primary methods:
 * <ul>
 *     <li>tagMsg(Object tag, Object... args)</li>
 *     <li>tagMsgStack(Object tag, Object... args )</li>
 *     <li>tagFmt(Object tag, String fmt, Object... args)</li>
 * </ul>
 * <p>
 * Slower methods will automatically generate TAG from stack trace.
 * <ul>
 *     <li>tag(String tag)</li>
 *     <li>self()</li>
 *     <li>msg(String msg)</li>
 *     <li>msg(String msg, Throwable tr)</li>
 *     <li>fmt(String fmt, Object... args)</li>
 *     <li>cat(String separator, Object... args)</li>
 *     <li>tr(Throwable tr)</li>
 * </ul>
 * <p>
 * <b>Examples:</b>
 * <br><pre><font color="#006000">
 *    // Optimized methods, caller provides TAG.
 *    ALog.d.tagMsg(this, "log this message");
 *    ALog.d.tagMsg(this, "log this message with exception", ex);
 *    ALog.d.tagMsg(this, "Data", badData, " should be", goodData);
 *    ALog.d.tagFmt(TAG, "First:%s Last:%s", firstName, lastName);
 * </font><font color="#a06000">
 *    // Slower calls will generate TAG from stack trace.
 *    ALog.d.msg("log this message");
 *    ALog.d.msg("log this message with exception", ex);
 *    ALog.d.fmt("First:%s Last:%s", firstName, lastName);
 * </font>
 *    // Cascaded usage:
 *    ALog.e.tag("MyFooClass").fmt("First:%s Last:%s", mFirst, mLast);
 *
 *    // Redirect to file.
 *    ALog.i.out(ALogFileWriter.Default).tag("FooBar").cat(" ", item.name, item.desc, item.tag);
 *
 * </pre>
 *
 */

@SuppressWarnings({"WeakerAccess", "unused"})
public enum ALog {

    // Logging levels (2=V, 3=D, 4=I, 5=W 6=E 7=A)
    // ==== Log levels to system log file.

    none(ALog.VERBOSE - 1),   // 1- Lowest level which cannot be sent.
    v(ALog.VERBOSE),                // 2 - Verbose
    d(ALog.DEBUG),                  // 3 - Debug
    i(ALog.INFO),                   // 4 - Info
    w(ALog.WARN),                   // 5 - Warning
    e(ALog.ERROR),                  // 6 - Error
    a(ALog.ASSERT),                 // 7 - Assert

    // Log levels to private log file.
    fv(ALog.VERBOSE, ALogFileWriter.Default),
    fd(ALog.DEBUG, ALogFileWriter.Default),
    fi(ALog.INFO, ALogFileWriter.Default),
    fw(ALog.WARN, ALogFileWriter.Default),
    fe(ALog.ERROR, ALogFileWriter.Default),
    fa(ALog.ASSERT, ALogFileWriter.Default),
    ;

    /**
     * Log priorite levels.
     */
    public static final int VERBOSE = Log.VERBOSE;
    public static final int DEBUG = Log.DEBUG;
    public static final int INFO = Log.INFO;
    public static final int WARN = Log.WARN;
    public static final int ERROR = Log.ERROR;
    public static final int ASSERT = Log.ASSERT;
    public static final int NOLOGGING = Log.ASSERT + 1;

    /**
     * User can use adb setprop to override logging state for a Release build prior to startup.
     * <p>
     *  Enable logging:
     *      adb shell setprop log.tag.TWCatak DEBUG
     *  Disable logging:
     *      adb shell setprop log.tag.TWCatak ASSERT
     *
     *  Global:
     *      adb shell setprop persist.log.tag V
     */
    public static final String PROPERTY_TAG = "TWCatak";
    public static final int PROPERTY_LEVEL = Log.DEBUG;

    /**
     * Global  Minimum priority level to log, defaults to NOLOGGING.
     */
    public static int MIN_LEVEL = NOLOGGING;
    public static String TAG_PREFIX = "TWCatak";

    private final int mLevel;
    private final ALogOut mOut = new ALogOut();
    private static final ThreadLocal<String> THREAD_TAG = new ThreadLocal<>();

    // Helper to make Log tag from stack, provide class and line number.
    private static final String NAME = ALog.class.getCanonicalName();

    public static long errorCnt = 0;

    // Filter out private information before logging. 
    interface Filter {
        String doFilter(String rawStr);
    }
    public final Filter filter = str -> str.replaceAll("[A-Za-z]*[Kk]ey=[^&,]+", "");

    // ---------------------------------------------------------------------------------------------

    ALog(int level) {
        mLevel = level;
    }

    /**
     * Provide custom Log Output stream.
     *
     * @param level  to log (2=V, 3=D, 4=I, 5=W 6=E 7=A)
     * @param logPrn custom output stream
     *
     * @see ALogOut
     */
    ALog(int level, ALogOut.LogPrinter logPrn) {
        mLevel = level;
        mOut.outPrn = logPrn;
    }
    
    /**
     *   <h2>Use adb to optionally override logging in release build.</h2>
     *
     *   <li>Auto app enable Logging
     *   <pre>adb shell setprop  log.tag.Auto DEBUG  </pre>
     *   <br>Auto app disable Logging
     *   <pre>adb shell setprop  log.tag.Auto SILENT  </pre>
     *   <p/>
     *
     *  <h2>Use adb to enable Test Data server:</h2>
     *  <li>Enable Test Data server - requires Big-IP Vpn and emulator
     *  <pre>   adb shell setprop  log.tag.WXTEST DEBUG </pre>
     *  <li>Disable Test Data server
     *  <pre>   adb shell setprop  log.tag.WXTEST INFO  </pre>
     *
     *
     * See Proguard for other tricks:
     * https://source.android.com/docs/core/tests/debug/understanding-logging#log-standards
     * <pre>
     * -assumenosideeffects class android.util.Log {
     *   public static *** v(...);
     *   public static *** d(...);
     *   public static *** i(...);
     *   public static *** w(...);
     *   public static *** e(...);
     * }
     * -maximumremovedandroidloglevel 5  # V=2,D=3, I=4, W=5, E=6, A=7
     * </pre>
     */
    public static void init(@Nullable Context ignored, @NonNull String msg) {
        ALog.MIN_LEVEL = isDebugApp()  ? ALog.DEBUG : ALog.WARN; // was ALog.NOLOGGING;
        logPrintLn( "ALog Init Type="
                + BuildConfig.BUILD_TYPE
                + (isDebugApp()?" DebugApp":"")
                + " Log=" + MIN_LEVEL
                + " API="+Build.VERSION.SDK_INT
                + " DEV="+Build.DEVICE
                + " With="
                + " " + msg);
        // ALogFileWriter.init(context);
    }

    private static boolean DEBUG_APP = false;
    public static boolean isDebugApp() {
        DEBUG_APP = BuildConfig.DEBUG;
        try {
            DEBUG_APP |= Log.isLoggable(ALog.PROPERTY_TAG, ALog.PROPERTY_LEVEL);
        } catch (Exception ignore) { }
        return DEBUG_APP;
    }

    // ---------------------------------------------------------------------------------------------
    // Common API for logging messages.

    /**
     * If valid log level, Print tag and msg.
     *
     * @param tagObj Tag to print to log output target.
     * @param msgStr Message to print to log output target.
     */
    public void tagMsg(Object tagObj, String msgStr) {
        if (mLevel >= MIN_LEVEL) {
            println(tagStr(tagObj), filter.doFilter(msgStr));
        }
    }

    /**
     * If valid log level, Print tag with args joined together
     *
     * @param tagObj Present as tag
     * @param args   If valid level, print all args.
     */
    public void tagMsg(Object tagObj, Object... args) {
        if (mLevel >= MIN_LEVEL) {
            String msgStr = join("", 0, args, null);
            println(tagStr(tagObj), filter.doFilter(msgStr));
        }
    }

    /**
     * If valid log level, Print tag and msg.
     *
     * @param tagObj Tag to print to log output target.
     * @param msgStr Message to print to log output target.
     * @param tr     Trowable stack trace added to output target.
     */
    public void tagMsg(Object tagObj, String msgStr, Throwable tr) {
        if (mLevel >= MIN_LEVEL) {
            if (isUnitTest()) {
                println(tagStr(tagObj), msgStr);
            } else {
                String msg = filter.doFilter(msgStr) + "\n" + Log.getStackTraceString(tr);
                println(tagStr(tagObj), msg);
            }
        }
    }

    /**
     * If valid log level, format message and print.
     * <p>
     * Example:
     * <br> <font color="green">
     * AppLog.LOG.d().fmt("First:%s Last:%s", firstName, lastName);
     * </font>
     * <p><font color="#ff0000">
     * Warning - Slower then tagFmt(tag, fmt, ...) because Tag generated from stack.
     * </font><p>
     *
     * @param fmt  Format used by String.format to build message to print to log output target.
     * @param args Optional arguments passed to String.format(fmt, ....)
     */
    public void tagFmt(Object tagObj, String fmt, Object... args) {
        if (mLevel >= MIN_LEVEL) {
            println(tagStr(tagObj), filter.doFilter(String.format(fmt, args)));
        }
    }

    // ---------------------------------------------------------------------------------------------
    // Specialized methods

    /**
     * Log exception and throw it.
     * maxRows = 0, show all rows.
     */
    public static void throwIt(Object tag, Error ex, int maxRows) throws Error {
        ALog.e.tagMsg(tag, getMsgStack(ex, maxRows));
        if (e.mLevel >= MIN_LEVEL) {
            throw ex;
        }
    }

    public static <T extends Exception>  void throwIt(Object tag, T ex)  throws T {
        ALog.e.tagMsg(tag, getMsgStack(ex, 0));
        if (e.mLevel >= MIN_LEVEL) {
            throw ex;
        }
    }

    /**
     * If valid log level, Print tag, msg and stack trace.
     *
     * @param tagObj Present as tag
     * @param args   If valid level, print all args.
     */
    public void tagMsgStack(Object tagObj, int maxRows, Object... args) {
        if (mLevel >= MIN_LEVEL) {
            String msgStr = join("", 0, args, null);
            println(tagStr(tagObj), filter.doFilter(msgStr) + " -stack- " + getMsgStack(null, maxRows));
        }
    }

    /**
     * Helper to return string of Throwable (exception)  message and stack trace.
     *
     * @param tr Exception to inspect or null for current stack trace.
     * @return message and stack trace
     */
    public static String getMsgStack(Throwable tr, int maxRows) {
        if (tr == null) {
            tr = new Exception("", null);
        }
        return getErrorMsg(tr) + "\n" + getStackTraceString(tr, maxRows);
    }

    /**
     * Handy function to get a loggable stack trace from a Throwable
     *
     * @param tr An exception to log
     */
    public static String getStackTraceString(Throwable tr, int maxRows) {
        if (tr == null) {
            return "";
        }

        // This is to reduce the amount of log spew that apps do in the non-error
        // condition of the network being unavailable.
        StringBuilder strackSb = new StringBuilder();
        Throwable tr2Trace = tr;
        while (tr2Trace != null) {
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try (PrintStream ps = new PrintStream(baos, true)) {
            	tr2Trace.printStackTrace(ps);
            }
            String stackTrace = baos.toString();
            int nthPos;
            if (maxRows > 0 && (nthPos = indexOf(stackTrace, "\n", maxRows)) > 0) {
                stackTrace = stackTrace.substring(0, nthPos);
            }
            strackSb.append(stackTrace).append("\n\n");
            if (tr2Trace.getCause() == null || tr2Trace.getCause() == tr2Trace) {
                break;
            }
            tr2Trace = tr2Trace.getCause();
        }

        /*
            java.lang.Exception:
            	at xxxx.ALog.getMsgStack(ALog.java:339)
            	at xxxx.ALog.tagMsgStack(ALog.java:506)
            	at xxxx.ExecState.reset(ExecState.java:51)
            	at xxxx.WxManager.start(WxManager.java:140)
         */
        return strackSb.toString().replaceAll("[\t a-z.]+ALog[^\n]+\n", "");
    }

    // ---------------------------------------------------------------------------------------------
    // Output methods

    /**
     * Replace default output log stream with custom output log stream.
     * <p>
     * Example:
     * <br><font color="green">
     * ALog.i.out(ALogFileWriter.Default).tag("FooBar").cat(" ", "aaaa", "bbbbb", "ccccc");
     * </font>
     *
     * @param logPrn Output print target
     * @return ALog chained instance
     */
    public ALog out(ALogOut.LogPrinter logPrn) {
        mOut.outPrn = logPrn;
        return this;
    }
    public ALogOut.LogPrinter getOut() {
        return mOut.outPrn;
    }

    // Raw unfiltered logging.
    public static void logPrintLn(String msg) {
        try {
            Log.println(Log.ASSERT, TAG_PREFIX, msg);
        } catch (Exception ex) {
            // Unit tests don't have Log mocked, so write to console.
            System.err.println(TAG_PREFIX + " : " + msg);
        }
    }

    /**
     * Print level, tag and message to output target.
     */
    protected void println(String tag, String msg) {
        try {
            int preLen = TAG_PREFIX.length();
            int tagLen = tag.length();
            final int maxTagLen = mOut.outPrn.maxTagLen();

            // As of Nougat (7.0, api 24) the tag length must not exceed 23 characters.
            // If tag is too long, only show prefix in Tag field and present remainder
            // in message field.
            if (preLen + tagLen <= maxTagLen) {
                mOut.outPrn.println(mLevel, TAG_PREFIX + tag, msg);
            } else {
                mOut.outPrn.println(mLevel, TAG_PREFIX, tag + ": " + msg);
            }

            if (mLevel >= ERROR) {
                errorCnt++;
            }
        } catch (IllegalArgumentException ex) {
            mOut.outPrn.println(mLevel, TAG_PREFIX, ex.getMessage());
        }
    }

    // ---------------------------------------------------------------------------------------------
    // Tag manipulation

    public static String id(Object object) {
        return "@" + Integer.toHexString(System.identityHashCode(object))
                + ":" + object.getClass().getName();
    }

    public static String tagStr(Object obj) {
        String str = tagId(obj);

        if (isUnitTest()) {
            str = "Utest";  // Unit test
        } else {
            if (Looper.getMainLooper().getThread() == Thread.currentThread()) {
                // str = str + "#Tmain";
                str = "Tmain#" + str;
            } else {
                // str = str + "#T" + Thread.currentThread().getId();
                str = "T" + Thread.currentThread().getId() + "#" + str;
            }
        }

        return str;
    }

    public static String tagId(Object obj) {
        String str;
        if (obj == null) {
            str = "(null)";
        } else if (obj instanceof String) {
            str = obj.toString();
        } else {
            str = obj.getClass().getSimpleName() + "@" + Integer
                    .toHexString(System.identityHashCode(obj));
        }
        return str;
    }

    /**
     * Set tag to automatically identify self (class which is calling ALog by stack inspection).
     * <br><font color="red">
     * Warning - Stack inspection is very slow.
     * </font>
     *
     * @return ALog chained instance
     * @see #tag(String)
     */
    public ALog self() {
        if (mLevel >= MIN_LEVEL) {
            THREAD_TAG.set(null);
        }
        return this;
    }

    /**
     * Get previously set <b>tag</b> or generate a tag by inspecting the stacktrace.
     * @return User provided tag or "filename:lineNumber"
     */
    public String findTag() {
        String tag = THREAD_TAG.get();
        return (tag != null) ? tag : makeTag();
    }

    /**
     * Make a Log tag by locating class in stack trace.
     *
     * @return "filename:lineNumber"
     */
    private static String makeTag() {
        String tag = "";
        final StackTraceElement[] ste = Thread.currentThread().getStackTrace();
        for (int idx = 0; idx < ste.length; idx++) {
            StackTraceElement elem = ste[idx];
            if (elem.getMethodName().equals("makeTag") && elem.getClassName().equals(NAME)) {
                while (++idx < ste.length) {
                    elem = ste[idx];
                    if (!elem.getClassName().equals(NAME)) {
                        break;
                    }
                }
                tag = elem.getFileName() + ":" + elem.getLineNumber();
                return tag;
            }
        }
        return tag;
    }

    /**
     * Make a Log tag by locating class calling logger, then backup until found entry with
     * 'containsClass'
     *
     * @return "filename:lineNumber"
     */
    public static String makeTag(String containsClass) {
        String tag = "";
        final StackTraceElement[] ste = Thread.currentThread().getStackTrace();
        for (int idx = 0; idx < ste.length; idx++) {
            StackTraceElement elem = ste[idx];
            if (elem.getMethodName().equals("makeTag") && elem.getClassName().equals(NAME)) {
                idx++;  // skip caller
                while (++idx < ste.length) {
                    elem = ste[idx];
                    if (elem.getClassName().contains(containsClass)) {
                        break;
                    }
                }
                tag = elem.getFileName() + ":" + elem.getLineNumber();
                return tag;
            }
        }
        return tag;
    }

    // ---------------------------------------------------------------------------------------------
    // Slow methods which automatically generate TAG from stack trace.

    /**
     * Set log tag, if not set or set with empty string, ALog will auto generate a log from stack trace.
     *
     * @param tagStr Tag to use in subsequent log printing.
     * @return ALog chained instance
     * @see #self()
     */
    public ALog tag(String tagStr) {
        if (mLevel >= MIN_LEVEL) {
            THREAD_TAG.set(tagStr);
        }
        return this;
    }

    /**
     * Set log tag, if not set or set with empty string, ALog will auto generate a log from stack trace.
     *
     * @param obj Tag to use in subsequent log printing. Use object's simple class name.
     * @return ALog chained instance
     * @see #self()
     */
    public ALog tag(Object obj) {
        if (mLevel >= MIN_LEVEL) {
            THREAD_TAG.set(tagStr(obj));
        }
        return this;
    }

    /**
     * If valid log level, Print msg with any previously set tag.
     * <p><font color="#ff0000">
     * Warning - Slower then tagMsg(this, msg) because Tag generated from stack.
     * </font><p>
     *
     * @param args Message to print to log output target
     * @see #MIN_LEVEL
     */
    public void msg(Object... args) {
        if (mLevel >= MIN_LEVEL) {
            String msgStr = join("", 0, args, null);
            println(findTag(), msgStr);
        }
    }

    /**
     * If valid log level, Print msg with Throwable and any previously set tag.
     * <p><font color="#ff0000">
     * Warning - Slower then tagMsg(this, msg, tr) because Tag generated from stack.
     * </font><p>
     *
     * @param msgStr Message to print to log output target
     * @param tr     Throwable stack trace logged.
     */
    public void msg(String msgStr, Throwable tr) {
        if (mLevel >= MIN_LEVEL) {
            cat("\n", msgStr, getStackTraceString(tr, 0));
        }
    }

    /**
     * If valid log level, format message and print.
     * <p>
     * Example:
     * <br> <font color="green">
     * AppLog.LOG.d().fmt("First:%s Last:%s", firstName, lastName);
     * </font>
     * <p><font color="#ff0000">
     * Warning - Slower then tagFmt(tag, fmt, ...) because Tag generated from stack.
     * </font><p>
     *
     * @param fmt  Format used by String.format to build message to print to log output target.
     * @param args Optional arguments passed to String.format(fmt, ....)
     */
    public void fmt(String fmt, Object... args) {
        if (mLevel >= MIN_LEVEL) {
            String msgStr = String.format(fmt, args);
            println(findTag(), msgStr);
        }
    }

    /**
     * If valid log level, Concatenate strings with <b>separator</b>
     * <p>
     * Example:
     * <br><font color="green">
     * AppLog.LOG.d().cat(" to ", fromTag, toTag);
     * <br>
     * AppLog.LOG.d().cat(", ", firstName, middleName, lastName);
     * </font>
     *
     * @param separator String place between argument values.
     * @param args      One or more object to stringize.
     */
    public void cat(String separator, Object... args) {
        if (mLevel >= MIN_LEVEL) {
            String msgStr = join(separator, 0, args, null);
            println(findTag(), msgStr);
        }
    }

    /**
     * If valid log level, Log Throwable message and stacktrace.
     *
     * @param tr Throwable logged, message and stack.
     */
    public void tr(Throwable tr) {
        if (mLevel >= MIN_LEVEL) {
            cat("\n", tr.getLocalizedMessage(), getStackTraceString(tr, 0));
        }
    }

    // ---------------------------------------------------------------------------------------------
    // Utility methods.

    /**
     * Helper to format objects into strings.
     */
    @NonNull
    public CharSequence toString(@Nullable Object obj) {
        if (mLevel >= MIN_LEVEL) {
            return getErrorMsg(obj);
        } else {
            return "";
        }
    }

    public static Exception chainError(@Nullable Exception parentEx, @Nullable Exception childEx) {
        if (childEx != null) {
            if (parentEx != null) {
                childEx.addSuppressed(parentEx);
            }
            parentEx = childEx;
        }
        return childEx;
    }

    @NonNull
    public static CharSequence getErrorMsg(@Nullable Object obj) {
        if (obj instanceof Throwable) {
            Throwable tr = (Throwable) obj;

            // Get message parts.
            String name = tr.getClass().getSimpleName();
            String error = " " + tr.getLocalizedMessage();
            boolean hasCause = (tr.getCause() != null && (tr.getCause() != tr));
            String causeLbl = hasCause ? "\n Cause=" : "";

            if (isUnitTest()) {
                return tr.toString();
            } else {
                // Colorize
                SpannableStringBuilder ssb = new SpannableStringBuilder();
                SpannableString span = new SpannableString(name + error + causeLbl);
                span.setSpan(new StyleSpan(Typeface.BOLD), 0, name.length(), 0);
                if (hasCause) {
                    int start = name.length() + error.length();
                    span.setSpan(new ForegroundColorSpan(Color.RED), start, start + causeLbl.length(), 0);
                    span.setSpan(new StyleSpan(Typeface.BOLD), start, start + causeLbl.length(), 0);
                    ssb.append(span);
                    ssb.append(getErrorMsg(tr.getCause()));
                    return ssb;
                }
                return span;
            }

        } else if (obj != null) {
            return obj.toString();
        }
        return "";
    }

    public static boolean isUnitTest() {
        return (Build.DEVICE == null && Build.HARDWARE == null);
    }

    /**
     *  Return position for nth occurrence of find in str.
     */
    public static int indexOf(String str, String find, int nth) {
        int pos = str.indexOf(find);
        while (--nth > 0 && pos != -1)
            pos = str.indexOf(find, pos + 1);
        return pos;
    }

    /**
     * Returns a string containing the tokens joined by delimiters.
     *
     * @param tokens an array objects to be joined. Strings will be formed from
     *               the objects by calling object.toString().
     */
    public static String join(CharSequence delimiter, int idx, Object[] tokens, StringBuilder sb) {
        if (sb == null) {
            sb = new StringBuilder();
        }
        while (idx < tokens.length) {
            Object token = tokens[idx];
            if (idx != 0 && token != null) {
                sb.append(delimiter);
            }
            idx++;
            if (token instanceof Throwable) {
                Throwable tr = (Throwable) token;
                sb.append(getErrorMsg(token));
            } else if (token instanceof Fmt) {
                ((Fmt) token).append(delimiter, idx, tokens, sb);  // format All remainnng tokens.
                break;
            } else if (token != null) {
                sb.append(getString(null, token));
            }
        }
        return sb.toString();
    }

    public static String merge(CharSequence delimiter, Object... tokens) {
        StringBuilder sb = new StringBuilder();
        for (Object token : tokens) {
            if (sb.length() != 0 && token != null) {
                sb.append(delimiter);
            }
            if (token instanceof Throwable) {
                Throwable tr = (Throwable) token;
                sb.append(getErrorMsg(token));
            } else if (token != null) {
                sb.append(getString(null, token));
            }
        }
        return sb.toString();
    }

    public void showToastAnyThread(@NonNull Context context, @NonNull String msg, int showLength) {
        boolean isDEBUG = false;    // TODO - get compiled state
        if (isDEBUG) {
            if (Looper.getMainLooper().getThread() == Thread.currentThread()) {
                Toast.makeText(context, msg, showLength).show();
            } else {
                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(() -> Toast.makeText(context, msg, showLength).show());
            }
        }
    }

    /**
     * Include memory usage with message
     */
    public void memory(Object tagObj, Context context, Object... args) {
        if (mLevel >= MIN_LEVEL) {
            if (Build.VERSION.SDK_INT >= 23) {
                System.gc();
                ActivityManager activityManager =
                        (ActivityManager) context.getSystemService(ACTIVITY_SERVICE);
                Debug.MemoryInfo[] memInfos =
                        activityManager.getProcessMemoryInfo(new int[]{android.os.Process.myPid()});
                Debug.MemoryInfo memoryInfo = memInfos[0];

                tagMsg(tagObj,
                        join("", 0, args, null),
                        " Memory javaHeap=",
                        memoryInfo.getMemoryStat("summary.java-heap"),
                        " nativeHeap=",
                        memoryInfo.getMemoryStat("summary.native-heap"),
                        " graphics=",
                        memoryInfo.getMemoryStat("summary.graphics"),
                        " NetRcv=", TrafficStats.getUidRxBytes(android.os.Process.myUid())
                );
            }
        }
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Custom formatting TAGs uses optional following parameters.
     * <p>
     * Example:
     * ALog.tagMsg(this, " Foo=", ALog.Fmt.Id, objFoo, " helloWorld");
     */
    public enum Fmt {
        Id() {
            void append(CharSequence delimiter, int idx, Object[] tokens, StringBuilder sb) {
                // Throws exception if missing argument.
                sb.append(tagId(tokens[idx]));
                join(delimiter, idx + 1, tokens, sb);
            }
        },
        Hex() {
            void append(CharSequence delimiter, int idx, Object[] tokens, StringBuilder sb) {
                // Throws exception if missing argument.
                Object obj = tokens[idx];
                if (obj instanceof Number) {
                    Number num = (Integer) obj;
                    sb.append(String.format("%x", num.longValue()));
                    join(delimiter, idx + 1, tokens, sb);
                }
            }
        },
        ;

        void append(CharSequence delimiter, int idx, Object[] tokens, StringBuilder sb) {
            join(delimiter, idx, tokens, sb);
        }
    }
}
