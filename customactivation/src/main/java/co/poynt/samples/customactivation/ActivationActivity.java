package co.poynt.samples.customactivation;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.UUID;

import co.poynt.api.model.Address;
import co.poynt.api.model.AddressType;
import co.poynt.api.model.Business;
import co.poynt.api.model.BusinessType;
import co.poynt.api.model.Phone;
import co.poynt.api.model.PhoneType;
import co.poynt.api.model.Processor;
import co.poynt.api.model.Store;
import co.poynt.api.model.StoreDevice;
import co.poynt.api.model.StoreDeviceType;
import co.poynt.api.model.TerritoryType;
import timber.log.Timber;

/**
 * Created by sathyaiyer on 8/6/15.
 */

/**
 * This activity will get business info from the user and
 * will created a business object.
 * Business object will be returned as RETURN_RESULT.
 */
public class ActivationActivity extends Activity {


    private Button cancelButton;
    private Button doneButton;
    private EditText businessName;
    private EditText businessPhone;
    private EditText businessAddress1;
    private EditText businessAddress2;
    private EditText businessCity;
    private EditText businessState;
    private EditText businessZip;
    private Intent originalIntent = null;
    private Intent resultIntent = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        originalIntent = getIntent();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activation_layout);

        businessName = (EditText) findViewById(R.id.business_name);
        businessPhone = (EditText) findViewById(R.id.business_phone);
        businessAddress1 = (EditText) findViewById(R.id.business_address_line1);
        businessAddress2 = (EditText) findViewById(R.id.business_address_line2);
        businessCity = (EditText) findViewById(R.id.business_address_city);
        businessState = (EditText) findViewById(R.id.business_address_state);
        businessZip = (EditText) findViewById(R.id.business_address_code);

        doneButton = (Button) findViewById(R.id.yes);

        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Business business = createBusinessObject();
                if (business != null) {
                    finishWithSuccess(business);
                }
            }
        });

        cancelButton = (Button) findViewById(R.id.no);
        cancelButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        setResult(RESULT_CANCELED, originalIntent);
                        finish();
                    }
                }
        );
    }

    private void sendResult() {

    }

    /**
     * validate the input editText and create a business object.
     * @return
     */
    private Business createBusinessObject() {
        if (validateInput(businessName)
                && validateInput(businessAddress1)
                && validateInput(businessAddress2)
                && validateInput(businessCity)
                && validateInput(businessState)
                && validateInput(businessPhone)
                && validateInput(businessZip)
                ) {

            Business business = new Business();
            // Acquirer is hardcoded to Processor.CHASE_PAYMENTECH
            // check Processor object to see other Processors supported.
            business.setAcquirer(Processor.CHASE_PAYMENTECH);

            // Business address.
            Address address = new Address();
            address.setLine1(businessAddress1.getText().toString());
            address.setCity(businessCity.getText().toString());
            address.setTerritoryType(TerritoryType.STATE);
            address.setTerritory(businessState.getText().toString());
            address.setPostalCode(businessZip.getText().toString());
            address.setType(AddressType.BUSINESS);
            business.setAddress(address);

            // Business Phone
            Phone phone = new Phone();
            phone.setItuCountryCode("1");//hardcoded to USA
            phone.setAreaCode(businessPhone.getText().toString().substring(0,3));
            phone.setLocalPhoneNumber(businessPhone.getText().toString());
            phone.setType(PhoneType.BUSINESS);
            business.setPhone(phone);

            ArrayList<Store> stores = new ArrayList<Store>();
            ArrayList<StoreDevice> storeDevices = new ArrayList<StoreDevice>();

            // create one store object and one store device object.
            Store store = new Store();
            store.setPhone(phone);
            store.setAddress(address);
            store.setDisplayName(businessName.getText().toString());
            store.setExternalStoreId(UUID.randomUUID().toString());

            StoreDevice device = new StoreDevice();
            device.setType(StoreDeviceType.TERMINAL);
            device.setExternalTerminalId(UUID.randomUUID().toString());

            storeDevices.add(device);
            store.setStoreDevices(storeDevices);
            stores.add(store);

            business.setStores(stores);
            business.setDoingBusinessAs(businessName.getText().toString());
            business.setLegalName(businessName.getText().toString());
            // change the business type to MERCHANT for actual device activation.
            business.setType(BusinessType.TEST_MERCHANT);
            // set the MCC that is applicable for this business.
            business.setMcc("7300");
            business.setSic("");
            return business;
        }

        return null;
    }

    /**
     * Validate that edittext is not empty.
     * @param editText
     * @return
     */
    private boolean validateInput(EditText editText) {
        String text = editText.getText().toString();
        if (text == null || (text != null && text.isEmpty())) {
            editText.setHintTextColor(Color.RED);
            return false;
        } else {
            editText.setHintTextColor(Color.WHITE);
            return true;
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Timber.d("onStop");
    }

    private void finishWithError() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                resultIntent = new Intent();
                setResult(RESULT_CANCELED, resultIntent);
                finish();
            }
        });
    }

    /**
     * Send business object to calling activity, which in this case will
     * be OOBE. This business object will be used by OOBE to activate the
     * device with poynt backend server.
     * @param business
     */
    private void finishWithSuccess(final Business business) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                resultIntent = new Intent();
                resultIntent.putExtra("data", business);
                setResult(RESULT_OK, resultIntent);
                finish();
            }
        });
    }
}
