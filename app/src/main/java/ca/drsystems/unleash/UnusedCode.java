package ca.drsystems.unleash;

/**
 * Created by SRoddick3160 on 3/11/2015.
 */
public class UnusedCode {
//    private void startRegistration(){
//        Map record = new HashMap();
//        record.put("listenport", String.valueOf(12345));
//        record.put("buddyname", "Unleash" + (int) (Math.random() * 1000));
//        record.put("available", "visible");
//
//        // Service information.  Pass it an instance name, service type
//        // _protocol._transportlayer , and the map containing
//        // information other devices will want once they connect to this one.
//        WifiP2pDnsSdServiceInfo serviceInfo =
//                WifiP2pDnsSdServiceInfo.newInstance("_Unleash", "_presence._tcp", record);
//
//        // Add the local service, sending the service info, network channel,
//        // and listener that will be used to indicate success or failure of
//        // the request.
//        mManager.addLocalService(mChannel, serviceInfo, new WifiP2pManager.ActionListener() {
//            @Override
//            public void onSuccess() {
//                Log.v("P2P", "Added Local Service, starting DiscoverService()");
//
//                discoverService();
//            }
//
//            @Override
//            public void onFailure(int arg0) {
//                Log.v("P2P", "Could not add local service " + arg0);
//            }
//        });
//    }
//
//
//    private void discoverService() {
//
//        WifiP2pManager.DnsSdTxtRecordListener txtListener = new WifiP2pManager.DnsSdTxtRecordListener() {
//            @Override
//        /* Callback includes:
//         * fullDomain: full domain name: e.g "printer._ipp._tcp.local."
//         * record: TXT record dta as a map of key/value pairs.
//         * device: The device running the advertised service.
//         */
//            public void onDnsSdTxtRecordAvailable(
//                    String fullDomain, Map record, WifiP2pDevice device) {
//                buddies.put(device.deviceAddress, record.get("buddyname").toString());
//                Log.d("P2P", "DnsSdTxtRecord available -" + record.toString());
//            }
//        };
//
//        WifiP2pManager.DnsSdServiceResponseListener servListener = new WifiP2pManager.DnsSdServiceResponseListener() {
//            @Override
//            public void onDnsSdServiceAvailable(String instanceName, String registrationType,
//                                                WifiP2pDevice resourceType) {
//
//                // Update the device name with the human-friendly version from
//                // the DnsTxtRecord, assuming one arrived.
////                resourceType.deviceName = buddies
////                        .containsKey(resourceType.deviceAddress) ? buddies
////                        .get(resourceType.deviceAddress) : resourceType.deviceName;
//
//                Log.v("P2P", resourceType.toString());
//
//                if(!peersAvailable.contains(resourceType))
//                    peersAvailable.add(resourceType);
//
//                Log.v("P2P", "DnsSd peerList: " + peersAvailable);
//                numPlayer = (TextView) findViewById(R.id.numPlayers);
//                numPlayer.setText("" + peersAvailable.size());
//                for(WifiP2pDevice device : peersAvailable)
//                {
//                    if(!deviceServiceStarted || deviceServiceStarted && host)
//                        connect(device);
//                }
//
//
//
//                Log.d("P2P", "onDnsSdServiceAvailable " + instanceName);
//            }
//        };
//        mManager.setDnsSdResponseListeners(mChannel, servListener, txtListener);
//
//        Log.v("P2P", "mManager has setDnsSdResponseListeners, starting initializeDiscover()");
//        initializeDiscovery();
//    }

//FOR USE IN ONDESTROY
//    mManager.clearServiceRequests(mChannel, null);
//    mManager.clearLocalServices(mChannel, new ActionListener() {
//        @Override
//        public void onSuccess() {
//            Log.v("P2P", "We cleared the services!");
//        }
//
//        @Override
//        public void onFailure(int reason) {
//            Log.v("P2P", "We didn't clear the services!");
//        }
//    });
}
