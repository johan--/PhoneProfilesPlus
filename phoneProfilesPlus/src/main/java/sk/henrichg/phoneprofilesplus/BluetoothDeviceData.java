package sk.henrichg.phoneprofilesplus;

class BluetoothDeviceData {

    String name;
    final String address;
    final int type;
    final boolean custom;
    final long timestamp;

    //BluetoothDeviceData() {
    //}

    BluetoothDeviceData(String name, String address, int type, boolean custom, long timestamp)
    {
        this.name = name;
        this.address = address;
        this.type = type;
        this.custom = custom;
        this.timestamp = timestamp;
    }

    String getName() {
        if (name != null)
            return name;
        else
            return "";
    }

    String getAddress() {
        if (address != null)
            return address;
        else
            return "";
    }

    void setName(String name) {
        this.name = name;
    }

}
