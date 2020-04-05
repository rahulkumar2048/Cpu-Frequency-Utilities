//package com.performancetweaker.app.utils;
//
//import android.content.Context;
//import android.os.Build;
//
//import com.asksven.android.common.kernelutils.AlarmsDumpsys;
//import com.asksven.android.common.kernelutils.Wakelocks;
//import com.asksven.android.common.kernelutils.WakeupSources;
//import com.asksven.android.common.privateapiproxies.Alarm;
//import com.asksven.android.common.privateapiproxies.BatteryStatsProxy;
//import com.asksven.android.common.privateapiproxies.BatteryStatsTypes;
//import com.asksven.android.common.privateapiproxies.BatteryStatsTypesLolipop;
//import com.asksven.android.common.privateapiproxies.NativeKernelWakelock;
//import com.asksven.android.common.privateapiproxies.StatElement;
//import com.asksven.android.common.privateapiproxies.Wakelock;
//import com.stericson.RootTools.RootTools;
//
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.Comparator;
//
//public class BatteryStatsUtils {
//
//    /*
//     * This Singleton class acts as a bridge between Android-Common Library and the rest
//	 * of the Application
//	 */
//
//    private static BatteryStatsUtils batteryStatsUtils = null;
//    private static Context context;
//
//    private BatteryStatsUtils() {
//    }
//
//    public static BatteryStatsUtils getInstance(Context ctx) {
//        if (batteryStatsUtils == null) {
//            context = ctx;
//            batteryStatsUtils = new BatteryStatsUtils();
//            return batteryStatsUtils;
//        } else {
//            return batteryStatsUtils;
//        }
//    }
//
//    public ArrayList<NativeKernelWakelock> getNativeKernelWakelocks(boolean filterZeroValues) {
//
//        ArrayList<NativeKernelWakelock> nativeKernelWakelocks = new ArrayList<>();
//        ArrayList<StatElement> kernelWakelocks;
//        if (Wakelocks.fileExists()) {
//            kernelWakelocks = Wakelocks.parseProcWakelocks(context);
//        } else {
//            kernelWakelocks = WakeupSources.parseWakeupSources(context);
//        }
//
//        for (StatElement statElement : kernelWakelocks) {
//            NativeKernelWakelock wakelock = (NativeKernelWakelock) statElement;
//
//            if (filterZeroValues) {
//                if (wakelock.getDuration() / 1000 > 0) {
//                    nativeKernelWakelocks.add(wakelock);
//                }
//            } else {
//                nativeKernelWakelocks.add(wakelock);
//            }
//        }
//        /*
//         * sort the data on the basis of duration
//		 */
//        Comparator<NativeKernelWakelock> timeComparator = new NativeKernelWakelock.TimeComparator();
//        Collections.sort(nativeKernelWakelocks, timeComparator);
//        return nativeKernelWakelocks;
//    }
//
//    public ArrayList<Wakelock> getCpuWakelocksStats(boolean filterZeroValues) {
//        ArrayList<Wakelock> myWakelocks = new ArrayList<>();
//        ArrayList<StatElement> cpuWakelocks = new ArrayList<>();
//
//        BatteryStatsProxy stats = BatteryStatsProxy.getInstance(context);
//
//        int statsType = 0;
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            statsType = BatteryStatsTypesLolipop.STATS_CURRENT;
//        } else {
//            statsType = BatteryStatsTypes.STATS_CURRENT;
//        }
//
//        try {
//            cpuWakelocks = stats.getWakelockStats(context, BatteryStatsTypes.WAKE_TYPE_PARTIAL,
//                    statsType, 0);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        for (int i = 0; i < cpuWakelocks.size(); i++) {
//            Wakelock wl = (Wakelock) cpuWakelocks.get(i);
//            if (filterZeroValues) {
//                if ((wl.getDuration() / 1000) > 0) {
//                    myWakelocks.add(wl);
//                }
//            } else {
//                myWakelocks.add(wl);
//            }
//        }
//
//
//		/*
//         * Sort the data
//		 */
//        Comparator<Wakelock> comparator = new Wakelock.WakelockTimeComparator();
//        Collections.sort(myWakelocks, comparator);
//
//        return myWakelocks;
//    }
//
//    public ArrayList<Alarm> getAlarmStats() {
//        ArrayList<Alarm> myWakelocks = new ArrayList<>();
//        ArrayList<StatElement> alarms;
//        if (RootTools.isAccessGiven()) {
//            alarms = AlarmsDumpsys.getAlarms(true);
//        } else {
//            return myWakelocks;
//        }
//
//        for (StatElement statElement : alarms) {
//
//            Alarm alarm = (Alarm) statElement;
//            //alarm.getMaxValue();
//            if (alarm.getWakeups() > 0) myWakelocks.add(alarm);
//        }
//        Collections.sort(myWakelocks);
//
//        return myWakelocks;
//    }
//}
