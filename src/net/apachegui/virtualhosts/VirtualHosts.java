package net.apachegui.virtualhosts;

import java.util.ArrayList;

import net.apachegui.db.SettingsDao;
import net.apachegui.directives.DocumentRoot;
import net.apachegui.directives.ServerName;
import net.apachegui.global.Constants;
import net.apachegui.modules.SharedModuleHandler;
import net.apachegui.modules.StaticModuleHandler;
import apache.conf.parser.Directive;
import apache.conf.parser.Enclosure;
import apache.conf.parser.EnclosureParser;

public class VirtualHosts {

    public static VirtualHost[] getAllVirtualHosts() throws Exception {

        EnclosureParser parser = new EnclosureParser(SettingsDao.getInstance().getSetting(Constants.confFile), SettingsDao.getInstance().getSetting(Constants.serverRoot),
                StaticModuleHandler.getStaticModules(), SharedModuleHandler.getSharedModules());

        Enclosure virtualHostEnclosures[] = parser.getEnclosure(Constants.virtualHostDirectiveString, true);

        ArrayList<VirtualHost> virtualHosts = new ArrayList<VirtualHost>();

        VirtualHost virtualHost;
        ArrayList<NetworkInfo> networkInfo;
        for (Enclosure virtualHostEnclosure : virtualHostEnclosures) {

            virtualHost = new VirtualHost();

            virtualHost.setEnclosure(virtualHostEnclosure);
           
            networkInfo = new ArrayList<NetworkInfo>();
            String values[] = virtualHostEnclosure.getValue().split(" ");
            for (String value : values) {
                networkInfo.add(extractNetworkInfo(value));
            }

            virtualHost.setNetworkInfo(networkInfo.toArray(new NetworkInfo[networkInfo.size()]));

            for (Directive directive : virtualHostEnclosure.getDirectives()) {
                if (directive.getType().equals(Constants.serverNameDirectiveString)) {
                    virtualHost.setServerName(new ServerName(directive.getValues()[0]));
                } else if (directive.getType().equals(Constants.documentRootDirectiveString)) {
                    virtualHost.setDocumentRoot(new DocumentRoot(directive.getValues()[0]));
                }                 
            }

            virtualHosts.add(virtualHost);
        }

        return virtualHosts.toArray(new VirtualHost[virtualHosts.size()]);
    }

    private static NetworkInfo extractNetworkInfo(String value) {

        NetworkInfo networkInfo = new NetworkInfo();

        // There is no port number
        if (!value.contains(":") || (value.contains("[") && value.contains("]") && !value.contains("]:"))) {

            String address;

            if (value.contains("[") && value.contains("]")) {
                address = value.substring(value.indexOf("[") + 1, value.indexOf("]"));
            } else {
                address = value;
            }

            networkInfo.setAddress(address);

        } else {

            String address;
            int port;

            if (value.contains("[") && value.contains("]")) {
                address = value.substring(value.indexOf("[") + 1, value.indexOf("]"));
                port = Integer.parseInt(value.substring(value.lastIndexOf(":") + 1));

            } else {
                String addressPort[] = value.split(":");
                address = addressPort[0];
                port = Integer.parseInt(addressPort[1]);
            }

            networkInfo.setAddress(address);
            networkInfo.setPort(port);
        }

        return networkInfo;
    }
}
