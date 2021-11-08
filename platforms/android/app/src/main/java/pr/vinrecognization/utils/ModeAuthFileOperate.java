package pr.vinrecognization.utils;

import com.kernal.lisence.Common;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * Software:
 * Version: 1.0.0
 * Company: eastime
 *
 * @author LMM
 * @time:2021/1/18 18
 */
public class ModeAuthFileOperate {
    public ModeAuthFileOperate() {
    }

    public ModeAuthFileResult ReadAuthFile(String resultString) {
        ModeAuthFileResult wlci = new ModeAuthFileResult();
        Common common = new Common();
        String SysCertVersion = "wtversion5_5";

        try {
            resultString = common.getDesPassword(resultString, SysCertVersion);
        } catch (Exception var32) {
            var32.printStackTrace();
            return wlci;
        }

        DocumentBuilder builder = null;
        Document document = null;

        try {
            builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            document = builder.parse(new ByteArrayInputStream(resultString.getBytes("UTF-8")));
        } catch (ParserConfigurationException var28) {
            var28.printStackTrace();
            return wlci;
        } catch (IOException var29) {
            var29.printStackTrace();
            return wlci;
        } catch (SAXException var30) {
            var30.printStackTrace();
            return wlci;
        } catch (Exception var31) {
            var31.printStackTrace();
            return wlci;
        }

        try {
            Element element = document.getDocumentElement();
            NodeList PlatformNodeList = element.getElementsByTagName("Platform");
            Node PlatformNode = PlatformNodeList.item(0);
            Node DevcodeNode;
            if (PlatformNode.getNodeName().equals("Platform")) {
                NamedNodeMap nnMap = PlatformNode.getAttributes();
                DevcodeNode = nnMap.getNamedItem("Android");
                wlci.androidPlatform = DevcodeNode.getNodeValue();
            }

            NodeList DevcodeNodeList = element.getElementsByTagName("Devcode");
            DevcodeNode = DevcodeNodeList.item(0);
            if (DevcodeNode.getNodeName().equals("Devcode")) {
                NamedNodeMap nnMap = DevcodeNode.getAttributes();
                Node Value = nnMap.getNamedItem("Value");
                wlci.devcode = Value.getNodeValue();
            }

            NodeList productNodeList = element.getElementsByTagName("Product");

            for(int i = 0; i < productNodeList.getLength(); ++i) {
                Node node = productNodeList.item(i);
                if (node.getNodeName().equals("Product")) {
                    NamedNodeMap nnMap = node.getAttributes();
                    Node typeNode = nnMap.getNamedItem("Type");
                    wlci.product_type[i] = typeNode.getNodeValue();
                    NodeList productChildNodeList = node.getChildNodes();

                    for(int j = 0; j < productChildNodeList.getLength(); ++j) {
                        Node childnode = productChildNodeList.item(j);
                        NamedNodeMap childnnMap;
                        Node childAttributes;
                        Node childAttributesPackageName;
                        if (childnode.getNodeName().equals("Authtype")) {
                            childnnMap = childnode.getAttributes();
                            childAttributes = childnnMap.getNamedItem("Switch");
                            wlci.authtype_switch[i] = childAttributes.getNodeValue();
                            childAttributesPackageName = childnnMap.getNamedItem("Type");
                            wlci.authtype_type[i] = childAttributesPackageName.getNodeValue();
                        } else if (childnode.getNodeName().equals("Devtype")) {
                            childnnMap = childnode.getAttributes();
                            childAttributes = childnnMap.getNamedItem("Switch");
                            wlci.devtype_switch[i] = childAttributes.getNodeValue();
                            childAttributesPackageName = childnnMap.getNamedItem("Type");
                            wlci.devtype_type[i] = childAttributesPackageName.getNodeValue();
                        } else if (childnode.getNodeName().equals("TFMode")) {
                            childnnMap = childnode.getAttributes();
                            childAttributes = childnnMap.getNamedItem("Switch");
                            wlci.tfmode_switch[i] = childAttributes.getNodeValue();
                        } else {
                            Node childAttributesStartDate;
                            if (childnode.getNodeName().equals("MNOMode")) {
                                childnnMap = childnode.getAttributes();
                                childAttributes = childnnMap.getNamedItem("Switch");
                                wlci.mnomode_switch[i] = childAttributes.getNodeValue();
                                childAttributesPackageName = childnnMap.getNamedItem("Deviceid");
                                wlci.mnomode_deviceid[i] = childAttributesPackageName.getNodeValue();
                                childAttributesStartDate = childnnMap.getNamedItem("SIM");
                                wlci.mnomode_sim[i] = childAttributesStartDate.getNodeValue();
                            } else if (childnode.getNodeName().equals("PRJMode")) {
                                childnnMap = childnode.getAttributes();
                                childAttributes = childnnMap.getNamedItem("Switch");
                                wlci.prjmode_switch[i] = childAttributes.getNodeValue();
                                if ("17".equals(wlci.product_type[i])) {
                                    childAttributesPackageName = childnnMap.getNamedItem("templatetype");
                                    wlci.prjmode_templatetype = childAttributesPackageName.getNodeValue();
                                }

                                childAttributesPackageName = childnnMap.getNamedItem("PackageName");
                                wlci.prjmode_packagename[i] = childAttributesPackageName.getNodeValue();
                                childAttributesStartDate = childnnMap.getNamedItem("StartDate");
                                if (childAttributesStartDate != null) {
                                    wlci.prjmode_startdate[i] = childAttributesStartDate.getNodeValue();
                                }

                                Node childAttributesClosingDate = childnnMap.getNamedItem("ClosingDate");
                                wlci.prjmode_closingdate[i] = childAttributesClosingDate.getNodeValue();
                                Node childAttributesVersion = childnnMap.getNamedItem("Version");
                                wlci.prjmode_version[i] = childAttributesVersion.getNodeValue();
                                Node childAttributesAppName = childnnMap.getNamedItem("app_name");
                                wlci.prjmode_app_name[i] = childAttributesAppName.getNodeValue();
                                Node childAttributesCompanyName = childnnMap.getNamedItem("company_name");
                                wlci.prjmode_company_name[i] = childAttributesCompanyName.getNodeValue();
                            }
                        }
                    }
                }
            }

            return wlci;
        } catch (Exception var33) {
            var33.printStackTrace();
            return wlci;
        }
    }

    public ModeAuthFileResult testReadAuthFile(String filePath) {
        ModeAuthFileResult wlci = new ModeAuthFileResult();
        File file = new File(filePath);
        if (file.exists()) {
            String resultString = "";
            String stringLine = "";

            try {
                FileReader fileReader = new FileReader(filePath);
                BufferedReader br = new BufferedReader(fileReader);
                stringLine = br.readLine();

                while(true) {
                    if (stringLine == null) {
                        br.close();
                        fileReader.close();
                        break;
                    }

                    resultString = resultString + stringLine;
                    stringLine = br.readLine();
                }
            } catch (FileNotFoundException var9) {
                return null;
            } catch (IOException var10) {
                return null;
            }

            wlci = this.ReadAuthFile(resultString);
        }

        return wlci;
    }

    public static void main(String[] args) {
    }
}
