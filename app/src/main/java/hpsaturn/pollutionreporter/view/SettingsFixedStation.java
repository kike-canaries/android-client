package hpsaturn.pollutionreporter.view;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.SwitchPreference;

import com.hpsaturn.tools.Logger;

import hpsaturn.pollutionreporter.R;
import hpsaturn.pollutionreporter.models.ApiConfig;
import hpsaturn.pollutionreporter.models.CommandConfig;
import hpsaturn.pollutionreporter.models.InfluxdbConfig;
import hpsaturn.pollutionreporter.models.ResponseConfig;
import hpsaturn.pollutionreporter.models.SampleConfig;
import hpsaturn.pollutionreporter.models.SensorConfig;
import hpsaturn.pollutionreporter.models.SensorData;
import hpsaturn.pollutionreporter.models.SensorName;
import hpsaturn.pollutionreporter.models.SensorType;
import hpsaturn.pollutionreporter.models.WifiConfig;

/**
 * Created by Antonio Vanegas @hpsaturn on 2/17/19.
 */

public class SettingsFixedStation extends SettingsBaseFragment {

    public static final String TAG = SettingsFixedStation.class.getSimpleName();

    @Override
    public void onCreatePreferencesFix(@Nullable Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.settings_fixed_station, rootKey);
    }

    public void rebuildUI(){
        getPreferenceScreen().removeAll();
        addPreferencesFromResource(R.xml.settings_fixed_station);
    }

    public void refreshUI(){
        updateSensorNameSummary();
        updateWifiSummary();
        updateApiHostSummary();
        updateApiUriSummary();
        updateInfluxDbSummmary();
        updateInfluxPortSummary();
    }

    @Override
    protected void onConfigRead(ResponseConfig config) {
        Logger.d(TAG, "[Config] onConfigCallBack");

        printResponseConfig(config);

        boolean notify_sync = false;

        if (getSensorName().length() > 0 && !getSensorName().equals(config.dname)){
            notify_sync = true;
        }
        if (config.wenb != getWifiSwitch().isChecked()) {
            notify_sync = true;
        }
        if (!config.ifxdb.equals(getInfluxDbDname())){
            notify_sync = true;
        }
        if (config.ienb != getInfluxDbSwitch().isChecked()) {
            notify_sync = true;
        }
        if (config.aenb != getApiSwitch().isChecked()) {
            notify_sync = true;
        }

        saveDeviceInfoString(config);
        setStatusSwitch(true);

        if (notify_sync) {
            saveAllPreferences(config);
            updateSwitches(config);
            rebuildUI();
            updateStatusSummary(true);
            updatePreferencesSummmary(config);
            Logger.v(TAG, "[Config] notify device sync complete");
            getMain().showSnackMessage(R.string.msg_sync_complete);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        infoPreferenceInit();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

        if (!onSensorReading) {

            if (key.equals(getString(R.string.key_setting_dname))) {
                saveSensorName(getSensorName());
            } else if (key.equals(getString(R.string.key_setting_ssid))) {
                getWifiSwitch().setEnabled(isWifiSwitchFieldsValid());
            } else if (key.equals(getString(R.string.key_setting_pass))) {
                getWifiSwitch().setEnabled(isWifiSwitchFieldsValid());
            } else if (key.equals(getString(R.string.key_setting_enable_wifi))) {
                saveWifiConfig();
            } else if (key.equals(getString(R.string.key_setting_apiusr))) {
                getApiSwitch().setEnabled(isApiSwitchFieldsValid());
            } else if (key.equals(getString(R.string.key_setting_apipss))) {
                getApiSwitch().setEnabled(isApiSwitchFieldsValid());
            } else if (key.equals(getString(R.string.key_setting_apisrv))) {
                getApiSwitch().setEnabled(isApiSwitchFieldsValid());
            } else if (key.equals(getString(R.string.key_setting_enable_api))) {
                saveApiConfig();
            } else if (key.equals(getString(R.string.key_setting_ifxdb))) {
                getInfluxDbSwitch().setEnabled(isInfluxDbSwitchFieldsValid());
            } else if (key.equals(getString(R.string.key_setting_ifxip))) {
                getInfluxDbSwitch().setEnabled(isInfluxDbSwitchFieldsValid());
            } else if (key.equals(getString(R.string.key_setting_enable_ifx))) {
                saveInfluxDbConfig();
            } else if (key.equals(getString(R.string.key_setting_vars))) {
                getMain().selectedVarsUpdated();
            }

        }
        else
            Logger.i(TAG,"skyp onSharedPreferenceChanged because is in reading mode!");

    }


    /***********************************************************************************************
     * Sensor name section
     **********************************************************************************************/

    private void saveSensorName(String name) {
        if(name.length() > 0 ) {
            saveSharedPreference(R.string.key_setting_dname,name);
            SensorName config = new SensorName();
            config.dname = name;
            sendSensorConfig(config);
        }
        updateSensorNameSummary();
    }

    String getSensorName() {
        return getSharedPreference(getString(R.string.key_setting_dname));
    }

    private void updateSensorNameSummary() {
        updateSummary(R.string.key_setting_dname);
    }



    /***********************************************************************************************
     * Wifi switch
     **********************************************************************************************/

    private void saveWifiConfig() {

        if (getWifiSwitch().isChecked() && isWifiSwitchFieldsValid()) {
            WifiConfig config = new WifiConfig();
            config.ssid = getSharedPreference(getString(R.string.key_setting_ssid));
            config.pass = getSharedPreference(getString(R.string.key_setting_pass));
            config.wenb = true;
            updateWifiSummary();
            sendSensorConfig(config);
        }
        else
            setWifiSwitch(false);
    }

    private boolean isWifiSwitchFieldsValid() {
        Logger.v(TAG, "[Config] validating->" + getString(R.string.key_setting_enable_wifi));
        String ssid = getSharedPreference(getString(R.string.key_setting_ssid));
        String pass = getSharedPreference(getString(R.string.key_setting_pass));
        Logger.v(TAG, "[Config] values -> " + ssid );
        updateWifiSummary();
        return ssid.length() != 0;
    }

    private void setWifiSwitch(boolean checked) {
        SwitchPreference wifiSwitch = getWifiSwitch();
        wifiSwitch.setEnabled(isWifiSwitchFieldsValid());
        wifiSwitch.setChecked(checked);
        updateWifiSummary();
        enableWifiOnDevice(checked);
    }

    private void updateWifiSummary(){
        updateSummary(R.string.key_setting_ssid);
        updatePasswSummary(R.string.key_setting_pass);
    }

    private SwitchPreference getWifiSwitch() {
        return findPreference(getString(R.string.key_setting_enable_wifi));
    }

    private void enableWifiOnDevice(boolean enable) {
        CommandConfig config = new CommandConfig();
        config.cmd = getSharedPreference(getString(R.string.key_setting_wmac));
        config.act = "wst";
        config.wenb = enable;
        sendSensorConfig(config);
    }

    /***********************************************************************************************
     * API switch
     **********************************************************************************************/

    private void saveApiConfig() {

        if (getApiSwitch().isChecked() && isApiSwitchFieldsValid()) {
            ApiConfig config = new ApiConfig();
            config.apiusr = getSharedPreference(getString(R.string.key_setting_apiusr));;
            config.apipss = getSharedPreference(getString(R.string.key_setting_apipss));
            config.apisrv = getSharedPreference(getString(R.string.key_setting_apisrv));
            config.apiuri = getSharedPreference(getString(R.string.key_setting_apiuri));
            config.apiprt = 80;  // TODO: sending via UI
            config.aenb = true;
            sendSensorConfig(config);
        } else
            setApiSwitch(false);
    }

    private boolean isApiSwitchFieldsValid(){
        Logger.v(TAG, "[Config] validating ->" + getString(R.string.key_setting_enable_api));
        String api_usr = getSharedPreference(getString(R.string.key_setting_apiusr));
        String api_pss = getSharedPreference(getString(R.string.key_setting_apipss));
        String api_srv = getSharedPreference(getString(R.string.key_setting_apisrv));
        Logger.v(TAG, "[Config] values -> " + api_usr +  " " + api_pss + " " + api_srv);
        apiSummaryUpdate();
        return !(api_usr.length() == 0 || api_pss.length() == 0 || api_srv.length() == 0);
    }

    private void setApiSwitch(boolean checked) {
        Logger.v(TAG, "[Config] API switch check -> " + checked);
        SwitchPreference apiSwitch = getApiSwitch();
        apiSwitch.setEnabled(isApiSwitchFieldsValid());
        apiSwitch.setChecked(checked);
        apiSummaryUpdate();
        enableApiOnDevice(checked);
    }

    private void apiSummaryUpdate(){
        updateApiUsrSummmary();
        updateApiUriSummary();
        updateApiHostSummary();
        updatePasswSummary(R.string.key_setting_apipss);
    }

    private SwitchPreference getApiSwitch() {
        return findPreference(getString(R.string.key_setting_enable_api));
    }

    private void enableApiOnDevice(boolean enable) {
        CommandConfig config = new CommandConfig();
        config.cmd = getSharedPreference(getString(R.string.key_setting_wmac));
        config.act = "ast";
        config.aenb = enable;
        sendSensorConfig(config);
    }

    /***********************************************************************************************
     * InfluxDB switch
     **********************************************************************************************/

    private void saveInfluxDbConfig() {

        if (getInfluxDbSwitch().isChecked() && isInfluxDbSwitchFieldsValid()) {
            InfluxdbConfig config = new InfluxdbConfig();
            config.ifxdb = getSharedPreference(getString(R.string.key_setting_ifxdb));
            config.ifxip = getSharedPreference(getString(R.string.key_setting_ifxip));
            config.ienb = true;
            sendSensorConfig(config);
        } else
            setInfluxDbSwitch(false);
    }

    private boolean isInfluxDbSwitchFieldsValid(){
        Logger.v(TAG, "[Config] validating->" + getString(R.string.key_setting_ifxdb));
        String ifxdb = getSharedPreference(getString(R.string.key_setting_ifxdb));
        String ifxip = getSharedPreference(getString(R.string.key_setting_ifxip));
        updateInfluxSummary();
        Logger.v(TAG, "[Config] values -> " + ifxdb +  " " + ifxip);
        return !(ifxdb.length() == 0 || ifxip.length() == 0);
    }

    private void setInfluxDbSwitch(boolean checked) {
        Logger.v(TAG, "[Config] InfluxDb switch check -> " + checked);
        SwitchPreference ifxdbSwitch = getInfluxDbSwitch();
        ifxdbSwitch.setEnabled(isInfluxDbSwitchFieldsValid());
        ifxdbSwitch.setChecked(checked);
        updateInfluxSummary();
        enableInfluxDbOnDevice(checked);
    }

    private void updateInfluxSummary(){
        updateInfluxDbSummmary();
        updateInfluxPortSummary();
    }

    private void enableInfluxDbOnDevice(boolean enable) {
        CommandConfig config = new CommandConfig();
        config.cmd = getSharedPreference(getString(R.string.key_setting_wmac));
        config.act = "ist";
        config.ienb = enable;
        sendSensorConfig(config);
    }

    private SwitchPreference getInfluxDbSwitch() {
        return findPreference(getString(R.string.key_setting_enable_ifx));
    }

    private String getInfluxDbDname() {
        return getSharedPreference(getString(R.string.key_setting_ifxdb));
    }

    /***********************************************************************************************
     * Device info
     **********************************************************************************************/

    private void infoPreferenceInit() {
        Preference infoPreference = findPreference(getString(R.string.key_device_info));

        infoPreference.setOnPreferenceClickListener(preference -> {
            readSensorConfig();
            return false;
        });
    }

    public void onReadSensorData(SensorData data) {
        String info = getSharedPreference(getString(R.string.key_device_info));
        info = info + "\n-----------------------"
                +"\n"+data.dsl+"->PM2.5:"+data.P25+" PM1:"+data.P1+" CO2:"+data.CO2
                + "\n T:"+data.tmp+" H:"+data.hum;

        updateSummary(R.string.key_device_info,info);
        saveSharedPreference(R.string.key_device_info,info);
    }

    private void saveDeviceInfoString(ResponseConfig config) {
        String info = "MAC:"
                + config.vmac
                +"\nFirmware: "+config.vflv+" rev"+config.vrev+" ("+config.vtag+")"
                +"\n[WiFi:"+(config.wenb ? "On" : "Off")+"][IFDB:"+(config.ienb ? "On" : "Off")
                +"][GW:"+(config.wsta ? "On" : "Off")+"]";
        if(config.vrev<774)info="\n!!YOUR FIRMWARE IS OUTDATED!!\n\n"+info;
        updateSummary(R.string.key_device_info,info);
        saveSharedPreference(R.string.key_device_info,info);
    }



    /***********************************************************************************************
     * Misc preferences section
     **********************************************************************************************/

    /***********************************************************************************************
     * Update methods
     **********************************************************************************************/


    private void updatePreferencesSummmary(ResponseConfig config) {
        if(config.dname !=null)updateSummary(R.string.key_setting_dname,config.dname);
        if(config.apiusr !=null)updateSummary(R.string.key_setting_apiusr,config.apiusr);
        if(config.apisrv !=null)updateSummary(R.string.key_setting_apisrv,config.apisrv);
        if(config.apiuri !=null)updateSummary(R.string.key_setting_apiuri,config.apiuri);
        if(config.ssid !=null)updateSummary(R.string.key_setting_ssid,config.ssid);
        if(config.ifxdb !=null)updateSummary(R.string.key_setting_ifxdb,config.ifxdb);
        if(config.ifxip !=null)updateSummary(R.string.key_setting_ifxip,config.ifxip);
        updatePasswSummary(R.string.key_setting_pass);
        updatePasswSummary(R.string.key_setting_apipss);

    }

    private void updatePasswSummary(int key) {
        String passw = getSharedPreference(getString(key));
        if(passw.length()>0) updateSummary(key,R.string.msg_passw_seted);
        else updateSummary(key,R.string.msg_passw_unseted);
    }

    private void updateInfluxDbSummmary() {
        updateSummary(R.string.key_setting_ifxdb);
    }

    private void updateInfluxPortSummary() {
        updateSummary(R.string.key_setting_ifxip);
    }

    private void updateApiUsrSummmary() {
        updateSummary(R.string.key_setting_apiusr);
    }

    private void updateApiHostSummary() {
        updateSummary(R.string.key_setting_apisrv);
    }

    private void updateApiUriSummary() {
        updateSummary(R.string.key_setting_apiuri);
    }

    private void updateSwitch(int key,boolean enable){
        SwitchPreference mSwitch = findPreference(getString(key));
        mSwitch.setChecked(enable);
    }

    private void updateSwitches(SensorConfig config){
        updateSwitch(R.string.key_setting_enable_wifi,config.wenb);
        updateSwitch(R.string.key_setting_enable_ifx,config.ienb);
        updateSwitch(R.string.key_setting_enable_api,config.aenb);
    }

    /***********************************************************************************************
     * Update Preferences methods
     **********************************************************************************************/

    private void saveAllPreferences(ResponseConfig config) {
        saveSharedPreference(R.string.key_setting_dname, config.dname);
        saveSharedPreference(R.string.key_setting_ssid, config.ssid);
        saveSharedPreference(R.string.key_setting_ifxdb, config.ifxdb);
        saveSharedPreference(R.string.key_setting_ifxip, config.ifxip);
        saveSharedPreference(R.string.key_setting_apiusr, config.apiusr);
        saveSharedPreference(R.string.key_setting_apisrv, config.apisrv);
        saveSharedPreference(R.string.key_setting_wmac, "" + config.wmac);
        saveDeviceInfoString(config);
    }



}
