// ICartServiceAidl.aidl
package com.example.common;
parcelable android.os.Bundle;

interface ICartServiceAidl {
     void updateValue(int newValue);
     int getCurrentValue();
}
