package hpsaturn.pollutionreporter.models;

/**
 * Created by Antonio Vanegas @hpsaturn on 10/13/20.
 */
public class ResponseConfig extends SensorConfig {

    public String dname;

    public int stime;

    public int stype = -1;

    public float toffset = 0;

    public float altoffset = 0;

    public float sealevel = 1013.25F;

    public String ssid;

    public String apiusr;

    public String apisrv;

    public String apiuri;

    public String apiprt;

    public String ifxdb;

    public String ifxip;

    public int ifxpt;

    public String geo;

    public String lskey;

    public String vmac;

    public String vflv;

    public String vtag;

    public int vrev = 0;

    public String anaireid;

    public String hassip;

    public String hassusr;

    public String hasspsw;

    public int hasspt;

    public int deepSleep = 0;
}
