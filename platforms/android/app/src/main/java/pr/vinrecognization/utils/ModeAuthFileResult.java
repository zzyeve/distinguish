package pr.vinrecognization.utils;

import android.os.Build;
import android.util.Log;

import com.kernal.lisence.Common;
import com.kernal.lisence.VersionAuthFileOperate;
import com.wintone.cipher.Base64;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * Software:
 * Version: 1.0.0
 * Company: eastime
 *
 * @author LMM
 * @time:2021/1/18 17
 */
public class ModeAuthFileResult  {
    public String devcode = "";
    public String androidPlatform;
    public String[] product_type = new String[]{"", "", "", "", "", "", "", "", "", ""};
    public String[] authtype_switch = new String[]{"", "", "", "", "", "", "", "", "", ""};
    public String[] authtype_type = new String[]{"", "", "", "", "", "", "", "", "", ""};
    public String[] devtype_switch = new String[]{"", "", "", "", "", "", "", "", "", ""};
    public String[] devtype_type = new String[]{"", "", "", "", "", "", "", "", "", ""};
    public String[] tfmode_switch = new String[]{"", "", "", "", "", "", "", "", "", ""};
    public String[] mnomode_switch = new String[]{"", "", "", "", "", "", "", "", "", ""};
    public String[] mnomode_sim = new String[]{"", "", "", "", "", "", "", "", "", ""};
    public String[] mnomode_deviceid = new String[]{"", "", "", "", "", "", "", "", "", ""};
    public String[] prjmode_switch = new String[]{"", "", "", "", "", "", "", "", "", ""};
    public String[] prjmode_packagename = new String[]{"", "", "", "", "", "", "", "", "", ""};
    public String[] prjmode_startdate = new String[]{"", "", "", "", "", "", "", "", "", ""};
    public String[] prjmode_closingdate = new String[]{"", "", "", "", "", "", "", "", "", ""};
    public String[] prjmode_version = new String[]{"", "", "", "", "", "", "", "", "", ""};
    public String[] prjmode_app_name = new String[]{"", "", "", "", "", "", "", "", "", ""};
    public String[] prjmode_company_name = new String[]{"", "", "", "", "", "", "", "", "", ""};
    public String prjmode_templatetype = "";
    public String ExpiratioTime;
    public List<String> prjmode_company_names = new ArrayList();
    public List<String> prjmode_app_names = new ArrayList();
    public List<String> prjmode_packagenames = new ArrayList();
    private Base64 base64 = new Base64();

    public ModeAuthFileResult() {
    }

    public void print() {
        System.out.println("devcode=" + this.devcode);
        System.out.println("androidPlatform=" + this.androidPlatform);

        int i;
        for(i = 0; i < 3; ++i) {
            System.out.println("product_type[" + i + "]=" + this.product_type[i]);
        }

        for(i = 0; i < 3; ++i) {
            System.out.println("authtype_switch[" + i + "]=" + this.authtype_switch[i]);
        }

        for(i = 0; i < 3; ++i) {
            System.out.println("authtype_type[" + i + "]=" + this.authtype_type[i]);
        }

        for(i = 0; i < 3; ++i) {
            System.out.println("devtype_switch[" + i + "]=" + this.devtype_switch[i]);
        }

        for(i = 0; i < 3; ++i) {
            System.out.println("devtype_type[" + i + "]=" + this.devtype_type[i]);
        }

        for(i = 0; i < 3; ++i) {
            System.out.println("tfmode_switch[" + i + "]=" + this.tfmode_switch[i]);
        }

        for(i = 0; i < 3; ++i) {
            System.out.println("mnomode_switch[" + i + "]=" + this.mnomode_switch[i]);
        }

        for(i = 0; i < 3; ++i) {
            System.out.println("mnomode_deviceid[" + i + "]=" + this.mnomode_deviceid[i]);
        }

        for(i = 0; i < 3; ++i) {
            System.out.println("mnomode_sim[" + i + "]=" + this.mnomode_sim[i]);
        }

        for(i = 0; i < 3; ++i) {
            System.out.println("prjmode_switch[" + i + "]=" + this.prjmode_switch[i]);
        }

        for(i = 0; i < 3; ++i) {
            System.out.println("prjmode_packagename[" + i + "]=" + this.prjmode_packagename[i]);
        }

        for(i = 0; i < 3; ++i) {
            System.out.println("prjmode_closingdate[" + i + "]=" + this.prjmode_closingdate[i]);
        }

        for(i = 0; i < 3; ++i) {
            System.out.println("prjmode_version[" + i + "]=" + this.prjmode_version[i]);
        }

        for(i = 0; i < 3; ++i) {
            System.out.println("prjmode_app_name[" + i + "]=" + this.prjmode_app_name[i]);
        }

        for(i = 0; i < 3; ++i) {
            System.out.println("prjmode_company_name[" + i + "]=" + this.prjmode_company_name[i]);
        }

    }

    public boolean isTF(String type) {
        boolean is = false;

        for(int i = 0; i < this.product_type.length; ++i) {
            if (this.product_type[i].equals(type)) {
                String tf = this.tfmode_switch[i];
                if (tf != null && !tf.equals("") && tf.equals("on")) {
                    is = true;
                    break;
                }
            }
        }

        return is;
    }

    public boolean isCheckDevType(String type) {
        boolean is = false;

        for(int i = 0; i < this.product_type.length; ++i) {
            if (this.product_type[i].equals(type)) {
                String devtype = this.devtype_switch[i];
                if (devtype != null && !devtype.equals("") && devtype.equals("on")) {
                    is = true;
                    break;
                }
            }
        }

        return is;
    }

    public int isDevcodeOK(String type, String Devcode) {
        int is = -10401;
        if (this.devcode != null && !this.devcode.equals("") && this.devcode.equals(Devcode)) {
            is = 0;
        }

        return is;
    }

    public String[] allowDevType(String type) {
        for(int i = 0; i < this.product_type.length; ++i) {
            if (this.product_type[i].equals(type)) {
                String devtype = this.devtype_type[i];
                return devtype.split(";");
            }
        }

        return null;
    }

    public int isAllowDevTypeAndDevCode(String type, String Devcode) {
        int is = -10402;

        try {
            is = this.isDevcodeOK(type, Devcode);
            if (is == 0) {
                is = -10402;
                String device_model = Build.MODEL;
                System.out.println("device_model=" + device_model);
                String[] strarr = this.allowDevType(type);
                if (strarr != null) {
                    System.out.println("strarr[0]=" + strarr[0]);
                }

                if (strarr == null) {
                    return is;
                }

                for(int i = 0; i < strarr.length; ++i) {
                    if (strarr[i].equals(device_model)) {
                        is = 0;
                        break;
                    }
                }
            }
        } catch (Exception var7) {
            var7.printStackTrace();
        }

        return is;
    }

    public boolean isSIM(String type) {
        boolean is = false;

        for(int i = 0; i < this.product_type.length; ++i) {
            if (this.product_type[i].equals(type)) {
                String mnomode = this.mnomode_switch[i];
                if (mnomode != null && !mnomode.equals("") && mnomode.equals("on")) {
                    String strmnomode_sim = this.mnomode_sim[i];
                    if (strmnomode_sim != null && !strmnomode_sim.equals("") && strmnomode_sim.equals("on")) {
                        is = true;
                        break;
                    }
                }
            }
        }

        return is;
    }

    public boolean isCheckPRJMode(String type) {
        boolean is = false;

        for(int i = 0; i < this.product_type.length; ++i) {
            if (this.product_type[i].equals(type)) {
                String prjmode = this.prjmode_switch[i];
                if (prjmode != null && !prjmode.equals("") && prjmode.equals("on")) {
                    is = true;
                    return is;
                }
            }
        }

        return is;
    }

    public int isCheckPRJOK(String productType, String devCode, String company_name) {
        int is = -10600;
        new Common();
        int index = -1;

        for(int i = 0; i < this.product_type.length; ++i) {
            if (this.product_type[i].equals(productType)) {
                index = i;
            }
        }

//        short is;
        if (this.devcode.equals(devCode)) {
            boolean packagenameIsNull = false;
            if (this.prjmode_packagename[index].equals("")) {
                packagenameIsNull = true;
            }

            this.prjmode_packagenames.clear();

            for(int i = 0; i < this.prjmode_packagename[index].split("#").length; ++i) {
                this.prjmode_packagenames.add(this.prjmode_packagename[index].split("#")[i]);
            }

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                String date = sdf.format(new Date());
                GregorianCalendar cal = new GregorianCalendar();
                GregorianCalendar cal1 = new GregorianCalendar();
                GregorianCalendar cal2 = new GregorianCalendar();
                if (this.prjmode_closingdate[index].equals("")) {
                    this.prjmode_closingdate[index] = "2115-01-01";
                }

                double dayCount = -30.0D;

                try {
                    if (this.prjmode_startdate[index] != null && !this.prjmode_startdate[index].equals("")) {
                        cal.setTime(sdf.parse(date));
                        cal1.setTime(sdf.parse(this.prjmode_startdate[index]));
                        cal2.setTime(sdf.parse(this.prjmode_closingdate[index]));
                        dayCount = (double)((cal2.getTimeInMillis() - cal.getTimeInMillis()) / 86400000L);
                        if (dayCount >= -30.0D) {
                            dayCount = (double)((cal2.getTimeInMillis() - cal1.getTimeInMillis()) / 86400000L);
                        }
                    } else {
                        cal.setTime(sdf.parse(date));
                        cal1.setTime(sdf.parse(date));
                        cal2.setTime(sdf.parse(this.prjmode_closingdate[index]));
                        dayCount = (double)((cal2.getTimeInMillis() - cal1.getTimeInMillis()) / 86400000L);
                    }
                } catch (ParseException var29) {
                    var29.printStackTrace();
                }

                long modifyDate = cal.getTimeInMillis() - cal1.getTimeInMillis();
                if (modifyDate >= 0L) {
                    if (dayCount >= -30.0D) {
                        VersionAuthFileOperate versionAuth = new VersionAuthFileOperate();
                        String apiVersion = versionAuth.getVersionStringByType(productType);
                        boolean versionIsNull = false;
                        if (this.prjmode_version[index].equals("")) {
                            versionIsNull = true;
                        }

                        if (!apiVersion.startsWith(this.prjmode_version[index]) && !versionIsNull) {
                            is = -10604;
                        } else {
                            boolean app_nameIsNull = false;
                            if (this.prjmode_app_name[index].equals("")) {
                                app_nameIsNull = true;
                            }

                            this.prjmode_app_names.clear();
                            String temp_prjmode_app_name = "";

                            try {
                                temp_prjmode_app_name = this.base64.decodeStrFromStr(this.prjmode_app_name[index]);
                            } catch (IOException var28) {
                                var28.printStackTrace();
                            }

                            for(int i = 0; i < temp_prjmode_app_name.split("#").length; ++i) {
                                this.prjmode_app_names.add(temp_prjmode_app_name.split("#")[i]);
                            }

//                            if (!this.prjmode_app_names.contains(app_name) && !app_nameIsNull) {
//                                is = -10605;
//                            } else {
                                boolean company_nameIsNull = false;
                                if (this.prjmode_company_name[index].equals("")) {
                                    company_nameIsNull = true;
                                }

                                this.prjmode_company_names.clear();
                                String temp_prjmode_company_name = "";

                                try {
                                    temp_prjmode_company_name = this.base64.decodeStrFromStr(this.prjmode_company_name[index]);
                                } catch (IOException var27) {
                                    var27.printStackTrace();
                                }

                                for(int i = 0; i < temp_prjmode_company_name.split("#").length; ++i) {
                                    this.prjmode_company_names.add(temp_prjmode_company_name.split("#")[i]);
                                }
                            Log.d("prjmode_company_names",prjmode_company_names+"///"+company_name);
                                if (!this.prjmode_company_names.contains(company_name) && !company_nameIsNull) {
                                    is = -10606;
                                } else {
                                    is = 0;
                                    if (dayCount < 0.0D) {
                                        this.ExpiratioTime = this.prjmode_closingdate[index];
                                        is = -10090;
                                    }
                                }
//                            }
                        }
                    } else {
                        is = -10603;
                    }
                } else {
                    is = -10607;
                }
//            }
        } else {
            is = -10601;
        }

        return is;
    }

    public int isCheckTemplatetypeOK(String templatetype) {
        int is = -1;
        if (this.prjmode_templatetype != null) {
            is = -10608;
            if (!this.prjmode_templatetype.equals("")) {
                String[] templatetypeArray = this.prjmode_templatetype.split("#");

                for(int i = 0; i < templatetypeArray.length; ++i) {
                    if (templatetype.equals(templatetypeArray[i])) {
                        is = 0;
                    }
                }
            } else {
                is = 0;
            }
        }

        return is;
    }

    public String getExpiratioTime() {
        return this.ExpiratioTime != null && !this.ExpiratioTime.equals("") && !this.ExpiratioTime.equals("null") ? this.ExpiratioTime : "";
    }

    public static void main(String[] args) {
        ModeAuthFileOperate mafo = new ModeAuthFileOperate();
        String filePath = "D:/Android/Common/type/authmode.lsc";
        ModeAuthFileResult mafr = mafo.testReadAuthFile(filePath);
        mafr.print();
        System.out.println("isTF=" + mafr.isTF("10"));
        System.out.println("isSIM=" + mafr.isSIM("10"));
        System.out.println("isCheckDevType=" + mafr.isCheckDevType("10"));
        System.out.println("isDevcodeOK=" + mafr.isDevcodeOK("10", "SBGAQC7EZAIAXRY"));
        String[] devtypes = mafr.allowDevType("10");
        if (devtypes != null) {
            for(int i = 0; i < devtypes.length; ++i) {
                System.out.println("AllowDevTypes=" + devtypes[i]);
            }
        }

        System.out.println(" - - - - - - - - - - - - - - - -");
        System.out.println("isCheckPRJMode=" + mafr.isCheckPRJMode("11"));
        System.out.println("isCheckPRJOK=" + mafr.isCheckPRJOK("10", "SBGAQC7EZAIAXRY",  "北京文通科技有限公司"));
    }
}
