package wisetoro.com.jboxsetting;

import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class WifiUtils
{

    private static final String TAG = "WifiUtils";
    private String mApName = null;
//    private static Context mContext = null;
    private String mPassWord = null;
//    private final boolean DEBUG = true;
//    private final int SECURITY_EAP = 3;
//    private final int SECURITY_OPEN = 0;
//    private final int SECURITY_UNKNOW = 4;
//    private final int SECURITY_WEP = 2;
//    private final int SECURITY_WPA = 1;
//    private List<ScanResult> mWifiAccessPointlist = null;
    private WifiManager mWifiManager = null;

    public WifiUtils(Context appContext)
    {
        this.mWifiManager = ((WifiManager)appContext.getSystemService(Context.WIFI_SERVICE));
    }

//    static String convertToQuotedString(String paramString)
//    {
//        return "\"" + paramString + "\"";
//    }
//
//    public static String getApName()
//    {
//        return mApName;
//    }

    private WifiConfiguration getConfigBySecurityType(WifiConfiguration paramWifiConfiguration, int paramInt)
    {
        switch (paramInt)
        {
            default:
                paramWifiConfiguration.allowedKeyManagement.set(0);
                return paramWifiConfiguration;
            case 0:
                paramWifiConfiguration.allowedKeyManagement.set(0);
                return paramWifiConfiguration;
            case 1:
                paramWifiConfiguration.allowedKeyManagement.set(1);
                paramWifiConfiguration.allowedAuthAlgorithms.set(0);
                paramWifiConfiguration.preSharedKey = ("\"" + getPassWord() + "\"");
                return paramWifiConfiguration;
            case 2:
                paramWifiConfiguration.allowedKeyManagement.set(0);
                paramWifiConfiguration.allowedAuthAlgorithms.set(0);
                paramWifiConfiguration.allowedAuthAlgorithms.set(1);
                String str = getPassWord();
                int i = str.length();
                if (((i == 10) || (i == 26) || (i == 58)) && (str.matches("[0-9A-Fa-f]*")))
                {
                    paramWifiConfiguration.wepKeys[0] = str;
                    return paramWifiConfiguration;
                }
                paramWifiConfiguration.wepKeys[0] = ('"' + str + '"');
                return paramWifiConfiguration;
            case 3:
        }
        paramWifiConfiguration.allowedKeyManagement.set(2);
        paramWifiConfiguration.allowedAuthAlgorithms.set(0);
        paramWifiConfiguration.preSharedKey = ("\"" + getPassWord() + "\"");
        return paramWifiConfiguration;
    }

    public String getPassWord()
    {
        return mPassWord;
    }

    public boolean isEthConnected(Context paramContext)
    {
        NetworkInfo.State localState = ((ConnectivityManager)paramContext.getSystemService(Context.CONNECTIVITY_SERVICE)).getNetworkInfo(9).getState();
        return NetworkInfo.State.CONNECTED == localState;
    }

    public boolean isWifiConnected(Context paramContext)
    {
        NetworkInfo.State localState = ((ConnectivityManager)paramContext.getSystemService(Context.CONNECTIVITY_SERVICE)).getNetworkInfo(1).getState();
        return NetworkInfo.State.CONNECTED == localState;
    }

    static String removeDoubleQuotes(String paramString)
    {
        int i = paramString.length();
        if ((i > 1) && (paramString.charAt(0) == '"') && (paramString.charAt(i - 1) == '"'))
            paramString = paramString.substring(1, i - 1);
        return paramString;
    }

    private ArrayList<ScanResult> removeDuplicateWithOrder(ArrayList<ScanResult> paramArrayList)
    {
        HashSet localHashSet = new HashSet();
        ArrayList localArrayList = new ArrayList();
        Iterator localIterator = paramArrayList.iterator();
        while (localIterator.hasNext())
        {
            Object localObject = localIterator.next();
            if (localHashSet.add(localObject))
                localArrayList.add((ScanResult)localObject);
        }
        return localArrayList;
    }

//    public static void setApName(String paramString)
//    {
//        mApName = paramString;
//    }
//
//    public static void setPassWord(String paramString)
//    {
//        mPassWord = paramString;
//    }

    public int closeWifi()
    {
        int i = this.mWifiManager.getWifiState();
        if ((i == 3) || (i == 2))
            this.mWifiManager.setWifiEnabled(false);
        return this.mWifiManager.getWifiState();
    }

    public void connect2AccessPoint(ScanResult paramScanResult, String pwd)
    {
        mPassWord = pwd;
        mApName = paramScanResult.SSID;
        int i = getSecurityType(paramScanResult);
        List localList = this.mWifiManager.getConfiguredNetworks();
        WifiConfiguration localWifiConfiguration1 = getSavedWifiConfig(paramScanResult.SSID, localList);
        if (localWifiConfiguration1 == null)
        {
            WifiConfiguration localWifiConfiguration2 = new WifiConfiguration();
            localWifiConfiguration2.SSID = ("\"" + paramScanResult.SSID + "\"");
            WifiConfiguration localWifiConfiguration3 = getConfigBySecurityType(localWifiConfiguration2, i);
            localWifiConfiguration3.status = 2;
            int j = this.mWifiManager.addNetwork(localWifiConfiguration3);
            this.mWifiManager.enableNetwork(j, true);
            this.mWifiManager.saveConfiguration();
            return;
        }
        localWifiConfiguration1.status = 2;
        WifiConfiguration localWifiConfiguration4 = getConfigBySecurityType(localWifiConfiguration1, i);
        this.mWifiManager.enableNetwork(localWifiConfiguration4.networkId, true);
        this.mWifiManager.updateNetwork(localWifiConfiguration4);
        this.mWifiManager.saveConfiguration();
    }

    public boolean disconnect()
    {
        return this.mWifiManager.disconnect();
    }

    public WifiInfo getCurrentWifiInfo()
    {
        return this.mWifiManager.getConnectionInfo();
    }

    public WifiConfiguration getSavedWifiConfig(String paramString, List<WifiConfiguration> paramList)
    {
        Iterator localIterator = paramList.iterator();
        while (localIterator.hasNext())
        {
            WifiConfiguration localWifiConfiguration = (WifiConfiguration)localIterator.next();
            if (localWifiConfiguration.SSID.equals("\"" + paramString + "\""))
                return localWifiConfiguration;
        }
        return null;
    }

    public int getSecurityType(ScanResult paramScanResult)
    {
        if (paramScanResult.capabilities == null)
            return 0;
        if (paramScanResult.capabilities.contains("WPA"))
            return 1;
        if (paramScanResult.capabilities.contains("WEP"))
            return 2;
        if (paramScanResult.capabilities.contains("EAP"))
            return 3;
        return 4;
    }

    public List<ScanResult> getWifiAccessPointList()
    {
        ArrayList localArrayList1 = new ArrayList();
        ArrayList localArrayList2 = new ArrayList();
        localArrayList1.clear();
        localArrayList2.clear();
        Iterator localIterator = ((ArrayList)this.mWifiManager.getScanResults()).iterator();
        while (localIterator.hasNext())
        {
            ScanResult localScanResult = (ScanResult)localIterator.next();
            if ((localScanResult.SSID != null) && (localScanResult.SSID.length() != 0) && (!localScanResult.capabilities.contains("[IBSS]")))
                localArrayList2.add(localScanResult);
        }
        return removeDuplicateWithOrder(localArrayList2);
    }

    public int getWifiLevel(ScanResult paramScanResult)
    {
        return paramScanResult.level;
    }

    public int getWifiState()
    {
        return this.mWifiManager.getWifiState();
    }

    public int openWifi()
    {
        int i = this.mWifiManager.getWifiState();
        if ((i == 1) || (i == 0) || (i == 4))
            this.mWifiManager.setWifiEnabled(true);
        if (!this.mWifiManager.isWifiEnabled())
            this.mWifiManager.setWifiEnabled(true);
        return this.mWifiManager.getWifiState();
    }

    public boolean saveConfiguration()
    {
        return this.mWifiManager.saveConfiguration();
    }

    public boolean startScan()
    {
        return this.mWifiManager.startScan();
    }

    public void stopWifi(WifiConfiguration paramWifiConfiguration)
    {
        this.mWifiManager.disableNetwork(paramWifiConfiguration.networkId);
    }

    public void unSaveConfig(WifiConfiguration paramWifiConfiguration)
    {
        this.mWifiManager.removeNetwork(paramWifiConfiguration.networkId);
    }

    public class sortByLevel
            implements Comparator<ScanResult>
    {
        public sortByLevel()
        {
        }

        public int compare(ScanResult paramScanResult1, ScanResult paramScanResult2)
        {
            if (paramScanResult1.level > paramScanResult2.level)
                return 1;
            return 0;
        }
    }
}